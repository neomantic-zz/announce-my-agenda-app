package com.neomantic.calendar_out_loud;

import java.text.Format;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.text.format.Time;
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


	private static final String[] CALENDAR_PROJECTION= new String[] {
		Calendars._ID, 
		Calendars.ACCOUNT_NAME, 
		Calendars.ACCOUNT_TYPE,
		Calendars.CALENDAR_DISPLAY_NAME,
		Calendars.OWNER_ACCOUNT
	};	
	
	private static final int ID_INDEX_CALENDAR_PROJECTION = 0;
	private static final int ACCOUNT_NAME_INDEX_CALENDAR_PROJECTION = 1;
	private static final int ACCOUNT_TYPE_INDEX_CALENDAR_PROJECTION = 2;
	private static final int CALENDAR_DISPLAY_NAME_INDEX_CALENDAR_PROJECTION = 3;
	private static final int OWNER_ACCOUNT_INDEX_CALENDAR_PROJECTION = 3;

	
	private static final String[] EVENT_PROJECTION = new String[] {
			CalendarContract.Events.CALENDAR_ID,
			CalendarContract.Events.TITLE,
			CalendarContract.Events.CALENDAR_DISPLAY_NAME,
			CalendarContract.Events.EVENT_LOCATION,
			CalendarContract.Events.DTSTART,
			CalendarContract.Events.DTEND
	};

	private static final int TITLE_INDEX_EVENT_PROJECTION = 1;
	private static final int DESCRIPTION_INDEX_EVENT_PROJECTION = 2;
	private static final int EVENT_LOCATION_INDEX_EVENT_PROJECTION = 3;
	private static final int DSTART_EVENT_INDEX_EVENT_PROJECTION = 4;
	private static final int DTEND_EVENT_INDEX_EVENT_PROJECTION = 5;

	
	private static final String [] INSTANCE_PROJECTION = new String[] {
		Instances.TITLE,
		Instances.BEGIN,
		Instances.EVENT_ID,
		Calendars._ID,
		Calendars.VISIBLE,
	};
	private static final int TITLE_INDEX_INSTANCE_PROJECTION = 0;
	private static final int BEGIN_INDEX_INSTANCE_PROJECTION = 1;
	private static final int EVENT_ID_INDEX_INSTANCE_PROJECTION = 2;
	private static final int CALENDAR_ID_INDEX_INSTANCE_PROJECTION = 3;
	private static final int CALENDAR_VISIBILE_INDEX_INSTANCE_PROJECTION = 4;
	
	private TextToSpeech mTTS;

	static final String TAG = "CalendarOutLoud"; 
	
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
        
        while(cursor.moveToNext()) {
        	Log.i(TAG, "ID: " + cursor.getString(ID_INDEX_CALENDAR_PROJECTION));
        	Log.i(TAG, "AccountName: " + cursor.getString(ACCOUNT_NAME_INDEX_CALENDAR_PROJECTION));
        	Log.i(TAG, "Display Name: " + cursor.getString(CALENDAR_DISPLAY_NAME_INDEX_CALENDAR_PROJECTION));
        	Log.i(TAG, "Owner Account: " + cursor.getString(OWNER_ACCOUNT_INDEX_CALENDAR_PROJECTION));
        	Log.i(TAG, "Account Type: " + cursor.getString(ACCOUNT_TYPE_INDEX_CALENDAR_PROJECTION));
        }
        
        cursor.moveToPosition(4);
        
        //if (cursor.moveToNext()) {

        	/*String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
        		+ Calendars.ACCOUNT_TYPE + " = ?) AND ("
        		+ Calendars.OWNER_ACCOUNT + " = ?))";
        	
        		String[] selectionArgs = new String[] {
        				cursor.getString(cursor.getColumnIndex(Calendars.ACCOUNT_NAME)),
        				cursor.getString(cursor.getColumnIndex(Calendars.ACCOUNT_TYPE)),
        				cursor.getString(cursor.getColumnIndex(Calendars.ACCOUNT_NAME))}; */
        		//Submit the query and get a Cursor object back. 
        		//Cursor cur = cr.query(Calendars.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null);
        	

        	Calendar cal = Calendar.getInstance(); //new GregorianCalendar(TimeZone.getDefault());
        	cal.set(Calendar.HOUR_OF_DAY, 0);
        	cal.set(Calendar.MINUTE, 0);
        	cal.set(Calendar.SECOND, 0);
        	cal.set(Calendar.MILLISECOND, 0);
        	long begin = cal.getTime().getTime();
        	cal.set(Calendar.HOUR_OF_DAY, 24);
        	cal.set(Calendar.MINUTE, 59);
        	cal.set(Calendar.SECOND, 59);
        	cal.set(Calendar.MILLISECOND, 59);
        	long end = cal.getTime().getTime();

        	String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND ";
        	selection += "(" + CalendarContract.Events.DTSTART + " > ?) AND ";
        	selection += "(" + CalendarContract.Events.DTEND + " < ?))";
        	
        	Log.d(TAG, "NAME	: " + cursor.getString(cursor.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME)));

        	String[] selectionArgs = new String [] {
        		cursor.getString(ID_INDEX_CALENDAR_PROJECTION),
        		Long.toString(begin),
        		Long.toString(end)
        	};

        	Format dateFormat = DateFormat.getDateFormat(this);
        	Format timeFormat = DateFormat.getTimeFormat(this);
        	
        	
    		Agenda a = new Agenda(this.getContentResolver());
    		Cursor ev = a.events();
    		
    		while(ev.moveToNext()) {
    			String hello = ev.getString(Agenda.INDEX_TITLE);
    			Log.i(TAG, hello);
    			hello = ev.getString(Agenda.INDEX_EVENT_LOCATION);
    			hello = dateFormat.format(ev.getLong(Agenda.INDEX_BEGIN));
    			hello = dateFormat.format(ev.getLong(Agenda.INDEX_END));
    		}
    		
        	
        	
        	Cursor cur = cr.query(Events.CONTENT_URI, EVENT_PROJECTION, selection, selectionArgs, null);
        	while (cur.moveToNext()) {
        		String blah = cur.getString(TITLE_INDEX_EVENT_PROJECTION);
        		blah = cur.getString(DESCRIPTION_INDEX_EVENT_PROJECTION);
        		blah = dateFormat.format(cur.getLong(DSTART_EVENT_INDEX_EVENT_PROJECTION)) + timeFormat.format(cur.getLong(DSTART_EVENT_INDEX_EVENT_PROJECTION));
        		blah = cur.getString(DTEND_EVENT_INDEX_EVENT_PROJECTION);
        		blah = cur.getString(EVENT_LOCATION_INDEX_EVENT_PROJECTION);

        		/*Log.i(TAG, );
        		Log.i(TAG, );
        		Log.i(TAG, );
        		Log.i(TAG, );
//        		Log.i(TAG, );*/
        		

        	}
        		
        	
        	
        //}
        
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
