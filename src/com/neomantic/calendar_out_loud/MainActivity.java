package com.neomantic.calendar_out_loud;

import java.util.HashMap;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

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

	private Script mScript;

	private boolean mRebuildAgendaScript = true;

	private Button mSpeakButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mSpeakButton = (Button)findViewById(R.id.speak_button);
        mSpeakButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				final Button button = (Button) v; 
				if (mTTS.isSpeaking()) {
					mTTS.stop();
					MainActivity.this.setSpeechButtonEnabled();
				} else {
					MainActivity.this.speakAgenda();
					mSpeakButton.setText(R.string.stop_speaking_agenda);
				}
			}
        });
        
        final ContentResolver cr = getContentResolver();
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
        
        mTTS = new TextToSpeech(this, this);
        
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	
	public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
        	if ( mTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
        		mTTS.setLanguage(Locale.US);
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    
        /*mTTS.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
			
			public void onUtteranceCompleted(String utteranceId) {
				//this helped
				//http://stackoverflow.com/questions/4652969/android-tts-onutterancecompleted-callback-isnt-getting-called
				runOnUiThread(new Runnable() {
	        
	                public void run() {
	                	MainActivity.this.setSpeechButtonEnabled();
	                }
	            });

			}
        })*/;
	}
	
	protected void speakAgenda() {
		if (mRebuildAgendaScript) {
			buildAgendaScript();
			mRebuildAgendaScript = false;
		}
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stupidAndroidNeedsStringToHitOnUtteranceCompletedListner");
		mTTS.speak(mScript.write(getResources()), TextToSpeech.QUEUE_FLUSH, params);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mRebuildAgendaScript  = true;
		super.onListItemClick(l, v, position, id);
	}
	
	private void buildAgendaScript() {
        final Agenda a = new Agenda(this.getContentResolver());
        final Cursor events = a.events();
        mScript = new Script();
        while(events.moveToNext()) {
        	mScript.add(new AgendaLine(events));
        }
	}
	
    protected void setSpeechButtonEnabled() {
		mSpeakButton.setText(R.string.start_speaking_agenda);
	}

}
