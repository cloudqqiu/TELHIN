package IMDbPreprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
	public static void main(String[]args)throws Exception{
		String movieString="[[Image:Levellers declaration and standard.gif|thumb|200px|Woodcut from a [[Diggers]] document by [[William Everard (Digger)|William Everard]]]]";

		 Pattern p = Pattern.compile("\\[\\[[^\\[]*?\\]\\]"); 
			Matcher m = p.matcher(movieString); 
			while (m.find()) {
				String movieName=m.group().trim();		
				System.out.println(movieName);
			}
		
	}
}
