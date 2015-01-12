package name.duzenko.chessopeningexplorer;

import java.util.ArrayList;

@SuppressWarnings("serial")
class Pieces extends ArrayList<Piece> {
		
		Piece whiteKing, blackKing, whiteKingRook, whiteQueenRook, blackKingRook, blackQueenRook;
		
		public Pieces() {
			for(int i=1; i<=8; i++)
				add(i, 2, ' ', true);
			for(int i=1; i<=8; i++)
				add(i, 7, ' ', false);
			whiteKingRook = new Piece(8, 1, 'R', true);
			add(whiteKingRook);
			whiteQueenRook = new Piece(1, 1, 'R', true);
			add(whiteQueenRook);
			blackKingRook = new Piece(8, 8, 'R', false);
			add(blackKingRook);
			blackQueenRook = new Piece(1, 8, 'R', false);
			add(blackQueenRook);
			add(2, 1, 'N', true);
			add(7, 1, 'N', true);
			add(2, 8, 'N', false);
			add(7, 8, 'N', false);
			add(3, 1, 'B', true);
			add(6, 1, 'B', true);
			add(3, 8, 'B', false);
			add(6, 8, 'B', false);
			add(4, 1, 'Q', true);
			whiteKing = new Piece(5, 1, 'K', true);
			add(whiteKing);
			add(4, 8, 'Q', false);
			blackKing = new Piece(5, 8, 'K', false); 
			add(blackKing);
		}
		
		void add(int col, int row, char kind, boolean white) {
			add(new Piece(col, row, kind, white));
		}
		
		int getCol(char col) {
			assert(col>='a' && col<='h');
			return col - 'a' + 1;
		}
		
		int getRow(char row) {
			assert(row>='1' && row<='8');
			return row - '1' + 1;
		}
		
		Piece getPiece(int col, int row) {
			for(int i=0; i<size(); i++) {
				if(get(i).col==col && get(i).row==row) 
					return get(i);
			}
			return null;
		}
		
		Piece checkPieceOn(int col, int row) {
			for (int j=0; j<size(); j++)
				if (row == get(j).row && col == get(j).col)
					return get(j);
			return null;
		}
		
		boolean checkPiecesBetween(Piece piece, int col, int row) {
			int tCol = piece.col, tRow = piece.row;
			do {
				if (piece.col < col)
					tCol++;
				if (piece.row < row)
					tRow++;
				if (piece.col > col)
					tCol--;
				if (piece.row > row)
					tRow--;
				if (tCol == col && tRow == row)
					return true;
			} while (checkPieceOn(tCol, tRow) == null);
			return false;
		}
		
		Piece getPiece(int col, int row, int fromCol, int fromRow, char kind, boolean white) {
			for(int i=0; i<size(); i++) {
				Piece piece = get(i);
				if (get(i).kind != kind || get(i).white != white)
					continue;
				if(fromCol!=0 && get(i).col!=fromCol)
					continue;
				if(fromRow!=0 && get(i).row!=fromRow)
					continue;
				switch(kind) {
				case ' ':
					int dr = row-piece.row;
					if(fromCol != 0 && Math.abs(dr) == 1 || get(i).col==col && white ^ dr < 0 && Math.abs(dr) < 3)
						if (checkPiecesBetween(piece, col, row))
							return piece;
				break;
				case 'N':
					if(Math.abs(get(i).col-col) + Math.abs(get(i).row-row) == 3 && get(i).row!=row && get(i).col!=col)
						return get(i);					
					break;
				case 'B':
					if(Math.abs(get(i).col-col) == Math.abs(get(i).row-row))
						if (checkPiecesBetween(piece, col, row))
							return piece;
					break;
				case 'R':
					if(get(i).col==col || get(i).row==row)
						if (checkPiecesBetween(piece, col, row))
							return piece;
					break;
				case 'Q': case 'K':
					return get(i);					
				}
			}
			return null;
		}
		
		void parseMove(String move, boolean white) {
			if (move.equals("c5") && !white)
				System.out.println('"' + move + '"');
			if(move.equals("O-O")) {
				if(white) {
					whiteKing.col = 7;
					whiteKingRook.col = 6;
				} else {
					blackKing.col = 7;
					blackKingRook.col = 6;
				}
				return;
			}
			if(move.equals("O-O-O")) {
				if(white) {
					whiteKing.col = 3;
					whiteQueenRook.col = 4;					
				} else {
					blackKing.col = 3;
					blackQueenRook.col = 4;					
				}
				return;
			}
			char ck = move.charAt(0);
			if(ck>='a' && ck<='h') { 
				move = ' ' + move;
				ck = ' ';
			}
			if (move.endsWith("+") || move.endsWith("#"))
				move = move.substring(0, move.length()-1);
			int col = getCol(move.charAt(move.length()-2)), row = getRow(move.charAt(move.length()-1)), fromCol = 0, fromRow = 0;
			move = move.replace("x", "");
			if(move.length()>3)
				if(move.charAt(1)>='a')
					fromCol = getCol(move.charAt(1));
				else
					fromRow = getRow(move.charAt(1));
//			boolean capture = move.indexOf('x') >= 0;
			Piece piece = null;
			piece = getPiece(col, row, fromCol, fromRow, ck, white);
			if(piece==null)
				System.out.println(move + " null " + col + " " + row + " " + fromCol + " " + fromRow);
			else {
				Piece beaten = getPiece(col, row);
				if(beaten!=null) {
					//System.out.println("piece captured");
					remove(beaten);
				}
				//System.out.println(piece.col + " " + piece.row + " " + col + " " + row);
				piece.col = col;
				piece.row = row;
			}
		}
	}