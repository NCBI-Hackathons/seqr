package gov.nih.nlm.ncbi.seqr.tokenizer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.util.*;
import org.apache.lucene.util.AttributeFactory;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Hashtable;
import java.util.Map;

/**
 * Factory
 */
public class SequenceTokenizerFactory extends TokenizerFactory  {
    public static final String INDEXER = "indexer";
    public static final String SKIP = "skip";


    private final String seqrIndexerFiles;
    private final int seqrSkipValue;
    private MappedByteBuffer mem = null;
    private Hashtable<Character, Integer> ptable;

    /**
     * Creates a new SequenceTokenizerFactory
     */
    public SequenceTokenizerFactory(Map<String, String> args) {
        super(args);
        seqrIndexerFiles = get(args, INDEXER, "");
        seqrSkipValue = getInt(args, SKIP, -1);
        ////build a hash table //ptable is: A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,V,W,Y  convert to 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19
        ptable = new Hashtable<Character, Integer>();
        char[] letters = {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y'};
        for (int j = 0; j < 20; j++) {
            ptable.put(letters[j], Integer.valueOf(j));
        }
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    /**
     * Split the input using configured pattern
     */
    @Override
    public SequenceTokenizer create(final AttributeFactory factory, final Reader in) {
        return new SequenceTokenizer(factory, in,   this.getMappedByteBuffer(), seqrSkipValue, ptable);
    }

    private MappedByteBuffer getMappedByteBuffer() {
        if (seqrIndexerFiles.isEmpty()) {
            return null;
        }
        long bufferSize = 3200000;
        FileChannel fc = null;
        //read the index array from the file index;
        try {
            fc = new RandomAccessFile(new File(seqrIndexerFiles), "rw").getChannel();
            //reader = new InputStreamReader(new FileInputStream(filename));
            mem = fc.map(FileChannel.MapMode.READ_ONLY, 0, bufferSize);
            mem.order(ByteOrder.LITTLE_ENDIAN);
            return mem;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed: read in the index file:" + seqrIndexerFiles);
            return null;
        }
    }

}
