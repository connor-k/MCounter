package com.connork.mcounter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

/**
 * The starting activity for the application, sets player names, gametype, and other basic info.
 */
public class SetupActivity extends Activity implements OnClickListener {
	private Menu optionsMenu;
	private int numPlayers;
	private int startingLife;
	private int[] playerManaColors;
	private String[] playerNames;
	private Button[] playerNameButtons;
	private Button[] playerManaButtons;
	private CheckBox teamTogetherCheckbox;
	private RadioGroup nPlayersRadioGroup;
	private RadioButton[] nPlayersRadioButtons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		setTitle(R.string.setup);

		// Style the ActionBar
		ActionBar actionBar = getActionBar();
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.ab_texture_tile);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bMap);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		actionBar.setBackgroundDrawable(bitmapDrawable);

		// Initialize the private members to store player names and colors
		playerNames = new String[Constants.MAX_PLAYERS];
		playerNameButtons = new Button[Constants.MAX_PLAYERS];
		playerManaColors = new int[Constants.MAX_PLAYERS];
		playerManaButtons = new Button[Constants.MAX_PLAYERS];
		nPlayersRadioButtons = new RadioButton[Constants.MAX_PLAYERS - 1];
		
		// Initialize name and mana buttons
		playerNameButtons[Constants.PLAYER_1] = (Button) findViewById(R.id.button_setup_p1);
		playerNameButtons[Constants.PLAYER_2] = (Button) findViewById(R.id.button_setup_p2);
		playerNameButtons[Constants.PLAYER_3] = (Button) findViewById(R.id.button_setup_p3);
		playerNameButtons[Constants.PLAYER_4] = (Button) findViewById(R.id.button_setup_p4);
		
		playerManaButtons[Constants.PLAYER_1] = (Button) findViewById(R.id.button_setup_p1_mana);
		playerManaButtons[Constants.PLAYER_2] = (Button) findViewById(R.id.button_setup_p2_mana);
		playerManaButtons[Constants.PLAYER_3] = (Button) findViewById(R.id.button_setup_p3_mana);
		playerManaButtons[Constants.PLAYER_4] = (Button) findViewById(R.id.button_setup_p4_mana);
		// Register listeners for both sets of buttons
		for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
			playerNameButtons[i].setOnClickListener(this);
			playerManaButtons[i].setOnClickListener(this);
		}

		// Set up the radio group to select how many people are playing
		nPlayersRadioButtons[0] = (RadioButton) findViewById(R.id.radio_setup_2p);
		nPlayersRadioButtons[1] = (RadioButton) findViewById(R.id.radio_setup_3p);
		nPlayersRadioButtons[2] = (RadioButton) findViewById(R.id.radio_setup_4p);
		nPlayersRadioGroup = (RadioGroup) findViewById(R.id.radiogroup_setup_number_players);
		nPlayersRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.radio_setup_3p:
						numPlayers = 3;
						break;
					case R.id.radio_setup_4p:
						numPlayers = 4;
						break;
					default: // Two players
						numPlayers = 2;
						break;
				}
				for (int i = 0; i < 4; i++) {
					// Enable the buttons if it's a valid player for this game
					playerNameButtons[i].setEnabled(i < numPlayers);
					playerManaButtons[i].setEnabled(i < numPlayers);
				}
			}
		});

		// Initialize the team together checkbox
		teamTogetherCheckbox = (CheckBox) findViewById(R.id.checkbox_setup_team_together);
		teamTogetherCheckbox.setOnClickListener(this);

		// Initialize the SeekBar that determines initial life
		SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar_setup_life);
		final TextView seekBarText = (TextView) findViewById(R.id.textview_setup_initial_life);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { 
			@Override 
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
				// Don't let them start with less than 1 life
				if (progress < 1) {
					seekBar.setProgress(1);
				} else {
					startingLife = progress;
					seekBarText.setText("Initial Life: " + startingLife);
				}
			} 

			@Override 
			public void onStartTrackingTouch(SeekBar seekBar) {} 

			@Override 
			public void onStopTrackingTouch(SeekBar seekBar) {}
		}); 

		// Restore the last state if possible, otherwise get the player names etc from storage
		if (savedInstanceState != null) {
			numPlayers = savedInstanceState.getInt("setup_num_players", 2);
			playerNames = savedInstanceState.getStringArray("setup_player_names");
			startingLife = savedInstanceState.getInt("setup_starting_life", 20);
			playerManaColors = savedInstanceState.getIntArray("setup_mana_color");
			teamTogetherCheckbox.setChecked(savedInstanceState.getBoolean("setup_team_together",
					false));
		} else {
			String names = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
					.getString("KEY_PLAYER_NAMES", "Player 1, Player 2, Player 3, Player 4");
			playerNames = names.split(",");
			numPlayers = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
					.getInt("KEY_NUM_PLAYERS", 2);
			startingLife = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
					.getInt("KEY_STARTING_LIFE", 20);
			String icons = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
					.getString("KEY_MANA_COLOR", Constants.MANA_WHITE + "," + Constants.MANA_RED + "," +  Constants.MANA_BLUE + "," + Constants.MANA_GREEN);
			String[] iconNums = icons.split(",");
			for (int i = 0; i < iconNums.length; i++) {
				playerManaColors[i] = Integer.parseInt(iconNums[i]);
			}
			teamTogetherCheckbox.setChecked(PreferenceManager.getDefaultSharedPreferences(
					getApplicationContext()).getBoolean("KEY_TEAM_TOGETHER", false));
		}
		// Select the appropriate radio button for how many people are playing
		nPlayersRadioButtons[numPlayers - 2].toggle();

		// Set the initial life progress bar to previous state
		seekBar.setProgress(startingLife);
		seekBarText.setText("Initial Life: " + startingLife);

		// Update player button labels and mana icons
		updateViews();
	}

	/**
	 * Update all button text and icons.
	 */
	private void updateViews() {
		// Set button text and mana to player names. If teaming together, include team number.
		if (teamTogetherCheckbox.isChecked()) {
			playerNameButtons[Constants.PLAYER_1].setText("Team 1: " 
					+ playerNames[Constants.PLAYER_1]);
			playerNameButtons[Constants.PLAYER_2].setText("Team 2: " 
					+ playerNames[Constants.PLAYER_2]);
			playerNameButtons[Constants.PLAYER_3].setText("Team 1: " 
					+ playerNames[Constants.PLAYER_3]);
			playerNameButtons[Constants.PLAYER_4].setText("Team 2: " 
					+ playerNames[Constants.PLAYER_4]);
		}
		for (int i = 0; i < Constants.MAX_PLAYERS; ++i) {
			// If it's not teams, set the labels to just be the player's name.
			if (!teamTogetherCheckbox.isChecked()) {
				playerNameButtons[i].setText(playerNames[i]);
			}
			// Regardless of the label, set the mana icon.
			setManaColor(i, playerManaColors[i]);
		}
	}

	/**
	 * Handle the appropriate action when a button is clicked.
	 * @param v The view that was clicked.
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.checkbox_setup_team_together:
				updateViews();
				break;
			case R.id.button_setup_p1:
				editPlayerName(Constants.PLAYER_1);
				break;
			case R.id.button_setup_p2:
				editPlayerName(Constants.PLAYER_2); 
				break;
			case R.id.button_setup_p3:
				editPlayerName(Constants.PLAYER_3);
				break;
			case R.id.button_setup_p4:
				editPlayerName(Constants.PLAYER_4); 
				break;
			case R.id.button_setup_p1_mana:
				editPlayerMana(Constants.PLAYER_1);
				break;
			case R.id.button_setup_p2_mana:
				editPlayerMana(Constants.PLAYER_2); 
				break;
			case R.id.button_setup_p3_mana:
				editPlayerMana(Constants.PLAYER_3);
				break;
			case R.id.button_setup_p4_mana:
				editPlayerMana(Constants.PLAYER_4); 
				break;
			default:
				// Do nothing.
		}
	}

	/** 
	 * Create a dialog to set a player's name.
	 * @param player The int code for the player to edit
	 */
	private void editPlayerName(final int player) {
		AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this).create();
		alertDialog.setTitle("Edit Player Name");
		alertDialog.setMessage("Enter Player " + (player + 1) + "\'s name:");                
		// Set an EditText view to get the name   
		final EditText input = new EditText(this); 
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		input.setHint("Player " + (player + 1));
		input.setText(playerNames[player]);
		int position = input.length();
		Editable etext = input.getText();
		Selection.setSelection(etext, position);
		alertDialog.setView(input);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done", 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String name = input.getText().toString();
				// Check that it's a valid name
				if (name.matches("[ A-Za-z0-9!#$%&'()*+.:;<=>?@_`{|}~-]+")) {
					playerNames[player] = name;
					playerNameButtons[player].setText(name);
				} else {
					Toast.makeText(SetupActivity.this, "Invalid name", Toast.LENGTH_SHORT).show();
				}
			} });
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing
			} });
		alertDialog.show();
	}

	/**
	 * Create a dialog to set a player's mana icon.
	 * @param player The int code for the player to edit
	 * @param color The int code for the mana color to set
	 */
	private void setManaColor(int player, int color) {
		switch (color) {
			case Constants.MANA_WHITE:
				playerManaButtons[player].setBackgroundResource(R.drawable.mana_white);
				playerManaColors[player] = Constants.MANA_WHITE;
				break;
			case Constants.MANA_BLUE:
				playerManaButtons[player].setBackgroundResource(R.drawable.mana_blue);
				playerManaColors[player] = Constants.MANA_BLUE;
				break;
			case Constants.MANA_BLACK:
				playerManaButtons[player].setBackgroundResource(R.drawable.mana_black);
				playerManaColors[player] = Constants.MANA_BLACK;
				break;
			case Constants.MANA_RED:
				playerManaButtons[player].setBackgroundResource(R.drawable.mana_red);
				playerManaColors[player] = Constants.MANA_RED;
				break;
			case Constants.MANA_GREEN:
				playerManaButtons[player].setBackgroundResource(R.drawable.mana_green);
				playerManaColors[player] = Constants.MANA_GREEN;
				break;
			default: // MANA_COLORLESS
				playerManaButtons[player].setBackgroundResource(R.drawable.mana_colorless);
				playerManaColors[player] = Constants.MANA_COLORLESS;
				break;
		}
	}

	/**
	 * Dialog to set a player's mana color.
	 * @param player The int code for the player to edit
	 */
	private void editPlayerMana(final int player) {
		// Create a view for the dialog
		ScrollView scrollView = (ScrollView) View.inflate(this, R.layout.alertdialog_setup_mana, 
				null);
		final RadioGroup radioGroup = (RadioGroup) scrollView.findViewById(
				R.id.radiogroup_setup_mana);
		// Select their currently chosen color
		switch (playerManaColors[player]) {
			case Constants.MANA_WHITE:
				radioGroup.check(R.id.radio_setup_mana_white);
				break;
			case Constants.MANA_BLUE:
				radioGroup.check(R.id.radio_setup_mana_blue);
				break;
			case Constants.MANA_BLACK:
				radioGroup.check(R.id.radio_setup_mana_black);
				break;
			case Constants.MANA_RED:
				radioGroup.check(R.id.radio_setup_mana_red);
				break;
			case Constants.MANA_GREEN:
				radioGroup.check(R.id.radio_setup_mana_green);
				break;
			default: // MANA_COLORLESS
				radioGroup.check(R.id.radio_setup_mana_colorless);
				break;
		}

		// Create the dialog
		final AlertDialog alertDialog = new AlertDialog.Builder(SetupActivity.this).create();
		alertDialog.setTitle("Choose " + playerNames[player] + "\'s Mana Color");
		alertDialog.setView(scrollView);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done", 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				RadioGroup radioGroup = (RadioGroup) alertDialog.findViewById(
						R.id.radiogroup_setup_mana);
				switch (radioGroup.getCheckedRadioButtonId()) {
					case R.id.radio_setup_mana_white:
						setManaColor(player, Constants.MANA_WHITE);
						break;
					case R.id.radio_setup_mana_blue:
						setManaColor(player, Constants.MANA_BLUE);
						break;
					case R.id.radio_setup_mana_black:
						setManaColor(player, Constants.MANA_BLACK);
						break;
					case R.id.radio_setup_mana_red:
						setManaColor(player, Constants.MANA_RED);
						break;
					case R.id.radio_setup_mana_green:
						setManaColor(player, Constants.MANA_GREEN);
						break;
					default: // Colorless
						setManaColor(player, Constants.MANA_COLORLESS);
						break;
				}
			}
		});
		alertDialog.show();
	}

	/**
	 * Checks to see if it is possible to continue a game in progress.
	 * @return True if there exists a valid game that can be continued, else false
	 */
	private boolean checkForContinue() {
		// Verify that the number of players, player names, and teams match
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SetupActivity.this);
		int saved_num_players = pref.getInt(MainActivity.KEY_NUM_PLAYERS, -1);
		String[] temp_player_names = Arrays.copyOf(playerNames, numPlayers);
		Arrays.sort(temp_player_names); // Don't care about order for equality, so sort both
		String[] saved_player_names = pref.getString(MainActivity.KEY_PLAYER_NAMES, ",,,")
				.split(",", saved_num_players);
		Arrays.sort(saved_player_names);
		boolean teams = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.getBoolean("KEY_TEAM_TOGETHER", false);
		return pref.getBoolean(MainActivity.KEY_GAME_IN_PROGRESS, false) && saved_num_players 
				== numPlayers && Arrays.equals(temp_player_names, saved_player_names) 
				&& startingLife == pref.getInt(MainActivity.KEY_STARTING_LIFE, -1) 
				&& teams == teamTogetherCheckbox.isChecked();
	}

	/**
	 * Create the menu in the action bar.
	 * @param menu The menu to create.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.optionsMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_setup, menu);
		// Let the progress spinner go as they choose their options
		setWorkingActionButtonState(true);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handle clicks on the menu items.
	 * @param item The clicked menu item.
	 * @return Returns true if consumed here.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_setup_done:
				// Start the game! Pass all of the data through the extras.
				final Intent intent = new Intent(SetupActivity.this, MainActivity.class);
				boolean teams = teamTogetherCheckbox.isChecked();
				String[] team_names = null;
				if (teams) {
					team_names = new String[2];
					team_names[0] = playerNames[Constants.PLAYER_1] + (numPlayers > 2 ? " & " 
							+ playerNames[Constants.PLAYER_3] : "");
					team_names[1] = playerNames[Constants.PLAYER_2] + (numPlayers > 3 ? " & " 
							+ playerNames[Constants.PLAYER_4] : "");
				}
				intent.putExtra(MainActivity.KEY_NUM_PLAYERS, teams ? 2 : numPlayers);
				intent.putExtra(MainActivity.KEY_PLAYER_NAMES, teams ? team_names : playerNames);
				intent.putExtra(MainActivity.KEY_STARTING_LIFE, startingLife);
				intent.putExtra(MainActivity.KEY_MANA_COLOR, playerManaColors);
				// Give the option to continue a previous game instead, if applicable
				if (checkForContinue()) {
					String[] options = {"Create New Game", "Continue Game"};
					new AlertDialog.Builder(this)
					.setTitle("Continue Game?")
					.setItems(options, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int which) {
							intent.putExtra(MainActivity.KEY_CONTINUE_GAME, which);
							startActivity(intent);
						}
					}).show();
				} else {
					startActivity(intent);
				}
				return true;
			case R.id.menu_setup_shuffle_players:
				// Shuffle the order of players
				Random numgen = new Random();
				for (int i = 0; i < numPlayers; i++) {
					// Get random index
					int index = numgen.nextInt(numPlayers);
	
					// Swap name
					String temp_name = playerNames[index];
					playerNames[index] = playerNames[i];
					playerNames[i] = temp_name;
	
					// Swap mana color
					int temp_mana = playerManaColors[index];
					playerManaColors[index] = playerManaColors[i];
					playerManaColors[i] = temp_mana;
				}
				updateViews();
				return true;
			case R.id.menu_setup_settings:
				Intent intent2 = new Intent(SetupActivity.this, Prefs.class);
				startActivity(intent2);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Set the state of the working spinner.
	 * @param refreshing True if the spinner should animate.
	 */
	private void setWorkingActionButtonState(boolean refreshing) {
		if (optionsMenu != null) {
			MenuItem refreshItem = optionsMenu.findItem(R.id.menu_setup_working);
			if (refreshItem != null) {
				refreshItem.setActionView(refreshing ? R.layout.actionbar_indeterminate_progress 
						: null);
			}
		}
	}

	/**
	 * Save activity state when it's paused.
	 */
	@Override
	protected void onPause() {
		// Save the player names, etc.
		String names = playerNames[Constants.PLAYER_1] + ',' + playerNames[Constants.PLAYER_2] + ',' + playerNames[Constants.PLAYER_3] + ',' + playerNames[Constants.PLAYER_4];
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("KEY_PLAYER_NAMES", names).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("KEY_NUM_PLAYERS", numPlayers).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("KEY_TEAM_TOGETHER", teamTogetherCheckbox.isChecked()).commit();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("KEY_STARTING_LIFE", startingLife).commit();
		// Save the player mana colors
		String mana = playerManaColors[Constants.PLAYER_1] + "," + playerManaColors[Constants.PLAYER_2] + "," + playerManaColors[Constants.PLAYER_3] + "," + playerManaColors[Constants.PLAYER_4];
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("KEY_MANA_COLOR", mana).commit();

		super.onPause();
	}

	/**
	 * Save activity state if it may be killed.
	 * @param outState The Bundle to add my state information to. 
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("setup_num_players", numPlayers);
		outState.putStringArray("setup_player_names", playerNames);
		outState.putInt("setup_starting_life", startingLife);
		outState.putIntArray("setup_mana_color", playerManaColors);
		outState.putBoolean("setup_team_together", teamTogetherCheckbox.isChecked());
	}
}
