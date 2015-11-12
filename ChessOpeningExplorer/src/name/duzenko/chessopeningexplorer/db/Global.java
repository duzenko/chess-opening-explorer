package name.duzenko.chessopeningexplorer.db;

import java.io.File;

import name.duzenko.chessopeningexplorer.MainActivity;
import name.duzenko.chessopeningexplorer.play.PlayActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Global {
	
	public static String dbDir;
	static String dbTxtFileName;
	static String dbTreeFileName;
	static String dbTxtName = "ficsgamesdb_2012_standard_nomovetimes_1213340.txt";
	static String dbTreeName = "ficsgamesdb_2012_standard_nomovetimes_1213340.tree";
	public static File dbTxtFile;
	public static File dbTreeFile;
	public static SharedPreferences settings;
	
	static String init(Context context) {
        settings = context.getSharedPreferences("", PlayActivity.MODE_PRIVATE);
		String path = context.getExternalFilesDir("") + File.separator;
		if (android.os.Build.MANUFACTURER.contains("samsung")) {
			File f = new File("/storage/extSdCard/Android/data/" + MainActivity.class.getPackage().getName() + "/files/");
			f.mkdirs();
			if (f.exists())
				path = f.getAbsolutePath() + "/";
		}
//		String path = context.getExternalFilesDir(null).getAbsolutePath() + File.separator;
		System.out.println("global init " + path);
		dbDir = path;
    	dbTxtFileName = path+dbTxtName;
    	dbTreeFileName = path + dbTreeName;
    	dbTxtFile = new File(dbTxtFileName);
    	dbTreeFile = new File(dbTreeFileName);
    	if(!dbTxtFile.exists() || dbTxtFile.length() != 607443125) 
    		return "Tree file not found or wrong size in " + dbTxtFileName;
    	if(!dbTreeFile.exists() || dbTreeFile.length() != 94555368) 
    		return "Tree file not found or wrong size in " + dbTreeFileName;
    	return null;
	}
	
	public static String getMoves() {
        return settings.getString("moves", "");
		
	}
	
	public static void setMoves(String moves) {
		Editor editor = settings.edit();
		editor.putString("moves", moves);
		editor.commit();
	}

}
