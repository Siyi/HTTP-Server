package server;

public class NullAuthenticator extends Authenticator {

	@Override
	public void authenthicateRequest(HttpExchange ex)
			throws AuthenticationException {
		return;

	}

	@Override
	public String getAuthenticatorResponse() {
		
		return "";
	}

}
