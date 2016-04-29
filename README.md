### 自定义的DNS服务，支持假域名IP轮循调用 

让使用httpclient3和httpclient4的http调用支持假域名策略。支持host配置修改实时刷新，不需要重启进程。

使用方法:

# HttpClient3

```

	static CustomizeNameService ns = new CustomizeNameService(new File(CustomizeNameService.class.getResource("/hosts.properties").getFile()));
	
	static{
		//httpclient3 这样使用
		Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(ns), 443));
		Protocol.registerProtocol("http", new Protocol("http", new CustmizeHttpProtocolSocketFactory(ns), 80));
	}
	HttpClient httpclient = new HttpClient();
	以下像正常使用httpclient一样
```

# HttpClient4

```

	//httpclient4 这样使用
	static CustomizeNameService ns = new CustomizeNameService(new File(CustomizeNameService.class.getResource("/hosts.properties").getFile()));

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
	以下像正常使用httpclient一样
	
```