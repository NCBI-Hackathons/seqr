package gov.nih.nlm.ncbi.seqr.nuc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.transcription.Frame;

public class DNASequenceStreamMap extends LinkedHashMap<String, ProteinSequence> {
	
	private final Iterator<Map.Entry<String, DNASequence>> seq;
	protected Iterator<Map.Entry<String, ProteinSequence>> iter = new HashMap<String, ProteinSequence>().entrySet().iterator();
	
	public DNASequenceStreamMap(Map<String, DNASequence> seq){
		this.seq = seq.entrySet().iterator();
	}
	
	@Override
	public Set<Map.Entry<String, ProteinSequence>> entrySet(){
		return new HashSet<Map.Entry<String, ProteinSequence>>(){
			
			public Iterator<Map.Entry<String, ProteinSequence>> iterator(){
				return new Iterator<Map.Entry<String, ProteinSequence>>(){

					public boolean hasNext() {
						return seq.hasNext() || iter.hasNext();
					}

					public java.util.Map.Entry<String, ProteinSequence> next() {
						try {
							return iter.next();
						} catch (NoSuchElementException e){
							Map.Entry<String, DNASequence> contig = seq.next();
							String name = contig.getKey();
							DNASequence dna = contig.getValue();
							List<Map.Entry<String, ProteinSequence>> proteins = sixFrameTranslate(name, dna);
							iter = proteins.iterator();
							return iter.next();
						}
					}
					private List<java.util.Map.Entry<String, ProteinSequence>> sixFrameTranslate(
							String name,
							DNASequence dna) {
						ArrayList<Map.Entry<String, ProteinSequence>> l = new ArrayList<Map.Entry<String, ProteinSequence>>();
						l.add(new SimpleEntry<String, ProteinSequence>(name + "_one", dna.getRNASequence(Frame.ONE).getProteinSequence()));
						l.add(new SimpleEntry<String, ProteinSequence>(name + "_two", dna.getRNASequence(Frame.TWO).getProteinSequence()));
						l.add(new SimpleEntry<String, ProteinSequence>(name + "_three", dna.getRNASequence(Frame.THREE).getProteinSequence()));
						l.add(new SimpleEntry<String, ProteinSequence>(name + "_revone", dna.getRNASequence(Frame.REVERSED_ONE).getProteinSequence()));
						l.add(new SimpleEntry<String, ProteinSequence>(name + "_revtwo", dna.getRNASequence(Frame.REVERSED_TWO).getProteinSequence()));
						l.add(new SimpleEntry<String, ProteinSequence>(name + "_revthree", dna.getRNASequence(Frame.REVERSED_THREE).getProteinSequence()));
						return l;
					}
					
				};
			}
		};
	}
	
	
 public static Map<String, ProteinSequence> maybeConvert(File fasta, Boolean convert) throws IOException{
	 if (convert != null && convert) {
		 return new DNASequenceStreamMap(FastaReaderHelper.readFastaDNASequence(fasta)); 
	 } else {
		 return FastaReaderHelper.readFastaProteinSequence(fasta);
	 }
 }
	
	

}
