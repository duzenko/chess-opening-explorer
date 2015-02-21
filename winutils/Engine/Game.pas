unit Game;

interface uses
  SysUtils, Classes, Contnrs, StrUtils, PngImage, RTTI, Types, Math,
  Generics.Collections, dialogs, uitypes, Forms,
  SimTypes, Pieces, Position;

type
  TPieceList = class(TObjectList)
  private
    FEnpassent: TPoint;
    function GetItem(Index: ShortInt): TPiece;
    procedure SetItem(Index: ShortInt; const Value: TPiece);
    procedure Load(Reader: TReader);
    procedure Save(Writer: TWriter);
    function GetPosition: TPosition;
  public
    function GetPieceAt(Col, Row: ShortInt; var Piece: TPiece): Boolean;
    procedure Move(const PieceInfo: TPieceInfo);
    property Position: TPosition read GetPosition;
    property Items[Index: ShortInt]: TPiece read GetItem write SetItem; default;
  end;

  TGame = class
  private
    FPieces: TPieceList;
    FSide: TSide;
    FValue: TPositionValue;
    const
      GameFileName = '..\..\res\game.dfm';
  public
    constructor Create; reintroduce;
    destructor Destroy; override;

    procedure Load();
    procedure Save();

    function CreatePiece(id: ShortInt): TPiece;
    function GetNextMove(Depth: Integer): Boolean;
    
    property Pieces: TPieceList read FPieces write FPieces;
    property Side: TSide read FSide write FSide;
    property Value: TPositionValue read FValue;
  end;

implementation

{ TGame }

constructor TGame.Create;
var
  I: ShortInt;
begin
  inherited Create();
  Assert(MaxDepth mod 4 = 3);
  for I := Low(PieceClasses) to High(PieceClasses) do
    RegisterClass(PieceClasses[I]);
  Pieces := TPieceList.Create;
  try
    Load;
  except
  on E: exception do
    MessageDlg(E.Message, mtError, [mbOK], 0);
  end;
end;

function TGame.CreatePiece(id: ShortInt): TPiece;
begin
  Result := PieceClasses[id].Create;
end;

destructor TGame.Destroy;
begin
  Pieces.Free;
  inherited;
end;

function TGame.GetNextMove(Depth: Integer): Boolean;
var
  Position: TPosition;
  PieceInfo: TPieceInfo;
begin
  Screen.Cursor := uitypes.crHourGlass;
  PositionCount := 0;
  GlobalMoveCount := 0;
  CalcDepth := Depth;
  Position := Pieces.GetPosition;
  Result := Position.GetBestMoveFor(Side, PieceInfo, MoveChain);
  if Result then begin
    FSide := Opposite(Side);
    Pieces.Move(PieceInfo);
    FValue := PieceInfo.MoveValue;
  end;
  Screen.Cursor := crDefault;
end;

procedure TGame.Load;
var
  s: TStream;
  Reader: TReader;
begin
  if not FileExists(GameFileName) then
    Exit;
  s := TFileStream.Create(GameFileName, SysUtils.fmOpenRead);
  try
    Reader := TReader.Create(s, 32);
    try
      Pieces.Load(Reader);
      FSide := TSide(Reader.ReadBoolean);
    finally
      Reader.Free;
    end;
  finally
    s.Free;
  end;
end;

procedure TGame.Save;
var
  s: TStream;
  Writer: TWriter;
begin
  s := TFileStream.Create(GameFileName, fmCreate);
  try
    Writer := TWriter.Create(s, 32);
    try
      Pieces.Save(Writer);
      Writer.WriteBoolean(Boolean(Side));
    finally
      Writer.Free;
    end;
  finally
    s.Free;
  end;
end;

{ TPieceList }

function TPieceList.GetItem(Index: ShortInt): TPiece;
begin
  Result := TPiece(inherited GetItem(Index));
end;

function TPieceList.GetPieceAt(Col, Row: ShortInt; var Piece: TPiece): Boolean;
var
  i: ShortInt;
begin
  Result := false;
  for I := 0 to Count-1 do
    if (Items[i].Col = Col) and (Items[i].Row = Row) then begin
      Piece := Items[i];
      Exit(true);
    end;
end;

function TPieceList.GetPosition: TPosition;
var
  i, j: ShortInt;
  Piece: TPiece;
begin
  FillChar(Result, sizeof(Result), 0);
  Result.Depth := 0;
  Result.Value := 0;
  Result.Enpassant := FEnpassent.x in [1..8];
  if Result.Enpassant then begin
    Result.EnpassantField := FEnpassent;
  end;
  for i := 0 to Count-1 do begin
    Result.Pieces[i] := Items[i].Rec;
    Result.PieceFields[i].x := Items[i].Col;
    Result.PieceFields[i].y := Items[i].Row;
  end;
  Result.PieceCount := Count;
  for i := 1 to 8 do
    for j := 1 to 8 do
      if GetPieceAt(j, i, Piece) then begin
        Result.Fields[j, i] := Piece.Rec;
        if Piece.Side = White then
          Inc(Result.Value, Piece.Value)
        else
          Dec(Result.Value, Piece.Value);
      end else
        Result.Fields[j, i].data := 0;
end;

procedure TPieceList.Load(Reader: TReader);
var
  cl: TPersistentClass;
  Piece: TPiece;
  i, cnt: ShortInt;
begin
  Clear;
  with Reader do begin
    cnt := ReadInteger;
    for I := 0 to cnt-1 do begin
      cl := GetClass(ReadString);
      Piece := TPiece(cl.Create);
      Piece.Row := ReadInteger;
      Piece.Col := ReadInteger;
      Piece.Side := TSide(ReadBoolean);
      Add(Piece)
    end;
  end;
end;

procedure TPieceList.Move(const PieceInfo: TPieceInfo);
var
  Piece: TPiece;
begin
  if GetPieceAt(PieceInfo.Field.x + PieceInfo.Move.X,
      PieceInfo.Field.y + PieceInfo.Move.Y, Piece)
  then
    Remove(Piece);
  if (PieceInfo.Piece.GetPieceCode = TPawn.GetCode) and (Abs(PieceInfo.Move.Y) = 2) then
    FEnpassent := PieceInfo.Field
  else
    FEnpassent.x := 0;
  if not GetPieceAt(PieceInfo.Field.x, PieceInfo.Field.y, Piece)
  then
    raise Exception.Create('');
  Inc(Piece.Col, PieceInfo.Move.X);
  Inc(Piece.Row, PieceInfo.Move.Y);
end;

procedure TPieceList.Save(Writer: TWriter);
var
  I: ShortInt;
begin
  with Writer do begin
    WriteInteger(Count);
    for I := 0 to Count-1 do begin
      WriteString(Items[i].ClassName);
      WriteInteger(Items[i].Row);
      WriteInteger(Items[i].Col);
      WriteBoolean(boolean(Items[i].Side));
    end;
  end;
end;

procedure TPieceList.SetItem(Index: ShortInt; const Value: TPiece);
begin
  inherited SetItem(Index, Value);
end;

end.
