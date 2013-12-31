package com.connork.mtgcounter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class SetupActivity extends Activity implements OnClickListener {
	private Menu optionsMenu;
	private int num_players;
	private Button button_players[] = new Button[4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		setTitle("Setup");

		// Style the ActionBar
		ActionBar actionBar = getActionBar();
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.ab_texture_tile);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bMap);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		actionBar.setBackgroundDrawable(bitmapDrawable);

		// Set up click listeners for all the buttons
		button_players[0] = (Button) findViewById(R.id.button_setup_p1);
		button_players[1] = (Button) findViewById(R.id.button_setup_p2);
		button_players[2] = (Button) findViewById(R.id.button_setup_p3);
		button_players[3] = (Button) findViewById(R.id.button_setup_p4);

		//TODO replace with radio buttons
		new AlertDialog.Builder(this)
		.setTitle("How Many Players?")
		.setItems(R.array.num_players, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialoginterface, int num) 
			{
				num_players = (num + 2);
				for (int i = 0; i < 4; i++) {
					button_players[i].setOnClickListener(SetupActivity.this);
					if (i > num_players - 1) {
						button_players[i].setEnabled(false);
					}
					else {
						button_players[i].setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("KEY_NAME_P" + (i + 1), "Player " + (i + 1)));
					}
				}
			}
		}).show();

		// Set up the SeekBar that determines initial life
		SeekBar seekBar = (SeekBar)findViewById(R.id.seekbar_setup_life);
		final TextView seekBarText = (TextView)findViewById(R.id.textview_setup_initial_life);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

			@Override 
			public void onProgressChanged(SeekBar seekBar, int progress, 
					boolean fromUser) { 
				// Don't let them start with less than 1 life
				if (seekBar.getProgress() < 1) {
					seekBar.setProgress(1);
				}
				else {
					seekBarText.setText("Initial Life: " + progress);
				}
			} 

			@Override 
			public void onStartTrackingTouch(SeekBar seekBar) {
			} 

			@Override 
			public void onStopTrackingTouch(SeekBar seekBar) { 
			} 
		}); 
		
		//TODO Pass MainActivity the num players, names, initial life
		//TODO Add settings, keep screen on
		//TODO Activity lifecycle stuff
		//TODO Don't access SharedPreferences often
	} 

	/** Handle when a button is clicked, call appropriate action */
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.button_setup_p1:
			editPlayerName(1);
			break;
		case R.id.button_setup_p2:
			editPlayerName(2); 
			break;
		case R.id.button_setup_p3:
			editPlayerName(3);
			break;
		case R.id.button_setup_p4:
			editPlayerName(4); 
			break;
		}
	}

	/** Set a player's name */
	private void editPlayerName(int p)
	{
		final int player = p;
		AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this).create();
		alertDialog.setTitle("Edit Player Name");
		alertDialog.setMessage("Enter Player " + player + "\'s name:");                
		// Set an EditText view to get name   
		final EditText input = new EditText(this); 
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		input.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("KEY_NAME_P" + player, "Player " + player));
		int position = input.length();
		Editable etext = input.getText();
		Selection.setSelection(etext, position);
		alertDialog.setView(input);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = input.getText().toString();
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("KEY_NAME_P" + player, name).commit();
				button_players[player - 1].setText(name);
			} });
		alertDialog.show();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		this.optionsMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_setup, menu);
		// Let the progress spinner go as they choose their options
		setWorkingActionButtonState(true);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_setup_done:
			Intent intent = new Intent(SetupActivity.this, MainActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setWorkingActionButtonState(final boolean refreshing) {
		if (optionsMenu != null) {
			final MenuItem refreshItem = optionsMenu
					.findItem(R.id.menu_setup_working);
			if (refreshItem != null) {
				if (refreshing) {
					refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
				} else {
					refreshItem.setActionView(null);
				}
			}
		}
	}
}
