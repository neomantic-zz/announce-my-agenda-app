package com.neomantic.calendar_out_loud;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
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
import android.util.Log;


public class Agenda {
	
	private static final String AGENDA_SORT_ORDER =
		CalendarContract.Instances.START_DAY + " ASC, " +
		CalendarContract.Instances.BEGIN + " ASC, " +
		CalendarContract.Events.TITLE + " ASC";

	
	private ContentResolver mContentResolver;
	
	public Agenda(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}
	
	private static final String[] PROJECTION = new String[] {
		Instances._ID, // 0
		Instances.TITLE, // 1
		Instances.EVENT_LOCATION, // 2
		Instances.ALL_DAY, // 3
		Instances.BEGIN, // 4
		Instances.END, // 5
		Instances.EVENT_ID, // 6
		Instances.START_DAY, // 7 Julian start day
		Instances.START_MINUTE, // 8 Julian start day
		Instances.END_DAY, // 9 Julian end day
		Instances.END_MINUTE, // 10 Julian end day
		Instances.SELF_ATTENDEE_STATUS, // 11
		Instances.EVENT_TIMEZONE, // 12
		Events.CALENDAR_ID, // 13
		Calendars.VISIBLE, // 14
	};

	public static final int INDEX_INSTANCE_ID = 0;
	public static final int INDEX_TITLE = 1;
	public static final int INDEX_EVENT_LOCATION = 2;
	public static final int INDEX_ALL_DAY = 3;
	public static final int INDEX_BEGIN = 4;
	public static final int INDEX_END = 5;
	public static final int INDEX_EVENT_ID = 6;
	public static final int INDEX_START_DAY = 7;
	public static final int INDEX_START_MINUTE = 8;
	public static final int INDEX_END_DAY = 9;
	public static final int INDEX_END_MINUTE = 10;
	public static final int INDEX_SELF_ATTENDEE_STATUS = 11;
	public static final int INDEX_TIME_ZONE = 12;
	
	private Uri buildQueryUri(long start, long end) {
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, start);
		ContentUris.appendId(builder, end);
		return builder.build();
	}

	public Cursor events() {
		String[] calendarIds = new String[]{"5","7"}; 
		return mContentResolver.query(
				buildQueryUri(buildStartTime(), buildEndTime()), 
				PROJECTION, 
				buildSelection(calendarIds.length), 
				calendarIds, 
				AGENDA_SORT_ORDER);
	};

	private long buildStartTime() {
		Calendar cal = new GregorianCalendar(TimeZone.getDefault());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime().getTime();
	}
	
	private long buildEndTime() {
		Calendar cal = new GregorianCalendar(TimeZone.getDefault());
		cal.set(Calendar.HOUR_OF_DAY, 24);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);
		return cal.getTime().getTime();
		
	}
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

