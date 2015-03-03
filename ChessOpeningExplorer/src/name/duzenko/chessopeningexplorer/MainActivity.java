package name.duzenko.chessopeningexplorer;

import java.io.IOException;
import java.io.RandomAccessFile;

import name.duzenko.chessopeningexplorer.chess.Moves;
import name.duzenko.chessopeningexplorer.db.ChessOption;
import name.duzenko.chessopeningexplorer.db.Global;
import name.duzenko.chessopeningexplorer.db.LoaderActivity;
import name.duzenko.chessopeningexplorer.play.PlayActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener {

	TextView textMoves;
	ListView listView;
	MoveAdapter arrayAdapter;
	Moves optionsStack = new Moves();
	
	RandomAccessFile treeStream, txtStream;
	
//	ChessMove lastMove;
	ChessOption chessOption;
	
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
    		if (AppPreferences.hideActionBar())
    			if (ViewConfiguration.get(this).hasPermanentMenuKey())
    				getActionBar().hide();
    			else
    				Toast.makeText(this, "No Menu key found. Option to hide ActionBar ignored", Toast.LENGTH_SHORT).show();
    		else
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
			break;

		case R.id.action_search:
			if (optionsStack.ecoFound == null) {
				Toast.makeText(this, R.string.ecoNotFound, Toast.LENGTH_LONG).show();
				break;
			}
			Uri uri = Uri.parse("http://www.google.com/#q="+(optionsStack.ecoFound[1] + ' ' + optionsStack.ecoFound[2]));
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
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

	String moveSeq = "";
    
    void refresh() {
		((ImageView) findViewById(R.id.imageView)).setImageBitmap(optionsStack.generateImage(moveSeq, getResources(), getAssets()));
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
    	try {
//			chessOption.load(treeStream, chessMove.fileRecNo);
    		if(optionsStack.size()>0)
    			moveSeq = moveSeq + " " + chessMove.move;
    		else
    			moveSeq = chessMove.move;
//    		textMoves.setVisibility(View.VISIBLE);
    		optionsStack.push(chessOption);
    		chessOption = chessMove;
	    	showOption();
		} catch (IOException e) {
			Toast.makeText(this, e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
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
//				textMoves.setVisibility(View.GONE);
			}
			chessOption = optionsStack.pop();
			try {
//				treeStream.seek(ChessOption.recordSize*lastOptionNo);
//				chessOption.load(treeStream, chessOption.fileRecNo);
				showOption();
			} catch (IOException e) {
				Toast.makeText(this, e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}
	
	public void BoardClick(View view) {
		optionsStack.flippedBoard = !optionsStack.flippedBoard;
		refresh();
	}
	
	void startPlay() {
		SharedPreferences settings = getSharedPreferences("", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("startFEN", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		editor.putString("moves", moveSeq);
		editor.putBoolean("flipped", optionsStack.flippedBoard);
		editor.commit();
		Intent intent = new Intent(MainActivity.this, PlayActivity.class);
		startActivity(intent);
	}

}