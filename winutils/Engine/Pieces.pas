unit Pieces;

interface uses
  SysUtils, Classes, PngImage, Generics.Collections,
  Position, SimTypes;

const
  PieceCount = 6;

type
  TPieceClass = class of TPiece;
  TPieceClasses = array[1..PieceCount] of TPieceClass;

  TGetMovesProc = function (Field: TPoint; const Position: TPosition;
    var Result: TMoves): ShortInt;

  TPiece = class(TPersistent)
  private
    FValue: ShortInt;
    FSide: TSide;
    function GetPng: TPngImage;
    procedure SetWhite(const Value: TSide);
    function GetPngName: string;
    class var
      Images: TDictionary<string, TPngImage>;
    function GetRec: TPieceRec;
  protected
  public
    Col, Row: ShortInt;

    class function GetCode: ShortInt; virtual; abstract;
    class function GetClass(PieceRec: TPieceRec): TPieceClass; inline;
    class function GetAbsoluteValue: ShortInt; virtual; abstract;

    property Side: TSide read FSide write SetWhite;
    property Png: TPngImage read GetPng;
    property Value: ShortInt read FValue;
    property Rec: TPieceRec read GetRec;
  end;

  TPawn = class(TPiece)
  public
    procedure AfterConstruction; override;
    class function GetAbsoluteValue: ShortInt; override;
    class function GetCode: ShortInt; override;
  end;

  TKnight = class(TPiece)
  public
    class function GetAbsoluteValue: ShortInt; override;
    class function GetAllMoves(Field: TPoint; const Position: TPosition;
      var Moves: TMoves): ShortInt; static;
    class function GetCode: ShortInt; override;
  end;

  TBishop = class(TPiece)
  public
    class function GetAbsoluteValue: ShortInt; override;
    class function GetAllMoves(Field: TPoint; const Position: TPosition;
      var Moves: TMoves): ShortInt; static;
    class function GetCode: ShortInt; override;
  end;

  TRook = class(TPiece)
  public
    class function GetAbsoluteValue: ShortInt; override;
    class function GetAllMoves(Field: TPoint; const Position: TPosition;
      var Moves: TMoves): ShortInt; static;
    class function GetCode: ShortInt; override;
  end;

  TQueen = class(TPiece)
  public
    class function GetAbsoluteValue: ShortInt; override;
    class function GetAllMoves(Field: TPoint; const Position: TPosition;
      var Moves: TMoves): ShortInt; static;
    class function GetCode: ShortInt; override;
  end;

  TKing = class(TPiece)
  public
    class function GetAbsoluteValue: ShortInt; override;
    class function GetAllMoves(Field: TPoint; const Position: TPosition;
      var Moves: TMoves): ShortInt; static;
    class function GetCode: ShortInt; override;
  end;

function GetWhitePawnMoves(Field: TPoint; const Position: TPosition;
  var Moves: TMoves): ShortInt;
function GetBlackPawnMoves(Field: TPoint; const Position: TPosition;
  var Moves: TMoves): ShortInt;

var
  PawnCode: ShortInt;
  PieceClasses: TPieceClasses = (TPawn, TKnight, TBishop, TRook, TQueen, TKing);
  PieceSymbols: array[1..PieceCount] of Char = (' ', 'N', 'B', 'R', 'Q', 'K');
  GetMoveProcs: array[0..PieceCount] of TGetMovesProc = (
    GetWhitePawnMoves, GetBlackPawnMoves,
    TKnight.GetAllMoves, TBishop.GetAllMoves, TRook.GetAllMoves, TQueen.GetAllMoves, TKing.GetAllMoves
  );

implementation

{ TPiece }

class function TPiece.GetClass(PieceRec: TPieceRec): TPieceClass;
begin
  Result := PieceClasses[PieceRec.GetPieceCode];
end;

function TPiece.GetPng: TPngImage;
begin
  if Images=nil then
    Images := TDictionary<String,TPngImage>.Create;
  if Images.TryGetValue(GetPngName, Result) then
    Exit(Result);
  if Result = nil then begin
    Result := TPngImage.Create;
    Result.LoadFromFile('..\..\res\' + GetPngName);
    Images.Add(GetPngName, Result);
  end;
end;

function TPiece.GetPngName: string;
begin
  Result := SideNames[Side] + '_chess_' + Copy(ClassName, 2, MaxInt) + '.png';
end;

function TPiece.GetRec: TPieceRec;
begin
  Result.data := GetCode;
  if Side = Black then
    Result.data := Result.data or ShortInt($80);
end;

procedure TPiece.SetWhite(const Value: TSide);
begin
  FSide := Value;
  if FSide = White then
    FValue := GetAbsoluteValue
  else
    FValue := GetAbsoluteValue;
end;

{ TPawn }

procedure TPawn.AfterConstruction;
begin
  inherited;
  PawnCode := GetCode;
end;

class function TPawn.GetAbsoluteValue: ShortInt;
begin
  Result := 1;
end;

function GetWhitePawnMoves(Field: TPoint; const Position: TPosition; var Moves: TMoves): ShortInt;
var
  tmp: TPieceRec;
begin
  Result := 0;
    if Position.Fields[Field.x, Field.y+1].data=0 then begin
      Moves[0].X := 0;
      Moves[0].Y := 1;
      Result := 1;
    end;
    if (Field.y=2) and (Position.Fields[Field.x, 3].data=0) and (Position.Fields[Field.x, 4].data=0)
    then begin
      Moves[Result].X := 0;
      Moves[Result].Y := 2;
      Inc(Result);
    end;
    if (Field.x>1) then begin
      tmp := Position.Fields[Field.x-1, Field.y+1];
    if (
      (not tmp.data=0)
        and (tmp.Side = Black)
      or Position.Enpassant and (Position.EnpassantField.Equal(Field.x-1, Field.y+1))
    )
    then begin
      Moves[Result].X := -1;
      Moves[Result].Y := 1;
      Inc(Result);
    end;
    end;
    if (Field.x<8) then begin
        tmp := Position.Fields[Field.x+1, Field.y+1];
    if (
      (tmp.data<>0)
        and (tmp.Side = Black)
      or Position.Enpassant and (Position.EnpassantField.Equal(Field.x+1, Field.y+1))
    )
    then begin
      Moves[Result].X := 1;
      Moves[Result].Y := 1;
      Inc(Result);
    end;
    end;
end;

function GetBlackPawnMoves(Field: TPoint; const Position: TPosition; var Moves: TMoves): ShortInt;
var
  tmp: TPieceRec;
begin
  Result := 0;
    if Position.Fields[Field.x, Field.y-1].data=0 then begin
      Moves[0].X := 0;
      Moves[0].Y := -1;
      Result := 1;
    end;
    if (Field.y=7) and (Position.Fields[Field.x, 6].data = 0) and (Position.Fields[Field.x, 5].data = 0) then begin
      Moves[Result].X := 0;
      Moves[Result].Y := -2;
      Inc(Result);
    end;
    if (Field.x>1) then begin
      tmp := Position.Fields[Field.x-1, Field.y-1];
    if (
      (tmp.data<>0)
        and (tmp.Side = White)
      or Position.Enpassant and (Position.EnpassantField.Equal(Field.x-1, Field.y-1))
    )
    then begin
      Moves[Result].X := -1;
      Moves[Result].Y := -1;
      Inc(Result);
    end;
    end;
    if (Field.x<8) then begin
      tmp := Position.Fields[Field.x+1, Field.y-1];
    if (
      (tmp.data<>0)
        and (tmp.Side = White)
      or Position.Enpassant and (Position.EnpassantField.Equal(Field.x+1, Field.y-1))
    )
    then begin
      Moves[Result].X := 1;
      Moves[Result].Y := -1;
      Inc(Result);
    end;
    end;
end;

class function TPawn.GetCode: ShortInt;
begin
  Result := 1;
end;

{ TKing }

class function TKing.GetAbsoluteValue: ShortInt;
begin
  Result := 0;
end;

class function TKing.GetAllMoves(Field: TPoint; const Position: TPosition;
  var Moves: TMoves): ShortInt;
begin
  Result := 0;
  if (Field.x>1) and (Field.y>1) then begin
    Moves[Result].X := -1;  Moves[Result].Y := -1;
    Inc(Result);
  end;
  if (Field.y>1) then begin
    Moves[Result].X := -0;  Moves[Result].Y := -1;
    Inc(Result);
  end;
  if (Field.x<8) and (Field.y>1) then begin
    Moves[Result].X := +1;  Moves[Result].Y := -1;
    Inc(Result);
  end;
  if (Field.x>1) then begin
    Moves[Result].X := -1;  Moves[Result].Y := -0;
    Inc(Result);
  end;
  if (Field.x<8) then begin
    Moves[Result].X := +1;  Moves[Result].Y := -0;
    Inc(Result);
  end;
  if (Field.x>1) and (Field.y<8) then begin
    Moves[Result].X := -1;  Moves[Result].Y := +1;
    Inc(Result);
  end;
  if (Field.y<8) then begin
    Moves[Result].X := -0;  Moves[Result].Y := +1;
    Inc(Result);
  end;
  if (Field.x<8) and (Field.y<8) then begin
    Moves[Result].X := +1;  Moves[Result].Y := +1;
    Inc(Result);
  end;
end;

class function TKing.GetCode: ShortInt;
begin
  Result := 6;
end;

{ TKnight }

class function TKnight.GetAbsoluteValue: ShortInt;
begin
  Result := 3;
end;

class function TKnight.GetAllMoves(Field: TPoint; const Position: TPosition;
  var Moves: TMoves): ShortInt;
var
  p: PWord;
begin
  p := @moves;
  if (Field.x>1) and (Field.y>2) then begin
    p^ := byte(-1) or byte(-2) shl 8;
    Inc(p);
  end;
  if (Field.x>1) and (Field.y<7) then begin
    p^ := byte(-1) or byte(2) shl 8;
    Inc(p);
  end;
  if (Field.x<8) and (Field.y>2) then begin
    p^ := +1 or byte(-2) shl 8;
    Inc(p);
  end;
  if (Field.x<8) and (Field.y<7) then begin
    p^ := +1 or 2 shl 8;
    Inc(p);
  end;
  if (Field.x<7) and (Field.y>1) then begin
    p^ := +2 or byte(-1) shl 8;
    Inc(p);
  end;
  if (Field.x<7) and (Field.y<8) then begin
    p^ := +2 or 1 shl 8;
    Inc(p);
  end;
  if (Field.x>2) and (Field.y<8) then begin
    p^ := byte(-2) or 1 shl 8;
    Inc(p);
  end;
  if (Field.x>2) and (Field.y>1) then begin
    p^ := byte(-2) or byte(-1) shl 8;
    Inc(p);
  end;
  Result := (integer(p) - integer(@moves)) div 2;
end;

class function TKnight.GetCode: ShortInt;
begin
  Result := 2;
end;

{ TBishop }

class function TBishop.GetAbsoluteValue: ShortInt;
begin
  Result := 3;
end;

class function TBishop.GetAllMoves(Field: TPoint; const Position: TPosition;
  var Moves: TMoves): ShortInt;
var
  j, x, y: ShortInt;
  p: PWord;
begin
  Result := 0;
  p := @Moves;
  j := 1;  x := Field.x-j;  y := Field.y-j;
  while (x>0) and (y>0) do begin
    p^ := byte(-j) + byte(-j) shl 8;
    Inc(Result);
    Inc(p);
    if Position.Fields[x, y].data<>0 then
      Break;
    Inc(j);    Dec(x);    Dec(y);
  end;
  j := 1;  x := Field.x+j;  y := Field.y-j;
  while (x<8) and (y>0) do begin
    p^ := byte(j) + byte(-j) shl 8;
    Inc(p);
    Inc(Result);
    if Position.Fields[x, y].data<>0 then
      Break;
    Inc(j);    Inc(x);    Dec(y);
  end;
  j := 1;  x := Field.x-j;  y := Field.y+j;
  while (x>0) and (y<8) do begin
    p^ := byte(-j) + byte(j) shl 8;
    Inc(p);
    Inc(Result);
    if Position.Fields[x, y].data<>0 then
      Break;
    Inc(j);   Dec(x);    Inc(y);
  end;
  j := 1;  x := Field.x+j;  y := Field.y+j;
  while (Field.x+j<8) and (Field.y+j<8) do begin
    p^ := byte(j) + byte(j) shl 8;
    Inc(p);
    Inc(Result);
    if Position.Fields[x, y].data<>0 then
      Break;
    Inc(j);   Inc(x);   Inc(y);
  end;
end;

class function TBishop.GetCode: ShortInt;
begin
  Result := 3;
end;

{ TRook }

class function TRook.GetAbsoluteValue: ShortInt;
begin
  Result := 5;
end;

class function TRook.GetAllMoves(Field: TPoint; const Position: TPosition;
  var Moves: TMoves): ShortInt;
var
  I: Integer;
  j: ShortInt;
  p: PWord;
begin
  Result := 0;
  p := @Moves[0];
  i := 1;
  j := Field.x-i;
  while (j>0) do begin
    p^ := byte(-i);
    Inc(p);
    Inc(Result);
    if Position.Fields[j, Field.y].data <> 0 then
      Break;
    Inc(i);
    Dec(j);
  end;
  i := 1;
  j := Field.x+i;
  while (j<8) do begin
    p^ := +i;
    Inc(p);
    Inc(Result);
    if Position.Fields[j, Field.y].data <> 0 then
      Break;
    Inc(i);
    Inc(j);
  end;
  i := 1;
  j := Field.y-i;
  while (j>0) do begin
    p^ := -i shl 8;
    Inc(p);
    Inc(Result);
    if Position.Fields[Field.x, j].data <> 0 then
      Break;
    Inc(i);
    Dec(j);
  end;
  i := 1;
  j := Field.y+i;
  while (j<8) do begin
    p^ := i shl 8;
    Inc(p);
    Inc(Result);
    if Position.Fields[Field.x, j].data <> 0 then
      Break;
    Inc(i);
    Inc(j);
  end;
end;

class function TRook.GetCode: ShortInt;
begin
  Result := 4;
end;

{ TQueen }

class function TQueen.GetAbsoluteValue: ShortInt;
begin
  Result := 9;
end;

class function TQueen.GetAllMoves(Field: TPoint; const Position: TPosition;
  var Moves: TMoves): ShortInt;
var
  RookSize: ShortInt;
  RookMoves: TMoves;
begin
  RookSize := TRook.GetAllMoves(Field, Position, RookMoves);
  Result := TBishop.GetAllMoves(Field, Position, Moves);
  Move(RookMoves[0], Moves[Result], RookSize*SizeOf(TMove));
  Inc(Result, RookSize);
end;

class function TQueen.GetCode: ShortInt;
begin
  Result := 5;
end;

end.
