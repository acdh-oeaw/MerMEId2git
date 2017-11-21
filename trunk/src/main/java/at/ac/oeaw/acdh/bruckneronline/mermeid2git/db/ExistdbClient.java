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
	
	protected final File rootDir;
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
		this.rootDir = rootDir;
	}
	
	public Collection getRootCollection() {
		return rootCollection;
	}
	
	public File getRootDirectory() {
		return rootDir;
	}
	
	public boolean addCallback(ExistdbClientCallback ecc) {
		return callbacks.add(ecc);
	}
	
	/**
	 * Downloads resources from given {@link Collection} into {@link #getRootDirectory()}, using filtering if provided.
	 * 
	 * @param includeResourceNames include only resources with name being substring of this {@link List}, pass {@code null} for no filtering
	 * @param excludeResourceNames , pass {@code null} for no filtering
	 * @throws ExistdbClientException
	 */
	public void download(Collection c, List<String> includeResourceNames, List<String> excludeResourceNames) throws ExistdbClientException {
		final long tsStartCompleteDownload = System.currentTimeMillis();
		
		try {
			String dbPath = "";
			Collection currentCollection = c;
			while (currentCollection != null) {
				String currentCollectionPath = currentCollection.getName();
				String currentCollectionName = currentCollectionPath.substring(currentCollectionPath.lastIndexOf('/') + 1);
				dbPath = File.separator + currentCollectionName + dbPath;
				currentCollection = currentCollection.getParentCollection();
			}
			
			File downloadTo = new File(rootDir + dbPath);
			if (!downloadTo.exists()
					&& !downloadTo.mkdirs()) {
				for (ExistdbClientCallback ece : callbacks) {
					ece.exceptionOccured(new ExistdbClientException("could not create directory '" + downloadTo.getAbsolutePath() + "'"));
				}
			}
			
			String[] resourceNames = c.listResources();
			
			if (resourceNames != null) {
				for (String resourceName : resourceNames) {
					final long tsStartCurrentResoruce = System.currentTimeMillis();
					
					if ((includeResourceNames != null
							&& !containsAsSubstring(resourceName, includeResourceNames))
						|| (excludeResourceNames != null
							&& containsAsSubstring(resourceName, excludeResourceNames))
						) {
						for (ExistdbClientCallback ece : callbacks) {
							ece.skippingResource(resourceName);
						}
						continue;
					}
					
					for (ExistdbClientCallback ece : callbacks) {
						ece.downloadStarted(resourceName);
					}
					
					EXistResource resource = (EXistResource) c.getResource(resourceName);
					
					File xmlFile = new File(downloadTo, resource.getId());
					if (xmlFile.exists()
							&& !xmlFile.delete()) {
						for (ExistdbClientCallback ece : callbacks) {
							ece.exceptionOccured(new ExistdbClientException("could not delete file '" + xmlFile.getAbsolutePath() + "'"));
						}
						continue;
					}
					
					try {
						if (!xmlFile.createNewFile()) {
							for (ExistdbClientCallback ece : callbacks) {
								ece.exceptionOccured(new ExistdbClientException("could not create file '" + xmlFile.getAbsolutePath() + "'"));
							}
							continue;
						}
						
						String xml = (String) resource.getContent();
						FileOutputStream fos = new FileOutputStream(xmlFile);
						OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
						osw.write(xml);
						osw.flush();
						osw.close();

					} catch (IOException e) {
						for (ExistdbClientCallback ece : callbacks) {
							ece.exceptionOccured(new ExistdbClientException("exception while downloading resource '" + resource.getId() + "'", e));
						}
						continue;
					}
					
					for (ExistdbClientCallback ece : callbacks) {
						ece.downloadFinished(xmlFile, System.currentTimeMillis() - tsStartCurrentResoruce);
					}
				}
			}
			
		} catch (XMLDBException e) {
			throw new ExistdbClientException("unable to download resources", e);
		}
		
		final long tsDuration = System.currentTimeMillis() - tsStartCompleteDownload;
		for (ExistdbClientCallback ece : callbacks) {
			try {
				ece.downloadFinished(c.getName(), c.getResourceCount(), tsDuration);
				
			} catch (XMLDBException e) {
				throw new ExistdbClientException("can't get collection info", e);
			}
		}
	}
	
	public void close() throws ExistdbClientException {
		try {
			if (rootCollection != null) {
				rootCollection.close();
			}
		} catch (XMLDBException e) {
			throw new ExistdbClientException("cannot close database connections", e);
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
}
