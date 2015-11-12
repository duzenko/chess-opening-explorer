object Form2: TForm2
  Left = 0
  Top = 0
  Caption = 'Form2'
  ClientHeight = 300
  ClientWidth = 635
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
    Left = 8
    Top = 8
    Width = 55
    Height = 14
    Caption = 'Lines read'
  end
  object lLines: TLabel
    Left = 109
    Top = 8
    Width = 16
    Height = 14
    Caption = '0M'
  end
  object Label2: TLabel
    Left = 8
    Top = 28
    Width = 92
    Height = 14
    Caption = 'Lines per second'
  end
  object lLps: TLabel
    Left = 109
    Top = 28
    Width = 14
    Height = 14
    Caption = '0K'
  end
  object Label3: TLabel
    Left = 8
    Top = 48
    Width = 72
    Height = 14
    Caption = 'Time elapsed'
  end
  object lTime: TLabel
    Left = 109
    Top = 48
    Width = 32
    Height = 14
    Caption = '00:00'
  end
  object Label4: TLabel
    Left = 8
    Top = 68
    Width = 64
    Height = 14
    Caption = 'Games read'
  end
  object lGames: TLabel
    Left = 109
    Top = 68
    Width = 16
    Height = 14
    Caption = '0M'
  end
  object Label5: TLabel
    Left = 8
    Top = 88
    Width = 78
    Height = 14
    Caption = 'Total progress'
  end
  object lProgress: TLabel
    Left = 109
    Top = 88
    Width = 19
    Height = 14
    Caption = '0%'
  end
  object Label6: TLabel
    Left = 8
    Top = 108
    Width = 88
    Height = 14
    Caption = 'Objects created'
  end
  object lObjects: TLabel
    Left = 109
    Top = 108
    Width = 16
    Height = 14
    Caption = '0M'
  end
  object Label7: TLabel
    Left = 8
    Top = 128
    Width = 80
    Height = 14
    Caption = 'Memory status'
  end
  object lMem1: TLabel
    Left = 109
    Top = 128
    Width = 16
    Height = 14
    Caption = '0M'
  end
  object Label8: TLabel
    Left = 8
    Top = 148
    Width = 67
    Height = 14
    Caption = 'Total moves'
  end
  object lTotalMoves: TLabel
    Left = 109
    Top = 148
    Width = 16
    Height = 14
    Caption = '0M'
  end
  object Ratio: TLabel
    Left = 8
    Top = 168
    Width = 27
    Height = 14
    Caption = 'Ratio'
  end
  object lRatio: TLabel
    Left = 109
    Top = 168
    Width = 16
    Height = 14
    Caption = '0M'
  end
  object Label9: TLabel
    Left = 8
    Top = 188
    Width = 51
    Height = 14
    Caption = 'Snapshot'
  end
  object lSnapshot: TLabel
    Left = 109
    Top = 188
    Width = 16
    Height = 14
    Caption = '0M'
  end
  object Button1: TButton
    Left = 424
    Top = 44
    Width = 97
    Height = 25
    Caption = 'Start'
    TabOrder = 0
    OnClick = Button1Click
  end
  object Button2: TButton
    Left = 527
    Top = 44
    Width = 97
    Height = 25
    Caption = 'Stop'
    Enabled = False
    TabOrder = 1
    OnClick = Button2Click
  end
  object Button3: TButton
    Left = 527
    Top = 264
    Width = 97
    Height = 25
    Cancel = True
    Caption = 'Exit'
    TabOrder = 2
    OnClick = Button3Click
  end
  object RadioGroup1: TRadioGroup
    Left = 424
    Top = 75
    Width = 185
    Height = 82
    Caption = ' Task '
    ItemIndex = 0
    Items.Strings = (
      'Convert to TXT'
      'Build TREE')
    TabOrder = 3
  end
  object Button4: TButton
    Left = 8
    Top = 264
    Width = 97
    Height = 25
    Caption = '&View Tree'
    TabOrder = 4
    OnClick = Button4Click
  end
  object Edit1: TEdit
    Left = 184
    Top = 8
    Width = 393
    Height = 22
    TabOrder = 5
    Text = 'Edit1'
  end
  object Button5: TButton
    Left = 583
    Top = 8
    Width = 41
    Height = 25
    Caption = '...'
    TabOrder = 6
    OnClick = Button5Click
  end
  object Timer1: TTimer
    Enabled = False
    OnTimer = Timer1Timer
    Left = 112
    Top = 217
  end
  object OpenDialog1: TOpenDialog
    DefaultExt = 'pgn'
    Filter = 'PGN files (*.pgn)|*.pgn'
    Options = [ofHideReadOnly, ofFileMustExist, ofEnableSizing]
    Left = 312
    Top = 152
  end
end
