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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;


import IMDbPreprocess.CandidateGeneration;
import WebPage.GeneralModel;
import WebPage.ReadWebPageAndGoldenMapping;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.util.Pair;

public class LinkingViaEMNoContext {
	public HashMap<String, Double> authorNameToPriorScore=new HashMap<String, Double>();
	public HashMap<String, ArrayList<String>> MentionCandidateNames=new HashMap<String, ArrayList<String>>();
	public HashMap<String, String> fileNameToPath=new HashMap<String, String>();
	public HashMap<String, String> fileNameToGoldenEntityName=new HashMap<String, String>();
	public HashMap<String, String> fileNameToMentionName=new HashMap<String, String>();
	
	ArrayList<String> paths=new ArrayList<String>();
	HashMap<String, HashMap<String, HashMap<String, Double>>> distriForAllPathsCandidates=new HashMap<String, HashMap<String,HashMap<String,Double>>>();
	HashMap<String, Double> pathWeights=new HashMap<String, Double>();
	
	public HashMap<String, Double>  NametoScore=new HashMap<String, Double>();
    
	public ArrayList<String> movieObjs=new ArrayList<String>();
	public ArrayList<String> actorObjs=new ArrayList<String>();
	public ArrayList<String> directorObjs=new ArrayList<String>();
	public ArrayList<String> characterObjs=new ArrayList<String>();
	public ArrayList<String> termObjs=new ArrayList<String>();
	
	public HashMap<String, Double> finalGM=new HashMap<String, Double>();
	public HashMap<String, ArrayList<ArrayList<String>>> objSetsForWebpages=new HashMap<String, ArrayList<ArrayList<String>>>();
	
	public HashMap<String, HashMap<String, Double>> probDocCandi=new HashMap<String, HashMap<String,Double>>();
	
	public HashMap<String, String> fileNameToPrediEntityName=new HashMap<String, String>();
	
	boolean objFuncNega=false;
	
	
	double theta=0.1;
	double learningrate=0.000003;
	
	public LinkingViaEMNoContext() {
		// TODO Auto-generated constructor stub
		
		paths.add("AB");
		paths.add("AMK");
		paths.add("AMT");
		paths.add("AMC");
		paths.add("AMA");
		paths.add("AMD");
		
		for (String metapath : paths) {
			HashMap<String, HashMap<String, Double>> distriForAllCandis=new HashMap<String, HashMap<String,Double>>();
			distriForAllPathsCandidates.put(metapath, distriForAllCandis);
		}
		
	}
	public void ReadDistribution(String metapath, String candiName) throws Exception, IOException
	{
//		System.out.println("Read distribution ...");
		 NametoScore=new HashMap<String, Double>();
		File file=new  File(".\\newIMDB\\GeneratedData\\Distribution\\"+metapath+"\\"+candiName);
		if (!file.exists()) {
			return;
		}
		FileReader fr1 = new FileReader(file);
	    BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;

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
//			System.out.println("read dis for "+metapath);   //yyw
			HashMap<String, HashMap<String, Double>> distriForAllCandis=distriForAllPathsCandidates.get(metapath);
			
			for (String mention : MentionCandidateNames.keySet()) {
				ArrayList<String> canNames=MentionCandidateNames.get(mention);
	
				for (String canName: canNames) {
//						System.out.println("canName: "+canName);
					 
						
						ReadDistribution(metapath, canName);
						
						HashMap<String, Double> distriForCandi=NametoScore;
						distriForAllCandis.put(canName, distriForCandi);
						
				}
					
					
				}
		}
		
//			for (String metapath : distriForAllPathsCandidates.keySet()) {
//				HashMap<String, HashMap<String, Double>> distriForAllCandis=distriForAllPathsCandidates.get(metapath);
//				System.out.println(metapath+"  size: "+distriForAllCandis.size());
//				if (metapath.equals("AMA")) {
//					HashMap<String, Double> disForOneCandi=distriForAllCandis.get("Adams, Jonathan (XIV)");
//					for (String string : disForOneCandi.keySet()) {
//						System.out.println(string+" "+disForOneCandi.get(string));
//					}
//				}
//			}
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
	    br.close();   //yyw
	    write.close();
	  
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
//				if (!filename.equals("186")) {
//					continue;
//				}
			readObjectFile(filename);
			
//			System.out.println("movie: "+movieObjs.size());	
//			System.out.println("actor: "+actorObjs.size());
//			System.out.println("director: "+directorObjs.size());
//			System.out.println("character: "+characterObjs.size());
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
	
	public void randomInit() {
		Random random=new Random();
		double totalRan=0.0;
		for (String metapath : paths) {
			double ran=random.nextDouble();
			totalRan+=ran;
			pathWeights.put(metapath, ran);
		}
		for (String metapath : paths){
			pathWeights.put(metapath, pathWeights.get(metapath)/totalRan);
		}
	}
	
	public void initial() {
		Double initPathWeight=1.0/(double)paths.size();
		
		
		for (String metapath : paths) {
			pathWeights.put(metapath, 0.0);
		}
		
//			for (String metapath : paths) {
//				pathWeights.put(metapath, initPathWeight);
//			}
		
		
//			pathWeights.put("AB", 1.0);
			
	}
	public void expectationStep() {
		
//		System.out.println("expectation step: ");   //yyw
		
		//对每篇文章，计算其各个candidate的概率P(m,d,e),存放在probDocCandi中
		for (String filename : fileNameToMentionName.keySet()) {
			String mention=fileNameToMentionName.get(filename);
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
			
			
			if (!objSetsForWebpages.containsKey(filename)) {
				System.out.println("file not found "+ filename);
				continue;
			}
//				if (!filename.equals("186")) {
//				continue;
//				}
			HashMap<String, Double> probCandi=new HashMap<String, Double>();
			
			
//				System.out.println(filename);
			ArrayList<ArrayList<String>> objSetForOneWebpage=objSetsForWebpages.get(filename);
			double maxProbLN=-1.0E300;
			
			for (String candiName : canNames) {
				//计算P（d|e）
				double probDocForCandiLN=calProDocForOneEntity(objSetForOneWebpage, candiName);	
				probDocForCandiLN+=Math.log(authorNameToPriorScore.get(candiName));
				
//					System.out.println(candiName+"   "+probDocForCandiLN);
				probCandi.put(candiName, probDocForCandiLN);
				if (probDocForCandiLN>maxProbLN) {
					maxProbLN=probDocForCandiLN;
				}
			}
//				System.out.println("maxProbLN: "+maxProbLN);  //this number is for preventing overflow
			
			double totalProb=0.0;
			for (String candi : probCandi.keySet()) {
				probCandi.put(candi, Math.exp(probCandi.get(candi)-maxProbLN));
//					System.out.println(candi+"  "+probCandi.get(candi));
				totalProb+=probCandi.get(candi);
			}
			
			for (String candi : probCandi.keySet()) {
				probCandi.put(candi, probCandi.get(candi)/totalProb);
//					System.out.println(candi+"  "+probCandi.get(candi));
			}
			probDocCandi.put(filename, probCandi);
			
			
			double maxSim=-1.0;
			String maxCandiName="";
			
			for (String candiName : probCandi.keySet()) {
				Double similarity=probCandi.get(candiName);

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
			}
			
		}
		
//		calculAccu();   //yyw
	}
	
		
	public void calculAccu() {
		int totalFil=0;
		int corret=0;
		
		System.out.println("calcul");
		for (String filename : fileNameToPrediEntityName.keySet()) {
			String predictEntity=fileNameToPrediEntityName.get(filename);
			String goldenEntity=fileNameToGoldenEntityName.get(filename);
			totalFil++;
			if (predictEntity.equals(goldenEntity)) {
//				System.out.print("  "+filename);
				corret++;
			}
		}
		System.out.println();
		Double accu=(double)corret/(double)totalFil;
		System.out.println("accuracy: "+accu);
		System.out.println("correctly linked mentions: "+corret);
		System.out.println("total mentions: "+totalFil);
		
		StringBuilder stringBuilder=new StringBuilder();
		for (String meString : pathWeights.keySet()) {
			stringBuilder.append(meString+"\t"+pathWeights.get(meString)+"\n");
		}
		
				
		System.out.println(stringBuilder);
	}

	public Pair<Integer, Integer> calculAcc(){    //yyw
		int totalFil=0;
		int corret=0;

//		System.out.println("calcul");
		for (String filename : fileNameToPrediEntityName.keySet()) {
			String predictEntity=fileNameToPrediEntityName.get(filename);
			String goldenEntity=fileNameToGoldenEntityName.get(filename);
			totalFil++;
			if (predictEntity.equals(goldenEntity)) {
//				System.out.print("  "+filename);
				corret++;
			}
		}
		System.out.println();
		Double accu=(double)corret/(double)totalFil;
		System.out.println("accuracy: "+accu);
		System.out.println("correctly linked mentions: "+corret);
		System.out.println("total mentions: "+totalFil);

		return new Pair<Integer, Integer>(corret, totalFil);
	}
	
	public Double calProDocForOneEntity(ArrayList<ArrayList<String>> objSetForOneWebpage, String candiName) {
		Double prob=0.0;
		
		for (int i = 0; i < 5; i++) {
			if (i==0) {
				//movie title
				ArrayList<String> authorSet=objSetForOneWebpage.get(i);
				
				for (String object : authorSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("T")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					if (finalGM.get(object) != null){
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					}
					else
					{
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*0.000001;   //yyw
					}
					prob+=Math.log(probObjForCandi);
				}
			}
			else if (i==1) {
				//ACTOR
				ArrayList<String> venueSet=objSetForOneWebpage.get(i);
				for (String object : venueSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("A")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					if (finalGM.get(object) != null){
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					}
					else
					{
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*0.000001;   //yyw
					}
					prob+=Math.log(probObjForCandi);
				}
			}
			else if (i==2) {
				//DIRECTOR
				ArrayList<String> termSet=objSetForOneWebpage.get(i);
				for (String object : termSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("D")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					if (finalGM.get(object) != null){
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					}
					else
					{
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*0.000001;   //yyw
					}
					prob+=Math.log(probObjForCandi);
				}
			}
			else if (i==3) {
				//CHARACTER
				ArrayList<String> yearSet=objSetForOneWebpage.get(i);
				for (String object : yearSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("C")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					if (finalGM.get(object) != null){
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					}
					else
					{
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*0.000001;   //yyw
					}
					prob+=Math.log(probObjForCandi);
				}
			}
			else if (i==4) {
				//TERM
				ArrayList<String> yearSet=objSetForOneWebpage.get(i);
				for (String object : yearSet) {
					double probObjForCandi=0.0;
					for (String metapath : distriForAllPathsCandidates.keySet()) {
						if (metapath.endsWith("B")||metapath.endsWith("K")) {
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(metapath).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								probObjForCandi+=distriForOneCandi.get(object)*pathWeights.get(metapath);
							}
						}
					}
					if (finalGM.get(object) != null){
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*finalGM.get(object);
					}
					else
					{
						probObjForCandi=theta*probObjForCandi+(1.0-theta)*0.000001;   //yyw
					}
					prob+=Math.log(probObjForCandi);
				}
			}
		
		
	}
		return prob;
	}
	public void maximizeStep() {
		//gradient descent to learn weight for paths
		double beforObjFunc=calcuObjectFunc();
//			HashMap<String, Double> beforeweights=pathWeights;

//		System.out.println("maximize step:");   //yyw
//		System.out.println("initla objFunc:"+beforObjFunc);   //yyw
		
		while (true) {
			
			HashMap<String, Double> weightsTemp=new HashMap<String, Double>();
			
			double tempTotal=0.0;
			
			for (String metapath : paths) {
				Double pathWeight=pathWeights.get(metapath);
				
				double derivation=calculDerivation(metapath);
				
//				System.out.println("metapath:"+metapath+"    "+"derivation: "+derivation);   //yyw
				
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
//			}   //yyw
			
			double objFunc=calcuObjectFunc();
//			System.out.println("objFunc: "+objFunc+"    difference: "+(objFunc-beforObjFunc));   //yyw
			if (objFunc<beforObjFunc) {
				objFuncNega=true;
			}
			if (objFunc-beforObjFunc<10) {
				break;
			}
			
			beforObjFunc=objFunc;
			
			
			//stop according to weight change
//				boolean stop=true;
//				for (String metapath : pathWeights.keySet()) {
//					double diff=pathWeights.get(metapath)-beforeweights.get(metapath);
//					if (Math.abs(diff)>0.0001) {
//						stop=false;
//					}
//				}
//				if (stop) {
//					break;
//				}
//				beforeweights=pathWeights;
		}
		
		
		
	}
	
	public double calcuObjectFunc() {
		double objFunc=0.0;
		for (String filename : fileNameToMentionName.keySet()) {
			String mention=fileNameToMentionName.get(filename);
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
			
//				if (!filename.equals("186")) {
//				continue;
//				}
			
//				System.out.println(filename);
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
			
//				if (!filename.equals("186")) {
//				continue;
//				}
			
//				System.out.println(filename);
			ArrayList<ArrayList<String>> objSetForOneWebpage=objSetsForWebpages.get(filename);
		
			HashMap<String, Double> probForCandis=probDocCandi.get(filename);
			
			for (String candiName : canNames) {
				Double probForMapping=probForCandis.get(candiName);
				
				double partDeri=0.0;
				
				for (int i = 0; i < 5; i++) {
					if (i==0) {
						ArrayList<String> authorSet=objSetForOneWebpage.get(i);
						
						for (String object : authorSet) {
							if (!path.endsWith("T")) {
								continue;
							}
							
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								double fenzi=theta*distriForOneCandi.get(object);
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("T")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}

								if (finalGM.get(object) != null){
									fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								}
								else
								{
									fenmu=theta*fenmu+(1.0-theta)*0.000001;   //yyw
								}
								partDeri+=fenzi/fenmu;
							}
							
							
						}
					}
					else if (i==1) {
						ArrayList<String> venueSet=objSetForOneWebpage.get(i);
						
						for (String object : venueSet) {
							if (!path.endsWith("A")) {
								continue;
							}
							
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								double fenzi=theta*distriForOneCandi.get(object);
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("A")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								if (finalGM.get(object) != null){
									fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								}
								else
								{
									fenmu=theta*fenmu+(1.0-theta)*0.000001;   //yyw
								}
								partDeri+=fenzi/fenmu;
							}
							
							
						}
						
					}
					else if (i==2) {
						ArrayList<String> termSet=objSetForOneWebpage.get(i);
						
						for (String object : termSet) {
							if (!path.endsWith("D")) {
								continue;
							}
							
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								double fenzi=theta*distriForOneCandi.get(object);
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("D")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								if (finalGM.get(object) != null){
									fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								}
								else
								{
									fenmu=theta*fenmu+(1.0-theta)*0.000001;   //yyw
								}
								partDeri+=fenzi/fenmu;
							}
							
							
						}
					}
					else if (i==3) {
						ArrayList<String> yearSet=objSetForOneWebpage.get(i);
						
//							System.out.println(yearSet.size());
						for (String object : yearSet) {
							if (!path.endsWith("C")) {
								continue;
							}
							
//								System.out.println(object);
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								
								double fenzi=theta*distriForOneCandi.get(object);
//									System.out.println(fenzi);
								
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("C")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								if (finalGM.get(object) != null){
									fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								}
								else
								{
									fenmu=theta*fenmu+(1.0-theta)*0.000001;   //yyw
								}
								partDeri+=fenzi/fenmu;
							}
							
							
						}
					}
					else if (i==4) {
						ArrayList<String> yearSet=objSetForOneWebpage.get(i);
						
//							System.out.println(yearSet.size());
						for (String object : yearSet) {
							if (!(path.endsWith("B")||path.endsWith("K"))) {
								continue;
							}
							
//								System.out.println(object);
							HashMap<String, Double> distriForOneCandi=distriForAllPathsCandidates.get(path).get(candiName);
							if (distriForOneCandi.containsKey(object)) {
								
								double fenzi=theta*distriForOneCandi.get(object);
//									System.out.println(fenzi);
								
								double fenmu=0.0;
								for (String metapath : distriForAllPathsCandidates.keySet()) {
									if (metapath.endsWith("B")||metapath.endsWith("K")) {
										HashMap<String, Double> distri=distriForAllPathsCandidates.get(metapath).get(candiName);
										if (distri.containsKey(object)) {
											fenmu+=distri.get(object)*pathWeights.get(metapath);
										}
									}
								}
								if (finalGM.get(object) != null){
									fenmu=theta*fenmu+(1.0-theta)*finalGM.get(object);
								}
								else
								{
									fenmu=theta*fenmu+(1.0-theta)*0.000001;   //yyw
								}

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
//				System.out.println(objFuncNega);
			if (objFuncNega) {
				break;
			}
			beforeweights=pathWeights;
		}
		expectationStep();
		
		StringBuilder stringBuilder=new StringBuilder();
		for (String meString : pathWeights.keySet()) {
			stringBuilder.append(meString+"\t"+pathWeights.get(meString)+"\n");
		}
		
				
//		System.out.println(stringBuilder);   //yyw
			String fileString1=".\\data\\LinkHINData\\metapathWeights";
			FileWriter fileWriter=new FileWriter(fileString1);
			fileWriter.write(stringBuilder.toString());
			fileWriter.close();
		
		
		
	}
	public void saveResult() throws IOException{   //yyw
		StringBuilder stringBuilder1=new StringBuilder();
		StringBuilder stringBuilder2=new StringBuilder();
		for (String meString : fileNameToPrediEntityName.keySet()) {
			stringBuilder1.append(meString+"\t"+fileNameToPrediEntityName.get(meString)+"\n");
			if (!fileNameToPrediEntityName.get(meString).equals(fileNameToGoldenEntityName.get(meString))){
				stringBuilder2.append(meString+"\t"+fileNameToGoldenEntityName.get(meString)+"\t"+fileNameToPrediEntityName.get(meString)+"\n");
			}
		}


		String fileString1=".\\data\\LinkHINData\\predictresult";
		FileWriter fileWriter=new FileWriter(fileString1);
		fileWriter.write(stringBuilder1.toString());
		fileWriter.flush();
		String fileString2=".\\data\\LinkHINData\\errresult";
		fileWriter=new FileWriter(fileString2);
		fileWriter.write(stringBuilder2.toString());
		fileWriter.close();
	}
	public void for_ex_linking() throws Exception{  //yyw
		readDistributionForCandidates();
		readGeneralModel();
		readObjectSetsForPages();

		emAlgorithm();
		saveResult();
	}

	public static void main(String[]args)throws Exception{
		LinkingViaEMNoContext linkingViaEM=new LinkingViaEMNoContext();
		linkingViaEM.readDistributionForCandidates();
		linkingViaEM.readGeneralModel();
		linkingViaEM.readObjectSetsForPages();
		

//			for (int i = 0; i < 10; i++) {
//				linkingViaEM.initial();
//				linkingViaEM.expectationStep();
//			}
		
		linkingViaEM.emAlgorithm();
		linkingViaEM.saveResult(); //yyw
	}

}

