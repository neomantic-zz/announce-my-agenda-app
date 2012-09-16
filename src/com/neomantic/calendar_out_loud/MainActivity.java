package com.neomantic.calendar_out_loud;

import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.support.v4.app.NavUtils;

public class MainActivity extends ListActivity {

	private static final String[] PROJECTION= new String[] {
		Calendars._ID, 
		Calendars.ACCOUNT_NAME, 
		Calendars.ACCOUNT_TYPE,
		Calendars.CALENDAR_DISPLAY_NAME,
		Calendars.OWNER_ACCOUNT
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ListView calendarsListView = (ListView) findViewById(R.id.calendars);
        Cursor cursor = getContentResolver().query(Calendars.CONTENT_URI, PROJECTION, null, null, Calendars.ACCOUNT_NAME);
        SimpleCursorAdapter ca = new SimpleCursorAdapter(this, R.layout.calendar_list_item, cursor, 
        		new String[]{Calendars.CALENDAR_DISPLAY_NAME}, 
        		new int[]{R.id.calendarName},
        		CursorAdapter.FLAG_AUTO_REQUERY);
        //calendarsListView.setAdapter(ca);
        this.setListAdapter(ca);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
