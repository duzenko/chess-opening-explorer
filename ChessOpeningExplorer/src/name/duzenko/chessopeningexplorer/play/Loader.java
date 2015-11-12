package name.duzenko.chessopeningexplorer.play;

import java.util.ArrayList;
import java.util.List;

import name.duzenko.chessopeningexplorer.R;
import name.duzenko.chessopeningexplorer.db.Global;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class Loader extends AsyncTask<Void, Void, Void> {
	
	/**
	 * 
	 */
	private final Activity activity;

	/**
	 * @param playActivity
	 */
	public Loader(Activity activity) {
		this.activity = activity;
	}

	ProgressDialog dialog;
    protected List<String> posHistStr = new ArrayList<String>();

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		dialog = new ProgressDialog(activity);
		dialog.setTitle(R.string.loadingCuckoo);
		dialog.show();
		dialog.setCancelable(false);
	}

	@Override
	protected Void doInBackground(Void... params) {
//		if (PlayActivity.chessFont == null)
//			PlayActivity.chessFont = Typeface.createFromAsset(activity.getAssets(), "casefont.ttf");
        posHistStr.add("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        posHistStr.add(Global.getMoves());
        posHistStr.add("0");
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
        dialog.cancel();
		super.onPostExecute(result);
	}
	
}