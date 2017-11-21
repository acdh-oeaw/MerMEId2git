package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import org.eclipse.jgit.lib.PersonIdent;

/**
 * 
 * @author mcupak
 *
 */
public class Commiter {
	
	private final static String
		EMAIL_OPENING_BRACKET = " <",
		EMAIL_CLOSING_BRACKET = ">",
		FLAG_AUTO_INCLUDE = "autoinclude";

	public final PersonIdent personIdent;
	public final boolean autoInclude;
	
	public Commiter(String config) {
		
		int emailOpeningBracketIndex = config.indexOf(EMAIL_OPENING_BRACKET);
		int emailClosingBracketIndex = config.indexOf(EMAIL_CLOSING_BRACKET);
		if (emailOpeningBracketIndex == -1 || emailClosingBracketIndex == -1) {
			throw new IllegalArgumentException("unable to parse commiter '" + config + "'");
		}

		final String name = config.substring(0, emailOpeningBracketIndex);
		final String email = config.substring(emailOpeningBracketIndex + EMAIL_OPENING_BRACKET.length(), emailClosingBracketIndex);
		this.personIdent = new PersonIdent(name, email);
		
		final String flags = config.substring(emailClosingBracketIndex + EMAIL_CLOSING_BRACKET.length());
		if (flags.contains(FLAG_AUTO_INCLUDE)) {
			this.autoInclude = true;
		} else {
			this.autoInclude = false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (autoInclude ? 1231 : 1237);
		result = prime * result + ((personIdent == null) ? 0 : personIdent.hashCode());
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
		Commiter other = (Commiter) obj;
		if (autoInclude != other.autoInclude)
			return false;
		if (personIdent == null) {
			if (other.personIdent != null)
				return false;
		} else if (!personIdent.equals(other.personIdent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Commiter [personIdent=");
		builder.append(personIdent);
		builder.append(", autoInclude=");
		builder.append(autoInclude);
		builder.append("]");
		return builder.toString();
	}
}
