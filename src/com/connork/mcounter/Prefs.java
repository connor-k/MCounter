package com.connork.mcounter;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

/**
 * The preference screen for the application.
 */
public class Prefs extends PreferenceActivity {
	private static final String OPT_IMMERSIVE_MODE = "immersive_mode";
	private static final boolean OPT_IMMERSIVE_MODE_DEF = true;
	private static final String OPT_LIFE_COUNTER_TOUCHABLE = "life_counter_touchable";
	private static final boolean OPT_LIFE_COUNTER_TOUCHABLE_DEF = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Style the ActionBar
		ActionBar actionBar = getActionBar();
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.ab_texture_tile);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bMap);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		actionBar.setBackgroundDrawable(bitmapDrawable);
		actionBar.setDisplayHomeAsUpEnabled(true);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFragment())
			.commit();
	}

	/**
	 * A basic fragment to display the preferences.
	 */
	public static class PrefFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

	/**
	 * Get the value of the immersive mode preference.
	 * @param context The application context.
	 * @return Returns true if the game should display in immersive mode.
	 */
	public static boolean getImmersiveMode(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_IMMERSIVE_MODE, OPT_IMMERSIVE_MODE_DEF);
	}
	
	/**
	 * Get the value of the touchable seekbar preference.
	 * @param context The application context.
	 * @return True if the seekbar should move when touched.
	 */
	public static boolean getLifeCounterTouchable(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_LIFE_COUNTER_TOUCHABLE, OPT_LIFE_COUNTER_TOUCHABLE_DEF);
	}

	/**
	 * Handle clicks on the menu items (up button).
	 * @param item The clicked menu item.
	 * @return Returns true if consumed here.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {    
		case android.R.id.home:
			// Icon in action bar clicked; go home
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
