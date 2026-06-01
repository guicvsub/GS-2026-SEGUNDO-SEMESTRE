package com.fiap.demo.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Util {

    private static final String SHA_256 = "SHA-256";

    /**
     * Criptografa uma string usando SHA-256
     * @param input String a ser criptografada
     * @return Hash SHA-256 em formato hexadecimal
     */
    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Verifica se a senha informada corresponde ao hash armazenado
     * @param input Senha em texto plano
     * @param hash Hash SHA-256 armazenado
     * @return true se a senha corresponder ao hash
     */
    public static boolean matches(String input, String hash) {
        String inputHash = hash(input);
        return inputHash.equals(hash);
    }

    /**
     * Converte bytes para representação hexadecimal
     * @param bytes Array de bytes
     * @return String hexadecimal
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
