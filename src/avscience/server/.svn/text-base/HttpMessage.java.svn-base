package avscience.server;

import java.net.*;
import java.util.*;
import java.io.*;

public class HttpMessage
{
    private URL servlet;
    private String args;
    
    public HttpMessage(URL servlet)
    {
        this.servlet = servlet;
    }
    
    public InputStream sendGetMessage(Properties args) throws IOException
    {
        String argString = "";
        
        if (args != null)
        {
            argString = "?" + toEncodedString(args);
        }
        
        URL url = new URL(servlet.toExternalForm() + argString);
        URLConnection con = url.openConnection();
        con.setUseCaches(false);
        return con.getInputStream();
    }
    
    public OutputStream sendRecieveMessage(Properties args) throws IOException
    {
        String argString = "";
        
        if (args != null)
        {
            argString = "?" + toEncodedString(args);
        }
        
        URL url = new URL(servlet.toExternalForm() + argString);
        URLConnection con = url.openConnection();
        con.setUseCaches(false);
        return con.getOutputStream();
    }
    
    
    private String toEncodedString(Properties args)
    {
        StringBuffer buf = new StringBuffer();
        Enumeration names = args.propertyNames();
        
        while (names.hasMoreElements())
        {
            String name = (String) names.nextElement();
            String value = args.getProperty(name);
            buf.append(URLEncoder.encode(name) + "=" + URLEncoder.encode(value));
            if (names.hasMoreElements()) buf.append("&");
        }
        
        return buf.toString();
    
    }
    
}
