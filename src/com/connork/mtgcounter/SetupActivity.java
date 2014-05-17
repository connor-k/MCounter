package com.connork.mtgcounter;

import java.util.Random;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class SetupActivity extends Activity implements OnClickListener {
	private Menu optionsMenu;
	private int num_players, starting_life;
	private final int PLAYER_1 = 0, PLAYER_2 = 1, PLAYER_3 = 2, PLAYER_4 = 3, MANA_COLORLESS = 0, MANA_WHITE = 1, MANA_BLUE = 2, MANA_BLACK = 3, MANA_RED = 4, MANA_GREEN = 5;
	private int[] mana_color = new int[4]; // The mana color code for each player
	private String player_names[] = new String[4];
	private Button button_players[] = new Button[4], button_mana[] = new Button[4];
	private CheckBox checkbox_team_together;
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

		// Set up checkbox for teams
		checkbox_team_together = (CheckBox)findViewById(R.id.checkbox_setup_team_together);
		checkbox_team_together.setOnClickListener(this);

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
			checkbox_team_together.setChecked(savedInstanceState.getBoolean("setup_team_together", false));
		}
		else {
			String names = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("KEY_PLAYER_NAMES", "Player 1, Player 2, Player 3, Player 4");
			player_names = names.split(",");
			num_players = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("KEY_NUM_PLAYERS", 2);
			starting_life = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("KEY_STARTING_LIFE", 20);
			String icons = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("KEY_MANA_COLOR", MANA_WHITE + "," + MANA_RED + "," +  MANA_BLUE + "," + MANA_GREEN);
			String[] iconNums = icons.split(",");
			for (int i = 0; i < iconNums.length; i++) {
				mana_color[i] = Integer.parseInt(iconNums[i]);
			}
			checkbox_team_together.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("KEY_TEAM_TOGETHER", false));
		}
		// Select the appropriate radio button for how many people are playing
		radio_button[num_players - 2].toggle();

		// Set the initial life progress bar to previous state
		seekBar.setProgress(starting_life);
		seekBarText.setText("Initial Life: " + starting_life);

		// Update player names & mana color
		updateViews();
	}

	/** Update all button text and mana color */
	private void updateViews() {
		// Set button text and mana to player names
		for (int i = 0; i < 4; i++) {
			// Add team label to player names for clarity
			if (checkbox_team_together.isChecked()) {
				button_players[PLAYER_1].setText("Team 1: " + player_names[PLAYER_1]);
				button_players[PLAYER_2].setText("Team 2: " + player_names[PLAYER_2]);
				button_players[PLAYER_3].setText("Team 1: " + player_names[PLAYER_3]);
				button_players[PLAYER_4].setText("Team 2: " + player_names[PLAYER_4]);
			}
			else {
				button_players[i].setText(player_names[i]);
			}
			setManaColor(i, mana_color[i]);
		}
	}

	/** Handle when a button is clicked, call appropriate action */
	public void onClick(View v) {
		switch (v.getId()) 
		{
		case R.id.checkbox_setup_team_together:
			updateViews();
			break;
		case R.id.button_setup_p1:
			editPlayerName(PLAYER_1);
			break;
		case R.id.button_setup_p2:
			editPlayerName(PLAYER_2); 
			break;
		case R.id.button_setup_p3:
			editPlayerName(PLAYER_3);
			break;
		case R.id.button_setup_p4:
			editPlayerName(PLAYER_4); 
			break;
		case R.id.button_setup_p1_mana:
			editPlayerMana(PLAYER_1);
			break;
		case R.id.button_setup_p2_mana:
			editPlayerMana(PLAYER_2); 
			break;
		case R.id.button_setup_p3_mana:
			editPlayerMana(PLAYER_3);
			break;
		case R.id.button_setup_p4_mana:
			editPlayerMana(PLAYER_4); 
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
		// Set up the radio group in the dialog. Toggle the current mana color they have
		final int player = p;

		ScrollView scrollView = (ScrollView) getLayoutInflater().inflate(R.layout.alertdialog_setup_mana, null);

		final RadioGroup radioGroup = (RadioGroup) scrollView.findViewById(R.id.radiogroup_setup_mana);

		switch (mana_color[player]) {
		case MANA_COLORLESS:
			radioGroup.check(R.id.radio_setup_mana_colorless);
			break;
		case MANA_WHITE:
			radioGroup.check(R.id.radio_setup_mana_white);
			break;
		case MANA_BLUE:
			radioGroup.check(R.id.radio_setup_mana_blue);
			break;
		case MANA_BLACK:
			radioGroup.check(R.id.radio_setup_mana_black);
			break;
		case MANA_RED:
			radioGroup.check(R.id.radio_setup_mana_red);
			break;
		case MANA_GREEN:
			radioGroup.check(R.id.radio_setup_mana_green);
			break;
		}

		// Set up the dialog
		final AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this).create();
		alertDialog.setTitle("Choose " + player_names[player] + "\'s Mana Color");
		alertDialog.setView(scrollView);
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
			final Intent intent = new Intent(SetupActivity.this, MainActivity.class);
			intent.putExtra(MainActivity.KEY_NUM_PLAYERS, num_players);
			intent.putExtra(MainActivity.KEY_PLAYER_NAMES, player_names);
			intent.putExtra(MainActivity.KEY_STARTING_LIFE, starting_life);
			intent.putExtra(MainActivity.KEY_MANA_COLOR, mana_color);
			// Give option to continue previous game if possible
			if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(MainActivity.KEY_GAME_IN_PROGRESS, false) ) {
				String[] options = {"Continue Game", "Create New Game"};
				new AlertDialog.Builder(this)
				.setTitle("Continue Game?")
				.setItems(options, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						if (i == 0) {
							intent.putExtra(MainActivity.KEY_CONTINUE_GAME, 1);
						}
						else {
							intent.putExtra(MainActivity.KEY_CONTINUE_GAME, 0);
						}
						startActivity(intent);
					}
				}).show();
			}
			else {
				startActivity(intent);
			}
			return true;
		case R.id.menu_setup_shuffle_players:
			// Shuffle the order of players
			for (int i = 0; i < num_players; i++) {
				// Get random index
				Random numgen = new Random();
				int index = numgen.nextInt(num_players);

				// Swap name
				String temp_name = player_names[index];
				player_names[index] = player_names[i];
				player_names[i] = temp_name;

				// Swap mana color
				int temp_mana = mana_color[index];
				mana_color[index] = mana_color[i];
				mana_color[i] = temp_mana;
			}
			// Update the views
			for (int i = 0; i < num_players; i++) {
				button_players[i].setText(player_names[i]);
				setManaColor(i, mana_color[i]);
			}
			return true;
		case R.id.menu_setup_settings:
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
		// Save the player names, etc.
		String names = player_names[PLAYER_1] + ',' + player_names[PLAYER_2] + ',' + player_names[PLAYER_3] + ',' + player_names[PLAYER_4];
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("KEY_PLAYER_NAMES", names).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("KEY_NUM_PLAYERS", num_players).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("KEY_TEAM_TOGETHER", checkbox_team_together.isChecked()).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("KEY_STARTING_LIFE", starting_life).commit();
		// Save the player mana colors
		String mana = mana_color[PLAYER_1] + "," + mana_color[PLAYER_2] + "," + mana_color[PLAYER_3] + "," + mana_color[PLAYER_4];
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("KEY_MANA_COLOR", mana).commit();

		super.onPause();
	}

	@Override
	protected void onDestroy() {
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
		outState.putBoolean("setup_team_together", checkbox_team_together.isChecked());
	}
}
