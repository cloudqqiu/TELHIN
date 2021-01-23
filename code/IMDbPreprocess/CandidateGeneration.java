package IMDbPreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;




public class CandidateGeneration {
	
	
	//读取imdb的actors.list文件，根据名字一致，为每个mention产生candidate entity list，并存储
	//同时，根据每个演员的movie，tv数量来直接估算popularity
	
	ArrayList<String> MentionNames=new ArrayList<String>();
	public HashMap<String, ArrayList<String>> MentionCandidateNames=new HashMap<String, ArrayList<String>>();
	
	public HashMap<String, Double> candidAuthorNameToScore=new HashMap<String, Double>();
	public HashMap<String, Double> authorNameToScore=new HashMap<String, Double>();
	
	
	
	public CandidateGeneration() throws IOException{
		// TODO Auto-generated constructor stub

// 	Alive:
//		String webpageDirString=".\\IMDb\\OrigWebpages";
//		File webpageDir=new File(webpageDirString);
//		File[] mentionPagesFiles=webpageDir.listFiles();
//		for (File file : mentionPagesFiles) {
//			MentionNames.add(file.getName());
//		}

//		System.out.println(MentionNames.size());
//		for (String	mention : MentionNames) {
//			System.out.println(mention);
//		}

		//yyw:
		String mentionsDirString = ".\\newIMDB\\names";
		File nameFile = new File(mentionsDirString);
		InputStreamReader write = new InputStreamReader(new FileInputStream(nameFile.getAbsolutePath()));
		BufferedReader br = new BufferedReader(write);
		String line;
		while ((line = br.readLine()) != null)
		{
			String[] a = line.split(" ");
			MentionNames.add(a[1] + ", " + a[0]);
		}
		br.close();
		write.close();
	}

	
	public void writeCandidates() throws IOException {
		
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
//						System.err.println("author names: "+a[0]);
						if (actorNameToMovieNumber.containsKey(actorNm)) {
							actorNameToMovieNumber.put(actorNm, actorNameToMovieNumber.get(actorNm)+number);
						}
						else {
							actorNameToMovieNumber.put(actorNm, number);
						}
						number=-1;
						
						
						actorNm=a[0].trim();
						

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
		    	
			number++;
					
				
		    	pubID++;
			  
		  }
		  
		 	  
		  
		StringBuilder authorBuilder=new StringBuilder();
		for (String mention : MentionNames) {
			if (!ambiguMentionToActorNames.containsKey(mention)) {
				System.err.println(mention+" no appear!!!!!!!!!1");
			}
			HashSet<String> candiNames=ambiguMentionToActorNames.get(mention);
			for (String candiName:candiNames) {

				authorBuilder.append(mention+"\t"+candiName+"\n");
			}
		}
		
		FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\Candidates");
		fileWriter3.write(authorBuilder.toString());
		fileWriter3.close();
	}
	
	
	public void ReadCandidates() throws IOException {
//		System.out.println("Read candidates ...");
		FileReader fr1 = new FileReader(".\\newIMDB\\GeneratedData\\Candidates");
	    BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;
	   
	    while((line1 = br1.readLine())!=null){
	    	String[] strings=line1.split("\t");
	    	if (strings.length==2) {
	    		
	    		String mentionString=strings[0];
	    		String candiString=strings[1];
	    		if (MentionCandidateNames.containsKey(mentionString)) {
					MentionCandidateNames.get(mentionString).add(candiString);
				}
	    		else {
					ArrayList<String> arrayList=new ArrayList<String>();
					arrayList.add(candiString);
					MentionCandidateNames.put(mentionString, arrayList);
				}
	    		
			}
	    	else {
				System.err.println("hhhh1  "+line1);
			}
	    	
	    }
	    br1.close();
	    fr1.close();
	    
//	   System.out.println(MentionCandidateNames.get("Harrison, Richard"));
	}

	//同时，根据每个演员的movie，tv数量归一化来直接估算popularity
	public void writeCandidateAuthorsPriorScore() throws IOException {
		
		//存储每个歧义名字的对应候选演员
		HashMap<String,HashSet<String>> ambiguMentionToActorNames=new HashMap<String, HashSet<String>>();
		//存储每个演员的movie，tv数量
		HashMap<String, Integer> actorNameToMovieNumber=new HashMap<String, Integer>();
		
		
		File f=new File(".\\IMDb\\IMDbData\\actors.list"); 
		
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
	    	
		number++;
				
			
	    	pubID++;
		  
	  }
		
	  
	  for (String mentionName : MentionNames) {
		int totalMovieNo=0;
		for (String candiNmae : MentionCandidateNames.get(mentionName)) {
			totalMovieNo+=actorNameToMovieNumber.get(candiNmae);
		}
		for (String candiNmae : MentionCandidateNames.get(mentionName)) {
			if (candidAuthorNameToScore.containsKey(candiNmae)) {
				System.err.println("errrrrrrrrrrrrrrr");
			}
			candidAuthorNameToScore.put(candiNmae, actorNameToMovieNumber.get(candiNmae)/(double)totalMovieNo);
		}
	}
		
		//just write candiAuthor global score into disk
		StringBuilder authorBuilder=new StringBuilder();
		for (String authorID : candidAuthorNameToScore.keySet()) {
			authorBuilder.append(authorID+"\t"+candidAuthorNameToScore.get(authorID)+"\n");
		}
		FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\CandidateActorPriorScoreViaMovieNumber");
		fileWriter3.write(authorBuilder.toString());
		fileWriter3.close();
	}
	
	public void ReadPriorPageRankScore() throws IOException {
//		System.out.println("Read Prior PageRank Score ...");   //yyw
		FileReader fr1 = new FileReader(".\\newIMDB\\GeneratedData\\CandidateActorPriorScoreViaMovieNumber");
	    BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;
	   
	    while((line1 = br1.readLine())!=null){
	    	String[] strings=line1.split("\t");
	    	if (strings.length==2) {
	    		
	    		if (!strings[1].equals("null")) {
					double score=Double.parseDouble(strings[1]);
					authorNameToScore.put(strings[0], score);
				}
	    		
			}
	    	else {
				System.err.println("hhhh1  "+line1);
			}
	    	
	    }
	    br1.close();
	    fr1.close();
	    
//	    System.out.println(authorNameToScore.get("Evans, Chris (VIII)"));
	}
	
	
	public static void main(String[]args)throws Exception{
		CandidateGeneration candidateGeneration=new CandidateGeneration();
		
		
//		candidateGeneration.writeCandidates();
		
		candidateGeneration.ReadCandidates();
		candidateGeneration.writeCandidateAuthorsPriorScore();
		
//		candidateGeneration.ReadPriorPageRankScore();
	}
}
