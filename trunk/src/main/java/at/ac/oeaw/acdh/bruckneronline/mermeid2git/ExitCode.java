package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

/**
 * 
 * @author mcupak
 *
 */
public enum ExitCode {

	OK(0),
	EXCEPTION_WHILE_SAVING_DEFAULT_SETTINGS(1),
	EXCEPTION_LOADING_SETTINGS(2),
	EXCEPTION_WHILE_DBCLIENT_INIT(3),
	EXCEPTION_WHILE_GITREPO_INIT(4),
	EXCEPTION_WHILE_XML_PARSING_INIT(5),
	EXCEPTION_GETTING_EXECUTION_DIRECTORY(6),
	EXCEPTION_INVALID_XPATH_EXPRESSION(7),
	NO_COMMITTERS_DEFINED(8);
	
	public final int intCode;
	
	private ExitCode(int intCode) {
		this.intCode = intCode;
	}
	
	@Override
	public String toString() {
		return name() + "(" + intCode + ")";
	}
}
