package name.duzenko.chessopeningexplorer.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class FileLog extends PrintStream {

	PrintStream sysOut, sysErr;
	
	public FileLog(File file) throws FileNotFoundException {
		super(file);
		sysOut = System.out;
		sysErr = System.err;
		System.setOut(this);
		System.setErr(this);
	}
	
	@Override
	public synchronized void println(String str) {
		super.println(str+'\r');
		sysOut.println(str);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

}
