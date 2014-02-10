/** "Announce My Agenda" Android App
    Copyright (C) 2014 Chad Albers

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package com.neomantic.calendar_out_loud;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.TimeZone;

import android.content.res.Resources;
import android.database.Cursor;

public class AgendaLine {

	private String mEventLocation = "";
	private String mEventTitle = "";
	private String mBeginTime = "";
	private String mEndTime = "";

	public static final int FIRST_MEETING = R.string.first_agenda_line_item;
	public static final int FIRST_MEETING_NO_LOCATION = R.string.first_agenda_line_item_no_location;

	public static final int MEETING = R.string.agenda_line_item;
	public static final int MEETING_NO_LOCATION = R.string.agenda_line_item_no_location;

	public static final int ONLY_ONE_MEETING = R.string.agenda_line_one_meeting;
	public static final int ONLY_ONE_MEETING_NO_LOCATION = R.string.agenda_line_one_meeting_no_location;

	public static final int NEXT_MEETING = R.string.agenda_line_item_next_meeting;
	public static final int NEXT_MEETING_NO_LOCATION = R.string.agenda_line_item_next_meeting_no_location;

	public static final int LAST_MEETING = R.string.agenda_line_item_final_meeting;
	public static final int LAST_MEETING_NO_LOCATION = R.string.agenda_line_item_final_meeting_no_location;

	private int[] DEFAULTS = new int[] {NEXT_MEETING, MEETING};
	private int mFormatResourceId;

	public AgendaLine(Cursor cursor) {

		mEventTitle = cursor.getString(Agenda.INDEX_TITLE);
		mEventLocation = cursor.getString(Agenda.INDEX_EVENT_LOCATION);

		GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
		cal.setTimeInMillis(cursor.getLong(Agenda.INDEX_BEGIN));


		mBeginTime = Integer.toString(cal.get(Calendar.HOUR)) + " ";
		mBeginTime += parseMinute(cal);
		mBeginTime += parseAMPM(cal);

		cal = new GregorianCalendar(TimeZone.getDefault());
		cal.setTimeInMillis(cursor.getLong(Agenda.INDEX_END));

		mEndTime = Integer.toString(cal.get(Calendar.HOUR)) + " ";
		mEndTime += parseMinute(cal);
		mEndTime += parseAMPM(cal);

		Random r = new Random();
		mFormatResourceId = DEFAULTS[r.nextInt(1)];
	}

	private String parseMinute(GregorianCalendar cal) {
		String min = "";
		int minute = cal.get(Calendar.MINUTE);
		if (minute > 0) {
			min = Integer.toString(minute);
			min += " ";
		}
		return min;
	}

	private String parseAMPM(GregorianCalendar cal) {
		if (cal.get(Calendar.AM_PM) == Calendar.AM ) {
			return "A M";
		} else {
			return "P M";
		}
	}

	public String getEventLocation() {
		return mEventLocation;
	}

	public String getEventTitle() {
		return mEventTitle;
	}

	public String getBeginTime() {
		return mBeginTime;
	}

	public String getEndTime() {
		return mEndTime;
	}

	public void setFirst() {
		if (hasLocation()) {
			mFormatResourceId = FIRST_MEETING;
		} else {
			mFormatResourceId = FIRST_MEETING_NO_LOCATION;
		}
	}

	public void setLast() {
		if (hasLocation()) {
			mFormatResourceId = LAST_MEETING;
		} else {
			mFormatResourceId = LAST_MEETING_NO_LOCATION;
		}
	}

	public void setOnlyOneMeeting() {
		if (hasLocation()) {
			mFormatResourceId= ONLY_ONE_MEETING;
		} else {
			mFormatResourceId= ONLY_ONE_MEETING_NO_LOCATION;
		}
	}

	private boolean hasLocation() {
		/* NOTE - will be adding more logic here to find phone numbers etc */
		//return !mEventLocation.isEmpty();
		return false;
	}

	public String format(Resources res) {
		String[] eventArgs = null;
		/* TODO - I hate handling it this way */
		switch(mFormatResourceId) {
		case FIRST_MEETING:
			eventArgs = new String[]{mBeginTime, mEndTime, mEventTitle, mEventLocation};
			break;
		case FIRST_MEETING_NO_LOCATION:
			eventArgs = new String[]{mBeginTime, mEndTime, mEventTitle};
			break;
		case MEETING:
			eventArgs = new String[]{mBeginTime, mEndTime, mEventTitle, mEventLocation};
			break;
		case MEETING_NO_LOCATION:
			eventArgs = new String[]{mBeginTime, mEndTime, mEventTitle};
			break;
		case ONLY_ONE_MEETING:
			eventArgs = new String[]{mBeginTime, mEndTime, mEventTitle, mEventLocation};
			break;
		case ONLY_ONE_MEETING_NO_LOCATION:
			eventArgs = new String[]{mBeginTime, mEndTime, mEventTitle};
			break;
		case NEXT_MEETING:
			eventArgs = new String[]{mEventTitle, mBeginTime, mEndTime, mEventLocation};
			break;
		case NEXT_MEETING_NO_LOCATION:
			eventArgs = new String[]{mEventTitle, mBeginTime, mEndTime};
			break;
		case LAST_MEETING:
			eventArgs = new String[]{mBeginTime, mEndTime, mEventLocation, mEventTitle};
			break;
		case LAST_MEETING_NO_LOCATION:
			eventArgs = new String[]{mBeginTime, mEndTime, mEventTitle};
			break;
		}
		if (eventArgs == null) {
			return "";
		}
		return String.format(res.getString(mFormatResourceId), (Object[])eventArgs);
	}

}
