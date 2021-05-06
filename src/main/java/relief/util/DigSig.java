package relief.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;


public class DigSig {
		
	/*
	 * bks is for android bouncy castle blur blur.
	 * jks is for desktops 
	 * 
	 * prerequisite for this is creating keystore
	 * Use the following as an example: 
	 *    keytool -genkeypair -alias mycert -keyalg RSA -sigalg SHA256withRSA -keystore mykeystore.jks
	 * See below for the hard-coded value for the password, alias, the name of the keystore as well as sigalg
	 */
	
	//keystore related constants  
    //private static String keyStoreFile = "data/mykeystore.bks";  
	private static String keyStoreFile = "data/mykeystore.jks";
    private static String password = "mypkpassword";  
    private static String alias = "mycert";  
	private static final String DIG_SIG_TYPE = "SHA256withRSA";
	
	private static KeyStore keystore = null;  
    private static char[] storePass = null;  
 	private static boolean keyloadedFlag = false;
 	 		
	public DigSig() throws Exception {
			
	}
	
	public static void init() throws Exception, CertificateException, IOException {

		System.out.println("init function executed");
		
		//keystore = KeyStore.getInstance("BKS");  
		keystore = KeyStore.getInstance("JKS");
	    storePass = password.toCharArray();  

		// load the key store from file system
	 	FileInputStream fileInputStream = new FileInputStream(keyStoreFile);
	 	keystore.load(fileInputStream, storePass);
	 	fileInputStream.close();
	}
	
	public static byte[] getDigSig(byte[] plainText) throws Exception {

		if (!keyloadedFlag) {
			init();
			keyloadedFlag = true;
		}
		
        /***************************signing********************************/  
        //read the private key  
        KeyStore.ProtectionParameter keyPass = new KeyStore.PasswordProtection(storePass);  
        KeyStore.PrivateKeyEntry privKeyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(alias, keyPass);  
        PrivateKey privateKey = privKeyEntry.getPrivateKey();  
		
        //initialize the signature with signature algorithm and private key  
        Signature signature = Signature.getInstance(DIG_SIG_TYPE);  
        signature.initSign(privateKey);  


        //update signature with data to be signed  
        signature.update(plainText);  

        //sign the data  
        byte[] signedInfo = signature.sign();  

        //System.out.println(signedInfo.toString());
        
        return signedInfo;
	}
	
	public static boolean verifyDigSig(byte[] plainText, byte[] signature) throws Exception {
		
		if (!keyloadedFlag) {
			init();
		}
		
		/**************************verify the signature****************************/  
        Certificate publicCert = keystore.getCertificate(alias);  

        //create signature instance with signature algorithm and public cert, to verify the signature.  
        Signature verifySig = Signature.getInstance(DIG_SIG_TYPE);  
        verifySig.initVerify(publicCert);  

        //update signature with signature data.  
        verifySig.update(plainText);  

        //verify signature  
        boolean isVerified = verifySig.verify(signature);  

        //if (isVerified) {  
        //   System.out.println("Signature verified successfully");  
        //}  
        
        return isVerified;
	}
	
	
	public static void main(String[] args) throws Exception {
		//Read the string into a buffer  
        String data = "{\n" +  
                      "  \"schemas\":[\"urn:scim:schemas:core:1.0\"],\n" +  
                      "  \"userName\":\"bjensen\",\n" +  
                      "  \"externalId\":\"bjensen\",\n" +  
                      "  \"name\":{\n" +  
                      "    \"formatted\":\"Ms. Barbara J Jensen III\",\n" +  
                      "    \"familyName\":\"Jensen\",\n" +  
                      "    \"givenName\":\"Barbara\"\n" +  
                      "  }\n" +  
                      "}";  

        byte[] dataInBytes = data.getBytes();  
        
        byte[] signedInfo = getDigSig(dataInBytes);
        if (verifyDigSig(dataInBytes, signedInfo)) {
        	System.out.println("DigSig main: testing functions succeeded!");
        } else {
        	System.out.println("DigSig main: testing functions FAILED");
        }
	}
}
