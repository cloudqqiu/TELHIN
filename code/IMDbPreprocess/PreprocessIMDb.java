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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//读取IMDB原始数据，将其转化为我们需要的图结构，保存成我们便于利用的格式。为了尽可能小的保存数据，我们只保存
//候选实体随机游走可能用到的信息。
//主要处理actors.list，actresses.list文件

public class PreprocessIMDb {
	
	
	public HashMap<Integer, String> ActorIDToName=new HashMap<Integer, String>();
	public HashMap<String, Integer> ActorNameToID=new HashMap<String, Integer>();
	public HashMap<Integer, ArrayList<Integer>> MovieIDToActorIDList=new HashMap<Integer, ArrayList<Integer>>();
	public HashMap<Integer, ArrayList<Integer>> ActorIDToMovieIDList=new HashMap<Integer, ArrayList<Integer>>();
	
	
	public HashMap<Integer, String> MovieIDToTitle=new HashMap<Integer, String>();
	public HashMap<String, Integer> MovieTitleToID=new HashMap<String, Integer>();
	
	
	public HashMap<Integer, ArrayList<Integer>> MovieIDToCharacterIDList=new HashMap<Integer, ArrayList<Integer>>();
	public HashMap<Integer, String> CharacterIDToName=new HashMap<Integer, String>();
	public HashMap<String, Integer> CharacterNameToID=new HashMap<String, Integer>();
	
	
	public HashMap<Integer, String> TermIDToName=new HashMap<Integer, String>();
	public HashMap<String, Integer> TermNameToID=new HashMap<String, Integer>();
	public HashMap<Integer, ArrayList<Integer>> ActorIDToBioTermIDList=new HashMap<Integer, ArrayList<Integer>>();
	
	public HashMap<Integer, ArrayList<Integer>> MovieIDToDirectorIDList=new HashMap<Integer, ArrayList<Integer>>();
	public HashMap<Integer, ArrayList<Integer>> MovieIDToKeywordTermIDList=new HashMap<Integer, ArrayList<Integer>>();
	
	public HashMap<String, Double> authorNameToScore=new HashMap<String, Double>();
	
	public HashSet<Integer> candiActorIDSet=new HashSet<Integer>();
	
	public void ReadIMDBactresses() throws Exception {
	
	
		//现将要保存的movie id set拿到
		ReadDBLPfromOurStore();
		
		for (Integer integer : MovieIDToTitle.keySet()) {
			if (!MovieTitleToID.containsKey(MovieIDToTitle.get(integer))) {
				MovieTitleToID.put(MovieIDToTitle.get(integer), integer);
			}
			else {
				System.err.println("duplicate movie title");
			}
			
		}
		
		for (Integer integer : ActorIDToName.keySet()) {
			if (!ActorNameToID.containsKey(ActorIDToName.get(integer))) {
				ActorNameToID.put(ActorIDToName.get(integer), integer);
			}
			else {
				System.err.println("dupkicate actor name");
			}
			
		}
		
		for (Integer integer : CharacterIDToName.keySet()) {
			if (!CharacterNameToID.containsKey(CharacterIDToName.get(integer))) {
				CharacterNameToID.put(CharacterIDToName.get(integer), integer);
			}
			else {
				System.err.println("duplicate character name: "+CharacterIDToName.get(integer));
			}
		}
	
		File f=new File(".\\IMDb\\IMDbData\\actresses.list"); 
		
		InputStreamReader write = new InputStreamReader(new FileInputStream(f.getAbsolutePath()));
		
//		InputStreamReader write = new InputStreamReader(new FileInputStream(f.getAbsolutePath()),"UTF-8");
		
     
		
    	BufferedReader br=new BufferedReader(write);
		String line;
		int lineNo=1;
		
		String actorNm="null";
		
		
	  while ((line = br.readLine() )!=null) {
		  
		  if (lineNo>=242) {
			  
			   	String[] a=line.split("\t");
			   	
			   	if (a.length<2) {
			   		lineNo++;
					continue;
				}
//			   	System.out.print(lineNo+": "+line+": a.length: "+a.length+"    ");
//			   	for (int i = 0; i < a.length; i++) {
//					System.out.print(a[i]+"-length"+a[i].length()+"：  ");
//				} 	
//			   	System.out.println();
			   	
			   	String movieString="null";
			   	String movieName="null";
			   	String characName="null";
			   	
				if (a[0].length()>0) {
					//根据格式，说明为actor name存在的那一行
//					System.err.println("Actor names: "+a[0]);
					actorNm=a[0].trim();	
//					System.out.println(actorNm);
				}
				movieString=a[a.length-1].trim();
				
				//处理这种情况Breakin' Till Dawn (2011)  (as Jesse Brown)  [Mini-Van]
				movieString=movieString.replaceAll("\\(as .+?\\)", "");
				//去掉<20>indicate billing position in credits
				movieString=movieString.replaceAll("<.+?>", "").trim();	
				movieString=movieString.replaceAll("\\(voice\\)", "").trim();
				movieString=movieString.replaceAll("\\(archive footage.*?\\)", "").trim();
				movieString=movieString.replaceAll("\\(also archive footage\\)", "").trim();
				movieString=movieString.replaceAll("\\(archive sound.*?\\)", "").trim();
				movieString=movieString.replaceAll("\\(uncredited.*?\\)", "").trim();
				movieString=movieString.replaceAll("\\(credit only\\)", "").trim();
				movieString=movieString.replaceAll("\\(rumored.*?\\)", "").trim();
				
				
				
				
				if (movieString.contains("[")) {
					Pattern p = Pattern.compile("(.+?)\\[(.+?)\\]");
					Matcher m = p.matcher(movieString);
					if (m.find()) {
						movieName=m.group(1).trim();
						characName=m.group(2).trim();
						
						
						//对character name进行处理，过滤
						if (characName.contains("himself")||characName.contains("Himself")||characName.contains("Herself")||characName.contains("herself")||characName.contains("Themselves")||characName.contains("Narrator")||characName.contains("Guest Appearance")||characName.contains("Presenter")||characName.contains("Co-starring")||characName.contains("segment")) {
							characName="null";
						}
						characName=characName.replaceAll("\\(\\d+?\\)", "").trim();
						characName=characName.replaceAll("\\(.+?\\)", "").trim();
						characName=characName.replaceAll("\\s-\\s.+", "").trim();
						characName=characName.replaceAll(",.*", "").trim();
						
						
						if (characName.trim().length()==0) {
							characName="null";
						}
							
						
					}
				}
				else {
					movieName=movieString;
					
				}
				
				
//				System.out.println(actorNm+":::: "+line);
//				System.out.println("   movie name: "+movieName);
//				System.out.println("   character:  "+characName);
						
						
//				if (lineNo>9957800) {
//					System.out.println(actorNm+":::: "+line);
//						System.out.println("   movie name: "+movieName);
//						System.out.println("   character:  "+characName);
//				}
				
				//只有当当前movie是可能随机游走到的Movie时才加入相关信息
				if (MovieTitleToID.containsKey(movieName)) {
					
					
					Integer MovieID=MovieTitleToID.get(movieName);
					
					
					//将actor name加入我们结构中
					Integer ActorID=null;
		    		if (ActorNameToID.containsKey(actorNm)) {
						ActorID=ActorNameToID.get(actorNm);
//						System.out.println("here2  "+ActorID);
					}
		    		else {
		    			ActorID=ActorNameToID.size();
						ActorNameToID.put(actorNm, ActorID);
						ActorIDToName.put(ActorID, actorNm);
					}
					
		    		//加入他们之间关系
		    		if (MovieIDToActorIDList.containsKey(MovieID)) {
		    			ArrayList<Integer> ActorIDList=MovieIDToActorIDList.get(MovieID);
		    			ActorIDList.add(ActorID);
					}
		    		else {
		    			System.err.println("111111111111");
					}
		    		
								
					
		    		Integer CharacterID=null;
		    		if (!characName.equals("null")) {
		    			//将Character name加入我们结构中
			    		
			    		if (CharacterNameToID.containsKey(characName)) {
			    			CharacterID=CharacterNameToID.get(characName);
						}
			    		else {
			    			CharacterID=CharacterNameToID.size();
			    			CharacterNameToID.put(characName, CharacterID);
							CharacterIDToName.put(CharacterID, characName);
						}
					}
		    		
		    		
		    		
		    		if (CharacterID!=null) {
						if (MovieIDToCharacterIDList.containsKey(MovieID)) {
						ArrayList<Integer> charIDList=MovieIDToCharacterIDList.get(MovieID);
						charIDList.add(CharacterID);
					}
		    		else {
		    			System.err.println("222222222222222");
		    			ArrayList<Integer> charIDList=new ArrayList<Integer>();
		    			charIDList.add(CharacterID);
		    			MovieIDToCharacterIDList.put(MovieID, charIDList);
					}
					}
					
					
					
				}
				
				
	    		
				
			}
	    	if (lineNo>9957913) {
	    		
				break;
			}
		
	    	lineNo++;
		  
	  }
		
	  
	  //test
//	  Integer actorId=ActorNameToID.get("Evans, Chris (V)");
//	  for (Integer movieid : MovieIDToActorIDList.keySet()) {
//		  
//		for (Integer actInteger : MovieIDToActorIDList.get(movieid)) {
//			if (actInteger.equals(actorId)) {
//				System.out.println(MovieIDToTitle.get(movieid));
//			}
//		}
//	}
	  
	}

	public void ReadIMDBactors() throws Exception {
		
		CandidateGeneration candidateGeneration=new CandidateGeneration();
		candidateGeneration.ReadPriorPageRankScore();
		authorNameToScore=candidateGeneration.authorNameToScore;
		
		File f=new File(".\\newIMDB\\IMDbData\\actors.list");
		
		InputStreamReader write = new InputStreamReader(new FileInputStream(f.getAbsolutePath()));
    	BufferedReader br=new BufferedReader(write);
		String line;
		int lineNo=1;
		
		String actorNm="null";
		
		
	  while ((line = br.readLine() )!=null) {
		  
		  if (lineNo>=240) {
			  
			   	String[] a=line.split("\t");
			   	
			   	if (a.length<2) {
			   		lineNo++;
					continue;
				}
//			   	System.out.print(lineNo+": "+line+": a.length: "+a.length+"    ");
//			   	for (int i = 0; i < a.length; i++) {
//					System.out.print(a[i]+"-length"+a[i].length()+"：  ");
//				}
			   	
			   	String movieString="null";
			   	String movieName="null";
			   	String characName="null";
			   	
				if (a[0].length()>0) {
					//根据格式，说明为actor name存在的那一行
//					System.err.println("Actor names: "+a[0]);
					actorNm=a[0].trim();				
				}
				movieString=a[a.length-1].trim();
				
				//处理这种情况Breakin' Till Dawn (2011)  (as Jesse Brown)  [Mini-Van]
				movieString=movieString.replaceAll("\\(as .+?\\)", "");
				//去掉<20>indicate billing position in credits
				movieString=movieString.replaceAll("<.+?>", "").trim();	
				movieString=movieString.replaceAll("\\(voice\\)", "").trim();
				movieString=movieString.replaceAll("\\(archive footage.*?\\)", "").trim();
				movieString=movieString.replaceAll("\\(also archive footage\\)", "").trim();
				movieString=movieString.replaceAll("\\(archive sound.*?\\)", "").trim();
				movieString=movieString.replaceAll("\\(uncredited.*?\\)", "").trim();
				movieString=movieString.replaceAll("\\(credit only\\)", "").trim();
				movieString=movieString.replaceAll("\\(rumored.*?\\)", "").trim();
				
				
				
				
				if (movieString.contains("[")) {
					Pattern p = Pattern.compile("(.+?)\\[(.+?)\\]");
					Matcher m = p.matcher(movieString);
					if (m.find()) {
						movieName=m.group(1).trim();
						characName=m.group(2).trim();
						
						
						//对character name进行处理，过滤
						if (characName.contains("himself")||characName.contains("Himself")||characName.contains("Themselves")||characName.contains("Narrator")||characName.contains("Guest Appearance")||characName.contains("Presenter")||characName.contains("Co-starring")||characName.contains("segment")) {
							characName="null";
						}
						characName=characName.replaceAll("\\(\\d+?\\)", "").trim();
						characName=characName.replaceAll("\\(.+?\\)", "").trim();
						characName=characName.replaceAll("\\s-\\s.+", "").trim();
						characName=characName.replaceAll(",.*", "").trim();
						
						
						if (characName.trim().length()==0) {
							characName="null";
						}
						System.out.println(actorNm+" "+line);
						System.out.println("   movie name: "+movieName);
						System.out.println("   character:  "+characName);
					}
				}
				else {
					movieName=movieString;
					
				}
				
				
					//将actor name加入我们结构中
				Integer ActorID=null;
	    		if (ActorNameToID.containsKey(actorNm)) {
					ActorID=ActorNameToID.get(actorNm);
//					System.out.println("here2  "+ActorID);
				}
	    		else {
	    			ActorID=ActorNameToID.size();
					ActorNameToID.put(actorNm, ActorID);
					ActorIDToName.put(ActorID, actorNm);
				}
				
				
	    		
	    		//将movie name加入我们结构中
	    		Integer MovieID=null;
	    		if (MovieTitleToID.containsKey(movieName)) {
					MovieID=MovieTitleToID.get(movieName);
				}
	    		else {
					MovieID=MovieTitleToID.size();
					MovieTitleToID.put(movieName, MovieID);
					MovieIDToTitle.put(MovieID, movieName);
				}
	    		
	    		//加入他们之间关系
	    		if (MovieIDToActorIDList.containsKey(MovieID)) {
	    			ArrayList<Integer> ActorIDList=MovieIDToActorIDList.get(MovieID);
	    			ActorIDList.add(ActorID);
				}
	    		else {
	    			ArrayList<Integer> ActorIDList=new ArrayList<Integer>();
	    			ActorIDList.add(ActorID);
	    			MovieIDToActorIDList.put(MovieID, ActorIDList);
				}
	    		
							
				
	    		Integer CharacterID=null;
	    		if (!characName.equals("null")) {
	    			//将Character name加入我们结构中
		    		
		    		if (CharacterNameToID.containsKey(characName)) {
		    			CharacterID=CharacterNameToID.get(characName);
					}
		    		else {
		    			CharacterID=CharacterNameToID.size();
		    			CharacterNameToID.put(characName, CharacterID);
						CharacterIDToName.put(CharacterID, characName);
					}
				}
	    		
	    		
	    		
	    		if (CharacterID!=null) {
					if (MovieIDToCharacterIDList.containsKey(MovieID)) {
					ArrayList<Integer> charIDList=MovieIDToCharacterIDList.get(MovieID);
					charIDList.add(CharacterID);
				}
	    		else {
	    			ArrayList<Integer> charIDList=new ArrayList<Integer>();
	    			charIDList.add(CharacterID);
	    			MovieIDToCharacterIDList.put(MovieID, charIDList);
				}
				}
	    		
				
			}
	    	if (lineNo>16868926) {
	    		
				break;
			}
		
	    	lineNo++;
		  
	  }
		
	  
	  //test
//	  Integer actorId=ActorNameToID.get("Evans, Chris (V)");
//	  for (Integer movieid : MovieIDToActorIDList.keySet()) {
//		  
//		for (Integer actInteger : MovieIDToActorIDList.get(movieid)) {
//			if (actInteger.equals(actorId)) {
//				System.out.println(MovieIDToTitle.get(movieid));
//			}
//		}
//	}
	  
	}
	
	//用来存储Actresses.list信息
	public void WriteToFileActresses() throws IOException {
			
			
		    StringBuilder movieactorBuilder=new StringBuilder();
		    for (Integer PaperID : MovieIDToActorIDList.keySet()) {
		    	
		    	ArrayList<Integer> authorIDList=MovieIDToActorIDList.get(PaperID);
		    	for (Integer integer : authorIDList) {
		    		movieactorBuilder.append(PaperID.toString()+"\t"+integer.toString()+"\n");
				}
		    	
			}
		    FileWriter fileWriter2=new FileWriter(".\\IMDb\\GeneratedData\\IMDBtransformedData\\MovieActor2");
			fileWriter2.write(movieactorBuilder.toString());
			fileWriter2.close();
			
			StringBuilder authorBuilder=new StringBuilder();
			for (Integer authorID : ActorIDToName.keySet()) {
				authorBuilder.append(authorID.toString()+"\t"+ActorIDToName.get(authorID)+"\n");
			}
			FileWriter fileWriter3=new FileWriter(".\\IMDb\\GeneratedData\\IMDBtransformedData\\Actor2");
			fileWriter3.write(authorBuilder.toString());
			fileWriter3.close();
			
			StringBuilder paperauthorBuilder=new StringBuilder();
		    for (Integer PaperID : MovieIDToCharacterIDList.keySet()) {
		    	
		    	ArrayList<Integer> authorIDList=MovieIDToCharacterIDList.get(PaperID);
		    	for (Integer integer : authorIDList) {
					paperauthorBuilder.append(PaperID.toString()+"\t"+integer.toString()+"\n");
				}
		    	
			}
		    FileWriter fileWriter4=new FileWriter(".\\IMDb\\GeneratedData\\IMDBtransformedData\\MovieCharacter2");
		    fileWriter4.write(paperauthorBuilder.toString());
		    fileWriter4.close();
			
		    
		    StringBuilder venueBuilder=new StringBuilder();
			for (Integer venueID : CharacterIDToName.keySet()) {
				venueBuilder.append(venueID+"\t"+CharacterIDToName.get(venueID)+"\n");
			}
			FileWriter fileWriter5=new FileWriter(".\\IMDb\\GeneratedData\\IMDBtransformedData\\Character2");
			fileWriter5.write(venueBuilder.toString());
			fileWriter5.close();
		}
		
	//用来存储Actors.list信息
	public void WriteToFileActors() throws IOException {
		
		//只保留candidate entity相关的信息
		for (String	candiName : authorNameToScore.keySet()) {
			if (!candiActorIDSet.contains(ActorNameToID.get(candiName))) {
				candiActorIDSet.add(ActorNameToID.get(candiName));
			}
			else {
				System.err.println("duplicate candidate actor id");
			}
			
		}
		
		//保存这些candidate actor参演过的Movie ids
		HashSet<Integer> filteredMovieIDSet=new HashSet<Integer>();
		
		//根据candidate entity id 找出所有他们参演的movie id
		for (Integer candiActorID : candiActorIDSet) {
			for (Integer movieid : MovieIDToActorIDList.keySet()) {
				for (Integer actInteger : MovieIDToActorIDList.get(movieid)) {
					if (actInteger.equals(candiActorID)) {
						filteredMovieIDSet.add(movieid);
					}
				}
			}
		}
		
		
		StringBuilder movietitleBuilder=new StringBuilder();
		for (Integer PaperID : MovieIDToTitle.keySet()) {
			if (filteredMovieIDSet.contains(PaperID)) {
				movietitleBuilder.append(PaperID.toString()+"\t"+MovieIDToTitle.get(PaperID)+"\n");
		
			}
		}
	    FileWriter fileWriter1=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieTitle");
	    fileWriter1.write(movietitleBuilder.toString());
	    fileWriter1.close();
	    
	    StringBuilder movieactorBuilder=new StringBuilder();
	    for (Integer PaperID : MovieIDToActorIDList.keySet()) {
	    	if (filteredMovieIDSet.contains(PaperID)) {
	    	ArrayList<Integer> authorIDList=MovieIDToActorIDList.get(PaperID);
	    	for (Integer integer : authorIDList) {
	    		movieactorBuilder.append(PaperID.toString()+"\t"+integer.toString()+"\n");
			}
	    	}
		}
	    FileWriter fileWriter2=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieActor");
		fileWriter2.write(movieactorBuilder.toString());
		fileWriter2.close();
		
		StringBuilder authorBuilder=new StringBuilder();
		for (Integer authorID : ActorIDToName.keySet()) {
			authorBuilder.append(authorID.toString()+"\t"+ActorIDToName.get(authorID)+"\n");
		}
		FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\Actor");
		fileWriter3.write(authorBuilder.toString());
		fileWriter3.close();
		
		StringBuilder paperauthorBuilder=new StringBuilder();
	    for (Integer PaperID : MovieIDToCharacterIDList.keySet()) {
	    	if (filteredMovieIDSet.contains(PaperID)) {
	    	ArrayList<Integer> authorIDList=MovieIDToCharacterIDList.get(PaperID);
	    	for (Integer integer : authorIDList) {
				paperauthorBuilder.append(PaperID.toString()+"\t"+integer.toString()+"\n");
			}
	    	}
		}
	    FileWriter fileWriter4=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieCharacter");
	    fileWriter4.write(paperauthorBuilder.toString());
	    fileWriter4.close();
		
	    
	    StringBuilder venueBuilder=new StringBuilder();
		for (Integer venueID : CharacterIDToName.keySet()) {
			venueBuilder.append(venueID+"\t"+CharacterIDToName.get(venueID)+"\n");
		}
		FileWriter fileWriter5=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\Character");
		fileWriter5.write(venueBuilder.toString());
		fileWriter5.close();
	}
	
	
	public void ReadDBLPfromOurStore() throws IOException {
		System.out.println("Read from our store... ...");
		FileReader fr1 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieTitle");
	    BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;
	   
	    while((line1 = br1.readLine())!=null){
	    	String[] strings=line1.split("\t");
	    	if (strings.length==2) {
	    		Integer paperID=Integer.parseInt(strings[0]);
	    		String title=strings[1];
	    		if (MovieIDToTitle.containsKey(paperID)) {
					System.err.println("");
				}
	    		else {
					
	    			MovieIDToTitle.put(paperID, title);
				}
			}
	    	else {
				System.err.println("hhhh1  "+line1);
			}
	    	
	    }
	    br1.close();
	    fr1.close();
	    
	    FileReader fr2 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieActor");
	    BufferedReader br2 = new BufferedReader(fr2);
	    String line2 = null;
	   
	    while((line2 = br2.readLine())!=null){
	    	String[] strings=line2.split("\t");
	    	if (strings.length==2) {
	    		Integer paperID=Integer.parseInt(strings[0]);
	    		Integer authorID=Integer.parseInt(strings[1]);
	    		if (MovieIDToActorIDList.containsKey(paperID)) {
	    			MovieIDToActorIDList.get(paperID).add(authorID);
				}
	    		else {
					ArrayList<Integer> arrayList=new ArrayList<Integer>();
					arrayList.add(authorID);
					MovieIDToActorIDList.put(paperID, arrayList);
				}

	    		
			}
	    	else {
				System.err.println("hhhh2  "+line2);
			}
	    	
	    }
	    br2.close();
	    fr2.close();
	    
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
	    
	    FileReader fr4 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieCharacter");
	    BufferedReader br4 = new BufferedReader(fr4);
	    String line4 = null;
	   
	    while((line4 = br4.readLine())!=null){
	    	String[] strings=line4.split("\t");
	    	if (strings.length==2) {
	    		Integer paperID=Integer.parseInt(strings[0]);
	    		Integer authorID=Integer.parseInt(strings[1]);
	    		if (MovieIDToCharacterIDList.containsKey(paperID)) {
	    			MovieIDToCharacterIDList.get(paperID).add(authorID);
				}
	    		else {
					ArrayList<Integer> arrayList=new ArrayList<Integer>();
					arrayList.add(authorID);
					MovieIDToCharacterIDList.put(paperID, arrayList);
				}
			}
	    	else {
				System.err.println("hhhh4  "+line4);
			}
	    	
	    }
	    br4.close();
	    fr4.close();
		
		
	    FileReader fr5 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\Character");
	    BufferedReader br5 = new BufferedReader(fr5);
	    String line5 = null;
	   
	    while((line5 = br5.readLine())!=null){
	    	String[] strings=line5.split("\t");
	    	if (strings.length==2) {
	    		CharacterIDToName.put(Integer.parseInt(strings[0]), strings[1]);
			}
	    	else {
				System.err.println("hhhh5   "+line5);
			}
	    	
	    }
	    br5.close();
	    fr5.close();
	    
		
	    FileReader fr6 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\ActorBioTerm");
	    BufferedReader br6 = new BufferedReader(fr6);
	    String line6 = null;
	   
	    while((line6 = br6.readLine())!=null){
	    	String[] strings=line6.split("\t");
	    	if (strings.length==2) {
	    		Integer paperID=Integer.parseInt(strings[0]);
	    		Integer authorID=Integer.parseInt(strings[1]);
	    		if (ActorIDToBioTermIDList.containsKey(paperID)) {
	    			ActorIDToBioTermIDList.get(paperID).add(authorID);
				}
	    		else {
					ArrayList<Integer> arrayList=new ArrayList<Integer>();
					arrayList.add(authorID);
					ActorIDToBioTermIDList.put(paperID, arrayList);
				}
			}
	    	else {
				System.err.println("hhhh4  "+line4);
			}
	    	
	    }
	    br6.close();
	    fr6.close();
	    
	    FileReader fr7 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\Term");
	    BufferedReader br7 = new BufferedReader(fr7);
	    String line7 = null;
	   
	    while((line7 = br7.readLine())!=null){
	    	String[] strings=line7.split("\t");
	    	if (strings.length==2) {
	    		TermIDToName.put(Integer.parseInt(strings[0]), strings[1]);
			}
	    	else {
				System.err.println("hhhh3  "+line7);
			}
	    	
	    }
	    br7.close();
	    fr7.close();
		
	    
	    FileReader fr8 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieDirector");
	    BufferedReader br8 = new BufferedReader(fr8);
	    String line8 = null;
	   
	    while((line8 = br8.readLine())!=null){
	    	String[] strings=line8.split("\t");
	    	if (strings.length==2) {
	    		Integer paperID=Integer.parseInt(strings[0]);
	    		Integer authorID=Integer.parseInt(strings[1]);
	    		if (MovieIDToDirectorIDList.containsKey(paperID)) {
	    			MovieIDToDirectorIDList.get(paperID).add(authorID);
				}
	    		else {
					ArrayList<Integer> arrayList=new ArrayList<Integer>();
					arrayList.add(authorID);
					MovieIDToDirectorIDList.put(paperID, arrayList);
				}
			}
	    	else {
				System.err.println("hhhh4  "+line4);
			}
	    	
	    }
	    br8.close();
	    fr8.close();
	    
	    FileReader fr9 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieKeywordTerm");
	    BufferedReader br9 = new BufferedReader(fr9);
	    String line9 = null;
	   
	    while((line9 = br9.readLine())!=null){
	    	String[] strings=line9.split("\t");
	    	if (strings.length==2) {
	    		Integer paperID=Integer.parseInt(strings[0]);
	    		Integer authorID=Integer.parseInt(strings[1]);
	    		if (MovieIDToKeywordTermIDList.containsKey(paperID)) {
	    			MovieIDToKeywordTermIDList.get(paperID).add(authorID);
				}
	    		else {
					ArrayList<Integer> arrayList=new ArrayList<Integer>();
					arrayList.add(authorID);
					MovieIDToKeywordTermIDList.put(paperID, arrayList);
				}
			}
	    	else {
				System.err.println("hhhh4  "+line4);
			}
	    	
	    }
	    br9.close();
	    fr9.close();
	    
	    System.out.println("Read from our store: OK");
	    
	    

//		  Integer actorId=556368;
//		  for (Integer movieid : MovieIDToActorIDList.keySet()) {
//			  
//			for (Integer actInteger : MovieIDToActorIDList.get(movieid)) {
//				if (actInteger.equals(actorId)) {
//					System.out.println(MovieIDToTitle.get(movieid));
//				}
//			}
//		}
	}
	
	public static void main(String[]args)throws Exception{
		PreprocessIMDb preprocessIMDb=new PreprocessIMDb();
		preprocessIMDb.ReadIMDBactors();
		preprocessIMDb.WriteToFileActors();
		
//		preprocessIMDb.ReadDBLPfromOurStore();
		
//		preprocessIMDb.ReadIMDBactresses();
//	    preprocessIMDb.WriteToFileActresses();
	    
	}

}
