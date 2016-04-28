package com.zyzx;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustmizeHttpProtocolSocketFactory extends DefaultProtocolSocketFactory {

	private static final Log LOG = LogFactory.getLog(CustmizeHttpProtocolSocketFactory.class);
	private CustomizeNameService ns = null;

	public CustmizeHttpProtocolSocketFactory(File hostfile) {
		super();
		ns = new CustomizeNameService(hostfile);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress local, int localport) throws IOException, UnknownHostException {

    	InetAddress[] ip = ns.lookupAllHostAddr(host);
    	
    	for(int i = 0;i<ip.length;i++){
    		try{
    			if(LOG.isDebugEnabled()){
    				LOG.debug("createSocket to "+ip[i].toString());
    			}
    			
    			Socket socket =	new Socket(ip[i], port, local, localport);
    			 return socket;
    		}catch(Exception e){
    			LOG.warn("createSocket to "+ip[i].toString()+" failed .try another Address");
    		}
    	}
    	
    	throw new IOException("All InetAddress is invalid : "+host);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    	InetAddress[] ip = ns.lookupAllHostAddr(host);
    	
    	for(int i = 0;i<ip.length;i++){
    		try{
    			if(LOG.isDebugEnabled()){
    				LOG.debug("createSocket to "+ip[i].toString());
    			}
    			Socket socket =	new Socket(ip[i], port);
    			 return socket;
    		}catch(Exception e){
    			LOG.warn("createSocket to "+ip[i].toString()+" failed .try another Address");
    		}
    	}
    	
    	throw new IOException("All InetAddress is invalid : "+host);
	}
}
