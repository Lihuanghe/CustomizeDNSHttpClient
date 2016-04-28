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
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.junit.Test;
public class CustomizeNameService  {

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
			return InetAddress.getAllByName(host);
		}
		return t;
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
			
			if(allip == null || host == null||"".equals(allip)) continue;
			String[] ipArr = allip.split(",");
			List<InetAddress> inetAddr = new ArrayList<InetAddress>();
			for(int i = 0;i<ipArr.length;i++){
				try{
					InetAddress ip = InetAddress.getByName(ipArr[i]);
					inetAddr.add(ip);
				}catch(Exception e){
					
				}
			}
			if(inetAddr.size()>0){
				map.put(host.trim(), inetAddr.toArray(new InetAddress[0]));
			}
		}
	}
	@Test
	public void test() throws Exception{
		
		watchconfigFile(new File("D:\\logs\\a.txt"));
		LockSupport.park();
	}
}
