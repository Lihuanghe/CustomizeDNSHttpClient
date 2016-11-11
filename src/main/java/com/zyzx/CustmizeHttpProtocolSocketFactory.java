package com.zyzx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustmizeHttpProtocolSocketFactory extends DefaultProtocolSocketFactory {

	private static final Log LOG = LogFactory.getLog(CustmizeHttpProtocolSocketFactory.class);
	private CustomizeNameService ns = null;

	public CustmizeHttpProtocolSocketFactory(CustomizeNameService ns) {
		super();
		this.ns = ns;
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress local, int localport) throws IOException, UnknownHostException {

		InetAddress[] ip = ns.lookupAllHostAddr(host);

		for (int i = 0; i < ip.length; i++) {
			try {
				if (LOG.isDebugEnabled()) {
					LOG.debug("createSocket to " + ip[i].toString());
				}

				Socket socket = new Socket(ip[i], port, local, localport);
				ns.moveToFirstInetAddress(host, i, ip[i]);
				return socket;
			} catch (Exception e) {
				LOG.warn("createSocket to " + ip[i].toString() + " failed .try another Address");
			}
		}

		throw new IOException("All InetAddress is invalid : " + host);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		InetAddress[] ip = ns.lookupAllHostAddr(host);

		for (int i = 0; i < ip.length; i++) {
			try {
				if (LOG.isDebugEnabled()) {
					LOG.debug("createSocket to " + ip[i].toString());
				}
				Socket socket = new Socket(ip[i], port);
				ns.moveToFirstInetAddress(host, i, ip[i]);
				return socket;
			} catch (Exception e) {
				LOG.warn("createSocket to " + ip[i].toString() + " failed .try another Address");
			}
		}

		throw new IOException("All InetAddress is invalid : " + host);
	}

	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException,
			UnknownHostException, ConnectTimeoutException {
		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null");
		}
		int timeout = params.getConnectionTimeout();
		if (timeout == 0) {
			InetAddress[] ip = ns.lookupAllHostAddr(host);
			for (int i = 0; i < ip.length; i++) {
				try {
					if (LOG.isDebugEnabled()) {
						LOG.debug("createSocket to " + ip[i].toString());
					}

					Socket socket = new Socket(ip[i], port, localAddress, localPort);
					ns.moveToFirstInetAddress(host, i, ip[i]);
					return socket;
				} catch (Exception e) {
					LOG.warn("createSocket to " + ip[i].toString() + " failed .try another Address");
				}
			}

			throw new IOException("All InetAddress is invalid : " + host);
		} else {
			InetAddress[] ip = ns.lookupAllHostAddr(host);

			for (int i = 0; i < ip.length; i++) {
				try {
					if (LOG.isDebugEnabled()) {
						LOG.debug("createSocket to " + ip[i].toString());
					}
					Socket socket = new Socket();
					SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
					SocketAddress remoteaddr = new InetSocketAddress(ip[i], port);
					socket.bind(localaddr);
					socket.connect(remoteaddr, timeout);
					ns.moveToFirstInetAddress(host, i, ip[i]);
					return socket;
				} catch (Exception e) {
					LOG.warn("createSocket to " + ip[i].toString() + " failed .try another Address");
				}
			}
			throw new IOException("All InetAddress is invalid : " + host);
		}
	}
}
