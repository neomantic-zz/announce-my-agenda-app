package com.neomantic.calendar_out_loud;

import java.util.Locale;

import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.NavUtils;

public class MainActivity extends ListActivity implements OnInitListener {


	private static final String[] PROJECTION= new String[] {
		Calendars._ID, 
		Calendars.ACCOUNT_NAME, 
		Calendars.ACCOUNT_TYPE,
		Calendars.CALENDAR_DISPLAY_NAME,
		Calendars.OWNER_ACCOUNT
	};
	private TextToSpeech mTTS;
	private int MY_DATA_CHECK_CODE = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Cursor cursor = getContentResolver().query(Calendars.CONTENT_URI, PROJECTION, null, null, Calendars.ACCOUNT_NAME);
        SimpleCursorAdapter ca = new SimpleCursorAdapter(
        		this,
        		android.R.layout.simple_list_item_multiple_choice,
        		cursor, 
        		new String[]{Calendars.CALENDAR_DISPLAY_NAME}, 
        		new int[]{android.R.id.text1},
        		CursorAdapter.FLAG_AUTO_REQUERY);
        ListView view = this.getListView();
        view.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        view.setItemsCanFocus(false);
        this.setListAdapter(ca);
        
        mTTS = new TextToSpeech(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
        	if ( mTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
        		mTTS.setLanguage(Locale.US);
        } else {
            Log.e("TTS", "Initilization Failed!");
        }		
	}
	
    //speak the user text
    private void speakWords(String speech) {
         mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		speakWords(((TextView) v.findViewById(android.R.id.text1)).getText().toString());
		super.onListItemClick(l, v, position, id);
	}

}
