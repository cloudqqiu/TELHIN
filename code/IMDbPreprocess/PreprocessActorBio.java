package IMDbPreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class PreprocessActorBio {

	
	public HashMap<String, Double> authorNameToScore=new HashMap<String, Double>();
	public HashMap<String, String> actorBiostring=new HashMap<String, String>();
	
	public HashMap<Integer, String> ActorIDToName=new HashMap<Integer, String>();
	public HashMap<String, Integer> ActorNameToID=new HashMap<String, Integer>();
	
	
	HashMap<Integer, String> TermIDToName=new HashMap<Integer, String>();
	HashMap<String, Integer> TermNameToID=new HashMap<String, Integer>();
	HashMap<Integer, ArrayList<Integer>> ActorIDToBioTermIDList=new HashMap<Integer, ArrayList<Integer>>();
	
	
	public void ReadIMDBactorsbio() throws Exception {
	
		HashSet<String> biotag=new HashSet<String>();
		
		CandidateGeneration candidateGeneration=new CandidateGeneration();
		candidateGeneration.ReadPriorPageRankScore();
		authorNameToScore=candidateGeneration.authorNameToScore;
		
		System.out.println(authorNameToScore.size());
		
		
		
		File f=new File(".\\newIMDB\\IMDbData\\biographies.list");
		
		InputStreamReader write = new InputStreamReader(new FileInputStream(f.getAbsolutePath()));
		BufferedReader br=new BufferedReader(write);
		String line;
		int lineNo=1;
		
		String actorNm="null";
		StringBuilder stringBuilder=new StringBuilder();
		
	  while ((line = br.readLine() )!=null) {
		  
		  if (lineNo>=6) {
			  
//			  System.out.println(lineNo+":  "+line+"   length:"+line.length());
			  if (line.length()>0&&!line.startsWith("------------------")) {
				  //只保留其中这些关键信息
				  if (line.startsWith("TR:")||line.startsWith("RN:")||line.startsWith("SP:")||line.startsWith("NK:")||line.startsWith("OW:")||line.startsWith("QU:")||line.startsWith("BO:")||line.startsWith("BG:")||line.startsWith("TM:")||line.startsWith("DD:")||line.startsWith("DB:")) {
					stringBuilder.append(line.substring(4)+" ");
				}
				
			}
			   	if (line.startsWith("NM:")) {
					
					actorNm=line.substring(4).trim();
				}
			   	
			   	if (line.startsWith("------------------")) {
					if (authorNameToScore.containsKey(actorNm)) {
						//将每个actor的bio信息存入actorBiostring
						actorBiostring.put(actorNm, stringBuilder.toString());
					}
					stringBuilder.delete(0, stringBuilder.length());
				}
			   	
			   	
		  }
//	  		if (lineNo>180) {
//				break;
//			}
	  		lineNo++;
		  
	  	}
	  
	  
	  System.out.println(actorBiostring.size());
	  
	  
//	  System.out.println(actorBiostring.get("Evans, Chris (V)"));
//	  for (String string : biotag) {
//		System.out.println(string);
//	}
	  
	 
	  
	  for (String actorNmString : authorNameToScore.keySet()) {
		if (!actorBiostring.containsKey(actorNmString)) {
			System.out.println(actorNmString);
		}
	}
	  
	  stringBuilder.delete(0, stringBuilder.length());

	  for (String actorNmString : actorBiostring.keySet()) {
		stringBuilder.append(actorNmString+":::::::;"+"\n");
		stringBuilder.append(actorBiostring.get(actorNmString)+"\n"+"\n");

	}

	  FileWriter fileWriter5=new FileWriter(".\\newIMDB\\GeneratedData\\bio");
		fileWriter5.write(stringBuilder.toString());
		fileWriter5.close();
	  
	  
	}
	
	//split bio text into terms and remove stop words and stem, then store into disk
		public void transformBioTextToTermList() throws IOException {
			
			FileReader fr3 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\Actor");
		    BufferedReader br3 = new BufferedReader(fr3);
		    String line3 = null;
		   
		    while((line3 = br3.readLine())!=null){
		    	String[] strings=line3.split("\t");
		    	if (strings.length==2) {
		    		ActorIDToName.put(Integer.parseInt(strings[0]), strings[1]);
				}
		    	else {
					System.err.println("hhhh3  "+line3);
				}
		    	
		    }
		    br3.close();
		    fr3.close();
		    
		    for (Integer integer : ActorIDToName.keySet()) {
				if (!ActorNameToID.containsKey(ActorIDToName.get(integer))) {
					ActorNameToID.put(ActorIDToName.get(integer), integer);
				}
				else {
					System.err.println("dupkicate actor name");
				}
				
			}
					
			HashSet<String> stopwordlistHashSet=new HashSet<String>();
			
			//read stop word list
			FileReader fr1 = new FileReader(".\\newIMDB\\stopwordlist.txt");
		    BufferedReader br1 = new BufferedReader(fr1);
		    String line1 = null;
		   
		    while((line1 = br1.readLine())!=null){
		    	stopwordlistHashSet.add(line1.trim());
		    	
		    }
		    br1.close();
		    fr1.close();
			

			for (String	actorNm : actorBiostring.keySet()) {
				
				Integer actorID=ActorNameToID.get(actorNm);
				
//				if (!actorNm.equals("O'Brien, Pat (III)")) {
//					continue;
//				}
				
				//对biography 文本进行预处理
				String title=actorBiostring.get(actorNm);
				
				//remove mention name itself from txtPage
				String mentionName=actorNm.replaceAll("\\([IVXLC]+?\\)", "").trim();
				
				title=title.replaceAll(mentionName, " ");
				  String alterMn=mentionName;
				  String[] strings2=mentionName.split(" ");
					if (strings2.length==2) {
						alterMn=strings2[1].trim()+" "+strings2[0].substring(0, strings2[0].length()-1).trim();
					}
					title=title.replaceAll(alterMn, " ");
					
					
				title=title.replaceAll("\\(qv\\)", " ");
				title=title.replaceAll("\\*", " ");
				title=title.replaceAll("\'s", " ");
				title=title.replaceAll("\\([IVXLC]+?\\)", " ");
				title=title.replaceAll("\"", " ");
				title=title.replaceAll("\'", " ");
				title=title.toLowerCase();
				title=title.replaceAll("\\W", " ");
				title=title.replaceAll("_", " ");
				title=title.replaceAll("\\s+", " ");
				
				
//				System.out.println(title);
				
				
				
				String[] titleTerms=title.split(" ");
				
				for (String titleTerm : titleTerms) {
//					System.out.println(titleTerm);
					
					
					
					if (titleTerm.length()>2) {
						if (!stopwordlistHashSet.contains(titleTerm)) {
							

							//stem
							char[] w;
							   Stemmer s = new Stemmer();
							   String aString=titleTerm;
							   w=aString.toCharArray();
							   s.add(w, aString.length()); 

							   s.stem();
							   
							    titleTerm = s.toString();

							     
//							   System.out.print(titleTerm+" ");
							
							Integer termID=-1;
							
							if (TermNameToID.containsKey(titleTerm)) {
								termID=TermNameToID.get(titleTerm);
							}
							else {
								termID=TermNameToID.size();
								TermNameToID.put(titleTerm, termID);
								TermIDToName.put(termID, titleTerm);
							}
							
							if (termID!=-1) {
								if (ActorIDToBioTermIDList.containsKey(actorID)) {
									ArrayList<Integer> termIDList=ActorIDToBioTermIDList.get(actorID);
									termIDList.add(termID);
								}
								else {
									ArrayList<Integer> termIDList=new ArrayList<Integer>();
									termIDList.add(termID);
									ActorIDToBioTermIDList.put(actorID, termIDList);
									
								}
							}
							else {
								System.out.println("errrr1");
							}
							
						}
						
					}
					
				}
				
				
				
				
			}
			
//			System.out.println(ActorNameToID.get("O'Brien, Pat (III)"));
//			ArrayList<Integer> termIDList=ActorIDToBioTermIDList.get(ActorNameToID.get("O'Brien, Pat (III)"));
//			
//			if (termIDList!=null) {
//				for (Integer integer : termIDList) {
//				System.out.print(integer+" ");
//			}
//			System.out.println();
//			}
//			else {
//				System.out.println("null");
//			}
			
			StringBuilder paperauthorBuilder=new StringBuilder();
		    for (Integer PaperID : ActorIDToBioTermIDList.keySet()) {
		    	ArrayList<Integer> authorIDList=ActorIDToBioTermIDList.get(PaperID);
		    	for (Integer integer : authorIDList) {
					paperauthorBuilder.append(PaperID.toString()+"\t"+integer.toString()+"\n");
				}
				
			}
		    FileWriter fileWriter2=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\ActorBioTerm");
			fileWriter2.write(paperauthorBuilder.toString());
			fileWriter2.close();
			
			StringBuilder authorBuilder=new StringBuilder();
			for (Integer authorID : TermIDToName.keySet()) {
				authorBuilder.append(authorID.toString()+"\t"+TermIDToName.get(authorID)+"\n");
			}
			FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\Term");
			fileWriter3.write(authorBuilder.toString());
			fileWriter3.close();
		    

			
		}
	
	public static void main(String[]args)throws Exception{
		PreprocessActorBio preprocessActorBio=new PreprocessActorBio();
		preprocessActorBio.ReadIMDBactorsbio();
		preprocessActorBio.transformBioTextToTermList();
		
	}
}
