package name.duzenko.chessopeningexplorer;

import java.util.ArrayList;
import java.util.List;

import chess.ChessParseError;
import chess.Move;
import chess.Position;
import chess.TextIO;
import guibase.ChessController;
import guibase.GUIInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class PlayActivity extends Activity implements GUIInterface {
	
	private static final int ttLogSize = 10;
	protected static final int PROMOTE_DIALOG = 0, CLIPBOARD_DIALOG = 1;
	private boolean playerWhite;
	ChessController ctrl;
	ChessBoard cb;
	TextView status, moveList, thinking;
	static Typeface chessFont;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		cb = (ChessBoard)findViewById(R.id.chessboard);
		status = (TextView)findViewById(R.id.textStatus);
		thinking = (TextView)findViewById(R.id.textThinking);
		moveList = (TextView)findViewById(R.id.textMoves);
		new Loader().execute();
        cb.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (ctrl.humansTurn() && (event.getAction() == MotionEvent.ACTION_UP)) {
                    int sq = cb.eventToSquare(event);
                    Move m = cb.mousePressed(sq);
                    if (m != null) {
                        ctrl.humanMove(m);
                    }
                    return false;
                }
                return false;
            }
        });
        
        cb.setOnTrackballListener(new ChessBoard.OnTrackballListener() {
            public void onTrackballEvent(MotionEvent event) {
                if (ctrl.humansTurn()) {
                    Move m = cb.handleTrackballEvent(event);
                    if (m != null) {
                        ctrl.humanMove(m);
                    }
                }
            }
        });
        cb.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!ctrl.computerThinking())
                    showDialog(CLIPBOARD_DIALOG);
                return true;
            }
        });
	}
	
	class Loader extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new ProgressDialog(PlayActivity.this);
			dialog.setTitle(R.string.loadingCuckoo);
			dialog.show();
			dialog.setCancelable(false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (chessFont == null)
				chessFont = Typeface.createFromAsset(getAssets(), "casefont.ttf");
            SharedPreferences settings = getSharedPreferences("", MODE_PRIVATE);
            playerWhite = !settings.getBoolean("flipped", false);
	        ctrl = new ChessController(PlayActivity.this);
	        ctrl.newGame(playerWhite, ttLogSize, false);
	        {
	            String fen = "";
	            String moves = "";
	            String numUndo = "0";
	            String tmp;
	            tmp = settings.getString("startFEN", null);
	            if (tmp != null) fen = tmp;
	            tmp = settings.getString("moves", null);
	            if (tmp != null) moves = tmp;
	            tmp = settings.getString("numUndo", null);
	            if (tmp != null) numUndo = tmp;
	            List<String> posHistStr = new ArrayList<String>();
	            posHistStr.add(fen);
	            posHistStr.add(moves);
	            posHistStr.add(numUndo);
	            ctrl.setPosHistory(posHistStr);
	        }
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
	        cb.setFont(chessFont);					
	        ctrl.startGame();
	        dialog.cancel();
			super.onPostExecute(result);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		case R.id.action_about:
			Uri uri = Uri.parse("http://web.comhem.se/petero2home/javachess/");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
        case R.id.action_undo:
            ctrl.takeBackMove();
            return true;
 		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setPosition(Position pos) {
        cb.setPosition(pos);
        ctrl.setHumanWhite(playerWhite);
	}

	@Override
	public void setSelection(int sq) {
        cb.setSelection(sq);
	}

	@Override
	public void setStatusString(String str) {
        status.setText(str);
	}

	@Override
	public void setMoveListString(String str) {
        moveList.setText(str);
//        moveListScroll.fullScroll(ScrollView.FOCUS_DOWN);
	}

	@Override
	public void setThinkingString(String str) {
        thinking.setText(str);
	}

	@Override
	public int timeLimit() {
		return 5000;
	}

	@Override
	public boolean randomMode() {
		return false;
	}

	@Override
	public boolean showThinking() {
		return true;
	}

	@Override
	public void requestPromotePiece() {
        runOnUIThread(new Runnable() {
			public void run() {
                showDialog(PROMOTE_DIALOG);
            }
        });
	}

	@Override
	public void runOnUIThread(Runnable runnable) {
        runOnUiThread(runnable);
	}

	@Override
	public void reportInvalidMove(Move m) {
        String msg = String.format("Invalid move %s-%s", TextIO.squareToString(m.from), TextIO.squareToString(m.to));
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case PROMOTE_DIALOG: {
            final CharSequence[] items = {"Queen", "Rook", "Bishop", "Knight"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Promote pawn to?");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    ctrl.reportPromotePiece(item);
                }
            });
            AlertDialog alert = builder.create();
            return alert;
        }
        case CLIPBOARD_DIALOG: {
            final CharSequence[] items = {"Copy Game", "Copy Position", "Paste"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Clipboard");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                    case 0: {
                        String pgn = ctrl.getPGN();
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                        clipboard.setText(pgn);
                        break;
                    }
                    case 1: {
                        String fen = ctrl.getFEN() + "\n";
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                        clipboard.setText(fen);
                        break;
                    }
                    case 2: {
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                        if (clipboard.hasText()) {
                            String fenPgn = clipboard.getText().toString();
                            try {
                                ctrl.setFENOrPGN(fenPgn);
                            } catch (ChessParseError e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                    }
                }
            });
            AlertDialog alert = builder.create();
            return alert;
        }
        }
        return null;
    }

}
