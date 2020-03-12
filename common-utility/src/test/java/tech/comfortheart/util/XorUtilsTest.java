package tech.comfortheart.util;

import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static tech.comfortheart.util.XorUtils.XOR;


public class XorUtilsTest {
    @Test
    public void testXOR()
    {
        String psw = "Hello";
        String es = XOR(psw, "key");
        assertEquals("IwAVbG8=", new String(Base64.getEncoder().encode(es.getBytes())));
        assertEquals("Hello", XOR(es, "key"));

        assertEquals("aGVsbA==", new String(XorUtils.encodeBase64(XorUtils.stringToBitSet("hell").toByteArray())));
        assertEquals("Bhweb2Q=", new String(XorUtils.encodeBase64(XorUtils.xorAsBitSet("mygod", "key"))));

        assertEquals("Hey", new String(XorUtils.decodeBase64(Base64.getEncoder().encode("Hey".getBytes()))));

        assertEquals("SGV5", new String(XorUtils.encodeBase64("Hey".getBytes())));

        new XorUtils();
    }
}
