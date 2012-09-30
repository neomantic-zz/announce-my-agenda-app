package com.neomantic.calendar_out_loud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Calendars;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

public class MainActivity extends ListActivity implements OnInitListener {


	private static final String[] CALENDAR_PROJECTION= new String[] {
		Calendars._ID, // 0 
		Calendars.ACCOUNT_NAME, //1 
		Calendars.ACCOUNT_TYPE, //2
		Calendars.CALENDAR_DISPLAY_NAME, //3
		Calendars.OWNER_ACCOUNT // 4
	};	
	
	public static final int ID_INDEX_CALENDAR_PROJECTION = 0;
	private static final int ACCOUNT_NAME_INDEX_CALENDAR_PROJECTION = 1;
	private static final int ACCOUNT_TYPE_INDEX_CALENDAR_PROJECTION = 2;
	private static final int CALENDAR_DISPLAY_NAME_INDEX_CALENDAR_PROJECTION = 3;
	private static final int OWNER_ACCOUNT_INDEX_CALENDAR_PROJECTION = 4;
		
	private TextToSpeech mTTS;

	static final String TAG = "CalendarOutLoud";

	private static final String PREF_KEY_CALENDAR_LIST = "CALENDAR_LIST"; 
	private Script mScript;
	private boolean mRebuildAgendaScript = true;
	private Button mSpeakButton;
	private Editor mEditor;
	private Set<String> mPreferencesSet = new HashSet<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mSpeakButton = (Button)findViewById(R.id.speak_button);
        mSpeakButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
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
        ArrayList<String> calendars = new ArrayList<String>();
        calendars.add("1");
        SimpleCursorAdapter ca = new SimpleCursorAdapter(
        		this,
        		android.R.layout.simple_list_item_multiple_choice,
        		cursor, 
        		new String[]{Calendars.CALENDAR_DISPLAY_NAME}, 
        		new int[]{android.R.id.text1},
        		CursorAdapter.FLAG_AUTO_REQUERY);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = prefs.edit();
        mPreferencesSet = prefs.getStringSet(PREF_KEY_CALENDAR_LIST, null);
        if (mPreferencesSet == null) {
        	mEditor.putStringSet(PREF_KEY_CALENDAR_LIST, (Set<String>)(new HashSet<String>()));
        	mEditor.commit();
        	mPreferencesSet = prefs.getStringSet(PREF_KEY_CALENDAR_LIST, null);
        }

        Log.v(TAG, "NumberOf: " + mPreferencesSet.size());
        
		ca.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				String calendarId = cursor.getString(ID_INDEX_CALENDAR_PROJECTION);
				if (view.getTag(R.integer.checkview_id_key) == null ) {
					view.setTag(R.integer.checkview_id_key, calendarId);	
				}
				final CheckedTextView checkedTextView = (CheckedTextView) view;
				if(mPreferencesSet.contains(calendarId)) {
					checkedTextView.setChecked(true);
				}
				checkedTextView.setOnClickListener( new View.OnClickListener() {
					public void onClick(View v) {
						final CheckedTextView itemView = (CheckedTextView)v;
						mRebuildAgendaScript = true;
						if (itemView.isChecked()) {
							mPreferencesSet.remove(itemView.getTag(R.integer.checkview_id_key));
						} else {
							mPreferencesSet.add((String) itemView.getTag(R.integer.checkview_id_key)); 	
						}
						mEditor.putStringSet(PREF_KEY_CALENDAR_LIST, mPreferencesSet);
						mEditor.commit();
						itemView.toggle();
					}
				});
				return false;
			}
		});
        
        final ListView view = getListView();
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
		if( status == TextToSpeech.SUCCESS) {
			mTTS.setOnUtteranceProgressListener( new UtteranceProgressListener() {
				@Override
				public void onDone(String arg0) {
					runOnUiThread(new Runnable() {
						public void run() {
							MainActivity.this.setSpeechButtonEnabled();    
				            }
				        });
					}

					@Override
					public void onError(String arg0) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onStart(String arg0) {
						// TODO Auto-generated method stub
					}
				});
            } else {
            	Log.e("TTS", "Initilization Failed!");
            }

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
