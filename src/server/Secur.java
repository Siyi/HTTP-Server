package server;

import java.util.HashMap;
import java.util.Iterator;

public class Secur 
{
	private static Secur instance = null;	
	
	
	HashMap<String, String> realms = new HashMap<String, String>();
	
	private Secur()
	{
		realms.put("\\src\\server\\private\\", "admin:admin");
	}
	
	public static Secur getInstance()
	{
		if (instance == null)
			instance = new Secur();	
		return instance;
	}
	
	public String isAuthenticationRequired(String path)
	{
		Iterator<String> it = realms.keySet().iterator();
		String key = null;
		while(it.hasNext())
		{
			key = it.next();
			if (path.contains(key))
			{				
				return realms.get(key);
			}
		}			
		return null;
	}

}
