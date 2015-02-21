package name.duzenko.chessopeningexplorer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreferences {
	
	static SharedPreferences preferences;
	
	public static boolean alternativeBoard() {
		return preferences.getBoolean("altBoard", false);
	}
	
	static void setAlternativeBoard(boolean value) {
		Editor editor = preferences.edit();
		editor.putBoolean("altBoard", value);
		editor.commit();
	}

}
