package at.ac.oeaw.acdh.bruckneronline.mermeid2git.db;

import java.io.File;
import java.util.List;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

/**
 * 
 * @author mcupak
 *
 */
public class MermeidExistdbClient extends ExistdbClient {

	protected final File mermeidDataDir;
	protected final Collection mermeidCollection;

	public MermeidExistdbClient(MermeidExistdbConnectionInfo meci, File repoDir) throws ExistdbClientException {
		super(meci, repoDir);
		try {
			mermeidCollection = rootCollection.getChildCollection(meci.collectionName);
			
		} catch (XMLDBException e) {
			throw new ExistdbClientException("Unable to get Mermeid collection", e);
		}
		mermeidDataDir = new File(rootCollectionDir, meci.collectionName);
	}
	
	public void download(List<String> includeResourceNames, List<String> excludeResourceNames) throws XMLDBException {
		super.download(mermeidCollection, includeResourceNames, excludeResourceNames);
	}
	
	public void close() throws XMLDBException {
		if (mermeidCollection != null) {
			mermeidCollection.close();
		}
		super.close();
	}
	
	public File getMermeidDataDir() {
		return mermeidDataDir;
	}

	public Collection getMermeidCollection() {
		return mermeidCollection;
	}
}
