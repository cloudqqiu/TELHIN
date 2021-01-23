package WebPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

//对原始网页进行预处理，提取出其中文本以及对网页进行编号，并保存，记录下编号与网页存放位置的对应关系
public class ReadWebPageAndGoldenMapping {

	ArrayList<String> MentionNames=new ArrayList<String>();
	
	
	//webpage new filename to original path
	public HashMap<String, String> fileNameToPath=new HashMap<String, String>();
	public HashMap<String, String> fileNameToGoldenEntityName=new HashMap<String, String>();
	public HashMap<String, String> fileNameToMentionName=new HashMap<String, String>();
	
	
	public ReadWebPageAndGoldenMapping() {
		// TODO Auto-generated constructor stub
//		String mentionDirString=".\\IMDb\\OrigWebpages";
        String mentionDirString=".\\newIMDB\\OrigWebpages";
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
	
	public void readWebPagesAndPreproFromOrig() throws Exception {
		
		Integer totalNo=0;
		
		for (String mention : MentionNames) {
			
			HtmlParser htmlParser=new HtmlParser();
		  	
			System.out.println(mention);
			
			String mentionDirString=".\\IMDb\\OrigWebpages\\"+mention;
			File mentionDir=new File(mentionDirString);
			File[] golodenClusterFiles=mentionDir.listFiles();
			for (File goldenCluster : golodenClusterFiles) {
				if (goldenCluster.isDirectory()) {
					
					File [] webpageFiles=goldenCluster.listFiles();
					for (File webpageFile : webpageFiles) {
						if (!webpageFile.getName().startsWith("url")) {
							totalNo++;
							System.out.println(totalNo);
//							System.out.println(webpageFile.getAbsolutePath());
							
							InputStreamReader write = new InputStreamReader(new FileInputStream(webpageFile.getAbsolutePath()),"UTF-8");
					    	BufferedReader br=new BufferedReader(write);
							String s;
			
						   StringBuilder stringBuilder=new StringBuilder();
						  while ((s = br.readLine() )!=null) {
						 	stringBuilder.append(s);
						 	stringBuilder.append(" ");
						   }
						
						  
						 String websiteString=stringBuilder.toString();
						 
						 
						   websiteString=htmlParser.CommonhtmlTotxt(websiteString);
			
						        String fileString1=".\\IMDb\\TextWebpages\\"+totalNo;
						    	FileWriter fileWriter=new FileWriter(fileString1);
								fileWriter.write(websiteString);
								fileWriter.close();
								
							fileNameToPath.put(totalNo.toString(), webpageFile.getAbsolutePath());
							fileNameToMentionName.put(totalNo.toString(), mention);
						
							String newAuthorName=goldenCluster.getName();
							
							fileNameToGoldenEntityName.put(totalNo.toString(), newAuthorName);
							
							
						}
						
					}
				}
				
			}
			
//			break;
			
			
		}
		
		writeFileMappingResult();
		
	}
	
	public void writeFileMappingResult() throws IOException {
		StringBuilder stringBuilder1=new StringBuilder();
		for (String fileName : fileNameToPath.keySet()) {
			stringBuilder1.append(fileName+"\t"+fileNameToPath.get(fileName)+"\n");
		}
		
//		System.out.println(stringBuilder1.toString());
		FileWriter fileWriter1=new FileWriter(".\\IMDb\\TextWebpages\\fileNameToPath");
		fileWriter1.write(stringBuilder1.toString());
		fileWriter1.close();
		
		StringBuilder stringBuilder2=new StringBuilder();
		for (String fileName : fileNameToMentionName.keySet()) {
			stringBuilder2.append(fileName+"\t"+fileNameToMentionName.get(fileName)+"\n");
		}
		
//		System.out.println(stringBuilder2.toString());
		FileWriter fileWriter2=new FileWriter(".\\IMDb\\TextWebpages\\fileNameToMentionName");
		fileWriter2.write(stringBuilder2.toString());
		fileWriter2.close();
		
		StringBuilder stringBuilder3=new StringBuilder();
		for (String fileName : fileNameToGoldenEntityName.keySet()) {
			stringBuilder3.append(fileName+"\t"+fileNameToGoldenEntityName.get(fileName)+"\n");
		}
		
//		System.out.println(stringBuilder3.toString());
		FileWriter fileWriter3=new FileWriter(".\\IMDb\\TextWebpages\\fileNameToGoldenEntityName");
		fileWriter3.write(stringBuilder3.toString());
		fileWriter3.close();
	}
	
	
	public void readTxtFileMappingResult() throws IOException {
		FileReader fr1 = new FileReader(".\\newIMDB\\TextWebpages\\fileNameToPath");
	    BufferedReader br1 = new BufferedReader(fr1);
	    String line1 = null;
	
	    while((line1 = br1.readLine())!=null){
	    	String[] strings=line1.split("\t");
	    	if (strings.length==2) {
	    		fileNameToPath.put(strings[0], strings[1]);
	    	}
	    }
	    
	    FileReader fr2 = new FileReader(".\\newIMDB\\TextWebpages\\fileNameToMentionName");
	    BufferedReader br2 = new BufferedReader(fr2);
	    String line2 = null;
	
	    while((line2 = br2.readLine())!=null){
	    	String[] strings=line2.split("\t");
	    	if (strings.length==2) {
	    		fileNameToMentionName.put(strings[0], strings[1]);
	    	}
	    }
	    
	    FileReader fr3 = new FileReader(".\\newIMDB\\TextWebpages\\fileNameToGoldenEntityName");
	    BufferedReader br3 = new BufferedReader(fr3);
	    String line3 = null;
	
	    while((line3 = br3.readLine())!=null){
	    	String[] strings=line3.split("\t");
	    	if (strings.length==2) {
	    		fileNameToGoldenEntityName.put(strings[0], strings[1]);
	    	}
	    }
	    
//	    System.out.println(fileNameToPath.get("3"));
//	    System.out.println(fileNameToMentionName.get("3"));
//	    System.out.println(fileNameToGoldenEntityName.get("3"));
	    
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
		
//		int totalPage=0;
//		for (String mentionName : mentionNameToFileList.keySet()) {
//			System.out.println(mentionName+" webpage no:"+mentionNameToFileList.get(mentionName).size());
//			totalPage+=mentionNameToFileList.get(mentionName).size();
//		}
//		System.out.println("totalPage: "+totalPage);
		//   yyw

        //yyw
        br1.close();
        fr1.close();
        br2.close();
        fr2.close();
        br3.close();
        fr3.close();
	}
	
public void readOrigniaWebPagesStatistics() throws Exception {
		
		Integer totalNo=0;
		
		for (String mention : MentionNames) {
			
			HtmlParser htmlParser=new HtmlParser();
		  	
			System.out.println(mention);
			
			String mentionDirString=".\\IMDb\\OrigWebpages\\"+mention;
			File mentionDir=new File(mentionDirString);
			File[] golodenClusterFiles=mentionDir.listFiles();
			for (File goldenCluster : golodenClusterFiles) {
				if (goldenCluster.isDirectory()) {//&&!goldenCluster.getName().equals("null")
					
					File [] webpageFiles=goldenCluster.listFiles();
					for (File webpageFile : webpageFiles) {
						if (!webpageFile.getName().startsWith("url")) {
							totalNo++;
							System.out.println(totalNo);
//							System.out.println(webpageFile.getAbsolutePath());
							
							
						
						}
				}
				
			}
			
//			break;
			
			}
		}
		
		
		
	}
	
	public static void main(String[]args)throws Exception{
		ReadWebPageAndGoldenMapping readWebPageAndGoldenMapping=new ReadWebPageAndGoldenMapping();
//		readWebPageAndGoldenMapping.readWebPagesAndPreproFromOrig();
		readWebPageAndGoldenMapping.readTxtFileMappingResult();
//		readWebPageAndGoldenMapping.readOrigniaWebPagesStatistics();
	}
}

