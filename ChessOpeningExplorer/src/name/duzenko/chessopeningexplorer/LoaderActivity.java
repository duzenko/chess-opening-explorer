package name.duzenko.chessopeningexplorer;

import java.io.File;
import name.duzenko.chessopeningexplorer.apkx.SampleDownloaderActivity;

import com.google.android.vending.expansion.downloader.Helpers;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

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
		String error = load();
		if(error!=null) {
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if(null==Global.init(path+"/Chess Opening Explorer/"))
			startActivity(new Intent(this, MainActivity.class));
		else
			installDb();
	}
	
	String path = null;
	
	protected void onDestroy() {
		super.onDestroy();
		ExtractTask.stopRequested = true;
	};
	
	String load() {
    	File storageDir = new File("/storage/");
    	if(storageDir.isDirectory()) {
    	    String[] dirList = storageDir.list();
    	    long maxfree = 0;
    	    for(String string: dirList) {
    	    	File sdcard = new File("/storage/" + string);
    	    	if(sdcard.getTotalSpace()>maxfree) {
    	    		maxfree = sdcard.getTotalSpace();
    	    		path = sdcard.getPath();
    	    	}
    	    	System.out.println("Found " + string + ", " + sdcard.getFreeSpace()/1024/1024 + "MB available, " + sdcard.getTotalSpace()/1024/1024 + "MB total");
    	    }
    	}
    	if(path==null) {
    		path = Environment.getExternalStorageDirectory().getPath();
    		System.out.println("Fall back to " + path);
    	}
    	if(path==null) 
    		return "No sdcard found";
    	return null;
	}
	
	void installDb() {
		if (!expansionFilesDelivered()) 
			startActivityForResult(new Intent(this, SampleDownloaderActivity.class), 0);
		else
			new ExtractTask(this).execute();		
	}
	
	public static int obbFileSize = 247349813;
	public static String obbFileName;

	boolean expansionFilesDelivered() {
		obbFileName = Helpers.getExpansionAPKFileName(this, true, 1);
		return Helpers.doesFileExist(this, obbFileName, obbFileSize, false);
	}
	
}
