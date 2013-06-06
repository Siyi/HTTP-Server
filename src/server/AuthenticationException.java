package server;

public class AuthenticationException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message = "";
	public AuthenticationException(String error)
	{
		//System.out.println("Authentication Failed - Send request for more");
		message = error;
	}
	
	public String getMessage()
	{
		return message;
	}

}
