package name.duzenko.chessopeningexplorer;

import guibase.ChessController;
import guibase.GUIInterface;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import chess.Move;
import chess.Position;
import chess.TextIO;

import name.duzenko.chessopeningexplorer.chess.Moves;
import name.duzenko.chessopeningexplorer.db.ChessOption;
import name.duzenko.chessopeningexplorer.db.Global;
import name.duzenko.chessopeningexplorer.db.LoaderActivity;
import name.duzenko.chessopeningexplorer.play.ChessBoard;
import name.duzenko.chessopeningexplorer.play.Loader;
import name.duzenko.chessopeningexplorer.play.PlayActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener, GUIInterface {

	TextView textMoves;
	ListView listView;
	MoveAdapter arrayAdapter;
	Moves optionsStack = new Moves();
	
	RandomAccessFile treeStream, txtStream;
	
//	ChessMove lastMove;
	ChessOption chessOption;
	
	ChessController ctrl;
	ChessBoard cb;
	List<String> posHistStr;
	
	private OnTouchListener cbTouch = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			System.out.println(event.getAction());
            if ((event.getAction() == MotionEvent.ACTION_DOWN)) {
                int sq = cb.eventToSquare(event);
                Move m = cb.mousePressed(sq);
                if (m != null) {
                	String s = TextIO.moveToString(cb.getPosition(), m, false);
                	ChessOption option = arrayAdapter.find(s);
                	if (option == null)
                		Toast.makeText(MainActivity.this, String.format(getString(R.string.move_not_in_list), s), Toast.LENGTH_LONG).show();
                	else {
                		selectMove(option);
                		loadController();                		
                	}
                }
                return true; 
            }
            return false; // allow long touch
		}
		
	};
	
	OnLongClickListener cbLongTouch = new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			System.out.println("long touch detected");
			openOptionsMenu();
			return false;
		}
	};

	@Override 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppPreferences.preferences = getSharedPreferences(null, MODE_PRIVATE);
        textMoves = (TextView) findViewById(R.id.textMoves);
        arrayAdapter = new MoveAdapter(this, 0);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(this);

    	if (Global.dbTreeFile == null || Global.dbTxtFile == null) {
    		finish();
    		startActivity(new Intent(this, LoaderActivity.class));
    		return;
    	}
        String error;
		try {
			error = load();
		} catch (IOException e) {
			error = e.getClass().getSimpleName();
			e.printStackTrace();
		}
    	if(error!=null) {
    		Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    		finish();
    		return;
    	}
    	cb = (ChessBoard)findViewById(R.id.chessboard);
    	ctrl = new ChessController(this);
        cb.setFlipped(false);
		new Loader(this){
			@Override
			protected Void doInBackground(Void... params) {
		        ctrl = new ChessController(MainActivity.this);
		        ctrl.newGame(true, PlayActivity.ttLogSize, false);
		        super.doInBackground(params);
		        MainActivity.this.posHistStr = posHistStr;
	            ctrl.setPosHistory(posHistStr);
	            return null;
			}
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				String s = Global.getMoves();
				if(s.length()>0) {
					String moves[] = Global.getMoves().split(" ");
					for (int i = 0; i < moves.length; i++) {
						ChessOption chessMove = arrayAdapter.find(moves[i]);
						selectMove(chessMove);
					}
				}
				loadController();
			};
		}.execute();
    	cb.setOnTouchListener(cbTouch);
    	cb.setOnLongClickListener(cbLongTouch);
    }
    
    String load() throws IOException {
    	txtStream = new RandomAccessFile(Global.dbTxtFile, "r");
    	treeStream = new RandomAccessFile(Global.dbTreeFile, "r");
    	reset();
    	return null;
    }
    
    void unload() throws IOException {
    	if(treeStream!=null)
    		treeStream.close();
    	treeStream = null;
    	if(txtStream!=null)
    		txtStream.close();
    	txtStream = null;
    }
    
	@SuppressLint("NewApi")
	@Override
    protected void onResume() {
    	super.onResume();
    	if (android.os.Build.VERSION.SDK_INT >= 11)
    		if (AppPreferences.hideActionBar()) {
    			getActionBar().hide();
	    		if (!ViewConfiguration.get(this).hasPermanentMenuKey())
	    			Toast.makeText(this, "No Menu key found. Use long click on the chess board to popup the menu", Toast.LENGTH_SHORT).show();
    		} else
    			getActionBar().show();
    	refresh();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	try {
			unload();
		} catch (IOException e) {
    		Toast.makeText(this, e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
		}
    	super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
	    	optionsStack.chessBoard = null;
	    	optionsStack.pieceImages.clear();
			break;

		case R.id.action_play:
			startPlay();
			break;

		case R.id.action_reset:
			reset();
			loadController();
			break;

		case R.id.action_search:
			if (optionsStack.ecoFound == null) {
				Toast.makeText(this, R.string.ecoNotFound, Toast.LENGTH_LONG).show();
				break;
			}
			Uri uri = Uri.parse("http://www.google.com/#q="+(optionsStack.ecoFound[1] + ' ' + optionsStack.ecoFound[2]));
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;

		case R.id.action_flip:
			cb.setFlipped(!cb.getFlipped());
			break;
			
		default:
			break;
		}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    private void reset() {
    	optionsStack.clear();
    	chessOption = new ChessOption(0, 0);
		moveSeq = "";
		try {
	    	chessOption.load(treeStream);
			showOption();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		textMoves.setVisibility(View.GONE);
	}
    
    void loadController() {
		String s = Global.getMoves();
		boolean b = true;
		if(s.length()>0) { 
			String moves[] = Global.getMoves().split(" ");
			b = moves.length % 2 == 0;
		} 
    	posHistStr.set(1, moveSeq);
		ctrl.setPosHistory(posHistStr);
		ctrl.setHumanWhite(b);
		System.out.println(b + " " + s);
        ctrl.startGame(); 
    }
    
    void selectMove(ChessOption chessMove) {
    	try {
    		if(optionsStack.size()>0)
    			moveSeq = moveSeq + " " + chessMove.move;
    		else
    			moveSeq = chessMove.move;
    		Global.setMoves(moveSeq);
    		optionsStack.push(chessOption);
    		chessOption = chessMove;
	    	showOption();
		} catch (IOException e) {
			Toast.makeText(this, e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}    	
    }

	String moveSeq = "";
    
    void refresh() {
//		((ImageView) findViewById(R.id.imageView)).setImageBitmap(
    	optionsStack.generateImage(moveSeq, getResources(), getAssets());
		textMoves.setText((optionsStack.ecoFound != null ? (optionsStack.ecoFound[1] + ' ' + optionsStack.ecoFound[2] + '\n') : "") + optionsStack.movesWithNumbers);
    }
    
    int readMove(int idx, int offset) throws IOException {
		ChessOption tmp = new ChessOption(idx, offset);
		tmp.load(treeStream);
//		ChessMove chessMove = new ChessMove();
		txtStream.seek(tmp.TxtPos + tmp.TxtOffset);
		while(true) {
			char c = (char) txtStream.readByte();
			if(c==' ' || c==10)
				break;
			tmp.move += c; 
		}
//		System.arraycopy(tmp.Results, 0, chessMove.stat, 0, tmp.Results.length);
		if(tmp.move.length() > 0)
			arrayAdapter.add(tmp);    
		return tmp.Next;
    }
    
    void showOption() throws IOException {
//    	System.out.println(chessOption.move);
    	refresh();
    	arrayAdapter.clear();
    	int idx = chessOption.First;
    	if(idx==0) {
    		txtStream.seek(chessOption.TxtPos + chessOption.TxtOffset);
    		char c;
    		int offset = 0;
    		while(true) {
    			c = (char) txtStream.readByte();
    			offset++;
    			if(c==' ' || c==10)
    				break;
    		}
    		if(c==' ')
    			readMove(chessOption.fileRecNo, chessOption.TxtOffset + offset);
    	}
    	else {
    		while (idx!=0) {
    			idx = readMove(idx, 0);
    		}
    	}
    	arrayAdapter.sort();
    	((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ChessOption chessMove = arrayAdapter.getItem(position);
		selectMove(chessMove);
		loadController();
	}
	
	@Override
	public void onBackPressed() {
		if(optionsStack.size()<=0)
			super.onBackPressed();
		else {
			String s = moveSeq;
			int lastSpace = s.lastIndexOf(' ');
			if(lastSpace>=0)
				moveSeq = s.substring(0, lastSpace);
			else {
				moveSeq = "";
			}
    		Global.setMoves(moveSeq);
			chessOption = optionsStack.pop(); 
			try {
				showOption();
			} catch (IOException e) {
				Toast.makeText(this, e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			loadController();
		}
	}
	
	public void MovesClick(View view) {
		if(listView.getAdapter().getCount() > 0)
			onItemClick(listView, listView, 0, 0);
	}
	
	
	void startPlay() {
		SharedPreferences settings = getSharedPreferences("", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
//		editor.putString("startFEN", );
		editor.putBoolean("flipped", cb.getFlipped());
		editor.commit();
		Intent intent = new Intent(MainActivity.this, PlayActivity.class);
		startActivity(intent);
	}

	@Override
	public void setPosition(Position pos) {
		cb.setPosition(pos);
	}

	@Override
	public void setSelection(int sq) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStatusString(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMoveListString(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setThinkingString(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int timeLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean randomMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean showThinking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void requestPromotePiece() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runOnUIThread(Runnable runnable) {
		// TODO Auto-generated method stub
		this.runOnUiThread(runnable);
	}

	@Override
	public void reportInvalidMove(Move m) {
		// TODO Auto-generated method stub
		
	}

}