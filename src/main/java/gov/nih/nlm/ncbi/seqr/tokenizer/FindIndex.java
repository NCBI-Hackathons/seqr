package gov.nih.nlm.ncbi.seqr.tokenizer;

import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.ByteOrder;
import java.io.RandomAccessFile;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;

public class FindIndex {

	private List<Integer> keyindex; // the key index contain the index for in
									// the index file

	// private int sz; // the size of keyindex

	public FindIndex(String s) {
		// construct from the input sequence
		// s file name
		// //build a hash table //ptable is:
		// A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,V,W,Y convert to
		// 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19
		Hashtable<Character, Integer> ptable = new Hashtable<Character, Integer>();
		char[] letters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L',
				'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y' };
		for (int j = 0; j < 20; j++) {
			ptable.put(letters[j], Integer.valueOf(j));
		}

		InputStreamReader seqreader = null;
		BufferedReader seqbr = null;
		int indexkey = 0;
		keyindex = new ArrayList<Integer>();
		try {
			File seqfilename = new File(s);
			seqreader = new InputStreamReader(new FileInputStream(seqfilename));
			seqbr = new BufferedReader(seqreader);
			char[] token = new char[5]; // generate 5-mers
			TokenKey tk = new TokenKey();

			while (seqbr.read(token, 0, 5) >= 4) {
				// System.out.println(String.valueOf(token)); //check whether
				// read in seq data is normal
				tk = new TokenKey(token);
				indexkey = tk.getKey(ptable);
				// System.out.println(tk.getKey(ptable)); // check whether the
				// key is caomputed correctly
				// indexvalue = mem.getInt(4*tk.getKey(ptable));
				// System.out.println(indexvalue);
				if (indexkey != -1) {
					keyindex.add(indexkey);
				}
			}
		} catch (Exception e) {
			System.out.println("Failed: read in the sequence data file");
		} finally {
			try {
				if (seqreader != null)
					seqreader.close();
				if (seqbr != null)
					seqbr.close();
			} catch (Exception ec) {
			}
		}
	}

	// add a new method to read directly from the string
	public static List<Integer> hashIndex(String s) {
		// constructor the key index directly from the raw sequence input
		// specify the int value 0, for the mode - reading sequence directly

		Hashtable<Character, Integer> ptable = new Hashtable<Character, Integer>();
		char[] letters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L',
				'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y' };
		for (int j = 0; j < 20; j++) {
			ptable.put(letters[j], Integer.valueOf(j));
		}
		TokenKey tk = new TokenKey();
		char[] inputseq = (s.toUpperCase()).toCharArray();
		List<Integer> indexList = new ArrayList<Integer>();
		int indexkey = 0;
		char[] token = new char[5];
		for (int k = 0; k < (s.length() - 4); k += 5) {
			for (int j = 0; j < 5; j++) {
				token[j] = inputseq[k + j];
			}
			tk = new TokenKey(token);
			indexkey = tk.getKey(ptable);
			if (indexkey != -1) {
				indexList.add(indexkey);
			}
		}
		return indexList;
	}

	// new method end, block it if it does not work
	public FindIndex() {
		// default constructor
		// sz = 0;
		keyindex = null;
	}

	public int[] getIndex(String s) {
		// s: index file name; return the index
		long bufferSize = 3368800 * 4;
		FileChannel fc = null;
		MappedByteBuffer mem = null;

		try {
			fc = new RandomAccessFile(new File(s), "rw").getChannel();
			// fc = new RandomAccessFile(new File(s,"rw").getChannel();
			mem = fc.map(FileChannel.MapMode.READ_ONLY, 0, bufferSize);
			mem.order(ByteOrder.LITTLE_ENDIAN);
		} catch (Exception e) {
			System.out.println("Failed: read in the index file");
		} finally {
			try {// if (fc!=null)
					// fc.close();
					// if (br!=null)
					// br.close();
			} catch (Exception ec) {
			}
		}

		int sz = keyindex.size();
		int[] indexread = new int[sz];
		for (int i = 0; i < sz; i++) {
			indexread[i] = mem.getInt(4 * keyindex.get(i));
		}
		return indexread;
	}

	public int[] getIndex(MappedByteBuffer m) {
		// can also take the MappedByteBuffer, more efficient
		m.order(ByteOrder.LITTLE_ENDIAN);
		int sz = keyindex.size();
		int[] indexread = new int[sz];
		for (int i = 0; i < sz; i++) {
			indexread[i] = m.getInt(4 * keyindex.get(i));
		}
		return indexread;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// the convinient way to get the index file - for each findindex object,
		// load the index file
		FindIndex pindex = new FindIndex(args[0]);
		// a more efficient way, read the index file first into
		// MappedByteBuffer, different findindex objects can share one
		// ByteBuffer
		int[] sindex = pindex.getIndex(args[1]);
		for (int i = 0; i < sindex.length; i++) {
			System.out.println(sindex[i]);
		}
	}
}
