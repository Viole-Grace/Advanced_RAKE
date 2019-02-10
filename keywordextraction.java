import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.Scanner;
import java.util.*;

import java.io.FileInputStream; 
import java.io.InputStream;  

import opennlp.tools.postag.POSModel; 
import opennlp.tools.postag.POSSample; 
import opennlp.tools.postag.POSTaggerME; 
import opennlp.tools.tokenize.WhitespaceTokenizer; 

// Keyword Extraction is done using a techdnique called RAKE - Rapid Automatic Keyword Extraction. Then stem, lemmitize and tag words.
public class keywordextraction
{
	static Scanner sc=new Scanner(System.in);
    String language;
    String stopWordsPattern;

    keywordextraction(String language) 
    {
        this.language = language;
        InputStream stream = this.getClass().getResourceAsStream(language+".txt");
        //System.out.println("\n\n\nStream : "+stream);
        String line;

        if (stream != null) 
        {
            try 
            {
                ArrayList<String> stopWords = new ArrayList<>();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                while ((line = bufferedReader.readLine()) != null)
                    stopWords.add(line.trim());
                ArrayList<String> regexList = new ArrayList<>();
                for (String word : stopWords) 
                {
                    String regex = "\\b" + word + "(?![\\w-])";
                    regexList.add(regex);
                }
                this.stopWordsPattern = String.join("|", regexList);
            }
            catch (Exception e) 
            {
               throw new Error("An error occurred reading stop words for language " + language);
            }
        }
        else throw new Error("Could not find stop words required for language " + language);

    }
    private String[] getSentences(String text) 
    {
        return text.split("[.!?,;:\\t\\\\\\\\\"\\\\(\\\\)\\\\'\\u2019\\u2013]|\\\\s\\\\-\\\\s");
    }
    private String[] separateWords(String text, int size) 
    {
        String[] split = text.split("[^a-zA-Z0-9_\\\\+/-\\\\]");
        ArrayList<String> words = new ArrayList<>();

        for (String word : split) 
        {
            String current = word.trim().toLowerCase();
            int len = current.length();
            if (len > size && len > 0 && !current.contains("U+"))//unicode digits ;  there are 610 patterns and all start with 'U+'
                words.add(current);
        }

        return words.toArray(new String[words.size()]);
    }
    private String[] getKeywords(String[] sentences) 
    {
        ArrayList<String> phraseList = new ArrayList<>();

        for (String sentence : sentences) 
        {
            String temp = sentence.trim().replaceAll(this.stopWordsPattern, "|");
            String[] phrases = temp.split("\\|");

            for (String phrase : phrases) 
            {
                phrase = phrase.trim().toLowerCase();

                if (phrase.length() > 0)
                    phraseList.add(phrase);
            }
        }

        return phraseList.toArray(new String[phraseList.size()]);
    }
    private LinkedHashMap<String, Double> calculateWordScores(String[] phrases) 
    {
        LinkedHashMap<String, Integer> wordFrequencies = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> wordDegrees = new LinkedHashMap<>();
        LinkedHashMap<String, Double> wordScores = new LinkedHashMap<>();

        for (String phrase : phrases) 
        {
            String[] words = this.separateWords(phrase, 0);
            int length = words.length;
            int degree = length - 1;

            for (String word : words) 
            {
                wordFrequencies.put(word, wordDegrees.getOrDefault(word, 0) + 1);
                wordDegrees.put(word, wordFrequencies.getOrDefault(word, 0) + degree);
            }
        }

        for (String item : wordFrequencies.keySet()) 
        {
            wordDegrees.put(item, wordDegrees.get(item) + wordFrequencies.get(item));
            wordScores.put(item, wordDegrees.get(item) / (wordFrequencies.get(item) * 1.0));
        }

        return wordScores;
    }
    private LinkedHashMap<String, Double> getCandidateKeywordScores(String[] phrases, LinkedHashMap<String, Double> wordScores) 
    {
        LinkedHashMap<String, Double> keywordCandidates = new LinkedHashMap<>();
        for (String phrase : phrases) 
        {
            double score = 0.0;

            String[] words = this.separateWords(phrase, 0);

            for (String word : words) 
            {
                score += wordScores.get(word);
            }

            keywordCandidates.put(phrase, score);
        }

        return keywordCandidates;
    }
    private LinkedHashMap<String, Double> sortHashMap(LinkedHashMap<String, Double> map) 
    {
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        List<Map.Entry<String, Double>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(list);

        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext(); ) 
        {
            Map.Entry<String, Double> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
    //This function calls all other functions and is hence the mergepoint of the algorithm.
    public LinkedHashMap<String, Double> getKeywordsFromText(String text) 
    {
        String[] sentences = this.getSentences(text);
        String[] keywords = this.getKeywords(sentences);

        LinkedHashMap<String, Double> wordScores = this.calculateWordScores(keywords);
        LinkedHashMap<String, Double> keywordCandidates = this.getCandidateKeywordScores(keywords, wordScores);

        return this.sortHashMap(keywordCandidates);
    }

    //Remove the static void main method if youre implementing this as a part of a module, else : include the entire code as a standalone module class
    public static void main(String Args[])throws IOException
    {
    	String user_text;
    	System.out.println("Enter A text block to find the keywords in it : ");
    	user_text=sc.nextLine();
    	LinkedHashMap<String, Double> key_eval = new LinkedHashMap<String, Double>();
    	System.out.println("Enter language : ");
    	String lg=sc.nextLine();
    	keywordextraction obj=new keywordextraction(lg);
    	key_eval=obj.getKeywordsFromText(user_text);
    	String answer[]=new String[key_eval.size()];    	
    	Set<String> keys = key_eval.keySet();
    	//System.out.println("Number of keywords : "+key_eval.size());
    	int sz=key_eval.size()/5;
    	//System.out.println("Keywords : ");
    	int count=key_eval.size();
    	for(String k:keys)
	    	answer[--count]=k;
	    String answers[]=new String[10];
	    int getval=0;
    	for(int i=0;i<key_eval.size();i++)
    	{
    		if(answer[i].length() !=1 && i%sz==0)
    			{
    				//System.out.print("'"+answer[i]+"' ");
    				answers[getval++]=answer[i];
    			}
    		//custom bubble sort
    	}
    	String temp="";
    	for(int i=0;i<getval;i++)
    	{
    		for(int j=0;j<getval-i-1;j++)
    		{
    			if(answers[j].length()<answers[j+1].length())
    			{
    				temp=answers[j];
    				answers[j]=answers[j+1];
    				answers[j+1]=temp;
    			}
    		}
    	}
    	System.out.println("\nKeywords with priority list : "); //These are the keywords that need to be shown to the user.

    	
    	for(int i=0;i<getval;i++)
    		System.out.print("'"+answers[i]+"' ");

    	
    	//System.out.println("\nAll Key Words have been printed.");

    	InputStream inputStream = new FileInputStream("en-pos-maxent.bin"); 
	    POSModel model = new POSModel(inputStream); 
	    POSTaggerME tagger = new POSTaggerME(model);        
	    //String sentence = ;
	       
	      //Tokenizing the sentence using WhitespaceTokenizer class  
	    
	    /*
	    WhitespaceTokenizer whitespaceTokenizer= WhitespaceTokenizer.INSTANCE; 
	    String[] tokens = whitespaceTokenizer.tokenize(answer); 
	    */

	      //Generating tags 
	    String[] tags = tagger.tag(answer);      
	      //Instantiating the POSSample class 
	    POSSample sample = new POSSample(answer, tags); 
	    
	    /*
	    System.out.println("\n");
	    System.out.println(sample.toString());
	    System.out.println("\n\nPOS tagging DONE.");
	    */

	    //better to see only required tags

	    String[] x = sample.toString().split(" ");
	    ArrayList<String> list = new ArrayList<String>();  

	    for(int i=0;i<x.length;i++)
	    {
	    	if(!(x[i].substring(x[i].lastIndexOf("_")+1).contains("NNP")) && !(x[i].substring(x[i].lastIndexOf("_")+1).contains("NNS")))
	    	{
		        if (x[i].substring(x[i].lastIndexOf("_")+1).contains("NN")) //|| x[i].substring(x[i].lastIndexOf("_")+1).contains("JJ"))
		        {
		            list.add(x[i].split("_")[0]);
		        }
	    	}
	    }
	    //list has all tagged elements now, get all tagged elements from the list 
	    if(list.size()<=6)
		    for(int i=0;i<list.size();i++)
		    {
		        System.out.println(list.get(i));
		    }
		else
	    	System.out.println("'"+list.get(0)+"' , '"+list.get(1)+"' , '"+list.get(2)+"' , '"+list.get(list.size()/2 -1)+"' , '"+list.get(list.size()/2 +1)+"' , '"+list.get(list.size()-1)+"' , '"+list.get(list.size()-2)+"'");
    }

}