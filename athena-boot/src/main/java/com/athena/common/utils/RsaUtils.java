package com.athena.common.utils;

import com.athena.common.exception.RRException;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 解密工具，格式与前端 {@code src/utils/crypto.ts}（JSEncrypt）保持一致：
 * 私钥为 Base64(PKCS#8)，密文为 hex。
 */
public final class RsaUtils {

    private static final String ALGORITHM = "RSA";

    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private RsaUtils() {
    }

    public static String decryptByPrivate(String privateKeyString, String content) {
        try {
            PrivateKey privateKey = getPrivateKey(privateKeyString);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            // RSA 单次解密长度受密钥长度限制，前端 JSEncrypt 会按同样长度分块加密
            int splitLength = ((RSAPrivateKey) privateKey).getModulus().bitLength() / 8;
            StringBuilder result = new StringBuilder();
            for (byte[] block : splitBytes(hexToBytes(content), splitLength)) {
                result.append(new String(cipher.doFinal(block), StandardCharsets.UTF_8));
            }
            return result.toString();
        }
        catch (Exception exception) {
            throw new RRException("RSA 解密失败", exception);
        }
    }

    private static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] decoded = DECODER.decode(key.replace("\r\n", ""));
        return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private static byte[][] splitBytes(byte[] bytes, int splitLength) {
        int remainder = bytes.length % splitLength;
        int quotient = remainder != 0 ? bytes.length / splitLength + 1 : bytes.length / splitLength;
        byte[][] arrays = new byte[quotient][];
        for (int i = 0; i < quotient; i++) {
            int length = (i == quotient - 1 && remainder != 0) ? remainder : splitLength;
            arrays[i] = new byte[length];
            System.arraycopy(bytes, i * splitLength, arrays[i], 0, length);
        }
        return arrays;
    }

    private static byte[] hexToBytes(String hex) {
        int length = hex.length() / 2;
        hex = hex.toUpperCase();
        byte[] result = new byte[length];
        char[] chars = hex.toCharArray();
        for (int i = 0; i < length; i++) {
            int position = i * 2;
            result[i] = (byte) (toByte(chars[position]) << 4 | toByte(chars[position + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
