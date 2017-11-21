package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import java.io.File;

/**
 * 
 * @author mcupak
 *
 */
public class CommitProposal {

	public enum InitialState {
		UNTRACKED("UT"),
		UNCOMMITED("UC"),
		REMOVED("RM");	// not yet implemented - the tool always downloads only, does not clear from local what is not found in MerMEId
		
		private final String toString;
		
		private InitialState(String toString) {
			this.toString = toString;
		}
		
		@Override
		public String toString() {
			return toString;
		}
	}

	public final File file;
	public final String wab;
	public final String title;
	public final String responsible;
	
	public final InitialState initialState;
	
	private String commitMessage;
	private boolean skip;
	private Commiter commiter;
	
	public CommitProposal(InitialState initialState, File file, String wab, String title, String responsible, Commiter commiter) {
		this.initialState = initialState;
		this.file = file;
		this.wab = wab;
		this.title = title;
		this.responsible = responsible;
		this.commiter = commiter;
		this.skip = commiter == null ? false : !commiter.autoInclude;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String proposedCommitMessage) {
		this.commitMessage = proposedCommitMessage;
	}
	
	public boolean isCommitMessageEmpty() {
		return "".equals(commitMessage);
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public Commiter getCommiter() {
		return commiter;
	}

	public void setCommiter(Commiter commiter) {
		this.commiter = commiter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commitMessage == null) ? 0 : commitMessage.hashCode());
		result = prime * result + ((commiter == null) ? 0 : commiter.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((initialState == null) ? 0 : initialState.hashCode());
		result = prime * result + ((responsible == null) ? 0 : responsible.hashCode());
		result = prime * result + (skip ? 1231 : 1237);
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((wab == null) ? 0 : wab.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommitProposal other = (CommitProposal) obj;
		if (commitMessage == null) {
			if (other.commitMessage != null)
				return false;
		} else if (!commitMessage.equals(other.commitMessage))
			return false;
		if (commiter == null) {
			if (other.commiter != null)
				return false;
		} else if (!commiter.equals(other.commiter))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (initialState != other.initialState)
			return false;
		if (responsible == null) {
			if (other.responsible != null)
				return false;
		} else if (!responsible.equals(other.responsible))
			return false;
		if (skip != other.skip)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (wab == null) {
			if (other.wab != null)
				return false;
		} else if (!wab.equals(other.wab))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommitProposal [file=");
		builder.append(file);
		builder.append(", wab=");
		builder.append(wab);
		builder.append(", title=");
		builder.append(title);
		builder.append(", responsible=");
		builder.append(responsible);
		builder.append(", initialState=");
		builder.append(initialState);
		builder.append(", commitMessage=");
		builder.append(commitMessage);
		builder.append(", skip=");
		builder.append(skip);
		builder.append(", commiter=");
		builder.append(commiter);
		builder.append("]");
		return builder.toString();
	}
}
