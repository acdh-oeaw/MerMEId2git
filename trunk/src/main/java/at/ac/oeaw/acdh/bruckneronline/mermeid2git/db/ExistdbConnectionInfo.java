package at.ac.oeaw.acdh.bruckneronline.mermeid2git.db;

/*
 * TODO : refactor into reusables
 */
/**
 * 
 * @author mcupak
 *
 */
public class ExistdbConnectionInfo {
	
	public static final String ROOT_COLLECTION_NAME = "db";
	public static final String DRIVER_NAME = "org.exist.xmldb.DatabaseImpl";
	
	public final String uri, user, password;
	public final boolean ssl;
	
	public ExistdbConnectionInfo(String uri, String user, String password, boolean ssl) {
		this.uri = uri;
		this.user = user;
		this.password = password;
		this.ssl = ssl;
	}
}
