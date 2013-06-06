package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class TinyHttp 
{
	private static final int PORT = 8888;
	private ServerSocket serverSocket;
	private StaticThreadPool pool = StaticThreadPool.getInstance(6, false);
	private static FileCache cache = new LRU();
	
	private Authenticator authenticator = null;
	

	public void init() 
	{
		try 
		{
			try 
			{
				serverSocket = new ServerSocket(PORT);
				pool = StaticThreadPool.getInstance(6, true);
											
				System.out.println("Socket created.");
			
				while(true) 
				{	
					System.out.println( "Listening to a connection on the local port " +
										serverSocket.getLocalPort() + "..." );
					Socket client = serverSocket.accept();
					client.setSoTimeout(Constants.getTimeout());
					System.out.println( "\nA connection established with the remote port " + 
										client.getPort() + " at " +
										client.getInetAddress().toString() );
					
					pool.execute(new HttpHander(this, client, serverSocket));
				}
			} 			
			finally 
			{
				serverSocket.close();
			}
		}
		catch(IOException exception){
			exception.printStackTrace();
		}
	}
	
	public String getServerInfo()
	{
		return serverSocket.getInetAddress().toString();
	}
	
	
	public static void main(String[] args) 
	{
		TinyHttp server = new TinyHttp();
		if (args.length > 0)
			server.setAuthenticator(args[0]);
		else
			server.setAuthenticator();
		server.init();
		
	}
	
	public void setAuthenticator(String var)
	{
		if ("-b".equals(var))
		{
			this.authenticator = new BasicAuthenticator();
		}
		else if ("-d".equals(var))
		{
			this.authenticator = new DigestAuthenticator();
		}
		else if ("-n".equals(var))
		{
			this.authenticator = new NullAuthenticator();
		}
	}
	public static FileCache getCache() {
		return cache;
	}
	
	public void setAuthenticator() {
		this.authenticator = new NullAuthenticator();;
	}
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}
	

	public Authenticator getAuthenticator() {
		return authenticator;
	}

	
}


