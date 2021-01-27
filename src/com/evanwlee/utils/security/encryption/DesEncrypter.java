package com.evanwlee.utils.security.encryption;
/*
 * DesEncrypter.java
 *
 * Created on May 25, 2005, 2:43 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import javax.crypto.Cipher;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import javax.crypto.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.string.StringUtils;

public class DesEncrypter {
    Cipher ecipher;
    Cipher dcipher;
    private Logger log = LoggerFactory.getLogger(DesEncrypter.class.getName());
    
    // 8-byte Salt
    byte[] salt = {
        (byte)0xA9, (byte)0x9B, 
        (byte)0xC8, (byte)0x32,
        (byte)0x56, (byte)0x35,
        (byte)0xE3, (byte)0x03
    };
    
    // Iteration count
    int iterationCount = 19;
    
    public DesEncrypter() {
    	this("pass");
    }
    public DesEncrypter(String passPhrase) {
        try {
            
            if("".equals(passPhrase)){
                passPhrase = "pass";
            }
            
            // Create the key
            KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            ecipher = Cipher.getInstance(key.getAlgorithm());
            dcipher = Cipher.getInstance(key.getAlgorithm());
            
            // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
            
            // Create the ciphers
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        } catch (java.security.InvalidAlgorithmParameterException e) {
            log.error("Problem in DesEncrypt.DesEncrypter(InvalidAlgorithmParameterException): " + e);
        } catch (java.security.spec.InvalidKeySpecException e) {
            log.error("Problem in DesEncrypt.DesEncrypter(InvalidKeySpecException): " + e);
        } catch (javax.crypto.NoSuchPaddingException e) {
            log.error("Problem in DesEncrypt.DesEncrypter(NoSuchPaddingException): " + e);
        } catch (java.security.NoSuchAlgorithmException e) {
            log.error("Problem in DesEncrypt.DesEncrypter(NoSuchAlgorithmException): " + e);
        } catch (java.security.InvalidKeyException e) {
            log.error("Problem in DesEncrypt.DesEncrypter(InvalidKeyException): " + e);
        }catch (Exception e) {
            log.error("Problem in DesEncrypt.DesEncrypter(Exception): " + e);
        }
    }
    
    public String encrypt(String str) {
    	if("".equals(str.trim())){
    		return "";
    	}
        try {
        	log.trace("Encrypting string: " + str);
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");
            
            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);
            
            // Encode bytes to base64 to get a string
           // return new sun.misc.BASE64Encoder().encode(enc);
            return new Base64().encodeToString(enc);
        } catch (javax.crypto.BadPaddingException e) {
            log.error("Problem in DesEncrypt.encrypt(BadPaddingException): " + e);
        } catch (IllegalBlockSizeException e) {
            log.error("Problem in DesEncrypt.encrypt(IllegalBlockSizeException): " + e);
        } catch (java.io.IOException e) {
            log.error("Problem in DesEncrypt.encrypt(IOException): " + e);
        }catch (Exception e) {
            log.error("Problem in DesEncrypt.encrypt(Exception): " + e);
        }
        return null;
    }
    
    public  String decrypt(String str) {
    	if(StringUtils.isEmpty(str)){
    		return "";
    	}
        try {
        	log.trace("Decrypting string: " + str);
            // Decode base64 to get bytes
            //byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
            
            Base64 decoder = new Base64();
           // byte[] saltArray = decoder.decode(saltD);
            byte[] ciphertextArray = decoder.decode(str);
            
            // Decrypt
            byte[] utf8 = dcipher.doFinal(ciphertextArray);
            
            // Decode using utf-8
            return new String(utf8, "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
            log.error("Problem in DesEncrypt.decrypt(BadPaddingException): " + e);
        } catch (IllegalBlockSizeException e) {
            log.error("Problem in DesEncrypt.decrypt(IllegalBlockSizeException): " + e);
        } catch (java.io.IOException e) {
            log.error("Problem in DesEncrypt.decrypt(IOException): " + e);
        } catch (Exception e) {
            log.error("Problem in DesEncrypt.decrypt(Exception): " + e);
        }
        return null;
    }
    
    public static void main(String args[]){
        try{
            if (args.length != 2) {
                System.out.println(
                        "USAGE: java DesEncrypter " +
                        "[-e plain_password] or [-d encrypted_password");
                System.exit(1);
            }
            DesEncrypter crypter = new DesEncrypter();
            
            if("-e".equals(args[0])){
                // Encrypt
                String encrypted = crypter.encrypt(args[1]);
                System.out.println(encrypted);
            }else{
                // Decrypt
                String decrypted = crypter.decrypt(args[1]);
                System.out.println(decrypted);
            }
            

            
        }catch(Exception e){
            System.err.println("Problem in DesEncrypt.main(): " + e);
        }
    }
}

