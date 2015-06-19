/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.auth.otp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;

public class OneTimePassword {
    
    private static final Logger logger = Logger.getLogger(OneTimePassword.class.getName());

    public static final String QR_URL = "url";
    public static final String SECRET = "secret";
    public static final String OTPAUTH = "otpauth";

    public static final int WINDOW = 3;

    public static final String GURL = "https://www.google.com/chart?chs=250x250&cht=qr&chl=otpauth://totp/";

    private final static SecureRandom random = new SecureRandom();

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
    public static Map<String, String> generateQR(String subject, String issuer) {
        byte[] secret = new byte[10];
        random.nextBytes(secret);
        Base32 base32 = new Base32();
        byte[] encodedSecret = base32.encode(secret);
        String secretBase32 = new String(encodedSecret);
        return generateQR(subject, issuer, secretBase32);
    }        

    public static Map<String, String> generateQR(String subject, String issuer, String secret) {

        String otpAuth = null;
        Map<String, String> result = new HashMap<>();
        try {
            otpAuth = URLEncoder.encode(issuer + ":" + subject + "?secret=" 
                    + secret + "&issuer=" + issuer, "utf-8");
            result.put(OTPAUTH, otpAuth);
        } catch (UnsupportedEncodingException ex) {
            //Should not happen
            logger.log(Level.SEVERE, "Encoding otpAuth", ex);
            return null;
        }

        result.put(SECRET, secret);
        result.put(QR_URL, GURL + otpAuth);        
        
        return result;
    }

    public static String bytesToHexStr(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean checkCode(
            String secret,
            long code,
            long t)
            throws NoSuchAlgorithmException,
            InvalidKeyException {

        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        long time = t / 30000;

        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.         
        for (int i = -WINDOW; i <= WINDOW; ++i) {
            long hash = verifyCode(decodedKey, time + (i * 30000));
            if (hash == code)
                return true;
        }

        // The validation code is invalid.
        return false;
    }

    private static int verifyCode(
            byte[] key,
            long t)
            throws NoSuchAlgorithmException,
            InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }

        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        int offset = hash[20 - 1] & 0xF;

        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }

        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;

        return (int) truncatedHash;
    }

}
