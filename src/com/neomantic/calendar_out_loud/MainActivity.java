package com.neomantic.calendar_out_loud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.media.AudioManager;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
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
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
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

	static final String TAG = MainActivity.class.getName();

	private static final String PREF_KEY_CALENDAR_LIST = "CALENDAR_LIST"; 
	private Script mScript;
	private boolean mRebuildAgendaScript = true;
	private Button mSpeakButton;
	private Editor mEditor;
	private Set<String> mPreferencesSet;

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

        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        mEditor = prefs.edit();
        mPreferencesSet = getCalendarIdsPrefs();
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
        
        getListView().setItemsCanFocus(false);
        setListAdapter(ca);
        mTTS = new TextToSpeech(this, this);
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	
	public void onInit(int status) {
		if( status == TextToSpeech.SUCCESS) {
			int result = mTTS.setLanguage(mTTS.getLanguage());
			if (result == TextToSpeech.LANG_MISSING_DATA ||
					result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(this, "Languaged Not Supported", Toast.LENGTH_SHORT);
			} else { 
				setSpeechVolume();
				mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
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
			}
		} else {
		   	Log.e("TTS", "Initilization Failed!");
		}

	}
	
	private void setSpeechVolume() {
		final AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(am.STREAM_MUSIC, am.getStreamMaxVolume(am.STREAM_SYSTEM), 0);		
	}

	@Override
	protected void onDestroy() {
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		super.onDestroy();
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
        final Agenda a = new Agenda(getContentResolver());
        final Set<String> set = getCalendarIdsPrefs();
        final Cursor events = a.events(set.toArray(new String[set.size()]));
        
        mScript = new Script();
        while(events.moveToNext()) {
        	mScript.add(new AgendaLine(events));
        }
	}
	
    protected void setSpeechButtonEnabled() {
		mSpeakButton.setText(R.string.start_speaking_agenda);
	}
    
    private Set<String> getCalendarIdsPrefs() {
    	return getPreferences(MODE_PRIVATE).getStringSet(PREF_KEY_CALENDAR_LIST, new HashSet<String>());
    }
}
