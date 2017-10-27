/*

Network Notifier

Copyright (C) 2015 Jesús Diéguez Fernández

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA or see http://www.gnu.org/licenses/.

*/

package es.reprogramador.util;

import java.security.MessageDigest;

public class digestHelper {
    private String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
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
        MessageDigest digest;
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
