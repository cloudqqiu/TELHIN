package WebPage;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import IMDbPreprocess.CandidateGeneration;
import IMDbPreprocess.Stemmer;

//objset中object存放的顺序为：movie, actor, director, character, term
public class DecomposeWebpageIntoObjects {
	
	public HashMap<String, ArrayList<String>> MentionCandidateNames=new HashMap<String, ArrayList<String>>();
	
	public HashMap<String, Double> NametoScore=new HashMap<String, Double>();
	
	public HashSet<String> movietitlesInDis=new HashSet<String>();
	public HashSet<String> actorNamesInDis=new HashSet<String>();
	public HashSet<String> directorNamesInDis=new HashSet<String>();
	public HashSet<String> characNamesInDis=new HashSet<String>();
		
	public ArrayList<String> movietitlesDisSorted=new ArrayList<String>();
	public ArrayList<String> actorNamesDisSorted=new ArrayList<String>();
	public ArrayList<String> directorNamesDisSorted=new ArrayList<String>();
	public ArrayList<String> characNamesDisSorted=new ArrayList<String>();
	
	public HashMap<String, String> fileNameToPath=new HashMap<String, String>();
	public HashMap<String, String> fileNameToGoldenEntityName=new HashMap<String, String>();
	public HashMap<String, String> fileNameToMentionName=new HashMap<String, String>();
	
	HashSet<String> MentionNames=new HashSet<String>();
	
	public DecomposeWebpageIntoObjects() {
		// TODO Auto-generated constructor stub
		String mentionDirString=".\\IMDb\\OrigWebpages";
		File mentionDir=new File(mentionDirString);
		File[] mentionFiles=mentionDir.listFiles();
		for (File mention : mentionFiles) {
			if (mention.isDirectory()) {
//				System.out.println(mention.getName());
				MentionNames.add(mention.getName());
			}
		}
		
//		System.out.println(MentionNames.size());
	}
	
	public void geneCandidates() throws IOException {
		CandidateGeneration candidateGeneration=new CandidateGeneration();
		candidateGeneration.ReadCandidates();
		candidateGeneration.ReadPriorPageRankScore();
		
		MentionCandidateNames=candidateGeneration.MentionCandidateNames;
		
		
	}

	public void ReadDistribution(String metapath, String candiName) throws Exception, IOException
	{
//		System.out.println("Read distribution ...");
		 NametoScore=new HashMap<String, Double>();
		File file=new  File(".\\IMDb\\GeneratedData\\Distribution\\"+metapath+"\\"+candiName);
		if (!file.exists()) {
			return;
		}
		FileReader fr1 = new FileReader(file);
	    BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;
	   
//	    System.out.println(metapath+"   "+candiName);
	    
	    
	   
	    
	    while((line1 = br1.readLine())!=null){
	    	String[] strings=line1.split("\t");
	    	if (strings.length==2) {
	    		String authorName=strings[0];
	    		
	    		
	    		if (NametoScore.containsKey(authorName)) {
//	    			System.out.println(authorName+"  "+NametoScore.get(authorName)+"  "+strings[1]);
	    			NametoScore.put(authorName, NametoScore.get(authorName)+Double.parseDouble(strings[1]));
	    			System.err.println(authorName+"  "+NametoScore.get(authorName));
				}
	    		else {
					NametoScore.put(authorName, Double.parseDouble(strings[1]));
				}
	    		
			}
	    	else {
				System.err.println("hhhh1  "+line1);
			}
	    	
	    }
	    br1.close();
	    fr1.close();
		
	}

public void getCandidateDistribution(String mention) throws IOException, Exception {
	//get the set of author names, venue names, and years according to the distribution of candidates
	
	ArrayList<String> paths=new ArrayList<String>();
//	paths.add("AB");
//	paths.add("AMK");
	paths.add("AMT");
	paths.add("AMC");
	paths.add("AMA");
	paths.add("AMD");
	
	
	ArrayList<String> canNames=MentionCandidateNames.get(mention);
	movietitlesInDis=new HashSet<String>();
	actorNamesInDis=new HashSet<String>();
	directorNamesInDis=new HashSet<String>();
	characNamesInDis=new HashSet<String>();
	
	movietitlesDisSorted=new ArrayList<String>();
	actorNamesDisSorted=new ArrayList<String>();
	directorNamesDisSorted=new ArrayList<String>();
	characNamesDisSorted=new ArrayList<String>();
	
	for (String canName: canNames) {
		
//		if (!canName.equals("Evans, Chris (V)")) {
//			continue;
//		}
//		System.out.println("canName: "+canName);
		for (String metapath : paths) {
			
			ReadDistribution(metapath, canName);
			
			if (metapath.endsWith("T")) {
				for (String string : NametoScore.keySet()) {
					movietitlesInDis.add(string);
				}
			}
			else if (metapath.endsWith("A")) {
				
				for (String string : NametoScore.keySet()) {
					
					actorNamesInDis.add(string);
				}
			}
			else if (metapath.endsWith("D")) {
				for (String string : NametoScore.keySet()) {
					directorNamesInDis.add(string);
				}
			}
			else if (metapath.endsWith("C")) {
				for (String string : NametoScore.keySet()) {
					characNamesInDis.add(string);
				}
			}
			
		}
		
		
	}
	
	//sort the author name and venue name according to their length
	//from long to short
	
	ArrayList<WordFreq> authorNamelist=new ArrayList<WordFreq>();
	for (String authorName : actorNamesInDis) {
		
		WordFreq wordFreq=new WordFreq();
		wordFreq.name=authorName;
		wordFreq.freq=authorName.split(" ").length;
		authorNamelist.add(wordFreq);
	}
	
	Collections.sort(authorNamelist,Collections.reverseOrder());
	
	
	for (WordFreq wordFreq : authorNamelist) {
//		if (wordFreq.freq==1) {
//			System.out.println(wordFreq.name);
//		}
		actorNamesDisSorted.add(wordFreq.name);
	}
	
	ArrayList<WordFreq> venueNamelist=new ArrayList<WordFreq>();
	for (String venueName : movietitlesInDis) {
		
		WordFreq wordFreq=new WordFreq();
		wordFreq.name=venueName;
		wordFreq.freq=venueName.split(" ").length;
		venueNamelist.add(wordFreq);
	}
	
	Collections.sort(venueNamelist,Collections.reverseOrder());
	
	
	for (WordFreq wordFreq : venueNamelist) {
		
		movietitlesDisSorted.add(wordFreq.name);
	}
	
	ArrayList<WordFreq> directorNamelist=new ArrayList<WordFreq>();
	for (String authorName : directorNamesInDis) {
		
		WordFreq wordFreq=new WordFreq();
		wordFreq.name=authorName;
		wordFreq.freq=authorName.split(" ").length;
		directorNamelist.add(wordFreq);
	}
	
	Collections.sort(directorNamelist,Collections.reverseOrder());
	
	
	for (WordFreq wordFreq : directorNamelist) {
//		if (wordFreq.freq==1) {
//			System.out.println(wordFreq.name);
//		}
		directorNamesDisSorted.add(wordFreq.name);
	}
	
	
	ArrayList<WordFreq> charaNamelist=new ArrayList<WordFreq>();
	for (String authorName : characNamesInDis) {
		
		WordFreq wordFreq=new WordFreq();
		wordFreq.name=authorName;
		wordFreq.freq=authorName.split(" ").length;
		charaNamelist.add(wordFreq);
	}
	
	Collections.sort(charaNamelist,Collections.reverseOrder());
	
	
	for (WordFreq wordFreq : charaNamelist) {
//		if (wordFreq.freq==1) {
//			System.out.println(wordFreq.name);
//		}
		characNamesDisSorted.add(wordFreq.name);
	}
	
//	System.out.println("\n\nmovie:  "+movietitlesDisSorted.size());
//	for (String string : movietitlesDisSorted) {
//		System.out.println(string);
//	}
//	System.out.println("\n\nactor:  "+actorNamesDisSorted.size());
//	for (String string : actorNamesDisSorted) {
//		System.out.println(string);
//	}
//	System.out.println("\n\ndirector:  "+directorNamesDisSorted.size());
//	
//	for (String string : directorNamesDisSorted) {
//		System.out.println(string);
//	}
//	System.out.println("\n\ncharac:  "+characNamesDisSorted.size());
//	
//	for (String string : characNamesDisSorted) {
//		System.out.println(string);
//	}
}

public void decomposeTxt() throws Exception {
	ReadWebPageAndGoldenMapping readWebPageAndGoldenMapping=new ReadWebPageAndGoldenMapping();
	readWebPageAndGoldenMapping.readTxtFileMappingResult();
	fileNameToPath=readWebPageAndGoldenMapping.fileNameToPath;
	fileNameToMentionName=readWebPageAndGoldenMapping.fileNameToMentionName;
	fileNameToGoldenEntityName=readWebPageAndGoldenMapping.fileNameToGoldenEntityName;
    
	HashMap<String, ArrayList<String>> mentionNameToFileList=new HashMap<String, ArrayList<String>>();
	for (String filenameString : fileNameToMentionName.keySet()) {
		String mentionName=fileNameToMentionName.get(filenameString);
		
		if (mentionNameToFileList.containsKey(mentionName)) {
			mentionNameToFileList.get(mentionName).add(filenameString);
		}
		else {
			ArrayList<String> fileList=new ArrayList<String>();
			fileList.add(filenameString);
			mentionNameToFileList.put(mentionName, fileList);
		}
	}
	
	HashSet<String> stopwordlistHashSet=new HashSet<String>();
	
	//read stop word list
	FileReader fr1 = new FileReader(".\\IMDb\\stopwordlist.txt");
    BufferedReader br1 = new BufferedReader(fr1);
    String line1 = null;
   
    while((line1 = br1.readLine())!=null){
    	stopwordlistHashSet.add(line1.trim());
    	
    }
    br1.close();
    fr1.close();
    
	for (String mentionName : mentionNameToFileList.keySet()) {
//		if (!mentionName.equals("Bing Liu")) {
//			continue;
//		}
		if (!MentionNames.contains(mentionName)) {
			System.err.println("noooooooooooooo");
			continue;
		}
//		System.out.println("mentionName: "+mentionName);
		
		//get authorNamesDisSorted, venueNamesDisSorted, yearNamesInDis
		getCandidateDistribution(mentionName);
//		System.out.println("\n\nmovie:  "+movietitlesDisSorted.size());
//		System.out.println("\n\nactor:  "+actorNamesDisSorted.size());
//		System.out.println("\n\ndirector:  "+directorNamesDisSorted.size());
//		System.out.println("\n\ncharac:  "+characNamesDisSorted.size());
		
		ArrayList<String> fileList=mentionNameToFileList.get(mentionName);
		for (int index=fileList.size()-1;index>=0;index--) {
			String txtFileName=fileList.get(index);
			
			if (Integer.parseInt(txtFileName)>260) {  //64
				continue;
			}
			
			String fileString1=".\\IMDb\\ObjSet\\"+txtFileName;
			File fff=new File(fileString1);
//			if (fff.exists()) {
//				continue;
//			}
			
			StringBuilder txtStringBuilder=new StringBuilder();
			
//			System.out.println("txtFileName: "+txtFileName);
			//read txt from disk
			InputStreamReader write = new InputStreamReader(new FileInputStream(".\\IMDb\\TextWebpages\\"+txtFileName),"UTF-8");
	    	BufferedReader br=new BufferedReader(write);
			String s;

		   StringBuilder stringBuilder=new StringBuilder();
		  while ((s = br.readLine() )!=null) {
		 	stringBuilder.append(s);
		 	stringBuilder.append(" ");
		   }
		  String txtPage=stringBuilder.toString().trim();
//		  System.out.println(txtPage);
		  
		  //先识别movie name
		  for (String movieName : movietitlesDisSorted) {
//			  System.out.println("movieName:"+movieName);
			  String origVN=movieName;
			 
			  
			  movieName=movieName.trim();
			  
			  if (movieName.length()==0) {
				  System.err.println("movieName name length == 0");
				continue;
			}
			  

			  movieName=movieName.replaceAll("\\*", "\\\\*");
			  movieName=movieName.replaceAll("\\+", "\\\\+");
			  movieName=movieName.replaceAll("\\?", "\\\\?");
			  
				if (movieName.endsWith("\\?")) {
					movieName=movieName.substring(0,movieName.length()-2).trim();
				}
				if (movieName.endsWith("!")) {
					movieName=movieName.substring(0,movieName.length()-1).trim();
				}
				
				movieName=movieName.replaceAll("\\(", "\\\\(");
				movieName=movieName.replaceAll("\\)", "\\\\)");
				
				movieName=movieName.replaceAll("\\.", "\\\\.");
				if (movieName.endsWith("\\.")) {
					movieName=movieName.substring(0,movieName.length()-2).trim();
				}
				movieName=movieName.trim();
				
				
				String regString="\\b"+movieName+"\\b";
//				System.out.println(regString);
				Pattern p = Pattern.compile(regString);
				Matcher m = p.matcher(txtPage);
				while (m.find()) {
					System.out.println("mivie: "+m.group());
					txtStringBuilder.append(origVN+"+");
				}
				
				txtPage=m.replaceAll(" ");
//				System.out.println(txtPage);
		}
		  txtStringBuilder.append("\n");
		  
		  
		  
		  //删去mention自身
		  //remove mention name itself from txtPage
//		  System.out.println("mentionName: "+mentionName);
		  txtPage=txtPage.replaceAll(mentionName, " ");
		  String alauthorNmae=mentionName;
		  Pattern p2 = Pattern.compile("(.+?),\\s+(.+)");
			Matcher m2 = p2.matcher(mentionName);
			if (m2.find()) {
				alauthorNmae=m2.group(2).trim()+" "+m2.group(1).trim();
//				System.out.println(actorString+"       "+actorName);

			}
			else {
				System.err.println("ykykykykyykyk");
			}
			txtPage=txtPage.replaceAll(alauthorNmae, " ");
			
//			System.out.println(txtPage);
		  //begin decompose txtPage using authorNamesDisSorted, venueNamesDisSorted, yearNamesInDis
		  //and then store
		  for (String authorName : actorNamesDisSorted) {
			  String originalAN=authorName;
			  
			  authorName=authorName.trim();
			  
			  if (authorName.length()==0) {
				  System.err.println("author name length == 0");
				continue;
			}
			  
			  authorName=authorName.replaceAll("\\*", "\\\\*");
			  authorName=authorName.replaceAll("\\+", "\\\\+");
			  authorName=authorName.replaceAll("\\?", "\\\\?");
			  authorName=authorName.replaceAll("\\(", "\\\\(");
			  authorName=authorName.replaceAll("\\)", "\\\\)");
			  authorName=authorName.replaceAll("\\.", "\\\\.");
				
				authorName=authorName.trim();
				
				alauthorNmae=authorName;
				String[] strings=authorName.split(" ");
				if (strings.length==2) {
					alauthorNmae=strings[1].trim()+", "+strings[0].trim();
				}
				else if (strings.length>2) {
					alauthorNmae=strings[strings.length-1]+", ";
					for (int i = 0; i < strings.length-1; i++) {
						alauthorNmae+=strings[i]+" ";
					}
					alauthorNmae=alauthorNmae.trim();
				}
				if (alauthorNmae.endsWith("\\.")) {
					alauthorNmae=alauthorNmae.substring(0,alauthorNmae.length()-2).trim();
				}
				
				String regString="\\b("+authorName+"|"+alauthorNmae+")\\b";
//				System.out.println(regString);
				Pattern p = Pattern.compile(regString);
				Matcher m = p.matcher(txtPage);
				while (m.find()) {
					System.out.println("actor: "+m.group());
					txtStringBuilder.append(originalAN+"+");
				}
				
				txtPage=m.replaceAll(" ");
//				System.out.println(txtPage);
		}
		  txtStringBuilder.append("\n");


		  for (String authorName : directorNamesDisSorted) {
			  String originalAN=authorName;
			  
			  authorName=authorName.trim();
			  
			  if (authorName.length()==0) {
				  System.err.println("author name length == 0");
				continue;
			}
			  
			  authorName=authorName.replaceAll("\\*", "\\\\*");
			  authorName=authorName.replaceAll("\\+", "\\\\+");
			  authorName=authorName.replaceAll("\\?", "\\\\?");
			  authorName=authorName.replaceAll("\\(", "\\\\(");
			  authorName=authorName.replaceAll("\\)", "\\\\)");
			  authorName=authorName.replaceAll("\\.", "\\\\.");
				
				authorName=authorName.trim();
				
				alauthorNmae=authorName;
				String[] strings=authorName.split(" ");
				if (strings.length==2) {
					alauthorNmae=strings[1].trim()+", "+strings[0].trim();
				}
				else if (strings.length>2) {
					alauthorNmae=strings[strings.length-1]+", ";
					for (int i = 0; i < strings.length-1; i++) {
						alauthorNmae+=strings[i]+" ";
					}
					alauthorNmae=alauthorNmae.trim();
				}
				if (alauthorNmae.endsWith("\\.")) {
					alauthorNmae=alauthorNmae.substring(0,alauthorNmae.length()-2).trim();
				}
				
				String regString="\\b("+authorName+"|"+alauthorNmae+")\\b";
//				System.out.println(regString);
				Pattern p = Pattern.compile(regString);
				Matcher m = p.matcher(txtPage);
				while (m.find()) {
					System.out.println("director: "+m.group());
					txtStringBuilder.append(originalAN+"+");
				}
				
				txtPage=m.replaceAll(" ");
//				System.out.println(txtPage);
		}
		  txtStringBuilder.append("\n");

		  for (String authorName : characNamesDisSorted) {
			  if (authorName.equals("Actor")||authorName.equals("Character")||!authorName.contains(" ")) {
				continue;
			}
			  String originalAN=authorName;
			  
//			  System.out.println(authorName);
			  authorName=authorName.trim();
			  
			  if (authorName.length()==0) {
				  System.err.println("author name length == 0");
				continue;
			}
			  
			  authorName=authorName.replaceAll("\\*", "\\\\*");
			  authorName=authorName.replaceAll("\\+", "\\\\+");
			  authorName=authorName.replaceAll("\\?", "\\\\?");
			  authorName=authorName.replaceAll("\\(", "\\\\(");
			  authorName=authorName.replaceAll("\\)", "\\\\)");
			  authorName=authorName.replaceAll("\\.", "\\\\.");
				
				authorName=authorName.trim();
				
				
				if(authorName.contains("[")) {
					continue;
				}
				String regString="\\b"+authorName+"\\b";
//				System.out.println(regString);
				
				Pattern p = Pattern.compile(regString);
				
				Matcher m = p.matcher(txtPage);
				while (m.find()) {
					System.out.println("character: "+m.group());
					txtStringBuilder.append(originalAN+"+");
				}
				
				txtPage=m.replaceAll(" ");
//				System.out.println(txtPage);
		}
		  txtStringBuilder.append("\n");
		  
  
		  //term 
		  txtPage=txtPage.toLowerCase();
			txtPage=txtPage.replaceAll("\\W", " ");
			txtPage=txtPage.replaceAll("_", " ");
			txtPage=txtPage.replaceAll("\\s+", " ");
			
			
			String[] terms=txtPage.split(" ");
			System.out.println("terms.length: "+terms.length);
			int tNo=0;
			
			for (String term : terms) {
				if (term.length()>2) {
					if (!stopwordlistHashSet.contains(term)) {
						
						
						//stem
						char[] w;
						   Stemmer stem = new Stemmer();
						   String aString=term;
						   w=aString.toCharArray();
						   stem.add(w, aString.length()); 

						   stem.stem();
						   
						    term = stem.toString();
						    
						    tNo++;
						    txtStringBuilder.append(term+"+");
					}
				}
			}
		  
			System.out.println("tNo: "+tNo);
			System.out.println(txtStringBuilder.toString());
		 
	    	FileWriter fileWriter=new FileWriter(fileString1);
			fileWriter.write(txtStringBuilder.toString());
			fileWriter.close();
		  
		}
		
	}
	
}

public static void main(String[]args)throws Exception{
	DecomposeWebpageIntoObjects dIntoObjects=new DecomposeWebpageIntoObjects();
	dIntoObjects.geneCandidates();
//	dIntoObjects.getCandidateDistribution("Alexander, Peter");
	
	dIntoObjects.decomposeTxt();
}

}

