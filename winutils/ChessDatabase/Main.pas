unit Main;

interface uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics,
  Vcl.Controls, Vcl.Forms, Vcl.Dialogs, Vcl.StdCtrls, Math,
  WorkThread, Vcl.ExtCtrls;

type
  TForm2 = class(TForm)
    Button1: TButton;
    Button2: TButton;
    Label1: TLabel;
    lLines: TLabel;
    Button3: TButton;
    Timer1: TTimer;
    Label2: TLabel;
    lLps: TLabel;
    Label3: TLabel;
    lTime: TLabel;
    Label4: TLabel;
    lGames: TLabel;
    Label5: TLabel;
    lProgress: TLabel;
    Label6: TLabel;
    lObjects: TLabel;
    Label7: TLabel;
    lMem1: TLabel;
    Label8: TLabel;
    lTotalMoves: TLabel;
    Ratio: TLabel;
    lRatio: TLabel;
    Label9: TLabel;
    lSnapshot: TLabel;
    RadioGroup1: TRadioGroup;
    Button4: TButton;
    procedure Button1Click(Sender: TObject);
    procedure Button2Click(Sender: TObject);
    procedure FormDestroy(Sender: TObject);
    procedure Button3Click(Sender: TObject);
    procedure Timer1Timer(Sender: TObject);
    procedure Button4Click(Sender: TObject);
  private
    Thread: TConvertThread;
    LastLines: Integer;
    dtStart: TDateTime;
    procedure ThreadTerminated(Sender: TObject);
    procedure WMUser(var Message: TMessage); message WM_USER;
  public
    { Public declarations }
  end;

var
  Form2: TForm2;

implementation

{$R *.dfm}

uses TreeView;

procedure TForm2.Button1Click(Sender: TObject);
begin
  LastLines := 0;
  TOption.TotalMoves := 0;
  dtStart := Now;
  Timer1.Enabled := true;
  FreeAndNil(Thread);
  Thread := TConvertThread.Create(true);
  Thread.OnTerminate := ThreadTerminated;
  Thread.Indexing := RadioGroup1.ItemIndex = 0;
  Thread.Start;
  Button1.Enabled := false;
  Button2.Enabled := True;
end;

procedure TForm2.Button2Click(Sender: TObject);
begin
  Thread.Terminate;
end;

procedure TForm2.Button3Click(Sender: TObject);
begin
  Close;
end;

procedure TForm2.Button4Click(Sender: TObject);
begin
  fTreeView.ShowModal;
end;

procedure TForm2.FormDestroy(Sender: TObject);
begin
  Thread.Free;
end;

procedure TForm2.ThreadTerminated(Sender: TObject);
begin
    Button1.Enabled := true;
    Button2.Enabled := false;
    Timer1.Enabled := false;
end;

procedure TForm2.Timer1Timer(Sender: TObject); {$J+}
var
  i: Integer;
  s: string;
  mmState: TMemoryManagerState;
begin
  lLps.Caption := IntToStr((Thread.LinesRead - LastLines) div 1000) + 'K' ;
  lLines.Caption := IntToStr(Thread.LinesRead div 1000000) + 'M';
  lGames.Caption := IntToStr(Thread.GamesRead div 1000) + 'K';
  lTime.Caption := FormatDateTime('nn:ss', Now - dtStart);
  lProgress.Caption := IntToStr(Thread.Progress) + '%';
  lObjects.Caption := IntToStr(TOption.ObjectsCreated div 1000) + 'K';
  GetMemoryManagerState(mmState);
  for I := Low(mmState.SmallBlockTypeStates) to High(mmState.SmallBlockTypeStates) do
    if mmState.SmallBlockTypeStates[i].AllocatedBlockCount >= 10000 then
      s := s
        + IntToStr(mmState.SmallBlockTypeStates[i].InternalBlockSize) + 'x'
        + IntToStr(mmState.SmallBlockTypeStates[i].AllocatedBlockCount div 1000) + '='
        + IntToStr(mmState.SmallBlockTypeStates[i].ReservedAddressSpace div 1000000) + ',';
  lMem1.Caption := s + IntToStr(mmState.ReservedMediumBlockAddressSpace div 1000000) + ','
    + IntToStr(mmState.ReservedLargeBlockAddressSpace div 1000000);
  lTotalMoves.Caption := IntToStr(TOption.TotalMoves div 1000000) + 'M';
  lRatio.Caption := '1 : ' + IntToStr(TOption.TotalMoves div Max(1, TOption.ObjectsCreated));
//  lSnapshot.Caption := Thread.Reused;
  LastLines := Thread.LinesRead;
end;

procedure TForm2.WMUser(var Message: TMessage);
begin
end;

end.
