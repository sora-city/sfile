package com.example.kuntan.sfile;

/**
 * Created by kuntan on 4/5/2016.
 */

import android.util.Log;

import java.io.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.EmptyStackException;

import javax.crypto.*;
import javax.crypto.spec.*;

public class SecureFile {
    static int SHA1_LEN = 20;
    private byte[] KeySignature;  // passcode signature
    private byte[] FileSignature; // signature of the whole file excluding this field
    private byte[] FileTextSignature; // signature of the text file
    private byte[] FileData;          // encrypted file payload
    private String strFileText;       // decrypted file text

    public enum SF_ERROR {
        SUCCESS,
        ERROR,
        FILE_CORRUPT,
        KEY_ERROR,
        TEXT_CORRUPT
    }
    public SecureFile () {
        KeySignature = new byte[SHA1_LEN];

        FileData = null;
        FileSignature = new byte[SHA1_LEN];

        strFileText   = "";
        FileTextSignature = new byte[SHA1_LEN];
    }

    public String ByteToString (byte[] bytes) {
        String str = "";
        for (int i=0; i<bytes.length; i++) {
            String s = Integer.toHexString(bytes[i]&0xff);
            if ( s.length() == 1) str = str + "0" + s;
            else str = str + s;
        }
        return str;
    }

    public void setText (String str ) {
        strFileText = str;
    }
    public String getText () {
        return strFileText;
    }
    private SecretKeySpec generateKey ( String passcode ) {
        // generate key from a pass phrase
        // KeyGenerator kgen = null;
        byte[] salt = {
                (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
                (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
        };
        try {
            /*kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance( "SHA1PRNG", "Crypto");
            sr.setSeed(passcode.getBytes());
            kgen.init(128, sr);
            SecretKey sk = kgen.generateKey();
            byte[] enCodeFormat = sk.getEncoded();
            SecretKeySpec k = new SecretKeySpec (enCodeFormat, "AES");
            */
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(passcode.toCharArray(), salt, 65536, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec k = new SecretKeySpec(tmp.getEncoded(), "AES");
            return k;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public byte[] cipher (int cipher_mode, byte[] data, String passcode ) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(cipher_mode, generateKey (passcode));
            byte[] result = cipher.doFinal(data);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte[] encryptData ( byte[] data, String passcode ) {

        byte[] encode = cipher (Cipher.ENCRYPT_MODE, data,passcode);
        return encode;
    }

    public byte[] decryptData ( byte[] data, String passcode) {
        byte[] result = cipher ( Cipher.DECRYPT_MODE, data, passcode );
        return result;
    }

    public byte[] generateDigest ( byte[] data ) {
        MessageDigest md = null;
        byte[] digest = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            digest = md.digest(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return digest;
    }

    public byte[] generateDigest ( String text ) {
        return generateDigest (text.getBytes());
    }

    public boolean ReadFile ( String fname ) {
        boolean bSucc = false;
        InputStream input = null;

        try {
            try {
                File f = new File (fname);
                int size = (int)f.length();
                if ( size < SHA1_LEN * 3) {
                    return false;
                }

                input = new BufferedInputStream (new FileInputStream (f));

                int byteRead = input.read ( FileSignature, 0, SHA1_LEN);
                if ( byteRead != SHA1_LEN) throw new EmptyStackException();

                byteRead = input.read ( KeySignature, 0, SHA1_LEN);
                if ( byteRead != SHA1_LEN) throw new EmptyStackException();

                byteRead = input.read ( FileTextSignature, 0, SHA1_LEN);
                if ( byteRead != SHA1_LEN) throw new EmptyStackException();

                size = size - SHA1_LEN * 3;
                FileData = new byte[size];
                byteRead = input.read ( FileData, 0, size );
                if ( byteRead != size) throw new EmptyStackException();

                bSucc = true;
            } finally {
                input.close ();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return bSucc;
    }

    public boolean SaveFile ( String fname, byte[] s1, byte[] s2, byte[] s3, byte[] data ) {
        OutputStream output = null;
        boolean bSucc = false;
        try {
            try {
                File f = new File (fname);

                output = new BufferedOutputStream (new FileOutputStream (f));
                output.write(s1);
                output.write(s2);
                output.write(s3);
                output.write(data);
                bSucc = true;
            } finally {
                output.close ();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return bSucc;
    }

    public SF_ERROR LoadSecureFile ( String fname, String passcode ) {
        SF_ERROR Code = SF_ERROR.ERROR;
        if ( !ReadFile ( fname ) ) {
            return SF_ERROR.FILE_CORRUPT;
        }

        // 1. Check file integrity
        MessageDigest fmd = null;
        byte[] dig = null;
        try {
            fmd = MessageDigest.getInstance("SHA-1");
            fmd.update(KeySignature);
            fmd.update(FileTextSignature);
            fmd.update(FileData);

            dig = fmd.digest();
            if ( !Arrays.equals(dig, FileSignature) ) {
                return SF_ERROR.FILE_CORRUPT;
            }

            dig = generateDigest(passcode);
            if ( !Arrays.equals(dig, KeySignature) ) {
                return SF_ERROR.KEY_ERROR;
            }

            byte[] result = decryptData ( FileData, passcode );

            dig = generateDigest (result);
            if ( !Arrays.equals(dig, FileTextSignature) ) {
                return SF_ERROR.TEXT_CORRUPT;
            }

            strFileText = new String (result);
            Code = SF_ERROR.SUCCESS;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Code;
    }

    public boolean SaveSecureFile (String fname, String passcode ) {
        // When this function is called, we assume the strFileText is updated
        boolean bSucc = false;

        MessageDigest fmd = null;

        try {
            // encrypt data
            // String s = new String (strFileText.getBytes(), "GB2312"); // support chinese
            byte[] data = strFileText.getBytes();
            byte[] endata = encryptData ( data, passcode );

            // generate signature
            KeySignature       = generateDigest ( passcode.getBytes());
            FileTextSignature = generateDigest ( data );

            fmd = MessageDigest.getInstance("SHA-1");
            fmd.update(KeySignature);
            fmd.update(FileTextSignature);
            fmd.update(endata);

            FileSignature = fmd.digest();
            SaveFile (fname, FileSignature, KeySignature, FileTextSignature, endata);
            bSucc = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return bSucc;
    }
}
