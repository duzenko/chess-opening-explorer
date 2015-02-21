program ChessDatabase;

uses
  Classes,
  Vcl.Forms,
  Main in 'Main.pas' {Form2},
  WorkThread in 'WorkThread.pas',
  TreeView in 'TreeView.pas' {fTreeView};

{$R *.res}

begin
  ReportMemoryLeaksOnShutdown := true;
  TThread.NameThreadForDebugging('VCL');
  Application.Initialize;
  Application.MainFormOnTaskbar := True;
  Application.CreateForm(TForm2, Form2);
  Application.CreateForm(TfTreeView, fTreeView);
  Application.Run;
end.
