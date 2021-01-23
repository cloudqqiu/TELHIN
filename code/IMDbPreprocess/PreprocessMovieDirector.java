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

public class PreprocessMovieDirector {
	
	
	
	public HashMap<Integer, String> MovieIDToTitle=new HashMap<Integer, String>();
	public HashMap<String, Integer> MovieTitleToID=new HashMap<String, Integer>();
	
	public HashMap<Integer, String> ActorIDToName=new HashMap<Integer, String>();
	public HashMap<String, Integer> ActorNameToID=new HashMap<String, Integer>();
	
	
	public HashMap<Integer, ArrayList<Integer>> MovieIDToDirectorIDList=new HashMap<Integer, ArrayList<Integer>>();
	
	
	public void ReadIMDBmoviedirector() throws IOException {
		
		
	    
		FileReader fr1 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieTitle");
	    BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;
	   
	    while((line1 = br1.readLine())!=null){
	    	String[] strings=line1.split("\t");
	    	if (strings.length==2) {
	    		Integer paperID=Integer.parseInt(strings[0]);
	    		String title=strings[1];
	    		if (MovieIDToTitle.containsKey(paperID)) {
					System.err.println("333333333333");
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
		
//	   System.out.println(MovieIDToTitle.size());
	    
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
				System.err.println("duplicate movie title");
			}
			
		}
		
		
		File f=new File(".\\newIMDB\\IMDbData\\directors.list");
		
		InputStreamReader write = new InputStreamReader(new FileInputStream(f.getAbsolutePath()));
		BufferedReader br=new BufferedReader(write);
		String line;
		int lineNo=1;
		
		String directorName="null";
		
		 while ((line = br.readLine() )!=null) {
			  
			  if (lineNo>=236) {
				  
				  String[] a=line.split("\t");
				   	
				   	if (a.length<2) {
				   		lineNo++;
						continue;
					}
				   	
				   	
//				   	System.out.print(lineNo+": "+line+": a.length: "+a.length+"    ");
//				   	for (int i = 0; i < a.length; i++) {
//						System.out.print(a[i]+"-length"+a[i].length()+"£º  ");
//					} 	
//				   	System.out.println();
			
				   	
				   if (a[0].length()>0) {
						directorName=a[0].trim();	
				}
				   
				   
				 //È¥³ýMovieÖÐµÄ(uncredited)  (as Alex Frank) (co-director)  (attached) (supervising director) (collaborating director) (second director)
					//(exterior scenes) (outdoors) (opening sequence) (segment "Kakvo ti stava") (unconfirmed)
					
				   
				   String movieString=a[a.length-1].trim();
				   movieString=movieString.replaceAll("\\([A-Za-z\\-\\ /]*?director.*?\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(uncredited\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(as .+?\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(attached\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(unconfirmed\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(exterior scenes\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(outdoors\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(opening sequence\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(segment .+?\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(film segments\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(stage direction\\)", " ").trim();
				   movieString=movieString.replaceAll("\\(English version\\)", " ").trim();


				   
				   
					if (MovieTitleToID.containsKey(movieString)) {
						
//						if (!movieString.equals("Walk Hard: The Dewey Cox Story (2007)")) {
//							continue;
//						}
						
						
						Integer MovieID=MovieTitleToID.get(movieString);
						
//						System.out.println(movieString);
						
						
						Integer ActorID=null;
			    		if (ActorNameToID.containsKey(directorName)) {
							ActorID=ActorNameToID.get(directorName);
//							System.out.println("here2  "+directorName);
						}
			    		else {
			    			ActorID=ActorNameToID.size();
							ActorNameToID.put(directorName, ActorID);
							ActorIDToName.put(ActorID, directorName);
						}
						
			    		if (MovieIDToDirectorIDList.containsKey(MovieID)) {
			    			ArrayList<Integer> ActorIDList=MovieIDToDirectorIDList.get(MovieID);
			    			ActorIDList.add(ActorID);
						}
			    		else {
			    			ArrayList<Integer> ActorIDList=new ArrayList<Integer>();
			    			ActorIDList.add(ActorID);
			    			MovieIDToDirectorIDList.put(MovieID, ActorIDList);
						}
						
					}
				   	
			  }
			  
			  		
			  if (lineNo>2509995) {
		    		
					break;
				}
			  lineNo++;
		  }
		 
		 System.out.println(MovieIDToTitle.size());
		 System.out.println(MovieIDToDirectorIDList.size());
		 
//		 for (Integer movieInteger : MovieIDToTitle.keySet()) {
//			if (!MovieIDToDirectorIDList.containsKey(movieInteger)) {
//				System.out.println(MovieIDToTitle.get(movieInteger));
//			}
//		}
		 
		 StringBuilder paperauthorBuilder=new StringBuilder();
		    for (Integer PaperID : MovieIDToDirectorIDList.keySet()) {
		    	ArrayList<Integer> authorIDList=MovieIDToDirectorIDList.get(PaperID);
		    	for (Integer integer : authorIDList) {
					paperauthorBuilder.append(PaperID.toString()+"\t"+integer.toString()+"\n");
				}

			}
		    FileWriter fileWriter2=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieDirector");
			fileWriter2.write(paperauthorBuilder.toString());
			fileWriter2.close();

			StringBuilder authorBuilder=new StringBuilder();
			for (Integer authorID : ActorIDToName.keySet()) {
				authorBuilder.append(authorID.toString()+"\t"+ActorIDToName.get(authorID)+"\n");
			}
			FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\Actor2");
			fileWriter3.write(authorBuilder.toString());
			fileWriter3.close();
			
	}
	
	
	public static void main(String[]args)throws Exception{
		PreprocessMovieDirector preprocessMovieDirector=new PreprocessMovieDirector();
		preprocessMovieDirector.ReadIMDBmoviedirector();
		
		
	}

}
