program Chess;
{$IF CompilerVersion >= 21.0}
  {$WEAKLINKRTTI ON}
  {$RTTI EXPLICIT METHODS([]) PROPERTIES([]) FIELDS([])}
{$IFEND}
uses
  Vcl.Forms,
  Main in 'Main.pas' {Form1},
  SimTypes in 'SimTypes.pas',
  Game in 'Game.pas',
  Position in 'Position.pas',
  Pieces in 'Pieces.pas';

{$R *.res}

begin
  Application.Initialize;
  Application.MainFormOnTaskbar := True;
  Application.CreateForm(TForm1, Form1);
  Application.Run;
end.
