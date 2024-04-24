package csd2324.trab1.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class InsecureHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String s, SSLSession sslSession) {
        return s.equals("localhost");
    }
}
