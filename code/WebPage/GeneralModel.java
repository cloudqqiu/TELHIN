package WebPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class GeneralModel {
	
	public HashMap<String, String> fileNameToPath=new HashMap<String, String>();
	public HashMap<String, String> fileNameToGoldenEntityName=new HashMap<String, String>();
	public HashMap<String, String> fileNameToMentionName=new HashMap<String, String>();
	
	public ArrayList<String> movieObjs=new ArrayList<String>();
	public ArrayList<String> actorObjs=new ArrayList<String>();
	public ArrayList<String> directorObjs=new ArrayList<String>();
	public ArrayList<String> characterObjs=new ArrayList<String>();
	public ArrayList<String> termObjs=new ArrayList<String>();
	
	public HashMap<String, Integer> generalmodel=new HashMap<String, Integer>();
	public HashMap<String, Double> finalGM=new HashMap<String, Double>();
	
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
	  	br.close();   //yyw
	    write.close();
	}
	
	public void constructGeneralModel() throws Exception {
		
		ReadWebPageAndGoldenMapping readWebPageAndGoldenMapping=new ReadWebPageAndGoldenMapping();
		readWebPageAndGoldenMapping.readTxtFileMappingResult();
		fileNameToPath=readWebPageAndGoldenMapping.fileNameToPath;
		fileNameToMentionName=readWebPageAndGoldenMapping.fileNameToMentionName;
		fileNameToGoldenEntityName=readWebPageAndGoldenMapping.fileNameToGoldenEntityName;
	    
		Integer totalFreq=0;
		int fileTotal=0;
		
		for (String filename : fileNameToMentionName.keySet()) {
			String fileAd=".\\newIMDB\\ObjSet\\"+filename;
			File fff=new File(fileAd);
			if (!fff.exists()) {
				System.err.println(filename+" do not have object file");
				continue;
			}
//			if (!filename.equals("212")) {
//				continue;
//			}
			fileTotal++;
			readObjectFile(filename);
			
//			System.out.println("movie: "+movieObjs.size());   //yyw
			for (String string : movieObjs) {
//				System.out.println(string);   //yyw
				totalFreq++;
				if (generalmodel.containsKey(string)) {
					generalmodel.put(string, generalmodel.get(string)+1);
				}
				else {
					generalmodel.put(string, 1);
				}
				
			}
//			System.out.println("actor: "+actorObjs.size());   //yyw
			for (String string : actorObjs) {
//				System.out.println(string);   //yyw
				totalFreq++;
				if (generalmodel.containsKey(string)) {
					generalmodel.put(string, generalmodel.get(string)+1);
				}
				else {
					generalmodel.put(string, 1);
				}
			}
//			System.out.println("director: "+directorObjs.size());   //yyw
			for (String string : directorObjs) {
//				System.out.println(string);   //yyw
				totalFreq++;
				if (generalmodel.containsKey(string)) {
					generalmodel.put(string, generalmodel.get(string)+1);
				}
				else {
					generalmodel.put(string, 1);
				}
			}
//			System.out.println("character: "+characterObjs.size());   //yyw
			for (String string : characterObjs) {
//				System.out.println(string);   //yyw
				totalFreq++;
				if (generalmodel.containsKey(string)) {
					generalmodel.put(string, generalmodel.get(string)+1);
				}
				else {
					generalmodel.put(string, 1);
				}
			}
//			System.out.println("term: "+termObjs.size());   //yyw
			for (String string : termObjs) {
//				System.out.println(string);   //yyw
				totalFreq++;
				if (generalmodel.containsKey(string)) {
					generalmodel.put(string, generalmodel.get(string)+1);
				}
				else {
					generalmodel.put(string, 1);
				}
			}
			
		}
		
		int verifyFreq=0;
		
		for (String object : generalmodel.keySet()) {
			verifyFreq+=generalmodel.get(object);
			finalGM.put(object, (double)generalmodel.get(object)/(double)totalFreq);
		}
		if (verifyFreq!=totalFreq) {
			System.err.println("total freq difference: "+(verifyFreq-totalFreq));
		}
		
		
		StringBuilder stringBuilder=new StringBuilder();
		for (String object : finalGM.keySet()) {
			stringBuilder.append(object+"\t"+finalGM.get(object)+"\n");
		}
		
//		System.out.println("fileTotal: "+fileTotal);   //yyw
		String fileString1=".\\newIMDB\\finalGM";
		FileWriter fileWriter=new FileWriter(fileString1);
		fileWriter.write(stringBuilder.toString());
		fileWriter.close();
	}
	
	public void readFinalGM() throws Exception, FileNotFoundException {
		String fileAd=".\\newIMDB\\finalGM";
		InputStreamReader write = new InputStreamReader(new FileInputStream(fileAd),"UTF-8");
    	BufferedReader br=new BufferedReader(write);
		String s;
	    while ((s = br.readLine() )!=null) {
	    	String[] strings=s.split("\t");
	    	if (strings.length==2) {
				finalGM.put(strings[0], Double.parseDouble(strings[1]));
			}
	    	else {
				System.err.println("parse error for finalGM");
			}
	    	
	    }
	}
	
	public void printFinalGM() {
		System.out.println(finalGM.size());
		System.out.println(finalGM.get("Holby City"));
	}
	
	public static void main(String[]args)throws Exception{
		GeneralModel generalModel=new GeneralModel();
		generalModel.constructGeneralModel();
		
//		generalModel.readFinalGM();
		
//		generalModel.printFinalGM();
	}

}

