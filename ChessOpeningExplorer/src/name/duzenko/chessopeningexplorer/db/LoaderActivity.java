package name.duzenko.chessopeningexplorer.db;

import name.duzenko.chessopeningexplorer.MainActivity;
import name.duzenko.chessopeningexplorer.R;
import name.duzenko.chessopeningexplorer.apkx.SampleDownloaderActivity;
import name.duzenko.chessopeningexplorer.log.FileLog;

import com.google.android.vending.expansion.downloader.Helpers;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LoaderActivity extends Activity {

	static FileLog fileLog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loader);
		/*try {
			File file = new File("/sdcard/chessOE.log");
			System.out.println("delete log: " + file.delete());
			fileLog = new FileLog(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		if(null==Global.init(this))
			startActivity(new Intent(this, MainActivity.class));
		else
			installDb();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		ExtractTask.stopRequested = true;
	};
	
	void installDb() {
		if (!expansionFilesDelivered()) 
			startActivityForResult(new Intent(this, SampleDownloaderActivity.class), 0);
		else
			new ExtractTask(this).execute();		
	}
	
	public static int obbFileSize = 234301805;
	public static String obbFileName;

	boolean expansionFilesDelivered() {
		obbFileName = Helpers.getExpansionAPKFileName(this, true, 21);
		System.out.println(Helpers.generateSaveFileName(this, obbFileName));
		return Helpers.doesFileExist(this, obbFileName, obbFileSize, false);
	}
	
}
