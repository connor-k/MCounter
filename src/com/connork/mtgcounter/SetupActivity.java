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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SetupActivity extends Activity implements OnClickListener {
	private Menu optionsMenu;
	private int num_players, starting_life;
	private String player_names[] = new String[4];
	private Button button_players[] = new Button[4];
	private RadioGroup radio_group;
	private RadioButton radio_button[] = new RadioButton[3];

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
		for (int i = 0; i < 4; i++) {
			button_players[i].setOnClickListener(this);
		}

		radio_button[0] = (RadioButton) findViewById(R.id.radio_setup_2p);
		radio_button[1] = (RadioButton) findViewById(R.id.radio_setup_3p);
		radio_button[2] = (RadioButton) findViewById(R.id.radio_setup_4p);
		radio_group = (RadioGroup) findViewById(R.id.radiogroup_setup_number_players);
		radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) 
			{
				RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
				String text = checkedRadioButton.getText().toString();

				if (text.equals(radio_button[0].getText())) {
					num_players = 2;
				}
				else if (text.equals(radio_button[1].getText())) {
					num_players = 3;
				}
				else if (text.equals(radio_button[2].getText())) {
					num_players = 4;
				}
				for (int i = 0; i < 4; i++) {
					if (i < num_players) {
						button_players[i].setEnabled(true);
					}
					else {
						button_players[i].setEnabled(false);
					}
				}
			}
		});

		// Set up the SeekBar that determines initial life
		SeekBar seekBar = (SeekBar)findViewById(R.id.seekbar_setup_life);
		final TextView seekBarText = (TextView)findViewById(R.id.textview_setup_initial_life);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

			@Override 
			public void onProgressChanged(SeekBar seekBar, int progress, 
					boolean fromUser) { 
				// Don't let them start with less than 1 life
				if (progress < 1) {
					seekBar.setProgress(1);
				}
				else {
					starting_life = progress;
					seekBarText.setText("Initial Life: " + starting_life);
				}
			} 

			@Override 
			public void onStartTrackingTouch(SeekBar seekBar) {
			} 

			@Override 
			public void onStopTrackingTouch(SeekBar seekBar) { 
			} 
		}); 

		//TODO Add settings, keep screen on

		// Restore last state if possible
		if (savedInstanceState != null)
		{
			num_players = savedInstanceState.getInt("setup_num_players", 2);
			player_names = savedInstanceState.getStringArray("setup_player_names");
			starting_life = savedInstanceState.getInt("setup_starting_life", 20);
		}
		else {
			String names = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("KEY_PLAYER_NAMES", "Player 1, Player 2, Player 3, Player 4");
			player_names = names.split(",");
			num_players = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("KEY_NUM_PLAYERS", 2);
			starting_life = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("KEY_STARTING_LIFE", 20);
		}
		radio_button[num_players - 2].toggle();
		for (int i = 0; i < 4; i++) {
			button_players[i].setText(player_names[i]);
		}
		seekBar.setProgress(starting_life);
		seekBarText.setText("Initial Life: " + starting_life);
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
		input.setText(player_names[player - 1]);
		int position = input.length();
		Editable etext = input.getText();
		Selection.setSelection(etext, position);
		alertDialog.setView(input);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = input.getText().toString();
				player_names[player - 1] = name;
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
			intent.putExtra(MainActivity.KEY_NUM_PLAYERS, num_players);
			intent.putExtra(MainActivity.KEY_PLAYER_NAMES, player_names);
			intent.putExtra(MainActivity.KEY_STARTING_LIFE, starting_life);
			startActivity(intent);
			return true;
		case R.id.menu_main_settings:
			Intent intent2 = new Intent(SetupActivity.this, Prefs.class);
			startActivity(intent2);
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


	@Override
	protected void onPause()
	{

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		// Save the player names, etc.
		String names = player_names[0] + ',' + player_names[1] + ',' + player_names[2] + ',' + player_names[3];
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("KEY_PLAYER_NAMES", names).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("KEY_NUM_PLAYERS", num_players).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("KEY_STARTING_LIFE", starting_life).commit();
		super.onDestroy();
	}

	@Override
	protected void onResume() 
	{

		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("setup_num_players", num_players);
		outState.putStringArray("setup_player_names", player_names);
		outState.putInt("setup_starting_life", starting_life);
	}
}
