package IMDbPreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PreprocessMovieKeyword {
	
	
	public HashMap<Integer, String> MovieIDToTitle=new HashMap<Integer, String>();
	public HashMap<String, Integer> MovieTitleToID=new HashMap<String, Integer>();
	
	HashMap<Integer, String> TermIDToName=new HashMap<Integer, String>();
	HashMap<String, Integer> TermNameToID=new HashMap<String, Integer>();
	HashMap<Integer, ArrayList<Integer>> MovieIDToKeywordTermIDList=new HashMap<Integer, ArrayList<Integer>>();
	
	
	public void ReadIMDBmoviekeywords() throws IOException {
		
		HashSet<String> stopwordlistHashSet=new HashSet<String>();
		
		//read stop word list
		FileReader fr11 = new FileReader(".\\newIMDB\\stopwordlist.txt");
	    BufferedReader br11 = new BufferedReader(fr11);
	    String line11 = null;
	   
	    while((line11 = br11.readLine())!=null){
	    	stopwordlistHashSet.add(line11.trim());
	    	
	    }
	    br11.close();
	    fr11.close();
	    
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
		
	   System.out.println(MovieIDToTitle.size());
	    
	    
	    FileReader fr3 = new FileReader(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\Term");
	    BufferedReader br3 = new BufferedReader(fr3);
	    String line3 = null;
	   
	    while((line3 = br3.readLine())!=null){
	    	String[] strings=line3.split("\t");
	    	if (strings.length==2) {
	    		TermIDToName.put(Integer.parseInt(strings[0]), strings[1]);
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
		
		for (Integer integer : TermIDToName.keySet()) {
			if (!TermNameToID.containsKey(TermIDToName.get(integer))) {
				TermNameToID.put(TermIDToName.get(integer), integer);
			}
			else {
				System.err.println("duplicate term");
			}
		}
		
		File f=new File(".\\newIMDB\\IMDbData\\keywords.list");
		
		InputStreamReader write = new InputStreamReader(new FileInputStream(f.getAbsolutePath()));
		BufferedReader br=new BufferedReader(write);
		String line;
		int lineNo=1;
		
		 while ((line = br.readLine() )!=null) {
			  
			  if (lineNo>=69171) {
				  
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
				   	
					
				   	String movieName="null";
				   	String keywordsString="null";
				   	
					
				   	movieName=a[0].trim();	
					keywordsString=a[a.length-1].trim();
					
					if (MovieTitleToID.containsKey(movieName)) {
						
//						if (!movieName.equals("Walk Hard: The Dewey Cox Story (2007)")) {
//							continue;
//						}
						
						
						Integer MovieID=MovieTitleToID.get(movieName);
						
						keywordsString=keywordsString.toLowerCase();
						keywordsString=keywordsString.replaceAll("\\W", " ");
						keywordsString=keywordsString.replaceAll("_", " ");
						keywordsString=keywordsString.replaceAll("\\s+", " ");
						
//						System.out.print(keywordsString+":::::::::");
						
						
						String[] titleTerms=keywordsString.split(" ");
						
						for (String titleTerm : titleTerms) {
//							System.out.print(titleTerm+"+");
							
							
							
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

									     
//									   System.out.print(titleTerm+" ");
									
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
										if (MovieIDToKeywordTermIDList.containsKey(MovieID)) {
											ArrayList<Integer> termIDList=MovieIDToKeywordTermIDList.get(MovieID);
											termIDList.add(termID);
										}
										else {
											ArrayList<Integer> termIDList=new ArrayList<Integer>();
											termIDList.add(termID);
											MovieIDToKeywordTermIDList.put(MovieID, termIDList);
											
										}
									}
									else {
										System.out.println("errrr1");
									}
									
								}
								
							}
							
						}
						
						
					}
				   	
			  }
			  
			  			  
			  lineNo++;
		  }
		 
		 
		 System.out.println(MovieIDToKeywordTermIDList.size());
		 
		 
		 for (Integer integer	 : MovieIDToTitle.keySet()) {
			if (MovieIDToKeywordTermIDList.containsKey(integer)) {
				
			}
			else {
				System.out.println(integer);
			}
		}
		 StringBuilder paperauthorBuilder=new StringBuilder();
		    for (Integer PaperID : MovieIDToKeywordTermIDList.keySet()) {
		    	ArrayList<Integer> authorIDList=MovieIDToKeywordTermIDList.get(PaperID);
		    	for (Integer integer : authorIDList) {
					paperauthorBuilder.append(PaperID.toString()+"\t"+integer.toString()+"\n");
				}

			}
		    FileWriter fileWriter2=new FileWriter(".\\newIMDB\\GeneratedData\\IMDBtransformedData\\MovieKeywordTerm");
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
		PreprocessMovieKeyword preprocessActorBio=new PreprocessMovieKeyword();
		preprocessActorBio.ReadIMDBmoviekeywords();
		
		
	}

}
