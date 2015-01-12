package name.duzenko.chessopeningexplorer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import android.content.res.AssetManager;
import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

@SuppressWarnings("serial")
public class Moves extends Stack<Integer> {
	
	HashMap<String, Bitmap> pieceImages = new HashMap<String, Bitmap>();
	Bitmap chessBoard, output;
	float cellSize;
	RectF dstPiece = new RectF();
	
	StringBuilder movesWithNumbers = new StringBuilder();
	String eco[][], ecoFound[];
	Paint paint = new Paint();
	
	Bitmap generateImage(String moveString, Resources res, AssetManager assetManager) {
		movesWithNumbers.setLength(0);
		Pieces pieces = new Pieces();
		if(moveString.length()>0) {
			String moves[] = moveString.split(" ");
			boolean white = true;
			for(int i=0; i<moves.length; i++) {
				if (i%2==0)
					movesWithNumbers.append(i / 2 + 1).append('.');
				movesWithNumbers.append(moves[i]);
				if (i < moves.length - 1)
					movesWithNumbers.append(' ');
				pieces.parseMove(moves[i], white);
				white = !white;
			}
		}
		
		int borderWidth = AppPreferences.alternativeBoard()?0:8; 
		if(chessBoard==null) {
			System.out.println("Loading the chess board...");
			if (AppPreferences.alternativeBoard())
				chessBoard = BitmapFactory.decodeResource(res, R.drawable.original2);
			else
				chessBoard = BitmapFactory.decodeResource(res, R.drawable.chess1);
			System.out.println("chessboard " + chessBoard.getWidth() + " " + chessBoard.getHeight());
			output = chessBoard.copy(android.graphics.Bitmap.Config.ARGB_8888, true);
			cellSize = (chessBoard.getWidth() - 2*borderWidth) / 8f;
		}
		
		getOpeningName(assetManager);
		
        Canvas canvas = new Canvas(output);
        if (flippedBoard) {
        	Matrix mx = new Matrix();
        	mx.setRotate(180, chessBoard.getWidth()/2, chessBoard.getHeight()/2);
        	canvas.drawBitmap(chessBoard, mx, null);
        } else
        	canvas.drawBitmap(chessBoard, 0, 0, null);
        paint.setTextSize(24);
        for(char c=1; c<=8; c++) {
        	canvas.drawText(Character.toString((char) (!flippedBoard?'a'+c-1:'h'-c+1)), borderWidth + (c-0.2f)*cellSize, borderWidth + 17, paint);
        	canvas.drawText(Character.toString((char) (!flippedBoard?'8'-c+1:'1'+c-1)), borderWidth + 1, borderWidth + (c-0.1f)*cellSize, paint);
        }
        for(int i=0; i<pieces.size(); i++) {
        	Piece piece = pieces.get(i);
        	Bitmap pieceBitmap = pieceImages.get(piece.getPng());
        	if (pieceBitmap==null) {
        		try {
					pieceBitmap = BitmapFactory.decodeStream(assetManager.open("pieces/" + piece.getPng()));
					pieceImages.put(piece.getPng(), pieceBitmap);
				} catch (IOException e) {
					e.printStackTrace();
				}        		
        	}
        	int x = flippedBoard ? 8-piece.col : piece.col-1, y = flippedBoard ? piece.row-1 : 8-piece.row;
        	dstPiece.left = borderWidth + x * cellSize; 
        	dstPiece.right = borderWidth + (x+1) * cellSize; 
        	dstPiece.top = borderWidth + y * cellSize; 
        	dstPiece.bottom = borderWidth + (y+1) * cellSize; 
            canvas.drawBitmap(pieceBitmap, null, dstPiece, null);
        }
		return output;
	}

	void getOpeningName(AssetManager assetManager) {
		if (eco==null) 
			try {
				AssetInputStream is = (AssetInputStream) assetManager.open("eco.csv");
				System.out.println(is.getClass().getName());
				byte[] buffer = new byte[is.available()];
				is.read(buffer);
				String []eco3 = new String(buffer).split("\r\n");
				eco = new String[eco3.length][];
				for (int i = 0; i < eco3.length; i++) 
					eco[i] = eco3[i].split(";");
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		
		ecoFound = null;
		if (movesWithNumbers.length() == 0) 
			return;
		int ecoNo = -1, ecoPartIndex = -1, ecoPartStrength = 0;
		String withNumbers = movesWithNumbers.toString();
		for (int i = 0; i < eco.length; i++) {
			if (eco[i][0].equals(withNumbers)) {
				ecoNo = i;
				break;
			}
			if (withNumbers.startsWith(eco[i][0])) {
				int s = indexOfDifference(eco[i][0], withNumbers);
				if (ecoPartStrength < s) {
					ecoPartIndex = i;
					ecoPartStrength = s;
				}
			}
		}
		/*if (ecoNo<0)
			for (int i = eco.length-1; i>0; i--) 
				if (withNumbers.startsWith(eco[i-1].substring(0, eco[i-1].indexOf(';')-1))) {
					ecoNo = i-1;
					break;
				}

		if (ecoNo<0)
			for (int i = eco.length-1; i>0; i--) 
				if (eco[i].startsWith(withNumbers)) {
					ecoNo = i;
					break;
				}*/
		if (ecoNo < 0)
			ecoNo = ecoPartIndex;
		if (ecoNo >= 0) 
			ecoFound = eco[ecoNo];
	}
	
	public static int indexOfDifference(CharSequence cs1, CharSequence cs2) {
	    int i;
	    for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
	        if (cs1.charAt(i) != cs2.charAt(i)) {
	            break;
	        }
	    }
	    return i;
	}

	boolean flippedBoard;
}