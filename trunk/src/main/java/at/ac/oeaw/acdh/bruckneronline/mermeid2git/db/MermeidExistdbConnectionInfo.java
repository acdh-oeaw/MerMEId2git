package at.ac.oeaw.acdh.bruckneronline.mermeid2git.db;

/**
 * 
 * @author mcupak
 *
 */
public class MermeidExistdbConnectionInfo extends ExistdbConnectionInfo {

	public final String collectionName;
	
	public MermeidExistdbConnectionInfo(String uri, String user, String password, String collectionName, boolean ssl) {
		super(uri, user, password, ssl);
		
		this.collectionName = collectionName;
	}
}
