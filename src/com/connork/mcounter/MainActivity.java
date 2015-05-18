package com.connork.mcounter;

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

import java.util.Random;

public class MainActivity extends Activity implements OnClickListener {
	// Keys for passing information from setup
	public static String KEY_NUM_PLAYERS = "num_players";
	public static String KEY_PLAYER_NAMES = "player_names";
	public static String KEY_STARTING_LIFE = "starting_life";
	public static String KEY_MANA_COLOR	= "player_icons";
	public static String KEY_CONTINUE_GAME = "continue_game";
	public static String KEY_GAME_IN_PROGRESS = "game_in_progress";
	public static String KEY_LIFE = "life";
	public static String KEY_ALIVE = "alive";

	private int NUM_PLAYERS;
	private boolean[] alive;
	private boolean gameStarted;
	private String[] playerNames;
	private int[] playerManaColors;
	private int STARTING_LIFE;
	// The current life of each player
	private int[] life;
	// The target life for the end of an animation
	private int[] lifeTarget;
	private TextView[] lifeTextViews;
	private SeekBar[] lifeSeekBars;
	private Button[] changeLifeButtons;
	// Integer designating which player the +- adjust buttons apply to
	private int trackPlayer13Toggle;
	private int trackPlayer24Toggle;
	private RadioGroup track13RadioGroup;
	private RadioGroup track24RadioGroup;
	private RadioButton trackPlayerRadioButtons[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get passed information and initialize player names, life, etc.
		NUM_PLAYERS = getIntent().getIntExtra(KEY_NUM_PLAYERS, 2);
		String[] names = getIntent().getStringArrayExtra(KEY_PLAYER_NAMES);
		int[] icons = getIntent().getIntArrayExtra(KEY_MANA_COLOR);
		// Set names and mana colors
		playerNames = new String[NUM_PLAYERS];
		playerManaColors = new int[NUM_PLAYERS];
		for (int i = 0; i < NUM_PLAYERS; ++i) {
			playerNames[i] = names[i];
			playerManaColors[i] = icons[i];
		}
		STARTING_LIFE = getIntent().getIntExtra(KEY_STARTING_LIFE, 20);
		life = new int[NUM_PLAYERS];
		// All players are *not* alive initially. Not playing = dead.
		alive = new boolean[Constants.MAX_PLAYERS];
		gameStarted = false;
		changeLifeButtons = new Button[8];
		trackPlayerRadioButtons = new RadioButton[4];
		lifeTextViews = new TextView[NUM_PLAYERS];
		lifeSeekBars = new SeekBar[NUM_PLAYERS];
		trackPlayer13Toggle = Constants.PLAYER_1;
		trackPlayer24Toggle = Constants.PLAYER_2;
		
		// If continuing a game, restore life/alive state
		if (getIntent().getIntExtra(KEY_CONTINUE_GAME, 0) == 1) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
					MainActivity.this);
			life = lifePrefConvert(prefs.getString(KEY_LIFE, "20,20,20,20"), NUM_PLAYERS);
			alive = alivePrefConvert(prefs.getString(KEY_ALIVE, "1111"), NUM_PLAYERS);
		} else if (savedInstanceState != null) {
			// Restore last life state if possible and they're not continuing
			life = savedInstanceState.getIntArray(KEY_LIFE);
			alive = savedInstanceState.getBooleanArray(KEY_ALIVE);
		} else {
			for (int i = 0; i < NUM_PLAYERS; ++i) {
				life[i] = STARTING_LIFE;
				alive[i] = true;
			}
		}

		// Set up all the views and change the layout based on the number of players
		switch (NUM_PLAYERS) {
			case Constants.MAX_PLAYERS:
				// Use the 4-player layout
				setContentView(R.layout.activity_main4);
	
				// Set Player 4's name and life info
				((TextView) findViewById(R.id.textview_main_p4_name)).setText(
						playerNames[Constants.PLAYER_4]);
				lifeTextViews[Constants.PLAYER_4] = (TextView) findViewById(
						R.id.textview_main_p4_life);
				lifeSeekBars[Constants.PLAYER_4] = (SeekBar) findViewById(
						R.id.progressbar_main_p4_life);
				lifeSeekBars[Constants.PLAYER_4].setThumb(getManaIcon(Constants.PLAYER_4));
				// The radio group to select who the +-life buttons route to (player 2 or 4) 
				trackPlayerRadioButtons[Constants.PLAYER_2] = (RadioButton) findViewById(
						R.id.radio_main_p2);
				trackPlayerRadioButtons[Constants.PLAYER_2].setText(playerNames[1]);
				trackPlayerRadioButtons[Constants.PLAYER_4] = (RadioButton) findViewById(
						R.id.radio_main_p4);
				trackPlayerRadioButtons[Constants.PLAYER_4].setText(playerNames[3]);
				track24RadioGroup = (RadioGroup) findViewById(R.id.radiogroup_main_p24);
				track24RadioGroup.setOnCheckedChangeListener(
						new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
							case R.id.radio_main_p2:
								trackPlayer24Toggle = Constants.PLAYER_2;
								break;
							case R.id.radio_main_p4:
								trackPlayer24Toggle = Constants.PLAYER_4;
								break;
							default:
								// Do nothing.	
						}
					}
				});
				// Enable/prevent them from sliding the SeekBar to change their life based on prefs
				lifeSeekBars[Constants.PLAYER_4].setOnSeekBarChangeListener(
						new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
						if (!fromUser || Prefs.getLifeCounterTouchable(MainActivity.this)) {
							life[Constants.PLAYER_4] = (int)((double) prog*STARTING_LIFE 
									/seekBar.getMax());
							updateViews(Constants.PLAYER_4);
						}
					}
	
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
	
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {}
				});
				// Fall through
			case (Constants.MAX_PLAYERS - 1):
				// Use the 3-player layout if this isn't 4-player
				if (NUM_PLAYERS == 3) {
					setContentView(R.layout.activity_main3);
				}
	
				// Set Player 3's name and life info
				((TextView) findViewById(R.id.textview_main_p3_name)).setText(
						playerNames[Constants.PLAYER_3]);
				lifeTextViews[Constants.PLAYER_3] = (TextView)findViewById(
						R.id.textview_main_p3_life);
				lifeSeekBars[Constants.PLAYER_3] = (SeekBar) findViewById(
						R.id.progressbar_main_p3_life);
				lifeSeekBars[Constants.PLAYER_3].setThumb(getManaIcon(Constants.PLAYER_3));
				// The radio group to select who the +-life buttons route to (player 1 or 3) 
				trackPlayerRadioButtons[Constants.PLAYER_1] = (RadioButton) findViewById(
						R.id.radio_main_p1);
				trackPlayerRadioButtons[Constants.PLAYER_1].setText(
						playerNames[Constants.PLAYER_1]);
				trackPlayerRadioButtons[Constants.PLAYER_3] = (RadioButton) findViewById(
						R.id.radio_main_p3);
				trackPlayerRadioButtons[Constants.PLAYER_3].setText(
						playerNames[Constants.PLAYER_3]);
				track13RadioGroup = (RadioGroup) findViewById(R.id.radiogroup_main_p13);
				track13RadioGroup.setOnCheckedChangeListener(
						new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
							case R.id.radio_main_p1:
								trackPlayer13Toggle = Constants.PLAYER_1;
								break;
							case R.id.radio_main_p3:
								trackPlayer13Toggle = Constants.PLAYER_3;
								break;
							default:
								// Do nothing.
						}
					}
				});
				// Enable/prevent them from sliding the SeekBar to change their life based on prefs
				lifeSeekBars[Constants.PLAYER_3].setOnSeekBarChangeListener(
						new SeekBar.OnSeekBarChangeListener() { 
					@Override
					public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
						if (!fromUser || Prefs.getLifeCounterTouchable(MainActivity.this)) {
							life[Constants.PLAYER_3] = (int)((double) prog*STARTING_LIFE 
									/seekBar.getMax());
							updateViews(Constants.PLAYER_3);
						}
					}
	
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
	
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {}
				});
				// Fall through
			case (Constants.MAX_PLAYERS - 2):
				// Use the 2-player layout if this isn't 3 or 4-player
				if (NUM_PLAYERS == 2) {
					setContentView(R.layout.activity_main);
				}
			
				// Set up the +-life buttons
				changeLifeButtons[0] = (Button) findViewById(R.id.button_main_p1_lifep1);
				changeLifeButtons[1] = (Button) findViewById(R.id.button_main_p1_lifep5);
				changeLifeButtons[2] = (Button) findViewById(R.id.button_main_p1_lifem1);
				changeLifeButtons[3] = (Button) findViewById(R.id.button_main_p1_lifem5);
				changeLifeButtons[4] = (Button) findViewById(R.id.button_main_p2_lifep1);
				changeLifeButtons[5] = (Button) findViewById(R.id.button_main_p2_lifep5);
				changeLifeButtons[6] = (Button) findViewById(R.id.button_main_p2_lifem1);
				changeLifeButtons[7] = (Button) findViewById(R.id.button_main_p2_lifem5);
				for (int i = 0; i < changeLifeButtons.length; ++i) {
					changeLifeButtons[i].setOnClickListener(this);
				}
	
				// Set Player 1 and 2's names and life info
				((TextView) findViewById(R.id.textview_main_p1_name)).setText(
						playerNames[Constants.PLAYER_1]);
				((TextView) findViewById(R.id.textview_main_p2_name)).setText(
						playerNames[Constants.PLAYER_2]);
				lifeTextViews[Constants.PLAYER_1] = (TextView)findViewById(
						R.id.textview_main_p1_life);
				lifeTextViews[Constants.PLAYER_2] = (TextView)findViewById(
						R.id.textview_main_p2_life);
				lifeSeekBars[Constants.PLAYER_1] = (SeekBar) findViewById(
						R.id.progressbar_main_p1_life);
				lifeSeekBars[Constants.PLAYER_1].setThumb(getManaIcon(Constants.PLAYER_1));
				lifeSeekBars[Constants.PLAYER_2] = (SeekBar) findViewById(
						R.id.progressbar_main_p2_life);
				lifeSeekBars[Constants.PLAYER_2].setThumb(getManaIcon(Constants.PLAYER_2));
	
				// Enable/prevent them from sliding the SeekBar to change their life based on prefs
				lifeSeekBars[Constants.PLAYER_1].setOnSeekBarChangeListener(
						new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
						if (!fromUser || Prefs.getLifeCounterTouchable(MainActivity.this)) {
							life[Constants.PLAYER_1] = (int)((double) prog*STARTING_LIFE
									/seekBar.getMax());
							updateViews(Constants.PLAYER_1);
						}
					}
	
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
	
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {}
				});
				lifeSeekBars[Constants.PLAYER_2].setOnSeekBarChangeListener(
						new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
						if (!fromUser || Prefs.getLifeCounterTouchable(MainActivity.this)) {
							life[Constants.PLAYER_2] = (int)((double) prog*STARTING_LIFE
									/seekBar.getMax());
							updateViews(Constants.PLAYER_2);
						}
					}
	
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
	
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {}
				});
				
				// Animate filling the life bars
				int[] temp = new int[NUM_PLAYERS];
				System.arraycopy(life, 0, temp, 0, life.length);
				lifeTarget = temp;
				for (int i = 0; i < lifeSeekBars.length; ++i) {
					final int player = i;
					ValueAnimator anim = ValueAnimator.ofInt(0, lifeSeekBars[player].getMax()
							*lifeTarget[player]/STARTING_LIFE);
					anim.setDuration(3000);
					anim.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							int animProgress = (Integer) animation.getAnimatedValue();
							lifeSeekBars[player].setProgress(animProgress);
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
						// Set the seekbar's maxes and progresses to be starting life of the
						//		player, scaling back down
						for (int i = 0; i < NUM_PLAYERS; ++i) {
							lifeSeekBars[i].setMax(STARTING_LIFE);
							life[i] = lifeTarget[i];
							lifeSeekBars[i].setProgress(life[i]);
							updateViews(i);
						}
						// Start checking if players have lost
						gameStarted = true;
					}
				}.execute();
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
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(
				KEY_GAME_IN_PROGRESS, true).commit();
	}

	/**
	 * Get the given player's mana icon for the life tracker
	 * @param player The integer code for the player to get the drawable for
	 * @return The drawable icon
	 */
	private Drawable getManaIcon(int player) {
		switch (playerManaColors[player]) {
			case 1:
				return getResources().getDrawable(R.drawable.mana_white);
			case 2:
				return getResources().getDrawable(R.drawable.mana_blue);
			case 3:
				return getResources().getDrawable(R.drawable.mana_black);
			case 4:
				return getResources().getDrawable(R.drawable.mana_red);
			case 5:
				return getResources().getDrawable(R.drawable.mana_green);
			default: // Colorless
				return getResources().getDrawable(R.drawable.mana_colorless);
		}
	}

	/**
	 * Update life text and bars for a specific player
	 * @param player The integer code for the player to update
	 */
	private void updateViews(int player) {
		lifeTextViews[player].setText(life[player] + " / " + STARTING_LIFE);

		// Color the text based on life
		int color = 0;
		if (life[player] > STARTING_LIFE) {
			color = android.R.color.holo_purple;
		} else if (life[player] > STARTING_LIFE*3.0/4.0) {
			color = android.R.color.holo_blue_dark;
		} else if (life[player] > STARTING_LIFE*2.0/4.0) {
			color = android.R.color.holo_green_dark;
		} else if (life[player] > STARTING_LIFE/4.0) {
			color = android.R.color.holo_orange_dark;
		} else if (life[player] <= STARTING_LIFE/4.0) {
			color = android.R.color.holo_red_dark;
		}
		lifeTextViews[player].setTextColor(getResources().getColor(color));

		if (gameStarted) {
			// See if this player lost because of the life change
			int aliveCount = 0;
			for (int i = 0; i < life.length; ++i) {
				if (life[i] <= 0 && alive[i]) {
					++aliveCount;
					alive[i] = false;
					gameOver(i);
				}
			}
			// See if only 1 player remains, set continue flag to false
			if (aliveCount == 1) {
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
						.putBoolean(KEY_GAME_IN_PROGRESS, false).commit();
			}
		}
	}

	/**
	 * Handle when a button is clicked, call appropriate action
	 * @param v The view clicked 
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_main_p1_lifep1:
				// Adjust the selected player's life, then update the textview
				life[trackPlayer13Toggle] += 1;
				lifeSeekBars[trackPlayer13Toggle].setProgress(life[trackPlayer13Toggle]);
				updateViews(trackPlayer13Toggle);
				break;
			case R.id.button_main_p1_lifep5:
				life[trackPlayer13Toggle] += 5;
				lifeSeekBars[trackPlayer13Toggle].setProgress(life[trackPlayer13Toggle]);
				updateViews(trackPlayer13Toggle);
				break;
			case R.id.button_main_p1_lifem1:
				life[trackPlayer13Toggle] -= 1;
				lifeSeekBars[trackPlayer13Toggle].setProgress(life[trackPlayer13Toggle]);
				updateViews(trackPlayer13Toggle);
				break;
			case R.id.button_main_p1_lifem5:
				life[trackPlayer13Toggle] -= 5;
				lifeSeekBars[trackPlayer13Toggle].setProgress(life[trackPlayer13Toggle]);
				updateViews(trackPlayer13Toggle);
				break;
			case R.id.button_main_p2_lifep1:
				life[trackPlayer24Toggle] += 1;
				lifeSeekBars[trackPlayer24Toggle].setProgress(life[trackPlayer24Toggle]);
				updateViews(trackPlayer24Toggle);
				break;
			case R.id.button_main_p2_lifep5:
				life[trackPlayer24Toggle] += 5;
				lifeSeekBars[trackPlayer24Toggle].setProgress(life[trackPlayer24Toggle]);
				updateViews(trackPlayer24Toggle);
				break;
			case R.id.button_main_p2_lifem1:
				life[trackPlayer24Toggle] -= 1;
				lifeSeekBars[trackPlayer24Toggle].setProgress(life[trackPlayer24Toggle]);
				updateViews(trackPlayer24Toggle);
				break;
			case R.id.button_main_p2_lifem5:
				life[trackPlayer24Toggle] -= 5;
				lifeSeekBars[trackPlayer24Toggle].setProgress(life[trackPlayer24Toggle]);
				updateViews(trackPlayer24Toggle);
				break;
			default:
				// Do nothing.
		}
	}

	/**
	 * Display a dialog for someone losing
	 * @param player The integer code for the player who lost
	 */
	private void gameOver(int player) {
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
		alertDialog.setTitle(playerNames[player] + " has lost.");
		alertDialog.setMessage("GG.");
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Continue", 
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Dismiss, do nothing.
			}
		});
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "End Game",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertDialog.show();
	}

	/**
	 * Use KitKat Immersive Mode if possible otherwise just fullscreen
	 * @param hasFocus Indicates whether the window has focus
	 */
	@TargetApi(android.os.Build.VERSION_CODES.KITKAT)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// Immersive mode for kitkat
		if (Prefs.getImmersiveMode(MainActivity.this) && android.os.Build.VERSION.SDK_INT 
				>= android.os.Build.VERSION_CODES.KITKAT && hasFocus) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE);
		} else if (Prefs.getImmersiveMode(MainActivity.this) && hasFocus) {
			// Fullscreen for non-kitkat devices
			MainActivity.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	/**
	 * Create the menu.
	 * @param menu The menu to create.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handle clicks on the menu items.
	 * @param item The clicked menu item.
	 * @return Returns true if consumed here.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_main_random_player:
				// Pick a random player
				Random numgen = new Random();
				int index = numgen.nextInt(NUM_PLAYERS);
				// Make an AlertDialog to show the randomly chosen player
				AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
				alertDialog.setTitle("Random Player:");
				alertDialog.setMessage(playerNames[index] + ".");
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Continue", 
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Dismiss, do nothing.
					}
				});
				alertDialog.show();
				return true;
			case R.id.menu_main_settings:
				Intent intent = new Intent(MainActivity.this, Prefs.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Convert a String[] to comma-separated String
	 * Used for storing the player names
	 * @param names The array to convert
	 * @return A comma-separated String with the data from the array 
	 */
	private String namesPrefConvert(String[] names) {
		String s = "";
		for (int i = 0; i < names.length; ++i) {
			s += names[i] + (i != names.length - 1 ? "," : "");
		}
		return s;
	}

	/**
	 * Convert an int[] to a String
	 * Used for storing icon codes 
	 * @param icons The string to split (no delimiter)
	 * @return A String with the codes concatenated together
	 */
	private String iconsPrefConvert(int[] icons) {
		String s = "";
		for (int i = 0; i < icons.length; ++i) {
			s += "" + icons[i];
		}
		return s;
	}

	/**
	 * Convert life String to an int[]
	 * Used for retrieving the life from storage 
	 * @param life The string to split (comma-separated)
	 * @param n The number of items in that String
	 * @return An integer array with the split life values
	 */
	private int[] lifePrefConvert(String life, int n) {
		String[] temp = life.split(",", n);
		int[] lifeValues = new int[n];
		for (int i = 0; i < n; ++i) {
			lifeValues[i] = Integer.parseInt(temp[i]);
		}
		return lifeValues;
	}

	/**
	 * Convert life int[] to a String
	 * Used for storing life values 
	 * @param life The array of life values
	 * @return A comma-separated String with the life values
	 */
	private String lifePrefConvert(int[] life) {
		String s = "";
		for (int i = 0; i < life.length; ++i) {
			s += Integer.toString(life[i]) + (i != life.length - 1 ? ',' : "");
		}
		return s;
	}

	/**
	 * Convert a String to boolean[]
	 * Used for retrieving alive flags from storage
	 * @param alive The string of alive flags to parse (no delimiter)
	 * @param n The number of alive flags in the string
	 * @return The array of alive flags
	 */
	private boolean[] alivePrefConvert(String alive, int n) {
		boolean[] aliveFlags = new boolean[n];
		for (int i = 0; i < aliveFlags.length; ++i) {
			aliveFlags[i] = (alive.charAt(i) == '1');
		}
		return aliveFlags;
	}

	/**
	 * Convert a boolean[] to a String
	 * Used for storing alive flags
	 * @param alive The array of alive flags
	 * @return A String with the alive flags (no delimiter)
	 */
	private String alivePrefConvert(boolean[] life) {
		String s = "";
		for (int i = 0; i < life.length; ++i) {
			s += (life[i] ? '1' : '0');
		}
		return s;
	}
	
	/**
	 * Save activity state if it may be killed.
	 * @param outState The Bundle to add my state information to. 
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putIntArray(KEY_LIFE, life);
		outState.putBooleanArray(KEY_ALIVE, alive);
	}

	/**
	 * Save activity state when it's paused.
	 */
	@Override
	public void onPause() {
		super.onPause();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Set life to target if the animator is running
		if (!gameStarted) {
			for (int i = 0; i < NUM_PLAYERS; ++i) {
				lifeSeekBars[i].setMax(STARTING_LIFE);
				life[i] = lifeTarget[i];
				lifeSeekBars[i].setProgress(life[i]);
				updateViews(i);
			}
		}
		
		// Save for continue game
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext());
		pref.edit().putInt(KEY_NUM_PLAYERS, NUM_PLAYERS).commit();
		pref.edit().putString(KEY_PLAYER_NAMES, namesPrefConvert(playerNames)).commit();
		pref.edit().putInt(KEY_STARTING_LIFE, STARTING_LIFE).commit();
		pref.edit().putString(KEY_MANA_COLOR, iconsPrefConvert(playerManaColors)).commit();
		pref.edit().putString(KEY_LIFE, lifePrefConvert(life)).commit();
		pref.edit().putString(KEY_ALIVE, alivePrefConvert(alive)).commit();
	}

	/**
	 * Restore keep screen on flag if activity resumed.
	 */
	@Override
	public void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
}
