package at.ac.oeaw.acdh.bruckneronline.mermeid2git.spikes;

import java.util.Arrays;

/**
 * 
 * @author mcupak
 *
 */
public class PasswordEncryptionServiceTest {

	public static void main(String[] args) throws Exception {
		String password = "abcde";
		
		byte[] salt = PasswordEncryptionService.generateSalt();
		byte[] encryptedPassword = PasswordEncryptionService.getEncryptedPassword(password, salt);
		
		System.out.println("salt " + Arrays.toString(salt));
		System.out.println("encryptedPassword " + Arrays.toString(encryptedPassword));
		
		boolean authenticated = true;
		
		String password2 = "abcdef";
		authenticated = PasswordEncryptionService.authenticate(password2, encryptedPassword, salt);
		System.out.println(authenticated);
		
		String password3 = "abcde";
		authenticated = PasswordEncryptionService.authenticate(password3, encryptedPassword, salt);
		System.out.println(authenticated);
	}
}
