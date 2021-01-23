package IMDbPreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatCodePointException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class MetapathRW {
	
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
	HashMap<Integer, ArrayList<Integer>> MovieIDToKeywordTermIDList=new HashMap<Integer, ArrayList<Integer>>();
	
	public HashMap<String, Double> authorNameToScore=new HashMap<String, Double>();
	public HashMap<String, ArrayList<String>> MentionCandidateNames=new HashMap<String, ArrayList<String>>();
	
	public HashMap<String, Double> NametoScore;
	
	public void readGraphFromDisk() throws IOException {
		PreprocessIMDb preprocessDBLP=new PreprocessIMDb();
		preprocessDBLP.ReadDBLPfromOurStore();
		
		ActorIDToName=preprocessDBLP.ActorIDToName;
		MovieIDToTitle=preprocessDBLP.MovieIDToTitle;
		MovieIDToActorIDList=preprocessDBLP.MovieIDToActorIDList;
		MovieIDToCharacterIDList=preprocessDBLP.MovieIDToCharacterIDList;
		CharacterIDToName=preprocessDBLP.CharacterIDToName;
		ActorIDToBioTermIDList=preprocessDBLP.ActorIDToBioTermIDList;
		TermIDToName=preprocessDBLP.TermIDToName;
		MovieIDToDirectorIDList=preprocessDBLP.MovieIDToDirectorIDList;
		MovieIDToKeywordTermIDList=preprocessDBLP.MovieIDToKeywordTermIDList;
		
		for (Integer integer : ActorIDToName.keySet()) {
			if (!ActorNameToID.containsKey(ActorIDToName.get(integer))) {
				ActorNameToID.put(ActorIDToName.get(integer), integer);
			}
			else {
//				System.err.println("duplicate actor title  "+ActorIDToName.get(integer));
			}
			
		}
		
		for (Integer paperID : MovieIDToActorIDList.keySet()) {
			ArrayList<Integer> authorIDList=MovieIDToActorIDList.get(paperID);
			
			for (Integer authorID : authorIDList) {
				if (ActorIDToMovieIDList.containsKey(authorID)) {
					ActorIDToMovieIDList.get(authorID).add(paperID);
				}
				else {
					ArrayList<Integer> paperIDList=new ArrayList<Integer>();
					paperIDList.add(paperID);
					ActorIDToMovieIDList.put(authorID, paperIDList);
				}
			}
			
			
		}
		
//		System.out.println("ActorIDToName.size(): "+ActorIDToName.size());
		System.out.println("ActorIDToMovieIDList.size(): "+ActorIDToMovieIDList.size());
//		System.out.println(ActorIDToBioTermIDList.size());
		
	}
	
	public void geneCandidates() throws IOException {
		CandidateGeneration candidateGeneration=new CandidateGeneration();
		candidateGeneration.ReadCandidates();
		candidateGeneration.ReadPriorPageRankScore();
		
		MentionCandidateNames=candidateGeneration.MentionCandidateNames;
		authorNameToScore=candidateGeneration.authorNameToScore;
		
		System.out.println(MentionCandidateNames.size());
		System.out.println(authorNameToScore.size());

	}
	
	
	public void randomWalkAlongMetapath(String metapath) throws IOException {
			
			
			
			for (String mention : MentionCandidateNames.keySet()) {
				
				
				
				ArrayList<String> canNames=MentionCandidateNames.get(mention);
				
	//			System.out.println("mention: "+mention+"   candidate number: "+canIDs.size());
				
				for (String canName: canNames) {
					
					
					
					Integer candiID = ActorNameToID.get(canName);
					
//					if (!ActorIDToName.get(candiID).equals("Evans, Chris (V)")) {
//						continue;
//					}
					
//					System.out.println(candiID);
//					System.out.println(metapath+"   "+ActorIDToName.get(candiID));
					
										
					if (metapath.equals("AB")) {
						HashMap<Integer, Double> IDtoScore=new HashMap<Integer, Double>();
						
						if (ActorIDToBioTermIDList.containsKey(candiID)) {
							ArrayList<Integer> biotermIDList=ActorIDToBioTermIDList.get(candiID);
							Double contriScore=1.0/(double)biotermIDList.size();
							for (Integer integer : biotermIDList) {
								if (IDtoScore.containsKey(integer)) {
									IDtoScore.put(integer, IDtoScore.get(integer)+contriScore);
								}
								else {
									IDtoScore.put(integer, contriScore);
								}
							}
							
							//normalize to sum 1
							double score=0.0;
							for (Integer integer : IDtoScore.keySet()) {
								score+=IDtoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (Integer integer : IDtoScore.keySet()) {
								IDtoScore.put(integer, IDtoScore.get(integer)/score);
							}
							
							StringBuilder authorBuilder=new StringBuilder();
							for (Integer integer : IDtoScore.keySet()) {
								authorBuilder.append(TermIDToName.get(integer)+"\t"+IDtoScore.get(integer)+"\n");
							
							}
							FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\Distribution\\"+metapath+"\\"+ActorIDToName.get(candiID));
							fileWriter3.write(authorBuilder.toString());
							fileWriter3.close();
							
						}
						
						
					}
					else if (metapath.equals("AMK")) {
						
						if (ActorIDToMovieIDList.containsKey(candiID)) {
							HashMap<Integer, Double> IDtoScore=new HashMap<Integer, Double>();
						
							
							ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
							Double contriScore=1.0/(double)movieIDList.size();
							
							
							for (Integer integer : movieIDList) {
								//如果是电视剧，具体哪集没有列出，如Clayton, John (I)的"E Street" (1989)  [Sergeant Roy Harrison]，这种情况下，A-M-C,A-M-D,A-M-A,A-M-K 元路径的随机游走就利用这个
								//信息，害怕产生出很多不相关的关系，
								String movieName=MovieIDToTitle.get(integer);
								if (movieName.startsWith("\"")) {
									if (!(movieName.contains("{")&&movieName.contains("}"))) {
										System.out.println(movieName);
										continue;
									}
									
								}
								if (IDtoScore.containsKey(integer)) {
									IDtoScore.put(integer, IDtoScore.get(integer)+contriScore);
								}
								else {
									IDtoScore.put(integer, contriScore);
								}
							}
							
							//normalize to sum 1
							double score=0.0;
							for (Integer integer : IDtoScore.keySet()) {
								score+=IDtoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (Integer integer : IDtoScore.keySet()) {
								IDtoScore.put(integer, IDtoScore.get(integer)/score);
							}
							
							HashMap<Integer, Double> idScoreTemp=new HashMap<Integer, Double>();
							
							for (Integer beforeID : IDtoScore.keySet()) {
								if (MovieIDToKeywordTermIDList.containsKey(beforeID)) {
									
									ArrayList<Integer> authorIDList=MovieIDToKeywordTermIDList.get(beforeID);
									if (!IDtoScore.containsKey(beforeID)) {
										continue;
									}
									contriScore=IDtoScore.get(beforeID)/(double)authorIDList.size();
								
									
									
									for (Integer paperID : authorIDList) {
										if (idScoreTemp.containsKey(paperID)) {
											idScoreTemp.put(paperID, idScoreTemp.get(paperID)+contriScore);
										}
										else {
											idScoreTemp.put(paperID, contriScore);
										}
									}
									
									
								}
								else {
//									System.err.println("no term id contained  222");
								}
							}
							
							if (idScoreTemp.size()>0) {
								score=0.0;
								for (Integer integer : idScoreTemp.keySet()) {
									score+=idScoreTemp.get(integer);
								}
								
//								System.out.println("score: "+score);
								for (Integer integer : idScoreTemp.keySet()) {
									idScoreTemp.put(integer, idScoreTemp.get(integer)/score);
								}
								
								StringBuilder authorBuilder=new StringBuilder();
								for (Integer integer : idScoreTemp.keySet()) {
									authorBuilder.append(TermIDToName.get(integer)+"\t"+idScoreTemp.get(integer)+"\n");
								
								}
								FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\Distribution\\"+metapath+"\\"+ActorIDToName.get(candiID));
								fileWriter3.write(authorBuilder.toString());
								fileWriter3.close();
							
							
							}
							
							
							
							
						}
					}
					else if (metapath.equals("AMT")) {
						
						if (ActorIDToMovieIDList.containsKey(candiID)) {
							HashMap<String, Double> titletoScore=new HashMap<String, Double>();
						
							
							ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
							Double contriScore=1.0/(double)movieIDList.size();
							
							
							for (Integer integer : movieIDList) {
								String movieString=MovieIDToTitle.get(integer);
								
								//去除movie中的年份，以及电视剧中引号，年份和集
								String movieName="null";
								
								Pattern p = Pattern.compile("\"?(.+?)\"?\\s+\\([\\d/IV\\?]+?\\).*");
								Matcher m = p.matcher(movieString);
								if (m.find()) {
									movieName=m.group(1).trim();
//									System.out.println(movieString+"       "+movieName);
//									System.out.println(movieName);
								}
								
									
								
								if (!movieName.equals("null")) {
									if (titletoScore.containsKey(movieName)) {
										titletoScore.put(movieName, titletoScore.get(movieName)+contriScore);
									}
									else {
										titletoScore.put(movieName, contriScore);
									}
								}
								
							}
							
							//normalize to sum 1
							double score=0.0;
							for (String integer : titletoScore.keySet()) {
								score+=titletoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (String  integer : titletoScore.keySet()) {
								titletoScore.put(integer, titletoScore.get(integer)/score);
							}
							
							
								
								StringBuilder authorBuilder=new StringBuilder();
								for (String integer : titletoScore.keySet()) {
									authorBuilder.append(integer+"\t"+titletoScore.get(integer)+"\n");
								
								}
								FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\Distribution\\"+metapath+"\\"+ActorIDToName.get(candiID));
								fileWriter3.write(authorBuilder.toString());
								fileWriter3.close();
							
							
						}
					}
					else if (metapath.equals("AMC")) {
						
						if (ActorIDToMovieIDList.containsKey(candiID)) {
							HashMap<Integer, Double> IDtoScore=new HashMap<Integer, Double>();
						
							
							ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
							Double contriScore=1.0/(double)movieIDList.size();
							
							
							for (Integer integer : movieIDList) {
								//如果是电视剧，具体哪集没有列出，如Clayton, John (I)的"E Street" (1989)  [Sergeant Roy Harrison]，这种情况下，A-M-C,A-M-D,A-M-A,A-M-K 元路径的随机游走就利用这个
								//信息，害怕产生出很多不相关的关系，
								String movieName=MovieIDToTitle.get(integer);
								if (movieName.startsWith("\"")) {
									if (!(movieName.contains("{")&&movieName.contains("}"))) {
//										System.out.println(movieName);
										continue;
									}
									
								}
								if (IDtoScore.containsKey(integer)) {
									IDtoScore.put(integer, IDtoScore.get(integer)+contriScore);
								}
								else {
									IDtoScore.put(integer, contriScore);
								}
							}
							
							//normalize to sum 1
							double score=0.0;
							for (Integer integer : IDtoScore.keySet()) {
								score+=IDtoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (Integer integer : IDtoScore.keySet()) {
								IDtoScore.put(integer, IDtoScore.get(integer)/score);
							}
							
							HashMap<String, Double> titletoScore=new HashMap<String, Double>();
							
							for (Integer beforeID : IDtoScore.keySet()) {
								if (MovieIDToCharacterIDList.containsKey(beforeID)) {
									
									ArrayList<Integer> authorIDList=MovieIDToCharacterIDList.get(beforeID);
									if (!IDtoScore.containsKey(beforeID)) {
										continue;
									}
									contriScore=IDtoScore.get(beforeID)/(double)authorIDList.size();
								
									
									
									for (Integer paperID : authorIDList) {
										String charString=CharacterIDToName.get(paperID);
										
										if (charString.contains("/")) {
											//当string为Vincent Varela/Freddy Canpil时，可以将其看成两个character
											System.out.println(charString);
											
											String[] a=charString.split("/");
											for (String string : a) {
												if (string.trim().length()>0&&!string.equals("self")&&!string.equals("Themself")) {
													
													String charName=string.trim();
													
													System.out.println("    "+charName);	
													
													
													if (titletoScore.containsKey(charName)) {
														titletoScore.put(charName, titletoScore.get(charName)+contriScore);
													}
													else {
														titletoScore.put(charName, contriScore);
													}
												}
											}
											
										}
										else {
											String charName=charString.trim();
											
											if (charName.length()>0&&!charName.equals("self")&&!charName.equals("Themself")) {
												if (titletoScore.containsKey(charName)) {
												titletoScore.put(charName, titletoScore.get(charName)+contriScore);
											}
											else {
												titletoScore.put(charName, contriScore);
											}
											}
											
											
										}
										
									}
									
									
								}
								else {
//									System.err.println("no term id contained  222");
								}
							}
							
							if (titletoScore.size()==0) {
								continue;
							}
							//normalize to sum 1
							score=0.0;
							for (String integer : titletoScore.keySet()) {
								score+=titletoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (String  integer : titletoScore.keySet()) {
								titletoScore.put(integer, titletoScore.get(integer)/score);
							}
							
							
								
								StringBuilder authorBuilder=new StringBuilder();
								for (String integer : titletoScore.keySet()) {
									authorBuilder.append(integer+"\t"+titletoScore.get(integer)+"\n");
								
								}
								FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\Distribution\\"+metapath+"\\"+ActorIDToName.get(candiID));
								fileWriter3.write(authorBuilder.toString());
								fileWriter3.close();
							
							
							
							
						}
					}
					else if (metapath.equals("AMA")) {
						
						if (ActorIDToMovieIDList.containsKey(candiID)) {
							HashMap<Integer, Double> IDtoScore=new HashMap<Integer, Double>();
						
							
							ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
							Double contriScore=1.0/(double)movieIDList.size();
							
							
							for (Integer integer : movieIDList) {
								//如果是电视剧，具体哪集没有列出，如Clayton, John (I)的"E Street" (1989)  [Sergeant Roy Harrison]，这种情况下，A-M-C,A-M-D,A-M-A,A-M-K 元路径的随机游走就利用这个
								//信息，害怕产生出很多不相关的关系，
								String movieName=MovieIDToTitle.get(integer);
								if (movieName.startsWith("\"")) {
									if (!(movieName.contains("{")&&movieName.contains("}"))) {
//										System.out.println(movieName);
										continue;
									}
									
								}
								if (IDtoScore.containsKey(integer)) {
									IDtoScore.put(integer, IDtoScore.get(integer)+contriScore);
								}
								else {
									IDtoScore.put(integer, contriScore);
								}
							}
							
							//normalize to sum 1
							double score=0.0;
							for (Integer integer : IDtoScore.keySet()) {
								score+=IDtoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (Integer integer : IDtoScore.keySet()) {
								IDtoScore.put(integer, IDtoScore.get(integer)/score);
							}
							
							HashMap<String, Double> titletoScore=new HashMap<String, Double>();
							
							for (Integer beforeID : IDtoScore.keySet()) {
								if (MovieIDToActorIDList.containsKey(beforeID)) {
									
									ArrayList<Integer> authorIDList=MovieIDToActorIDList.get(beforeID);
									if (!IDtoScore.containsKey(beforeID)) {
										continue;
									}
									contriScore=IDtoScore.get(beforeID)/(double)authorIDList.size()-1;
								
									
									
									for (Integer paperID : authorIDList) {
										//注意去除自身
										if (paperID.equals(candiID)) {
											continue;
										}
										
										String actorString=ActorIDToName.get(paperID);
										
										
											String actorName="null";
											if (actorString.contains(",")) {
												
												if (actorString.contains("(")) {
													Pattern p = Pattern.compile("(.+?),\\s+(.+)(\\s+\\([IVXLC]+\\))");
													Matcher m = p.matcher(actorString);
													if (m.find()) {
														actorName=m.group(2).trim()+" "+m.group(1).trim();
//														System.out.println(actorString+"       "+actorName);
	
													}
													else {
//														System.out.println(actorString);
													}
												}
												else {
													Pattern p = Pattern.compile("(.+?),\\s+(.+)");
													Matcher m = p.matcher(actorString);
													if (m.find()) {
														actorName=m.group(2).trim()+" "+m.group(1).trim();
//														System.out.println(actorString+"       "+actorName);
	
													}
//													else {
//														System.out.println(actorString);
//													}
												}
												
											}
											else {
//												System.out.println(actorString);
												Pattern p = Pattern.compile("(.+?)\\s\\([IVXLC]+\\)");
												Matcher m = p.matcher(actorString);
												if (m.find()) {
													//名字中无逗号，有罗马数字
													actorName=m.group(1).trim();
//													System.out.println(actorString+"       "+actorName);
//													System.out.println(movieName);
												}
												else {
													//名字中无逗号和罗马数字
//													System.out.println(actorString);
													actorName=actorString;
												}
												
											}
											
											
											
											if (!actorName.equals("null")) {
												if (titletoScore.containsKey(actorName)) {
												titletoScore.put(actorName, titletoScore.get(actorName)+contriScore);
											}
											else {
												titletoScore.put(actorName, contriScore);
											}
											}
											
											
										
										
									}
									
									
								}
								else {
//									System.err.println("no term id contained  222");
								}
							}
							
							if (titletoScore.size()==0) {
								continue;
							}
							//normalize to sum 1
							score=0.0;
							for (String integer : titletoScore.keySet()) {
								score+=titletoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (String  integer : titletoScore.keySet()) {
								titletoScore.put(integer, titletoScore.get(integer)/score);
							}
							
							
								
								StringBuilder authorBuilder=new StringBuilder();
								for (String integer : titletoScore.keySet()) {
									authorBuilder.append(integer+"\t"+titletoScore.get(integer)+"\n");
								
								}
								FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\Distribution\\"+metapath+"\\"+ActorIDToName.get(candiID));
								fileWriter3.write(authorBuilder.toString());
								fileWriter3.close();
							
							
							
							
						}
					}
					else if (metapath.equals("AMD")) {
						
						if (ActorIDToMovieIDList.containsKey(candiID)) {
							HashMap<Integer, Double> IDtoScore=new HashMap<Integer, Double>();
						
							
							ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
							Double contriScore=1.0/(double)movieIDList.size();
							
							
							for (Integer integer : movieIDList) {
								//如果是电视剧，具体哪集没有列出，如Clayton, John (I)的"E Street" (1989)  [Sergeant Roy Harrison]，这种情况下，A-M-C,A-M-D,A-M-A,A-M-K 元路径的随机游走就利用这个
								//信息，害怕产生出很多不相关的关系，
								String movieName=MovieIDToTitle.get(integer);
								if (movieName.startsWith("\"")) {
									if (!(movieName.contains("{")&&movieName.contains("}"))) {
//										System.out.println(movieName);
										continue;
									}
									
								}
								if (IDtoScore.containsKey(integer)) {
									IDtoScore.put(integer, IDtoScore.get(integer)+contriScore);
								}
								else {
									IDtoScore.put(integer, contriScore);
								}
							}
							
							//normalize to sum 1
							double score=0.0;
							for (Integer integer : IDtoScore.keySet()) {
								score+=IDtoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (Integer integer : IDtoScore.keySet()) {
								IDtoScore.put(integer, IDtoScore.get(integer)/score);
							}
							
							HashMap<String, Double> titletoScore=new HashMap<String, Double>();
							
							for (Integer beforeID : IDtoScore.keySet()) {
								if (MovieIDToDirectorIDList.containsKey(beforeID)) {
									
									ArrayList<Integer> authorIDList=MovieIDToDirectorIDList.get(beforeID);
									if (!IDtoScore.containsKey(beforeID)) {
										continue;
									}
									contriScore=IDtoScore.get(beforeID)/(double)authorIDList.size();
								
									
									
									for (Integer paperID : authorIDList) {
										//注意去除自身
																				
										String actorString=ActorIDToName.get(paperID);
										
										
											String actorName="null";
											if (actorString.contains(",")) {
												
												if (actorString.contains("(")) {
													Pattern p = Pattern.compile("(.+?),\\s+(.+)(\\s+\\([IVXLC]+\\))");
													Matcher m = p.matcher(actorString);
													if (m.find()) {
														actorName=m.group(2).trim()+" "+m.group(1).trim();
//														System.out.println(actorString+"       "+actorName);
	
													}
													else {
//														System.out.println(actorString);
													}
												}
												else {
													Pattern p = Pattern.compile("(.+?),\\s+(.+)");
													Matcher m = p.matcher(actorString);
													if (m.find()) {
														actorName=m.group(2).trim()+" "+m.group(1).trim();
//														System.out.println(actorString+"       "+actorName);
	
													}
//													else {
//														System.out.println(actorString);
//													}
												}
												
											}
											else {
//												System.out.println(actorString);
												Pattern p = Pattern.compile("(.+?)\\s\\([IVXLC]+\\)");
												Matcher m = p.matcher(actorString);
												if (m.find()) {
													//名字中无逗号，有罗马数字
													actorName=m.group(1).trim();
//													System.out.println(actorString+"       "+actorName);
//													System.out.println(movieName);
												}
												else {
													//名字中无逗号和罗马数字
//													System.out.println(actorString);
													actorName=actorString;
												}
												
											}
											
											
											
											if (!actorName.equals("null")) {
												if (titletoScore.containsKey(actorName)) {
												titletoScore.put(actorName, titletoScore.get(actorName)+contriScore);
											}
											else {
												titletoScore.put(actorName, contriScore);
											}
											}
											
											
										
										
									}
									
									
								}
								else {
//									System.err.println("no term id contained  222");
								}
							}
							
							if (titletoScore.size()==0) {
								continue;
							}
							//normalize to sum 1
							score=0.0;
							for (String integer : titletoScore.keySet()) {
								score+=titletoScore.get(integer);
							}
							
//							System.out.println("score: "+score);
							for (String  integer : titletoScore.keySet()) {
								titletoScore.put(integer, titletoScore.get(integer)/score);
							}
							
							
								
								StringBuilder authorBuilder=new StringBuilder();
								for (String integer : titletoScore.keySet()) {
									authorBuilder.append(integer+"\t"+titletoScore.get(integer)+"\n");
								
								}
								FileWriter fileWriter3=new FileWriter(".\\newIMDB\\GeneratedData\\Distribution\\"+metapath+"\\"+ActorIDToName.get(candiID));
								fileWriter3.write(authorBuilder.toString());
								fileWriter3.close();
							
							
							
							
						}
					}
				}
			}
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

	public void checkAmbiguousNameInDistri() throws IOException, Exception {
		ArrayList<String> paths=new ArrayList<String>();
		paths.add("AB");
		paths.add("AMK");
		paths.add("AMT");
		paths.add("AMC");
		paths.add("AMA");
		paths.add("AMD");
		
		
		for (String mention : MentionCandidateNames.keySet()) {
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
		
			for (String canName: canNames) {
				
				for (String metapath : paths) {
//					System.out.println("1");
					ReadDistribution(metapath, canName);
				}
				
			}
		}
	}
	
	public void printDistribution() {
		System.out.println("size: "+NametoScore.size());
		for (String nameString : NametoScore.keySet()) {
			System.out.println(nameString+"   "+NametoScore.get(nameString));
		}
		
	}
	
	
	public static void main(String[]args)throws Exception{
		MetapathRW metapathRW=new MetapathRW();
		
		metapathRW.readGraphFromDisk();
		metapathRW.geneCandidates();
		
		ArrayList<String> paths=new ArrayList<String>();
		paths.add("AB");
		paths.add("AMK");
		paths.add("AMT");
		paths.add("AMC");
		paths.add("AMA");
		paths.add("AMD");
		
//		metapathRW.randomWalkAlongMetapath("AMD");
		
		for (String metapath: paths) {
			System.out.println(metapath);
			metapathRW.randomWalkAlongMetapath(metapath);

		}
		
//		for (String metapath : paths) {
//			System.out.println();
//			System.out.println(metapath);
//			metapathRW.ReadDistribution(metapath, "Adams, Jonathan (I)");
//			metapathRW.printDistribution();
//		}
		
		
		
//		metapathRW.checkAmbiguousNameInDistri();
		
		
	}

}
