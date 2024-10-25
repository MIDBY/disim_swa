package it.univaq.framework.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import it.univaq.example.webshop.business.UserResourceDB;
import it.univaq.example.webshop.model.User;
import it.univaq.example.webshop.model.UserRoleEnum;
import jakarta.ws.rs.core.UriInfo;

/**
 *
 * Una classe di utilità di supporto all'autenticazione 
 * qui usiamo JWT per tutte le operazioni
 *
 */
public class AuthHelpers {
    
    private static AuthHelpers instance = null;
    private final JWTHelpers jwt;
    
    public AuthHelpers() {
        jwt = JWTHelpers.getInstance();
    }
    
    public boolean authenticateUser(String email, String password) {
        User user = UserResourceDB.getUserByEmail(email);
        try {
            if(checkPasswordHashPBKDF2(password, user.getPassword()))
                return true;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean authorize(String email) {
        User user = UserResourceDB.getUserByEmail(email);
        if(user.getRole().equals(UserRoleEnum.ORDINANTE))
            return user.isAccepted();
        else
            return true;
    }
    
    public String issueToken(UriInfo context, String username) {
        return jwt.issueToken(context, username);
    }
    
    public void revokeToken(String token) {
        jwt.revokeToken(token);
    }
    
    public String validateToken(String token) {
        return jwt.validateToken(token);
    }
    
    public static AuthHelpers getInstance() {
        if (instance == null) {
            instance = new AuthHelpers();
        }
        return instance;
    }

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

     //--------- PASSWORD SECURITY ------------
    //support functions for the password hashing functions
    private static String bytesToHexString(byte[] byteArray) {
        StringBuilder hexStringBuffer = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            char[] hexDigits = new char[2];
            hexDigits[0] = Character.forDigit((byteArray[i] >> 4) & 0xF, 16);
            hexDigits[1] = Character.forDigit((byteArray[i] & 0xF), 16);
            hexStringBuffer.append(new String(hexDigits));
        }
        return hexStringBuffer.toString();
    }

    private static byte[] hexStringToBytes(String hexString) {
        byte[] byteArray = new byte[hexString.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            int val = Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
            byteArray[i] = (byte) val;
        }
        return byteArray;
    }
    
    //password hashing with PBKDF2 + salt
    private static String getPasswordHashPBKDF2(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (salt.length != 16) {
            throw new IllegalArgumentException("Salt must be 16 bytes");
        }
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        byte[] digest = factory.generateSecret(spec).getEncoded();
        return bytesToHexString(salt) + bytesToHexString(digest);
    }

    public static String getPasswordHashPBKDF2(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return getPasswordHashPBKDF2(password, salt);
    }

    //check password hashed by getPasswordHashPBKDF2
    public static boolean checkPasswordHashPBKDF2(String password, String passwordhash) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[16];
        System.arraycopy(hexStringToBytes(passwordhash), 0, salt, 0, 16);
        return (getPasswordHashPBKDF2(password, salt)).equals(passwordhash);
    }

    
}