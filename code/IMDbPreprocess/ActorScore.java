package IMDbPreprocess;


public class ActorScore implements Comparable<ActorScore> {
	

    public Integer	score;
    public String	 nameString;
 

    public int compareTo(ActorScore o) {
       if(this.score> o.score)
    	   return 1;
       else {
		return -1;
	}
    }
 


}