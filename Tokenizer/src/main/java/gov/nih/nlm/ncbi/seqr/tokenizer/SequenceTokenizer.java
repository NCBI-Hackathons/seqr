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

package gov.nih.nlm.ncbi.seqr.tokenizer;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

import java.io.BufferedReader;
import java.io.Reader;
import java.nio.MappedByteBuffer;
import java.util.Hashtable;

/**
 * This tokenizer is for tokenizing protein sequences into indexing tokens
 */

public final class SequenceTokenizer extends Tokenizer {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final StringBuilder str = new StringBuilder();
    private final MappedByteBuffer mem;
    private final Hashtable<Character, Integer> ptable;
    private final BufferedReader br;

    /**
     * creates a new SequenceTokenizer returning tokens
     */
    public SequenceTokenizer(Reader input, MappedByteBuffer mem, int skip, Hashtable<Character, Integer> ptable) {
        this(DEFAULT_TOKEN_ATTRIBUTE_FACTORY, input, mem, skip, ptable);
    }

    /**
     * creates a new SequenceTokenizer returning tokens
     */
    public SequenceTokenizer(AttributeFactory factory, Reader input, MappedByteBuffer mem, int skip, Hashtable<Character, Integer> ptable) {
        super(factory, input);
        this.mem = mem;
        this.ptable = ptable;
        this.br = new BufferedReader(input);
    }

    @Override
    public boolean incrementToken() {
        clearAttributes();
        char[] token = new char[5]; //generate 5-mers
        int indexValue = 0;
        TokenKey tk = new TokenKey();
        try {
            while (br.read(token, 0, 5) >= 4) {
                //System.out.println(String.valueOf(token) + "=AA"); //check whether read in seq data is normal
                tk = new TokenKey(token);
                // System.out.println(tk.getKey(ptable) + "=key");
                indexValue = mem.getInt(4 * tk.getKey(ptable));
                //System.out.println(indexValue + "=sss");
                // found a non-zero-length token
                termAtt.setEmpty().append(Integer.toString(indexValue));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("unable to process tokens ");
        }
        //System.out.println("no more buffer ");
        return false;//no more tokens
    }
}
