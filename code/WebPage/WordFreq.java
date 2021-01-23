package WebPage;


public class WordFreq implements Comparable<WordFreq> {
	

    public int freq;
    public String name;
 

    public int compareTo(WordFreq o) {
       return this.freq - o.freq;
    }
 


}
