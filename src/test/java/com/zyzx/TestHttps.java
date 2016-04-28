package com.zyzx;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.zyzx.CustmizeHttpProtocolSocketFactory;
import com.zyzx.CustomizeNameService;
import com.zyzx.EasySSLProtocolSocketFactory;

public class TestHttps {
	private static final String uri = "http://www.baidu.com";

	@Test
	public void testhttpclient31() throws HttpException, IOException, KeyStoreException {
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.setMaxConnectionsPerHost(10);
		// httpClient3.1版本：
		Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(new File("D:\\logs\\a.txt")), 443));
		Protocol.registerProtocol("http", new Protocol("http", new CustmizeHttpProtocolSocketFactory(new File("D:\\logs\\a.txt")), 80));

		long start = System.currentTimeMillis();
		HttpClient httpclient = new HttpClient();
		GetMethod get = new GetMethod(uri);
		try {
			httpclient.executeMethod(get);
		} finally {
			get.releaseConnection();
		}
	
		long end = System.currentTimeMillis();
		System.out.println(get.getStatusCode());
		System.out.println(end - start);
	}

	private static final DnsResolver dnsResolver = new DnsResolver() {

		private final CustomizeNameService ns = new CustomizeNameService(new File("D:\\logs\\a.txt"));

		public InetAddress[] resolve(String hostname) throws UnknownHostException {
			return ns.lookupAllHostAddr(hostname);
		}
	};

	@Test
	public void testhttpclient42() throws Exception {

		BasicClientConnectionManager connectionManager = new BasicClientConnectionManager() {

			@Override
			protected ClientConnectionOperator createConnectionOperator(SchemeRegistry schreg) {
				return new DefaultClientConnectionOperator(schreg, dnsResolver);
			}
		};

		DefaultHttpClient httpclient = new DefaultHttpClient(connectionManager);

		SSLContext sslContext;

		sslContext = SSLContext.getInstance("SSL");

		// set up a TrustManager that trusts everything
		try {
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					System.out.println("testhttpclient42.getAcceptedIssuers");
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
					System.out.println("testhttpclient42.checkClientTrusted");
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
					System.out.println("testhttpclient42.checkServerTrusted");
				}
			} }, new SecureRandom());
		} catch (KeyManagementException e) {
		}
		SSLSocketFactory ssf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		ClientConnectionManager ccm = httpclient.getConnectionManager();
		SchemeRegistry sr = ccm.getSchemeRegistry();
		sr.register(new Scheme("https", 443, ssf));

		// httpClient4.2版本：
		HttpGet get = new HttpGet(uri);
		HttpResponse resp = httpclient.execute(get);

		HttpEntity entity = resp.getEntity();
		String message = EntityUtils.toString(entity, "utf-8");
		System.out.println(resp.getStatusLine());

	}
}
