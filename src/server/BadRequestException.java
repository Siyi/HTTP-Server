package server;

public class BadRequestException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message = "";
	public BadRequestException(String error)
	{
		message = error;
	}
	
	public String getMessage()
	{
		return message;
	}

}
