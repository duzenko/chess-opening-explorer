package name.duzenko.chessopeningexplorer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity {

	ToggleButton altBoard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		altBoard = (ToggleButton) findViewById(R.id.toggleAltBoard);
		altBoard.setChecked(AppPreferences.alternativeBoard());
	}
	
	@Override
	protected void onPause() {
		AppPreferences.setAlternativeBoard(altBoard.isChecked());
		super.onPause();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

}
