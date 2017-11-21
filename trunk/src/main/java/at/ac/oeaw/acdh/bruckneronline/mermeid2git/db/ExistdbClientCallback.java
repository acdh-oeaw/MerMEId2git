package at.ac.oeaw.acdh.bruckneronline.mermeid2git.db;

import java.io.File;

/**
 * 
 * @author mcupak
 *
 */
public interface ExistdbClientCallback {

	/**
	 * @param resourceName name of the resource being downloaded
	 */
	public void downloadStarted(String resourceName);
	
	/**
	 * @param f {@link File} into which the resource finished downloading
	 */
	public void downloadFinished(File f, long duration);
	
	public void downloadFinished(String collectionName, int resourceCount, long duration);
	
	public void skippingResource(String resourceName);
	
	/**
	 * @param ece a non-critical {@link ExistdbClientException}, which must not be interrupting
	 */
	public void exceptionOccured(ExistdbClientException ece);
}
