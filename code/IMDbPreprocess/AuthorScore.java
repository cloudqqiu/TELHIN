package IMDbPreprocess;


public class AuthorScore implements Comparable<AuthorScore> {
	

    public double	score;
    public Integer id;
    public String	 nameString;
 

    public int compareTo(AuthorScore o) {
       if((this.score - o.score)>0.000000001)
    	   return 1;
       else {
		return -1;
	}
    }
 


}
