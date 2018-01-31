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
	public void resourceDownloadStarted(String resourceName);
	
	/**
	 * @param f {@link File} into which the resource finished downloading
	 */
	public void resourceDownloadFinished(File f, long duration);
	
	public void resourceSkipped(String resourceName);
	
	public void collectionDownloadStarted(String collectionName);
	
	public void collectionDownloadFinished(String collectionName, long duration);
	
	public void collectionSkipped(String collectionName);
	
	public void recursiveDownloadStarted(String rootCollectionName);
	
	public void recursiveDownloadFinished(String rootCollectionName, long duration);
	
	/**
	 * @param ece a non-critical {@link ExistdbClientException}, which must not be interrupting
	 */
	public void exceptionOccured(ExistdbClientException ece);
}
