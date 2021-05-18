package relief.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    private static String keyStorePW = "mypkpassword";  
    private static String digSigAlias = "mycert";  
	private static String digSigType = "SHA256withRSA";
	
	private static KeyStore keystore = null;  
    private static char[] storePass = null;  
 	//private static boolean keyloadedFlag = false;

    public DigSig() throws Exception {
    	init();
    }
    
	public DigSig(String configFile) throws Exception {
		parseConfigInit(configFile);

		init();
	}
	
	private void parseConfigInit(String configFile) {
		File confFile = new File(configFile);
		if (!confFile.exists()) {
			if (!confFile.mkdir()) {
				System.err.println("Unable to find " + confFile);
	            System.exit(1);
	        }
		}
		try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if (line.startsWith("keyStoreFile")) {
		    	   String[] tokens = line.split("=");
		    	   keyStoreFile = tokens[1];
		    	   DebugLog.log("keyStoreFile=" + keyStoreFile);
		       } else if (line.startsWith("digSigType")) {
		    	   String[] tokens = line.split("=");
		    	   digSigType = tokens[1];
		    	   DebugLog.log("digSigType=" + digSigType);
		       } else if (line.startsWith("digSigAlias")) {
		    	   String[] tokens = line.split("=");
		    	   digSigAlias = tokens[1];
		    	   DebugLog.log("digSigAlias=" + digSigAlias);
		       } else if (line.startsWith("keyStorePW")) {
		    	   String[] tokens = line.split("=");
		    	   keyStorePW = tokens[1];
		    	   DebugLog.log("keyStorePW=" + keyStorePW);
		       }
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void init() throws Exception, CertificateException, IOException {

		System.out.println("init function executed");
		
		//keystore = KeyStore.getInstance("BKS");  
		keystore = KeyStore.getInstance("JKS");
	    storePass = keyStorePW.toCharArray();  

		// load the key store from file system
	 	FileInputStream fileInputStream = new FileInputStream(keyStoreFile);
	 	keystore.load(fileInputStream, storePass);
	 	fileInputStream.close();
	}
	
	
	public byte[] getDigSig(byte[] plainText) throws Exception {

		//if (!keyloadedFlag) {
		//	init();
		//	keyloadedFlag = true;
		//}
		
        /***************************signing********************************/  
        //read the private key  
        KeyStore.ProtectionParameter keyPass = new KeyStore.PasswordProtection(storePass);  
        KeyStore.PrivateKeyEntry privKeyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(digSigAlias, keyPass);  
        PrivateKey privateKey = privKeyEntry.getPrivateKey();  
		
        //initialize the signature with signature algorithm and private key  
        Signature signature = Signature.getInstance(digSigType);  
        signature.initSign(privateKey);  


        //update signature with data to be signed  
        signature.update(plainText);  

        //sign the data  
        byte[] signedInfo = signature.sign();  

        //System.out.println(signedInfo.toString());
        
        return signedInfo;
	}
	
	public boolean verifyDigSig(byte[] plainText, byte[] signature) throws Exception {
		
		//if (!keyloadedFlag) {
		//	init();
		//}
		
		/**************************verify the signature****************************/  
        Certificate publicCert = keystore.getCertificate(digSigAlias);  

        //create signature instance with signature algorithm and public cert, to verify the signature.  
        Signature verifySig = Signature.getInstance(digSigType);  
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
        
        DigSig digSig = new DigSig();
        
        byte[] signedInfo = digSig.getDigSig(dataInBytes);
        if (digSig.verifyDigSig(dataInBytes, signedInfo)) {
        	System.out.println("DigSig main: testing functions succeeded!");
        } else {
        	System.out.println("DigSig main: testing functions FAILED");
        }
	}
}
