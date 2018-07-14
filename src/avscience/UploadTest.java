import java.io.*;
import java.net.*;
import java.util.*;
import avscience.ppc.*;
import org.jdom.*;

public class UploadTest 
{
	final static int dataPushSize=8000;
	static final String dataUrl = "http://kahrlconsulting.com:8084/avscience/PitServlet";
	
	public static void main(String[] args)
	{
		new UploadTest().bounceObjectFromServer();
	}
	
    public UploadTest() 
    {
        bounceObjectFromServer();
    }
    /// sends an object to the server, and gets one back depending on params. Connects https. return object, and params can be null. 
	private Object bounceObjectFromServer()
	{
		System.out.println("bounceObjectToServer()");
		Object result=null;
		try
		{
			StringBuffer encUrl = new StringBuffer(dataUrl);
			encUrl.append("?TYPE=XMLPIT_SEND");
			encUrl.append("");
			
			System.out.println("Url: "+encUrl.toString());
			URL servletUrl = new URL(encUrl.toString());
			HttpURLConnection servletConnection = (HttpURLConnection)servletUrl.openConnection();
		   
			servletConnection.setChunkedStreamingMode(dataPushSize);
			servletConnection.setRequestProperty("Content-type","text/xml");

			servletConnection.setRequestMethod("POST");
			servletConnection.setDoOutput(true);
                        servletConnection.setDoInput(true);	
			servletConnection.setUseCaches(false);
			servletConnection.connect();
		
			File file = new File("test.xml");
			FileReader reader = new FileReader(file);
			
			char[] chars = new char[(int)file.length()];
			
			reader.read(chars, 0, chars.length);
			
			OutputStreamWriter oos = new OutputStreamWriter(servletConnection.getOutputStream());
			oos.write(chars, 0, chars.length);
			oos.flush(); 
			oos.close();
		
			System.out.println("READING OBJECT RESPONSE");
			ObjectInputStream ois = new ObjectInputStream(servletConnection.getInputStream());
			if ( ois!=null )result = ois.readObject();
		}
		catch(Exception e){System.out.println(e.toString());}
		System.out.println("result: "+result);
		return result;
	}
}