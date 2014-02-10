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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.res.Resources;

public class Script {

	ArrayList <AgendaLine> mAgendaItems = new ArrayList<AgendaLine>();

	public void add(AgendaLine line) {
		mAgendaItems.add(line);
	}

	public String write(Resources res) {
		String script = res.getString(getGreetingResourceId()) + " ";
		final int numberOfItems = mAgendaItems.size();
		if (numberOfItems == 0) {
			return script + res.getString(R.string.agenda_no_meeting);
		}

		for (int i = 0; i < numberOfItems; i++) {
			AgendaLine line = mAgendaItems.get(i);
			if (i == 0 && numberOfItems == 1) {
				line.setOnlyOneMeeting();
			} else if (i == 0) {
				line.setFirst();
			} else if (i == (numberOfItems - 1)) {
				line.setLast();
			}
			script += line.format(res);
		}
		return script;
	}

	private int getGreetingResourceId() {
		final GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
		if (cal.get(Calendar.AM_PM) == Calendar.AM) {
			return R.string.greeting_good_morning;
		}
		int hour = cal.get(Calendar.HOUR);
		if (hour > 6) {
			return R.string.greeting_evening;
		}

		return R.string.greeting_afternoon;
	}
}
