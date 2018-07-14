package avscience.server;

import java.sql.*;
import java.util.*;
import java.io.*;
import avscience.ppc.*;
import java.security.MessageDigest;

public class DAO {

    private final static String DBUrl = "jdbc:mysql:///avscience_dev";

    java.util.Date startDate = new java.util.Date(114, 4, 19);

    public DAO() {
        System.out.println("DAO() StartDate: " + startDate.toString());
        loadDriver();
    }

    public static void setNewsProps(String newNews) {

        Properties props = getNewsProps();
        props.setProperty("current_news", newNews);
        try {
            File file = new File("news.properties");
            FileOutputStream fout = new FileOutputStream(file);
            props.save(fout, "new_change");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    static Properties getNewsProps() {
        Properties props = new Properties();
        try {
            File file = new File("news.properties");
            FileInputStream fin = new FileInputStream(file);
            props.load(fin);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return props;
    }

    public String getNews() {
        System.out.println("getNews()");
        Properties props = getNewsProps();
        String news = props.getProperty("current_news");
        return news;
    }

    public void addPitXML(String serial, String xml) {
        System.out.println("addPitXML");
        String query = "UPDATE PIT_TABLE SET PIT_XML = ? WHERE SYSTEM_SERIAL = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, xml);
            ps.setString(2, serial);
            int i = ps.executeUpdate();
            if (i > 0) {
                System.out.println("PIT XML added.");
            } else {
                System.out.println("PIT XML NOT added!!");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public boolean checkBuild(avscience.ppc.PitObs pit) {
        int bld = pit.getBuild();
        /// build # on whick pit changed to top/bottom instead of start/end
        if (bld >= 563) {
            return true;
        } else {
            return false;
        }
    }

    public void writeECPTTestPits() {
        File pitfile = new File("/Users/markkahrl/ISSW2014_PitData.csv");
        File joinfile = new File("/Users/markkahrl/JoinData.csv");
        File testfile = new File("/Users/markkahrl/CT_TestData.csv");
        StringBuffer pitBuffer = new StringBuffer();
        StringBuffer joinBuffer = new StringBuffer();
        StringBuffer testBuffer = new StringBuffer();
        pitBuffer.append("SERIAL ; OBSERVER ; LOCATION ; MTN_RANGE ; STATE ; ELV ; ELV_UOM ; LAT ; LONG ; ASPECT ; INCLINE ; PRECIP ; SKY ; WINDSPEED ; WIND_DIR ; WIND_LOADING ; AIR_TEMP ; AIR_TEMP_UOM ; STABILITY ; MEASURE_FROM ; DATE ; DEPTH_UNITS ; DENSITY_UNITS ; PI_DEPTH \n");
        joinBuffer.append("pit_serial,ecScore , CTScore,ECT ShearQuality , CT ShearQuality, Total depth,depth to problematic layer , number of taps, Release type,length of cut ,length of column , slope angle, depth units \n");
        testBuffer.append("pit_serial, Score, CTScore \n");

        Hashtable pits = getAllPits();
        Enumeration e = pits.keys();
        while (e.hasMoreElements()) {
            String serial = (String) e.nextElement();
            String dat = (String) pits.get(serial);
            avscience.ppc.PitObs pit = null;
            try
            {
                pit = new avscience.ppc.PitObs(dat);
            }
            catch(Exception ee)
            {
                System.out.println(ee.toString());
                continue;
            }
            
            avscience.ppc.Location loc = pit.getLocation();
            if (loc == null) {
                loc = new avscience.ppc.Location();
            }

            System.out.println("writing tests for pit: " + serial);
            java.util.Enumeration tests = pit.getShearTests();
            boolean hasCTTest = false;
            boolean hasECPTTest = false;
            while (tests.hasMoreElements()) {

                avscience.ppc.ShearTestResult result = (avscience.ppc.ShearTestResult) tests.nextElement();
                String code = result.getCode().trim();
                String score = result.getScore().trim();
                System.out.println("Pit:: " + serial + " code " + code + " Score: " + score);
                if (code.equals("CT")) {
                    hasCTTest = true;
                }
                if (score.equals("ECTP")) {
                    hasECPTTest = true;
                }

            }
            if (true) /// if (hasCTTest & hasECPTTest)  
            {
                avscience.ppc.Layer l = pit.getPILayer();

                System.out.println("writing data for pit: " + serial);
                pitBuffer.append(serial + " ; ");
              //  pitBuffer.append(u.getFirst() + " " + u.getLast() + " ; ");
                pitBuffer.append(loc.getName() + " ; ");
                pitBuffer.append(loc.getRange() + " ; ");
                pitBuffer.append(loc.getState() + " ; ");
                pitBuffer.append(loc.getElv() + " ; ");
                pitBuffer.append(pit.getPrefs().getElvUnits() + " ; ");
                pitBuffer.append(loc.getLat() + " ; ");
                pitBuffer.append(loc.getLongitude() + " ; ");
                pitBuffer.append(pit.getAspect() + " ; ");
                pitBuffer.append(pit.getIncline() + " ; ");
                pitBuffer.append(pit.getPrecip() + " ; ");
                pitBuffer.append(pit.getSky() + " ; ");
                pitBuffer.append(pit.getWindspeed() + " ; ");
                pitBuffer.append(pit.getWinDir() + " ; ");
                pitBuffer.append(pit.getWindLoading() + " ; ");
                pitBuffer.append(pit.getAirTemp() + " ; ");
                pitBuffer.append(pit.getPrefs().getTempUnits() + " ; ");
                pitBuffer.append(pit.getStability() + " ; ");
                pitBuffer.append(pit.getMeasureFrom() + " ; ");
                pitBuffer.append(pit.getDate() + " ; ");
               // pitBuffer.append(pit.getTime() + " ; ");
                ///	pitBuffer.append(pit.getPitNotes()+", ");
                pitBuffer.append(pit.getPrefs().getDepthUnits() + " ; ");

                pitBuffer.append(pit.getPrefs().getRhoUnits() + " ;");
                pitBuffer.append(pit.iDepth + "\n");
                ////////
                System.out.println("writing tests for pit: " + serial);
                tests = pit.getShearTests();
                avscience.ppc.ShearTestResult ectTest = null;
                avscience.ppc.ShearTestResult ctTest = null;
                String numberOfTaps = null;
                String releaseType = null;
                String lengthOfCut = null;
                String lengthOfColumn = null;

                while (tests.hasMoreElements()) {
                    avscience.ppc.ShearTestResult result = (avscience.ppc.ShearTestResult) tests.nextElement();
                    String code = result.getCode();
                    String score = result.getScore();
                    String rt = result.getReleaseType();
                    rt = rt.trim();
                    if (rt.length() > 0) {
                        releaseType = rt;
                    }
                    /////
                    String nt = result.numberOfTaps;
                    nt = nt.trim();
                    if (nt.length() > 0) {
                        numberOfTaps = nt;
                    }

                    String lc = result.lengthOfCut;
                    lc = lc.trim();
                    if (lc.length() > 0) {
                        lengthOfCut = lc;
                    }

                    String lcc = result.lengthOfColumn;
                    lcc = lcc.trim();
                    if (lcc.length() > 0) {
                        lengthOfColumn = lcc;
                    }

                    // int scr = new java.lang.Integer(score).intValue();
                    int ecScore = result.getECScoreAsInt();
                    int ctScore = result.getCTScoreAsInt();
                    if (ectTest != null) {
                        if (ecScore != 0) {
                            if (ecScore < ectTest.getECScoreAsInt()) {
                                ectTest = result;
                            }
                        }

                    }

                    if (score.equals("ECTP") & ectTest == null) {
                        ectTest = result;
                    }

                    if (ctTest != null) {
                        if (ctScore != 0) {
                            if (ctScore > ctTest.getCTScoreAsInt()) {
                                ctTest = result;
                            }
                        }

                    }

                    if (score.equals("ECTP") & ectTest == null) {
                        ectTest = result;
                    }
                    if (code.equals("CT") & ctTest == null) {
                        ctTest = result;
                    }

                    ////
                    if (code.equals("CT")) {
                        testBuffer.append(serial + ", " + result.getScore() + ", " + result.getCTScore() + "\n");
                    }
                }
                // joinBuffer.append(serial+", "+ectTest.getECScore()+", "+ctTest.getCTScore()+", "+ectTest.getQuality()+", "+ctTest.getQuality()+", "+getMaxDepth(pit)+", "+pit.iDepth+", "+numberOfTaps+", "+releaseType+", "+lengthOfCut+", "+lengthOfColumn+", "+pit.getIncline()+", " +pit.getUser().getDepthUnits()+"\n");

            }
        }

        FileOutputStream out = null;
        PrintWriter writer = null;
        try {
            out = new FileOutputStream(testfile);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(testBuffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public int getMaxDepth(avscience.ppc.PitObs pit) {
        System.out.println("getMaxDepth");
        int max = 0;
        if (pit == null) {
            System.out.println("PIT IS NULL.");
            return 0;
        }

        System.out.println("max depth layers.");
        java.util.Enumeration e = null;
        if (pit.hasLayers()) {
            e = pit.getLayers();
            if (e != null) {
                while (e.hasMoreElements()) {
                    avscience.ppc.Layer l = (avscience.ppc.Layer) e.nextElement();
                    int end = l.getEndDepthInt();
                    if (end > max) {
                        max = end;
                    }
                    int start = l.getStartDepthInt();
                    if (start > max) {
                        max = start;
                    }
                }
            }
        }
        System.out.println("max depth tests.");
        if (max == 0) {
            e = pit.getShearTests();
            if (e != null) {
                while (e.hasMoreElements()) {
                    avscience.ppc.ShearTestResult result = (avscience.ppc.ShearTestResult) e.nextElement();
                    int depth = result.getDepthValueInt();
                    if (depth > max) {
                        max = depth;
                    }
                }
            }
            //	max+=6;
        }
        
        return max;
    }

    void writePitToFile(avscience.ppc.PitObs pit) {
        ///avscience.ppc.User u = pit.getUser();
        FileOutputStream out = null;
        PrintWriter writer = null;
        File file = new File("TestPitFile.txt");
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        StringBuffer buffer = new StringBuffer();
        avscience.ppc.Location loc = pit.getLocation();
        buffer.append(pit.getDateString() + "\n");
       // buffer.append("Observer ," + u.getFirst() + " " + u.getLast() + "\n");
        buffer.append("Location ," + loc.getName() + "\n");
        buffer.append("Mtn Range ," + loc.getRange() + "\n");
        buffer.append("State/Prov ," + loc.getState() + "\n");
        buffer.append("Elevation " + pit.getPrefs().getElvUnits() + " ," + loc.getElv() + "\n");
        buffer.append("Lat. ," + loc.getLat() + "\n");
        buffer.append("Long. ," + loc.getLongitude() + "\n");

        Hashtable labels = getPitLabels();
       // avscience.util.Hashtable atts = pit.attributes;
        Enumeration e = labels.keys();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            String v ="";
            try
            {
                v = (String) pit.get(s);
            }
            catch(Exception ee)
            {
                System.out.println(ee.toString());
            }
            String l = (String) labels.get(s);
            s = l + " ," + v + "\n";
            if (!(s.trim().equals("null"))) {
                buffer.append(s);
            }
        }
        buffer.append("Activities: \n");
        java.util.Enumeration ee = pit.getActivities().elements();
        while (ee.hasMoreElements()) {
            String s = (String) ee.nextElement();
            buffer.append(s + "\n");
        }

        if (file == null) {
            return;
        }
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        try {
            writer.print(buffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public LinkedHashMap getPitsFromQuery(String whereclause) {
        System.out.println("getPitsFromQuery(): " + whereclause);
        LinkedHashMap v = new LinkedHashMap();
        String[][] pits = getPitListArrayFromQuery(whereclause, false);

        for (int i = 0; i < pits[1].length; i++) {
            String serial = pits[1][i];
            String data = getPPCPit(serial);

            v.put(serial, data);
        }
        return v;
    }

    public String[][] getPitStringArrayFromQuery(String whereclause) {
        System.out.println("getPitsFromQuery(): " + whereclause);
        String[][] pits = getPitListArrayFromQuery(whereclause, false);
        String[][] rpits = new String[3][pits[1].length];
      
        for (int i = 0; i < pits[1].length; i++) {
            String serial = pits[1][i];
            String data = getPPCPit(serial);
            String name = pits[0][i];

            rpits[0][i] = serial;
            rpits[1][i] = name;
            rpits[2][i] = data;

        }
        return rpits;
    }

    Hashtable getPitLabels() {
        Hashtable attributes = new Hashtable();

        attributes.put("aspect", "Aspect");
        attributes.put("incline", "Slope Angle");
        attributes.put("precip", "Precipitation");
        attributes.put("sky", "Sky Cover");
        attributes.put("windspeed", "Wind Speed");
        attributes.put("winDir", "Wind Direction");
        attributes.put("windLoading", "Wind Loading");

        attributes.put("airTemp", "Air Temperature");
        attributes.put("stability", "Stability on simular slopes");

        attributes.put("measureFrom", "Measure from: ");

        attributes.put("date", "Date");
        attributes.put("time", "Time");
        attributes.put("pitNotes", "Notes");
        return attributes;
    }

    
    private void loadDriver() {
        System.out.println("Load Driver..");
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.out.println("Unable to Load Driver: " + e.toString());
        }
    }

    private Connection getConnection() throws SQLException {
        System.out.println("get Connection..");
        return DriverManager.getConnection(DBUrl, "root", "port");
    }

    boolean hasUser(User u) {
        String userName = u.getUserName();

        boolean has = false;
        String query = "SELECT * FROM USER_TABLE WHERE USERNAME ='" + userName + "' AND EMAIL = '"+u.getEmail()+"'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                has = true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return has;
    }

    public boolean addUser(User user) {
        boolean add = false;
        if (!hasUser(user)) {
            add = true;
            System.out.println("dao: Adding user: ");
            String query = "INSERT INTO USER_TABLE (USERNAME, EMAIL, PROF, AFFIL, FIRST, LAST, PHONE) VALUES (?,?,?,?,?,?,?)";

            try {
                Connection conn = getConnection();
                if (conn == null) {
                    System.out.println("Connection null::");
                } else {
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, user.getUserName());
                    stmt.setString(2, user.getEmail());
                    stmt.setBoolean(3, user.getProf());
                    stmt.setString(4, user.getAffil());
                    stmt.setString(5, user.getFirst());
                    stmt.setString(6, user.getLast());
                    stmt.setString(7, user.getPhone());
                    int r = stmt.executeUpdate();
                    if (r > 0) {
                        System.out.println("User added.");
                    } else {
                        System.out.println("User not added.");
                    }
                }

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return add;
    }

    public void updateUser(User user) {
        deleteUser(user);
        addUser(user);
    }

    public void deleteUser(User user) {
        String query = "DELETE FROM USER_TABLE WHERE USERNAME = '" + user.getUserName() + "'";

        try {
            Statement stmt = getConnection().createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public boolean userExist(String userName) {
        boolean exist = false;
        String query = "SELECT * FROM USER_TABLE WHERE USERNAME ='" + userName + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                exist = true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return exist;
    }

    public boolean authWebUser(String userName, String email) {
        System.out.println("authWebUser");
        System.out.println("user: " + userName);
        System.out.println("email: " + email);
        boolean auth = false;
        String query = "SELECT * FROM USER_TABLE WHERE USERNAME ='" + userName + "' AND EMAIL = '" + email + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                auth = true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Auth: " + auth);
        return auth;
    }

    public boolean authDataUser(String userName, String email) {
        System.out.println("authWebUser");
        System.out.println("user: " + userName);
        System.out.println("email: " + email);
        boolean auth = false;
        String query = "SELECT * FROM USER_TABLE WHERE USERNAME ='" + userName + "' AND EMAIL = '" + email + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                boolean duser = rs.getBoolean("DATAUSER");
                auth = duser;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Auth: " + auth);
        return auth;
    }

    public boolean authSuperUser(String userName, String email) {
        System.out.println("authSuperUser");
        System.out.println("user: " + userName);
        System.out.println("email: " + email);
        boolean auth = false;
        String query = "SELECT * FROM USER_TABLE WHERE USERNAME ='" + userName + "' AND EMAIL = '" + email + "'";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                System.out.println("SU: " + rs.getBoolean("SUPERUSER"));
                if (rs.getBoolean("SUPERUSER")) {
                    auth = true;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("Auth: " + auth);
        return auth;
    }
    
    private boolean xmlExistsAndUnAltered(String xml)
    {
        String query = "SELECT PIT_XML FROM PIT_TABLE WHERE PIT_XML = ?";
        PreparedStatement stmt = null;
        
        try 
        {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, xml);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
                
        return false;
    }

    private boolean pitUnAltered(String data) {
        System.out.println("pitUnAltered()");
        boolean un = false;
        avscience.ppc.PitObs pit = null;
        try
        {
            pit = new avscience.ppc.PitObs(data);
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
            return false;
        }
        String name = pit.getName();
        String ser = pit.getSerial();
        System.out.println("pit: " + name);

        if (ser.trim().length() > 0) {
            String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE PIT_NAME = ? AND LOCAL_SERIAL = ?";
            PreparedStatement stmt = null;
            String s = "";
            if ((name != null) && (name.trim().length() > 0)) {
                try {
                    stmt = getConnection().prepareStatement(query);
                    stmt.setString(1, name);
                    stmt.setString(2, ser);
                    ResultSet rs = stmt.executeQuery();
                    System.out.println("Query executed:");
                    if (rs.next()) {
                        System.out.println("Result::");
                        s = rs.getString("PIT_DATA");
                    }
                    un = s.equals(data);
                } catch (Throwable e) {
                    System.out.println(e.toString());
                }
            }
        } else {
            System.out.println("no local serial for pit: " + name);
            String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE PIT_NAME = ?";
            PreparedStatement stmt = null;
            String s = "";
            if ((name != null) && (name.trim().length() > 0)) {
                try {
                    stmt = getConnection().prepareStatement(query);
                    stmt.setString(1, name);

                    ResultSet rs = stmt.executeQuery();
                    System.out.println("Query executed:");
                    if (rs.next()) {
                        System.out.println("Result::");
                        s = rs.getString("PIT_DATA");
                    }
                    un = s.equals(data);
                } catch (Throwable e) {
                    System.out.println(e.toString());
                }
            }
        }

        System.out.println("pit unaltered?: " + un);
        return un;
    }

    private boolean occUnAltered(String data) {
        System.out.println("occUnAltered");
        boolean unaltered = false;

        String query = "SELECT SERIAL FROM OCC_TABLE WHERE OCC_DATA = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, data);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                unaltered = true;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return unaltered;
    }

    public String removeDelims(String s) {
        System.out.println("s: " + s);
        String d = "'";
        char delim = d.charAt(0);
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == delim) {
                chars[i] = ' ';
            }
        }
        String result = new String(chars);
        System.out.println("result: " + result);
        return result;
    }
  
    public String generateSerial(PitObs pit)
    {
        String s = pit.toString();
        byte[] bts = s.getBytes();
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] res = md.digest(bts);
            
            long sum = 0;
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i<res.length; i++)
            {
                sum += Math.abs(res[i]);
            }
           return (sum+"-"+(int)res[0]+(int)res[1]+":"+System.currentTimeMillis());
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
            return s;
        }
    }
    
    public int writePitToDB(avscience.ppc.PitObs pit, String xml) 
    {
        if (xmlExistsAndUnAltered(xml))
         {
              System.out.println("Pit XML already in DB");
              return 0;
         }
        addUser(pit.getUser());
        if (pit.getUserHash()==null) pit.setUserhash(pit.getUser());
        String system_serial = null;
        int retValue = -1;
        System.out.println("writePitToDB::PitObs");
        if (pit == null)
        {
            System.out.println("Pit is null !!! ");
            return -1;
        }
            String data = pit.toString();
            if (pitPresent(pit)) 
            {
                System.out.println("Pit already in DB");
                if (pitUnAltered(data)) 
                {
                    return 0;
                } 
                else 
                {
                    system_serial = pit.getSystemSerial();
                    System.out.println("deleting pit:");
                    deletePit(pit.getSystemSerial());

                }
            }
            if (system_serial == null) 
            {
                system_serial = generateSerial(pit);
                pit.setSystemSerial(system_serial);
            }

            System.out.println("writing pit to DB : " + pit.getName());
            String query = "INSERT INTO PIT_TABLE (PIT_DATA, AIR_TEMP, ASPECT, CROWN_OBS, DATE_TEXT, TIMESTAMP, INCLINE, LOC_NAME, LOC_ID, STATE, MTN_RANGE, LAT, LONGITUDE, NORTH, WEST, ELEVATION, USERHASH, WINDLOADING, PIT_NAME, HASLAYERS, LOCAL_SERIAL, PRECIP, SKY_COVER, WIND_SPEED, WIND_DIR, STABILITY, SHARE, ACTIVITIES, TEST_PIT, PLATFORM, SYSTEM_SERIAL, LAST_UPDATED, PIT_XML, AVIPIT, AVILOC, ILAYER, IDEPTH, SKI_AREA_PIT, BC_PIT, SURFACE_PEN, BOOT_SKI) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                  ///                                 1        2          3       4           5         6           7       8         9      10      11       12     13        14    15     16         17         18          19         20            21           22       23       24      25           26       27         28     29            30       31          32              34       35        36      37     38         39          40        41                  
             String pit_name = pit.generateName();
             pit.setName(pit_name);
            try {
                Connection conn = getConnection();
                if (conn == null) {
                    System.out.println("Connection null::");
                    return -1;
                } 
                
                    PreparedStatement stmt = conn.prepareStatement(query);
                    long ptime = 0;
                    ptime = pit.getTimestamp();
                    stmt.setString(1, data);
                 
                    float temp = -999.9f;
                    System.out.println("setting air temp");
                    try {
                        if ((pit.getAirTemp() != null) && (pit.getAirTemp().trim().length() > 0)) {
                            temp = stringToFloat(pit.getAirTemp());
                            if (pit.getPrefs().getTempUnits().equals("F")) {
                                temp = FtoC(temp);
                            }
                        }
                        // air temp C
                        stmt.setFloat(2, temp);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        stmt.setFloat(2, -999.9f);
                    }
                    System.out.println("setting aspect");
                    try {
                        stmt.setInt(3, stringToInt(pit.getAspect()));
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        stmt.setInt(3, -999);
                    }
                    // is Crown obs?
                    System.out.println("setting crown obs");
                    boolean co = pit.getCrownObs();
                    stmt.setBoolean(4, co);
                    /// date of pit
                    System.out.println("setting datestring");

                    stmt.setString(5, pit.getDateString());

                    // date entered here
                    System.out.println("setting timestamp");
                    stmt.setDate(6, new java.sql.Date(System.currentTimeMillis()));
                    // incline
                    System.out.println("setting incline");
                    try {
                        String incl = pit.getIncline();
                        System.out.println("incline: " + incl);
                        stmt.setInt(7, stringToInt(incl));
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        stmt.setInt(7, -999);
                    }
                    // location
                    System.out.println("getting loc");
                    avscience.ppc.Location loc = pit.getLocation();
                    System.out.println("Locname : " + loc.getName().trim());
                    String ln = loc.getName().trim();
                    ln.replaceAll("'", "");
                    System.out.println("setting locname");
                    stmt.setString(8, ln);
                    System.out.println("setting locID");
                    stmt.setString(9, loc.getID().trim());
                    System.out.println("setting state");
                    stmt.setString(10, loc.getState().trim());
                    System.out.println("setting range");
                    stmt.setString(11, loc.getRange().trim());
                    System.out.println("setting lat");
                    float lat = -999.9f;
                    try {
                        lat = stringToFloat(loc.getLat());
                        stmt.setFloat(12, lat);
                    } catch (Exception e) {
                        stmt.setFloat(12, -999.9f);
                        System.out.println(e.toString());
                    }
                    System.out.println("setting long");
                    float longitude = -999.9f;
                    try {
                        longitude = stringToFloat(loc.getLongitude());
                        stmt.setFloat(13, longitude);
                    } catch (Exception e) {
                        stmt.setFloat(13, -999.9f);
                        System.out.println(e.toString());
                    }
                    System.out.println("setting lat type");
                    stmt.setBoolean(14, loc.getLatType().equals("N"));
                    System.out.println("setting long type");
                    stmt.setBoolean(15, loc.getLongType().equals("W"));

                    System.out.println("setting elv");
                    try {
                        System.out.println("elv: " + loc.getElv());
                        int e = stringToInt(loc.getElv());
                        System.out.println("elevation: " + e);
                        if (pit.getPrefs().getElvUnits().equals("ft")) {
                            e = ft_to_m(e);
                        }
                        stmt.setInt(16, e);
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                        stmt.setInt(16, -999);
                    }
                    // user name
                    System.out.println("setting username");
                    stmt.setString(17, pit.getUserHash());
                    stmt.setString(18, pit.getWindLoading());
                    // name
                   // String pn = pit.getName().trim();
                   // pn.replaceAll("'", "");
                    System.out.println("setting name");
                    stmt.setString(19, pit_name);
                    System.out.println("setting has layers");
                    stmt.setBoolean(20, pit.hasLayers());
                    System.out.println("setting serial.");
                    stmt.setString(21, pit.getSerial());
                    System.out.println("setting wind loading:");
                    stmt.setString(22, pit.getPrecip());
                    stmt.setString(23, pit.getSky());
                    stmt.setString(24, pit.getWindspeed());
                    stmt.setString(25, pit.getWinDir());
                    stmt.setString(26, pit.getStability());
                    stmt.setBoolean(27, pit.getPrefs().getShare());
                    Timestamp ots = new Timestamp(pit.getTimestamp());
                    StringBuffer buffer = new StringBuffer(" ");
                    try {
                       // System.out.println("setting activities: ");
                        if (pit.getActivities() != null) {
                         //   System.out.println("# activities: " + pit.getActivities().size());
                            java.util.Enumeration e = pit.getActivities().elements();
                            if (e != null) {
                                while (e.hasMoreElements()) {
                                    String s = (String) e.nextElement();
                                    //System.out.println("acts: " + s);
                                    buffer.append(" : " + s + " : ");
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                    System.out.println("Setting activities:");
                    stmt.setString(28, buffer.toString());
                    boolean testPit = false;
                    System.out.println("Setting Test Pit?:");
                    if (pit.testPit != null) {
                        if (pit.testPit.trim().equals("true")) {
                            testPit = true;
                        }
                        stmt.setBoolean(29, testPit);
                    } else {
                        stmt.setBoolean(29, false);
                    }
                    System.out.println("Setting software version:");
                    if (pit.version != null) {
                        stmt.setString(30, pit.version);
                    } else {
                        stmt.setString(30, "");
                    }
                    
                    System.out.println("System serial: "+pit.getSystemSerial());
                    stmt.setString(31, pit.getSystemSerial());
                    stmt.setTimestamp(32, new Timestamp(System.currentTimeMillis()));
                    stmt.setString(33, xml);
                    stmt.setBoolean(34, pit.isAviPit());
                    stmt.setString(35, pit.aviLoc);
                    try
                    {
                        stmt.setInt(36, new Integer(pit.iLayerNumber).intValue());
                    }
                    catch(Exception ex)
                    {
                        stmt.setInt(36, 0);
                    }
                    
                    try
                    {
                        stmt.setDouble(37, new Double(pit.iDepth).doubleValue());
                    }
                    catch(Exception ee)
                    {
                        stmt.setDouble(37, 0.0);
                    }
                    stmt.setBoolean(38, pit.isSkiAreaPit());
                    stmt.setBoolean(39, pit.isBCPit());
                    try
                    {
                        stmt.setDouble(40, new Double(pit.getSurfacePen()).doubleValue());
                    }
                    catch(Exception exx)
                    {
                        stmt.setDouble(40,0.0);
                    }
                    
                    stmt.setString(41, pit.getSkiBoot());

                    System.out.println("execute query:");
                    retValue = stmt.executeUpdate();
                    System.out.println("Update executed!");
                    if ( retValue > 0 ) System.out.println("Pit added to DB !!!!!!");
                    conn.close();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            writeLayersToDB(pit);
            writeTestsToDB(pit);
            
        
        return retValue;
    }
    
    void writeLayersToDB(avscience.ppc.PitObs pit)
    {
        System.out.println("Write Layers to DB:");
        String serial = pit.getSystemSerial();
        int numLayers = pit.getLayersVector().size();
        System.out.println("Writing "+numLayers+" to DB.");
        Enumeration le = pit.getLayers();
        while (le.hasMoreElements())
        {
            avscience.ppc.Layer l = (avscience.ppc.Layer) le.nextElement();
            writeLayerToDB(l, serial);
        }
    }
    
    void writeTestsToDB(avscience.ppc.PitObs pit)
    {
        System.out.println("Write Tests to DB:");
        String serial = pit.getSystemSerial();
        Enumeration le = pit.getShearTests();
        while (le.hasMoreElements())
        {
            avscience.ppc.ShearTestResult test = (avscience.ppc.ShearTestResult) le.nextElement();
            writeTestToDB(test, serial);
        }
    }
    
    void writeTestToDB(avscience.ppc.ShearTestResult test, String serial)
    {
        System.out.println("Write Test to DB: for pit: "+serial);
        
        String query = "INSERT INTO TEST_TABLE (LABEL, CODE, SCORE, QUALITY, DEPTH, CT_SCORE, EC_SCORE, DEPTH_UNITS, DATE_STRING,"
                + " RELEASE_TYPE, FRACTURE_CHAR, FRACTURE_CAT, NUMBER_OF_TAPS, LENGTH_OF_CUT, LENGTH_OF_COLUMN, COMMENTS, PIT_SERIAL)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            System.out.println("Setting test string");
            stmt.setString(1, test.toString());
            System.out.println("setting test code");
            stmt.setString(2, test.getCode());
            System.out.println("setting test score");
            stmt.setString(3, test.getScore());
            System.out.println("setting test quality");
            stmt.setString(4, test.getQuality());
            System.out.println("setting depth");
            stmt.setDouble(5, test.getDepthValue());
            System.out.println("setting ct score");
            stmt.setInt(6, test.getCTScoreAsInt());
            System.out.println("setting ec score");
            stmt.setInt(7, test.getECScoreAsInt());
            System.out.println("setting depth units");
            stmt.setString(8, test.getDepthUnits());
            System.out.println("setting test date");
            stmt.setString(9, test.getDateString());
            System.out.println("setting releaseType");
            stmt.setString(10, test.getReleaseType());
            System.out.println("setting test character");
            stmt.setString(11, test.character);
            System.out.println("setting fracture cat");
            stmt.setString(12, test.fractureCat);
            System.out.println("setting number of taps");
            stmt.setInt(13, test.getNumberOfTaps());
            System.out.println("setting length of cut");
            stmt.setInt(14, test.getLengthOfCut());
            System.out.println("setting length of column");
            stmt.setInt(15, test.getLengthOfColumn());
            System.out.println("setting test comments");
            stmt.setString(16, test.getComments());
            System.out.println("setting serial: "+serial);
            stmt.setString(17, serial);
            
            int rw = stmt.executeUpdate();
            if (rw > 0 ) System.out.println("TEST added !!");
            else System.out.println("Error adding TEST");
            conn.close();
            
            
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        
    }
    
    
    void deletePitLayers(String serial)
    {
        String query = "DELETE FROM LAYER_TABLE WHERE PIT_SERIAL = ?";
        
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, serial);
            int rws = stmt.executeUpdate();
            System.out.println(rws+" layers deleted for pit: "+serial);
            conn.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    void deletePitTests(avscience.ppc.PitObs pit)
    {
       /// long serial = getDBSerial(pit);
       // deletePitTests(serial);
    }
    
    void deletePitTests(String serial)
    {
        String query = "DELETE FROM TEST_TABLE WHERE PIT_SERIAL = ?";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, serial);
            int rws = stmt.executeUpdate();
            System.out.println(rws+" tests deleted for pit: "+serial);
            conn.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    void writeLayerToDB(avscience.ppc.Layer layer, String serial) 
    {
        System.out.println("Write Layer to DB: for pit: "+serial);

        String query = "INSERT INTO LAYER_TABLE (START_DEPTH, END_DEPTH, LAYER_NUMBER, WATER_CONTENT, GRAIN_TYPE1, GRAIN_TYPE2, GRAIN_SIZE1, "
                + "GRAIN_SIZE2, GRAIN_SIZE_UNITS1, GRAIN_SIZE_UNITS2, GRAIN_SUFFIX1, GRAIN_SUFFIX2, HARDNESS1, HARDNESS2, HSUFFIX1, HSUFFIX2, "
                + "DENSITY1, DENSITY2, FROM_TOP, MULTIPLE_HARDNESS, MULTIPLE_DENSITY, MULTIPLE_GRAIN_SIZE, MULTIPLE_GRAIN_TYPE, PIT_SERIAL, "
                + "COMMENTS) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setDouble(1, layer.getStartDepth());
            stmt.setDouble(2, layer.getEndDepth());
            stmt.setInt(3, layer.getLayerNumber());
            stmt.setString(4, layer.getWaterContent());
            stmt.setString(5, layer.getGrainType1());
            stmt.setString(6, layer.getGrainType2());
            stmt.setDouble(7, layer.getGrainSize1_Dbl());
            stmt.setDouble(8, layer.getGrainSize2_Dbl());
            stmt.setString(9, layer.getGrainSizeUnits1());
            stmt.setString(10, layer.getGrainSizeUnits2());
            stmt.setString(11, layer.getGrainSuffix());
            stmt.setString(12, layer.getGrainSuffix1());
            
            stmt.setString(13, layer.getHardness1());
            stmt.setString(14, layer.getHardness2());
            
            stmt.setString(15, layer.getHSuffix1());
            stmt.setString(16, layer.getHSuffix2());
            
            stmt.setDouble(17, layer.getDensity1_Dbl());
            stmt.setDouble(18, layer.getDensity2__Dbl());
            
            int ftop = 0;
            if (layer.getFromTop()) ftop=1;
            stmt.setInt(19, ftop);
            
            int mltHrd=0;
            int mltGt=0;
            int mltGs=0;
            int mltRho=0;
            
            if (layer.getMultDensityBool()) mltRho=1;
            if (layer.getMultGrainSizeBool()) mltGs=1;
            if (layer.getMultGrainTypeBool()) mltGt=1;
            if (layer.getMultHardnessBool()) mltHrd=1;
            
            stmt.setInt(20, mltHrd);
            stmt.setInt(21, mltRho);
            stmt.setInt(22, mltGs);
            stmt.setInt(23, mltGt);
            stmt.setString(24, serial);
            stmt.setString(25, layer.getComments());
            
            int rw = stmt.executeUpdate();
            if (rw > 0 ) System.out.println("Layer added.");
            else System.out.println("Error adding layer");
            conn.close();

        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    public void writeOccToDB(String data) {
        System.out.println("writeOccToDB");
        avscience.ppc.AvOccurence occ = null;
        try
        {
            occ = new avscience.ppc.AvOccurence(data);
        }
        catch(Exception e)
        {
            occ = new avscience.ppc.AvOccurence();
            System.out.println(e.toString());
        }
        String pn = occ.getPitName();
        pn = removeDelims(pn);
        occ.setPitName(pn);

        if (occPresent(occ)) {
            System.out.println("Occ already in DB");
            if (occUnAltered(data)) {
                return;
            } else {
                System.out.println("Deleting occ..");
                deleteOcc(occ);
            }
        }

        System.out.println("writing occ to DB : " + occ.getPitName());
        String query = "INSERT INTO OCC_TABLE (OCC_DATA, OBS_DATE, TIMESTAMP, ELV_START, ELV_DEPOSIT, ASPECT, TYPE, TRIGGER_TYPE, TRIGGER_CODE, US_SIZE, CDN_SIZE, AVG_FRACTURE_DEPTH, MAX_FRACTURE_DEPTH, WEAK_LAYER_TYPE, WEAK_LAYER_HARDNESS, SNOW_PACK_TYPE, FRACTURE_WIDTH, FRACTURE_LENGTH, AV_LENGTH, AVG_START_ANGLE, MAX_START_ANGLE, MIN_START_ANGLE, ALPHA_ANGLE, DEPTH_DEPOSIT, LOC_NAME, LOC_ID, STATE, MTN_RANGE, LAT, LONGITUDE, NORTH, WEST, USERNAME, NAME, LOCAL_SERIAL, SHARE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.out.println("Connection null::");
            } else {
                PreparedStatement stmt = conn.prepareStatement(query);
                // String data = occ.dataString();
                //  data = URLEncoder.encode(data, "UTF-8");
                stmt.setString(1, data);
                String ser = occ.getSerial();
                System.out.println("Occ Serial: " + ser);
                String pdata = "";
                java.sql.Date odate = null;
                long otime = 0;
                avscience.ppc.PitObs pit = null;
                if ((ser != null) && (ser.trim().length() > 0)) {
                    System.out.println("getPitBySerial: " + ser);
                    pdata = getPitByLocalSerial(ser);
                    if ((pdata != null) && (pdata.trim().length() > 1)) {
                        System.out.println("getting pit by local serial.");
                        pit = new avscience.ppc.PitObs(pdata);
                        if (pit != null) {
                            otime = pit.getTimestamp();
                        }
                    } else {
                        System.out.println("Can't get Pit: " + ser + " by serial.");
                    }
                } else {
                    String wdata = getPit(pn);
                    //	= wpit.dataString();
                    if ((wdata != null) && (wdata.trim().length() > 1)) {
                        pit = new avscience.ppc.PitObs(wdata);
                    } else {
                        System.out.println("Can't get Pit: " + pn + " by Name.");
                    }
                }
                if (pit == null) {
                    System.out.println("Pit is null..");
                } 
                //else if (pit.getUser() == null) {
                //    System.out.println("Pit user is null.");
              //  }
                System.out.println("setting dates.");
                if (otime > 1) {
                    odate = new java.sql.Date(otime);
                } 
                System.out.println("Date: " + odate.toString());
                stmt.setDate(2, odate);
                ///stmt.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                stmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                //
                System.out.println("setting elevation.");
                int elvStart = 0;
                if (occ.getElvStart().trim().length() > 0) {
                    elvStart = stringToInt(occ.getElvStart());
                }
                if (pit.getPrefs().getElvUnits().equals("ft")) {
                    elvStart = ft_to_m(elvStart);
                }
                stmt.setInt(4, elvStart);
                System.out.println("setting elevation.");
                int elvDep = 0;
                if (occ.getElvDeposit().trim().length() > 0) {
                    elvDep = stringToInt(occ.getElvDeposit());
                }
                if (pit.getPrefs().getElvUnits().equals("ft")) {
                    elvDep = ft_to_m(elvDep);
                }
                stmt.setInt(5, elvDep);
                System.out.println("setting aspect");
                int aspect = 0;
                if (occ.getAspect().trim().length() > 0) {
                    aspect = stringToInt(occ.getAspect());
                }
                stmt.setInt(6, aspect);
                stmt.setString(7, occ.getType().trim());
                stmt.setString(8, occ.getTriggerType().trim());
                stmt.setString(9, occ.getTriggerCode().trim());
                stmt.setString(10, occ.getUSSize());
                String cs = occ.getCASize().trim();
                System.out.println("CASize: " + cs);
                if (cs.lastIndexOf('D') > -1) {
                    cs = cs.substring(1, cs.length());
                }
                System.out.println("cs: " + cs);
                float csize = 0;
                if (cs.trim().length() > 0) {
                    csize = (new Float(cs)).floatValue();
                }
                stmt.setFloat(11, csize);

                int aDepth = stringToInt(occ.getAvgFractureDepth());
                if (pit.getPrefs().getDepthUnits().equals("in")) {
                    aDepth = in_to_cm(aDepth);
                }
                stmt.setInt(12, aDepth);

                int mDepth = stringToInt(occ.getMaxFractureDepth());
                if (pit.getPrefs().getDepthUnits().equals("in")) {
                    mDepth = in_to_cm(mDepth);
                }
                stmt.setInt(13, mDepth);
                System.out.println("setting layer types");
                stmt.setString(14, occ.getWeakLayerType().trim());
                stmt.setString(15, occ.getWeakLayerHardness().trim());
                stmt.setString(16, occ.getSnowPackType().trim());

                int fw = stringToInt(occ.getFractureWidth());
                if (pit.getPrefs().getElvUnits().equals("ft")) {
                    fw = ft_to_m(fw);
                }
                stmt.setInt(17, fw);

                int fl = stringToInt(occ.getFractureLength());
                if (pit.getPrefs().getElvUnits().equals("ft")) {
                    fl = ft_to_m(fl);
                }
                stmt.setInt(18, fl);

                int al = stringToInt(occ.getLengthOfAvalanche());
                if (pit.getPrefs().getElvUnits().equals("ft")) {
                    al = ft_to_m(al);
                }
                stmt.setInt(19, al);
                System.out.println("setting angles");
                stmt.setInt(20, stringToInt(occ.getAvgStartAngle()));
                stmt.setInt(21, stringToInt(occ.getMaxStartAngle()));
                stmt.setInt(22, stringToInt(occ.getMinStartAngle()));
                stmt.setInt(23, stringToInt(occ.getAlphaAngle()));

                int d = stringToInt(occ.getDepthOfDeposit());
                if (pit.getPrefs().getElvUnits().equals("ft")) {
                    d = ft_to_m(d);
                }
                stmt.setInt(24, d);
                System.out.println("setting location");
                avscience.ppc.Location loc = pit.getLocation();
                String ln = loc.getName().trim();
                ln.replaceAll("'", "");
                stmt.setString(25, ln);
                stmt.setString(26, loc.getID().trim());
                stmt.setString(27, loc.getState().trim());
                stmt.setString(28, loc.getRange().trim());

                float lat = -999.9f;
                lat = stringToFloat(loc.getLat());
                stmt.setFloat(29, lat);
                float longitude = -999.9f;
                System.out.println("setting lat/lon.");
                longitude = stringToFloat(loc.getLongitude());
                stmt.setFloat(30, longitude);
                stmt.setBoolean(31, loc.getLatType().equals("N"));
                stmt.setBoolean(32, loc.getLongType().equals("W"));
                stmt.setString(33, pit.getUserHash().trim());
                pn = pit.getName().trim();
                pn.replaceAll("'", "");
                stmt.setString(34, pn);
                stmt.setString(35, occ.getSerial());
                boolean share = false;
                
                stmt.setBoolean(36, pit.getPrefs().getShare());
                System.out.println("executing occ update: ");
                stmt.executeUpdate();
                conn.close();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private int stringToInt(String s) {
        s = s.trim();
        System.out.println("stringToInt() " + s);
        int i = -999;
        if ((s != null) && (s.trim().length() > 0)) {
            try {
                // s = makeNumeric(s);
                if (s.trim().length() > 0) {
                    i = (new java.lang.Integer(s)).intValue();
                    i = java.lang.Math.abs(i);
                }
            } catch (Exception e) {
                System.out.println("stringToInt: " + e.toString());
            }
        }
        return i;
    }

    private float stringToFloat(String s) {
        s = s.trim();
        float f = -999.9f;
        try {
            if (s.trim().length() > 0) {
                f = (new java.lang.Float(s)).floatValue();
            }
        } catch (Exception e) {
            System.out.println("stringTofloat: " + e.toString());
        }
        return f;
    }

    private String makeNumeric(String s) {
        System.out.println("makeNumeric: " + s);
        int length = s.length();
        char[] chars = new char[length];
        Vector digs = new Vector();
        int j = 0;
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            Character C = new Character(c);
            if (Character.isDigit(C.charValue())) {
                digs.add(j, C);
                j++;
            }
        }
        length = digs.size();
        char[] newChars = new char[length];
        for (int i = 0; i < length; i++) {
            newChars[i] = ((Character) digs.elementAt(i)).charValue();
        }
        return new String(newChars);
    }

    private int in_to_cm(int in) {
        return (int) java.lang.Math.rint(in * 2.54);
    }

 /*   private boolean pitPresent(avscience.wba.PitObs pit) {
        System.out.println("pitPresent");
        String name = pit.getName();
        String user = pit.getUser().getName();
        if (name == null) {
            name = "";
        }
        if (user == null) {
            user = "";
        }
        String query = "SELECT * FROM PIT_TABLE WHERE PIT_NAME = ? AND USERNAME = ?";
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, user);
            ResultSet rs = stmt.executeQuery();
            conn.close();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }*/

    public void updateRanges() {
        String query = "SELECT SERIAL, PIT_DATA FROM PIT_TABLE";
        Statement stmt = null;
        String serial = "";
        //  String name = "";
        String data = "";
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                //   name = rs.getString("PIT_NAME");
                serial = rs.getString("SERIAL");
                data = rs.getString("PIT_DATA");
                avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
                String rng = pit.getLocation().getRange();
                System.out.println("Setting range for pit: " + serial + " to " + rng);
                try {
                    String q2 = "UPDATE PIT_TABLE SET MTN_RANGE = ? WHERE SERIAL = ?";
                    PreparedStatement stmt1 = conn.prepareStatement(q2);
                    stmt1.setString(1, rng);
                    stmt1.setString(2, serial);
                    stmt1.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

   /* public void updateObsTimes() {
        String query = "SELECT SERIAL, PIT_DATA FROM PIT_TABLE";
        Statement stmt = null;
        String serial = "";
        //  String name = "";
        String data = "";
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                serial = rs.getString("SERIAL");
                data = rs.getString("PIT_DATA");
                avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
                ////
                java.sql.Date pdate = null;
                long ptime = 0;
                ptime = pit.getTimestamp();
                if (ptime > 1) {
                    pdate = new java.sql.Date(ptime);
                } else {
                    String s = pit.getDateString();
                    System.out.println("datestring: " + s);
                    String dt = pit.getDate();
                    String tt = pit.getTime();

                    if (dt.trim().length() > 5) {
                        pdate = getDateTime(dt, tt);
                    } else {
                        pdate = getDate(s);
                    }
                    if (pdate == null) {
                        pdate = new java.sql.Date(System.currentTimeMillis());
                    }
                }

                ///////
                System.out.println("Setting obs_datetime for pit: " + serial);
                try {
                    String q2 = "UPDATE PIT_TABLE SET OBS_DATETIME = ? WHERE SERIAL = ?";
                    PreparedStatement stmt1 = conn.prepareStatement(q2);
                    Timestamp ots = new Timestamp(pdate.getTime());
                    stmt1.setTimestamp(1, ots);
                    stmt1.setString(2, serial);
                    stmt1.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }*/
    
    private String getDBSerial(avscience.ppc.PitObs pit) 
    {
        String name = pit.getName();
        String user = pit.getUserHash();
        String ser = pit.getSerial();
        return getDBSerial(name, user, ser);
    }
    
    private String getDBSerial(String name, String user, String ser) 
    {
        String serial = "";
        System.out.println("getDBSerial");
        String query = "SELECT * FROM PIT_TABLE WHERE PIT_NAME = ? AND USEHASH = ? AND LOCAL_SERIAL = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, user);
            stmt.setString(3, ser);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) serial = rs.getString("SYSTEM_SERIAL");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return serial;
    }

    private boolean pitPresent(avscience.ppc.PitObs pit) {
        System.out.println("pitPresent");
        String name = pit.getName();
        String user = pit.getUserHash();
        String ser = pit.getSerial();
        String query = "SELECT * FROM PIT_TABLE WHERE PIT_NAME = ? AND USERHASH = ? AND LOCAL_SERIAL = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, user);
            stmt.setString(3, ser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }

    private boolean occPresent(avscience.ppc.AvOccurence occ) {
        System.out.println("occPresent()");
        String name = occ.getPitName();
        String serial = occ.getSerial();
        if (name == null) {
            name = "";
        }
        if (serial == null) {
            serial = "";
        }

        String query = "SELECT * FROM OCC_TABLE WHERE NAME = ? AND LOCAL_SERIAL = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, serial);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }

    private float FtoC(float t) {
        t = t - 32;
        float c = t * (5 / 9);
        return c;
    }

    private int ft_to_m(int ft) {
        return (int) java.lang.Math.rint(ft / 3.29f);
    }

    /*public java.sql.Date getDateTime(String dt, String time) {
        String yr = "0";
        String mnth = "0";
        String dy = "0";
        String hr = "0";
        String min = "0";
        if (!(dt.trim().length() < 8)) {
            yr = dt.substring(0, 4);
            mnth = dt.substring(4, 6);
            dy = dt.substring(6, 8);
        }

        if (!(time.trim().length() < 4)) {
            hr = time.substring(0, 2);
            min = time.substring(2, 4);
        }

        int y = new java.lang.Integer(yr).intValue();
        int m = new java.lang.Integer(mnth).intValue() - 1;
        int d = new java.lang.Integer(dy).intValue();
        int h = new java.lang.Integer(hr).intValue();
        int mn = new java.lang.Integer(min).intValue();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(y, m, d, h, mn);
        long ts = cal.getTimeInMillis();
        System.out.println("date/time: " + new java.sql.Date(ts).toString());
        return new java.sql.Date(ts);
    }*/

    /*private java.sql.Date getDate(String date) {
        System.out.println("getDate(): " + date);
        //	if ( date == null ) return;
        String tm = "";
        date = date.trim();
        if (date.length() > 12) {
            tm = date.substring(11, date.length());
        }
        System.out.println("time: " + tm);
        if (date.length() > 10) {
            date = date.substring(0, 10);
        }
        long time = 0;
        int month = 0;
        int day = 0;
        int year = 0;
        Calendar cal = Calendar.getInstance();
        int start = 0;
        int end = 0;
        if (date.length() > 6) {
            end = date.indexOf("/");
            String m = date.substring(0, end);
            if ((m != null) && (m.trim().length() > 0)) {
                month = (new java.lang.Integer(m)).intValue();
            }
            start = end + 1;
            end = date.indexOf(" ", start);
            if (end > 1) {
                day = (new java.lang.Integer(date.substring(start, end))).intValue();
                year = (new java.lang.Integer(date.substring(date.length() - 4, date.length()))).intValue();
            } else {
                year = new java.lang.Integer(date.substring(date.length() - 4, date.length())).intValue();
                // month =  new java.lang.Integer(date.substring(4, 6)).intValue();
                day = new java.lang.Integer(date.substring(3, 5)).intValue();
            }
        }
        int hr = 0;
        int mn = 0;
        int sc = 0;

        if (tm.trim().length() > 6) {
            try {
                String h = "";
                String min = "";
                String s = "";
                start = 0;
                end = tm.indexOf(":", start);
                if (end > 0) {
                    h = tm.substring(start, end);
                }
                start = end + 1;
                end = tm.indexOf(":", start);
                if (end > 0) {
                    min = tm.substring(start, end);
                }
                start = end + 1;
                s = tm.substring(start, start + 2);

                String ap = tm.substring(tm.length() - 2, tm.length());

                hr = new java.lang.Integer(h).intValue();
                if (ap.equals("PM")) {
                    hr += 12;
                }
                mn = new java.lang.Integer(min).intValue();
                sc = new java.lang.Integer(s).intValue();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        month -= month;
        if (month > 11) {
            month = 11;
        }
        cal.set(year, month, day, hr, mn, sc);
        time = cal.getTimeInMillis();

        System.out.println("date/time: " + new java.sql.Date(time).toString());
        return new java.sql.Date(time);
    }

    public String cleanString(String s) {
        if (s.trim().length() < 2) {
            return s;
        }

        try {
            char[] chars = s.toCharArray();
            int l = chars.length;

            for (int jj = 0; jj < l; jj++) {
                int idx = jj;
                //logger.println("idx: "+idx);
                if ((idx < l) && (idx > 0)) {
                    char test = chars[idx];
                    if (test <= 0) {
                        chars[idx] = ' ';
                    }
                }

            }
            String tmp = "";
            tmp = new String(chars);
            if ((tmp != null) && (tmp.trim().length() > 5)) {
                s = tmp;
            }
        } catch (Throwable e) {
            System.out.println("cleanString failed: " + e.toString());
        }
        return s;

    }*/

    public String[][] getPitListArray(boolean datefilter) {
        Vector serials = new Vector();
        Vector names = new Vector();
        String query = "SELECT CROWN_OBS, TIMESTAMP, PIT_NAME, SHARE, SYSTEM_SERIAL FROM PIT_TABLE WHERE SHARE > 0 ORDER BY TIMESTAMP DESC";
        Statement stmt = null;
        Connection conn;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("TIMESTAMP");
                if ((datefilter) && (pitDate.after(startDate))) {
                    if (!rs.getBoolean("CROWN_OBS")) {
                        String serial = rs.getString("SYSTEM_SERIAL");
                        String name = rs.getString("PIT_NAME");
                        serials.insertElementAt(serial, i);
                        names.insertElementAt(name, i);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        String[][] list = new String[2][serials.size()];

        int i = 0;
        Iterator e = serials.iterator();
        Iterator ee = names.iterator();
        while (e.hasNext()) {
            String ser = (String) e.next();
            String nm = (String) ee.next();
            list[0][i] = nm;
            list[1][i] = ser;
            i++;
        }
        return list;
    }

    public String[][] getPitListArrayFromQuery(String whereclause, boolean datefilter) {
        System.out.println("getPitListArrayFromQuery()   " + whereclause);
        Vector serials = new Vector();
        Vector names = new Vector();
        String query = "SELECT CROWN_OBS, OBS_DATETIME, PIT_NAME, SERIAL ,SHARE FROM PIT_TABLE " + whereclause + " AND SHARE > 0 ORDER BY OBS_DATETIME DESC";
        System.out.println("Query:  " + query);
        Statement stmt = null;
        Connection conn;
        int i = 0;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATETIME");
                if (datefilter && (pitDate.after(startDate))) {
                    if (!rs.getBoolean("CROWN_OBS")) {
                        String serial = rs.getString("SYSTEM_SERIAL");
                        String name = rs.getString("PIT_NAME");
                        serials.insertElementAt(serial, i);
                        names.insertElementAt(name, i);
                        i++;
                    }
                } else if (!datefilter) {
                    if (!rs.getBoolean("CROWN_OBS")) {
                        String serial = rs.getString("SYSTEM_SERIAL");
                        String name = rs.getString("PIT_NAME");
                        serials.insertElementAt(serial, i);
                        names.insertElementAt(name, i);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        String[][] list = new String[2][serials.size()];

        i = 0;
        Enumeration e = serials.elements();
        Enumeration ee = names.elements();
        while (e.hasMoreElements()) {
            String ser = (String) e.nextElement();
            String nm = (String) ee.nextElement();
            list[0][i] = nm;
            list[1][i] = ser;
            i++;
        }
        return list;
    }

    public Hashtable getAllPits() {
        System.out.println("getAllPits()");
        Hashtable v = new Hashtable();
        String query = "SELECT SYSTEM_SERIAL, PIT_DATA FROM PIT_TABLE";
        try {
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String dat = rs.getString("PIT_DATA");
                String serial = rs.getString("SYSTEM_SERIAL");
                if ((dat != null) && (dat.trim().length() > 5)) {
                    v.put(serial, dat);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("No of PITS: " + v.size());
        return v;

    }

    public Vector getPitListFromQuery(String whereclause) throws Exception {

        System.out.println("DAO pitlist query");
        Vector v = new Vector();
        String query = "SELECT PIT_NAME, TIMESTAMP, SHARE FROM PIT_TABLE " + whereclause + " AND SHARE > 0 ORDER BY TIMESTAMP DESC";
        System.out.println("QUERY:: " + query);
        Statement stmt = null;
        whereclause = "";
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATETIME");
                // if (pitDate.after(startDate)) {
                String s = rs.getString(1);
                v.add(s);
                // }
            }
        } catch (Exception e) {
            whereclause = e.toString();
            System.out.println(e.toString());
            throw e;

        }

        return v;
    }

    public Vector getOccListFromQuery(String whereclause) throws Exception {
        System.out.println("DAO occlist query");
        Vector v = new Vector();
        String query = "SELECT NAME, TIMESTAMP, SHARE FROM OCC_TABLE " + whereclause + " AND SHARE > 0 ORDER BY TIMESTAMP";
        System.out.println("QUERY: " + query);
        Statement stmt = null;
        whereclause = "";
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            whereclause = e.toString();
            System.out.println(e.toString());
            throw e;

        }
        return v;
    }

    public String[][] getOccListArrayFromQuery(String whereclause, boolean datefilter) throws Exception {
        System.out.println("DaO occlist query " + whereclause);
        Vector names = new Vector();
        Vector serials = new Vector();
        String query = "SELECT NAME, TIMESTAMP, OBS_DATE, SERIAL, SHARE FROM OCC_TABLE " + whereclause + "  AND SHARE > 0 ORDER BY TIMESTAMP";
        System.out.println("QUERY: " + query);
        Statement stmt = null;
        whereclause = "";
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                java.util.Date date = rs.getDate("OBS_DATE");
                if ((datefilter) && (date.after(startDate))) {
                    String serial = "" + rs.getInt("SERIAL");
                    String name = rs.getString("NAME");
                    serials.insertElementAt(serial, i);
                    names.insertElementAt(name, i);
                    i++;
                } else if (!datefilter) {
                    String serial = "" + rs.getInt("SERIAL");
                    String name = rs.getString("NAME");
                    serials.insertElementAt(serial, i);
                    names.insertElementAt(name, i);
                    i++;
                }
            }
        } catch (Exception e) {
            whereclause = e.toString();
            System.out.println(e.toString());
            throw e;

        }
        System.out.println(serials.size() + " OCCs retieved.");
        String[][] list = new String[2][serials.size()];

        int i = 0;
        Enumeration e = serials.elements();
        Enumeration ee = names.elements();
        while (e.hasMoreElements()) {
            String ser = (String) e.nextElement();
            String nm = (String) ee.nextElement();
            list[0][i] = nm;
            list[1][i] = ser;
            i++;
        }
        return list;
    }

    public String[][] getOccListArray(boolean datefilter) {
        Vector names = new Vector();
        Vector serials = new Vector();
        String query = "SELECT NAME, TIMESTAMP, SERIAL, OBS_DATE, SHARE FROM OCC_TABLE WHERE SHARE > 0 ORDER BY TIMESTAMP";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                java.util.Date date = rs.getDate("OBS_DATE");
                if ((datefilter) && (date.after(startDate))) {
                    String serial = "" + rs.getInt("SERIAL");
                    String name = rs.getString("NAME");
                    serials.insertElementAt(serial, i);
                    names.insertElementAt(name, i);
                    i++;
                } else if (!datefilter) {
                    String serial = "" + rs.getInt("SERIAL");
                    String name = rs.getString("NAME");
                    serials.insertElementAt(serial, i);
                    names.insertElementAt(name, i);
                    i++;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        String[][] list = new String[2][serials.size()];

        int i = 0;
        Enumeration e = serials.elements();
        Enumeration ee = names.elements();
        while (e.hasMoreElements()) {
            String ser = (String) e.nextElement();
            String nm = (String) ee.nextElement();
            list[0][i] = nm;
            list[1][i] = ser;
            i++;
        }
        return list;
    }

    public Vector getOccList() {
        Vector v = new Vector();
        String query = "SELECT NAME, TIMESTAMP, SHARE FROM OCC_TABLE WHERE SHARE > 0 ORDER BY TIMESTAMP";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public Vector getLocationList() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT LOC_NAME FROM PIT_TABLE ORDER BY LOC_NAME";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public Vector getRangeList() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT MTN_RANGE, OBS_DATETIME FROM PIT_TABLE ORDER BY MTN_RANGE ASC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATETIME");
                if (pitDate.after(startDate)) {
                    String s = rs.getString(1);
                    s = s.trim();
                    if ((s.trim().length() > 0) && (!(v.contains(s)))) {
                        v.add(s);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public Vector getRangeListAll() 
    {
        Vector v = new Vector();
        String query = "SELECT DISTINCT MTN_RANGE FROM PIT_TABLE ORDER BY MTN_RANGE ASC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                s = s.trim();
                if ((s.trim().length() > 0) && (!(v.contains(s)))) {
                    v.add(s);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }
    
    public Vector getStateList() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT STATE, OBS_DATETIME FROM PIT_TABLE ORDER BY STATE ASC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                java.util.Date pitDate = rs.getDate("OBS_DATETIME");
                if (pitDate.after(startDate)) {
                    String s = rs.getString(1);
                    s = s.trim();
                    if ((s.trim().length() > 0) && (!(v.contains(s)))) {
                        v.add(s);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    ////////////////
    public Vector getStateListAll() {
        Vector v = new Vector();
        String query = "SELECT DISTINCT STATE FROM PIT_TABLE ORDER BY STATE ASC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
              //  java.util.Date pitDate = rs.getDate("OBS_DATE");
                String s = rs.getString(1);
                s = s.trim();
                if ((s.trim().length() > 0) && (!(v.contains(s)))) {
                    v.add(s);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }
    ////////////////////

    public Vector getPitList(String user) {
        Vector v = new Vector();
        String query = "SELECT PIT_NAME, OBS_DATETIME FROM PIT_TABLE WHERE USERHASH ='" + user + "' AND SHARE > 0 ORDER BY OBS_DATETIME DESC";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public Vector getOccList(String user) {
        Vector v = new Vector();
        String query = "SELECT NAME, TIMESTAMP FROM OCC_TABLE WHERE USERNAME ='" + user + "' ORDER BY TIMESTAMP";
        Statement stmt = null;
        try {
            stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String s = rs.getString(1);
                v.add(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    public void deletePit(String dbserial) {
        String query = "DELETE FROM PIT_TABLE WHERE SYSTEM_SERIAL = " + dbserial;
        try {
            Statement stmt = getConnection().createStatement();
            stmt.executeUpdate(query);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        deletePitLayers(dbserial);
        deletePitTests(dbserial);
    }

    public boolean deleteOcc(String user, String serial, String name) {
        System.out.println("deleteOcc. " + serial + " " + name);
        boolean del = false;
        String query = null;
        if ((serial != null) && (serial.trim().length() > 1)) {
            query = "DELETE FROM OCC_TABLE WHERE LOCAL_SERIAL = '" + serial + "' AND USERNAME = '" + user + "'";

        } else {
            query = "DELETE FROM OCC_TABLE WHERE PIT_NAME = '" + name + "' AND USERNAME = '" + user + "'";
        }

        try {
            Statement stmt = getConnection().createStatement();
            int n = stmt.executeUpdate(query);
            if (n > 0) {
                System.out.println("OCC deleted: " + serial);
                del = true;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        //  deletePit(user, serial, name);
        return del;
    }

    private void deleteOcc(avscience.ppc.AvOccurence occ) {
        System.out.println("DELETE OCC. ");
        String name = occ.getPitName();
        String serial = occ.getSerial();
        System.out.println("OCC SERIAL: " + serial);
        avscience.ppc.PitObs pit = null;

        if ((serial == null) || (serial.trim().length() < 2)) {
            System.out.println("getting pit by name: " + name);
            String data = getPit(name);
            try
            {
                pit = new avscience.ppc.PitObs(data);
            }
            catch(Exception ex)
            {
                System.out.println(ex.toString());
                return;
            }
            
          //  name = pit.getDBName();
        } else {
            System.out.println("getting pit by serial: " + serial);
            String data = getPitByLocalSerial(serial);
            try
            {
                pit = new avscience.ppc.PitObs(data);
            }
            catch(Exception ex)
            {
                System.out.println(ex.toString());
                return;
            }
        }
        String user = pit.getUserHash();
        deleteOcc(user, serial, name);
    }

    public String getPit(String name) {
        System.out.println("DAO: getting WBA pit: " + name);
        //  avscience.wba.PitObs pit = null;
        String s = "";
        String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE PIT_NAME = ?";
        PreparedStatement stmt = null;
        if ((name != null) && (name.trim().length() > 0)) {
            try {
                stmt = getConnection().prepareStatement(query);
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    System.out.println("pit in DB");

                    s = rs.getString("PIT_DATA");
                    if (s != null) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return s;
    }

    public String getPitByLocalSerial(String ser) {
        System.out.println("getPitByLocalSerial()");
        String s = "";
        String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE LOCAL_SERIAL = ?";
        PreparedStatement stmt = null;
        try {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, ser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("PIT in DB");

                s = rs.getString("PIT_DATA");
                // System.out.println("Data: "+s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return s;
    }

    public String getPPCOcc(String serial) {
        System.out.println("DAO: getting PPC occ: # " + serial);
        // avscience.ppc.PitObs pit = null;
        String query = "SELECT OCC_DATA FROM OCC_TABLE WHERE SERIAL = ?";
        PreparedStatement stmt = null;
        String s = "";
        if ((serial != null) && (serial.trim().length() > 0)) {
            int ser = 0;
            ser = new java.lang.Integer(serial).intValue();
            try {
                stmt = getConnection().prepareStatement(query);
                stmt.setInt(1, ser);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    System.out.println("OCC in DB");
                    //	System.out.println("Data: "+s);
                    s = rs.getString("OCC_DATA");
                } else {
                    System.out.println("PPC OCC query failed:.");
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return s;
    }
    
    public String getPitXML(String ser)
    {
        String query = "SELECT PIT_XML FROM PIT_TABLE WHERE SYSTEM_SERIAL = ?";
        PreparedStatement stmt = null;
        String xml = "none";
         try {
                stmt = getConnection().prepareStatement(query);
                stmt.setString(1, ser);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) 
                {
                    xml = rs.getString("PIT_XML");
                }
               }
         catch(Exception e)
         {
             System.out.println(e.toString());
         }
         return xml;
    }

    public String getPPCPit(String serial) {
        System.out.println("DAO: getting PPC pit: # " + serial);

        // avscience.ppc.PitObs pit = null;
        String query = "SELECT PIT_DATA FROM PIT_TABLE WHERE SYSTEM_SERIAL = ?";
        PreparedStatement stmt = null;
        String s = "";
        if ((serial != null) && (serial.trim().length() > 0)) {
            try {
                stmt = getConnection().prepareStatement(query);
                stmt.setString(1, serial);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    System.out.println("pit in DB");
                    //	System.out.println("Data: "+s);
                    s = rs.getString("PIT_DATA");
                    System.out.println("PIT Data: " + s);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        //System.out.println("Data: " + s);
        return s;
    }

    public avscience.ppc.AvOccurence getOcc(String name) {
        System.out.println("DAO: getting occ: " + name);
        avscience.ppc.AvOccurence occ = null;
        String query = "SELECT OCC_DATA FROM OCC_TABLE WHERE NAME = ?";
        PreparedStatement stmt = null;
        if ((name != null) && (name.trim().length() > 0)) {
            try {
                stmt = getConnection().prepareStatement(query);
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("occ in DB.");
                    String data = rs.getString("OCC_DATA");
                    if ((data != null) && (data.trim().length() > 0)) {
                        occ = new avscience.ppc.AvOccurence(data);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return occ;
    }

    /*public void tallyTests() {
        System.out.println("TallyTests()");
        String[] queries = new String[10];
        String[] years = {"2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012"};
        String[] types = ShearTests.getInstance().getShearTestDescriptions();
        int[] total = new int[years.length];
        int[][] testTotal = new int[years.length][types.length];
        int[] prof = new int[years.length];
        int[] testPits = new int[years.length];
        int[] ectNotes = new int[years.length];
        int[] pstNotes = new int[years.length];

        queries[0] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2003-01-01' AND OBS_DATE < '2004-01-01' ORDER BY OBS_DATE DESC";
        queries[1] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2004-01-01' AND OBS_DATE < '2005-01-01' ORDER BY OBS_DATE DESC";
        queries[2] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2005-01-01' AND OBS_DATE < '2006-01-01' ORDER BY OBS_DATE DESC";
        queries[3] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2006-01-01' AND OBS_DATE < '2007-01-01' ORDER BY OBS_DATE DESC";
        queries[4] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2007-01-01' AND OBS_DATE < '2008-01-01' ORDER BY OBS_DATE DESC";
        queries[5] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2008-01-01' AND OBS_DATE < '2009-01-01' ORDER BY OBS_DATE DESC";
        queries[6] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2009-01-01' AND OBS_DATE < '2010-01-01' ORDER BY OBS_DATE DESC";
        queries[7] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2010-01-01' AND OBS_DATE < '2011-01-01' ORDER BY OBS_DATE DESC";
        queries[8] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2011-01-01' AND OBS_DATE < '2012-01-01' ORDER BY OBS_DATE DESC";
        queries[9] = "SELECT SERIAL, OBS_DATE FROM PIT_TABLE WHERE OBS_DATE >= '2012-01-01' ORDER BY OBS_DATE DESC";

        for (int i = 0; i < queries.length; i++) {
            System.out.println("Getting tests for query: " + queries[i]);
            Statement stmt = null;
            try {
                stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(queries[i]);

                while (rs.next()) {
                    String s = rs.getString("SERIAL");
                    String data = getPPCPit(s);
                    avscience.ppc.PitObs pit = new avscience.ppc.PitObs(data);
                    if (pit.getPitNotes().contains("ECT")) {
                        ectNotes[i]++;
                    }
                    if (pit.getPitNotes().contains("PST")) {
                        pstNotes[i]++;
                    }
                    Enumeration tests = pit.getShearTests();
                    if (tests != null) {

                        if (tests.hasMoreElements()) {
                            testPits[i]++;
                            try {
                                avscience.ppc.User u = pit.getUser();
                                boolean prf = u.getProf();
                                if (prf) {
                                    prof[i]++;
                                }
                            } catch (Exception e) {
                                System.out.println(e.toString());
                            }

                        }
                    }

                    for (int j = 0; j < types.length; j++) {
                        tests = pit.getShearTests();
                        if (tests != null) {
                            while (tests.hasMoreElements()) {
                                avscience.ppc.ShearTestResult result = (avscience.ppc.ShearTestResult) tests.nextElement();
                                String cd = result.getCode();
                                String type = ShearTests.getInstance().getShearTestByCode(cd).getType();
                                if (types[j].equals(type)) {
                                    testTotal[i][j]++;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        System.out.println("Writing test tally results to file:");
        FileOutputStream out = null;
        PrintWriter writer = null;
        File file = new File("TestSummaryByYear.txt");
        try {
            out = new FileOutputStream(file);
            writer = new PrintWriter(out);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(" ,");

        for (int i = 0; i < years.length; i++) {
            buffer.append(years[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        buffer.append("Pits with tests: ,");

        for (int i = 0; i < testPits.length; i++) {
            buffer.append(testPits[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        buffer.append("Pits by professional: ,");

        for (int i = 0; i < prof.length; i++) {
            buffer.append(prof[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        for (int i = 0; i < types.length; i++) {
            buffer.append(types[i]);
            buffer.append(",");
            for (int j = 0; j < years.length; j++) {
                buffer.append(testTotal[j][i]);
                buffer.append(",");
            }
            buffer.append("\n");
        }
        buffer.append(",");
        for (int i = 0; i < total.length; i++) {
            buffer.append(total[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        buffer.append("ECT in notes,");
        for (int i = 0; i < ectNotes.length; i++) {
            buffer.append(ectNotes[i]);
            buffer.append(",");
        }
        buffer.append("\n");

        buffer.append("PST in notes,");
        for (int i = 0; i < pstNotes.length; i++) {
            buffer.append(pstNotes[i]);
            buffer.append(",");
        }
        buffer.append("\n");
        try {
            writer.print(buffer.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }*/
}