package com.neomantic.calendar_out_loud;

import java.util.Locale;

import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class MainActivity extends ListActivity implements OnInitListener {


	private static final String[] CALENDAR_PROJECTION= new String[] {
		Calendars._ID, // 0 
		Calendars.ACCOUNT_NAME, //1 
		Calendars.ACCOUNT_TYPE, //2
		Calendars.CALENDAR_DISPLAY_NAME, //3
		Calendars.OWNER_ACCOUNT // 4
	};	
	
	private static final int ID_INDEX_CALENDAR_PROJECTION = 0;
	private static final int ACCOUNT_NAME_INDEX_CALENDAR_PROJECTION = 1;
	private static final int ACCOUNT_TYPE_INDEX_CALENDAR_PROJECTION = 2;
	private static final int CALENDAR_DISPLAY_NAME_INDEX_CALENDAR_PROJECTION = 3;
	private static final int OWNER_ACCOUNT_INDEX_CALENDAR_PROJECTION = 4;
		
	private TextToSpeech mTTS;

	static final String TAG = "CalendarOutLoud"; 

	private Script script;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(Calendars.CONTENT_URI, CALENDAR_PROJECTION, null, null, Calendars.ACCOUNT_NAME);
        SimpleCursorAdapter ca = new SimpleCursorAdapter(
        		this,
        		android.R.layout.simple_list_item_multiple_choice,
        		cursor, 
        		new String[]{Calendars.CALENDAR_DISPLAY_NAME}, 
        		new int[]{android.R.id.text1},
        		CursorAdapter.FLAG_AUTO_REQUERY);
        ListView view = getListView();
        view.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        view.setItemsCanFocus(false);
        this.setListAdapter(ca);
        Agenda a = new Agenda(this.getContentResolver());
        
        Cursor ev = a.events();
        Resources res = getResources();
        script = new Script();
        while(ev.moveToNext()) {
        	script.add(new AgendaLine(ev));
        }

        Log.i(TAG, script.write(res));

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
         mTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		speakWords(script.write(getResources()));
		speakWords(((TextView) v.findViewById(android.R.id.text1)).getText().toString());
		super.onListItemClick(l, v, position, id);
	}

}
