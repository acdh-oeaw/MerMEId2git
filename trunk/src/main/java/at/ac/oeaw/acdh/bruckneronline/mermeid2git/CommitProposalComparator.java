package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import java.util.Comparator;

/**
 * 
 * @author mcupak
 *
 */
public class CommitProposalComparator implements Comparator<CommitProposal> {

	public enum CompareBy {
		RESOURCE,
		WAB,
		TITLE,
		RESPONSIBLE,
		COMMIT_MESSAGE,
		SKIP_FLAG,
		COMMITER_NAME
	}
	
	private CompareBy compareBy;
	private boolean acsending;
	
	public CommitProposalComparator() {
		this(CompareBy.WAB);
	}
	
	public CommitProposalComparator(CompareBy cb) {
		this.compareBy = cb;
		this.acsending = true;
	}
	
	public CompareBy getCompareBy() {
		return compareBy;
	}

	public void setCompareBy(CompareBy compareBy) {
		this.compareBy = compareBy;
	}

	public boolean isAcsending() {
		return acsending;
	}

	public void setAcsending(boolean acsending) {
		this.acsending = acsending;
	}
	
	public void switchAscDesc() {
		setAcsending(!isAcsending());
	}

	@Override
	public int compare(CommitProposal o1, CommitProposal o2) {
		int ret = 0;
		switch (compareBy) {
			case COMMIT_MESSAGE:
				ret = compareStr(o1.getCommitMessage(), o2.getCommitMessage());
				break;
				
			case COMMITER_NAME:
				Commiter c1 = o1.getCommiter();
				Commiter c2 = o2.getCommiter();
				String commiterName1 = c1 == null ? null : c1.personIdent.getName();
				String commiterName2 = c2 == null ? null : c2.personIdent.getName();
				ret = compareStr(commiterName1, commiterName2);
				break;
				
			case RESOURCE:
				ret = compareStr(o1.file.getName(), o2.file.getName());
				break;
				
			case RESPONSIBLE:
				ret = compareStr(o1.responsible, o2.responsible);
				break;
				
			case SKIP_FLAG:
				ret = Boolean.compare(o1.isSkip(), o2.isSkip());
				break;
				
			case TITLE:
				ret = compareStr(o1.title, o2.title);
				break;
				
			case WAB:
				ret = compareStr(o1.wab, o2.wab);
				break;
		}
		if (!acsending) {
			ret *= -1;
		}
		return ret;
	}
	
	private static int compareStr(String s1, String s2) {
		if (s1 == null && s2 == null) {
			return 0;
		} else if (s1 == null && s2 != null) {
			return -1;
		} else if (s1 != null && s2 == null) {
			return 1;
		} else {
			return s1.compareTo(s2);
		}
	}
}
