package server;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DigestAuthenticator extends Authenticator 
{	
	HttpExchange exchange = null;
	ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	final String HEX = "0123456789abcdef";
	private String serverNonce = "";
	
	public DigestAuthenticator()
	{
		setServerNonce();
	}

	@Override
	public void authenthicateRequest(HttpExchange ex) throws AuthenticationException
	{
		exchange = ex;
		if (isAuthenticationRequired(ex.getRequestURIPath()) &&
				!isClientResponseValid(ex.getRequestHeader("Authorization")))		
			throw new AuthenticationException(getAuthenticatorResponse());
		
		exchange = null;
	}
	
	@Override
	public String getAuthenticatorResponse()
	{		
		return "Digest realm=\"protected area\" nonce=\"" +getServerNonce()+ "\" qop=\"auth\" algorithm=MD5";
	}
	
	
	private boolean isAuthenticationRequired(String path)
	{
		System.out.println("Required: " +getSecurity().isAuthenticationRequired(path));
		return getSecurity().isAuthenticationRequired(path) !=null;
	}
	
	private boolean isClientResponseValid(String authFromClient)
	{		
		String HA1 = getHex("admin:protected area:admin");
		String HA2 = getHex(exchange.getRequestCommand() + ":" +exchange.getRequestURI());
		String nc = "", qop = "", cnonce = "";
		String serverResponse = "";
		String[] itemArray, item;
		
		HashMap<String, String> digestItems = new HashMap<String, String>();
		
		int start = authFromClient.indexOf("Authorization : Digest ");
		if (start > -1)
		{
			System.out.println("Found Digest");
			
			authFromClient = authFromClient.substring(22);
			itemArray = authFromClient.split(",");
			for (String s : itemArray)
	        {
				item = s.trim().split("=");
				digestItems.put(item[0].trim(), item[1].trim());
	        }
			
			qop = digestItems.get("qop");
			nc = digestItems.get("nc");
			cnonce = digestItems.get("cnonce");
			
			serverResponse = HA1 + ":" + getServerNonce() + ":" +
					nc + ":" + MDFive.getHex(cnonce) + ":" +
					qop + ":" + HA2;
			serverResponse = getHex(serverResponse);
		}
		
		return digestItems.get("response").equals(serverResponse);
	}
	protected String getAuthenticationCredentials(String credentials )
	{
		String result = null;
		int start = 0;
		
		
		start = credentials.indexOf("Authorization: Digest ");
		if (start > -1)
		{	
			result = credentials.substring(start + 22).trim();	
			//result = result));
		}
		
		System.out.println("Digest Credentials: " +result);
		return result;
	}
	
	public void setServerNonce()
	{
		lock.writeLock().lock();
		
		serverNonce = getHex(Constants.getServerNonceIn());
		
		lock.writeLock().unlock();
	}
	
	public String getServerNonce()
	{
		lock.readLock().lock();
		try
		{
			return serverNonce;
		}
		finally
		{
			lock.readLock().unlock();
		}	
	}
	
	String getHex(String str)
    {
    	String result = "";
    	try
    	{
    		MessageDigest md = MessageDigest.getInstance("MD5");
    		result = getHex(md.digest(str.getBytes()));
    	}
    	catch (NoSuchAlgorithmException e) 
    	{
			e.printStackTrace();
		}
    	
    	return result;
    }
	
	
	String getHex(byte[] md5binaryDigest) 
    {
        StringBuffer sb = new StringBuffer();
        for (byte b : md5binaryDigest)
        {
            sb.append(HEX.charAt((b >>> 4) & 0x0f));
            sb.append(HEX.charAt(b & 0x0f));
        }  
        
        return sb.toString();
    }
	
	
	

}
