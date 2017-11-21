package at.ac.oeaw.acdh.bruckneronline.mermeid2git.db;

import java.io.File;
import java.util.List;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

/*
 * TODO : refactor into reusables
 */
/**
 * 
 * @author mcupak
 *
 */
public class MermeidExistdbClient extends ExistdbClient {

	private final Collection dbCollectionMermeid;
	
	public MermeidExistdbClient(MermeidExistdbConnectionInfo meci, File repoDir) throws ExistdbClientException {
		super(meci, repoDir);
		try {
			dbCollectionMermeid = rootCollection.getChildCollection(meci.collectionName);
			
		} catch (XMLDBException e) {
			throw new ExistdbClientException("Unable to get Mermeid collection", e);
		}
	}
	
	public void download(List<String> includeResourceNames, List<String> excludeResourceNames) throws ExistdbClientException {
		super.download(dbCollectionMermeid, includeResourceNames, excludeResourceNames);
	}
	
	public void close() throws ExistdbClientException {
		try {
			if (dbCollectionMermeid != null) {
				dbCollectionMermeid.close();
			}
		} catch (XMLDBException e) {
			throw new ExistdbClientException("cannot close Mermeid collection", e);
		}
		super.close();
	}
}
