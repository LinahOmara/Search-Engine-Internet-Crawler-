# Search-Engine-Internet-Crawler-
* Ranker Algorithm:
  
- In case of query search:
For each word retrieve IDF, TF, no. of in links (popularity) and its position in document (title, body or header) from database 
Ranker equation: in links/5 * TF *IDF * (10 if title, 6 for header and 2 for body)

- In case of phrase search:
Calculate TF for the phrase in each document retrieved by Query processor and retrieve in links 
Ranker equation: in links * TF 


- How to run my code:
-First run the servlet ...open the browser...then enter the word in the search box ...
then wait until the urls appear.
