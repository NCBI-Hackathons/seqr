package gov.nih.nlm.ncbi.seqr.solr;

import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava.nbio.core.sequence.io.FastaReader;
import org.biojava.nbio.core.sequence.io.GenericFastaHeaderParser;
import org.biojava.nbio.core.sequence.io.ProteinSequenceCreator;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class FastaStreamParser extends StreamParser {
    private File fastaFile;

    private final static Logger logger = LoggerFactory.getLogger(FastaStreamParser.class);

    private class ProteinObj {
        private String id;
        private String acxn;
        private String origdefline;
        private int seqlen;
        private String sequence;

        public String getId() {
            return id;
        }

        public String getAcxn() {
            return acxn;
        }

        public String getOrigdefline() {
            return origdefline;
        }

        public int getSeqlen() {
            return seqlen;
        }

        public String getSequence() {
            return sequence;
        }

        public void setAcxn(String accession) {
            id = accession;
            acxn = accession;
        }

        public void setDefline(String defline) {
            origdefline = defline;
        }

        public void setSequence(String seq) {
            sequence = seq;
            seqlen = seq.length();
        }

    }

    public FastaStreamParser(File fastaFile) throws IOException {
        this.fastaFile = fastaFile;
    }

    public FastaStreamParser(String fastaFileName) throws IOException {
        this(new File(fastaFileName));
    }

    public void processing(Callback callback) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        FileInputStream inStream = new FileInputStream( fastaFile );
        FastaReader<ProteinSequence,AminoAcidCompound> fastaReader =
                new FastaReader<>(
                        inStream,
                        new GenericFastaHeaderParser<>(),
                        new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
        LinkedHashMap<String, ProteinSequence> b = fastaReader.process();

        for (  Map.Entry<String, ProteinSequence> entry : b.entrySet() ) {
            String header = entry.getValue().getOriginalHeader();
            String sequence = entry.getValue().getSequenceAsString();
            String[] parts = header.split("\\|");
            ProteinObj obj = new ProteinObj();

            if (parts.length < 3)
                logger.error("faste parsing error " + header);
            else {
                obj.setAcxn(parts[1]);
                obj.setDefline(parts[2]);
                obj.setSequence(sequence);

                JsonNode node = mapper.valueToTree(obj);
                callback.processSingleJSONRecord(node);
            }

        }
     }

    public void convertToJsonFile(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<ProteinObj> objList = new ArrayList<>();

        File jsonFile = new File(fileName);

        FileInputStream inStream = new FileInputStream( fastaFile );
        FastaReader<ProteinSequence,AminoAcidCompound> fastaReader =
                new FastaReader<>(
                        inStream,
                        new GenericFastaHeaderParser<>(),
                        new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
        LinkedHashMap<String, ProteinSequence> b = fastaReader.process();

        for (  Map.Entry<String, ProteinSequence> entry : b.entrySet() ) {
            String header = entry.getValue().getOriginalHeader();
            String sequence = entry.getValue().getSequenceAsString();
            String[] parts = header.split("\\|");
            ProteinObj obj = new ProteinObj();

            if (parts.length < 3)
                logger.error("faste parsing error " + header);
            else {
                obj.setAcxn(parts[1]);
                obj.setDefline(parts[2]);
                obj.setSequence(sequence);

                objList.add(obj);
            }
        }
        try {
            JsonNode node = mapper.valueToTree(objList);
            System.out.println(node.toString());
            mapper.writeValue(jsonFile, objList);
            //System.out.println(mapper.writeValueAsString(objList));

        } catch (JsonGenerationException e) {
            logger.error("JsonGeneration error during fasta conversion: " + e.getMessage());
        } catch (JsonMappingException e) {
            logger.error("JsonMapping error during fasta conversion: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during fasta conversion: " + e.getMessage());
        }
    }
}


