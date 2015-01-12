package name.duzenko.chessopeningexplorer;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
	
	int lastOptionNo;
	ChessOption chessOption = new ChessOption();
	
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
    	optionsStack.clear();
    	lastOptionNo = 0;
    	txtStream = new RandomAccessFile(Global.dbTxtFile, "r");
    	treeStream = new RandomAccessFile(Global.dbTreeFile, "r");
    	
    	chessOption.load(treeStream);
    	
    	showOption();
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
    
    @Override
    protected void onResume() {
    	super.onResume();
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
    
    String moveSeq = "";
    
    void refresh() {
		((ImageView) findViewById(R.id.imageView)).setImageBitmap(optionsStack.generateImage(moveSeq, getResources(), getAssets()));
		textMoves.setText((optionsStack.ecoFound != null ? (optionsStack.ecoFound[1] + ' ' + optionsStack.ecoFound[2] + '\n') : "") + optionsStack.movesWithNumbers);
    }
    
    void showOption() throws IOException {
    	refresh();
    	ChessOption tmp = new ChessOption();
    	arrayAdapter.clear();
    	int i = chessOption.First;
    	while (i!=0) {
    		ChessMove chessMove = new ChessMove();
    		chessMove.fileRecNo = i;
    		treeStream.seek(i*ChessOption.recordSize);
    		tmp.load(treeStream);
    		txtStream.seek(tmp.SrcPos);
    		while(true) {
    			char c = (char) txtStream.readByte();
    			if(c==' ' || c==10)
    				break;
    			chessMove.move += c; 
    		}
    		System.arraycopy(tmp.Results, 0, chessMove.stat, 0, tmp.Results.length);
    		arrayAdapter.add(chessMove);
    		i = tmp.Next;
    	}
    	arrayAdapter.sort();
    	((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ChessMove chessMove = arrayAdapter.getItem(position);
    	try {
    		if(optionsStack.size()>0)
    			moveSeq = moveSeq + " " + chessMove.move;
    		else
    			moveSeq = chessMove.move;
    		textMoves.setVisibility(View.VISIBLE);
    		optionsStack.push(lastOptionNo);
    		lastOptionNo = chessMove.fileRecNo;
    		treeStream.seek(chessMove.fileRecNo*ChessOption.recordSize);
			chessOption.load(treeStream);
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
				textMoves.setVisibility(View.GONE);
			}
			lastOptionNo = optionsStack.pop();
			try {
				treeStream.seek(ChessOption.recordSize*lastOptionNo);
				chessOption.load(treeStream);
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

}