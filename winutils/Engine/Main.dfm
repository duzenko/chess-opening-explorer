object Form1: TForm1
  Left = 0
  Top = 0
  BorderIcons = [biSystemMenu, biMinimize]
  BorderStyle = bsSingle
  Caption = 'Chess'
  ClientHeight = 548
  ClientWidth = 707
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -12
  Font.Name = 'Tahoma'
  Font.Style = []
  OldCreateOrder = False
  OnCreate = FormCreate
  OnDestroy = FormDestroy
  PixelsPerInch = 96
  TextHeight = 14
  object Label1: TLabel
    Left = 534
    Top = 194
    Width = 33
    Height = 14
    Caption = 'White'
  end
  object DrawGrid1: TDrawGrid
    Left = 8
    Top = 8
    Width = 512
    Height = 512
    BorderStyle = bsNone
    ColCount = 8
    DefaultRowHeight = 64
    DefaultDrawing = False
    FixedCols = 0
    RowCount = 8
    FixedRows = 0
    GridLineWidth = 0
    ScrollBars = ssNone
    TabOrder = 2
    OnDrawCell = DrawGrid1DrawCell
    OnSelectCell = DrawGrid1SelectCell
  end
  object RadioGroup1: TRadioGroup
    Left = 534
    Top = 8
    Width = 161
    Height = 65
    Caption = 'Color'
    Columns = 2
    ItemIndex = 0
    Items.Strings = (
      '&White'
      '&Black')
    TabOrder = 3
    OnClick = RadioGroup1Click
  end
  object RadioGroup2: TRadioGroup
    Left = 534
    Top = 79
    Width = 161
    Height = 105
    Caption = 'Piece'
    Columns = 2
    ItemIndex = 0
    Items.Strings = (
      '&Pawn'
      '&King')
    TabOrder = 4
  end
  object btnNext: TButton
    Left = 612
    Top = 190
    Width = 83
    Height = 25
    Caption = 'Next Move'
    TabOrder = 1
    OnClick = btnTestClick
  end
  object btnLoad: TButton
    Left = 534
    Top = 464
    Width = 75
    Height = 25
    Caption = 'Load'
    TabOrder = 5
    OnClick = btnLoadClick
  end
  object Button3: TButton
    Left = 620
    Top = 464
    Width = 75
    Height = 25
    Caption = 'Save'
    TabOrder = 6
    OnClick = Button3Click
  end
  object btnTest: TButton
    Left = 620
    Top = 375
    Width = 75
    Height = 25
    Caption = '&Test'
    TabOrder = 0
    OnClick = btnTestClick
  end
  object Button2: TButton
    Left = 620
    Top = 495
    Width = 75
    Height = 25
    Cancel = True
    Caption = 'Quit'
    TabOrder = 7
    OnClick = Button2Click
  end
  object lbMoves: TListBox
    Left = 534
    Top = 232
    Width = 161
    Height = 137
    Font.Charset = DEFAULT_CHARSET
    Font.Color = clWindowText
    Font.Height = -12
    Font.Name = 'Courier New'
    Font.Style = []
    ItemHeight = 15
    ParentFont = False
    TabOrder = 8
  end
  object SpinEdit1: TSpinEdit
    Left = 534
    Top = 375
    Width = 80
    Height = 23
    MaxValue = 0
    MinValue = 0
    TabOrder = 9
    Value = 5
  end
  object StatusBar1: TStatusBar
    Left = 0
    Top = 529
    Width = 707
    Height = 19
    Panels = <
      item
        Width = 111
      end
      item
        Width = 111
      end
      item
        Width = 111
      end
      item
        Width = 111
      end>
    ExplicitLeft = 360
    ExplicitTop = 272
    ExplicitWidth = 0
  end
  object Timer1: TTimer
    Interval = 99
    OnTimer = Timer1Timer
    Left = 552
    Top = 408
  end
end
