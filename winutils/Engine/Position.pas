unit Position;

interface uses
  SysUtils, RTLConsts, Classes, Math, StrUtils,
  SimTypes;

type
  TMoveChainLink = array[0..3] of char;
  TMoveChain = array[0..MaxDepth] of TMoveChainLink;

  TPosition = record // 166
    function NextPosition(const PieceInfo: TPieceInfo): TPosition;
    function GetBestMoveFor(ASide: TSide; var PieceInfo: TPieceInfo; var ABestMoves: TMoveChain): Boolean;
    case LinearAccess: Boolean of
    False:  (
      Fields: array[1..8, 1..8] of TPieceRec
    );
    True:   (
      LFields: array[1..64] of TPieceRec; // 64
      Enpassant: Boolean;
      Depth, Value, PieceCount: ShortInt;
      EnpassantField: TPoint;
      Pieces: array[0..31] of TPieceRec; // 32
      PieceFields: array[0..31] of TPoint; // 64
    );
  end;

  PMoveNode = ^TMoveNode;
  TMoveNode = record
    MoveDesc: String;
    next: PMoveNode;
  end;

var
  CalcDepth: ShortInt;
  PositionCount, GlobalMoveCount: Cardinal;
  MoveChain: TMoveChain;

implementation uses
  Pieces;

const
  LowestMoveValue = Low(shortInt);
  LowestMoveValueD = $80808080;
  MinPositionValue: TPositionValue = (LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue, LowestMoveValue);
  HighestMoveValue = High(shortInt);

{ TPosition }

function TPosition.GetBestMoveFor(ASide: TSide; var PieceInfo: TPieceInfo; var ABestMoves: TMoveChain): Boolean;
var
  CurPieceInfo: TPieceInfo;

var
  k, i, j, CmpByteCount, MoveCount: ShortInt;
  cc: PChar;
  //Another: TPieceRec;
  Moves: TMoves;
  next: TPosition;
  CurBestMoves: TMoveChain;
  CurSide, Oppo: TSide;
  PieceInfo2: TPieceInfo;
  PieceInfo_MoveValue, cur_0: Pointer;
begin
  Result := false;
  Oppo := Opposite(ASide);
  CmpByteCount := CalcDepth - Depth + 1;
  PieceInfo_MoveValue := @PieceInfo.MoveValue[0];
  cur_0 := @CurPieceInfo.MoveValue[0];
//  memsetd(LowestMoveValueD, @PieceInfo.MoveValue[0], length(PieceInfo.MoveValue));
  PieceInfo.MoveValue := MinPositionValue;
  for j := 0 to PieceCount-1 do begin
    if TSide(byte(Pieces[j].data) shr 7) <> ASide then // less mem writes
      Continue;
    CurSide := TSide(byte(Pieces[j].data) shr 7);
    CurPieceInfo.Piece := Pieces[j];
    CurPieceInfo.no := j;
    CurPieceInfo.Field := PieceFields[j];

    if CurPieceInfo.Piece.data and 7 = PawnCode then
      MoveCount := GetMoveProcs[integer(ASide = Black)](CurPieceInfo.Field, self, Moves)
    else
      MoveCount := GetMoveProcs[CurPieceInfo.Piece.GetPieceCode](CurPieceInfo.Field, self, Moves);

    Inc(GlobalMoveCount, MoveCount);
    for I := 0 to MoveCount-1 do begin
      CurPieceInfo.Move := moves[i];
      CurPieceInfo.Setup;
      if (LFields[CurPieceInfo.DestL].data <> 0) then
//        asm nop end else
        if (LFields[CurPieceInfo.DestL].Side = CurSide) then
          Continue
        else
          if LFields[CurPieceInfo.DestL].GetPieceCode = TKing.GetCode then begin
            PieceInfo := CurPieceInfo;
            PieceInfo.MoveValue[0] := HighestMoveValue;
            Result := true;
            Break;
          end;

      next := NextPosition(CurPieceInfo);
      if next.Depth <= CalcDepth then begin
        if next.GetBestMoveFor(Oppo, PieceInfo2, CurBestMoves) then
          for k := 0 to CalcDepth-next.Depth do
            CurPieceInfo.MoveValue[k] := -PieceInfo2.MoveValue[k]
        else
          CurPieceInfo.MoveValue[0] := HighestMoveValue
      end;
      if ASide = White then
        CurPieceInfo.MoveValue[CmpByteCount-1] := next.Value
      else
        CurPieceInfo.MoveValue[CmpByteCount-1] := -next.Value;

      if CompareValues(cur_0, PieceInfo_MoveValue, CmpByteCount) then
//      asm nop end else
      begin
        PieceInfo := CurPieceInfo;
        memcpy(@CurBestMoves, @ABestMoves, sizeof(ABestMoves));
        Result := true;
      end;
    end;
{    if CheckMate then
      Break;}
  end;

  if not Result then
    raise Exception.Create('bad code');
  cc := ABestMoves[Depth];
  cc^ := PieceSymbols[PieceInfo.Piece.data and 7];
  Inc(cc);
  if Fields[PieceInfo.Field.x + PieceInfo.Move.x, PieceInfo.Field.y + PieceInfo.Move.y].data<>0 then
    cc^ := 'x'
  else
    cc^ := ' ';
  Inc(cc);
  cc^ := Char(Ord('a') + PieceInfo.Field.x + PieceInfo.Move.x - 1);
  Inc(cc);
  cc^ := Char(Ord('0') + PieceInfo.Field.y + PieceInfo.Move.y);
end;

function TPosition.NextPosition(const PieceInfo: TPieceInfo): TPosition;
var
  i: ShortInt;
begin
  Result := Self;
  Result.Enpassant := false;
  Inc(Result.Depth);

  Result.LFields[PieceInfo.DestL] := PieceInfo.Piece;
  Result.LFields[PieceInfo.FieldL].data := 0;
  if LFields[PieceInfo.DestL].data <> 0 then
//    asm nop end else
  begin
    if not memscanw(PWord(@PieceInfo.Dest)^, @PieceFields, Length(PieceFields), i) then
      raise Exception.Create('bug');
    if not PieceFields[i].Equal(PieceInfo.Dest.X, PieceInfo.Dest.Y) then
      raise Exception.Create('bug');
    Result.Pieces[i] := Result.Pieces[PieceCount-1];
    Result.PieceFields[i] := Result.PieceFields[PieceCount-1];
    if LFields[PieceInfo.DestL].Side = White then
      Dec(Result.Value, TPiece.GetClass(LFields[PieceInfo.DestL]).GetAbsoluteValue)
    else
      Inc(Result.Value, TPiece.GetClass(LFields[PieceInfo.DestL]).GetAbsoluteValue);
    Dec(Result.PieceCount);
  end;
  Result.PieceFields[PieceInfo.no] := PieceInfo.Dest;

  if (PieceInfo.Piece.data and 7 = PawnCode) then begin
    if Enpassant and (PWord(@EnpassantField)^=PWord(@PieceInfo.Dest)^) then
      Result.Fields[PieceInfo.Dest.X, PieceInfo.Field.y].SetNull;

    if (Abs(PieceInfo.Move.Y) = 2) then begin
      Result.Enpassant := true;
      Result.EnpassantField.x := PieceInfo.Field.x;
      Result.EnpassantField.y := PieceInfo.Field.y + PieceInfo.Move.Y div 2;
    end;
    if PieceInfo.Dest.Y in [1, 8] then begin
      Inc(Result.Pieces[PieceInfo.no].data, 4);
      Result.LFields[PieceInfo.DestL] := Result.Pieces[PieceInfo.no];
//      raise Exception.Create('New Queen');
    end;
  end;
  Inc(PositionCount);
end;

end.
