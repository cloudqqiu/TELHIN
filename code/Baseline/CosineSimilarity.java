package Baseline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import IMDbPreprocess.CandidateGeneration;
import IMDbPreprocess.PreprocessIMDb;
import WebPage.ReadWebPageAndGoldenMapping;

public class CosineSimilarity {
	
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
	
	public HashMap<String, ArrayList<String>> MentionCandidateNames=new HashMap<String, ArrayList<String>>();
	
	HashMap<String, HashMap<String, Integer>> vectorsForCandis=new HashMap<String, HashMap<String,Integer>>();
	public ArrayList<String> movieObjs=new ArrayList<String>();
	public ArrayList<String> actorObjs=new ArrayList<String>();
	public ArrayList<String> directorObjs=new ArrayList<String>();
	public ArrayList<String> characterObjs=new ArrayList<String>();
	public ArrayList<String> termObjs=new ArrayList<String>();
	HashMap<String, Integer> documentFreqInCandi=new HashMap<String, Integer>();
	HashMap<String, Integer> documentFreqInWebpage=new HashMap<String, Integer>();
	
	
	public HashMap<String, String> fileNameToPath=new HashMap<String, String>();
	public HashMap<String, String> fileNameToGoldenEntityName=new HashMap<String, String>();
	public HashMap<String, String> fileNameToMentionName=new HashMap<String, String>();
	
	
	public HashMap<String, String> fileNameToPrediEntityName=new HashMap<String, String>();
	
	
	HashMap<String, HashMap<String, Integer>> vectorsForWebpages=new HashMap<String, HashMap<String,Integer>>();
	
	HashMap<String, Integer> NametoScore=new HashMap<String, Integer>();
	
	public HashMap<String, Double> authorNameToPriorScore=new HashMap<String, Double>();

	public HashMap<String, Double[]> word2vec = new HashMap<String, Double[]>();
	public int vec_leng = 0;
	public Double[] OOV;


	public void geneCandidates() throws IOException {
		CandidateGeneration candidateGeneration=new CandidateGeneration();
		candidateGeneration.ReadCandidates();
		candidateGeneration.ReadPriorPageRankScore();
		
		authorNameToPriorScore=candidateGeneration.authorNameToScore;
		MentionCandidateNames=candidateGeneration.MentionCandidateNames;
		
		
//		candidateGeneration.ReadPriorPageRankScore();
//		candidateGeneration.printCandis();
	}
	
	public void readVectorForCandi(String type, String candiName) throws Exception, IOException
	{
//		System.out.println("Read distribution ...");

		File file=new  File(".\\newIMDB\\Baseline-cosine\\VectorForCandi\\"+type+"\\"+candiName);
		if (!file.exists()) {
			return;
		}
		FileReader fr1 = new FileReader(file);
	    BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;
	   
//	    System.out.println(metapath+"   "+candiName);
	    
	    
	    NametoScore=new HashMap<String, Integer>();
	    
	    while((line1 = br1.readLine())!=null){
	    	String[] strings=line1.split("\t");
	    	if (strings.length==2) {
	    		String authorName=strings[0];
	    		
				NametoScore.put(authorName, Integer.parseInt(strings[1]));
				
	    		
			}
	    	else {
				System.err.println("hhhh1  "+line1);
			}
	    	
	    }
	    br1.close();
	    fr1.close();
		
	}
	
	public void	readVectorForCandidate() throws Exception{
		geneCandidates();
		
		for (String mention : MentionCandidateNames.keySet()) {
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
			
//			System.out.println("mention: "+mention+"   candidate number: "+canNames.size());
			
			for (String canName: canNames) {
//				if (!canName.equals("Ajay Gupta 0000")) {
//				continue;
//			   }
				
				HashMap<String, Integer> biotermVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> coactorVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> movietitleVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> characVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> directorVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> keywordtermVectorForCandi=new HashMap<String, Integer>();
				
				readVectorForCandi("Coactor", canName);
				coactorVectorForCandi=NametoScore;
				
				readVectorForCandi("BioTerm", canName);
				biotermVectorForCandi=NametoScore;
				
				readVectorForCandi("Keywords", canName);
				keywordtermVectorForCandi=NametoScore;
				
				readVectorForCandi("Character", canName);
				characVectorForCandi=NametoScore;
				
				readVectorForCandi("Director", canName);
				directorVectorForCandi=NametoScore;
				
				readVectorForCandi("MovieTitle", canName);
				movietitleVectorForCandi=NametoScore;
				
				HashMap<String, Integer> vectorForOneCandi=new HashMap<String, Integer>();
				
				for (String coauthor : coactorVectorForCandi.keySet()) {
					if (vectorForOneCandi.containsKey(coauthor)) {
//						System.err.println("duplicate author name in candi vector: "+coauthor);
					}
					else {
						vectorForOneCandi.put(coauthor, coactorVectorForCandi.get(coauthor));
					}
					
				}
				
				for (String coauthor : biotermVectorForCandi.keySet()) {
					if (vectorForOneCandi.containsKey(coauthor)) {
//						System.err.println("duplicate author name in candi vector: "+coauthor);
					}
					else {
						vectorForOneCandi.put(coauthor, biotermVectorForCandi.get(coauthor));
					}
					
				}
				for (String coauthor : movietitleVectorForCandi.keySet()) {
					if (vectorForOneCandi.containsKey(coauthor)) {
//						System.err.println("duplicate author name in candi vector: "+coauthor);
					}
					else {
						vectorForOneCandi.put(coauthor, movietitleVectorForCandi.get(coauthor));
					}
					
				}
				for (String coauthor : directorVectorForCandi.keySet()) {
					if (vectorForOneCandi.containsKey(coauthor)) {
//						System.err.println("duplicate author name in candi vector: "+coauthor);
					}
					else {
						vectorForOneCandi.put(coauthor, directorVectorForCandi.get(coauthor));
					}
					
				}
				
				for (String coauthor : keywordtermVectorForCandi.keySet()) {
					if (vectorForOneCandi.containsKey(coauthor)) {
//						System.err.println("duplicate author name in candi vector: "+coauthor);
					}
					else {
						vectorForOneCandi.put(coauthor, keywordtermVectorForCandi.get(coauthor));
					}
					
				}
				
				for (String coauthor : characVectorForCandi.keySet()) {
					if (vectorForOneCandi.containsKey(coauthor)) {
//						System.err.println("duplicate author name in candi vector: "+coauthor);
					}
					else {
						vectorForOneCandi.put(coauthor, characVectorForCandi.get(coauthor));
					}
					
				}
				
				vectorsForCandis.put(canName, vectorForOneCandi);
				
//				for (String string : vectorForOneCandi.keySet()) {
//					System.out.println(string+"  "+vectorForOneCandi.get(string));
//				}
			}
		}
		System.out.println("vectorsForCandis.size(): "+vectorsForCandis.size());
	}
	
	//generate vector for each candidate
	public void generateVectorForCandidate() throws IOException {
		geneCandidates();
		
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
		
		
		for (String mention : MentionCandidateNames.keySet()) {
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
			
//			System.out.println("mention: "+mention+"   candidate number: "+canNames.size());
			
			for (String canName: canNames) {
				HashMap<String, Integer> biotermVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> coactorVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> movietitleVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> characVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> directorVectorForCandi=new HashMap<String, Integer>();
				HashMap<String, Integer> keywordtermVectorForCandi=new HashMap<String, Integer>();
				
				
				
				Integer candiID = ActorNameToID.get(canName);
				
//				if (!AuthorIDToName.get(candiID).equals("Ajay Gupta 0000")) {
//					continue;
//				}
				
				System.out.println("candiID: "+candiID);
				
				//生成coactorVector
				if (ActorIDToMovieIDList.containsKey(candiID)) {
					
					ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
					
					for (Integer integer : movieIDList) {
						//如果是电视剧，具体哪集没有列出，如Clayton, John (I)的"E Street" (1989)  [Sergeant Roy Harrison]，这种情况下，A-M-C,A-M-D,A-M-A,A-M-K 元路径的随机游走就利用这个
						//信息，害怕产生出很多不相关的关系，
						String movieName=MovieIDToTitle.get(integer);
						if (movieName.startsWith("\"")) {
							if (!(movieName.contains("{")&&movieName.contains("}"))) {
//								System.out.println(movieName);
								continue;
							}
							
						}
						if (MovieIDToActorIDList.containsKey(integer)) {
							
							ArrayList<Integer> authorIDList=MovieIDToActorIDList.get(integer);
									
							
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
//												System.out.println(actorString+"       "+actorName);

											}
											else {
//												System.out.println(actorString);
											}
										}
										else {
											Pattern p = Pattern.compile("(.+?),\\s+(.+)");
											Matcher m = p.matcher(actorString);
											if (m.find()) {
												actorName=m.group(2).trim()+" "+m.group(1).trim();
//												System.out.println(actorString+"       "+actorName);

											}
//											else {
//												System.out.println(actorString);
//											}
										}
										
									}
									else {
//										System.out.println(actorString);
										Pattern p = Pattern.compile("(.+?)\\s\\([IVXLC]+\\)");
										Matcher m = p.matcher(actorString);
										if (m.find()) {
											//名字中无逗号，有罗马数字
											actorName=m.group(1).trim();
//											System.out.println(actorString+"       "+actorName);
//											System.out.println(movieName);
										}
										else {
											//名字中无逗号和罗马数字
//											System.out.println(actorString);
											actorName=actorString;
										}
										
									}
									
									
									
									if (!actorName.equals("null")) {
										if (coactorVectorForCandi.containsKey(actorName)) {
											coactorVectorForCandi.put(actorName, coactorVectorForCandi.get(actorName)+1);
									}
									else {
										coactorVectorForCandi.put(actorName, 1);
									}
									}
									
									
								
								
							}
							
							
						}
						
					}
					
					if (coactorVectorForCandi.size()>0) {
						StringBuilder authorBuilder=new StringBuilder();
					for (String coauthor : coactorVectorForCandi.keySet()) {
						authorBuilder.append(coauthor+"\t"+coactorVectorForCandi.get(coauthor)+"\n");
					}
					System.out.println(authorBuilder.toString());
					FileWriter fileWriter1=new FileWriter(".\\newIMDB\\Baseline-cosine\\VectorForCandi\\Coactor\\"+canName);
					fileWriter1.write(authorBuilder.toString());
					fileWriter1.close();
					}
					
					
					
					
					
				}
				
				//生成biotermVectorForCandi
				if (ActorIDToBioTermIDList.containsKey(candiID)) {
					ArrayList<Integer> biotermIDList=ActorIDToBioTermIDList.get(candiID);
					
					for (Integer integer : biotermIDList) {
						String termname=TermIDToName.get(integer);
						if (biotermVectorForCandi.containsKey(termname)) {
							biotermVectorForCandi.put(termname, biotermVectorForCandi.get(termname)+1);
						}
						else {
							biotermVectorForCandi.put(termname, 1);
						}
					}
					if (biotermVectorForCandi.size()>0) {
						StringBuilder authorBuilder=new StringBuilder();
					for (String integer : biotermVectorForCandi.keySet()) {
						authorBuilder.append(integer+"\t"+biotermVectorForCandi.get(integer)+"\n");
					
					}
					FileWriter fileWriter3=new FileWriter(".\\newIMDB\\Baseline-cosine\\VectorForCandi\\BioTerm\\"+canName);
					fileWriter3.write(authorBuilder.toString());
					fileWriter3.close();
					}
										
					
					
				}
				
				//生成movietitleVectorForCandi
				if (ActorIDToMovieIDList.containsKey(candiID)) {
					
					ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
					
					for (Integer integer : movieIDList) {
						String movieString=MovieIDToTitle.get(integer);
						
						//去除movie中的年份，以及电视剧中引号，年份和集
						String movieName="null";
						
						Pattern p = Pattern.compile("\"?(.+?)\"?\\s+\\([\\d/IV\\?]+?\\).*");
						Matcher m = p.matcher(movieString);
						if (m.find()) {
							movieName=m.group(1).trim();
//							System.out.println(movieString+"       "+movieName);
//							System.out.println(movieName);
						}
						
							
						
						if (!movieName.equals("null")) {
							if (movietitleVectorForCandi.containsKey(movieName)) {
								movietitleVectorForCandi.put(movieName, movietitleVectorForCandi.get(movieName)+1);
							}
							else {
								movietitleVectorForCandi.put(movieName, 1);
							}
						}
						
					}
					
					if (movietitleVectorForCandi.size()>0) {
						StringBuilder authorBuilder=new StringBuilder();
						for (String integer : movietitleVectorForCandi.keySet()) {
							authorBuilder.append(integer+"\t"+movietitleVectorForCandi.get(integer)+"\n");
						
						}
						FileWriter fileWriter3=new FileWriter(".\\newIMDB\\Baseline-cosine\\VectorForCandi\\MovieTitle\\"+canName);
						fileWriter3.write(authorBuilder.toString());
						fileWriter3.close();
					}
					
						
					
					
				}
				
				
				//生成characVectorForCandi
				if (ActorIDToMovieIDList.containsKey(candiID)) {
					
					ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
					
					for (Integer integer : movieIDList) {
						//如果是电视剧，具体哪集没有列出，如Clayton, John (I)的"E Street" (1989)  [Sergeant Roy Harrison]，这种情况下，A-M-C,A-M-D,A-M-A,A-M-K 元路径的随机游走就利用这个
						//信息，害怕产生出很多不相关的关系，
						String movieName=MovieIDToTitle.get(integer);
						if (movieName.startsWith("\"")) {
							if (!(movieName.contains("{")&&movieName.contains("}"))) {
//								System.out.println(movieName);
								continue;
							}
							
						}
						if (MovieIDToCharacterIDList.containsKey(integer)) {
							
							ArrayList<Integer> authorIDList=MovieIDToCharacterIDList.get(integer);
							
							for (Integer paperID : authorIDList) {
								String charString=CharacterIDToName.get(paperID);
								
								if (charString.contains("/")) {
									//当string为Vincent Varela/Freddy Canpil时，可以将其看成两个character
//									System.out.println(charString);
									
									String[] a=charString.split("/");
									for (String string : a) {
										if (string.trim().length()>0&&!string.equals("self")&&!string.equals("Themself")) {
											
											String charName=string.trim();
											
											System.out.println("    "+charName);	
											
											
											if (characVectorForCandi.containsKey(charName)) {
												characVectorForCandi.put(charName, characVectorForCandi.get(charName)+1);
											}
											else {
												characVectorForCandi.put(charName, 1);
											}
										}
									}
									
								}
								else {
									String charName=charString.trim();
									
									if (charName.length()>0&&!charName.equals("self")&&!charName.equals("Themself")) {
										if (characVectorForCandi.containsKey(charName)) {
											characVectorForCandi.put(charName, characVectorForCandi.get(charName)+1);
									}
									else {
										characVectorForCandi.put(charName, 1);
									}
									}
									
									
								}
								
							}
							
							
						}
						else {
//							System.err.println("no term id contained  222");
						}
					
					}
					
					if (characVectorForCandi.size()>0) {
						StringBuilder authorBuilder=new StringBuilder();
						for (String integer : characVectorForCandi.keySet()) {
							authorBuilder.append(integer+"\t"+characVectorForCandi.get(integer)+"\n");
						
						}
						FileWriter fileWriter3=new FileWriter(".\\newIMDB\\Baseline-cosine\\VectorForCandi\\Character\\"+canName);
						fileWriter3.write(authorBuilder.toString());
						fileWriter3.close();
					}
					
					
						
						
					
					
					
					
				}
				
				//生成directorVectorForCandi
				if (ActorIDToMovieIDList.containsKey(candiID)) {
					ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
					
					
					for (Integer integer : movieIDList) {
						//如果是电视剧，具体哪集没有列出，如Clayton, John (I)的"E Street" (1989)  [Sergeant Roy Harrison]，这种情况下，A-M-C,A-M-D,A-M-A,A-M-K 元路径的随机游走就利用这个
						//信息，害怕产生出很多不相关的关系，
						String movieName=MovieIDToTitle.get(integer);
						if (movieName.startsWith("\"")) {
							if (!(movieName.contains("{")&&movieName.contains("}"))) {
//								System.out.println(movieName);
								continue;
							}
							
						}
						if (MovieIDToDirectorIDList.containsKey(integer)) {
							
							ArrayList<Integer> authorIDList=MovieIDToDirectorIDList.get(integer);
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
//												System.out.println(actorString+"       "+actorName);

											}
											else {
//												System.out.println(actorString);
											}
										}
										else {
											Pattern p = Pattern.compile("(.+?),\\s+(.+)");
											Matcher m = p.matcher(actorString);
											if (m.find()) {
												actorName=m.group(2).trim()+" "+m.group(1).trim();
//												System.out.println(actorString+"       "+actorName);

											}
//											else {
//												System.out.println(actorString);
//											}
										}
										
									}
									else {
//										System.out.println(actorString);
										Pattern p = Pattern.compile("(.+?)\\s\\([IVXLC]+\\)");
										Matcher m = p.matcher(actorString);
										if (m.find()) {
											//名字中无逗号，有罗马数字
											actorName=m.group(1).trim();
//											System.out.println(actorString+"       "+actorName);
//											System.out.println(movieName);
										}
										else {
											//名字中无逗号和罗马数字
//											System.out.println(actorString);
											actorName=actorString;
										}
										
									}
									
									
									
									if (!actorName.equals("null")) {
										if (directorVectorForCandi.containsKey(actorName)) {
											directorVectorForCandi.put(actorName, directorVectorForCandi.get(actorName)+1);
									}
									else {
										directorVectorForCandi.put(actorName, 1);
									}
									}
									
									
								
								
							}
							
							
						}
					}
					
					if (directorVectorForCandi.size()>0) {
						StringBuilder authorBuilder=new StringBuilder();
						for (String integer : directorVectorForCandi.keySet()) {
							authorBuilder.append(integer+"\t"+directorVectorForCandi.get(integer)+"\n");
						
						}
						FileWriter fileWriter3=new FileWriter(".\\newIMDB\\Baseline-cosine\\VectorForCandi\\Director\\"+canName);
						fileWriter3.write(authorBuilder.toString());
						fileWriter3.close();
					}
					
						
					
					
					
					
				}
				
				//生成keywordtermVectorForCandi
				if (ActorIDToMovieIDList.containsKey(candiID)) {
					ArrayList<Integer> movieIDList=ActorIDToMovieIDList.get(candiID);
					
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
						if (MovieIDToKeywordTermIDList.containsKey(integer)) {
							
							ArrayList<Integer> authorIDList=MovieIDToKeywordTermIDList.get(integer);
							
							for (Integer paperID : authorIDList) {
								String termString=TermIDToName.get(paperID);
								
								if (keywordtermVectorForCandi.containsKey(termString)) {
									keywordtermVectorForCandi.put(termString, keywordtermVectorForCandi.get(termString)+1);
								}
								else {
									keywordtermVectorForCandi.put(termString, 1);
								}
							}
							
							
						}
						else {
//							System.err.println("no term id contained  222");
						}
					}
					if (keywordtermVectorForCandi.size()>0) {
						StringBuilder authorBuilder=new StringBuilder();
					for (String integer : keywordtermVectorForCandi.keySet()) {
						authorBuilder.append(integer+"\t"+keywordtermVectorForCandi.get(integer)+"\n");
					
					}
					FileWriter fileWriter3=new FileWriter(".\\newIMDB\\Baseline-cosine\\VectorForCandi\\Keywords\\"+canName);
					fileWriter3.write(authorBuilder.toString());
					fileWriter3.close();
					}
					
					
				}
			}
		}
	}
	
	//read object set from disk to authorObjs, venueObjs, yearObjs, termObjs
	public void readObjectFile(String filename) throws IOException, Exception {
		movieObjs=new ArrayList<String>();
		actorObjs=new ArrayList<String>();
		directorObjs=new ArrayList<String>();
		characterObjs=new ArrayList<String>();
		termObjs=new ArrayList<String>();
		
		
		String fileAd=".\\newIMDB\\ObjSet\\"+filename;
		InputStreamReader write = new InputStreamReader(new FileInputStream(fileAd),"UTF-8");
    	BufferedReader br=new BufferedReader(write);
		String s;
		int lineNo=0;
	    while ((s = br.readLine() )!=null) {
	    	lineNo++;
	    	if (s.trim().equals("")) {
					continue;
				}
	    	s=s.substring(0,s.length()-1);
	    	
	    	if (lineNo==1) {
				//movie object
	    		
	    		String[] authors=s.split("\\+");
	    		for (String string : authors) {
					if (string.length()>0) {
						movieObjs.add(string);
					}
				}
			}
	    	else if (lineNo==2) {
	    		//actor object
	    		
	    		String[] venues=s.split("\\+");
	    		for (String string : venues) {
					if (string.length()>0) {
						actorObjs.add(string);
					}
				}
			}
	    	else if (lineNo==3) {
	    		//director object
	    		
	    		String[] venues=s.split("\\+");
	    		for (String string : venues) {
					if (string.length()>0) {
						directorObjs.add(string);
					}
				}
			}
	    	else if (lineNo==4) {
	    		//character object
	    		
	    		String[] venues=s.split("\\+");
	    		for (String string : venues) {
					if (string.length()>0) {
						characterObjs.add(string);
					}
				}
			}
	    	else if (lineNo==5) {
	    		//term object
	    		
	    		String[] venues=s.split("\\+");
	    		for (String string : venues) {
					if (string.length()>0) {
						termObjs.add(string);
					}
				}
			}
	    	
	 	
	    }
	  
	}
	
	public void linkingViaCosine() {
		
		for (String filename : fileNameToMentionName.keySet()) {
			String mention=fileNameToMentionName.get(filename);
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
			
			Double maxSim=-1.0;
			String maxCandiName="";
			
			if (!vectorsForWebpages.containsKey(filename)) {
				continue;
			}
			HashMap<String, Integer> vectorForOneWebpage=vectorsForWebpages.get(filename);
			
			for (String candiName : canNames) {
				HashMap<String, Integer> vectorForOneCandi=vectorsForCandis.get(candiName);
//				Double similarity=cosineSTF(vectorForOneCandi, vectorForOneWebpage);
//				similarity=1000000*similarity*authorNameToPriorScore.get(candiName);

//				
//				System.out.println(similarity);
//				Double similarity=cosineSTFIDF(vectorForOneCandi, vectorForOneWebpage);
//				similarity=1000000*similarity*authorNameToPriorScore.get(candiName);

				Double similarity = cosineW2V(vectorForOneCandi, vectorForOneWebpage);
//                similarity=1000000*similarity*authorNameToPriorScore.get(candiName);

				if (similarity>maxSim) {
					maxSim=similarity;
					maxCandiName=candiName;
				}
			}
			if (maxCandiName.length()>0&&maxSim>-0.5) {
				fileNameToPrediEntityName.put(filename, maxCandiName);
			}
			else {
				System.err.println("no find mapping entity");
                System.err.printf("put NIL for this %s mention\n", mention);  // yyw
                fileNameToPrediEntityName.put(filename, "NIL");
			}
			
			
		}
		calculAccu();
	}
	
	public void calculAccu() {
		int totalFil=0;
		int corret=0;
		
		for (String filename : fileNameToPrediEntityName.keySet()) {
			String predictEntity=fileNameToPrediEntityName.get(filename);
			String goldenEntity=fileNameToGoldenEntityName.get(filename);
			totalFil++;
			if (predictEntity.equals(goldenEntity)) {
				corret++;
			}
		}
		Double accu=(double)corret/(double)totalFil;
		System.out.println("accuracy: "+accu);
		System.out.println("correctly linked mentions: "+corret);
		System.out.println("total mentions: "+totalFil);
	}
	public Double cosineSTFIDF(HashMap<String, Integer> contextAHashMap, HashMap<String, Integer> contextBHashMap) {
		
		
		double fenzi=0.0;
		double fenmuA=0.0;
		double fenmuB=0.0;
		
		for (String context : contextAHashMap.keySet()) {
			double freq=contextAHashMap.get(context);
			freq=freq/(double)documentFreqInCandi.get(context);
			
			fenmuA=fenmuA+freq*freq;
			
			if (contextBHashMap.containsKey(context)) {
				double freq2=contextBHashMap.get(context);
				freq2=freq2/(double)documentFreqInWebpage.get(context);
				
				fenzi=fenzi+freq*freq2;
//				System.out.println(context+"  "+freq+"  "+freq2);
			}
		}
		
		for (String context : contextBHashMap.keySet()) {
			double tfidf2=contextBHashMap.get(context);
			tfidf2=tfidf2/(double)documentFreqInWebpage.get(context);
			fenmuB=fenmuB+tfidf2*tfidf2;
		}
		double cosineSim=fenzi/(Math.sqrt(fenmuA)*Math.sqrt(fenmuB));
//		System.out.println(cosineSim);
		return cosineSim;
	}
	
public Double cosineSTF(HashMap<String, Integer> contextAHashMap, HashMap<String, Integer> contextBHashMap) {
		
		double fenzi=0.0;
		double fenmuA=0.0;
		double fenmuB=0.0;
		
		for (String context : contextAHashMap.keySet()) {
			int freq=contextAHashMap.get(context);
			
			fenmuA=fenmuA+freq*freq;
			
			if (contextBHashMap.containsKey(context)) {
				int freq2=contextBHashMap.get(context);
				fenzi=fenzi+freq*freq2;
//				System.out.println(context+"  "+freq+"  "+freq2);
			}
		}
		
		for (String context : contextBHashMap.keySet()) {
			int tfidf2=contextBHashMap.get(context);
			fenmuB=fenmuB+tfidf2*tfidf2;
		}
		double cosineSim=fenzi/(Math.sqrt(fenmuA)*Math.sqrt(fenmuB));
//		System.out.println(cosineSim);
		return cosineSim;
	}


	public void readVectorForWebpage() throws IOException, Exception {
		ReadWebPageAndGoldenMapping readWebPageAndGoldenMapping=new ReadWebPageAndGoldenMapping();
		readWebPageAndGoldenMapping.readTxtFileMappingResult();
		fileNameToPath=readWebPageAndGoldenMapping.fileNameToPath;
		fileNameToMentionName=readWebPageAndGoldenMapping.fileNameToMentionName;
		fileNameToGoldenEntityName=readWebPageAndGoldenMapping.fileNameToGoldenEntityName;
	    
		
		for (String filename : fileNameToMentionName.keySet()) {
			HashMap<String, Integer> vectorForOneWebpage=new HashMap<String, Integer>();
			
			
			
			String fileAd=".\\newIMDb\\ObjSet\\"+filename;
			File fff=new File(fileAd);
			if (!fff.exists()) {
//				System.err.println(filename+" do not have object file");
				continue;
			}
//			if (!filename.equals("186")) {
//				continue;
//			}
			readObjectFile(filename);
			
//			System.out.println("author: "+authorObjs.size());
			for (String string : actorObjs) {
//				System.out.println(string);
				
				if (vectorForOneWebpage.containsKey(string)) {
					vectorForOneWebpage.put(string, vectorForOneWebpage.get(string)+1);
				}
				else {
					vectorForOneWebpage.put(string, 1);
				}
				
			}
//			System.out.println("venue: "+venueObjs.size());
			for (String string : movieObjs) {
//				System.out.println(string);
				
				if (vectorForOneWebpage.containsKey(string)) {
					vectorForOneWebpage.put(string, vectorForOneWebpage.get(string)+1);
				}
				else {
					vectorForOneWebpage.put(string, 1);
				}
			}
//			System.out.println("year: "+yearObjs.size());
			for (String string : characterObjs) {
//				System.out.println(string);
				
				if (vectorForOneWebpage.containsKey(string)) {
					vectorForOneWebpage.put(string, vectorForOneWebpage.get(string)+1);
				}
				else {
					vectorForOneWebpage.put(string, 1);
				}
			}
			for (String string : directorObjs) {
//				System.out.println(string);
				
				if (vectorForOneWebpage.containsKey(string)) {
					vectorForOneWebpage.put(string, vectorForOneWebpage.get(string)+1);
				}
				else {
					vectorForOneWebpage.put(string, 1);
				}
			}
//			System.out.println("term: "+termObjs.size());
			for (String string : termObjs) {
//				System.out.println(string);
				
				if (vectorForOneWebpage.containsKey(string)) {
					vectorForOneWebpage.put(string, vectorForOneWebpage.get(string)+1);
				}
				else {
					vectorForOneWebpage.put(string, 1);
				}
			}
			
//			for (String ob : vectorForOneWebpage.keySet()) {
//				System.out.println(ob+"  "+vectorForOneWebpage.get(ob));
//			}
			vectorsForWebpages.put(filename, vectorForOneWebpage);
		}
		System.out.println("vectorsForWebpages.size(): "+vectorsForWebpages.size());
	}
	
	public void calculDocumentFreq() {
		for (String fileName : vectorsForCandis.keySet()) {
			HashMap<String, Integer> vectorForOneCandi=vectorsForCandis.get(fileName);
			for (String object : vectorForOneCandi.keySet()) {
				if (documentFreqInCandi.containsKey(object)) {
					documentFreqInCandi.put(object, documentFreqInCandi.get(object)+1);
				}
				else {
					documentFreqInCandi.put(object, 1);
				}
			}
		}
		
		for (String fileName : vectorsForWebpages.keySet()) {
			HashMap<String, Integer> vectorForOneCandi=vectorsForWebpages.get(fileName);
			for (String object : vectorForOneCandi.keySet()) {
				if (documentFreqInWebpage.containsKey(object)) {
					documentFreqInWebpage.put(object, documentFreqInWebpage.get(object)+1);
				}
				else {
					documentFreqInWebpage.put(object, 1);
				}
			}
		}
	}

	public void readW2V() throws Exception{
        HashMap<String, Double[]> result = new HashMap<String, Double[]>();
        FileReader fr1 = new FileReader(".\\newIMDB\\embedding_text.txt");
        BufferedReader br1 = new BufferedReader(fr1);
        String line1 = null;
        int w_num = 0, dimen = 0;
        while((line1 = br1.readLine())!=null){
            String[] strings=line1.split(" ");
            if (strings.length==2) {
                w_num = Integer.parseInt(strings[0]);
                dimen = Integer.parseInt(strings[1]);
            }
            else {
                Double[] temp = new Double[dimen];
                for (int i = 0; i < dimen; i++){
                    temp[i] = Double.parseDouble(strings[i + 1]);
                }
                result.put(strings[0], temp);
            }
        }
        br1.close();
        fr1.close();
        Double[] oov = new Double[dimen];
        for (int i = 0; i<dimen; i++){
        	oov[i] = 0.0;
		}
		for (String s : result.keySet()){
        	Double[] temp = result.get(s);
			for (int i = 0; i<dimen; i++){
				oov[i] += temp[i];
			}
		}
		for (int i = 0; i<dimen; i++){
			oov[i] /= w_num;
		}
        word2vec = result;
        vec_leng = dimen;
        OOV = oov;
        System.out.println("Read w2v");
    }

    public Double cosineW2V(HashMap<String, Integer> contextAHashMap, HashMap<String, Integer> contextBHashMap) {
        double fenzi=0.0;
        double fenmuA=0.0;
        double fenmuB=0.0;
        Double[] A = new Double[vec_leng];
        Double[] B = new Double[vec_leng];
        int A_num = 0;
        int B_num = 0;
        for (int i = 0; i < vec_leng; i++) {
            A[i] = 1.0 / vec_leng;
            B[i] = 1.0 / vec_leng;
        }
        for (String context : contextAHashMap.keySet()) {
            int wei = contextAHashMap.get(context);
            String[] c_context = context.split(" ");
            for (int k = 0; k < c_context.length; k++){
                if (word2vec.containsKey(c_context[k])) {
                    Double[] temp = word2vec.get(c_context[k]);
                    A_num += wei;
                    for (int i = 0; i < vec_leng; i++) {
                        A[i] += temp[i] * wei / c_context.length;
                    }
                }
                else{
                    for (int i = 0; i < vec_leng; i++) {
                        A[i] += OOV[i] * wei / c_context.length;
//                        A[i] += (1.0 / vec_leng) * wei / c_context.length;
                    }
                }
            }
        }
        for (String context : contextBHashMap.keySet()) {
            int wei = contextBHashMap.get(context);
            String[] c_context = context.split(" ");
            for (int k = 0; k < c_context.length; k++){
                if (word2vec.containsKey(c_context[k])) {
                    Double[] temp = word2vec.get(c_context[k]);
                    B_num += wei;
                    for (int i = 0; i < vec_leng; i++) {
                        B[i] += temp[i] * wei / c_context.length;
                    }
                }
                else{
                    for (int i = 0; i < vec_leng; i++) {
                        B[i] += OOV[i] * wei / c_context.length;
//                        B[i] += (1.0 / vec_leng) * wei / c_context.length;
                    }
                }
            }
        }
        for (int i = 0; i < vec_leng; i++) {
            A[i] /= A_num + 1;
            B[i] /= B_num + 1;
        }
        for (int i = 0; i < vec_leng; i++) {
            fenmuA += A[i] * A[i];
            fenmuB += B[i] * B[i];
            fenzi += A[i] * B[i];
        }

        double cosineSim=fenzi/(Math.sqrt(fenmuA)*Math.sqrt(fenmuB));
//		System.out.println(cosineSim);
        return cosineSim;
    }

	public static void main(String[]args)throws Exception{
		CosineSimilarity cosineSimilarity=new CosineSimilarity();
//		cosineSimilarity.generateVectorForCandidate();
		cosineSimilarity.readVectorForWebpage();
		cosineSimilarity.readVectorForCandidate();
		cosineSimilarity.calculDocumentFreq();
		cosineSimilarity.linkingViaCosine();
		
	}

}

