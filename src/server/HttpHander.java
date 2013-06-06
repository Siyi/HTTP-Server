package server;

import java.net.ServerSocket;
import java.net.Socket;

public class HttpHander implements Runnable 
{
	private Socket clientSocket = null;
	private ServerSocket serverSocket = null;
	private TinyHttp server = null;
	
	public HttpHander(TinyHttp server, Socket client, ServerSocket serverSocket)
	{
		this.server = server;
		this.clientSocket = client;
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() 
	{
		try
		{
			HttpExchange ex = new HttpExchange(clientSocket, serverSocket);
			try
			{
				server.getAuthenticator().authenthicateRequest(ex);
				ex.makeSuccessfulResponse();
			}
			catch (AuthenticationException e)
			{
				ex.makeErrorResponse(e);
			}	
			
		}
		catch (BadRequestException e)
		{
			System.out.println("Bad Request");
		}
	}

}
