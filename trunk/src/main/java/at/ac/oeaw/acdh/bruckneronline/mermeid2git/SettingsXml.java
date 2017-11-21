package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/*
 * TODO : refactor into reusables
 */
/**
 * 
 * @author marek
 *
 */
public class SettingsXml {
	
	public static final String ENCODING = "UTF-8";
	public static final String DEFAULT_FILE_NAME = "settings.xml";

	private final Properties props;
	
	private File lastLoadedFrom;
	
	public SettingsXml() {
		props = new Properties();
		lastLoadedFrom = null;
	}
	
	public synchronized void load(File f) throws IOException {
		if (!f.canRead()) {
			throw new IOException("Unable to read from '" + f.getAbsolutePath() + "' file");
		}
		load(new FileInputStream(f));
		lastLoadedFrom = f;
	}
	
	public synchronized void load(InputStream is) throws InvalidPropertiesFormatException, IOException {
		props.loadFromXML(is);
		is.close();
	}
	
	/**
	 * Calls {@link #save(String)} with an empty comment.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		save(null);
	}
	
	/**
	 * Calls {@link #save(File, String) with {@link File} from which settings were loaded last time
	 * 
	 * @throws IOException
	 */
	public void save(String comment) throws IOException {
		File f = lastLoadedFrom;
		if (f == null) {
			f = new File(DEFAULT_FILE_NAME);
		}
		save(f, comment);
	}
	
	/**
	 * Calls {@link #save(OutputStream, String)} with {@link FileOutputStream} as parameter
	 * 
	 * @param f
	 * @throws IOException
	 */
	public void save(File f, String comment) throws IOException {
		save(new FileOutputStream(f), comment);
	}
	
	/**
	 * 
	 * @param os
	 * @param comment may be {@code null}, or {@link #getDefaultComment()}
	 * @throws IOException
	 */
	public void save(OutputStream os, String comment) throws IOException {
		props.storeToXML(os, comment, ENCODING);
		os.close();
	}
	
	public String set(String key, String value) {
		return (String) props.setProperty(key, value);
	}
	
	public String setInt(String key, Integer value) {
		return (String) props.setProperty(key, String.valueOf(value.intValue()));
	}
	
	public String setBool(String key, Boolean value) {
		return (String) props.setProperty(key, String.valueOf(value.booleanValue()));
	}
	
	public String setDouble(String key, Double value) {
		return (String) props.setProperty(key, String.valueOf(value.doubleValue()));
	}
	
	public String setFile(String key, File f) {
		return (String) props.setProperty(key, f.getPath());
	}
	
	public String setBytes(String key, byte[] bytes) {
		Encoder e = Base64.getEncoder();
		String str = e.encodeToString(bytes);
		return (String) props.setProperty(key, str);
	}
	
	public String get(String key) {
		return props.getProperty(key);
	}
	
	public Integer getInt(String key) {
		String str = get(key);
		return str == null ? null : Integer.parseInt(str);
	}
	
	public Boolean getBool(String key) {
		String str = get(key);
		return str == null ? null : Boolean.parseBoolean(str);
	}
	
	public Double getDouble(String key) {
		String str = get(key);
		return str == null ? null : Double.parseDouble(str);
	}
	
	public File getFile(String key) {
		String str = get(key);
		return str == null ? null : new File(str);
	}
	
	public byte[] getBytes(String key) {
		String str = get(key);
		if (str == null) {
			return null;
		}
		Decoder d = Base64.getDecoder();
		return d.decode(str);
	}
	
	/**
	 * @return comment "saved at" with current time-stamp appended
	 */
	public static String getDefaultComment() {
		return "saved at " + DateTimeHelper.toCompleteDate(new Date(System.currentTimeMillis()));
	}
}
