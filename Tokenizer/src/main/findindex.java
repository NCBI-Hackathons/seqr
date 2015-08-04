package main;

import java.io.IOException;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.Reader;

public class findindex {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	////build a hash table //ptable is: A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,V,W,Y  convert to 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19
        Hashtable<Character,Integer> ptable = new Hashtable<Character,Integer>();
        char[] letters ={'A','C','D','E','F','G','H','I','K','L','M','N','P','Q','R','S','T','V','W','Y'};
        for(int j = 0;j<20;j++){
          ptable.put(letters[j],Integer.valueOf(j)); 
         }
        int[] indextable = new int[(int)(Math.pow(5,20))];//size 5^20x1
        
        InputStreamReader reader = null;
        BufferedReader br = null;
        //read the index array from the file index;
        try{
            File filename = new File(args[0]);
            reader = new InputStreamReader(new FileInputStream(filename));
            br = new BufferedReader(reader);
            String line = " ";
            line = br.readLine();
            int k = 0;
            while(line!=null){
                line = br.readLine();
                indextable[k]=Integer.parseInt(line);
                k++;
            }
        }catch (Exception e){
         System.out.println("Failed: read in the index file");
        }finally{
            try {if (reader!=null)
                reader.close();
            if (br!=null)
                br.close();
            }catch (Exception ec){}
            }
            
        
        // read the sequence data as (5-mers) and output the indexed value
        
        InputStreamReader seqreader = null;
        BufferedReader seqbr = null;
        
        
        
        try{
            File seqfilename = new File(args[1]);
            seqreader = new InputStreamReader(new FileInputStream(seqfilename));
            seqbr = new BufferedReader(seqreader);
            char[]token = new char[5]; //generate 5-mers
            tokenkey tk = new tokenkey();
            while(seqbr.read(token,0,5)>=4){
                 tk = new tokenkey(token);
                 int indexvalue = indextable[tk.getKey(ptable)];
                 System.out.println(indexvalue);
            }
        }catch (Exception e){
             System.out.println("Failed: read in the sequence data file");
        }finally{
            try{if (seqreader!=null)
                seqreader.close();
            if (seqbr!=null)
                seqbr.close();
            }catch (Exception ec){}
            }
        }
	}
