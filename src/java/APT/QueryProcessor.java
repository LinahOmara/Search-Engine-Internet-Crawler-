
package APT;



import static com.trigonic.jrobotx.Constants.USER_AGENT;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.el.stream.Stream;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class QueryProcessor {
        // stemming
     static Porter               myStem     = new Porter    ();
     static Map<String, Double> Pages       = new HashMap <>();
     static Map<String, String> PagesWithDoc= new HashMap <>();
     static Ranker myRanker                 = new Ranker    ();
public static DB db                         = new DB        ();
//==========================================================================
//====================     Query Processing   ==============================
//==========================================================================
     public static Map<String,Double>  DO_QueryProcess(String keyWord )
             throws SQLException, IOException{
/*
  Input Parameters: 
       1- keyWord : is the word(s) entered by the user, to search 
  TODO: 
       1- Steam the word, the user is searching "or Words"
       2- Retreave all the urls in which this word appears in
       3- Call the ranker,to return the rank of each URL 
       4- Sort these URLs by highest rank
       5- Send the sorted list to the web interface to display the results
*/
         String [] keyWordsArray ;
         String temp ;
         keyWordsArray =  keyWord.split(" ");
         String [] Word_Steamed  = new String [keyWordsArray.length] ;
         for (int i =0 ; i < keyWordsArray.length ; i ++ ){                                  
                                                                                //1- Steam the word, the user is searching "or Words"
         
         keyWordsArray[i] = keyWordsArray[i].replaceFirst("[^a-zA-Z]+","");     //Remove any special characters from each word
         temp = keyWordsArray[i];//
         Word_Steamed[i] =  myStem.stripAffixes( temp);
         String getURLquery = "select * from Indexer where word ='"+Word_Steamed[i]+"'";
	     ResultSet rs3  = db.runSql(getURLquery);
	     double rank; 
	     while (rs3.next()) {         
            //  3- Call ranker 
         //   rank =0;
            rank = Ranker.RankMe(Word_Steamed ,rs3.getString("pages"));
          //  System.out.println(rs3.getString("pages") + rank);
            Pages.put(rs3.getString("pages"),rank);

		 }
         }
         //4- Sort these URLs by highest rank
	 Pages =  sortByValue(Pages);
         //5- Get the doc of each url to send it to the servlet 
         for (Map.Entry<String, Double> entry : Pages.entrySet())
        {
//            Connection connection = Jsoup.connect(entry.getKey()).userAgent(USER_AGENT);
  //          Document doc = connection.ignoreHttpErrors(true).timeout(10 * 1000).get();
    //        String URL_TEXT = doc.text();
          //   String query4  = "select htmlDocument from Record where URL ='"+entry.getKey()+"'"; 
    	    // ResultSet rs5  = db.runSql( query4); 
	     //while (rs5.next()) 
             //{
            //    Document html = Jsoup.parse(rs5.getString("htmlDocument")); //a lazmto ???
                // put all doc n string 
              //  String  body  = html.body().text();
          //       PagesWithDoc.put(entry.getKey(),URL_TEXT);
             //}
        }
         return Pages;
    }
    //====================     Map Sorting DESCENDINGLY ========================
 public static <K, V extends Comparable<? super V>> Map<K, V> 
        sortByValue( Map<K, V> map )
    {
         List<Map.Entry<K, V>> list =new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
    
    //==========================================================================
    //====================     Phrase Searching   ==============================
    //==========================================================================
     public static Map<String, String> DO_PhraseSearching(String keyWord) throws SQLException{
          
/*
  Input Parameters: 
       1- keyWord : is the word(s) entered by the user, to search 
  TODO: 
       1- Steam the 1st word, the user is searching  
       2- Retreave all the urls in which this word appears in
         * check in each URL if the whole phrase exist , if YES->add to MAP
       3- Call the ranker,to return the rank of each URL 
       4- Sort these URLs by highest rank
       5- Retrive each doc and insert it in a new map 
       6- Send the new map to the web interface to display the results
*/
        // keyWord = keyWord.replaceFirst("[^a-zA-Z]+",""); 
         keyWord = keyWord.substring(1, keyWord.length()-1);                    //To remove The ""
         String [] keyWordsArray ; 
         keyWordsArray =  keyWord.split(" ");
         String   Word_Steamed    ;
                                 
                                                                                //1- Steam the word, the user is searching "or Words"
         
         keyWordsArray[0] = keyWordsArray[0].replaceFirst("[^a-zA-Z]+","");     //Remove any special characters from each word
         Word_Steamed =  myStem.stripAffixes( keyWordsArray[0]);
         String getURLquery     = "select * from Indexer where word =  "+"\""   // 2- Retreave all the urls in which this word appears in
                                   + Word_Steamed +"\"";
	 ResultSet rs1          = db.runSql(getURLquery);
	 while (rs1.next()) 
         {
            System.out.println(rs1.getString("pages"));
            System.out.println(keyWord);
            String  query   = "SELECT * FROM record WHERE htmlDocument LIKE '% "
                              + keyWord +" %' and URL = "   +"\""
                              +rs1.getString("pages")       +"\"";              // and check if the whole phrase exist or not :D 
            ResultSet rs2          = db.runSql(query);
            double rank; 
            while (rs2.next()) 
            { 
                rank = Ranker.RankMePhrase(keyWord ,rs2.getString("URL"));      // IF YES ->  Call ranker (Phrase , url)

                Pages.put(rs2.getString("URL"),rank);                           // Add this url and Frinal rank to the map
            
            }
         }
          //4- Sort these URLs by highest rank
	 Pages =  sortByValue(Pages);
         //5- Get the doc of each url to send it to the servlet 
         for (Map.Entry<String, Double> entry : Pages.entrySet())
        {
             String query4  = "select htmlDocument from Record where URL ='"
                                + entry.getKey() +"'"; 
    	     ResultSet rs5  = db.runSql( query4); 
	     while (rs5.next()) 
             {
                 PagesWithDoc.put(entry.getKey(),rs5.getString("htmlDocument"));
             }
        }

         return PagesWithDoc;
     }
}
