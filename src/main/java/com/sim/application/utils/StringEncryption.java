package com.sim.application.utils;

import javafx.util.Pair;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


public class StringEncryption {
    private final int AES_KEY_SIZE = 128;
    private final KeyGenerator keyGenerator;
    private final SecureRandom rng;

    public StringEncryption() {
        try {
            this.keyGenerator = KeyGenerator.getInstance("AES");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("AES key generator should always be available in a Java runtime", e);
        }
        this.rng = new SecureRandom();
        keyGenerator.init(AES_KEY_SIZE, rng);
    }

    private SecretKey generateAESKey() {
        return keyGenerator.generateKey();
    }

    private IvParameterSpec generateIV(Cipher cipher) {
        byte[] iv = new byte[cipher.getBlockSize()];
        rng.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public String getEncryptedVariableName() {
        SecretKey key = generateAESKey();
        String hex = Hex.encodeHexString(key.getEncoded());
        while (Character.isDigit(hex.charAt(0))) {
            key = generateAESKey();
            hex = Hex.encodeHexString(key.getEncoded());
        }
        return hex;
    }

    public Pair<String, String> encrypt(String constant) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        byte[] input = constant.getBytes();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey key = generateAESKey();
        IvParameterSpec iv = generateIV(cipher);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encryptedOutput = cipher.doFinal(input);

        byte[] keyIV = StringUtil.appendByteArray(key.getEncoded(), iv.getIV());
        String encodedKey = Base64.getEncoder().encodeToString(keyIV);
        String output = Base64.getEncoder().encodeToString(encryptedOutput);

        return new Pair<>(encodedKey, output);
    }
}

