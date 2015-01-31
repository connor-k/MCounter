package com.connork.mcounter;

import java.util.Random;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	// Some keys for passing information from setup
	public static String KEY_NUM_PLAYERS	= "num_players";
	public static String KEY_PLAYER_NAMES	= "player_names";
	public static String KEY_STARTING_LIFE	= "starting_life";
	public static String KEY_MANA_COLOR	= "player_icons";
	public static String KEY_CONTINUE_GAME	= "continue_game";
	public static String KEY_GAME_IN_PROGRESS = "game_in_progress";
	public static String KEY_LIFE	= "life";
	public static String KEY_ALIVE	= "alive";

	private int NUM_PLAYERS, STARTING_LIFE, selected_p13 = 0, selected_p24 = 1;
	public static int PLAYER_1 = 0, PLAYER_2 = 1, PLAYER_3 = 2, PLAYER_4 = 3;
	private int[] life;
	private boolean[] alive = {false, false, false, false};
	private boolean game_started;
	private String[] PLAYER_NAMES;
	private int[] PLAYER_ICONS;
	private TextView[] TEXTVIEW_LIFE;
	private SeekBar[] PROGRESSBAR_LIFE;
	private Button[] BUTTON_LIFE = new Button[8];
	private RadioGroup radiogroup_p13, radiogroup_p24;
	private RadioButton radio_button[] = new RadioButton[4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		game_started = false;
		
		// Get passed information and initialize player names, life, etc.
		String[] names;
		int[] icons;
		NUM_PLAYERS = getIntent().getIntExtra(KEY_NUM_PLAYERS, 2);
		names = getIntent().getStringArrayExtra(KEY_PLAYER_NAMES);
		STARTING_LIFE = getIntent().getIntExtra(KEY_STARTING_LIFE, 20);
		icons = getIntent().getIntArrayExtra(KEY_MANA_COLOR);
		life = new int[NUM_PLAYERS];
		// If continuing a game, restore live/alive state
		if (getIntent().getIntExtra(KEY_CONTINUE_GAME, 0) == 1) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			life = lifePrefConvert(prefs.getString(KEY_LIFE, "20,20,20,20"), NUM_PLAYERS);
			alive = alivePrefConvert(prefs.getString(KEY_ALIVE, "1111"), NUM_PLAYERS);
		}
		else {
			for (int i = 0; i < NUM_PLAYERS; ++i) {
				life[i] = STARTING_LIFE;
				alive[i] = true;
			}
		}

		PLAYER_NAMES = new String[NUM_PLAYERS];
		PLAYER_ICONS = new int[NUM_PLAYERS];
		for (int i = 0; i < NUM_PLAYERS; i++) {
			PLAYER_NAMES[i] = names[i];
			PLAYER_ICONS[i] = icons[i];
		}

		// Restore last life state if possible
		if (savedInstanceState != null) {
			life = savedInstanceState.getIntArray(KEY_LIFE);
			alive = savedInstanceState.getBooleanArray(KEY_ALIVE);
		}

		// The textview and progressbars to keep track of player life
		TEXTVIEW_LIFE = new TextView[NUM_PLAYERS];
		PROGRESSBAR_LIFE = new SeekBar[NUM_PLAYERS];

		// Set up all the views and change the layout based on the number of players
		// The switch has no breaks, so each case sets up that player number (with case 2 setting up players 1 and 2)
		switch (NUM_PLAYERS) {
		case 4: 
			// Use the 4-player layout
			setContentView(R.layout.activity_main4);

			// Set Player 4's name
			((TextView)findViewById(R.id.textview_main_p4_name)).setText(PLAYER_NAMES[PLAYER_4]);

			// Player 4's life text and progressbar
			TEXTVIEW_LIFE[PLAYER_4] = (TextView)findViewById(R.id.textview_main_p4_life);
			PROGRESSBAR_LIFE[PLAYER_4] = (SeekBar) findViewById(R.id.progressbar_main_p4_life);
			// Set mana icon
			PROGRESSBAR_LIFE[PLAYER_4].setThumb(getManaIcon(PLAYER_4));
			// The radio group to select who the +-life buttons route to (player 2 or 4) 
			radio_button[PLAYER_2] = (RadioButton) findViewById(R.id.radio_main_p2);
			radio_button[PLAYER_2].setText(PLAYER_NAMES[1]);
			radio_button[PLAYER_4] = (RadioButton) findViewById(R.id.radio_main_p4);
			radio_button[PLAYER_4].setText(PLAYER_NAMES[3]);
			radiogroup_p24 = (RadioGroup) findViewById(R.id.radiogroup_main_p24);
			radiogroup_p24.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) 
				{
					RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
					String text = checkedRadioButton.getText().toString();

					if (text.equals(radio_button[PLAYER_2].getText())) {
						selected_p24 = PLAYER_2;
					}
					else if (text.equals(radio_button[PLAYER_4].getText())) {
						selected_p24 = PLAYER_4;
					}
				}
			});
			// Check settings to enable/prevent them from sliding the seekbar to change their life.
			PROGRESSBAR_LIFE[PLAYER_4].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { 
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// Check settings to enable/prevent them from sliding the seekbar to change their life.
					if (!fromUser || Prefs.getLifeCounterTouchable(MainActivity.this)) {
						life[PLAYER_4] = (int)((double)progress * STARTING_LIFE / seekBar.getMax());
						updateViews(PLAYER_4);
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
		case 3:
			// Use the 3-player layout
			if (NUM_PLAYERS == 3) {
				setContentView(R.layout.activity_main3);
			}

			// Set Player 3's name
			((TextView)findViewById(R.id.textview_main_p3_name)).setText(PLAYER_NAMES[PLAYER_3]);

			// Player 3's life text and progressbar
			TEXTVIEW_LIFE[PLAYER_3] = (TextView)findViewById(R.id.textview_main_p3_life);
			PROGRESSBAR_LIFE[PLAYER_3] = (SeekBar) findViewById(R.id.progressbar_main_p3_life);
			// Set mana icon
			PROGRESSBAR_LIFE[PLAYER_3].setThumb(getManaIcon(PLAYER_3));
			// The radio group to select who the +-life buttons route to (player 1 or 3) 
			radio_button[PLAYER_1] = (RadioButton) findViewById(R.id.radio_main_p1);
			radio_button[PLAYER_1].setText(PLAYER_NAMES[PLAYER_1]);
			radio_button[PLAYER_3] = (RadioButton) findViewById(R.id.radio_main_p3);
			radio_button[PLAYER_3].setText(PLAYER_NAMES[PLAYER_3]);
			radiogroup_p13 = (RadioGroup) findViewById(R.id.radiogroup_main_p13);
			radiogroup_p13.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) 
				{
					RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
					String text = checkedRadioButton.getText().toString();

					if (text.equals(radio_button[PLAYER_1].getText())) {
						selected_p13 = PLAYER_1;
					}
					else if (text.equals(radio_button[PLAYER_3].getText())) {
						selected_p13 = PLAYER_3;
					}
				}
			});
			// Check settings to enable/prevent them from sliding the seekbar to change their life.
			PROGRESSBAR_LIFE[PLAYER_3].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { 
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// Check settings to enable/prevent them from sliding the seekbar to change their life.
					if (!fromUser || Prefs.getLifeCounterTouchable(MainActivity.this)) {
						life[PLAYER_3] = (int)((double)progress * STARTING_LIFE / seekBar.getMax());
						updateViews(PLAYER_3);
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
		case 2:
			// Use the 2-player layout
			if (NUM_PLAYERS == 2) {
				setContentView(R.layout.activity_main);
			}

			// Set Player 1 and 2's names
			((TextView)findViewById(R.id.textview_main_p1_name)).setText(PLAYER_NAMES[PLAYER_1]);
			((TextView)findViewById(R.id.textview_main_p2_name)).setText(PLAYER_NAMES[PLAYER_2]);

			// Set up the +-life buttons
			BUTTON_LIFE[0] = (Button) findViewById(R.id.button_main_p1_lifep1);
			BUTTON_LIFE[1] = (Button) findViewById(R.id.button_main_p1_lifep5);
			BUTTON_LIFE[2] = (Button) findViewById(R.id.button_main_p1_lifem1);
			BUTTON_LIFE[3] = (Button) findViewById(R.id.button_main_p1_lifem5);
			BUTTON_LIFE[4] = (Button) findViewById(R.id.button_main_p2_lifep1);
			BUTTON_LIFE[5] = (Button) findViewById(R.id.button_main_p2_lifep5);
			BUTTON_LIFE[6] = (Button) findViewById(R.id.button_main_p2_lifem1);
			BUTTON_LIFE[7] = (Button) findViewById(R.id.button_main_p2_lifem5);
			for (int i = 0; i < BUTTON_LIFE.length; i++) {
				BUTTON_LIFE[i].setOnClickListener(this);
			}

			// Setup Player 1 and 2's life text and progressbars
			TEXTVIEW_LIFE[PLAYER_1] = (TextView)findViewById(R.id.textview_main_p1_life);
			TEXTVIEW_LIFE[PLAYER_2] = (TextView)findViewById(R.id.textview_main_p2_life);

			PROGRESSBAR_LIFE[PLAYER_1] = (SeekBar) findViewById(R.id.progressbar_main_p1_life);
			// Set mana icon
			PROGRESSBAR_LIFE[PLAYER_1].setThumb(getManaIcon(PLAYER_1));

			PROGRESSBAR_LIFE[PLAYER_2] = (SeekBar) findViewById(R.id.progressbar_main_p2_life);
			// Set mana icon
			PROGRESSBAR_LIFE[PLAYER_2].setThumb(getManaIcon(PLAYER_2));

			// Animate filling the life bars if new game
			int[] temp = new int[NUM_PLAYERS];
			System.arraycopy(life, 0, temp, 0, life.length);
			final int[] life_target = temp;
			for (int i = 0; i < PROGRESSBAR_LIFE.length; i++) {
				final int player = i;

				ValueAnimator anim = ValueAnimator.ofInt(0, PROGRESSBAR_LIFE[player].getMax()*life_target[player]/STARTING_LIFE);
				anim.setDuration(3000);
				anim.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						int animProgress = (Integer) animation.getAnimatedValue();
						PROGRESSBAR_LIFE[player].setProgress(animProgress);
					}
				});
				anim.setStartDelay(300);
				anim.start();
			}

			// Make life text visible after the animation, and set max progress to initial life
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					try {
						Thread.sleep(3300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;
				}
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					// Set the seekbar's maxes and progresses to be starting life of the player, scaling back down
					for (int i = 0; i < NUM_PLAYERS; i++) {
						PROGRESSBAR_LIFE[i].setMax(STARTING_LIFE);
						life[i] = life_target[i];
						PROGRESSBAR_LIFE[i].setProgress(life[i]);
						updateViews(i);
					}
					// Start checking if players have lost
					game_started = true;
				}
			}.execute();

			// Player 2
			PROGRESSBAR_LIFE[PLAYER_2].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { 
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// Check settings to enable/prevent them from sliding the seekbar to change their life.
					if (!fromUser || Prefs.getLifeCounterTouchable(MainActivity.this)) {
						life[PLAYER_2] = (int)((double)progress * STARTING_LIFE / seekBar.getMax());
						updateViews(PLAYER_2);
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			// Player 1
			PROGRESSBAR_LIFE[PLAYER_1].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { 
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// Check settings to enable/prevent them from sliding the seekbar to change their life.
					if ((!fromUser && !game_started) || (fromUser && Prefs.getLifeCounterTouchable(MainActivity.this))) {
						life[PLAYER_1] = (int)((double)progress * STARTING_LIFE / seekBar.getMax());
						updateViews(PLAYER_1);
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}

			});
			break;
		}

		// Style the ActionBar
		ActionBar actionBar = getActionBar();
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.ab_texture_tile);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bMap);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		actionBar.setBackgroundDrawable(bitmapDrawable);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Add flag to continue this game
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(KEY_GAME_IN_PROGRESS, true).commit();
	}

	/** Get the given player's mana icon for the life tracker **/
	private Drawable getManaIcon(int player) {
		Drawable output = null;
		switch (PLAYER_ICONS[player]) {
		case 0:
			output = getResources().getDrawable(R.drawable.mana_colorless);
			break;
		case 1:
			output = getResources().getDrawable(R.drawable.mana_white);
			break;
		case 2:
			output = getResources().getDrawable(R.drawable.mana_blue);
			break;
		case 3:
			output = getResources().getDrawable(R.drawable.mana_black);
			break;
		case 4:
			output = getResources().getDrawable(R.drawable.mana_red);
			break;
		case 5:
			output = getResources().getDrawable(R.drawable.mana_green);
			break;
		}
		return output;
	}

	/** Update life for a specific player */
	void updateViews(int player) {
		TEXTVIEW_LIFE[player].setText(life[player] + " / " + STARTING_LIFE);

		// Color the text based on life
		int color = 0;
		if (life[player] > STARTING_LIFE) {
			color = android.R.color.holo_purple;
		}
		else if (life[player] > STARTING_LIFE*3.0/4.0) {
			color = android.R.color.holo_blue_dark;
		}
		else if (life[player] > STARTING_LIFE*2.0/4.0) {
			color = android.R.color.holo_green_dark;
		}
		else if (life[player] > STARTING_LIFE/4.0) {
			color = android.R.color.holo_orange_dark;
		}
		else if (life[player] <= STARTING_LIFE/4.0) {
			color = android.R.color.holo_red_dark;
		}
		TEXTVIEW_LIFE[player].setTextColor(getResources().getColor(color));

		if (game_started) {
			int alive_count = 0; // For final winner dialog
			for (int i = 0; i < life.length; i++) {
				if (life[i] <= 0) {
					// If someone has 0 or less life, make a gg dialog and mark them as dead (avoid multiple dialogs for same person).
					if (alive[i]) {
						++alive_count;
						alive[i] = false;
						gameOver(i);
						break;
					}
				}
			}
			if (alive_count == 1) {
				// Set continue flag to false
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(KEY_GAME_IN_PROGRESS, false).commit();
			}
		}
	}

	/** Handle when a button is clicked, call appropriate action */
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.button_main_p1_lifep1:
			// Adjust the selected player's life, then update the textview
			life[selected_p13] += 1;
			PROGRESSBAR_LIFE[selected_p13].setProgress(life[selected_p13]);
			updateViews(selected_p13);
			break;
		case R.id.button_main_p1_lifep5:
			life[selected_p13] += 5;
			PROGRESSBAR_LIFE[selected_p13].setProgress(life[selected_p13]);
			updateViews(selected_p13);
			break;
		case R.id.button_main_p1_lifem1:
			life[selected_p13] -= 1;
			PROGRESSBAR_LIFE[selected_p13].setProgress(life[selected_p13]);
			updateViews(selected_p13);
			break;
		case R.id.button_main_p1_lifem5:
			life[selected_p13] -= 5;
			PROGRESSBAR_LIFE[selected_p13].setProgress(life[selected_p13]);
			updateViews(selected_p13);
			break;
		case R.id.button_main_p2_lifep1:
			life[selected_p24] += 1;
			PROGRESSBAR_LIFE[selected_p24].setProgress(life[selected_p24]);
			updateViews(selected_p24);
			break;
		case R.id.button_main_p2_lifep5:
			life[selected_p24] += 5;
			PROGRESSBAR_LIFE[selected_p24].setProgress(life[selected_p24]);
			updateViews(selected_p24);
			break;
		case R.id.button_main_p2_lifem1:
			life[selected_p24] -= 1;
			PROGRESSBAR_LIFE[selected_p24].setProgress(life[selected_p24]);
			updateViews(selected_p24);
			break;
		case R.id.button_main_p2_lifem5:
			life[selected_p24] -= 5;
			PROGRESSBAR_LIFE[selected_p24].setProgress(life[selected_p24]);
			updateViews(selected_p24);
			break;
		}
	}

	/** Someone has lost */
	private void gameOver(int player) {
		// Make an AlertDialog with a loss message
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
		alertDialog.setTitle(PLAYER_NAMES[player] + " has lost.");
		alertDialog.setMessage("GG.");
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Continue", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//Dismiss
			} });
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "End Game", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			} });
		alertDialog.show();
	}

	/** Use KitKat Immersive Mode if possible otherwise just fullscreen */
	@TargetApi(android.os.Build.VERSION_CODES.KITKAT)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// Immersive mode for kitkat
		if (Prefs.getImmersiveMode(MainActivity.this) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && hasFocus) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE);}
		// Fullscreen for non-kitkat devices
		else if (Prefs.getImmersiveMode(MainActivity.this) && hasFocus) {
			MainActivity.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_main_random_player:
			// Get random index
			Random numgen = new Random();
			int index = numgen.nextInt(NUM_PLAYERS);
			
			// Make an AlertDialog to show the randomly chosen player
			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Random Player:");
			alertDialog.setMessage(PLAYER_NAMES[index] + ".");
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Continue", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//Dismiss
				} });
			alertDialog.show();
			return true;
		case R.id.menu_main_settings:
			Intent intent = new Intent(MainActivity.this, Prefs.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* Convert String to String array of player names */
	String[] namesPrefConvert(String names, int n) {
		String[] s = names.split(",", n);
		return s;
	}
	
	/* Convert String[] to String of player names */
	String namesPrefConvert(String[] names) {
		String s = "";
		for (int i = 0; i < names.length; ++i) {
			s += names[i] + (i != names.length - 1 ? "," : "");
		}
		return s;
	}
	
	/* Convert icons string to int[] */
	int[] iconsPrefConvert(String icons, int n) {
		int[] ic = new int[n];
		for (int i = 0; i < n; ++i) {
			ic[i] = icons.charAt(i) - '0';
		}
		return ic;
	}
	
	/* Convert icons int[] to string */
	String iconsPrefConvert(int[] icons) {
		String s = "";
		for (int i = 0; i < icons.length; ++i) {
			s += "" + icons[i];
		}
		return s;
	}
	
	/* Convert life string to int[] */
	int[] lifePrefConvert(String life, int n) {
		String[] temp = life.split(",", n);
		int[] li = new int[n];
		for (int i = 0; i < n; ++i) {
			li[i] = Integer.parseInt(temp[i]);
		}
		return li;
	}
	
	/* Convert life int[] to string */
	String lifePrefConvert(int[] life) {
		String s = "";
		for (int i = 0; i < life.length; ++i) {
			s += Integer.toString(life[i]) + (i != life.length - 1 ? ',' : "");
		}
		return s;
	}
	
	/* Convert alive string to boolean[] */
	boolean[] alivePrefConvert(String alive, int n) {
		boolean[] al = new boolean[n];
		for (int i = 0; i < al.length; ++i) {
			al[i] = (alive.charAt(i) == '1');
		}
		return al;
	}
	
	/* Convert alive boolean[] to string */
	String alivePrefConvert(boolean[] life) {
		String s = "";
		for (int i = 0; i < life.length; ++i) {
			s += (life[i] ? '1' : '0');
		}
		return s;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putIntArray(KEY_LIFE, life);
		outState.putBooleanArray(KEY_ALIVE, alive);
	}

	@Override
	public void onPause() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Save for continue game
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		pref.edit().putInt(KEY_NUM_PLAYERS, NUM_PLAYERS).commit();
		pref.edit().putString(KEY_PLAYER_NAMES, namesPrefConvert(PLAYER_NAMES)).commit();
		pref.edit().putInt(KEY_STARTING_LIFE, STARTING_LIFE).commit();
		pref.edit().putString(KEY_MANA_COLOR, iconsPrefConvert(PLAYER_ICONS)).commit();
		pref.edit().putString(KEY_LIFE, lifePrefConvert(life)).commit();
		pref.edit().putString(KEY_ALIVE, alivePrefConvert(alive)).commit();
		
		super.onPause();
	}

	@Override
	public void onResume() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onPause();
	}
}