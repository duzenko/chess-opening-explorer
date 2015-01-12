package name.duzenko.chessopeningexplorer;

class Piece {
	int col, row;
	char kind;
	boolean white;
	
	public Piece(int col, int row, char kind, boolean white) {
		this.col = col;
		this.row = row;
		this.kind = kind;
		this.white = white;
	}
	
	private String getPngAlt() {
		String start=white?"w":"b", ending;
		switch (kind) {
		case 'N':
			ending = "n";
			break;
		case 'B':
			ending = "b";
			break;
		case 'R':
			ending = "r";
			break;
		case 'Q':
			ending = "q";
			break;
		case 'K':
			ending = "k";
			break;
		default:
			ending = "p";
			break;
		}
		return "merida1/" + start + ending + ".png";		
	}
	
	String getPng() {
		if (AppPreferences.alternativeBoard())
			return getPngAlt();
		String start=white?"white":"black", ending;
		switch (kind) {
		case 'N':
			ending = "knight";
			break;
		case 'B':
			ending = "bishop";
			break;
		case 'R':
			ending = "rook";
			break;
		case 'Q':
			ending = "queen";
			break;
		case 'K':
			ending = "king";
			break;
		default:
			ending = "pawn";
			break;
		}
		return "old/" + start + "_chess_" + ending + ".png";
	}
}