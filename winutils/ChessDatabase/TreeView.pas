unit TreeView;

interface uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics,
  Controls, Forms, Dialogs,
  WorkThread, ComCtrls, StdCtrls, Contnrs;

type
  TCardinalStack = class(TStack)
    procedure Clear;
    function Pop: Cardinal;
    function Push(AItem: Cardinal): Cardinal;
  end;

  TfTreeView = class(TForm)
    ListView1: TListView;
    Button3: TButton;
    ListBox1: TListBox;
    TreeView1: TTreeView;
    procedure FormShow(Sender: TObject);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
    procedure ListView1SelectItem(Sender: TObject; Item: TListItem;
      Selected: Boolean);
    procedure ListView1KeyDown(Sender: TObject; var Key: Word;
      Shift: TShiftState);
  private
    tree: file of TOption;
    FOption: TOption;
    SharedHandle: THandle;
    FTxt: TFileStream;
    Stack: TCardinalStack;
    LastOptionNo: Cardinal;
    procedure ShowOption;
    procedure ReadTree(node: TTreeNode; first: DWORD);
  public
    { Public declarations }
  end;

var
  fTreeView: TfTreeView;

implementation

{$R *.dfm}

procedure TfTreeView.FormClose(Sender: TObject; var Action: TCloseAction);
begin
  UnmapViewOfFile(TOption.Src);
  FTxt.Free;
  CloseFile(tree);
  Stack.Free;
end;

procedure TfTreeView.FormShow(Sender: TObject);
begin
  Stack := TCardinalStack.Create();
  AssignFile(tree, ChangeFileExt(TConvertThread.pgn, '.tree'));
  Reset(tree);
  FTxt := TFileStream.Create(ChangeFileExt(TConvertThread.pgn, '.txt'), fmOpenRead);
  SharedHandle := CreateFileMapping(FTxt.Handle, nil, PAGE_READONLY, 0, FTxt.Size, nil);
  TOption.Src := MapViewOfFile(SharedHandle, FILE_MAP_READ, 0, 0, FTxt.Size);
  ListBox1.Clear;
  Stack.Clear;
  LastOptionNo := 0;

  Read(tree, FOption);
  ShowOption;

  Seek(tree, 0);
  Read(tree, FOption);
  ReadTree(nil, 1);
  TreeView1.FullExpand;
end;

procedure TfTreeView.ListView1KeyDown(Sender: TObject; var Key: Word;
  Shift: TShiftState);
begin
  if Key=VK_BACK then begin
    if Stack.Count>0 then begin
      LastOptionNo := Stack.Pop;
      Seek(tree, LastOptionNo);
      ListBox1.Items.Delete(ListBox1.Items.Count-1);
    end else
      Exit;
    Read(tree, FOption);
    ShowOption;
  end;
end;

procedure TfTreeView.ListView1SelectItem(Sender: TObject; Item: TListItem; Selected: Boolean);
var
  i, j: Cardinal;
  tmp: TOption;
  f: Boolean;
begin
  ListBox1.Items.Add(Item.Caption);
  i := FOption.First;
  while i <> 0 do begin
    Seek(tree, i);
    Read(tree, tmp);
      f := true;
      for j := 0 to MaxInt do begin
        if TOption.Src[tmp.SrcPos+j] in [#10, ' '] then
          Break;
        if TOption.Src[tmp.SrcPos+j]<>AnsiChar(Item.Caption[1+j]) then begin
          f := false;
          Break;
        end;
      end;
    if f then
      Break;
    i := tmp.Next;
  end;
  Assert(i<>0);
  Stack.Push(LastOptionNo);
  LastOptionNo := i;
  FOption := tmp;
  ShowOption;
end;

procedure TfTreeView.ReadTree(node: TTreeNode; first: DWORD);
var
  i, j: Cardinal;
  tmp: TOption;
  s: String;
  child: TTreeNode;
begin
  i := First;

  while i <> 0 do begin
    Seek(tree, i);
    Read(tree, tmp);
    child := TreeView1.Items.AddChild(node, '');
    with child do begin
      s := '';
      for j := 0 to MaxInt do
        if TOption.Src[tmp.SrcPos+j] in [' ', #10] then
          Break
        else
          s := s + Char(TOption.Src[tmp.SrcPos+j]);
      Text := Format('%s %d %d %d', [s, tmp.Results[0], tmp.Results[2], tmp.Results[1]]);
    end;
    if tmp.First <> 0 then
      ReadTree(child, tmp.First)
    else begin
      s := '...';
      for j := 0 to MaxInt do
        if TOption.Src[tmp.SrcPos+j] in [#10] then
          Break
        else
          s := s + Char(TOption.Src[tmp.SrcPos+j]);
      TreeView1.Items.AddChild(node, s);
    end;
    i := tmp.Next;
  end;
end;

procedure TfTreeView.ShowOption;
var
  i, j: Cardinal;
  tmp: TOption;
  s: String;
begin
  ListView1.Clear;
  i := FOption.First;
  if i=0 then begin
    s := '...';
    for j := 0 to MaxInt do
      if TOption.Src[FOption.SrcPos+j] in [#10] then
        Break
      else
        s := s + Char(TOption.Src[FOption.SrcPos+j]);
    MessageDlg(s, mtInformation, [mbok], 0);
  end;

  while i <> 0 do begin
    Seek(tree, i);
    Read(tree, tmp);
    with ListView1.Items.Add do begin
      s := '';
      for j := 0 to MaxInt do
        if TOption.Src[tmp.SrcPos+j] in [' ', #10] then
          Break
        else
          s := s + Char(TOption.Src[tmp.SrcPos+j]);
      Caption := s;
      SubItems.Add(IntToStr(tmp.Results[0]));
      SubItems.Add(IntToStr(tmp.Results[2]));
      SubItems.Add(IntToStr(tmp.Results[1]));
    end;
    i := tmp.Next;
  end;
end;

{ TCardinalStack }

procedure TCardinalStack.Clear;
begin
  List.Clear;
end;

function TCardinalStack.Pop: Cardinal;
begin
  Result := Cardinal(inherited Pop);
end;

function TCardinalStack.Push(AItem: Cardinal): Cardinal;
begin
  Result := Cardinal(inherited Push(pointer(AItem)));
end;

end.
