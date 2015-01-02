package es.reprogramador.util;

import java.security.MessageDigest;

public class digestHelper {
    private String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public String getMD2Hash(String data) {
        return getHash("MD2", data);
    }

    public String getMD5Hash(String data) {
        return getHash("MD5", data);
    }

    public String getSHA1Hash(String data) {
        return getHash("SHA-1", data);
    }

    public String getSHA256Hash(String data) {
        return getHash("SHA-256", data);
    }

    public String getSHA384Hash(String data) {
        return getHash("SHA-384", data);
    }

    public String getSHA512Hash(String data) {
        return getHash("SHA-512", data);
    }

    private String getHash(String digestType, String data) {
        String hash = "";
        MessageDigest digest=null;
        try {
            digest = MessageDigest.getInstance(digestType);
            digest.update(data.getBytes());

            hash = bytesToHexString(digest.digest());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return hash;
    }

}
