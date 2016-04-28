package com.zyzx;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;



public class EasyX509TrustManager implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
    	System.out.println("getAcceptedIssuers");
        return null;
    }

    public void checkClientTrusted(
            X509Certificate[] certs, String authType) {
    	System.out.println("checkClientTrusted");
    }

    public void checkServerTrusted(
            X509Certificate[] certs, String authType) {
    	for(X509Certificate cert : certs){
    		//System.out.println(cert);
    	}
    	System.out.println("checkServerTrusted");
    }
}
