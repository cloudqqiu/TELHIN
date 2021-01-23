package IMDbPreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class FindHighAmbiguousActorNames {

	public static void main(String[]args)throws Exception{
		
		//处理actor.list文件，找到其中歧义的名字，以及各个歧义名字演员的movie个数，寻找我们应该寻找网页的名字
		//由于IMDB中名字存储的格式为Mackay, Andrew (I)，已经为有歧义的名字排歧，每个人拥有唯一name
		
		//存储每个歧义名字的对应候选演员
		HashMap<String,HashSet<String>> ambiguMentionToActorNames=new HashMap<String, HashSet<String>>();
		//存储每个演员的movie，tv数量
		HashMap<String, Integer> actorNameToMovieNumber=new HashMap<String, Integer>();
		
		
		File f=new File(".\\newIMDB\\IMDbData\\actors.list");
		
		InputStreamReader write = new InputStreamReader(new FileInputStream(f.getAbsolutePath()));
    	BufferedReader br=new BufferedReader(write);
		String line;
		int pubID=1;
		int number=0;
		boolean ok=false;
		String actorNm="null";
		
		
	  while ((line = br.readLine() )!=null) {
		  
		  if (pubID>=240) {
				String[] a=line.split("\t");
				if (a[0].length()>0) {
					//根据格式，说明为actor name存在的那一行
//					System.err.println("author names: "+a[0]);
					if (actorNameToMovieNumber.containsKey(actorNm)) {
						actorNameToMovieNumber.put(actorNm, actorNameToMovieNumber.get(actorNm)+number);
					}
					else {
						actorNameToMovieNumber.put(actorNm, number);
					}
					number=-1;
					
					
					actorNm=a[0].trim();
					
//					if (actorNm.equals("Clayton, John (I)")) {
//						ok=true;
//					}
//					else {
//						ok=false;
//					}
					if (actorNm.matches(".*\\(\\w+\\)")) {
						//说明有歧义，为这种样式：Mackay, Andrew (I)
						String mention=actorNm.replaceAll("\\(\\w+\\)", "").trim();
						
						if (ambiguMentionToActorNames.containsKey(mention)) {
							ambiguMentionToActorNames.get(mention).add(actorNm);
						}
						else {
							HashSet<String> candiSet=new HashSet<String>();
							candiSet.add(actorNm);
							ambiguMentionToActorNames.put(mention, candiSet);
						}
					}
					
				}
				
			}
	    	if (pubID>16868926) {
				break;
			}
	    	
//	    	if (ok) {
//				System.out.println(line);
//			}
			number++;
				
			
	    	pubID++;
		  
	  }
	  
	  number=0;
	  
	  System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	  
	  for (String string : ambiguMentionToActorNames.keySet()) {
			if (ambiguMentionToActorNames.get(string).size()>1) {
				
//				System.out.println(string);
				ArrayList<ActorScore> candiScores=new ArrayList<ActorScore>();
				for (String candiString : ambiguMentionToActorNames.get(string)) {
					ActorScore authorScore=new ActorScore();
					authorScore.nameString=candiString;
					authorScore.score=actorNameToMovieNumber.get(candiString);
//					System.out.println(actorNameToMovieNumber.get(candiString));
					candiScores.add(authorScore);
					
					
				}
				Collections.sort(candiScores,Collections.reverseOrder());
				if (candiScores.get(0).score>100&&candiScores.get(1).score>100) {
					number++;
					System.out.print(string+" :  ");
					for (ActorScore authorScore : candiScores) {
						System.out.print(authorScore.nameString+"  ["+authorScore.score+"]     ");
					}
					System.out.println();
				}
			}
		}
	  System.out.println(number);
	  
	  
	  
//	  StringBuilder stringBuilder=new StringBuilder();
//	  
//	  int linenumber1=0;
//		for (String string : ambiguMentionToActorNames.keySet()) {
//			if (ambiguMentionToActorNames.get(string).size()>1) {
//				linenumber1++;
//				stringBuilder.append(linenumber1+":  "+ string+" :  ");
//				for (String candiString : ambiguMentionToActorNames.get(string)) {
//					stringBuilder.append(candiString+"  ["+actorNameToMovieNumber.get(candiString)+"]     ");
//				}
//				stringBuilder.append("\n");
//			}
//		}
//		FileWriter fileWriter1=new FileWriter(".\\IMDb\\ambiguousMentionToActorNames");
//	    fileWriter1.write(stringBuilder.toString());
//	    fileWriter1.close();    
	    
	    
	   
       
				
	    
	    
	}
}
