package g24;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static final int m = 4;
    public static final byte CR = 0xD, LF = 0xA;  // ASCII codes for <CRLF>
    public static final String CRLF = "\r\n";

    public static final String[] CYPHER_SUITES =  new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_DH_anon_WITH_AES_128_CBC_SHA"};

    public static final int generateHash(String key) throws NoSuchAlgorithmException {
        
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(key.getBytes(StandardCharsets.UTF_8));

        String sha1 = String.format("%040x", new BigInteger(1, md.digest()));

        int hashCode = sha1.hashCode();

        if(hashCode < 0)
            hashCode = -hashCode;
        return hashCode % ((int) Math.pow(2, Utils.m));
    }

	public static void usage(String message) {
	}
}
