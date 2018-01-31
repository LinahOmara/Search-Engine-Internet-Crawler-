/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APT;

import static com.trigonic.jrobotx.Constants.USER_AGENT;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Main extends HttpServlet {
PrintWriter out ;
 public static DB db = new DB();
            String word = "";
  static Map<String, Double> Page  = new HashMap<>();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
          response.setContentType("text/html");
           out = response.getWriter();
           word = request.getParameter("word");
      //    String searchString = getSearchResult(word);
       //    response.getWriter().write(searchString);
          out.print("<html><body background=\"1.jpg\"> <center><br> <br> <br> <br> <br> <br>");    
       out.print("<form action=\"Main\" method=\"POST\">"+
          "<input type=\"text\" name=\"word\" value=\""+word+"\" style=\"height:40px;width:700px;border-radius: 4px;border: 2px solid #fff\"  >"+
        "<button type=\"submit\" value=\"Submit\" style=\"height:40px;width:100px;background: rgba(255, 255, 255, 0); border-radius: 4px;\n" +
"  border: 2px solid #fff\" >Search</button> </form>");
     out.print("</center><br> <br>");
  out.print("<div><h1 style=font-size: 600%>");
            QueryProcessor myQueryprocessor = new QueryProcessor();
            String reader=null;//dah ely han5do mn lina
            String body;
            String[] bodyArray;
     if ( word.startsWith("\"") && word.endsWith("\"")){
              try {
                  myQueryprocessor.DO_PhraseSearching(word);
              for (String key: Page.keySet()){
                out.println("<a href="+key+">"+key+"</a><br>");
               // out.println("des : "+Jsoup.parse(Page.get(key)).body().toString());
            org.jsoup.Connection connection = Jsoup.connect(key).userAgent(USER_AGENT);
            Document doc = connection.ignoreHttpErrors(true).timeout(10 * 1000).get();
            String URL_TEXT = doc.text();
                bodyArray = URL_TEXT.split(" ");     
                for(int y=0;y<bodyArray.length;y++)
                {
                    bodyArray[y] = bodyArray[y].replaceFirst("[^a-zA-Z]+","");
                    
                    if(bodyArray[y].toLowerCase().equals(word))
                    {
                       out.println(URL_TEXT.substring(0,50)+"<br>");
                    break;
                    }
                }  
            }
              } catch (SQLException ex) {
                  Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
              }
          out.println("Double qoutes exist ");
       }else{
         try {
            Page = myQueryprocessor.DO_QueryProcess(word);
            for (String key: Page.keySet()){
                out.println("<a href="+key+">"+key+"</a><br>");
               // out.println("des : "+Jsoup.parse(Page.get(key)).body().toString());
            org.jsoup.Connection connection = Jsoup.connect(key).userAgent(USER_AGENT);
            Document doc = connection.ignoreHttpErrors(true).timeout(10 * 1000).get();
            String URL_TEXT = doc.text();
                bodyArray = URL_TEXT.split(" ");     
                for(int y=0;y<bodyArray.length;y++)
                {
                    bodyArray[y] = bodyArray[y].replaceFirst("[^a-zA-Z]+","");
                    
                    if(bodyArray[y].toLowerCase().equals(word))
                    {
                       out.println(URL_TEXT.substring(0,50)+"<br>");
                    break;
                    }
                }  
            }
          } catch (SQLException ex) {
              Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
              out.println("damm");
          }
     } 
        out.print("</h1></div></body></html>");
    }
public String getSearchResult(String search) {
String finalSearch = "";
try{
String s = "SELECT suggest FROM  SuggestTable WHERE suggest like'"+search+"%'ORDER BY suggest";
ResultSet rs = db.runSql(s);

if(!rs.next()){
     String s2 = "INSERT INTO SuggestTable (Suggest) VALUES ('"+search+"')";
     Statement st2 = db.conn.createStatement();
     st2.executeUpdate(s2);
            out.print("yees");
}
    while (rs.next())
    {
        String un = rs.getString("Suggest");
        finalSearch+= un+"\n";
    }
}catch(Exception e){}
return finalSearch;
}

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       doGet(request, response);
    }
}
