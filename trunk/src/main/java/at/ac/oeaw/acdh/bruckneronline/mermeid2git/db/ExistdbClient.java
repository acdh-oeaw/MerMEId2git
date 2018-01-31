package at.ac.oeaw.acdh.bruckneronline.mermeid2git.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.exist.xmldb.EXistResource;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;

/**
 * 
 * @author mcupak
 *
 */
public class ExistdbClient {
	
	protected final File rootCollectionDir;
	protected final Collection rootCollection;
	
	protected final List<ExistdbClientCallback> callbacks;
	
	public ExistdbClient(ExistdbConnectionInfo eci, File rootDir) throws ExistdbClientException {
		this.callbacks = new ArrayList<>();
		
        try {
	        Class<?> cl = Class.forName(ExistdbConnectionInfo.DRIVER_NAME);
	        Database database = (Database) cl.newInstance();
	        database.setProperty("ssl-enable", String.valueOf(eci.ssl));
			DatabaseManager.registerDatabase(database);
			rootCollection = DatabaseManager.getCollection(eci.uri + ExistdbConnectionInfo.ROOT_COLLECTION_NAME, eci.user, eci.password);

		} catch (XMLDBException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new ExistdbClientException("unable to establish database connection", e);
		}
	        
		try {
			if (!rootDir.exists()) {
				throw new IOException("can't find directory '" + rootDir.getAbsolutePath() + "'");
			}
			if (!rootDir.canWrite()) {
				throw new IOException("can't write to directory '" + rootDir.getAbsolutePath() + "'");
			}
			
		} catch (IOException e) {
			throw new ExistdbClientException("unable to initialize root directory", e);
		}
		this.rootCollectionDir = new File(rootDir, ExistdbConnectionInfo.ROOT_COLLECTION_NAME);
	}
	
	public Collection getRootCollection() {
		return rootCollection;
	}
	
	public File getRootCollectionDirectory() {
		return rootCollectionDir;
	}
	
	public boolean addCallback(ExistdbClientCallback ecc) {
		return callbacks.add(ecc);
	}
	
	public void recursiveDownload(List<String> colNameInclFilter, List<String> colNameExclFilter, List<String> resNameInclFilter, List<String> resNameExclFilter) throws XMLDBException {
		recursiveDownload(rootCollection, colNameInclFilter, colNameExclFilter, resNameInclFilter, resNameExclFilter);
	}
	
	public void recursiveDownload(Collection c, List<String> colNameInclFilter, List<String> colNameExclFilter, List<String> resNameInclFilter, List<String> resNameExclFilter) throws XMLDBException {
		final long tsStartRecursiveDownload = System.currentTimeMillis();

		for (ExistdbClientCallback ece : callbacks) {
			ece.recursiveDownloadStarted(c.getName());
		}
		
		recursiveDownload_impl(c, colNameInclFilter, colNameExclFilter, resNameInclFilter, resNameExclFilter);
		
		final long tsDuration = System.currentTimeMillis() - tsStartRecursiveDownload;
		for (ExistdbClientCallback ece : callbacks) {
			ece.recursiveDownloadFinished(c.getName(), tsDuration);
		}
	}
	
	private void recursiveDownload_impl(Collection c, List<String> colNameInclFilter, List<String> colNameExclFilter, List<String> resNameInclFilter, List<String> resNameExclFilter) throws XMLDBException {
		
		// should this collection be filtered by name?
		String collectionName = c.getName();
		if ((colNameInclFilter != null
				&& !containsAsSubstring(collectionName, colNameInclFilter))
			|| (colNameExclFilter != null
				&& containsAsSubstring(collectionName, colNameExclFilter))
			) {
			// yes, skip this one
			for (ExistdbClientCallback ece : callbacks) {
				ece.collectionSkipped(collectionName);
			}
			return;
		}
		
		// no, proceed with download
		download(c, resNameInclFilter, resNameExclFilter);
		
		// are there any child collections?
		String[] childCollectionNames = c.listChildCollections();
		if (childCollectionNames == null) {
			// no, nothing more to do
			return;
		}
		
		// yes, try to download them
		for (String childCollectionName : childCollectionNames) {
			Collection childCollection = c.getChildCollection(childCollectionName);
			recursiveDownload_impl(childCollection, colNameInclFilter, colNameExclFilter, resNameInclFilter, resNameExclFilter);
		}
	}
	
	/**
	 * <p>Downloads all resources from given {@link Collection} into {@link #getRootCollectionDirectory()}, using filtering if provided.</p>
	 * 
	 * @param resNameInclFilter include only resources with name being substring of this {@link List}, pass {@code null} for no filtering
	 * @param resNameExclFilter exclude all resources with name being substring of this {@link List}, pass {@code null} for no filtering. When given, this filter is applied after inclusion.
	 * @throws XMLDBException if unable to continue, recoverable exceptions will be forwarded to {@link ExistdbClientCallback#exceptionOccured(ExistdbClientException)}
	 */
	public void download(Collection c, List<String> resNameInclFilter, List<String> resNameExclFilter) throws XMLDBException {
		final long tsStartCollectionDownload = System.currentTimeMillis();
		
		for (ExistdbClientCallback ece : callbacks) {
			ece.collectionDownloadStarted(c.getName());
		}
		
		download_impl(c, resNameInclFilter, resNameExclFilter);
		
		final long tsDuration = System.currentTimeMillis() - tsStartCollectionDownload;
		for (ExistdbClientCallback ece : callbacks) {
			ece.collectionDownloadFinished(c.getName(), tsDuration);
		}
	}
	
	private void download_impl(Collection c, List<String> resNameInclFilter, List<String> resNameExclFilter) throws XMLDBException {
		
		// are there any resources to download?
		String[] resourceNames = c.listResources();
		if (resourceNames == null) {
			// no, nothing to do
			return;
		}
		
		// yes, try to download them
		for (String resourceName : resourceNames) {
			
			// should this resource be filtered by name?
			if ((resNameInclFilter != null
					&& !containsAsSubstring(resourceName, resNameInclFilter))
				|| (resNameExclFilter != null
					&& containsAsSubstring(resourceName, resNameExclFilter))
				) {
				// yes, but try the others
				for (ExistdbClientCallback ece : callbacks) {
					ece.resourceSkipped(resourceName);
				}
				continue;
			}
			
			// no, proceed with download
			EXistResource resource = (EXistResource) c.getResource(resourceName);
			try {
				resourceDownload(resource);
				
			} catch (IOException | XMLDBException e) {
				// failed to download this one, but try the others
				for (ExistdbClientCallback ece : callbacks) {
					ece.exceptionOccured(new ExistdbClientException("exception while downloading resource '" + resourceName + "'", e));
				}
				continue;
			}
		}
	}
	
	/**
	 * <p>Downloads a single resource.</p>
	 * 
	 * @param resource
	 * @throws IOException when file-system exception occurs
	 * @throws XMLDBException when database exception occurs
	 */
	public void resourceDownload(EXistResource resource) throws IOException, XMLDBException {
		final long tsStartCurrentResoruce = System.currentTimeMillis();
		final String resourceName = resource.getId();
		
		for (ExistdbClientCallback ece : callbacks) {
			ece.resourceDownloadStarted(resourceName);
		}

		File downloadTo = new File(rootCollectionDir.getParent() + constructDbPathFor(resource));
		if (!downloadTo.exists()
				&& !downloadTo.mkdirs()) {
			throw new IOException("could not create directory '" + downloadTo.getAbsolutePath() + "'");
		}

		File f = new File(downloadTo, resourceName);
		if (f.exists()
				&& !f.delete()) {
			throw new IOException("could not delete file '" + f.getAbsolutePath() + "'");
		}

		if (!f.createNewFile()) {
			throw new IOException("could not create file '" + f.getAbsolutePath() + "'");
		}
		
		String xml = (String) resource.getContent();
		FileOutputStream fos = new FileOutputStream(f);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		osw.write(xml);
		osw.flush();
		osw.close();

		final long tsDuration = System.currentTimeMillis() - tsStartCurrentResoruce;
		for (ExistdbClientCallback ece : callbacks) {
			ece.resourceDownloadFinished(f, tsDuration);
		}
	}
	
	public void close() throws XMLDBException {
		if (rootCollection != null) {
			rootCollection.close();
		}
	}
	
	private boolean containsAsSubstring(String str, List<String> substrings) {
		for (String substring : substrings) {
			if (str.contains(substring)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>Constructs complete path of a resource within the database by traversing its parent collections up to the root level.</p>
	 * <p>Uses {@link File#separator} as the separator.<p>
	 * 
	 * @param resource
	 * @return complete path of a resource within the database
	 * @throws XMLDBException when unable to traverse parent {@link Collection}s
	 */
	private String constructDbPathFor(EXistResource resource) throws XMLDBException {
		String path = "";
		Collection currentCollection = resource.getParentCollection();
		while (currentCollection != null) {
			String currentCollectionPath = currentCollection.getName();
			String currentCollectionName = currentCollectionPath.substring(currentCollectionPath.lastIndexOf('/') + 1);
			path = File.separator + currentCollectionName + path;
			currentCollection = currentCollection.getParentCollection();
		}
		return path;
	}
}
