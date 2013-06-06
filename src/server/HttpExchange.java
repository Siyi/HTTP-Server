package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class HttpExchange 
{	
	private ServerSocket server = null;
	private Socket client = null;
	private String command = "";
	private String protocolName = "";
	private String protocolVersion = "";
	private String target = "";	
	private String body = "";	
	private static PrintStream out = null;
	private HashMap<String, String> requestHeaderMap = new HashMap<String, String>();
	private HashMap<String, String> responseHeaderMap = new HashMap<String, String>();	
	
	public HttpExchange(Socket client, ServerSocket server) throws BadRequestException 
	{
		setServer(server);
		setClient(client);
		getRequestInformation();
	}	
	
	private void getRequestInformation() throws BadRequestException
	{
		String commandLine = "", options = "";	
		
		try
		{
			try 
			{					
				BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream() ) );  
				out = new PrintStream( client.getOutputStream() );  
				System.out.println( "I/O setup done" );					
				
				String line = "";
				
				commandLine = in.readLine();
				System.out.println(commandLine);				
				
				while ( in.ready() && line != null ) 
				{   
					line = in.readLine();						
					options += "\n" + line;
					System.out.println( "a " +line);
				}
				System.out.println(line);
				
				setCommandInfo(commandLine);
				setRequestInfo(options);
								
			}
			finally{}
			
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
		
		
	}
		
	private void setCommandInfo(String commandLine) throws BadRequestException
	{
		try
		{
			String[] params = commandLine.split("\\s+");
			String[] protocol;
			
			setRequestCommand(params[0]);
			setRequestURI(params[1]);  
			
			
			protocol = params[2].split("/");
			
			System.out.println(Arrays.toString(protocol));
			
			setProtocolName(protocol[0]);
			setProtocolVersion(protocol[1]);
			
	}
		catch(Exception e)
		{
			throw new BadRequestException("Poorly formed url");			
		}
	}

	private void setRequestInfo(String options)
	{
		String[] optionKvps = options.split("\n");
		String[] kvp;
		for(int i = 0; i < optionKvps.length; i++)
		{
			if (optionKvps[i].isEmpty()); 
			else if (optionKvps[i].contains(":")) 
			{
				kvp = optionKvps[i].split(":");
				requestHeaderMap.put(kvp[0].trim(), kvp[1].trim());
			}
			else 
				setRequestBody(optionKvps[i]);
		}
	}
	
	public void setServer(ServerSocket server) {
		this.server = server;
	}

	public ServerSocket getServer() {
		return server;
	}

	public void setClient(Socket client) {
		this.client = client;
	}

	public Socket getClient() {
		return client;
	}
	
	public void setProtocolVersion(String version) {
		this.protocolVersion = version;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public void setRequestURI(String target) {
		this.target = target;
	}

	public String getRequestURI() {
		return target;
	}
	
	public String getRequestURIPath()
	{
		
		String result ="";
		result = Constants.getRootPath() + (getRequestURI().substring(1).trim().isEmpty() ? 
						"index.html" : getRequestURI().substring(1)).replace("/", "\\");
		return result;
	}
	
	public String getRequestHeader(String key)
	{
		return key + " : " + requestHeaderMap.get(key);
	}
	
	public String getRequestHeaders()
	{
		String result = "";
		Iterator<String> it = requestHeaderMap.keySet().iterator();
		while (it.hasNext())
			result += getRequestHeader(it.next()) + "\n";
		
		return result;			
	}
	
	@Override 
	public String toString()
	{
		String result = "";
		
		
		result += getRequestCommand() + " " +getRequestURI()+ "<" +getRequestURIPath()+ "> /" +
							getProtocolName()+ "/" +getProtocolVersion() + "\n";		
		
		result += getRequestHeaders();
		
			
		return result;
	}

	public void setRequestCommand(String command) {
		this.command = command;
	}

	public String getRequestCommand() {
		return command;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setRequestBody(String body) {
		this.body = body;
	}

	public String getRequestBody() {
		return body;
	}
	
	private void setResponseHeader(String key, String value)
	{
		responseHeaderMap.put(key, value);
	}
	
	private String getResponseheader(String key)
	{
		return key + " : " + responseHeaderMap.get(key);
	}
	
	private String getResponseHeaders()
	{
		String result = "";
		Iterator<String> it = responseHeaderMap.keySet().iterator();
		while (it.hasNext())
			result += getResponseheader(it.next()) + "\n";
		
		return result;
	}
	
	public void makeSuccessfulResponse()
	{	
		if ("GET".equals(getRequestCommand().toUpperCase()))		
			makeGETResponse();
		else if ("HEAD".equals(getRequestCommand().toUpperCase()))
			makeHEADERResponse();
		else if ("PUT".equals(getRequestCommand().toUpperCase()))
			makePUTResponse();
		else if ("POST".equals(getRequestCommand().toUpperCase()))
			makePOSTResponse();
		else if ("DELETE".equals(getRequestCommand().toUpperCase()))
			makeDELETEResponse();		
	}
	
	public void makeErrorResponse(AuthenticationException e)
	{
		out.println("HTTP/1.0 401 Unauthorized");
		out.println("WWW-authenticate: " + e.getMessage());
		
		out.println("<html><body>Authorization Required</body></html>");
		out.flush();
		out.close();
		System.out.println("HTTP/1.0 401 Unauthorized");
	}
	
	public static void makeErrorResponse(BadRequestException e)
	{
		out.println("HTTP/1.0 400 Bad Request");		
		out.flush();
		out.close();
		
		System.out.println("File not found");
	}
	
	private void makeGETResponse()
	{
		System.out.println("Get Make response");
		try
		{
			System.out.println("File requested: " + getRequestURIPath());
			
			File file = new File(getRequestURIPath());
			int len = (int) file.length();
			if (!file.exists())
				throw new FileNotFoundException("File not found");
			
			DataInputStream fin = new DataInputStream(new FileInputStream(file));
			byte buf[] = new byte[len];
			fin.readFully(buf);
			
			out.println("HTTP/1.0 200 OK");
			out.println("Content-Type: text/html");			
			out.println("Content-Length: " + len);			
			
			out.println("");  			
					
			out.println(TinyHttp.getCache().fetch(getRequestURIPath()));			
			out.flush();
			fin.close();		
			
			System.out.println("Sent Response");
		}
		catch (FileNotFoundException e)
		{
			out.println("HTTP/1.0 404 Not Found");
			out.flush();
			System.out.println("File not found");
		}
		catch (IOException e)
		{
			System.out.println("IO Exception");
			e.printStackTrace();
		}		
		finally
		{
			out.close();
		}
	}
	
	private void makePUTResponse()
	{
		System.out.println("Put Make response");
		try
		{
			boolean success = false;
			File file = new File(getRequestURIPath());
			
			success = file.createNewFile();	
			
			FileWriter fw = new FileWriter(file);
			fw.write(getRequestBody(), 0, getRequestBody().length());
			fw.close();
			
			if (success) 
				out.println("HTTP/1.0 201 Created");
			else 
				out.println("HTTP/1.0 204 No Content");			
			
			out.println("");  	
			out.flush();
						
		}		
		catch(Exception e)
		{
			out.println("HTTP/1.0 501 Not Implemented");			
			out.flush();
			System.out.println("Other file qualm");
		}
		finally
		{
			out.close();
		}
	}
	
	private void makePOSTResponse()
	{
		System.out.println("Post Make response");
		try
		{
			File file = new File(getRequestURIPath());
			if (!file.exists())
				throw new FileNotFoundException("File not found");
			
			out.println("HTTP/1.0 200 OK");
			out.println("Content-Type: text/html");	
			out.println("Content-Length: " + getRequestBody().length());			
			out.println("");
			
			out.println(getRequestBody());
			out.flush();
						
		}
		catch(FileNotFoundException e )
		{
			out.println("HTTP/1.0 404 Not Found");			
			out.flush();
			System.out.println("File not found");
		}
		catch(Exception e)
		{
			out.println("HTTP/1.0 501 Not Implemented");			
			out.flush();
			System.out.println("Other file qualm");
		}
		finally
		{
			out.close();
		}
	}
	
	private void makeDELETEResponse()
	{
		System.out.println("Delete Make response");
		try
		{
			boolean success = false;
			File file = new File(getRequestURIPath());
			success = file.delete();
			if (success)
			{
				out.println("HTTP/1.0 204 No Content");				
			}
			else 
				throw new Exception("File could not be deleted");
			out.println("");  	
			out.flush();						
		}		
		catch(Exception e)
		{
			out.println("HTTP/1.0 501 Not Implemented");			
			out.println("");
			out.flush();
			System.out.println("File could not be deleted");
		}
		finally
		{
			out.close();
		}
	}
	
	private void makeHEADERResponse()
	{
		System.out.println("Header Make response");
		try
		{
			File file = new File(getRequestURIPath());
			if (!file.exists())
			{
				out.println("HTTP/1.0 404 Not Found\n");				
				out.flush();
				
				System.out.println("File not found");
			
			}
			else
			{
				int len = (int) file.length();
				
				out.println("HTTP/1.0 200 OK");
				out.println("Date: " + new Date());
				out.println("Server: " + server.getInetAddress().toString() );
				out.println("Content-Type: text/html");
				out.println("Last Modified: " + new Date(file.lastModified()));		
				out.println("Content-Length: " + len);
				
				out.println("");				
				
				out.flush();
			}
		}		
		finally
		{
			out.close();
		}
	}
}
