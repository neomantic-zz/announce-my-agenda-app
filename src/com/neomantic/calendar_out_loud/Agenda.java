package com.neomantic.calendar_out_loud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.format.Time;
import android.util.Log;


public class Agenda {
	
	private static final String AGENDA_SORT_ORDER =
		CalendarContract.Instances.START_DAY + " ASC, " +
		CalendarContract.Instances.BEGIN + " ASC, " +
		CalendarContract.Events.TITLE + " ASC";

	
	private ContentResolver mContentResolver;

	private boolean mHideDeclined = true;
	
	public Agenda(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}
	
	private static final String[] PROJECTION = new String[] {
		Instances._ID, // 0
		Instances.TITLE, // 1
		Instances.EVENT_LOCATION, // 2
		Instances.ALL_DAY, // 3
		Instances.HAS_ALARM, // 4
		Instances.CALENDAR_COLOR, // 5
		Instances.RRULE, // 6
		Instances.BEGIN, // 7
		Instances.END, // 8
		Instances.EVENT_ID, // 9
		Instances.START_DAY, // 10 Julian start day
		Instances.END_DAY, // 11 Julian end day
		Instances.SELF_ATTENDEE_STATUS, // 12
		Instances.ORGANIZER, // 13
		Instances.OWNER_ACCOUNT, // 14
		Instances.CAN_ORGANIZER_RESPOND, // 15
		Instances.EVENT_TIMEZONE, // 16
		Events.CALENDAR_ID,
		Calendars.VISIBLE,
	};

	public static final int INDEX_INSTANCE_ID = 0;
	public static final int INDEX_TITLE = 1;
	public static final int INDEX_EVENT_LOCATION = 2;
	public static final int INDEX_ALL_DAY = 3;
	public static final int INDEX_HAS_ALARM = 4;
	public static final int INDEX_COLOR = 5;
	public static final int INDEX_RRULE = 6;
	public static final int INDEX_BEGIN = 7;
	public static final int INDEX_END = 8;
	public static final int INDEX_EVENT_ID = 9;
	public static final int INDEX_START_DAY = 10;
	public static final int INDEX_END_DAY = 11;
	public static final int INDEX_SELF_ATTENDEE_STATUS = 12;
	public static final int INDEX_ORGANIZER = 13;
	public static final int INDEX_OWNER_ACCOUNT = 14;
	public static final int INDEX_CAN_ORGANIZER_RESPOND= 15;
	public static final int INDEX_TIME_ZONE = 16;
	public static final int CALENDAR_IN_INDEX = 17;
	public static final int CALENDAR_VISIBLE_INDEX = 18;
	
	
	private Uri buildQueryUri(long start, long end) {
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, start);
		ContentUris.appendId(builder, end);
		Uri blah = builder.build();
		return blah; 
	}

	public Cursor events() {
		Calendar cal = new GregorianCalendar(TimeZone.getDefault());
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	long begin = cal.getTime().getTime();
    	
    	cal = new GregorianCalendar(TimeZone.getDefault());
    	cal.set(Calendar.HOUR_OF_DAY, 24);
    	cal.set(Calendar.MINUTE, 59);
    	cal.set(Calendar.SECOND, 59);
    	cal.set(Calendar.MILLISECOND, 59);
    	long end = cal.getTime().getTime();

    	String[] calendarIds = new String[]{"5","7"}; 
    	    	
    	String blah = buildSelection(calendarIds.length);
    	Log.i(MainActivity.TAG, blah);
    	
		return mContentResolver.query(
				buildQueryUri(begin,end), 
				PROJECTION, 
				buildSelection(calendarIds.length), 
				calendarIds, 
				AGENDA_SORT_ORDER);
	};


	
	private String buildSelection(int numberOfCalendars) {
		/* showing only visible calendars, and events that arent decline */
    	String selection = Calendars.VISIBLE + " = 1 AND ";
    	selection += Instances.SELF_ATTENDEE_STATUS + " != " + Attendees.ATTENDEE_STATUS_DECLINED + " AND "; 
    	selection += Events.CALENDAR_ID + " IN(" + buildPlaceHolders(numberOfCalendars)+ ")";
    	return selection;
	}
	
	private String buildPlaceHolders(int numberOfPlaceHolders) {
		String placeHolders = "";
		String placeHolder = "?,";
		for(int i=0; i < numberOfPlaceHolders; i++) {
			if (i == (numberOfPlaceHolders - 1) ) {
				placeHolder = "?"; 
			}
			placeHolders += placeHolder;
		}
		return placeHolders;
	}

}