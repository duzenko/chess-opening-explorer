package name.duzenko.chessopeningexplorer;

import name.duzenko.chessopeningexplorer.db.Global;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity {

	ToggleButton altBoard, hideActionBar;
	EditText timeLimit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		altBoard = (ToggleButton) findViewById(R.id.toggleAltBoard);
		altBoard.setChecked(AppPreferences.alternativeBoard());
		hideActionBar = (ToggleButton) findViewById(R.id.toggleActionBar);
		hideActionBar.setChecked(AppPreferences.hideActionBar());
		timeLimit = (EditText) findViewById(R.id.editTimeLimit);
		timeLimit.setText(String.valueOf(AppPreferences.timeLimit()));
		((TextView)findViewById(R.id.textDbPath)).setText(Global.dbDir);
	}
	
	@Override
	protected void onPause() {
		AppPreferences.setAlternativeBoard(altBoard.isChecked());
		AppPreferences.setHideActionBar(hideActionBar.isChecked());
		try {
			AppPreferences.setTimeLimit(Integer.valueOf(timeLimit.getText().toString()));
		} catch(Exception exception) {
			Toast.makeText(this, "Invalid integer string", Toast.LENGTH_SHORT).show();
		}
		super.onPause();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

}
