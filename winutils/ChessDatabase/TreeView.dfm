object fTreeView: TfTreeView
  Left = 0
  Top = 0
  Caption = 'fTreeView'
  ClientHeight = 469
  ClientWidth = 902
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -12
  Font.Name = 'Tahoma'
  Font.Style = []
  OldCreateOrder = False
  OnClose = FormClose
  OnKeyDown = ListView1KeyDown
  OnShow = FormShow
  DesignSize = (
    902
    469)
  PixelsPerInch = 96
  TextHeight = 14
  object ListView1: TListView
    Left = 8
    Top = 8
    Width = 297
    Height = 452
    Anchors = [akLeft, akTop, akBottom]
    Columns = <
      item
        Caption = 'Move'
        Width = 66
      end
      item
        Caption = 'White'
        Width = 66
      end
      item
        Caption = 'Draw'
        Width = 66
      end
      item
        Caption = 'Black'
        Width = 66
      end>
    RowSelect = True
    TabOrder = 0
    ViewStyle = vsReport
    OnKeyDown = ListView1KeyDown
    OnSelectItem = ListView1SelectItem
  end
  object Button3: TButton
    Left = 794
    Top = 435
    Width = 97
    Height = 25
    Anchors = [akRight, akBottom]
    Cancel = True
    Caption = 'Exit'
    ModalResult = 1
    TabOrder = 1
    ExplicitLeft = 527
  end
  object ListBox1: TListBox
    Left = 311
    Top = 8
    Width = 121
    Height = 233
    ItemHeight = 14
    TabOrder = 2
  end
  object TreeView1: TTreeView
    Left = 438
    Top = 8
    Width = 453
    Height = 421
    Indent = 19
    TabOrder = 3
  end
end
