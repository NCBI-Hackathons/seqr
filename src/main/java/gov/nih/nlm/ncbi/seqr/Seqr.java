package gov.nih.nlm.ncbi.seqr;

import gov.nih.nlm.ncbi.seqr.nuc.DNASequenceStreamMap;
import gov.nih.nlm.ncbi.seqr.solr.JsonStreamParser;
import gov.nih.nlm.ncbi.seqr.solr.SeqrController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import net.sourceforge.argparse4j.internal.HelpScreenException;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.handler.admin.SystemInfoHandler;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;


import com.diffplug.common.base.Errors;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Seqr {

    private static SolrServer solrServer;

	private static String version = "0.0.1a";
	private static String versionSolr = "4";
    
    private static final String SEARCH = "search";
    private static final String INDEX = "index";
    private static final String COLLECTION = "sequence";


	private static final String FASTA = "fasta";
	private static final String JSON = "json";
	private static final String CSV = "CSV";

	static final String[] informats = {FASTA, JSON, CSV};
    
    public static void main(final String[] args) {

        ArgumentParser parser = buildParser();

        try {
			Namespace space = parser.parseArgs(args);
			handleCommand(space);
			System.out.println(space);
		} catch (HelpScreenException e) {
			System.exit(0);
        } catch (ArgumentParserException e) {
        	System.out.println(e);
            parser.printHelp();
            System.exit(1);
        } catch (URISyntaxException e) {
			System.out.println("Couldn't parse Solr server path or address.");
			System.exit(1);
		}
    }

	private static ArgumentParser buildParser() {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("seqr")
                .defaultHelp(true)
                .description("SEQR, now for shells.")
                .version("${prog} " + version);

        ArgumentGroup unused = parser.addArgumentGroup("unused").description("Unused compatibility arguments from BLASTP");
        //parser.addArgument("command").type(String.class).dest("command").help("Non-optional command").choices("search", "index", "load").required(true);
        Subparsers subprsrs = parser.addSubparsers().description("SEQR commands").metavar("COMMAND").dest("command");
        Subparser search = subprsrs.addParser("search").help("look for something");
        Subparser index = subprsrs.addParser("index").help("create an index");
        
        //add options for search
        search.addArgument("query_file").type(Arguments.fileType().acceptSystemIn().verifyCanRead()).dest("input_file").help("query file for input").metavar("QUERY FASTA");
        search.addArgument("-n", "--is_dna").action(Arguments.storeTrue()).help("Input FASTA is DNA nucleotide, not protein");
        search.addArgument("--solr_query").type(String.class).help("filtering query in Solr query language");
        search.addArgument("--index_file").type(Arguments.fileType().verifyCanRead()).help("pre-calculated index file");
        search.addArgument("--num_alignments").type(Integer.class).help("number of results to return");
        search.addArgument("--start_alignments").type(Integer.class).help("begin output at the Nth result").metavar("N");
        
        //add special outformat parser
        search.addArgument("--outfmt").type(String.class).dest("format").help(Output.OUTPUTHELP).nargs("+");
        
        //add required, options for index
        index.addArgument("input_files").type(Arguments.fileType().acceptSystemIn().verifyCanRead()).nargs("+").required(true);

        search.addArgument("-d", "--db").type(String.class).dest("solr_url_or_path").help("URL of Solr server, or path to Solr directory").required(true);
        index.addArgument("-d", "--db").type(String.class).dest("solr_url_or_path").help("URL of Solr server, or path to Solr directory").required(true);

        //add version
        parser.addArgument("--version").action(Arguments.version());
        parser.addArgument("--citation").action(Arguments.storeTrue());

	    //input file types
		parser.addArgument("--in_format").type(String.class).setDefault("fasta").choices(informats);

        //add options
        parser.addArgument("-o", "--out").type(Arguments.fileType()).dest("output_file").help("path to output directory");

        //add flags
        parser.addArgument("--parse_deflines").help("derive metadata from FASTA deflines and comments").action(Arguments.storeTrue());;

        
        //add unused flags
       
        unused.addArgument("--html").help("unused").action(Arguments.storeTrue());
        unused.addArgument("--lcase_masking").help("unused").action(Arguments.storeTrue());
        unused.addArgument("--remote").help("unused").action(Arguments.storeTrue());
        unused.addArgument("--show_gis").help("unused").action(Arguments.storeTrue());
        unused.addArgument("--ungapped").help("unused").action(Arguments.storeTrue());
        unused.addArgument("--use_sw_tback").help("unused").action(Arguments.storeTrue());

        //add unused options
        unused.addArgument("--import_search_strategy").type(String.class).dest("filename").help("unused").nargs(1);
        unused.addArgument("--export_search_strategy").type(String.class).dest("filename").help("unused").nargs(1);
        unused.addArgument("--task").type(String.class).dest("task_name").help("unused").nargs(1);    
        unused.addArgument("--dbsize").type(Integer.class).dest("num_letters").help("unused").nargs(1);
        unused.addArgument("--gilist").type(String.class).dest("filename").help("unused").nargs(1);
        unused.addArgument("--seqidlist").type(String.class).dest("filename").help("unused").nargs(1);
        unused.addArgument("--negative_gilist").type(String.class).dest("filename").help("unused").nargs(1);
        unused.addArgument("--entrez_query").type(String.class).dest("entrez_query").help("unused").nargs(1);
        unused.addArgument("--db_soft_mask").type(String.class).dest("filtering_algorithm").help("unused").nargs(1);
        unused.addArgument("--db_hard_mask").type(String.class).dest("filtering_algorithm").help("unused").nargs(1);
        unused.addArgument("--subject").type(String.class).dest("subject_input_file").help("unused").nargs(1);
        unused.addArgument("--subject_loc").type(String.class).dest("range").help("unused").nargs(1); 
        unused.addArgument("--evalue").type(String.class).dest("evalue").help("unused").nargs(1);
        unused.addArgument("--word_size").type(Integer.class).help("unused").nargs(1);
        unused.addArgument("--gapopen").type(String.class).dest("open_penalty").help("unused").nargs(1);
        unused.addArgument("--gapextend").type(String.class).dest("extend_penalty").help("unused").nargs(1);
        unused.addArgument("--qcov_hsp_perc").type(Float.class).help("unused").nargs(1);
        unused.addArgument("--max_hsps").type(Integer.class).help("unused").nargs(1);
        unused.addArgument("--xdrop_ungap").type(Float.class).help("unused").nargs(1);
        unused.addArgument("--xdrop_gap").type(Float.class).help("unused").nargs(1);
        unused.addArgument("--xdrop_gap_final").type(Float.class).help("unused").nargs(1);
        unused.addArgument("--searchsp").type(Integer.class).help("unused").nargs(1);
        unused.addArgument("--sum_stats").type(String.class).dest("bool").help("unused").nargs(1);
        unused.addArgument("--seg").type(String.class).dest("SEG_options").help("unused").nargs(1);
        unused.addArgument("--soft_masking").type(String.class).dest("soft_masking").help("unused").nargs(1);
        unused.addArgument("--matrix").type(String.class).dest("matrix_name").help("unused").nargs(1);
        unused.addArgument("--threshold").type(Float.class).help("unused").nargs(1);
        unused.addArgument("--culling_limit").type(Integer.class).help("unused").nargs(1);
        unused.addArgument("--best_hit_overhang").type(Float.class).help("unused").nargs(1);
        unused.addArgument("--best_hit_score_edge").type(Float.class).help("unused").nargs(1);
        unused.addArgument("--window_size").type(Integer.class).help("unused").nargs(1);
        unused.addArgument("--query_loc").type(String.class).dest("range").help("unused").nargs(1);
        unused.addArgument("--num_descriptions").type(Integer.class).help("unused").nargs(1);
        unused.addArgument("--line_length").type(String.class).dest("line_length").help("unused").nargs(1);
        unused.addArgument("--max_target_seqs").type(String.class).dest("num_sequences").help("unused").nargs(1);
        unused.addArgument("--num_threads").type(Integer.class).help("unused").nargs(1);
        unused.addArgument("--comp_based_stats").type(String.class).dest("compo").help("unused").nargs(1);
		return parser;
	}

    public static SolrServer getSolrServer(){
        return solrServer;
    }

	public static String getVersion(){
		return version;
	}

	public static String getVersionSolr(){
		return versionSolr;
	}

    
    public static void handleCommand(Namespace space) throws URISyntaxException {
    	//set up server
    	String solrString = space.getString("solr_url_or_path");
    	URI solrUri = new URI(solrString);
    	if (solrUri.isAbsolute()){
    		//solr server is remote, over http
    		HttpSolrServer httpSolrServer = new HttpSolrServer(solrString);
    		httpSolrServer.setAllowCompression(true);
    		solrServer = httpSolrServer;
    	} else {
    		//solr server is local
    		CoreContainer container = new CoreContainer(solrString);
            container.load();
           	solrServer = new EmbeddedSolrServer(container, COLLECTION);

			SystemInfoHandler sih = new SystemInfoHandler(container);
			versionSolr = sih.getVersion();
    	}

    	//handle outfmt if present
    	List<String> outputFields = null;
    	Integer outputCode = Output.TABULAR_WITH_COMMENT_LINES;
    	if (space.get("format") != null){
    		outputFields = space.getList("format");
    		outputCode = Integer.parseInt(outputFields.remove(0));
    	}


    	//handle streams
    	Writer outstream = new PrintWriter(System.out);
    	Map<String, ProteinSequence> queryFasta;

    	if (space.get("out") != null){
    		try {
				outstream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream((File) space.get("output_file"))));
			} catch (FileNotFoundException e) {
				System.out.println("Output file '" + space.getString("output_file") + "' not found.");
				System.exit(1);
			}
    	}


    	Output outputter = new Output(outstream, outputCode, outputFields);

		String informat = space.get("input_format");
		List<Map<String, String>> inputSequences = new ArrayList<>();

		Function<File,Stream<Map<String, String>>> getSequences;

		if (informat == "fasta") {
			Function<Map.Entry<String, ProteinSequence>, Map<String, String>> extractSequence = (Map.Entry<String, ProteinSequence> seq) ->
					new HashMap<String, String>() {
						{
							put("header", seq.getValue().getOriginalHeader());
							put("sequence", seq.getValue().getSequenceAsString() );
						}
					};
			Function<File, Set<Map.Entry<String, ProteinSequence>>> getFasta = (Errors.rethrow().wrap((File f) -> FastaReaderHelper.readFastaProteinSequence(f))).andThen(Map::entrySet);
			getSequences = 	(File f) ->
							         getFasta.apply(f)
									.stream()
				                    .map( extractSequence);
									//.collect(Collectors.toList());
		}

		else if (informat == "json") {
			//StreamSupport.stream(jn.spliterator(), false /* or whatever */);


			//	Function<File, Map<String, Object>[]> jsonToMap = Errors.rethrow().wrap((File j) -> new ObjectMapper().readValue(j,  Map[].class));
			//Function<File, Stream<Map<String, Object>>>
					getSequences = Errors.rethrow().wrap((File j) ->
				StreamSupport.stream( (new ObjectMapper().readValue(j, JsonNode.class)).spliterator(), false)
					.map((jn) -> new ObjectMapper().convertValue(jn, Map.class))); //new TypeReference<HashMap<String, Object>>() { });
		}
		else throw new NotImplementedException();//"input format " + informat + " not supported.");

		SeqrController control = new SeqrController(solrServer);
		//Function indexEntry = ((SolrInputDocument e) -> solrServer.add(e)).compose(SolrInputDocument::new);

	    String cmd = space.getString("command")	;
		Function<Map, UpdateResponse> indexEntry = Errors.rethrow().wrap((Map m) -> (
				solrServer.add(
						new SolrInputDocument((Map<String, SolrInputField>) m))));

		List<File> inFiles = space.getList("input_files");
		List<Stream<UpdateResponse>> resullts;
		if (cmd == INDEX) {
			inFiles.stream()
					.flatMap(getSequences.andThen((s) -> s.map(indexEntry)))
					.forEach(System.out::println);
			quit(0);
		}


    	if (space.get("input_files") != null){
    		for (File f : l){
    			try {
					inputFastas.add(FastaReaderHelper.readFastaProteinSequence(f));
				} catch (IOException e) {
					System.out.println("File '" + f.toString() + "' for indexing not found.");
					System.exit(1);
				}
    		}
    	}
    	if (space.get("input_file") != null){
    		try {
				queryFasta = DNASequenceStreamMap.maybeConvert((File) space.get("input_file"), (space.getBoolean("is_dna")));
                inputFastas.add(queryFasta);
			} catch (IOException ee) {
				System.out.println("Query file '" + space.getString("input_file") + "' not found.");
				System.exit(1);
			}
    	} else {
    		try {
    			queryFasta = FastaReaderHelper.readFastaProteinSequence(System.in);
    			inputFastas.add(queryFasta);
    		} catch (IOException e){
    			throw new RuntimeException(e);
    		}
    	}

		//TODO: implement smarter queries with more specific parameters
//    	String solrQuery = "";
//    	if (space.get("solr_query") != null){
//    		solrQuery = space.getString("solr_query");
//    	}



			if (informat == FASTA) {
				for (Map<String, ProteinSequence> fasta : inputFastas) {
					for (Map.Entry<String, ProteinSequence> contig : fasta.entrySet()) {
						ProteinSequence seq = contig.getValue();
						String protSeq = seq.getSequenceAsString();
						String cmd = space.getString("command");
						if (cmd == SEARCH) {
							try {
								SolrDocumentList solrDocList = control.search(protSeq);
								outputter.setTotalHits(solrDocList.size());
								for (SolrDocument doc : solrDocList) {
									outputter.write(doc);
								}
								outstream.flush();
							} catch (SolrServerException e) {
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ParserConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (TransformerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (cmd == INDEX) {
							String name = contig.getKey();
							control.index(name, protSeq);
						} else quit(1);

						solrServer.shutdown();

					}
				}
			}
		}


			private static void quit ( int exitcode){
				solrServer.shutdown();
				System.exit(exitcode);
			}

		}
