package server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;



public class LFU implements FileCache{
	int maxsize=5;
	Double count;
	private ReentrantLock lock=new ReentrantLock();
	HashMap<String,String> hashmap=new HashMap<String,String>();
	HashMap<String,Double> hashmap_policy=new HashMap<String,Double>();
	public String fetch(String targetFile){
		   lock.lock();
		   try{
	     if(hashmap.containsValue(targetFile)){
		      count=hashmap_policy.get(targetFile);
		      count++;
		      hashmap_policy.put(targetFile, count);
		     
		      return hashmap.get(targetFile);
	         }
	     
	     else
			  return cacheFile(targetFile);
		   }
		   finally{
			   lock.unlock();
		   }
	    
	   }
	private String cacheFile(String targetFile){
		File file=new File(targetFile);
		int len=(int)file.length();
		DataInputStream fin = null;
		try {
			fin = new DataInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		
		byte buf[]=new byte[len];
		try {
			fin.readFully(buf);
			fin.close();
		} catch (IOException e) {		
			e.printStackTrace();
		}
		String content=new String(buf);
		
		if(hashmap.size()>maxsize){
			ValueComparator bvc =  new ValueComparator(hashmap_policy);
	        TreeMap<String,Double> sorted_map = new TreeMap(bvc);
	        sorted_map.putAll(hashmap_policy);
	       
	       String key=sorted_map.firstEntry().getKey();	
	       hashmap.remove(key);  
	       hashmap_policy.remove(key);
	      }
		hashmap.put(targetFile, content);	
		hashmap_policy.put(targetFile, (double) 0);
		return content;
		}
	
	
}

