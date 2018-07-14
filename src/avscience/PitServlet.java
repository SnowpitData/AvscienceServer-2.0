import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import avscience.server.*;
import org.jdom.*;
import org.json.*;
import org.jdom.output.*;
import org.jdom.input.*;
import avscience.ppc.*;
import javax.imageio.ImageIO;
import java.awt.image.*;
import avscience.pc.PitFrame;
import java.security.MessageDigest;

public class PitServlet extends HttpServlet
{
     DAO dao = new DAO();
     
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException 
     {
        System.out.println("PitServlet");
        String type = request.getParameter("TYPE");
        if ( type!= null ) type = type.trim();
        System.out.println("TYPE " + type);
        
        if ( type==null) return;
       
        if (type.equals("GET_NEWS"))
        {
        	try
        	{
                    ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
                    out.writeObject(dao.getNews());
                    out.flush();
                    out.close();
        	}
        	catch(Exception e){System.out.println(e.toString());}
        }
        
        
        if ( type.equals("JSONOCC") )
        {
            try
            {
            	String serial = (String) request.getParameter("SERIAL");
            	if (( serial!=null) && (serial.trim().length()>0))
                {
	                System.out.println("Getting occ:: # " + serial);
	                String data = dao.getPPCOcc(serial);
	                if (data == null)
	                {
	                	System.out.println("occ null.");
	                	data="";
	                }
	                
	                if ( data.trim().length()<3) System.out.println("No data for occ # "+serial);
	                
	                ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
	                out.writeObject(data);
	                out.flush();
	                out.close();
	            }
            }
            catch(Exception e){System.out.println(e.toString());}
        }
        
         if ( type.equals("JSONPIT") )
        {
            try
            {
            	String serial = (String) request.getParameter("SERIAL");
            	if (( serial!=null) && (serial.trim().length()>0))
                {
	                System.out.println("Getting PIT:: # " + serial);
	                String data = dao.getPPCPit(serial);
	                if (data == null)
	                {
	                	System.out.println("occ null.");
	                	data="";
	                }
	                
	                if ( data.trim().length()<3) System.out.println("No data for occ # "+serial);
	                
	                ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
	                out.writeObject(data);
	                out.flush();
	                out.close();
	            }
            }
            catch(Exception e){System.out.println(e.toString());}
        }
        
        if ( type.equals("OCCCROWNOBS"))
        {
            try
            {
                String ser = (String) request.getParameter("SERIAL");
                if (( ser!=null) && (ser.trim().length()>0))
                {
	                System.out.println("Getting pit LOCAL SERIAL:: # " + ser);
	                String data = dao.getPitByLocalSerial(ser);
	                if (data == null)
	                {
	                	System.out.println("pit null.");
	                	data="";
	                }
	                
	                if ( data.trim().length()<3) System.out.println("No data for pit # "+ser);
	                
	                ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
	                out.writeObject(data);
	                out.flush();
	                out.close();
	            }
            }
            catch(Exception e){System.out.println(e.toString());}
        }
        
        if ( type.equals("JSONPITLIST_FROMQUERY"))
        {
            String query = (String) request.getParameter("QUERY");
            String[][] table = dao.getPitListArrayFromQuery(query, false);
            JSONArray ja = new JSONArray();
            int size = table[0].length;
            for ( int i = 0; i<size; i++ )
            {
                String serial = table[1][i];
        	String data = dao.getPPCPit(serial); 
                avscience.ppc.PitObs pit = null;
                try
                {
                    pit = new avscience.ppc.PitObs(data);
                }
                catch(Exception ex)
                {
                    System.out.println(ex.toString());
                    continue;
                }
                ja.put(pit);
            }
            
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
	        out.writeObject(ja.toString());
	        out.flush();
	        out.close();
            }
            catch(Exception e)
            {
                System.out.println(e.toString());
            }
            
        }
        
        if ( type.equals("JSONPITLIST"))
        {
            JSONArray ja = new JSONArray();
            String[][] table = dao.getPitListArray(true);
            int size = table[0].length;
            for ( int i = 0; i<size; i++ )
            {
                String serial = table[1][i];
        	String data = dao.getPPCPit(serial); 
                avscience.ppc.PitObs pit = null;
                try
                {
                    pit = new avscience.ppc.PitObs(data);
                }
                catch(Exception ex)
                {
                    System.out.println(ex.toString());
                    continue;
                }
                ja.put(pit);
            }
            
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
	        //out.writeObject(ja.toString());
                out.writeObject(table);
	        out.flush();
	        out.close();
            }
            catch(Exception e)
            {
                System.out.println(e.toString());
            }
            
        }
        
        if ( type.equals("XMLPITLIST"))
        {
        	Element e = new Element("Pit_List");
        	String[][] table = dao.getPitListArray(true);
        	int size = table[0].length;
        ///	if (size>24) size=24;
        	for ( int i = 0; i<size; i++ )
        	{
        		String serial = table[1][i];
        		String data = dao.getPPCPit(serial);
                        avscience.ppc.PitObs pit = null;
                        try
                        {
                            pit = new avscience.ppc.PitObs(data);
                        }
                        catch(Exception ex)
                        {
                            System.out.println(ex.toString());
                            continue;
                        }
        		Element ep = new Element("pit");
        		Element number = new Element("number");
        		number.setText(i+"");
        		ep.addContent(number);
        		Element id = new Element("id");
        		id.setText(serial);
        		ep.addContent(id);
        		Element user = new Element("User");
        		//avscience.ppc.User u = pit.getUser();
        		///if (u==null)u = new avscience.ppc.User();
        		//user.setText(pit.getUserHash());
        		//ep.addContent(user);
        		Element loc = new Element("location");
        		loc.setText(pit.getLocation().getName());
        		ep.addContent(loc);
        		e.addContent(ep);
        	}
        	Document doc = new Document(e);
        	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		try
		{
                    outputter.output(doc, response.getOutputStream());
		}
		catch(Exception ex)
		{
                    System.out.println(ex.toString());
		}
	
        }
        //////////////////////////
        if ( type.equals("XMLPITLIST_FROMQUERY"))
        {
        	String query = (String) request.getParameter("QUERY");
        	Element e = new Element("Pit_List");
        	String[][] table = dao.getPitListArrayFromQuery(query, false);
        	int size = table[0].length;
        	for ( int i = 0; i<size; i++ )
        	{
        		String serial = table[1][i];
        		String data = dao.getPPCPit(serial);
                        avscience.ppc.PitObs pit = null;
                        try
                        {
                            pit = new avscience.ppc.PitObs(data);
                        }
                        catch(Exception ex)
                        {
                            System.out.println(ex.toString());
                            continue;
                        }
        		Element ep = new Element("pit");
        		Element number = new Element("number");
        		number.setText(i+"");
        		ep.addContent(number);
        		Element id = new Element("id");
        		id.setText(serial);
        		ep.addContent(id);
        		Element user = new Element("User");
        		if ( pit!=null)
        		{
                            user.setText(pit.getUserHash());
                            ep.addContent(user);
                            Element loc = new Element("location");
                            loc.setText(pit.getLocation().getName());
                            ep.addContent(loc);
                            e.addContent(ep);
        			
        		}
        	}
        	Document doc = new Document(e);
        	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		try
		{
                    outputter.output(doc, response.getOutputStream());
		}
		catch(Exception ex)
		{
                    System.out.println(ex.toString());
		}
        }
     
     /*   if ( type.equals("IMAGE_FROM_XML"))
        {
        	System.out.println("IMAGE_FROM_XML");
        	Element er = new Element("Pit-send-status");
        	try
                {
                    SAXBuilder builder = new SAXBuilder();
                    System.out.println("Getting doc from input stream.....");
                    Document doc = builder.build(request.getInputStream());
                    System.out.println("Got doc: ");

                    String name = "Pit:"+System.currentTimeMillis()+".xml";
                    File pfile = new File(name);

                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    System.out.println("Saving pit info to xml: ");
                    try
                    {
                            outputter.output(doc, new FileOutputStream(pfile));
                    }
                    catch(Exception ex)
                    {
                            System.out.println(ex.toString());
                    }
                    System.out.println("Saved pit to file: "+name);

                    XMLReader reader = new XMLReader();
                    System.out.println("Getting pit from Doc....");
                    avscience.ppc.PitObs pit = reader.getPitFromDoc(doc);
                    if ( pit!=null )
                    {
                            System.out.println("Starting pit frame.");
                            PitFrame frame = new PitFrame(pit, null, true);
                            System.out.println("Getting image from pit frame.");
                            BufferedImage image = frame.getPitImage();
                            String serial = pit.getSerial();
                            File f = new File("/Users/kahrlconsulting/Sites/kahrlconsulting/pits/"+serial+".jpg");
                            if (f.exists()) f.delete();
                            ImageIO.write(image, "jpg", f);

                            String srl = "http://kahrlconsulting.com/pits/"+serial+".jpg";
                            response.sendRedirect(srl);
                            frame=null;
                            image=null;
                            f=null;
                    }
            }
            catch(Exception e)
            {
            	System.out.println(e.toString());
            	try
                {
	                er.setAttribute("status", "Could not render pti image ! "+e.toString());
	                Document dr = new Document(er);
	                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			outputter.output(dr, response.getOutputStream());
                }
                catch(Exception ee)
            	{
            		System.out.println(ee.toString());
            	}
            }
        }*/
        
        if ( type.equals("XMLPIT_SEND"))
        {
        	System.out.println("XMLPIT_SEND");
        	Element er = new Element("Pit-send-status");
        	try
            {
            	SAXBuilder builder = new SAXBuilder();
            	System.out.println("Getting doc from input stream.....");
            	Document doc = builder.build(request.getInputStream());
            	System.out.println("Got doc: ");
                
                XMLReader reader = new XMLReader();
                System.out.println("Getting pit from Doc....");
                avscience.ppc.PitObs pit = reader.getPitFromDoc(doc);
                String serial = dao.generateSerial(pit);
                pit.setSystemSerial(serial);
               
                String name = "./xml_data/Pit:"+serial+".xml";
                File pfile = new File(name);
                
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                System.out.println("Saving pit info to xml: ");
                try
                {
                        outputter.output(doc, new FileOutputStream(pfile));
                       
                }
                catch(Exception ex)
                {
                        System.out.println(ex.toString());
                }
                System.out.println("Saved pit to file: "+name);
                
                CharArrayWriter cwriter = new CharArrayWriter(8400);
                
                try
                {
                     outputter.output(doc, cwriter);   
                }
                catch(Exception ex)
                {
                     System.out.println(ex.toString());
                }
                String xml = new String(cwriter.toCharArray());
                
                int res = dao.writePitToDB(pit, xml);
				
              //  if (res == 1) er.setAttribute("status", "Pit Added to database.");
              //  else if (res == 0)  er.setAttribute("status", "Pit already in DB.");
             //   else if ( res < 0) er.setAttribute("status", "Warning: Attempted but failed to write pit to DB ! see server logs for errors.");
            //    Document dr = new Document(er);
	   //     outputter = new XMLOutputter(Format.getPrettyFormat());
	//	outputter.output(dr, response.getOutputStream());
		//////////////////////
                
                String s = res+"";
                ObjectOutputStream ois = new ObjectOutputStream(response.getOutputStream());
                ois.writeObject(s);
                ois.flush();
                ois.close();
            }
            catch(Exception e)
            {
            	System.out.println(e.toString());
            	try
                {
                    ObjectOutputStream ois = new ObjectOutputStream(response.getOutputStream());
                    ois.writeInt(-1);
                    ois.flush();
                    ois.close();
	           /* er.setAttribute("status", "Pit NOT Added to database: "+e.toString());
	            Document dr = new Document(er);
	            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    outputter.output(dr, response.getOutputStream());*/
                }
                catch(Exception ee)
            	{
            		System.out.println(ee.toString());
            	}
            }
        }
        
        if ( type.equals("LOAD_PIT_FROM_DIR"))
        {
            File[] dir = new File("/Users/markkahrl/RAW_XML_PITS/").listFiles();
            
            for (File f : dir)
            {
                try
                {
                    FileInputStream fin = new FileInputStream(f);
                    SAXBuilder builder = new SAXBuilder();
                    Document doc = builder.build(fin);
                    
                    FileReader freader = new FileReader(f);
                    char[] chars = new char[(int)f.length()];
                    freader.read(chars, 0, chars.length);
                    String xml = new String(chars);
                    
                    XMLReader reader = new XMLReader();
                    System.out.println("Getting pit from Doc....");
                    avscience.ppc.PitObs pit = reader.getPitFromDoc(doc);
                    String serial = dao.generateSerial(pit);
                    pit.setSystemSerial(serial);
                    System.out.println("Adding pit : .... "+serial);
                    int res = dao.writePitToDB(pit, xml);
                    
                    try
                    {
                        Thread.sleep(200);
                    }
                    catch(Exception e)
                    {
                        System.out.println(e.toString());
                    }
                }
                catch(Exception ex)
                {
                    System.out.println(ex.toString());
                }
            }
        }
       
        
        if ( type.equals("XMLPIT"))
        {
            try
            {
                String ser = (String) request.getParameter("SERIAL");
                if (( ser!=null) && (ser.trim().length()>0))
                {
                    ///String xml = dao.getPitXML(ser);
                    
                    PitObs pit = new PitObs(dao.getPPCPit(ser));
                    Document doc = new XMLWriter().getDocumentFromPit(pit);
                    
                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    outputter.output(doc, response.getOutputStream());
                   
	        }
            }
            catch(Exception e){System.out.println(e.toString());}
        } 
        
        if ( type.equals("CAAMLPIT"))
        {
            try
            {
                String ser = (String) request.getParameter("SERIAL");
                if (( ser!=null) && (ser.trim().length()>0))
                {
                    System.out.println("Getting pit for XML:: # " + ser);
                    String data = dao.getPPCPit(ser);
                    if (data == null)
                    {
                            System.out.println("pit is NULL!!!!!.");
                            data="";
                    }

                    if ( data.trim().length()<3) System.out.println("No data for pit!! # "+ser);
                    String dir = "/Users/mark/Sites/";
                    String file = "Pit_"+ser+"_CAAML.xml";
                    File xmlFile = new File(dir, file);
                    avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
                    avscience.ppc.CAAMLWriter writer = new avscience.ppc.CAAMLWriter(pit, xmlFile);
                    writer.writePitToCAAML(pit);
                    String url = "http://home.kahrlconsulting.com/"+file;
                    response.sendRedirect(url);
	        }
            }
            catch(Exception e){System.out.println(e.toString());}
        }
        
        if ( type.equals("DELETEPIT"))
        {
        	try
        	{
                    String serial = request.getParameter("SERIAL");
                    String user = request.getParameter("USERNAME");
                    String name = request.getParameter("PITNAME");
                    ///dao.deletePit(user, serial, name);
        	}
        	catch(Exception e){System.out.println(e.toString());}
        }
        
        if ( type.equals("DELETEDBPIT"))
        {
        	try
        	{
        		String serial = request.getParameter("DBSERIAL");
        		dao.deletePit(serial);
        	}
        	catch(Exception e){System.out.println(e.toString());}
        }
        
        if ( type.equals("DELETEOCC"))
        {
        	try
        	{
                    String serial = request.getParameter("SERIAL");
                    String user = request.getParameter("USERNAME");
                    String name = request.getParameter("OCCNAME");
                    dao.deleteOcc(user, serial, name);
        	}
        	catch(Exception e){System.out.println(e.toString());}
        }
        
        if ( type.equals("PITIMAGE"))
        {
            try
            {
                String serial = request.getParameter("SERIAL");
                System.out.println("Getting pit image for: "+serial);
                String data = dao.getPPCPit(serial);
                System.out.println("got data for: "+serial);
                avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
                System.out.println("Pit constructed.");
                if ( pit!=null )
                {
                    System.out.println("Starting pit frame.");
                    PitFrame frame = new PitFrame(pit, null, true);
                    System.out.println("Getting image from pit frame.");
                    BufferedImage image = frame.getPitImage();
                    File f = new File("/Users/kahrlconsulting/Sites/kahrlconsulting/pits/"+serial+".jpg");
                    if (f.exists()) f.delete();
                    ImageIO.write(image, "jpg", f);

                    String srl = "http://kahrlconsulting.com/pits/"+serial+".jpg";
                    response.sendRedirect(srl);
                    frame=null;
                    image=null;
                    f=null;
                }
                else System.out.println("Pit is null!!!!!!!!!!!!!");
            }
            catch(Exception e){System.out.println(e.toString());}
        }
        
        if ( type.equals("AUTHSUPERUSER"))
        {
            System.out.println("AUTHSUPERUSER");
            String auth = "FALSE";
            try
            {
            	String name = request.getParameter("USERNAME");
            	String email = request.getParameter("EMAIL");
            	System.out.println("Name: "+name+" email: "+email);
            	if ((name!=null ) && ( email!=null))
            	{
                    name = name.trim();
                    email = email.trim();
                    if (( name.length() > 1 ) && ( email.length() > 1 ))
                    {
            		if ( dao.authSuperUser(name, email)) auth = "TRUE";
                    }
            	}
            	ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
                out.writeObject(auth);
                out.flush();
                out.close();
            }
            catch(Exception e){System.out.println(e.toString());}
        }
        
        /*if ( type.equals("OCCURENCE_QUERY"))
        {
        	System.out.println("OCCURENCE_QUERY");
        	String user = request.getParameter("USER");
        	if (user!=null) user = URLDecoder.decode(user);
        	System.out.println("User: "+user);
        	String email = request.getParameter("EMAIL");
        	if (email!=null) email = URLDecoder.decode(email);
        	System.out.println("Email: "+email);
        	PrintWriter writer=null;
        	if ((user!=null) && ( email!=null))
        	{
        		if ((user.trim().length() > 0 ) && ( email.trim().length()>0))
        		{
        			if (dao.authDataUser(user, email))
        			{
        				System.out.println("User authed.");
        				String whereclause = request.getParameter("WHERECLAUSE");
        				whereclause = URLDecoder.decode(whereclause);
        				if ((whereclause!=null) && (whereclause.trim().length()>1))
        				{
        					System.out.println("Where clause: "+whereclause);
        					try
        					{
        						writer = response.getWriter();
        						StringBuffer buffer = new StringBuffer();
        						String[][] occs = dao.getOccListArrayFromQuery(whereclause, false);
        						
        						String[] sers = occs[1];
        						System.out.println(" occs recieved:: "+sers.length);
        						if ((sers!=null) && sers.length>0)
        						{
                                                            for (int i=0; i<sers.length; i++)
                                                            {
        							String sr = sers[i];
        							long otime=0;
        							String data = dao.getPPCOcc(sr);
        							if (( data!=null) && data.trim().length()>2)
        							{
                                                                    avscience.ppc.AvOccurence occ = new avscience.ppc.AvOccurence(data);
                                                                    String ser = occ.getSerial();
                                                                    String pn = occ.getPitName();
                                                                    avscience.ppc.PitObs pit=null;
                                                                  
									System.out.println("getPitBySerial: "+ser);
									String pdata = dao.getPitByLocalSerial(ser);
									if ((pdata!=null) && (pdata.trim().length()>1))
									 {
									    System.out.println("getting pit by local serial.");
									    pit = new avscience.ppc.PitObs(pdata);
									    if (pit!=null) otime = pit.getTimestamp();
									  }
									  else System.out.println("Can't get Pit: "+ser +" by serial.");
									
                                                                                LinkedHashMap attributes = setLabels(pit);
									        Location loc = pit.getLocation();
									      
									        Enumeration e = null;
									        if (pit.getUser()!=null) buffer.append("User: " + pit.getUser().getName() + "\n");
									        buffer.append("Avalanche Occurrence Record: \n");
									        buffer.append(occ.getPitName() + "\n");
									        buffer.append("Location: \n");
									        if (loc!=null) buffer.append(loc.toString() + "\n");
									     
									        String dtime="";
									        long ltime = pit.getTimestamp();
									    	try
									    	{
                                                                                    if ( ltime > 0 ) 
                                                                                    {
									    		Date date = new Date(ltime);
									    		dtime = date.toString();
                                                                                    }
									    	}
									    	catch(Exception ee){System.out.println(ee.toString());}
									        occ.put("dtime", dtime);
									   
									       	Iterator keys = occ.keys();
									        String l = null;
									        String v = null;
									        while ( keys.hasNext() )
									        {
									            String s = keys.next().toString();
									            v = (String) occ.get(s);
									            l = (String) attributes.get(s);
									            s = l + " " + v + "\n";
									            if (( v!=null ) && ( v.trim().length() > 0 ))
									            {
									            	if (!( s.trim().equals("null")) )buffer.append(s);
									            }
        									}
        								}
        								buffer.append("\n\n");
        							}
        							System.out.println(buffer.toString());
        							writer.println(buffer.toString());
        						}
        					}
        					catch(Exception e)
        					{
                                                    System.out.println(e.toString());
                                                    if (writer!=null) writer.println(e.toString());
        					}
        				}
        			}
        		}
        	}
            }*/
        }
        
    /* public LinkedHashMap setLabels(avscience.ppc.PitObs pit)
     {
     	LinkedHashMap attributes = new LinkedHashMap();
        avscience.ppc.User u = pit.getUser();
        if ( u==null ) u = new avscience.ppc.User();
        attributes.put("pitObs", "Name: ");
        attributes.put("dtime", "Date/Time: ");
        attributes.put("estDate", "Estimated date: ");
        attributes.put("estTime", "Estimated Time: ");
        attributes.put("elvStart", "Elevation Start: (" + u.getElvUnits() + ") ");
        attributes.put("elvDeposit", "Elevation Deposit: (" + u.getElvUnits() + ") ");
        attributes.put("fractureWidth", "Fracture Width: (" + u.getElvUnits() + ") ");
        attributes.put("fractureLength", "Fracture Length: (" + u.getElvUnits() + ") ");
        attributes.put("lengthOfAvalanche", "Avalanche Length: (" + u.getElvUnits() + ") ");
       
        attributes.put("aspect", "Primary Aspect: ");
        attributes.put("aspect1", "Aspect 1: ");
        attributes.put("aspect2", "Aspect 2: ");
        attributes.put("type", "Type: ");
        attributes.put("wcStart", "Water Content Start: ");
        attributes.put("wcDeposit", "Water Content Deposit: ");
        attributes.put("triggerType", "Trigger Type: ");
        attributes.put("triggerCode", "Trigger Code: ");
        attributes.put("causeOfRelease", "Cause of release: ");
        attributes.put("sympathetic", "Sympathetic? ");
        attributes.put("sympDistance", "Sympathetic/remote distance: ");
        
        attributes.put("USSize", "Size relative to Path: ");
        attributes.put("CASize", "Size destructive force: ");
        attributes.put("avgFractureDepth", "Average Fracture Depth: (" + u.getDepthUnits() + ") " );
        attributes.put("maxFractureDepth", "Max. Fracture Depth: (" + u.getDepthUnits() + ") ");
        attributes.put("levelOfBedSurface", "Level Of Bed Surface: ");
        attributes.put("weakLayerType", "Weak Layer Type: ");
        attributes.put("crystalSize", "Weak Layer Crystal Size: ");
        attributes.put("sizeSuffix", "Weak Layer Size suffix: ");
        attributes.put("weakLayerHardness", "Weak Layer Hardness: ");
        attributes.put("hsuffix", "Weak Layer Hardness suffix: ");
        
        attributes.put("crystalTypeAbove", "Crystal Type Above: ");
        attributes.put("crystalSizeAbove", "Crystal Size Above: ");
        attributes.put("sizeSuffixAbove", "Size suffix above: ");
        attributes.put("hardnessAbove", "Hardness above: ");
        attributes.put("hsuffixAbove", "Hardness suffix above: ");
        
        attributes.put("crystalTypeBelow", "Crystal Type Below: ");
        attributes.put("crystalSizeBelow", "Crystal Size Below: ");
        attributes.put("sizeSuffixBelow", "Size suffix below: ");
        attributes.put("hardnessBelow", "Hardness below: ");
        attributes.put("hsuffixBelow", "Hardness suffix below: ");
        attributes.put("snowPackType", "Snow Pack Typology: ");
        attributes.put("avgStartAngle", "Avg Start Angle: ");
        attributes.put("maxStartAngle", "Max Start Angle: ");
        attributes.put("minStartAngle", "Min Start Angle: ");
        attributes.put("alphaAngle", "Alpha Angle: ");
        attributes.put("depthOfDeposit",  "Depth of deposit: (" + u.getDepthUnits() + ") ");
        attributes.put("lengthOfDeposit", "Length of deposit: ");
        attributes.put("widthOfDeposit", "Width of deposit: ");
        attributes.put("densityOfDeposit", "Density of deposit (" + u.getRhoUnits() + ") ");
  	attributes.put("numPeopleCaught", "Number of people caught: ");
        attributes.put("numPeoplePartBuried", "Number of people part buried: ");
        attributes.put("numPeopleTotalBuried", "Number of people totally buried: ");
        
        attributes.put("injury", "Injuries: ");
        attributes.put("fatality", "Fatalites: ");
        
        attributes.put("bldgDmg", "Building Damage US $: ");
        attributes.put("eqDmg", "Equipment Damage US $: ");
        attributes.put("vehDmg", "Vehicle Damage US $: ");
        attributes.put("miscDmg", "Misc Damage US $: ");
        attributes.put("estDamage", "Total Damage US $: ");
        attributes.put("comments", "Comments: ");
        attributes.put("hasPit", "Has pit observation? ");
        return attributes;
    }*/
     
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException 
     {
        doGet(request, response);
     }
     
    class PitImageWriter extends Thread
    {
    	PitObs pit;
    	HttpServletResponse response;
    	String serial;
    	public PitImageWriter(PitObs pit, HttpServletResponse response, String serial) 
    	{
            this.pit=pit;
            this.response=response;
            this.serial=serial;
    	}
    }
    
    private String generateHash(PitObs pit)
    {
        String s = pit.toString();
        byte[] bts = s.getBytes();
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] res = md.digest(bts);
            return new String(res);
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
            return System.currentTimeMillis()+pit.getSerial()+pit.getName();
        }
    }
    
    
       /* if (type.equals("WRITE_SLF")) 
        {
        	dao.writeSLFLayersToFile();
        	return;
        }*/
        
        //if (type.equals("WRITE_ECTPITS")) dao.writeECPTTestPits();
            
      //  if (type.equals("WRITE_EMAIL_FILE")) dao.getEmailsAsCSVFile();
        
}