package Linking;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;










import IMDbPreprocess.AuthorScore;
import IMDbPreprocess.CandidateGeneration;
import WebPage.GeneralModel;
import WebPage.ReadWebPageAndGoldenMapping;


//加入ADC metapath，为每个entity加入top1 candidate概率大于0.999999999的context的效果
//此时ADC path的权重是通过迭代算法学习而来
public class LinkingAddingContextEMLearnItera {
	
	public HashMap<String, Double> authorNameToPriorScore=new HashMap<String, Double>();
	public HashMap<String, ArrayList<String>> MentionCandidateNames=new HashMap<String, ArrayList<String>>();
	public HashMap<String, String> fileNameToPath=new HashMap<String, String>();
	public HashMap<String, String> fileNameToGoldenEntityName=new HashMap<String, String>();
	public HashMap<String, String> fileNameToMentionName=new HashMap<String, String>();
	
	public ArrayList<String> paths=new ArrayList<String>();
	public HashMap<String, HashMap<String, HashMap<String, Double>>> distriForAllPathsCandidates=new HashMap<String, HashMap<String,HashMap<String,Double>>>();
	public HashMap<String, Double> pathWeights=new HashMap<String, Double>();
	
	public HashMap<String, Integer> entityContextDocNumber=new HashMap<String, Integer>();
	
	
	public HashMap<String, Double>  NametoScore=new HashMap<String, Double>();
    
	public ArrayList<String> movieObjs=new ArrayList<String>();
	public ArrayList<String> actorObjs=new ArrayList<String>();
	public ArrayList<String> directorObjs=new ArrayList<String>();
	public ArrayList<String> characterObjs=new ArrayList<String>();
	public ArrayList<String> termObjs=new ArrayList<String>();
	
	public HashMap<String, Double> finalGM=new HashMap<String, Double>();
	public HashMap<String, ArrayList<ArrayList<String>>> objSetsForWebpages=new HashMap<String, ArrayList<ArrayList<String>>>();
	
	public HashMap<String, HashMap<String, Double>> probDocCandi=new HashMap<String, HashMap<String,Double>>();
	
	public HashMap<String, String> fileNameToPrediEntityName;
	
	public HashMap<String, String> fileNameForContextAdd=new HashMap<String, String>();
	
	public HashMap<String, Integer> errorLinkMentions=new HashMap<String, Integer>();
	
boolean objFuncNega=false;
	
	
	double theta=0.2;
	double learningrate=0.000003;
	
	public LinkingAddingContextEMLearnItera() {
		// TODO Auto-generated constructor stub
		
		paths.add("AB");
//		paths.add("AMK");
//		paths.add("AMT");
//		paths.add("AMC");
//		paths.add("AMA");
//		paths.add("AMD");
		String addMetapath="DC";
		paths.add(addMetapath);
		
		for (String metapath : paths) {
			HashMap<String, HashMap<String, Double>> distriForAllCandis=new HashMap<String, HashMap<String,Double>>();
			distriForAllPathsCandidates.put(metapath, distriForAllCandis);
		}
	}
	
	public void equalPathWeights() {
		Double initPathWeight=1.0/(double)paths.size();
		
				
		for (String metapath : paths) {
			pathWeights.put(metapath, initPathWeight);
		}
	}
	public void ReadDistribution(String metapath, String candiName) throws Exception, IOException
	{
//		System.out.println("Read distribution ...");
		if (metapath.equals("DC")) {
			return;
		}
		File file=new  File(".\\newIMDB\\GeneratedData\\Distribution\\"+metapath+"\\"+candiName);
		
		if (!file.exists()) {
			return;
		}
		FileReader fr1 = new FileReader(file);
		
		 BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;
	   
//	    System.out.println(metapath+"   "+candiName);
	    
	    
	    NametoScore=new HashMap<String, Double>();
	    
	    while((line1 = br1.readLine())!=null){
	    	String[] strings=line1.split("\t");
	    	if (strings.length==2) {
	    		String authorName=strings[0];
	    		
	    		
	    		if (NametoScore.containsKey(authorName)) {
//	    			System.out.println(authorName+"  "+NametoScore.get(authorName)+"  "+strings[1]);
	    			NametoScore.put(authorName, NametoScore.get(authorName)+Double.parseDouble(strings[1]));
//	    			System.out.println(authorName+"  "+NametoScore.get(authorName));
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
	
	public void geneCandidates() throws IOException {
		CandidateGeneration candidateGeneration=new CandidateGeneration();
		candidateGeneration.ReadCandidates();
		candidateGeneration.ReadPriorPageRankScore();
		
		authorNameToPriorScore=candidateGeneration.authorNameToScore;
		MentionCandidateNames=candidateGeneration.MentionCandidateNames;
		
	}
	
	public void readDistributionForCandidates() throws IOException, Exception {
		geneCandidates();

		for (String metapath : paths){
			System.out.println("read dis for "+metapath);
			HashMap<String, HashMap<String, Double>> distriForAllCandis=distriForAllPathsCandidates.get(metapath);
			
			for (String mention : MentionCandidateNames.keySet()) {
				ArrayList<String> canNames=MentionCandidateNames.get(mention);
	
				for (String canName: canNames) {
//					System.out.println("canName: "+canName);
					 
						
						ReadDistribution(metapath, canName);
						
						HashMap<String, Double> distriForCandi=NametoScore;
						distriForAllCandis.put(canName, distriForCandi);
						
				}
					
					
				}
		}
		

	}
	
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
	
	public void readObjectSetsForPages() throws Exception {
		ReadWebPageAndGoldenMapping readWebPageAndGoldenMapping=new ReadWebPageAndGoldenMapping();
		readWebPageAndGoldenMapping.readTxtFileMappingResult();
		fileNameToPath=readWebPageAndGoldenMapping.fileNameToPath;
		fileNameToMentionName=readWebPageAndGoldenMapping.fileNameToMentionName;
		fileNameToGoldenEntityName=readWebPageAndGoldenMapping.fileNameToGoldenEntityName;
	   
		
		for (String filename : fileNameToMentionName.keySet()) {
			ArrayList<ArrayList<String>> objSetForOneWebpage=new ArrayList<ArrayList<String>>();
			
			String fileAd=".\\newIMDB\\ObjSet\\"+filename;
			File fff=new File(fileAd);
			if (!fff.exists()) {
				System.err.println(filename+" do not have object file");
				continue;
			}
//			if (!filename.equals("186")) {
//				continue;
//			}
			readObjectFile(filename);
			
//			System.out.println("author: "+authorObjs.size());
			
//			System.out.println("venue: "+venueObjs.size());
//			System.out.println("year: "+yearObjs.size());
//			System.out.println("term: "+termObjs.size());
			
			objSetForOneWebpage.add(movieObjs);
			objSetForOneWebpage.add(actorObjs);
			objSetForOneWebpage.add(directorObjs);
			objSetForOneWebpage.add(characterObjs);
			objSetForOneWebpage.add(termObjs);
			

			objSetsForWebpages.put(filename, objSetForOneWebpage);
		}
		

	}
	
	public void readGeneralModel() throws FileNotFoundException, Exception {
		GeneralModel generalModel=new GeneralModel();
		
		generalModel.readFinalGM();
		finalGM=generalModel.finalGM;
		
	}
	
	public Double calProDocForOneEntity(ArrayList<ArrayList<String>> objSetForOneWebpage, String candiName) {
		Double prob=0.0;
		
		for (int i = 0; i < 5; i++) {
			if (i==0) {
				ArrayList<String> authorSet=objSetForOneWebpage.get(i);
				
				for (String object : authorSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("T")||metapath.equals("DC")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					prob+=Math.log(probObjForCandi);
				}
			}
			else if (i==1) {
				ArrayList<String> venueSet=objSetForOneWebpage.get(i);
				for (String object : venueSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("A")||metapath.equals("DC")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					prob+=Math.log(probObjForCandi);
				}
			}
			else if (i==2) {
				ArrayList<String> termSet=objSetForOneWebpage.get(i);
				for (String object : termSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("D")||metapath.equals("DC")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					prob+=Math.log(probObjForCandi);
				}
			}
			else if (i==3) {
				ArrayList<String> yearSet=objSetForOneWebpage.get(i);
				for (String object : yearSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.equals("AMC")||metapath.equals("DC")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					prob+=Math.log(probObjForCandi);
				}
			}
			else if (i==4) {
				//TERM
				ArrayList<String> yearSet=objSetForOneWebpage.get(i);
				for (String object : yearSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("B")||metapath.endsWith("K")||metapath.equals("DC")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					prob+=Math.log(probObjForCandi);
				}
			}
		
		
	}
		return prob;
	}
	

	public void printWeights() {
		StringBuilder stringBuilder=new StringBuilder();
		double weightTotal=0.0;
		for (String meString : pathWeights.keySet()) {
			stringBuilder.append(meString+"\t"+pathWeights.get(meString)+"\n");
			weightTotal+=pathWeights.get(meString);
		}
		System.out.println(stringBuilder.toString());
//		System.out.println(weightTotal);
	}
	
	
	//找出那些top1 candidate概率大于0.99999999的网页
		public boolean findGoldenMappingMention(double threshold) {
			boolean addNew=false;
			
			HashMap<String, String> beforefileNameForContextAdd=fileNameForContextAdd;
			
			if (beforefileNameForContextAdd.size()==0) {
				addNew=true;
			}
			fileNameForContextAdd=new HashMap<String, String>();
			
			for (String filename : fileNameToMentionName.keySet()) {
				HashMap<String, Double> probCandi=probDocCandi.get(filename);
				
				double maxSim=-1.0;
				String maxCandiName="";
			
				for (String candiName : probCandi.keySet()) {
					Double similarity=probCandi.get(candiName);

					if (similarity>maxSim) {
					maxSim=similarity;
					maxCandiName=candiName;
					}
				}
				if (maxCandiName.length()>0&&maxSim>threshold) {   //threshold=0.999999999
					if (beforefileNameForContextAdd.size()>0&&!beforefileNameForContextAdd.containsKey(filename)) {
						addNew=true;
					}
					fileNameForContextAdd.put(filename, maxCandiName);
				}
			}
			return addNew;
		}
		
	public void addContextPath() {
		
//		System.out.println("Adding context knowledge... ...");
		String addMetapath="DC";
		
		
		//接下来开始加入entity的distribution
		HashMap<String, HashMap<String, Double>> contextDistriForAllCandis=distriForAllPathsCandidates.get(addMetapath);
		for (String mention : MentionCandidateNames.keySet()) {
			ArrayList<String> canNames=MentionCandidateNames.get(mention);

			for (String canName: canNames) {
				HashMap<String, Double> dis=new HashMap<String, Double>();
				contextDistriForAllCandis.put(canName, dis);
			}
		}
		
		
		
		for (String filename : fileNameForContextAdd.keySet()) {
			
//			if (!filename.equals("7")) {
//				continue;
//			}
			String predictEntity=fileNameForContextAdd.get(filename);
			ArrayList<ArrayList<String>> objSetForOneWebpage=objSetsForWebpages.get(filename);
			int existingContextDocNumber=0;
			if (entityContextDocNumber.containsKey(predictEntity)) {
				existingContextDocNumber=entityContextDocNumber.get(predictEntity);
			}
			if (existingContextDocNumber==0) {
				
				HashMap<String, Double> contextDis=contextDistriForAllCandis.get(predictEntity);
				
				//如果是第一次遇到这个实体需要添加context
				HashMap<String, Integer> contextTermFreq=new HashMap<String, Integer>();
				int totalFreq=0;
				for (ArrayList<String> arrayList : objSetForOneWebpage) {
					for (String string : arrayList) {
						totalFreq++;
						if (contextTermFreq.containsKey(string)) {
							contextTermFreq.put(string, contextTermFreq.get(string)+1);
						}
						else {
							contextTermFreq.put(string, 1);
						}
					}
				}
				
					
					for (String contextTerm : contextTermFreq.keySet()) {
						contextDis.put(contextTerm, contextTermFreq.get(contextTerm)/(double)totalFreq);
					}
					
					
//					for (String contextTerm : contextDis.keySet()) {
//						System.out.println(contextTerm+"  "+contextDis.get(contextTerm));
//					}
					
								
				existingContextDocNumber++;
				entityContextDocNumber.put(predictEntity, existingContextDocNumber);
//				System.out.println("existingContextDocNumber:"+existingContextDocNumber);
			}
			else {
				HashMap<String, Double> contextDis=contextDistriForAllCandis.get(predictEntity);
				//先将之前的分布的按比例缩小
				for (String contextTerm : contextDis.keySet()) {
					contextDis.put(contextTerm, contextDis.get(contextTerm)*(double)existingContextDocNumber/(double)(existingContextDocNumber+1));
				}
				
//				double totalProb=0.0;
//				for (String string : contextDis.keySet()) {
////					System.out.println(string+"  "+contextDis.get(string));
//					totalProb+=contextDis.get(string);
//				}
//				System.out.println("加入之前："+totalProb+"  "+contextDis.size()+"  "+contextDistriForAllCandis.get(predictEntity).size());
				
				
				HashMap<String, Integer> contextTermFreq=new HashMap<String, Integer>();
				int totalFreq=0;
				for (ArrayList<String> arrayList : objSetForOneWebpage) {
					for (String string : arrayList) {
						totalFreq++;
						if (contextTermFreq.containsKey(string)) {
							contextTermFreq.put(string, contextTermFreq.get(string)+1);
						}
						else {
							contextTermFreq.put(string, 1);
						}
					}
				}
				//加入最终distribution
				
				for (String contextTerm : contextTermFreq.keySet()) {
					Double probTerm=contextTermFreq.get(contextTerm)/(double)totalFreq/(double)(existingContextDocNumber+1);
					if (contextDis.containsKey(contextTerm)) {
//						System.out.println(contextTerm+"  before:"+contextDis.get(contextTerm)+"  now:"+probTerm);
						contextDis.put(contextTerm, contextDis.get(contextTerm)+probTerm);
					}
					else {
						contextDis.put(contextTerm, probTerm);
					}
					
				}
				
				existingContextDocNumber++;
				entityContextDocNumber.put(predictEntity, existingContextDocNumber);
//				System.out.println("existingContextDocNumber:"+existingContextDocNumber);
				
//				totalProb=0.0;
//				for (String string : contextDis.keySet()) {
////					System.out.println(string+"  "+contextDis.get(string));
//					totalProb+=contextDis.get(string);
//				}
//				System.out.println("加入之后: "+totalProb+"  "+contextDis.size());
//				System.out.println(contextDistriForAllCandis.get(predictEntity).size());
				
			}
			
		}
		
//		int number=0;
//		
//		for (String string : contextDistriForAllCandis.keySet()) {
//			if (contextDistriForAllCandis.get(string).size()>0) {
//				number++;
//				System.out.println(string+"  size:"+contextDistriForAllCandis.get(string).size());
//			}
//			
//		}
//		
//		System.out.println("contextDistriForAllCandis.size(): "+contextDistriForAllCandis.size()+" no nil size"+number);
		
		
		
	}
	
	
	
	public void calculAccu() throws IOException {
		fileNameToPrediEntityName=new HashMap<String, String>();
		
		for (String filename : fileNameToMentionName.keySet()) {
			HashMap<String, Double> probCandi=probDocCandi.get(filename);
			
			double maxSim=-1.0;
			String maxCandiName="";
		
			for (String candiName : probCandi.keySet()) {
				Double similarity=probCandi.get(candiName);

				if (similarity>maxSim) {
				maxSim=similarity;
				maxCandiName=candiName;
				}
			}
			if (maxCandiName.length()>0) {   
				fileNameToPrediEntityName.put(filename, maxCandiName);
			}
		}
		
		int totalFil=0;
		int corret=0;
		
		int number=0;
		for (String filename : fileNameToPrediEntityName.keySet()) {
			
			if (fileNameForContextAdd.containsKey(filename)) {
				
				String predictEntity=fileNameToPrediEntityName.get(filename);
				String goldenEntity=fileNameToGoldenEntityName.get(filename);
				if (predictEntity.equals(goldenEntity)) {
					number++;	
				}
				
			}
			String predictEntity=fileNameToPrediEntityName.get(filename);
			String goldenEntity=fileNameToGoldenEntityName.get(filename);
			totalFil++;
			if (predictEntity.equals(goldenEntity)) {
//				System.out.print("  "+filename);
				corret++;	
			}
			else {
//				System.err.println(goldenEntity+"  "+filename);
//				System.out.print("  "+filename);
			}
		}
		
//		System.out.println();
		Double accu=(double)corret/(double)totalFil;
		System.out.println("accuracy: "+accu);
		System.out.println("correctly linked mentions: "+corret);
		System.out.println("total mentions: "+totalFil);
		
		System.out.println("population mention number: "+fileNameForContextAdd.size()+" among them, correct number: "+number);
		
	}
	
	void writeContextPathDistributionIntoDisk() throws IOException
	{
		String addMetapath="DC";
		
		//接下来开始加入entity的distribution
		HashMap<String, HashMap<String, Double>> contextDistriForAllCandis=distriForAllPathsCandidates.get(addMetapath);
		for (String entity : contextDistriForAllCandis.keySet()) {
			HashMap<String, Double> contextDis=contextDistriForAllCandis.get(entity);
			
			//对分布中的单词根据概率排序
			ArrayList<AuthorScore> candiScores=new ArrayList<AuthorScore>();
			
			for (String term : contextDis.keySet()) {
				
				AuthorScore authorScore=new AuthorScore();
				authorScore.nameString=term;
				authorScore.score=contextDis.get(term);
				candiScores.add(authorScore);
			}
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
			Collections.sort(candiScores,Collections.reverseOrder());
			
			StringBuilder  stringBuilder=new StringBuilder();
			for (AuthorScore authorScore : candiScores) {
				stringBuilder.append(authorScore.nameString+"\t"+authorScore.score+"\n");
			}
			String fileString=".\\IMDb\\AddContextDistribution\\"+entity;
			FileWriter fileWriter=new FileWriter(fileString);
			fileWriter.write(stringBuilder.toString());
			fileWriter.close();
			
		}
		
		
	}
	
	public void initial() {
		Double initPathWeight=1.0/(double)paths.size();
		
		
		for (String metapath : paths) {
			pathWeights.put(metapath, 0.0);
		}
		
//		for (String metapath : paths) {
//			pathWeights.put(metapath, initPathWeight);
//		}
	}
	
	public void expectationStep() {
		
//		System.out.println("expectation step: ");
		
		//对每篇文章，计算其各个candidate的概率P(m,d,e),存放在probDocCandi中
				for (String filename : fileNameToMentionName.keySet()) {
					String mention=fileNameToMentionName.get(filename);
					ArrayList<String> canNames=MentionCandidateNames.get(mention);
					
					
					if (!objSetsForWebpages.containsKey(filename)) {
						System.out.println("file not found "+ filename);
						continue;
					}

					HashMap<String, Double> probCandi=new HashMap<String, Double>();
					
					
//					System.out.println(filename);
					ArrayList<ArrayList<String>> objSetForOneWebpage=objSetsForWebpages.get(filename);
					double maxProbLN=-1.0E300;
					
					for (String candiName : canNames) {
						//计算P（d|e）
						double probDocForCandiLN=calProDocForOneEntity(objSetForOneWebpage, candiName);	
//						probDocForCandiLN+=Math.log(authorNameToPriorScore.get(candiName));
						
//						System.out.println(candiName+"   "+probDocForCandiLN);
						probCandi.put(candiName, probDocForCandiLN);
						if (probDocForCandiLN>maxProbLN) {
							maxProbLN=probDocForCandiLN;
						}
					}
//					System.out.println("maxProbLN: "+maxProbLN);  //this number is for preventing overflow
					
					double totalProb=0.0;
					for (String candi : probCandi.keySet()) {
						probCandi.put(candi, Math.exp(probCandi.get(candi)-maxProbLN));
//						System.out.println(candi+"  "+probCandi.get(candi));
						totalProb+=probCandi.get(candi);
					}
					
					for (String candi : probCandi.keySet()) {
						probCandi.put(candi, probCandi.get(candi)/totalProb);
//						System.out.println(candi+"  "+probCandi.get(candi));
					}
					probDocCandi.put(filename, probCandi);
				
				}
	}

	public void maximizeStep() {
		//gradient descent to learn weight for paths
		double beforObjFunc=calcuObjectFunc();
//		HashMap<String, Double> beforeweights=pathWeights;
//		System.out.println("maximize step:");
//		System.out.println("initla objFunc:"+beforObjFunc);
		
		while (true) {
			
			HashMap<String, Double> weightsTemp=new HashMap<String, Double>();
			
			double tempTotal=0.0;
			
			for (String metapath : paths) {
				Double pathWeight=pathWeights.get(metapath);
				
				double derivation=calculDerivation(metapath);
				
//				System.out.println("metapath:"+metapath+"    "+"derivation: "+derivation);
				
				Double pathWeightTemp=pathWeight+learningrate*derivation;
				weightsTemp.put(metapath, pathWeightTemp);
				tempTotal+=pathWeightTemp;
				
			}
			
			for (String metapath : weightsTemp.keySet()) {
				weightsTemp.put(metapath, weightsTemp.get(metapath)/tempTotal);
			}
			pathWeights=weightsTemp;
			
//			for (String meString : pathWeights.keySet()) {
//				System.out.println(meString+"\t"+pathWeights.get(meString));
//			}
			
			double objFunc=calcuObjectFunc();
//			System.out.println("objFunc: "+objFunc+"    difference: "+(objFunc-beforObjFunc));
			if (objFunc<beforObjFunc) {
				objFuncNega=true;
			}
			if (objFunc-beforObjFunc<10) {
				break;
			}
			
			beforObjFunc=objFunc;
			
			
			//stop according to weight change
//			boolean stop=true;
//			for (String metapath : pathWeights.keySet()) {
//				double diff=pathWeights.get(metapath)-beforeweights.get(metapath);
//				if (Math.abs(diff)>0.0001) {
//					stop=false;
//				}
//			}
//			if (stop) {
//				break;
//			}
//			beforeweights=pathWeights;
		}
		
		
		
	}
	
	public double calcuObjectFunc() {
		double objFunc=0.0;
		for (String filename : fileNameToMentionName.keySet()) {
			String mention=fileNameToMentionName.get(filename);
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
			
//			if (!filename.equals("186")) {
//			continue;
//			}
			
//			System.out.println(filename);
			ArrayList<ArrayList<String>> objSetForOneWebpage=objSetsForWebpages.get(filename);
		
			HashMap<String, Double> probForCandis=probDocCandi.get(filename);
			
			for (String candiName : canNames) {
				Double probForMapping=probForCandis.get(candiName);
				double partFunc=calProDocForOneEntity(objSetForOneWebpage, candiName);	
				objFunc+=probForMapping*partFunc;
				
			}
		}
		
		return objFunc;
	}
	public double calculDerivation(String path) {
		double derivation=0.0;
		for (String filename : fileNameToMentionName.keySet()) {
			String mention=fileNameToMentionName.get(filename);
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
			
//			if (!filename.equals("186")) {
//			continue;
//			}
			
//			System.out.println(filename);
			ArrayList<ArrayList<String>> objSetForOneWebpage=objSetsForWebpages.get(filename);
		
			HashMap<String, Double> probForCandis=probDocCandi.get(filename);
			
			for (String candiName : canNames) {
				Double probForMapping=probForCandis.get(candiName);
				
				double partDeri=0.0;
				
				for (int i = 0; i < 5; i++) {
					if (i==0) {
						ArrayList<String> authorSet=objSetForOneWebpage.get(i);
						
						for (String object : authorSet) {
							if (!path.endsWith("T")&&!path.equals("DC")) {
								continue;
							}
							
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								double fenzi=theta*distriForOneCandi.get(object);
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("T")||path.equals("DC")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								partDeri+=fenzi/fenmu;
							}
							
							
						}
					}
					else if (i==1) {
						ArrayList<String> venueSet=objSetForOneWebpage.get(i);
						
						for (String object : venueSet) {
							if (!path.endsWith("A")&&!path.equals("DC")) {
								continue;
							}
							
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								double fenzi=theta*distriForOneCandi.get(object);
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("A")||path.equals("DC")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								partDeri+=fenzi/fenmu;
							}
							
							
						}
						
					}
					else if (i==2) {
						ArrayList<String> termSet=objSetForOneWebpage.get(i);
						
						for (String object : termSet) {
							if (!path.endsWith("D")&&!path.equals("DC")) {
								continue;
							}
							
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								double fenzi=theta*distriForOneCandi.get(object);
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("D")||path.equals("DC")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								partDeri+=fenzi/fenmu;
							}
							
							
						}
					}
					else if (i==3) {
						ArrayList<String> yearSet=objSetForOneWebpage.get(i);
						
//						System.out.println(yearSet.size());
						for (String object : yearSet) {
							if (!path.endsWith("C")&&!path.equals("DC")) {
								continue;
							}
							
//							System.out.println(object);
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								
								double fenzi=theta*distriForOneCandi.get(object);
//								System.out.println(fenzi);
								
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("C")||path.equals("DC")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								partDeri+=fenzi/fenmu;
							}
							
							
						}
					}
					else if (i==4) {
						ArrayList<String> yearSet=objSetForOneWebpage.get(i);
						
//						System.out.println(yearSet.size());
						for (String object : yearSet) {
							if (!(path.endsWith("B")||path.endsWith("K")||path.equals("DC"))) {
								continue;
							}
							
//							System.out.println(object);
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								
								double fenzi=theta*distriForOneCandi.get(object);
//								System.out.println(fenzi);
								
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("B")||metapath.endsWith("K")||path.equals("DC")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								partDeri+=fenzi/fenmu;
							}
							
							
						}
					}
				
				
			}
				derivation+=probForMapping*partDeri;
			}
		}
		
		return derivation;
	}
	
	public void emAlgorithm() throws IOException {
		initial();
		HashMap<String, Double> beforeweights=pathWeights;
		
		while (true) {
			expectationStep();
			maximizeStep();
			boolean stop=true;
			for (String metapath : pathWeights.keySet()) {
				double diff=pathWeights.get(metapath)-beforeweights.get(metapath);
				if (Math.abs(diff)>0.0001) {
					stop=false;
				}
			}
			if (stop) {
				break;
			}
//			System.out.println(objFuncNega);
			if (objFuncNega) {
				break;
			}
			beforeweights=pathWeights;
		}
		expectationStep();
		
//		StringBuilder stringBuilder=new StringBuilder();
//		for (String meString : pathWeights.keySet()) {
//			stringBuilder.append(meString+"\t"+pathWeights.get(meString)+"\n");
//		}
//		System.out.println(stringBuilder);

		
		
		
	}

	public void for_ex_linking() throws Exception{
        readDistributionForCandidates();
        readGeneralModel();
        readObjectSetsForPages();

        double threshold=0.999999999;   //0.999999999
        boolean addNewContextKnowlegde=true;
        int i=1;
        while (true) {
            System.out.println();
            System.out.println("iteration round: "+i);
            i++;
            emAlgorithm();

            addNewContextKnowlegde = findGoldenMappingMention(threshold);
            calculAccu();
            if (!addNewContextKnowlegde) {
                break;
            }
            addContextPath();
        }
//		linkingViaExistingPathWeights.writeContextPathDistributionIntoDisk();
        printWeights();
        calculAccu();
	}
	
	public static void main(String[]args)throws Exception{
		//初始化
		LinkingAddingContextEMLearnItera linkingViaExistingPathWeights=new LinkingAddingContextEMLearnItera();
		
		linkingViaExistingPathWeights.readDistributionForCandidates();
		linkingViaExistingPathWeights.readGeneralModel();
		linkingViaExistingPathWeights.readObjectSetsForPages();
		
		double threshold=0.999999999;   //0.999999999
		boolean addNewContextKnowlegde=true;
		int i=1;
		while (true) {
			System.out.println();
			System.out.println("iteration round: "+i);
			i++;
			linkingViaExistingPathWeights.emAlgorithm();
			
		addNewContextKnowlegde=linkingViaExistingPathWeights.findGoldenMappingMention(threshold);
		linkingViaExistingPathWeights.calculAccu();
		if (!addNewContextKnowlegde) {
			break;
		}
		linkingViaExistingPathWeights.addContextPath();
		}
//		linkingViaExistingPathWeights.writeContextPathDistributionIntoDisk();
		linkingViaExistingPathWeights.printWeights();
		linkingViaExistingPathWeights.calculAccu();
		
		
		
		
		
	}
	
}

