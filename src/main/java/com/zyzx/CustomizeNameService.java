package com.zyzx;



import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

import sun.net.util.IPAddressUtil;
public class CustomizeNameService  {
	private static final Log LOG = LogFactory.getLog(CustomizeNameService.class);
	private Map<String,InetAddress[]> map = new ConcurrentHashMap<String,InetAddress[]>();
	
	public CustomizeNameService(File srcfile )  {
		try {
			Properties t = new Properties();
			t.load(new FileInputStream(srcfile));
			loadhost(t);
			watchconfigFile(srcfile);
		} catch (Exception e) {
			
		}
	}
	public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
		if(host==null ||"".equals(host)) return null;
		InetAddress[] t = map.get(host);
		if(t== null || t.length ==0){
			byte[] arrayOfByte = IPAddressUtil.textToNumericFormatV4(host);
			if (arrayOfByte == null) {
				arrayOfByte = IPAddressUtil.textToNumericFormatV6(host);
			}
			if(arrayOfByte != null){
				//如果host本身就是个ip地址，设置InetAddress的hostName为空字符串，不可以设置成null,否则会
				//在jdk8以前，使用SSL会有问题 :http://mail-archives.apache.org/mod_mbox/jmeter-dev/201408.mbox/%3CCAH9fUpaW35vJXPAdRHpb7EHP_B=3Aeo-N+EGM01pqLuJTqzWmQ@mail.gmail.com%3E
				//当host为null时，SSL的Handshaker在调用getHostSE()时，在window上会调用InetAddress.getHostName()进行域名反解析，造成程序挂起10s钟。
				//
				return new  InetAddress[]{InetAddress.getByAddress(host, arrayOfByte)};
			}else{
				return InetAddress.getAllByName(host);
			}
			
		}
		return t;
	}
	
	//把第index的地址移到第一个位置
	public boolean moveToFirstInetAddress(String host,int index,InetAddress now){
		if(index ==0 || host==null ||"".equals(host)) return true;
		InetAddress[] t = map.get(host);
		
		if(index >= t.length) return true;
		
		synchronized (t) {
			//如果当前的地址now已经在第一个了，说明有其它线程已交换过位置了，不能再交换
			if(t[0]==now) return true;
			
			LOG.warn("IpAddress " +host +now + " is valid,but not the first element ,move it to first .");
			InetAddress tmp = t[0];
			t[0] = t[index];
			t[index] = tmp;
		}
		return true;
	}

	private void watchconfigFile(File srcfile) throws Exception{
		if(srcfile == null || "".equals(srcfile)) return;
		FileSystemManager manager = VFS.getManager();
		FileObject file= manager.resolveFile(srcfile.toURI().toString());

		DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener(){

			public void fileChanged(FileChangeEvent paramFileChangeEvent) throws Exception {
				FileObject file = paramFileChangeEvent.getFile();
				FileContent content = file.getContent();
				
				Properties t = new Properties();
				t.load(content.getInputStream());
				loadhost(t);
			}

			public void fileCreated(FileChangeEvent paramFileChangeEvent) throws Exception {
				FileObject file = paramFileChangeEvent.getFile();
				FileContent content = file.getContent();
				
				Properties t = new Properties();
				t.load(content.getInputStream());
				loadhost(t);
				
			}

			public void fileDeleted(FileChangeEvent paramFileChangeEvent) throws Exception {
				FileObject file = paramFileChangeEvent.getFile();
				
				map.clear();
			}
			
		});
		fm.setDelay(5000);
		fm.addFile(file); 
		fm.start();
	}
	
	private void loadhost(Properties prop){
		Map.Entry<Object,Object> item = null;
		if(prop == null) return;
		
		for(Iterator<Map.Entry<Object,Object>> itor = prop.entrySet().iterator();itor.hasNext();){
			item = itor.next();
			String host = (String)item.getKey();
			String allip = (String)item.getValue();
			LOG.info(host +"="+ allip);
			if(allip == null || host == null||"".equals(allip)) continue;
			String[] ipArr = allip.split("[ ,\\s]");
			List<InetAddress> inetAddr = new ArrayList<InetAddress>();
			for(int i = 0;i<ipArr.length;i++){
				try{
					if(ipArr[i] == null || "".equals(ipArr[i].trim())) continue;
					
					byte[] arrayOfByte = IPAddressUtil.textToNumericFormatV4(ipArr[i]);
					if (arrayOfByte == null) {
						arrayOfByte = IPAddressUtil.textToNumericFormatV6(ipArr[i]);
					}
					InetAddress ip = InetAddress.getByAddress(host, arrayOfByte);
					inetAddr.add(ip);
				}catch(Exception e){
					LOG.warn("not a IpAddress :" + ipArr[i]);
				}
			}
			if(inetAddr.size()>0){
				map.put(host.trim(), inetAddr.toArray(new InetAddress[0]));
			}
		}
	}

}
