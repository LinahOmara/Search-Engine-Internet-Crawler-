package APT;

/**
 *
 * @author Linah
 */
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class AptFinal {
    
    /**
     * @param args
     *            the command line arguments
     */
    public static DB db = new DB();
    public static void main(String[] args) throws Exception {
        
        // TODO code application logic here
        
      /*  String URLs[] = { "http://www.water.org", "http://www.dmoz.org/",
            "http://www.microsoft.com", "http://www.mit.edu",
            "http://www.wikipedia.org"};
        
        System.out.println("Enter number of threads : ");
        Scanner scanner= new Scanner(System.in);
        int threadsNumber = scanner.nextInt();
        System.out.println("Enter '1' for Crawling OR '2' for Recrawling");
        Scanner scanner1 = new Scanner(System.in);
        int CrawlOrRecrawl = scanner1.nextInt();
        
        System.out.println(CrawlOrRecrawl);
        
        
        /* * ================================================================
        * ========== // ==================== Crawl
        * ============================== //
        * ================================
        * ========================================== // All crawl work is
        * done by calling crawl object 
        CrawlerFinal crawler = new CrawlerFinal(URLs, CrawlOrRecrawl); // set initial thread to use its name in recrawling
        Thread t1 = new Thread(crawler);
        t1.setName("Thread1"); t1.start(); // start threads loop from index 1 , as we have previously created 1 // thread
        for (int i =1; i < threadsNumber; i++) {
            Thread t = new Thread(crawler);
            t.start();
        }
        */
        // ==========================================================================
        // ==================== Indexer ==============================
        // ==========================================================================
        APT_Indexer ni = new APT_Indexer();
        // ResultSet feha kol html files
        int finalArray[] = new int[ni.keywordsIndex.size()];
                String rs = "SELECT URL, htmlDocument FROM  record  WHERE ( visited =1 AND CAST(htmlDocument AS VARCHAR(128)) != '<null>')";
                
                ResultSet rs2 = db.runSql(rs);
                String html_name = "";
                int vd = 0;
                while (rs2.next()) {
                    html_name = rs2.getString("URL");
                    String html_doc = rs2.getString("htmlDocument");

                    // take html file and convert it to text file after removing
                    // all special characters
                    String File = ni.extractText(html_doc);
                    // load words in text file and calculate occurrence of every
                    // word in this file then put them in Map
                    HashMap<String,ArrayList<Occurrence>> KeywordsInFile = ni.loadKeyWords(File, html_name);
                    // sort keywords and put them in Hashmap keywords_index
                    ni.mergeKeyWords(KeywordsInFile);
                    System.out.println(ni.keywordsIndex.size());
                    // make hashMap : key(htmlfile) --> value (text file)
                } //
                //Statement stmtdoc2 = db.conn.createStatement();
                //String sqldoc = "UPDATE `record` SET `htmlDocument`=\"null\" WHERE `URL`=\""
                //+ html_name + "\"";
                //stmtdoc2.executeUpdate(sqldoc);
                //stmtdoc2.close();
                //rs2.close();
                Statement stmt = db.conn.createStatement();
                for (String key : ni.keywordsIndex.keySet()) {
                    for (int x = 0; x < ni.keywordsIndex.get(key).size(); x++) {
                        String sql = "INSERT INTO INDEXER (WORD, PAGES, TF, IDF, \"TYPE\", \"COUNT\")VALUES('"
                                +key+"','"
                                + ni.keywordsIndex.get(key).get(x).document+"',"
                                + ni.keywordsIndex.get(key).get(x).TotalCount+","
                                + ni.keywordsIndex.get(key).size()+ ",'" 
                                + ni.keywordsIndex.get(key).get(x).type +"',"
                                + ni.keywordsIndex.get(key).get(x).frequency+")";
                        stmt.executeUpdate(sql);
                        System.out.println("Key: " + key);
                    }
                }
                stmt.close();
                db.conn.close();
        
    }
    // catch ( SQLException | IOException e ){
    // System.out.println("Exception here ");}
    // ==========================================================================
    // ==================== Query Process ==============================
    // ==========================================================================
    // QueryProcessor myQueryProcessor = new QueryProcessor();
    
}
