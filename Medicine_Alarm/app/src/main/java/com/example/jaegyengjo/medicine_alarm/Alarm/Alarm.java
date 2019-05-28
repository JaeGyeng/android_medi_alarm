/* Copyright 2014 Sheldon Neilson www.neilson.co.za
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.example.jaegyengjo.medicine_alarm.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import com.example.jaegyengjo.medicine_alarm.Alarm.alert.AlarmAlertBroadcastReciever;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


public class Alarm implements Serializable {
	
	public enum Day{
		SUNDAY,
		MONDAY,
		TUESDAY,
		WEDNESDAY,
		THURSDAY,
		FRIDAY,
		SATURDAY;

		@Override
		public String toString() {
			switch(this.ordinal()){
				case 0:
					return "일요일";
				case 1:
					return "월요일";
				case 2:
					return "화요일";
				case 3:
					return "수요일";
				case 4:
					return "목요일";
				case 5:
					return "금요일";
				case 6:
					return "토요일";
			}
			return super.toString();
		}
		
	}
	private static final long serialVersionUID = 8699489847426803789L;
	private int id;
	private Boolean alarmActive = true;
	private Calendar alarmTime = Calendar.getInstance(); //시간 카렌더
	private Day[] days = {Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY};
	private String alarmTonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
	private Boolean vibrate = true;
	private String alarmName = "약 이름을 입력하세요.";
	
	public Alarm() {

	}

	public Boolean getAlarmActive() {
		return alarmActive;
	}
	public void setAlarmActive(Boolean alarmActive) {
		this.alarmActive = alarmActive;
	}

	public Calendar getAlarmTime() {
		if (alarmTime.before(Calendar.getInstance()))
			alarmTime.add(Calendar.DAY_OF_MONTH, 1);
		while(!Arrays.asList(getDays()).contains(Day.values()[alarmTime.get(Calendar.DAY_OF_WEEK)-1])){
			alarmTime.add(Calendar.DAY_OF_MONTH, 1);
		}
		return alarmTime;
	}


	public String getAlarmTimeString() {

		String time = "";
		if (alarmTime.get(Calendar.HOUR_OF_DAY) <= 9)
			time += "0";
		time += String.valueOf(alarmTime.get(Calendar.HOUR_OF_DAY));
		time += ":";

		if (alarmTime.get(Calendar.MINUTE) <= 9)
			time += "0";
		time += String.valueOf(alarmTime.get(Calendar.MINUTE));

		return time;
	}


	public void setAlarmTime(Calendar alarmTime) {
		this.alarmTime = alarmTime;
	}


	public void setAlarmTime(String alarmTime) {

		String[] timePieces = alarmTime.split(":");

		Calendar newAlarmTime = Calendar.getInstance();
		newAlarmTime.set(Calendar.HOUR_OF_DAY,
				Integer.parseInt(timePieces[0]));
		newAlarmTime.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
		newAlarmTime.set(Calendar.SECOND, 0);
		setAlarmTime(newAlarmTime);
	}


	public Day[] getDays() {
		return days;
	}


	public void setDays(Day[] days) {
		this.days = days;
	}

	public void addDay(Day day){
		boolean contains = false;
		for(Day d : getDays())
			if(d.equals(day))
				contains = true;
		if(!contains){
			List<Day> result = new LinkedList<Day>();
			for(Day d : getDays())
				result.add(d);
			result.add(day);
			setDays(result.toArray(new Day[result.size()]));
		}
	}
	
	public void removeDay(Day day) {
	    
		List<Day> result = new LinkedList<Day>();
	    for(Day d : getDays())
	        if(!d.equals(day))
	            result.add(d);
	    setDays(result.toArray(new Day[result.size()]));
	}
	

	public String getAlarmTonePath() {
		return alarmTonePath;
	}


	public void setAlarmTonePath(String alarmTonePath) {
		this.alarmTonePath = alarmTonePath;
	}
	

	public Boolean getVibrate() {
		return vibrate;
	}


	public void setVibrate(Boolean vibrate) {
		this.vibrate = vibrate;
	}

	public String getAlarmName() {
		return alarmName;
	}

	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRepeatDaysString() {
		StringBuilder daysStringBuilder = new StringBuilder();
		if(getDays().length == Day.values().length){
			daysStringBuilder.append("모든 요일");
		}else{
			Arrays.sort(getDays(), new Comparator<Day>() {
				@Override
				public int compare(Day lhs, Day rhs) {
					
					return lhs.ordinal() - rhs.ordinal();
				}
			});
			for(Day d : getDays()){
				switch(d){

					default:
						daysStringBuilder.append(d.toString().substring(0, 1));
						break;
				}				
				daysStringBuilder.append(',');
			}
			daysStringBuilder.setLength(daysStringBuilder.length()-1);
		}
			
		return daysStringBuilder.toString();
	}

	public void schedule(Context context) {
		setAlarmActive(true);
		
		Intent myIntent = new Intent(context, AlarmAlertBroadcastReciever.class);
		myIntent.putExtra("alarm", this);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, getAlarmTime().getTimeInMillis(), pendingIntent);
	}

	public String getTimeUntilNextAlarmMessage(){
		long timeDifference = getAlarmTime().getTimeInMillis() - System.currentTimeMillis();
		long days = timeDifference / (1000 * 60 * 60 * 24);
		long hours = timeDifference / (1000 * 60 * 60) - (days * 24);
		long minutes = timeDifference / (1000 * 60) - (days * 24 * 60) - (hours * 60);
		long seconds = timeDifference / (1000) - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
		String alert = "알림 시작 까지: ";
		if (days > 0) {
			alert += String.format(
					"%d days, %d hours, %d minutes and %d seconds", days,
					hours, minutes, seconds);
		} else {
			if (hours > 0) {
				alert += String.format("%d hours, %d minutes and %d seconds",
						hours, minutes, seconds);
			} else {
				if (minutes > 0) {
					alert += String.format("%d minutes, %d seconds", minutes,
							seconds);
				} else {
					alert += String.format("%d seconds", seconds);
				}
			}
		}
		return alert;
	}
}
