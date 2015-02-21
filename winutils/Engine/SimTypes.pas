unit SimTypes;

interface uses
  Windows, sysutils;

const
  MaxDepth = 11;

type
  TSide = (White, Black);

  TPoint = packed record
    x, y: ShortInt;
    function Equal(const AX, AY: ShortInt) : Boolean; inline;
  end;
  TMove = TPoint;
  TMoves = array[0..26] of TMove;

  TPieceRec = packed record
    data: ShortInt;
    procedure SetNull; inline;
    function Side: TSide; inline;
    function GetPieceCode: ShortInt; inline;
  end;

  TPositionValue = array[0..MaxDepth] of ShortInt;
  PPositionValue = ^TPositionValue;

  TPieceInfo = record
    MoveValue: TPositionValue;
    Field, Move, Dest: TMove;
    Piece: TPieceRec;
    DestL, FieldL: ShortInt;
    no: ShortInt;
    procedure Setup;
  end;


function Opposite(Side: TSide): TSide;
function CompareValues(v1, v2: PShortInt; Cnt: Integer): Boolean;
procedure memcpy(FromMem, ToMem:Pointer; Size:Integer);
procedure memsetd(v: Cardinal; Mem:Pointer; DSize:Integer);
function memscanw(w: Word; Mem: PWord; Size: Integer; var Index: ShortInt): Boolean;

const
  SideNames: array[TSide] of String = ('white', 'black');

implementation

function Opposite(Side: TSide): TSide;
begin
  Result := TSide(1 - Ord(Side));
end;

function memscanw(w: Word; Mem: PWord; Size: Integer; var Index: ShortInt): Boolean;
begin
  Index := 0;
  repeat
    if Mem^ = w then
      Exit(true);
    Inc(Mem);
    Inc(Index);
  until Index = Size;
  Result := false;
end;

{$IFDEF CPUX64}
function CompareValues(v1, v2: PShortInt; Cnt: Integer): Boolean; inline;
begin                                   
  repeat
    if v1^<>v2^ then
      if v1^<v2^ then
        Exit(false)
      else
        Exit(true);
    inc(v1);
    inc(v2);
    dec(cnt);
  until Cnt<0;
  Result := False;
end;
{$ELSE}
function CompareValues(v1, v2: PShortInt; Cnt: Integer): Boolean;
asm
  push bx
@loop:
  mov bl, [v1];
  cmp bl, [v2];
  je @equal
  jl @false
  mov eax, 1
  pop bx
  ret
@equal:
  inc v1;
  inc v2;
  dec cnt;
  jns @loop
@false:
  mov eax, 0
  pop bx
end;
{$ENDIF}

procedure memcpy(FromMem, ToMem:Pointer; Size:Integer);
asm
{$IFDEF CPUX64}
  push rdi
  push rsi
  mov rsi,FromMem
  mov rdi,ToMem
  mov ecx,Size
  repne movsb
  pop rsi
  pop rdi
{$ELSE}
  push edi
  push esi
  mov esi,FromMem
  mov edi,ToMem
  repne movsb
  pop esi
  pop edi
{$ENDIF}
end;

procedure memsetd(v: Cardinal; Mem:Pointer; DSize:Integer);
asm
{$IFDEF CPUX64}
  push rdi
  mov eax, v
  mov rdi,Mem
  mov ecx, DSize
  shr ecx, 2
  repne stosb
  pop rdi
{$ELSE}
  push edi
  shr ecx, 2
  mov edi,Mem
  repne stosd
  pop edi
{$ENDIF}
end;

{ TPoint }

function TPoint.Equal(const AX, AY: ShortInt): Boolean;
begin
  Result := (AX = x) and (AY = y);
end;

{ TPieceRec }

function TPieceRec.GetPieceCode: ShortInt;
begin
  Result := data and 7;
end;

procedure TPieceRec.SetNull;
begin
  data := 0;
end;

function TPieceRec.Side: TSide;
begin
  Result := TSide(data shr 31);
end;

{ TPieceInfo }

procedure TPieceInfo.Setup;
asm
{$IFDEF CPUX64}
  mov ax, self.Field
  mov dx, ax
  dec dl
  shl dl, 3
  add dl, dh
  mov self.FieldL, dl
  mov dx, self.Move
  add ah, dh
  add dl, al
  mov al, dl
  mov self.Dest, ax
  dec al
  shl al, 3
  add al, ah
  mov self.DestL, al
{$ELSE}
  mov ecx, self
  mov ax, ecx.Field  // two small ints per op
  mov dx, ax
  dec dl
  shl dl, 3
  add dl, dh
  mov ecx.FieldL, dl
  mov dx, ecx.Move   // neg summand will cause carriage if adding both in same reg
  add ah, dh
  add dl, al                  // dh potentially invalid
  mov al, dl
  mov ecx.Dest, ax
  dec al
  shl al, 3
  add al, ah
  mov ecx.DestL, al
{$ENDIF}
end;

end.
