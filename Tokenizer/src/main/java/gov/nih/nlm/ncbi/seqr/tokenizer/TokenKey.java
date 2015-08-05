package gov.nih.nlm.ncbi.seqr.tokenizer;
import java.util.*;
public class TokenKey {
    private char []tk;
    private int l; // the number of letters in one gov.nih.nlm.ncbi.tokenkey
    public TokenKey(String str){
        // generate char[] from input 5-letter string (AAAAC)
        l = str.length();
        tk = new char[l];
        tk=(str.toUpperCase()).toCharArray(); //convert the string inputs to uppercase
    }
    public TokenKey(char[] token){// it turns out that BufferedReader Method can take a char[]
        l=token.length;
        tk = token;
        for (int i =0; i<l;i++){
            if (Character.isLowerCase(tk[i])){
                tk[i]=Character.toUpperCase(tk[i]);
            }
        }
    }
    public TokenKey(){// default
        l = 0;
        tk = null;
    }
    public boolean isFullkey(){
        // check whether the key has five letters
        return (l==5);
    }

    public Integer getKey(Hashtable<Character,Integer> ptable){
        //ptable is: A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,V,W,Y  convert to 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19
        //if one letter is not a valid protein letter; return -1
        int sum = 0;
        for (int i=0;i<l;i++){
            Integer tv = ptable.get(tk[i]); // look up the value in the protein letter table
            if (tv!=null){
                //For example,  'AAACC' is 0*20^4 + 0*20^3 + 0*20^2 + 1*20^1 + 1*20^0 = 21, so the indexing term for 'AAACC' is the 22nd integer in the index file.
                sum = sum + tv*(int)(Math.pow(20,(4-i)));
            }
            else return -1;
        }
        return Integer.valueOf(sum);
    }
}