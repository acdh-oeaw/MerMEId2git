package at.ac.oeaw.acdh.bruckneronline.mermeid2git.db;

public class ExistdbClientException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExistdbClientException(String msg) {
		super(msg);
	}
	
	public ExistdbClientException(String msg, Throwable cause) {
		super(msg, cause);
	}
}