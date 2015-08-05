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

import org.apache.lucene.analysis.TokenStream;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple Tests to ensure this factory is working
 */
public class TestSequenceTokenizerFactory extends BaseTokenStreamFactoryTestCase {
    public void testFactory() throws Exception {
        final Reader reader = new StringReader("AAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAA");
        // create PatternTokenizer
        Map<String, String> args = new HashMap<String, String>();
        args.put("indexer", "resources/good_one.11.index");
        args.put("skip", "5");
        SequenceTokenizerFactory sf = new SequenceTokenizerFactory(args);
        TokenStream stream = sf.create(newAttributeFactory(), reader);
        assertTokenStreamContents(stream, new String[]{"141351", "141351","141351","240382","141351","141351","141351","141351"});
        TokenStream stream2 = sf.create(newAttributeFactory(), new StringReader("AAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAA"));
        assertTokenStreamContents(stream2, new String[]{"141351", "141351","141351","240382","141351","141351","141351","141351"});
    }
}
