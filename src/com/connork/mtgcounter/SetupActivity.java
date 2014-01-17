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
import android.widget.Toast;

public class SetupActivity extends Activity implements OnClickListener {
	private Menu optionsMenu;
	private int num_players, starting_life;
	private final int MANA_COLORLESS = 0, MANA_WHITE = 1, MANA_BLUE = 2, MANA_BLACK = 3, MANA_RED = 4, MANA_GREEN = 5;
	private int[] mana_color = new int[4]; // The mana color code for each player
	private String player_names[] = new String[4];
	private Button button_players[] = new Button[4], button_mana[] = new Button[4];
	private Button button_continue; //TODO
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

		// Set up click listeners for all the player name buttons
		button_players[0] = (Button) findViewById(R.id.button_setup_p1);
		button_players[1] = (Button) findViewById(R.id.button_setup_p2);
		button_players[2] = (Button) findViewById(R.id.button_setup_p3);
		button_players[3] = (Button) findViewById(R.id.button_setup_p4);
		for (int i = 0; i < 4; i++) {
			button_players[i].setOnClickListener(this);
		}

		// Set up click listeners for all the player mana colors
		button_mana[0] = (Button) findViewById(R.id.button_setup_p1_mana);
		button_mana[1] = (Button) findViewById(R.id.button_setup_p2_mana);
		button_mana[2] = (Button) findViewById(R.id.button_setup_p3_mana);
		button_mana[3] = (Button) findViewById(R.id.button_setup_p4_mana);
		for (int i = 0; i < 4; i++) {
			button_mana[i].setOnClickListener(this);
		}

		// Set up the radio group to select how many people are playing
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
						button_mana[i].setEnabled(true);
					}
					else {
						button_players[i].setEnabled(false);
						button_mana[i].setEnabled(false);
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

		// Restore last state if possible, otherwise get the player names etc from storage
		if (savedInstanceState != null)
		{
			num_players = savedInstanceState.getInt("setup_num_players", 2);
			player_names = savedInstanceState.getStringArray("setup_player_names");
			starting_life = savedInstanceState.getInt("setup_starting_life", 20);
			mana_color = savedInstanceState.getIntArray("setup_mana_color");
			for (int i = 0; i < mana_color.length; i++) {
				setManaColor(i, mana_color[i]);
			}
		}
		else {
			String names = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("KEY_PLAYER_NAMES", "Player 1, Player 2, Player 3, Player 4");
			player_names = names.split(",");
			num_players = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("KEY_NUM_PLAYERS", 2);
			starting_life = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("KEY_STARTING_LIFE", 20);
			String icons = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("KEY_MANA_COLOR", MANA_WHITE + "," + MANA_RED + "," +  MANA_BLUE + "," + MANA_GREEN);
			String[] iconNums = icons.split(",");
			for (int i = 0; i < iconNums.length; i++) {
				setManaColor(i, Integer.parseInt(iconNums[i]));
			}
		}
		// Select the appropriate radio button for how many people are playing
		radio_button[num_players - 2].toggle();
		// Set button text to player names
		for (int i = 0; i < 4; i++) {
			button_players[i].setText(player_names[i]);
		}
		// Set the initial life progress bar to previous state
		seekBar.setProgress(starting_life);
		seekBarText.setText("Initial Life: " + starting_life);
	} 

	/** Handle when a button is clicked, call appropriate action */
	public void onClick(View v) {
		switch (v.getId()) 
		{
		case R.id.button_setup_p1:
			editPlayerName(MainActivity.PLAYER_1);
			break;
		case R.id.button_setup_p2:
			editPlayerName(MainActivity.PLAYER_2); 
			break;
		case R.id.button_setup_p3:
			editPlayerName(MainActivity.PLAYER_3);
			break;
		case R.id.button_setup_p4:
			editPlayerName(MainActivity.PLAYER_4); 
			break;
		case R.id.button_setup_p1_mana:
			editPlayerMana(MainActivity.PLAYER_1);
			break;
		case R.id.button_setup_p2_mana:
			editPlayerMana(MainActivity.PLAYER_2); 
			break;
		case R.id.button_setup_p3_mana:
			editPlayerMana(MainActivity.PLAYER_3);
			break;
		case R.id.button_setup_p4_mana:
			editPlayerMana(MainActivity.PLAYER_4); 
			break;
		}
	}

	/** Set a player's name */
	private void editPlayerName(int p) {
		final int player = p;
		AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this).create();
		alertDialog.setTitle("Edit Player Name");
		alertDialog.setMessage("Enter Player " + (player+1) + "\'s name:");                
		// Set an EditText view to get name   
		final EditText input = new EditText(this); 
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		input.setHint("Player " + (player+1));
		input.setText(player_names[player]);
		int position = input.length();
		Editable etext = input.getText();
		Selection.setSelection(etext, position);
		alertDialog.setView(input);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = input.getText().toString();
				player_names[player] = name;
				button_players[player].setText(name);
			} });
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// empty
			} });
		alertDialog.show();
	}

	/** Change a player's mana icon and updates the array of icons */
	private void setManaColor(int player, int color) {
		switch (color) {
		case MANA_COLORLESS:
			button_mana[player].setBackgroundDrawable(getResources().getDrawable(R.drawable.mana_colorless));
			mana_color[player] = MANA_COLORLESS;
			break;
		case MANA_WHITE:
			button_mana[player].setBackgroundDrawable(getResources().getDrawable(R.drawable.mana_white));
			mana_color[player] = MANA_WHITE;
			break;
		case MANA_BLUE:
			button_mana[player].setBackgroundDrawable(getResources().getDrawable(R.drawable.mana_blue));
			mana_color[player] = MANA_BLUE;
			break;
		case MANA_BLACK:
			button_mana[player].setBackgroundDrawable(getResources().getDrawable(R.drawable.mana_black));
			mana_color[player] = MANA_BLACK;
			break;
		case MANA_RED:
			button_mana[player].setBackgroundDrawable(getResources().getDrawable(R.drawable.mana_red));
			mana_color[player] = MANA_RED;
			break;
		case MANA_GREEN:
			button_mana[player].setBackgroundDrawable(getResources().getDrawable(R.drawable.mana_green));
			mana_color[player] = MANA_GREEN;
			break;
		}
	}

	/** Dialog to set a player's mana color */
	private void editPlayerMana(int p) {
		final int player = p;
		final AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this).create();
		alertDialog.setTitle("Choose " + player_names[player] + "\'s Mana Color");
		alertDialog.setView(getLayoutInflater().inflate(R.layout.alertdialog_setup_mana, null));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				RadioGroup radioGroup = (RadioGroup)alertDialog.findViewById(R.id.radiogroup_setup_mana);

				switch (radioGroup.getCheckedRadioButtonId()) {
				case R.id.radio_setup_mana_colorless:
					setManaColor(player, MANA_COLORLESS);
					break;
				case R.id.radio_setup_mana_white:
					setManaColor(player, MANA_WHITE);
					break;
				case R.id.radio_setup_mana_blue:
					setManaColor(player, MANA_BLUE);
					break;
				case R.id.radio_setup_mana_black:
					setManaColor(player, MANA_BLACK);
					break;
				case R.id.radio_setup_mana_red:
					setManaColor(player, MANA_RED);
					break;
				case R.id.radio_setup_mana_green:
					setManaColor(player, MANA_GREEN);
					break;
				}
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
			intent.putExtra(MainActivity.KEY_PLAYER_ICONS, mana_color);
			//TODO pass mana color
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
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// Save the player names, etc.
		String names = player_names[MainActivity.PLAYER_1] + ',' + player_names[MainActivity.PLAYER_2] + ',' + player_names[MainActivity.PLAYER_3] + ',' + player_names[MainActivity.PLAYER_4];
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("KEY_PLAYER_NAMES", names).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("KEY_NUM_PLAYERS", num_players).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("KEY_STARTING_LIFE", starting_life).commit();
		// Save the player mana colors
		String mana = mana_color[MainActivity.PLAYER_1] + "," + mana_color[MainActivity.PLAYER_2] + "," + mana_color[MainActivity.PLAYER_3] + "," + mana_color[MainActivity.PLAYER_4];
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("KEY_MANA_COLOR", mana).commit();

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("setup_num_players", num_players);
		outState.putStringArray("setup_player_names", player_names);
		outState.putInt("setup_starting_life", starting_life);
		outState.putIntArray("setup_mana_color", mana_color);
	}
}
