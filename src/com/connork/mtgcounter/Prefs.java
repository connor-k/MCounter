package com.connork.mtgcounter;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class Prefs extends PreferenceActivity
{
	private static final String OPT_IMMERSIVE_MODE = "immersive_mode";
	private static final boolean OPT_IMMERSIVE_MODE_DEF = true;
	private static final String OPT_LIFE_COUNTER_TOUCHABLE = "life_counter_touchable";
	private static final boolean OPT_LIFE_COUNTER_TOUCHABLE_DEF = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// Style the ActionBar
		ActionBar actionBar = getActionBar();
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.ab_texture_tile);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bMap);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		actionBar.setBackgroundDrawable(bitmapDrawable);
		actionBar.setDisplayHomeAsUpEnabled(true);

		//TODO tabbed prefs
	}

	public static boolean getImmersiveMode(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_IMMERSIVE_MODE, OPT_IMMERSIVE_MODE_DEF);
	}
	
	public static boolean getLifeCounterTouchable(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_LIFE_COUNTER_TOUCHABLE, OPT_LIFE_COUNTER_TOUCHABLE_DEF);
	}

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
