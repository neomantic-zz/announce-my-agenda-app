package com.neomantic.calendar_out_loud;

import java.util.ArrayList;

import android.content.res.Resources;

public class Script {
	
	ArrayList <AgendaLine> mAgendaItems = new ArrayList<AgendaLine>();

	public void add(AgendaLine line) {
		mAgendaItems.add(line);
	}
	
	public String write(Resources res) {
		final int numberOfItems = mAgendaItems.size();
		if (numberOfItems == 0) {
			return res.getString(R.string.agenda_no_meeting);
		}
		String script = "";
		
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

}
