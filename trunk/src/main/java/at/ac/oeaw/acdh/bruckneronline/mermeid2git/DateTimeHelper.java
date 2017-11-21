package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/*
 * TODO : refactor into reusables
 */
/**
 * 
 * @author mcupak
 *
 */
public class DateTimeHelper {

	private static final SimpleDateFormat SDF__ISO_DATE, SDF__COMPLETE_DATE;
	static {
		SDF__ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");
		SDF__COMPLETE_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
		
		TimeZone tz = TimeZone.getDefault();
		SDF__ISO_DATE.setTimeZone(tz);
		SDF__COMPLETE_DATE.setTimeZone(tz);
	}
	
	public static synchronized Date parseIsoDate(String str) {
		return SDF__ISO_DATE.parse(str, new ParsePosition(0));
	}
	
	public static synchronized String toIsoDate(Date d) {
		return SDF__ISO_DATE.format(d);
	}
	
	public static synchronized String toCompleteDate(Date d) {
		return SDF__COMPLETE_DATE.format(d);
	}

}
