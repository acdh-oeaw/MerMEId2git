package at.ac.oeaw.acdh.bruckneronline.mermeid2git.spikes;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class PasswordTest2 {

	private static final String ALGORITHM = "PBEWithHmacSHA256AndAES_128";
	
	public static void main(String[] args) throws Exception {

		byte[] salt = PasswordEncryptionService.generateSalt();

		SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
		byte[] iv = new byte[16];
		rnd.nextBytes(iv);

		String password = "password";
		byte[] plaintext = "plaintext".getBytes(StandardCharsets.UTF_8);

		IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 10000, ivParamSpec);
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		try {
			SecretKeyFactory kf = SecretKeyFactory.getInstance(ALGORITHM);
			SecretKey secretKey = kf.generateSecret(keySpec);

			// On J2SE the SecretKeyfactory does not actually generate a key, it just wraps the password.
			// The real encryption key is generated later on-the-fly when initializing the cipher
			System.out.println(new String(secretKey.getEncoded()));

			// Encrypt
			Cipher enc = Cipher.getInstance(ALGORITHM);
			enc.init(Cipher.ENCRYPT_MODE, secretKey, pbeParamSpec);
			byte[] encrypted = enc.doFinal(plaintext);
			System.out.println("Encrypted text: " + Arrays.toString(encrypted));

			// Decrypt
			Cipher dec = Cipher.getInstance(ALGORITHM);
			dec.init(Cipher.DECRYPT_MODE, secretKey, pbeParamSpec);
			byte[] decrypted = dec.doFinal(encrypted);
			String message = new String(decrypted, StandardCharsets.UTF_8);

			System.out.println(message);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}
}
