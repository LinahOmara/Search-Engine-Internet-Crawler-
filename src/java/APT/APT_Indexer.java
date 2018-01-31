/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APT;
import java.io.*;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
/**
 *
 * @author Linah
 */
class Occurrence {
	 // Document in which a keyword occurs. 
    String document;
	 // The frequency (number of times) the keyword occurs in the above document.	
    int frequency;
    String type;
    int TotalCount;
    
    	 // Initializes this occurrence with the given document,frequency pair.
    public Occurrence(String doc, int freq, String Type , int totalCount) {
	document = doc;
	frequency = freq;
        type = Type;
        TotalCount = totalCount;
    }

    Occurrence(ArrayList<Occurrence> get) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public String toString() {
	return "(" + document + "," + frequency + "," +type + ","+ TotalCount +")";
    }
    public static Comparator<Occurrence> COMPARE_BY_FREQUENCY = new Comparator<Occurrence>() {
	public int compare(Occurrence one, Occurrence other) {
       	    Integer x = one.frequency;
            Integer y = other.frequency;
            return x.compareTo(y);
        }
    };

    void add(Occurrence get) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }
public class APT_Indexer {
    // Stemming
      Porter f=new Porter();
    //KeywordsInFile : [key (word)] --> [value (doc_name, freq )] 
    public HashMap<String,ArrayList<Occurrence>> keywordsIndex;

    public APT_Indexer() {
	keywordsIndex = new HashMap<>(1000,2.0f);
    }
    
    
public HashMap<String,ArrayList<Occurrence>> loadKeyWords(String File,String html_file) throws FileNotFoundException {
   HashMap<String, ArrayList<Occurrence>> fin = new HashMap<>(1000,2.0f);
    String Type = null;
    String[] finalArray = File.split(" ");
   
    for (String nu : finalArray) {
      if ("title".equals(nu)) {
         Type = "title";
      } else if ("header".equals(nu)) {
          Type = "header";
      } else if ("body".equals(nu)) {
          Type = "body";
      }else{
          // if word not equal null
          if (nu != null) 
          {
              //if word already exists in map
            if (fin.containsKey(nu))
            {
            	int count =0;
            	ArrayList<Occurrence> ge = fin.get(nu);
                // check on the size of array of ge 
                // every time inc totalcount of word and in case of this word aleardy exits in array inc the freq of this index
                // if not inc count 
            	for(int x=0 ; x<ge.size() ; x++){
            		if(ge.get(x).type.equals(Type)){
            			ge.get(x).frequency++;    
            		}else{
            			count++;
            		}
               	    ge.get(x).TotalCount++;
                    fin.put(nu, ge);
            	}
                // if this type didn't have place in array then in for loop every time we will in count and count become equal
                // size of array . So we will create new row in array for this type and add them in map
            	if(count == ge.size()){
                    ge.add(new Occurrence(html_file,1,Type,ge.get(0).TotalCount));
                    fin.put(nu, ge);
            	}
            }else {// lw el word mknt4 mwgoda fi el map
                // if word didn't exist before we will create an arraylist for it and add this array in map
            	ArrayList<Occurrence> ge = new ArrayList<>();
            	ge.add(new Occurrence (html_file,1,Type,1));
            	fin.put(nu, ge);
            }
          }
      }
   }
  return fin;
}

public void mergeKeyWords(HashMap<String,ArrayList<Occurrence>> kws) {
	for (String key: kws.keySet()) {// b3ml string key w h loop 3la el map w b7ot fi el (key) de el key bta3t el Map             
            if(keywordsIndex.containsKey(key)==false)// h4of eh key (word) de mwgoda fi el Hashmap el kbera wla
            {  
	        keywordsIndex.put(key, kws.get(key) );
	    }else{// lw el word kant already mwgoda fi el HashMap	 
	      ArrayList<Occurrence> ni = keywordsIndex.get(key);// hgeb el value elli already mwgoda fi el key(word) de mn el hashmap el kbera w a7otha fi arraylist ni
              int num_add = kws.get(key).size(); // size bta3 el array elli hdefo
              
              for (int t=0; t<num_add ; t++){ // b3ml add ll arraylist bta3 el word ll array list el adema
                 ni.add(kws.get(key).get(t));
              }
       //     Collections.sort(ni, Occurrence.COMPARE_BY_FREQUENCY);// b3ml sorting ll arraylist 
              keywordsIndex.put(key, ni );// b3d keda b7ot el key w el value(arraylist)el gdeda b3d el update fi el HashMap el kbera (b3ml update ll hashmap)
	    }
        }
    }

public String extractText(String reader) throws IOException {
    StringBuilder sb = new StringBuilder();
    StringBuilder Body = new StringBuilder();
    String headers,body,title;
    String[] titleArray = null,bodyArray ,headerArray = null ;
   
   // save the string in document to get headers and title 
    Document html = Jsoup.parse(reader); 
    // put all doc n string 
    body = html.text();
    System.out.print(body);
    title = html.title();   // title
    headers = html.body().select("h1,h2,h3,h4,h5,h6").text(); //headers
   body.replace(title, "");
    // remove headers from body 
    //for(int c =0 ; c<headerArray.length ; c++){
     //   System.out.println(headers.length());

        
//}
    bodyArray = body.split(" ");
    
    // write header & body words  in string
    sb.append("title");
    sb.append(" ");
    
    if(title.length()>0){
       titleArray = title.split(" ");//split the line to separated words
    for ( int i=0 ; i<titleArray.length ;i++){
      //byshel special characters mn kol klma 
        titleArray[i] = titleArray[i].replaceAll(".,-"," ");
        titleArray[i] = titleArray[i].replaceAll("[^a-zA-Z]+","");
	//lw feh satr fady byshelo
	if(!"".equals(titleArray[i])){
            titleArray[i]=  f.stripAffixes(titleArray[i]);
            sb.append(titleArray[i].toLowerCase());
            sb.append(" ");
            }
      }
    }
     sb.append("header");
    sb.append(" ");
    if(headers.length()>0){
         headerArray = headers.split(" ");
    for ( int i=0 ; i<headerArray.length ;i++){
      //byshel special characters mn kol klma 
        headerArray[i] = headerArray[i].replaceAll("[^a-zA-Z]+","");
	//lw feh satr fady byshelo
	if(!"".equals(headerArray[i])){
             headerArray[i]=  f.stripAffixes(headerArray[i]);
            sb.append(headerArray[i].toLowerCase());
            sb.append(" ");
            }
          }
    }
    sb.append("body");
    sb.append(" ");
    for ( int i=0 ; i<bodyArray.length ;i++){
      //byshel special characters mn kol klma 
        bodyArray[i] = bodyArray[i].replaceAll("[^a-zA-Z]+","");
	//lw feh satr fady byshelo
	if(!"".equals(bodyArray[i])){
             bodyArray[i]=  f.stripAffixes(bodyArray[i]);
            sb.append(bodyArray[i].toLowerCase());
            sb.append(" ");
            }
    }

//lazem a7wl el buffer l string 3shan hwa no3 a3la shwya
// string Y da feh el words kol word fi satr
    String Y = sb.toString();
  return Y;
}
	 
/*public static void main(String[] args) throws Exception  {
             
    APT_Indexer ni = new APT_Indexer();
    Statement st = db.conn.createStatement();
    ResultSet rs = st.executeQuery("SELECT `html_file` FROM `record` WHERE 1");
    while (rs.next()){
	String htmlfile = rs.getString("html_file");
	FileReader reader = new FileReader(htmlfile);
	//take html file and convert it to text file after removing all special characters
	String File = ni.extractText(reader);
	//load words in text file and calculate occurrence of every word in this file then put them in Map 
	HashMap<String,ArrayList<Occurrence>> KeywordsInFile = ni.loadKeyWords(File,htmlfile);
	// sort keywords and put them in Hashmap keywords_index 
	ni.mergeKeyWords(KeywordsInFile);
    }
    rs.close();
    st.close();
	// make hashMap : key(htmlfile) --> value (text file)
    Statement stmt= db.conn.createStatement();;
    int word_id = 1 ;
    for(String key: ni.keywordsIndex.keySet()){	
        for(int x=0; x <ni.keywordsIndex.get(key).size(); x++){
           String sql = "INSERT INTO `indexer`(`word`, `pages`, `count`, `word_id`) VALUES('"+key+"','"+ni.keywordsIndex.get(key).get(x).document+"','"+ni.keywordsIndex.get(key).get(x).frequency+"','"+word_id+"')" ;
	  stmt.executeUpdate(sql);
	    System.out.println("Key: " + key);
	    System.out.println("Value: " + ni.keywordsIndex.get(key));
        }       
        word_id++;
    }
    stmt.close();
    db.conn.close();
 }*/
}
