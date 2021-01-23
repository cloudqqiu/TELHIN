package Baseline;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;



import IMDbPreprocess.CandidateGeneration;
import WebPage.ReadWebPageAndGoldenMapping;

public class PopularityBasedMethod {
	
	public HashMap<String, Double> authorNameToPriorScore=new HashMap<String, Double>();
	public HashMap<String, ArrayList<String>> MentionCandidateNames=new HashMap<String, ArrayList<String>>();
	public HashMap<String, String> fileNameToPath=new HashMap<String, String>();
	public HashMap<String, String> fileNameToGoldenEntityName=new HashMap<String, String>();
	public HashMap<String, String> fileNameToMentionName=new HashMap<String, String>();
	
	
	public HashMap<String, String> fileNameToPrediEntityName=new HashMap<String, String>();
	
	
	
	public void geneCandidates() throws IOException {
		CandidateGeneration candidateGeneration=new CandidateGeneration();
		candidateGeneration.ReadCandidates();
		candidateGeneration.ReadPriorPageRankScore();
		
		authorNameToPriorScore=candidateGeneration.authorNameToScore;
		MentionCandidateNames=candidateGeneration.MentionCandidateNames;
		
		
//		candidateGeneration.ReadPriorPageRankScore();
//		candidateGeneration.printCandis();
	}
	
	public void calculAccu() throws IOException{
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

		StringBuilder stringBuilder=new StringBuilder();
		for (String meString : fileNameToPrediEntityName.keySet()) {
			stringBuilder.append(meString+"\t"+fileNameToPrediEntityName.get(meString)+"\n");
		}


		String fileString1=".\\data\\LinkHINData\\popularityresult";
		FileWriter fileWriter=new FileWriter(fileString1);
		fileWriter.write(stringBuilder.toString());
		fileWriter.close();
	}
	
	public void liningViaPopularity() throws IOException {
		geneCandidates();
		
		ReadWebPageAndGoldenMapping readWebPageAndGoldenMapping=new ReadWebPageAndGoldenMapping();
		readWebPageAndGoldenMapping.readTxtFileMappingResult();
		fileNameToPath=readWebPageAndGoldenMapping.fileNameToPath;
		fileNameToMentionName=readWebPageAndGoldenMapping.fileNameToMentionName;
		fileNameToGoldenEntityName=readWebPageAndGoldenMapping.fileNameToGoldenEntityName;
	    
		for (String filename : fileNameToMentionName.keySet()) {
			String mention=fileNameToMentionName.get(filename);
			ArrayList<String> canNames=MentionCandidateNames.get(mention);
			
			Double maxSim=-1.0;
			String maxCandiName="";
			
			
			
			for (String candiName : canNames) {
				Double similarity=authorNameToPriorScore.get(candiName);
				if (similarity>maxSim) {
					maxSim=similarity;
					maxCandiName=candiName;
				}
			}
			if (maxCandiName.length()>0&&maxSim>-0.5) {
				System.out.println(fileNameToMentionName.get(filename)+"  predict entity:"+maxCandiName+"  prior score:"+authorNameToPriorScore.get(maxCandiName));
				fileNameToPrediEntityName.put(filename, maxCandiName);
			}
			else {
				System.err.println("no find mapping entity");
			}
			
			
		}
		calculAccu();
		
	}

	public static void main(String[]args)throws Exception{
		PopularityBasedMethod popularityBasedMethod=new PopularityBasedMethod();
		popularityBasedMethod.liningViaPopularity();
	}
}

