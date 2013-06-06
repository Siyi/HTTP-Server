package server;

import it.sauronsoftware.base64.Base64;

public class BasicAuthenticator extends Authenticator
{
	@Override
	public void authenthicateRequest(HttpExchange ex) throws AuthenticationException
	{
		if (isAuthenticationRequired(ex.getRequestURIPath()) &&
				!getSecurity().isAuthenticationRequired(ex.getRequestURIPath()).
					equals(getAuthenticationCredentials(ex.getRequestHeader("Authorization"))))		
			throw new AuthenticationException(getAuthenticatorResponse());
	}
	
	@Override
	public String getAuthenticatorResponse()
	{		
		return "Basic realm='protected area'";
	}
	
	private boolean isAuthenticationRequired(String path)
	{
		System.out.println("Authentication: " +getSecurity().isAuthenticationRequired(path));
		return getSecurity().isAuthenticationRequired(path) !=null;
	}
	
	protected String getAuthenticationCredentials(String credentials )
	{
		System.out.println("Raw Credentials: " +credentials);
		String result = null;
		int start = 0;
		
		start = credentials.indexOf("Authorization : Basic");
		if (start > -1)
		{	
			result = credentials.substring(start + 21).trim();	
			result = new String(Base64.decode(result));
		}
		
		System.out.println("Credentials: " +result);
		return result;
	}
}
