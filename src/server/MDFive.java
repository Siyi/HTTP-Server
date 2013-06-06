package server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MDFive 
{
static final String HEX = "0123456789abcdef";
    
	public static void main(String[] args) 
	{
		String s ="abc";
		MessageDigest md = null;
		byte[] nonce = null;

		try 
		{
			md = MessageDigest.getInstance("MD5");
			nonce = md.digest(s.getBytes()); // 128-bit digest (8 bits * 16)
			/*
			int sh7 = 1 << 7;
			for(byte b : nonce){
				for(int k=0; k < 8; k++){
					System.out.print( ((b << k) & sh7) >>> 7 );
				}
				System.out.print(" ");
			}
			System.out.println("");*/
			
			System.out.println( getHex(nonce) );

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public static String getHex(String str)
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
	
    public static String getHex(byte[] md5binaryDigest) 
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
