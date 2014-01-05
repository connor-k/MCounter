package com.connork.mtgcounter;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	// Some keys for passing information from setup
	public static String KEY_NUM_PLAYERS = "mtg_num_players";
	public static String KEY_PLAYER_NAMES = "mtg_player_names";
	public static String KEY_STARTING_LIFE = "mtg_starting_life";

	private int NUM_PLAYERS, STARTING_LIFE, selected_p13 = 1, selected_p24 = 2;
	private int[] life;
	private String[] PLAYER_NAMES;
	private TextView[] TEXTVIEW_LIFE;
	private SeekBar[] PROGRESSBAR_LIFE;
	private Button[] BUTTON_LIFE = new Button[8];
	private RadioGroup radiogroup_p13, radiogroup_p24;
	private RadioButton radio_button[] = new RadioButton[4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get passed information
		NUM_PLAYERS = getIntent().getIntExtra(KEY_NUM_PLAYERS, 2);
		String[] names = getIntent().getStringArrayExtra(KEY_PLAYER_NAMES);
		STARTING_LIFE = getIntent().getIntExtra(KEY_STARTING_LIFE, 20);

		PLAYER_NAMES = new String[NUM_PLAYERS];
		life = new int[NUM_PLAYERS];
		for (int i = 0; i < NUM_PLAYERS; i++) {
			PLAYER_NAMES[i] = names[i];
			life[i] = STARTING_LIFE;
		}

		// Restore last life state if possible
		if (savedInstanceState != null) {
			life = savedInstanceState.getIntArray("main_life");
		}

		TEXTVIEW_LIFE = new TextView[NUM_PLAYERS];
		PROGRESSBAR_LIFE = new SeekBar[NUM_PLAYERS];

		switch (NUM_PLAYERS) {
		case 4: 
			setContentView(R.layout.activity_main4);
			((TextView)findViewById(R.id.textview_main_p4_name)).setText(PLAYER_NAMES[3]);
			
			TEXTVIEW_LIFE[3] = (TextView)findViewById(R.id.textview_main_p4_life);
			PROGRESSBAR_LIFE[3] = (SeekBar) findViewById(R.id.progressbar_main_p4_life);
			radio_button[1] = (RadioButton) findViewById(R.id.radio_main_p2);
			radio_button[1].setText(PLAYER_NAMES[2]);
			radio_button[3] = (RadioButton) findViewById(R.id.radio_main_p4);
			radio_button[3].setText(PLAYER_NAMES[3]);
			radiogroup_p24 = (RadioGroup) findViewById(R.id.radiogroup_main_p24);
			radiogroup_p24.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) 
				{
					RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
					String text = checkedRadioButton.getText().toString();

					if (text.equals(radio_button[1].getText())) {
						selected_p24 = 2;
					}
					else if (text.equals(radio_button[3].getText())) {
						selected_p24 = 4;
					}
				}
			});
		case 3:
			if (NUM_PLAYERS == 3) {
				setContentView(R.layout.activity_main3);
			}
			((TextView)findViewById(R.id.textview_main_p3_name)).setText(PLAYER_NAMES[2]);
			
			TEXTVIEW_LIFE[2] = (TextView)findViewById(R.id.textview_main_p3_life);
			PROGRESSBAR_LIFE[2] = (SeekBar) findViewById(R.id.progressbar_main_p3_life);
			radio_button[0] = (RadioButton) findViewById(R.id.radio_main_p1);
			radio_button[0].setText(PLAYER_NAMES[0]);
			radio_button[2] = (RadioButton) findViewById(R.id.radio_main_p3);
			radio_button[2].setText(PLAYER_NAMES[2]);
			radiogroup_p13 = (RadioGroup) findViewById(R.id.radiogroup_main_p13);
			radiogroup_p13.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) 
				{
					RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
					String text = checkedRadioButton.getText().toString();

					if (text.equals(radio_button[0].getText())) {
						selected_p13 = 1;
					}
					else if (text.equals(radio_button[2].getText())) {
						selected_p13 = 3;
					}
				}
			});
		
		case 2:
			if (NUM_PLAYERS == 2) {
				setContentView(R.layout.activity_main);
			}
			// Set player names
			((TextView)findViewById(R.id.textview_main_p1_name)).setText(PLAYER_NAMES[0]);
			((TextView)findViewById(R.id.textview_main_p2_name)).setText(PLAYER_NAMES[1]);

			// Set buttons etc.
			TEXTVIEW_LIFE[0] = (TextView)findViewById(R.id.textview_main_p1_life);
			TEXTVIEW_LIFE[1] = (TextView)findViewById(R.id.textview_main_p2_life);
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

			// Setup life meters, and make the seekbars unmovable
			PROGRESSBAR_LIFE[0] = (SeekBar) findViewById(R.id.progressbar_main_p1_life);
			PROGRESSBAR_LIFE[1] = (SeekBar) findViewById(R.id.progressbar_main_p2_life);
			// Animate filling the life bars
			for (int i = 0; i < PROGRESSBAR_LIFE.length; i++) {
				final int player = i;
				ValueAnimator anim = ValueAnimator.ofInt(0, PROGRESSBAR_LIFE[player].getMax());
				anim.setDuration(4000);
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
			// Make life text visible after the animation
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					try {
						Thread.sleep(4300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;
				}
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);

					for (int i = 0; i < NUM_PLAYERS; i++) {
						TEXTVIEW_LIFE[i].setText(life[i] + " / " + STARTING_LIFE);
						TEXTVIEW_LIFE[i].setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
						// Prevent them from sliding the seekbar to change their life
						PROGRESSBAR_LIFE[i].setOnTouchListener(new OnTouchListener(){
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								return true;
							}
						});
					}
				}
			}.execute();

			break;
		}

		// Style the ActionBar
		ActionBar actionBar = getActionBar();
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.ab_texture_tile);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bMap);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		actionBar.setBackgroundDrawable(bitmapDrawable);
		actionBar.setDisplayHomeAsUpEnabled(true);

		//TODO Add settings, keep screen on
		//TODO Activity lifecycle stuff	
	}

	/** Update life and progress bars */
	void updateViews(int p, int increment) {
		final int player = p;
		life[player] += increment;
		TEXTVIEW_LIFE[player].setText(life[player] + " / " + STARTING_LIFE);
		
		ValueAnimator anim = ValueAnimator.ofInt((int)((life[player] + -1*increment)*1000.0/STARTING_LIFE), (int)(life[player]*1000.0/STARTING_LIFE));
		anim.setDuration(2000);
		anim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int animProgress = (Integer) animation.getAnimatedValue();
				PROGRESSBAR_LIFE[player].setProgress(animProgress);
			}
		});
		anim.start();

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
	}


	/** Handle when a button is clicked, call appropriate action */
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.button_main_p1_lifep1:
			updateViews( selected_p13 == 1?0:2, 1);
			break;
		case R.id.button_main_p1_lifep5:
			updateViews(selected_p13 == 1?0:2, 5);
			break;
		case R.id.button_main_p1_lifem1:
			updateViews(selected_p13 == 1?0:2, -1);
			break;
		case R.id.button_main_p1_lifem5:
			updateViews(selected_p13 == 1?0:2, -5);
			break;
		case R.id.button_main_p2_lifep1:
			updateViews(selected_p24 == 2?1:3, 1);
			break;
		case R.id.button_main_p2_lifep5:
			updateViews(selected_p24 == 2?1:3, 5);
			break;
		case R.id.button_main_p2_lifem1:
			updateViews(selected_p24 == 2?1:3, -1);
			break;
		case R.id.button_main_p2_lifem5:
			updateViews(selected_p24 == 2?1:3, -5);
			break;
		}

		for (int i = 0; i < life.length; i++) {
			if (life[i] < 0) {
				gameOver(i + 1);
				break;
			}
		}
	}

	/** Someone has won */
	private void gameOver(int player) {
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Player " + player + " has lost.");
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
	
	/** Use KitKat Immersive Mode if possible */
	@TargetApi(android.os.Build.VERSION_CODES.KITKAT)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	        super.onWindowFocusChanged(hasFocus);
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && hasFocus) {
	    	getWindow().getDecorView().setSystemUiVisibility(
	                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
	                | View.SYSTEM_UI_FLAG_FULLSCREEN
	                | View.SYSTEM_UI_FLAG_IMMERSIVE);}
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putIntArray("main_life", life);
	}
}