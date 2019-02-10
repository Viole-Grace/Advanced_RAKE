import java.io.FileInputStream; 
import java.io.InputStream;  

import opennlp.tools.postag.POSModel; 
import opennlp.tools.postag.POSSample; 
import opennlp.tools.postag.POSTaggerME; 
import opennlp.tools.tokenize.WhitespaceTokenizer;  

public class postag { 
  
   public static void main(String args[]) throws Exception{ 
    
      //Loading Parts of speech-maxent model       
      InputStream inputStream = new FileInputStream("en-pos-maxent.bin"); 
      POSModel model = new POSModel(inputStream); 
       
      //Instantiating POSTaggerME class 
      POSTaggerME tagger = new POSTaggerME(model);        
      String sentence = "Zinta received the Filmfare Award for Best Actress in 2003 for her performance in the drama Kal Ho Naa Ho. She went on to play the lead female role in two consecutive annual top-grossing films in India, the science fiction film Koi... Mil Gaya (2003), which is her biggest commercial success,[4] and the star-crossed romance Veer-Zaara (2004), which earned her critical acclaim. She was later noted for her portrayal of independent, modern Indian women in Salaam Namaste (2005) and Kabhi Alvida Naa Kehna (2006), top-grossing productions in overseas markets"; 
       
      //Tokenizing the sentence using WhitespaceTokenizer class  
      WhitespaceTokenizer whitespaceTokenizer= WhitespaceTokenizer.INSTANCE; 
      String[] tokens = whitespaceTokenizer.tokenize(sentence);        
      //Generating tags 
      String[] tags = tagger.tag(tokens);      
      //Instantiating the POSSample class 
      POSSample sample = new POSSample(tokens, tags); 
      System.out.println(sample.toString()); 
   
   } 
} 