unit Main;

interface uses
  Winapi.Windows, Winapi.Messages, System.SysUtils, System.Variants, System.Classes,
  Vcl.Graphics, Types, Contnrs, UITypes, Vcl.Samples.Spin,
  Vcl.Controls, Vcl.Forms, Vcl.Dialogs, Vcl.Grids, Vcl.StdCtrls, Vcl.ExtCtrls,
  SimTypes, Pieces, Position, Game, Vcl.ComCtrls;

type
  TForm1 = class(TForm)
    DrawGrid1: TDrawGrid;
    RadioGroup1: TRadioGroup;
    RadioGroup2: TRadioGroup;
    btnNext: TButton;
    btnLoad: TButton;
    Button3: TButton;
    Label1: TLabel;
    btnTest: TButton;
    Button2: TButton;
    lbMoves: TListBox;
    SpinEdit1: TSpinEdit;
    StatusBar1: TStatusBar;
    Timer1: TTimer;
    procedure DrawGrid1DrawCell(Sender: TObject; ACol, ARow: Integer;
      Rect: TRect; State: TGridDrawState);
    procedure DrawGrid1SelectCell(Sender: TObject; ACol, ARow: Integer;
      var CanSelect: Boolean);
    procedure FormCreate(Sender: TObject);
    procedure FormDestroy(Sender: TObject);
    procedure Button1Click(Sender: TObject);
    procedure btnLoadClick(Sender: TObject);
    procedure Button3Click(Sender: TObject);
    procedure btnTestClick(Sender: TObject);
    procedure Button2Click(Sender: TObject);
    procedure RadioGroup1Click(Sender: TObject);
    procedure Timer1Timer(Sender: TObject);
  private
    Game: TGame;
    procedure UpdateUI;
  public
    { Public declarations }
  end;

var
  Form1: TForm1;

implementation

{$R *.dfm}

procedure TForm1.btnLoadClick(Sender: TObject);
begin
  Game.Load;
  UpdateUI;
end;

procedure TForm1.Button1Click(Sender: TObject);
begin
  if not Game.GetNextMove(SpinEdit1.Value) then
    MessageDlg('Game Over', mtInformation, [mbOK], 0);
  UpdateUI;
end;

procedure TForm1.Button2Click(Sender: TObject);
begin
  Close;
end;

procedure TForm1.btnTestClick(Sender: TObject);
var
  dt1, dt2: TDateTime;
begin
  if Sender = btnTest then
    Game.Load;
  dt1 := Now;
  Game.GetNextMove(SpinEdit1.Value);
  dt2 := Now;
  StatusBar1.Panels[0].Text := Format('%3.1f sec', [(dt2-dt1)*86400]);
  StatusBar1.Panels[1].Text := Format('%dM positions', [PositionCount div 1000000]);
  StatusBar1.Panels[2].Text := Format('%dM moves', [GlobalMoveCount div 1000000]);
  if(dt2=dt1) then
    StatusBar1.Panels[3].Text := ''
  else
    StatusBar1.Panels[3].Text := Format('%3.1fM positions/sec', [PositionCount/1000000/((dt2-dt1)*86400)]);
  UpdateUI;
end;

procedure TForm1.Button3Click(Sender: TObject);
begin
  Game.Save;
end;

procedure TForm1.DrawGrid1DrawCell(Sender: TObject; ACol, ARow: Integer;
  Rect: TRect; State: TGridDrawState);
var
  Piece: TPiece;
begin
  if Odd(ACol + ARow) then
    DrawGrid1.Canvas.Brush.Color := $FFFFFF
  else
    DrawGrid1.Canvas.Brush.Color := $CCDDDD;
  DrawGrid1.Canvas.FillRect(Rect);
  if Game.Pieces.GetPieceAt(ACol+1, 8-ARow, Piece) then
    DrawGrid1.Canvas.Draw(Rect.Left, Rect.Top, Piece.Png);
end;

procedure TForm1.DrawGrid1SelectCell(Sender: TObject; ACol, ARow: Integer;
  var CanSelect: Boolean);
var
  Piece: TPiece;
begin
  if Game.Pieces.GetPieceAt(ACol+1, 8-ARow, Piece) then
    Game.Pieces.Remove(Piece)
  else begin
    Piece := Game.CreatePiece(RadioGroup2.ItemIndex);
    Piece.Row := 8-ARow;
    Piece.Col := ACol+1;
    Piece.Side := TSide(RadioGroup1.ItemIndex);
    Game.Pieces.Add(Piece);
  end;
  DrawGrid1.Repaint;
end;

procedure TForm1.FormCreate(Sender: TObject);
var
  i: Integer;
begin
  SpinEdit1.MaxValue := MaxDepth;
  Game := TGame.Create;
  RadioGroup2.Items.Clear;
  for i := Low(PieceClasses) to High(PieceClasses) do
    RadioGroup2.Items.Add(Copy(PieceClasses[i].ClassName, 2, MaxInt));
  RadioGroup2.ItemIndex := 0;
  btnLoad.Click;
end;

procedure TForm1.FormDestroy(Sender: TObject);
begin
  Game.Free;
end;

procedure TForm1.RadioGroup1Click(Sender: TObject);
begin
  Game.Side := tside(RadioGroup1.ItemIndex);
  UpdateUI;
end;

procedure TForm1.Timer1Timer(Sender: TObject);
begin
  Timer1.Enabled := false;
  btnNext.Click;
  Timer1.Enabled := true;
end;

procedure TForm1.UpdateUI;
var
  I: Integer;
begin
  DrawGrid1.Repaint;
  Label1.Caption := SideNames[Game.Side];
  RadioGroup1.ItemIndex := integer(Game.Side);
  lbMoves.Clear;
  for I := 0 to SpinEdit1.Value do begin
    lbMoves.Items.Add(MoveChain[i]);//StringReplace(MoveChain[i], ' ', '', [rfReplaceAll]));
    if lbMoves.Items[i] <> '' then
      lbMoves.Items[i] := lbMoves.Items[i] + ' ' + IntToStr(Game.Value[SpinEdit1.Value - i]);
  end;
end;

end.
