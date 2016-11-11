package com.zyzx;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class TestHttps {
	private static final String uri = "http://www.baidu.com/a/c/e";

	private static final CustomizeNameService ns = new CustomizeNameService(new File(TestHttps.class.getResource("/hosts.properties").getFile()));
	static {
		// httpclient3 这样使用
		Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(ns), 443));
		Protocol.registerProtocol("http", new Protocol("http", new CustmizeHttpProtocolSocketFactory(ns), 80));
	}

	@Test
	public void testhttpclient31() throws HttpException, IOException, KeyStoreException {
		// httpClient3.1版本：
		long start = System.currentTimeMillis();
		HttpClient httpclient = new HttpClient();
		httpclient.setConnectionTimeout(1000);
		for (int i = 0; i < 1; i++) {
			
			GetMethod get = new GetMethod(uri);
			try {

				httpclient.executeMethod(get);
			} finally {
				get.releaseConnection();
			}
			System.out.println(get.getStatusCode());
		}
		long end = System.currentTimeMillis();

		System.out.println(end - start);
	}

	
	public void testhttpclient42() throws Exception {
		ClientConnectionManager cm = new PoolingClientConnectionManager() {
			@Override
			protected ClientConnectionOperator createConnectionOperator(SchemeRegistry schreg) {
				return new CustomizeClientConnectionOperator(schreg, ns);
			}
		};

		SchemeRegistry sr = cm.getSchemeRegistry();
		SSLContext sslContext;
		sslContext = SSLContext.getInstance("SSL");
		// set up a TrustManager that trusts everything
		try {
			sslContext.init(null, new TrustManager[] { new EasyX509TrustManager() }, new SecureRandom());
		} catch (KeyManagementException e) {
		}
		SSLSocketFactory ssf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		sr.register(new Scheme("https", 443, ssf));

		// httpClient4.2版本：
		DefaultHttpClient httpclient = new DefaultHttpClient(cm);
		for (int i = 0; i < 10; i++) {
	
			HttpGet get = new HttpGet(uri);
			try {
				HttpResponse resp = httpclient.execute(get);
				HttpEntity entity = resp.getEntity();
				String message = EntityUtils.toString(entity, "utf-8");
				System.out.println(resp.getStatusLine());
				
			} finally {
				get.releaseConnection();
			}

		}
	}
}
