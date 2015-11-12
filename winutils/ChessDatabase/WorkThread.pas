unit WorkThread; {$J+}

interface uses
  Windows, Messages, Classes, Vcl.Forms, SysUtils, Contnrs, Math, Generics.Collections;

const
  MoveLength = 7;
type
  TIndexEntry = packed record
    Offset: Cardinal;
    Size: Word;
  end;

  PPositionStat = ^TPositionStat;
  TPositionStat = record
    Wins, Loses, Draws: Integer;
  end;

  POption = ^TOption;
  TOption = record
  private
    procedure ParseContinue();
    procedure Init;
    procedure Done;
    function Get(txtPos: Cardinal; var Reused: Boolean): POption;
  public
    SrcPos, First, Next: Cardinal;
    Results: array[0..2] of Cardinal;
  class
    var
      ObjectsCreated, TotalMoves: Integer;
//      Reused: Boolean;
      All: TList<POption>;
      Src: PAnsiChar;
  end;

  TConvertThread = class(TThread)
  private
    FTxt: Text;
    procedure ParseStart(Result: Cardinal; var txtPos: Cardinal);
  protected
    procedure Index;
    procedure Process;
    procedure Execute; override;
  public
    GamesRead, LinesRead, Progress: Integer;
    Indexing: Boolean;
//    Reused: String;
  const
    pgn: string = 'd:\temp\ficsgamesdb_201501_standard2000_nomovetimes_1295906.pgn';//ficsgamesdb_2012_standard_nomovetimes_793102.pgn';
  end;

implementation

function GetFileSizeEx(hFile: THandle; var FileSize: Int64): BOOL; stdcall; external 'kernel32.dll' name 'GetFileSizeEx';

{ TConvertThread }

procedure TConvertThread.Execute;
begin
  NameThreadForDebugging('TConvertThread');
  SetThreadAffinityMask(GetCurrentThread, 4);
  LinesRead := 0;
  GamesRead := 0;
  AssignFile(Input, pgn);
  Reset(Input);
  AssignFile(FTxt, ChangeFileExt(pgn, '.txt'));
  if Indexing then begin
    Rewrite(FTxt);
    Index;
  end else begin
    Reset(FTxt);
    Process;
  end;
  CloseFile(FTxt);
  CloseFile(Input);
  //Root.Done;
end;

procedure TConvertThread.Index;
var
  s, dotless: ansistring;
  fp, fs: Int64;
  InputInfo: ^TTextRec;
  i, k: Integer;
begin
  if not GetFileSizeEx(TTextRec(Input).Handle, fs) then
    RaiseLastOSError;
  InputInfo := @input;
  while not Terminated and not EOF(Input) do begin
    if not SetFilePointerEx(InputInfo.Handle, 0, @fp, FILE_CURRENT) then
      RaiseLastOSError;
    Readln(Input, s);
    Inc(LinesRead);
    if Length(s)=0 then
      Continue;
    if s[1] = '[' then
      Continue;
    Inc(GamesRead);
    if not SetFilePointerEx(InputInfo.Handle, 0, @fp, FILE_CURRENT) then
      RaiseLastOSError;
    Dec(fp, InputInfo.BufSize-InputInfo.BufPos);
    Progress := fp * 100 div fs;

    if length(dotless) < length(s) then
      SetLength(dotless, length(s));
    i := 1;
    k := i;
    while s[i]<>'{' do begin
      dotless[k] := s[i];
      if dotless[k]='.' then begin
        while (k>0) and (dotless[k]<>' ') do
          Dec(k);
        if k>0 then
          Dec(k);
      end;
      Inc(i);
      Inc(k);
    end;
    if k>2 then
      Write(FTxt, s[length(s)], Copy(dotless, 2, k-2), #10);
  end;
end;

procedure TConvertThread.Process;
var
  InputInfo: ^TTextRec;
  GameResult, txtPos: Cardinal;
  fs, fp: Int64;
  tree: file of TOption;
  I: Integer;
var
  SharedHandle: THandle;
  Root: POption;
begin
  fp := 0;
  InputInfo := @ftxt;
  if not GetFileSizeEx(InputInfo.Handle, fs) then
    RaiseLastOSError;
  SharedHandle := CreateFileMapping(InputInfo.Handle, nil, PAGE_READONLY, 0, fs, nil);
  TOption.Src := MapViewOfFile(SharedHandle, FILE_MAP_READ, 0, 0, fs);
  txtPos := 0;
  TOption.All := TList<POption>.Create;
  New(Root);
  Root.Init;
  TOption.All.Add(Root);

  while not Terminated and (txtPos < fs) do begin
    GameResult := Ord(TOption.Src[txtPos]) - Ord('0');
    Inc(txtPos);

    ParseStart(GameResult, txtPos);
    while TOption.Src[txtPos] <> #10 do
      Inc(txtPos);
    Inc(txtPos);

    Inc(LinesRead);
    Inc(GamesRead);

    if not SetFilePointerEx(InputInfo.Handle, 0, @fp, FILE_CURRENT) then
      RaiseLastOSError;
    Inc(fp);
    Progress := Int64(txtPos) * 100 div fs;
  end;

  AssignFile(tree, ChangeFileExt(pgn, '.tree'));
  Rewrite(tree);
  for I := 0 to TOption.All.Count-1 do begin
    Write(tree, TOption.All[i]^);
    TOption.All[i].Done;
    Dispose(TOption.All[i]);
  end;
  FreeAndNil(TOption.All);
  CloseFile(tree);
  UnmapViewOfFile(TOption.Src);
  TOption.All := nil;
end;

procedure TConvertThread.ParseStart(Result: Cardinal; var txtPos: Cardinal);
var
  o: POption;
  Reused: Boolean;
begin
  o := TOption.All[0];
  Reused := false;
  while true do begin
    if Reused then
      if o.First = 0 then
        o.ParseContinue();
    Inc(o.Results[Result]);
    o := o.Get(txtPos, Reused);
    if not Reused then begin
      Inc(o.Results[Result]);
      Break;
    end;
    while TOption.Src[txtPos]<>' ' do
      Inc(txtPos);
    Inc(txtPos);
    if TOption.Src[txtPos] = #10 then
      Break;
  end;
end;

{ TOption }

function TOption.Get(txtPos: Cardinal; var Reused: Boolean): POption;
var
  p: Integer;
  identical: Boolean;
  I: Cardinal;
begin
  Inc(TotalMoves);
  p := First;
  if p<>0 then
    repeat // go through children
      identical := true;
      for I := 0 to MaxInt do begin // compare current and child's moves
        if Src[All[p].SrcPos+i] in [#0, ' '] then
          Break;
        if Src[All[p].SrcPos+i]<>Src[txtPos+i] then begin
          identical := false;
          Break;
        end;
      end;

      if (identical) then begin // return an existing child
        Reused := True;
        Exit(All[p]);
      end;
      
      if All[p].Next = 0 then  // loop break
        Break;
        
      p := All[p].Next; // next loop
    until false;
    
  New(Result); // new child
  Result.Init;
  All.Add(Result);
  Result.SrcPos := txtPos;
  //OutputDebugString(PChar(inttostr(integer(Result) div 1024 div 1024)));
  if First = 0 then
    First := All.Count-1
  else
    All[p].Next := All.Count-1;
  Reused := False;
end;

procedure TOption.Init;
begin
  Inc(ObjectsCreated);
  ZeroMemory(@Self, sizeof(Self));
end;

procedure TOption.Done;
begin
  Dec(ObjectsCreated);
  inherited;
end;

procedure TOption.ParseContinue();
var
  p: Cardinal;
  dummy: Boolean;
begin
  p := SrcPos;
  while Src[p]<>' ' do
    Inc(p);
  Inc(p);
  if Src[p] = #10 then
    Exit;
  with Get(p, dummy)^ do begin
    Results := self.Results;
//    SrcNo := self.SrcNo;
  end;
end;

end.
