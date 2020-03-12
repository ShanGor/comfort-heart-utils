package tech.comfortheart.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.BitSet;

public final class XorUtils {
    /**
     * Convert a String to BitSet.
     * @param str
     * @return
     */
    public static final BitSet stringToBitSet(final String str) {
        byte[] buf = str.getBytes(StandardCharsets.US_ASCII);
        return BitSet.valueOf(buf);
    }

    /**
     * Run it first time, encrypted, run it the second time with the same key, decrypt.
     * @param string
     * @param key
     * @return
     */
    public static final String XOR(final String string, final String key) {
        return new String(xorAsBitSet(string, key), StandardCharsets.US_ASCII);
    }

    /**
     * Run it first time, encrypted, run it the second time with the same key, decrypt.
     * @param string
     * @param key
     * @return
     */
    public static final byte[] xorAsBitSet(final String string, final String key) {
        BitSet set = stringToBitSet(string);
        set.xor(stringToBitSet(key));
        return set.toByteArray();
    }

    public static final byte[] encodeBase64(byte[] buf) {
        return Base64.getEncoder().encode(buf);
    }

    public static final byte[] decodeBase64(byte base64[]) {
        return Base64.getDecoder().decode(base64);
    }
}
