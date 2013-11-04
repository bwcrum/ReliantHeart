package com.numerex.tc65i.utilities.time;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeHelper {
	public static long systemTimeToMillis(String datetime) throws Exception {
		//datetime
		//11/08/29,19:59:24
		
		int index = -1;
		
		index = datetime.indexOf(",");
		String calendar = datetime.substring(0, index);
		String time = datetime.substring(index + 1);
		//System.out.println("calendar=<" + calendar + ">, time=<" + time + ">");
		
		index = calendar.indexOf("/");
		String year = "20" + calendar.substring(0, index);
		calendar = calendar.substring(index + 1);
		index = calendar.indexOf("/");
		String month = calendar.substring(0, index);
		calendar = calendar.substring(index + 1);
		String day = calendar;
		//System.out.println("year=<" + year + ">, month=<" + month + ">, day=<" + day + ">");

		index = time.indexOf(":");
		String hours = time.substring(0, index);
		time = time.substring(index + 1);
		index = time.indexOf(":");
		String minutes = time.substring(0, index);
		time = time.substring(index + 1);
		String seconds = time;
		//System.out.println("hours=<" + hours + ">, minutes=<" + minutes + ">, seconds=<" + seconds + ">");

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours));
		cal.set(Calendar.MINUTE, Integer.parseInt(minutes));
		cal.set(Calendar.SECOND, Integer.parseInt(seconds));
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
		cal.set(Calendar.MONTH, Integer.parseInt(month) - 1);
		cal.set(Calendar.YEAR, Integer.parseInt(year));

		return cal.getTime().getTime();
	}
	
	public static String getDBDateTimeFromMillis(long millis) throws Exception {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTime(new Date(millis));
		return getDBDateTime(cal);
	}
	
	public static String getDBDateTime(Calendar cal) {
		String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
		if (month.length() < 2) month = "0" + month;
		String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH)); 
		if (day.length() < 2) day = "0" + day;
		String year = Integer.toString(cal.get(Calendar.YEAR)); 

		String hours = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		if (hours.length() < 2) hours = "0" + hours;
		String minutes = Integer.toString(cal.get(Calendar.MINUTE));
		if (minutes.length() < 2) minutes = "0" + minutes;
		String seconds = Integer.toString(cal.get(Calendar.SECOND));
		if (seconds.length() < 2) seconds = "0" + seconds;

		//'05/29/2008 18:30:38'
		String date = "" 
			+ (year) + "-" 
			+ (month) + "-" 
			+ (day) + " "
			
			+ (hours) + ":" 
			+ (minutes) + ":" 
			+ (seconds)
			+ "";
		return date;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("millis=<" + systemTimeToMillis("11/08/29,19:59:24") + ">");
	}
}
