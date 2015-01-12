package name.duzenko.chessopeningexplorer;

import java.io.File;

public class Global {
	
	static String dbDir, dbTxtFileName, dbTreeFileName, dbTxtName = "ficsgamesdb_2012_standard_nomovetimes_793102.txt", dbTreeName = "ficsgamesdb_2012_standard_nomovetimes_793102.tree";
	static File dbTxtFile, dbTreeFile;
	
	static String init(String path) {
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

}
