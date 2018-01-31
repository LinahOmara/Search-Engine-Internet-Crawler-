package APT;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.trigonic.jrobotx.RobotExclusion;
import java.io.Reader;
import java.io.StringReader;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static java.lang.System.out;
import java.sql.PreparedStatement;
/**
 *
 * @author Linah
 */
public class CrawlerFinal implements Runnable {
    
 
    public static DB db = new DB();
    static boolean setRecrawl = false;
    public int CrawlOrRecrawl;
    static int Max_RecordID = 0;
    static int Max_Record_in_DB = 2000;
    static boolean[] Finish = { false, false, false, false, false };
    public int FinishCount;
    public static CyclicBarrier barrier;
    static boolean              StopAdd = false;
    static int                  Max_ID_recrawl = 0;
    static int                  crawel;
    final static Object         monitor = new Object();
    final static Object         monitor2 = new Object();
    private final static Object lock1 = new Object();
    private final static Object lock2 = new Object();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    
    public CrawlerFinal(String[] URLs , int x) throws SQLException,
            IOException {
        // ADD initial URLs to DB
        crawel=x;
        System.out.println(CrawlOrRecrawl);
//		  db.runSql2("TRUNCATE Record;"); // Empty the data base
//                  db.runSql2("TRUNCATE Indexer;"); // Empty the data base
if (x == 1) {
    System.out.println("am here ");
    for (int i = 0; i < URLs.length; i++) {
        checkAndAdd(URLs[i]);
    }
}

    }
    /* public void SetCrawlOrRecrawl(int Crawl ){
    CrawlOrRecrawl =Crawl;
    
    }
    public int GetCrawlerRecrawl(){
    return this.CrawlOrRecrawl;
    }*/
    public boolean IsFinished() {
        for (int i = 0; i < Finish.length; i++) {
            if (Finish[0] == false)
                return false;
            
        }
        return true;
    }
    
    public void run() {
        while (!IsFinished()) {
            System.out.println("crawl = "+ crawel);
            if (crawel == 2){
                try {
                    UpdateDBToRecrawl();
                    
                } catch (SQLException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            
            String URLbackFromDB;
            try {
                URLbackFromDB = GetFromDB();
                if (URLbackFromDB == "") {
                    synchronized (monitor) {
                        try {
                            System.out.println("i'm thread"
                                    + Thread.currentThread().getId()
                                    + "i will wait");
                            monitor.wait();
                            System.out.println("i'm thread"
                                    + Thread.currentThread().getId()
                                    + "i'm back");
                            URLbackFromDB = GetFromDB();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                parseURL(URLbackFromDB);
                try {
                    FinishVisitingOrGetMaximum();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                
                e.printStackTrace();
            }
            
        }
        // System.out.println(Finish);
    }
    public synchronized void UpdateDBToRecrawl() throws SQLException, IOException{
        System.out.println("Im in Recrawl "+Thread.currentThread().getId());
        System.out.println("My name is  "+Thread.currentThread().getName());
        
        
        //if (Thread.currentThread().getName()== "Thread1")
        if (! setRecrawl){
            System.out.println("Im Thread1 in  Recrawl if cond "+Thread.currentThread().getId());
            String query5 = "select recordID from Record where recordID = ( SELECT MAX(recordID) FROM Record )";
            ResultSet rs5 = db.runSql(query5);
            if (rs5.next()) {
                Max_ID_recrawl = rs5.getInt("recordID") / 2;
            }
            String query6 = "select * FROM record WHERE count > (SELECT MAX( count) FROM record ) /2  ORDER BY count DESC fetch first '"+Max_ID_recrawl +"' rows only";
            ResultSet rs6 = db.runSql(query6);
            String Update_URL ;
            while (rs6.next()) {
                Update_URL = rs6.getString("URL");
                Statement Update_updated_stmts = db.conn.createStatement();
                String sqlUpdate_updated = "UPDATE Record SET updated = 1   WHERE URL = '" + Update_URL + "'";
                Update_updated_stmts.executeUpdate(sqlUpdate_updated);
                
                Statement Update_visited_stmts = db.conn.createStatement();
                String sqlUpdate_visited = "UPDATE Record SET   visited = 0  WHERE URL = '" + Update_URL + "'";
                Update_visited_stmts.executeUpdate(sqlUpdate_visited);
                
            }
            setRecrawl = true;
        }
        
    }
    public synchronized String GetFromDB() throws SQLException, IOException {
        String LeastValue = "";
        Statement stmt8 = db.conn.createStatement();
        String query2 = "select URL from Record where visited = 0 Fetch first row only";
        ResultSet rs3 = stmt8.executeQuery(query2);
        while (rs3.next()) {
            LeastValue = rs3.getString("URL");
        }
        String LeastValueForQuery = "'" + LeastValue + "'";
        Statement stmt3 = db.conn.createStatement();
        String query3 = "UPDATE Record SET visited = 1 WHERE URL = " +LeastValueForQuery ;
        stmt3.executeUpdate(query3);
        return LeastValue;
        
    }
    
    public void FinishVisitingOrGetMaximum() throws SQLException, IOException,
            InterruptedException, BrokenBarrierException {
        String query = "select recordID from Record where recordID = ( SELECT MAX(recordID) FROM Record ) AND visited = 0";
        ResultSet rs2 = db.runSql(query);
        while (rs2.next()) {
            Max_RecordID = rs2.getInt("recordID");
            if (Max_RecordID > Max_Record_in_DB)
                synchronized (lock1) {
                    StopAdd = true;
                }
        }
        
        String FinishVisiting = "select URL from Record where visited = 0";
        ResultSet rs4 = db.runSql(FinishVisiting);
        System.out.println("await synchronized"
                + Thread.currentThread().getId());
        if (!rs4.next()) {
            synchronized (lock2) {
                System.out.println("await " + Thread.currentThread().getId());
                // barrier.await();
                Finish[FinishCount] = true;
                FinishCount++;
                
            }
        }
        
    }
    
    public static void checkAndAdd(String URL) throws SQLException, IOException {
        
        // ==============================================================================
        // ==============================CHECK==========================================
        // ==============================================================================
        // ==============Chack For Robot.txt
        
        try {
            URL urLink = new URL(URL);
            RobotExclusion robotExclusion = new RobotExclusion();
            if (robotExclusion.allows(urLink, USER_AGENT)) {
                // This function process the URL ,
                // i.e.:Remove http and https to know if the URL was parssed
                // before
                // or
                // not
                // __________ Remove (http:// & https:// ) from
                // URL____________________
                String newURL = URL, FinalURL = "";
                int count = 9;
                int startIndex1 = URL.indexOf("https://");
                int startIndex2 = URL.indexOf("http://");
                int endIndex = URL.lastIndexOf('/');
                // System.out.println("Start1 " + startIndex1 + "and Start 2 = "
                // + startIndex2 + "endIndex =" + endIndex);
                
                // ____________________IF URL ends With a "/"
                // ____________________
                int length = URL.length() - 1; // get lenght of the original URL
                // System.out.println("Len = " + length);
                if (endIndex == length && length > 0) {
                    newURL = URL.substring(0, length);
                    // System.out.println("Now URL is  = " + newURL);
                }
                
                // ____________________ if https://
                // existed______________________________
                if (startIndex1 == 0) {
                    newURL = newURL.substring(8);
                } else if (startIndex2 == 0) {
                    newURL = newURL.substring(7);
                }
                // System.out.println(newURL);
                
                FinalURL = "http://" + newURL;
                
                // =============Now We have the URL ready for DataBase :D
                // =======================
                // ==============================================================================
                // ==============================ADD=============================================
                // ==============================================================================
                
                // __________check if the given URL is already in
                // database__________________
                synchronized (monitor2) {
                    String sql = "select * from Record where URL = '"+ FinalURL +"'";
                    ResultSet rs = db.runSql(sql);
                    if (rs.next()) {
                        // ____________________HERE if found in
                        // DB______________________________
                        // _____________________INC
                        // Count____________________________________
                        Statement stmts = null;
                        String query = "select count from Record where URL = '"
                                + FinalURL + "'";
                        ResultSet rs9 = db.runSql(query);
                        while (rs9.next()) {
                            count = rs9.getInt("count");
                            // System.out.println("Count = " + count);
                        }
                        count++;
                        // _______________ Save new count to database
                        // ________________________
                        stmts = db.conn.createStatement();
                        String sqlUpdate = "UPDATE Record " + "SET count = "
                                + count + "WHERE URL = '" + FinalURL + "'";
                        stmts.executeUpdate(sqlUpdate);
                        // System.out.println("Count = " + count);
                        
                    } else {
                        // __________ Here if URL not found in the database
                        // ____________________
                        // _______________ Check if URL is HTML
                        // _____________________________
                        System.out.println(FinalURL);
                        
                        String FinalURL_Type = getHeaderType(FinalURL);
                        System.out.println(FinalURL_Type);
                        // System.out.println(FinalURL);
                        // System.out.println(FinalURL_Type);
                        // _____________if type != HTML => Dont
                        // Enter_______________
                        
                        if (FinalURL_Type != null
                                && FinalURL_Type.indexOf("html") != -1) {
                           
                            // _______________store the URL to database
                            // __________________
                            sql = "INSERT INTO  Record (URL) VALUES " + "(?)";
                            PreparedStatement stmt = db.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                            stmt.setString(1, FinalURL);

                            stmt.execute();
                            
                        }
                    }
                }
            }
            
        } catch (MalformedURLException e) {
            System.out.println("The URL is not valid.");
            System.out.println("URL : " + URL);
        }
    }
    
    public static void parseURL(String FinalURL) throws IOException,
            SQLException {
        // Inputs : Given URL
        /*
        * // TODO : 1- Open URL by Jsoup 2- Get the page text and add to DB (
        * html_file) 3- Get all URLs in the page Loop on them & Call =>
        * CheckAndALL(foundURL[i])
        */
        
        System.out.println("i'm thread" + Thread.currentThread().getId());
        System.out.println(FinalURL);
        
        // __________________ Open URL by Jsoup
        // _________________________________
        // Document doc = Jsoup.connect(FinalURL).timeout(10 * 1000).get();
        Connection connection = Jsoup.connect(FinalURL).userAgent(USER_AGENT);
        Document doc = connection.ignoreHttpErrors(true).timeout(10 * 1000).get();
        if (connection.response().statusCode() == 200) // 200 is the HTTP OK
            // status cod //
            // indicating that
            // everything is great.
        {
            
            // ________ Get the page text and add to DB ( html_file)
            // _______________
            System.out.print("hyd5ol");
            Statement stmtdoc = db.conn.createStatement();
            // System.out.println(doc.text());
            String URL_TEXT = doc.html();
            //System.out.println(URL_TEXT);
            String URL_Final_TEXT = URL_TEXT.replaceAll("[']", ""); // Remove
            URL_Final_TEXT = URL_Final_TEXT.replaceAll("[!]", ""); 
            // (')
            // ________________ save page body in
            // DB__________________________________
            System.out.println(URL_Final_TEXT.length());
            String sqldoc = "UPDATE Record SET htmlDocument = (?) , DocumentLength = "+ URL_Final_TEXT.length()+" WHERE URL = '" + FinalURL + "'";
            Reader r = new StringReader(URL_Final_TEXT); 
            if (URL_Final_TEXT != null){
            PreparedStatement stmt = db.conn.prepareStatement(sqldoc, Statement.RETURN_GENERATED_KEYS);
            stmt.setCharacterStream(1, r, URL_Final_TEXT.length());          
             //System.out.println(sqldoc);
            stmt.executeUpdate();
            }

            // System.out.println(FinalURL);
            // ________________Get all URLs in the page and
            // LOOP_____________________
            Elements links_in_page = doc.select("a[href]");
            if (!StopAdd) {
                
                for (Element link : links_in_page) {
                    checkAndAdd(link.attr("abs:href"));
                    
                }
                synchronized (monitor) {
                    monitor.notifyAll();
                    System.out.println(Thread.currentThread().getId());
                }
                
            }
        }
        
    }
    
    public static String getHeaderType(String my_url) throws IOException {
        URL url = new URL(my_url);
        URLConnection u = url.openConnection();
        String url_type = u.getHeaderField("Content-Type");
        // System.out.println(url_type.indexOf("html"));
        /*
        * if(!connection.response().contentType().contains("text/html")) {
        * System
        * .out.println("**Failure** Retrieved something other than HTML");
        * return false; }
        */
        return url_type;
        
    }
}
