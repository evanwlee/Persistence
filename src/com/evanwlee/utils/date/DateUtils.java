
package com.evanwlee.utils.date;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class DateUtils {
  public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

  public static String now() {
	  return now(DATE_FORMAT_NOW);
  }

  public static String now(String format) {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(format);
	    return sdf.format(cal.getTime());

  }

  public static Date dateNow() {
	    return Calendar.getInstance().getTime();

}
  public static String dateOneMonthFromNowString() {
	  Calendar cal = Calendar.getInstance(); 
	  cal.add(Calendar.MONTH, 1);
	   return format(cal.getTime(), "yyyy-MM-dd");
}
  public static String dateXDaysAgo(int days) {
	  Calendar cal = Calendar.getInstance(); 
	  cal.add(Calendar.DATE, -days);
	   return format(cal.getTime(), "yyyy-MM-dd");
}
  
  public static java.util.Date dateFromString(String strdate) {
	  return dateFromString(strdate,DATE_FORMAT_NOW);

  }
  public static java.util.Date dateFromString(String strdate, String format) {
	  SimpleDateFormat dateFormat=new SimpleDateFormat(format);
	  java.text.ParsePosition p=new  java.text.ParsePosition(0);
	  java.util.Date date=dateFormat.parse(strdate,p);

	  return date;
  }
  
  // Returns calendar set with Today's date and 00:00:00.000 as the time
  static public GregorianCalendar getCalendarZeroTime()
  {
    GregorianCalendar newCalendar = new GregorianCalendar();
    newCalendar.setTime( new Date() );
    newCalendar.set( Calendar.HOUR_OF_DAY, 0 );
    newCalendar.set( Calendar.MINUTE, 0 );
    newCalendar.set( Calendar.SECOND, 0 );
    newCalendar.set( Calendar.MILLISECOND, 0 );
    return newCalendar;
  } // getDateZeroTime

	/**
	 *
	 * @param dateFormat  -- format for the date value to be returned
	 * @param value -- timestamp to format
	 * @param token
	 * @return
	 */
	public static String formatTimestampAsString(String dateFormat, Timestamp value) {
		String displayValue = "";

			SimpleDateFormat localizedFormatter = new SimpleDateFormat();
			localizedFormatter.applyPattern(dateFormat);
			localizedFormatter.setLenient(false);
			localizedFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			Timestamp timestamp = (Timestamp)value;
			displayValue = localizedFormatter.format(new Date(timestamp.getTime()));

		return displayValue;
	}

  /**
   * Formats the date using the pattern.  The time zone defaults to
   * "America/Los_Angeles".  The language defaults to "en".
   * The country ID defaults to "US".
   * <p>
   * This method is a pass-through to DateUtils.format(Date gmtDate,
   * String pattern, String timeZoneId, String languageID, String countryID)
   * so that a default can be provided for time zone, language, and country.
   * <p>
   * @param date date to format
   * @param pattern a date pattern such as MM/dd/yyyy (e.g. 12/12/2005).
   * @return a String that represents the date in the given format.
   */
  public static String format(Date date, String pattern) {
    if (date != null) {
      return DateUtils.formatDatePattern(date, pattern, "GMT", "en", "US");
    }

    return "";
  }

  /**
   * This method is almost the same as the method above except with one more
   * para: timezone.
   * @param date
   * @param pattern
   * @param timeZone
   * @return  a String that represents the date in the given format.
   */
  public static String format(Date date, String pattern, String timeZone) {
    if (date != null) {
      return DateUtils.formatDatePattern(date, pattern, timeZone, "en", "US");
    }

    return "";
  }

  /**
   * Formats a <code>Date</code> given a <code>Date</code> object and a
   * pattern and timezone.  Whether or not the formatted string contains the
   * date and/or time is completely up to the <code>pattern</code> passed in as
   * a parameter
   *
   * @param gmtDate         a <code>Date</code> instance that is a that is to be
   * formatted to a <code>String</code>
   *
   * @param pattern         a string description of the pattern that the
   * formatter will use to create the formatted String.  If it contains a date
   * pattern, then the String will have the date.  If it contains a time
   * pattern, then the String will have the time.  If it contains both then the
   * String will have both date and time.
   *
   * @param timeZoneId      a recognized Java compliant timezone id
   *
   * @param languageId      a language id recongnized by the Java <code>Locale</code>
   * class
   *
   * @param countryId       a country id recognized by the Java <code>Locale</code>
   * class
   *
   * @return a formatted string of the <code>Date</code> value specified by the
   * pattern
   */
  public static String formatDatePattern(
    Date gmtDate, String pattern, String timeZoneId, String languageID,
    String countryID )
  {
    Locale locale = new Locale( languageID, countryID );

    DateFormat localizedDateFormat = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT, locale );
    localizedDateFormat.setLenient( false );

    ( ( SimpleDateFormat ) localizedDateFormat ).applyLocalizedPattern( pattern );

    localizedDateFormat.setTimeZone( TimeZone.getTimeZone( timeZoneId ) );

    String formattedDate = localizedDateFormat.format( gmtDate );

    return formattedDate;
  }
  
  /**
   * Calculate the different days between two dates
   * @param startDate
   * @param endDate
   * @return
   */
  	public static long daysBetween(Date start, Date end) {  
  		
  		GregorianCalendar startDate  = getGregorianCalendar(start);
  		GregorianCalendar endDate  = getGregorianCalendar(end);
  		
  		GregorianCalendar startDate_clone = (GregorianCalendar) startDate.clone();   		
  		GregorianCalendar endDate_clone = (GregorianCalendar) endDate.clone(); 
  		
  		long daysBetween = 0; 
  		if(startDate_clone.before(endDate)){

	       while (startDate_clone.before(endDate)) {  
	        startDate_clone.add(Calendar.DAY_OF_MONTH, 1);  
	        daysBetween++;  
	      }  
	      return daysBetween;  
	    } else{
	       while (endDate_clone.before(startDate)) {  
	    	   	endDate_clone.add(Calendar.DAY_OF_MONTH, 1);  
		        daysBetween++;  
		   }  
		   return daysBetween; 
	    } 
  	}
  	
    /**
     * DOCUMENT ME!
     *
     * @param date DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static GregorianCalendar getGregorianCalendar(Date date) {
      if (date == null) {
        return null;
      }

      GregorianCalendar ret = new GregorianCalendar();
      ret.setTime(date); 
      ret.set(Calendar.HOUR, 0);
      ret.set(Calendar.MINUTE, 0);
      ret.set(Calendar.SECOND, 0);
      ret.set(Calendar.MILLISECOND, 0);
      return ret;
    }

  public static void  main(String arg[]) {
    System.out.println("Now : " + DateUtils.now());
  }
}


