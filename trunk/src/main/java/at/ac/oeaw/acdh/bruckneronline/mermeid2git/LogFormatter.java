package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/*
 * TODO : refactor into reusables
 */
/**
 * 
 * @author mcupak
 *
 */
public class LogFormatter extends Formatter {

	private final DateFormat df;
	
	public LogFormatter(DateFormat df) {
		this.df = df;
	}
	
	public LogFormatter() {
		this(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS X"));
	}
	
	@Override
	public String format(LogRecord lr) {
		final StringBuilder sb = new StringBuilder();
		sb.append(formatMillis(lr.getMillis()));
		sb.append(' ');
		sb.append(lr.getLevel());
		/*sb.append(' ');
		sb.append(lr.getThreadID());*/
		sb.append(' ');
		sb.append(lr.getMessage());
		Throwable t = lr.getThrown();
		if (t != null) {
			sb.append('\n');
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			sb.append(sw.toString());
		}
		sb.append('\n');
		return sb.toString();
	}
	
	@Override
	public String formatMessage(LogRecord lr) {
		return format(lr);
	}
	
	private synchronized String formatMillis(long millis) {
		final Date d = new Date(millis);
		return df.format(d);
	}
}