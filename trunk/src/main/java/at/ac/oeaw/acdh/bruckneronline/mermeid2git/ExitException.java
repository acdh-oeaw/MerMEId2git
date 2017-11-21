package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

/**
 * 
 * @author mcupak
 *
 */
public class ExitException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public final ExitCode exitCode;
	
	public ExitException(String message, ExitCode ec) {
		this(message, ec, null);
	}
	
	public ExitException(String message, ExitCode ec, Throwable cause) {
		super(message, cause);
		this.exitCode = ec;
	}
}
