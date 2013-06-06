package server;

public abstract class Authenticator 
{
	Secur secure = Secur.getInstance();	
	public abstract void authenthicateRequest(HttpExchange ex) throws AuthenticationException;	
	public abstract String getAuthenticatorResponse();
	
	protected Secur getSecurity()
	{
		return secure;
	}
}
