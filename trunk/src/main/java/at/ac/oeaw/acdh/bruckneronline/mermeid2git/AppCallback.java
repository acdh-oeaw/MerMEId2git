package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import java.io.File;

/**
 * 
 * @author mcupak
 *
 */
public interface AppCallback {
	
	public void downloadDone();
	
	public void fileChangeDetected(CommitProposal cp);
	
	public void commitDone(File f, String commitMessage);
	
	public void pushDone();
}
