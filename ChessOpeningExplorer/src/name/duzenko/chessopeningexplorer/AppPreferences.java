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

	public static boolean hideActionBar() {
		return preferences.getBoolean("hideActionBar", false);
	}
	
	static void setHideActionBar(boolean value) {
		Editor editor = preferences.edit();
		editor.putBoolean("hideActionBar", value);
		editor.commit();
	}

	public static int timeLimit() {
		return preferences.getInt("timeLimit", 5);
	}
	
	static void setTimeLimit(int value) {
		Editor editor = preferences.edit();
		editor.putInt("timeLimit", value);
		editor.commit();
	}

}
