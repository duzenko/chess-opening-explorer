package name.duzenko.chessopeningexplorer.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import name.duzenko.chessopeningexplorer.MainActivity;
import name.duzenko.chessopeningexplorer.R;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.vending.expansion.downloader.Helpers;

public class ExtractTask extends AsyncTask<Void, Integer, String>{
	
	static public boolean stopRequested;
	/**
	 * 
	 */
	private final Activity activity;
	String fileName;

	/**
	 * @param loaderActivity
	 */
	public ExtractTask(Activity loaderActivity) {
		activity = loaderActivity;
	}

	@Override
	protected void onPreExecute() {
		stopRequested = false;
		activity.setContentView(R.layout.extracter);
		progressBar = (ProgressBar) activity.findViewById(R.id.progressBarExtract);
		System.out.println("progress bar " + R.id.progressBarExtract + " " + progressBar);
	}
	
    @Override
    protected String doInBackground(Void... params) {
		try {
			fileName = Helpers.getSaveFilePath(activity) + File.separator + LoaderActivity.obbFileName;
			ZipResourceFile zipFile = new ZipResourceFile(fileName);
			System.out.println("obb file is " + Helpers.getSaveFilePath(activity) + File.separator + LoaderActivity.obbFileName);
			fileName = "(zip content)";
			InputStream inputStream = zipFile.getInputStream(Global.dbTreeName);
			InputStream inputStream2 = zipFile.getInputStream(Global.dbTxtName);
			totalBytes = inputStream.available() + inputStream2.available();
			progressBar.setMax(totalBytes);
			System.out.println("total bytes " + totalBytes);

			fileName = Global.dbTreeName;
			saveStream(inputStream, Global.dbTreeName);
			
			fileName = Global.dbTxtName;
			saveStream(inputStream2, Global.dbTxtName);
			
			System.out.println("extract complete");
		} catch (IOException e) {
			e.printStackTrace();
			return e.getClass().getSimpleName();
		}
      return null;
    }
    
    ProgressBar progressBar;
    
    @Override
    protected void onProgressUpdate(Integer... values) {
    	final int p = processedBytes;//(int) (100L*processedBytes/totalBytes);
    	if(p==progressBar.getProgress())
    		return;
    	activity.runOnUiThread(new Runnable() {
    	    @Override
    	    public void run() {
    	    	progressBar.setProgress(p);
    	    	progressBar.invalidate();
    	    	//System.out.println("progress " + progressBar.getProgress() + " / " + progressBar.getMax());
    	    }
    	});
    }
    
    @Override
    protected void onPostExecute(String error) {
		if(error==null && !stopRequested)
			activity.startActivity(new Intent(activity, MainActivity.class));
		else {
			Toast.makeText(activity, "Extraction error: " + error + '\n' + fileName + '\n' + Global.dbDir, Toast.LENGTH_LONG).show();
			activity.finish();
		}
		stopRequested = true;
    }
    
    int processedBytes, totalBytes;

	void saveStream(InputStream input, String outName) throws IOException {
		try {
		    final File file = new File(Global.dbDir, outName);
//		    try {
//		    	System.out.println("parent file " + file.getParentFile());
//		    	if(!file.getParentFile().mkdirs())
//		    		System.out.println("Failed to create data dir");;
//		    } catch(Exception e) {
//		    	throw new NullPointerException("mkdirs error. " + Global.dbDir + " " + outName + ". " + e.getClass().getSimpleName() + ": " + e.getMessage() + " " + file);
//		    }
		    final OutputStream output = new FileOutputStream(file);
		    System.out.println("save stream " + outName);
		    try {
		        try {
		            final byte[] buffer = new byte[1024*128];
		            int read;
	
		            while (!stopRequested && (read = input.read(buffer)) != -1) {
		                output.write(buffer, 0, read);
		                processedBytes += read;
		                this.publishProgress(processedBytes);
		            }
	
		            output.flush();
		        } finally {
		            output.close();
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		} finally {
		    input.close();
		}
	}
}