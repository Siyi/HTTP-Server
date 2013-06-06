package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class HttpClient 
{
	private static final int SERVERPORT = 8888;
	private Socket socket;
	private Scanner in;
	private PrintWriter out;
	
	public ReentrantLock lock = new ReentrantLock();

	public void init()
	{
		try
		{
			socket = new Socket( "localhost", SERVERPORT );
			System.out.println( "Socket created on the local port " +
								socket.getLocalPort() );
			System.out.println( "A connection established with the remote port " +
								socket.getPort() + " at " +
								socket.getInetAddress().toString() );

			in = new Scanner( socket.getInputStream() );
			out = new PrintWriter( socket.getOutputStream() );
			System.out.println( "I/O setup done." );
		}
		catch(IOException exception){}
	}
	
	public void sendCommand( String command )
	{
		lock.lock();
		System.out.print( "Sending " + command );
		out.print( command );
		out.flush();
		if( command.equals("QUIT") )
		{
			try
			{
				socket.close();
				System.out.println( "A connection closed." );
			}
			catch (IOException exception)
			{
				exception.printStackTrace();
				System.out.println("Exception in input");
			}
		}
		lock.unlock();
	}
	
	public String getResponse()
	{
		lock.lock();
		
		String result = "";
		while(in.hasNextLine())
			result += in.nextLine() + "\n";
		
		lock.unlock();
		return result;
	}

	public static void main(String[] args)
	{
		final HttpClient client = new HttpClient();
		client.init();
		
		DigestAuthenticator da = new DigestAuthenticator();
		
		
		String response = "", HA1 = "", snonce = "", nc = "1", client_nonce = "client", qop = "auth", HA2="";		
		HA1 = MDFive.getHex("admin:protected area:admin");
		
		HA2 = MDFive.getHex("GET:/private/index_staff.html");
		
		snonce = da.getServerNonce();
		client_nonce = MDFive.getHex(client_nonce);
		
		response = HA1 + ":" + snonce + ":" + nc + ":" + MDFive.getHex(client_nonce) + ":" + qop + ":" + HA2;
		
		response = MDFive.getHex(response);
		
		
		String Authorization = "\nAuthorization: Digest uri=/private/index_staff.html," +
						"realm=protected area, qop=auth, algorithm=MD5, username=admin, " +
						"snonce=" +snonce+", cnonce=" +client_nonce+ ", nc=" +nc+ ", " + "response=" +response +"\n";
		
		client.sendCommand( "GET /private/index_staff.html /HTTP/1.0" +Authorization);
		
		System.out.println( client.getResponse() );	
		
		
	}

}
