package APT;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class Ranker {
	public static DB db = new DB();
	public static int numOfDocuments = 0;

	public static double RankMe (String words[] , String URL)throws SQLException, IOException{
		double rank = 0;
		double IDF = 0;
		double TF = 0 ;
		double inLinks = 0;
		double RankOfType = 0;
		
		Statement stmt = (Statement) db.conn.createStatement();
		String query1 = "SELECT COUNT(*) as count FROM record";
		ResultSet rs1 = db.runSql(query1);
		while (rs1.next()) {
			numOfDocuments = rs1.getInt("count");			
		}
		
		for (int i=0;i<words.length;i++){
			String query5 = "select *from indexer where word ='" + words[i] + "' and pages = '" + URL +"'";
			ResultSet rs5 = db.runSql(query5);
			while (rs5.next()){
				 TF  =(double)rs5.getInt("TF"); // TF not normalized 
				 IDF =(double)numOfDocuments  / (double)rs5.getInt("IDF");
				 if (rs5.getString("type").equals("title"))
					 RankOfType = 10;
				 else if (rs5.getString("type").equals("header"))
					 RankOfType = 6;
				 else
					 RankOfType = 2;
				 System.out.println(TF);
				 System.out.println(IDF);
			}
			String query6 = "select * from record where URL ='" + URL + "'";
			ResultSet rs6 = db.runSql(query6);
			while (rs6.next()){
				inLinks = rs6.getInt("count");
				System.out.println(inLinks);
			}
			rank += IDF * RankOfType * TF * (inLinks /5);
		}	
		return rank;			
	}
	public static double RankMePhrase(String phrase , String url) throws SQLException{
		double rank = 0 ;
		double inLinks = 0;
		int lastIndex = 0;
		int TF = 0;
		
		String query6 = "select * from record where URL ='" + url + "'";
		ResultSet rs6 = db.runSql(query6);
		while (rs6.next()){
			inLinks = rs6.getInt("count");
			System.out.println(inLinks);
		}		
		while(lastIndex != -1){
		    lastIndex = rs6.getString("htmlDocument").indexOf(phrase,lastIndex);
		    if(lastIndex != -1){
		        TF ++;
		        lastIndex += phrase.length();
		    }
		}	
		rank = TF * inLinks;
		return rank;
		
	}
}
