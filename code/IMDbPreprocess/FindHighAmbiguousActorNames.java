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
		
		//����actor.list�ļ����ҵ�������������֣��Լ���������������Ա��movie������Ѱ������Ӧ��Ѱ����ҳ������
		//����IMDB�����ִ洢�ĸ�ʽΪMackay, Andrew (I)���Ѿ�Ϊ��������������磬ÿ����ӵ��Ψһname
		
		//�洢ÿ���������ֵĶ�Ӧ��ѡ��Ա
		HashMap<String,HashSet<String>> ambiguMentionToActorNames=new HashMap<String, HashSet<String>>();
		//�洢ÿ����Ա��movie��tv����
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
					//���ݸ�ʽ��˵��Ϊactor name���ڵ���һ��
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
						//˵�������壬Ϊ������ʽ��Mackay, Andrew (I)
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
