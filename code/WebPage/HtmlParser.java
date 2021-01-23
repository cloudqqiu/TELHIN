package WebPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import tagGenerator.DirectoryStream;

public class HtmlParser 
{
    
    int forwardPeriod(String line, int index)
	{
		
		char[] cs1=line.toCharArray();
		for(int a=index; a>=0;a--)
		{
			if(cs1[a]=='.')
				return a+1;
		}
	    return 0;
	}
	
    
	int backwardPeriod(String line, int index)
	{
		
		char[] cs1=line.toCharArray();
		for(int a=index; a<cs1.length;a++)
		{
			if(cs1[a]=='.')
				return a+1;
		}
	    return cs1.length;
	}
  
    public static boolean isTrimEmpty(String astr)
    {
        if ((null == astr) || (astr.length() == 0))
        {
            return true;
        }
        if (isBlank(astr.trim()))
        {
            return true;
        }
        return false;
    }

 
    public static boolean isBlank(String astr)
    {
        if ((null == astr) || (astr.length() == 0))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public String CommonhtmlTotxt(String line) throws Exception
    {
   	    line = line.replaceAll("<!--.*?-->"," ");
        line = line.replaceAll("<(style|STYLE).*?/(style|STYLE)>"," ");   
        line = line.replaceAll("<script.*?</script>", " ");  
        line = line.replaceAll("<(script|SCRIPT).*?</(script|SCRIPT)>", " ");
        line = line.replaceAll("<%.*?%>"," ");
        line = line.replaceAll("<\\?.*?\\?>"," ");
        line = line.replaceAll("<select.*?</select>", " "); 
        line = line.replaceAll("<SELECT.*?</SELECT>", " ");
        line = line.replaceAll("</?(span|SPAN|abbr|ABBR|st1|ST1).*?>", " ");  
        line= line.replaceAll("\\|", ".");
        line = line.replaceAll("<(img|IMG).*?>", " ");
        line = line.replaceAll("</?[IBUibu]>","");  
        line = line.replaceAll("</?(EM|em|cite|CITE|VAR|var|tt|TT|code|CODE|samp|SAMP|blink|BLINK|FONT|font|basefont|BASEFONT|STRONG|strong|small|SMALL).*?>","");  

        
       
	       //2009.05.17
       Map<String,String> StringEscap = new HashMap<String,String>();
       StringEscap.put("&nbsp;", " ");
       StringEscap.put("~"," ");
       StringEscap.put("&hearts;"," "); 
      
       StringEscap.put("&nbsp"," ");
       
       StringEscap.put("TITLE:",".");
       

       StringEscap.put("::",":");
       
       
       Pattern pthash = Pattern.compile("&nbsp;|~|&hearts;|&divide;|&nbsp|TITLE:|::");
       Matcher mchash= pthash.matcher(line);
       while(mchash.find())
       {
    	   line = line.replace(mchash.group(), StringEscap.get(mchash.group()));
       }
       
       
     
//       Pattern ptascii = Pattern.compile("&#(\\d+);");
//       Matcher mcascii= ptascii.matcher(line);
//       char ch; 
//       while(mcascii.find())
//       {	 
//    	   //2009.12.30
//    	   int i = new Integer(mcascii.group(1));
//    	   if (i>127) line = line.replaceAll(mcascii.group(),",");
//    	   else //2009.12.30
//    	  {
//    		   ch = (char)Integer.parseInt(mcascii.group(1));
//           //line = line.replaceAll(mcascii.group(), Character.toString(ch).replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$"));
//       	   line = line.replace(mcascii.group(), Character.toString(ch));
//    	  }
//       }    
       
       
       line = line.replaceAll("---*",".");
       
       
       line = line.replaceAll("<[a-zA-Z/!].*?>",". ");
       line = line.replaceAll("\\s+"," ");
       line = line.replaceAll("\\ \\.",".");
       
       line = line.replaceAll("\\.[\\.\\ ]+",". ");
       line = line.replaceAll("(!|\\?)\\s*\\.",".");
       line = line.replaceAll("\\s+\\.",".");
       line = line.replaceAll("\\s+"," ");
       //2009.12.30
       if(line.startsWith(".", 0)) line = line.substring(1).trim();

      return line;
    }
    
    public static void main(String[]args)throws Exception{ 
		
	  	HtmlParser htmlParser=new HtmlParser();
	  	
		for (int i = 1; i < 2; i++) {
			Integer no=i;
			String fileString=".\\data\\LinkHINData\\OrigWebpages\\Qiang Shen\\0\\"+no.toString()+".htm";
		
			InputStreamReader write = new InputStreamReader(new FileInputStream(fileString),"UTF-8");
	    	BufferedReader br=new BufferedReader(write);
			String s;

		   StringBuilder stringBuilder=new StringBuilder();
		  while ((s = br.readLine() )!=null) {
		 	stringBuilder.append(s);
		 	stringBuilder.append(" ");
		   }
		
		  
		 String websiteString=stringBuilder.toString();
//		 System.out.println(websiteString);
		 
		   websiteString=htmlParser.CommonhtmlTotxt(websiteString);

		        String fileString1=".\\data\\test.txt";
//	            d.save(websiteString, fileString1);
		    
		        System.out.println(i+"  "+websiteString);
//		    	FileWriter fileWriter=new FileWriter(fileString1);
//				fileWriter.write(websiteString);
//				fileWriter.close();
		  
  
		}
}
}


