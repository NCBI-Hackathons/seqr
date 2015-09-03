package gov.nih.nlm.ncbi.seqr;

import com.diffplug.common.base.Throwing;

//import fj.data.Stream;
import java.util.stream.Stream;

import gov.nih.nlm.ncbi.seqr.nuc.DNASequenceStreamMap;
import gov.nih.nlm.ncbi.seqr.solr.JsonStreamParser;
import gov.nih.nlm.ncbi.seqr.solr.SeqrController;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
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

	private static final int COMMIT_PERIOD = 10000;
	private static SolrServer solrServer;

	private static String version = "0.0.1a";
	private static String versionSolr; // = "4";
    
    private static final String SEARCH = "search";
    private static final String INDEX = "index";
    private static final String COLLECTION = "sequence";


	private static final String FASTA = "fasta";
	private static final String JSON = "json";
	private static final String CSV = "CSV";

	static final String[] informats = {FASTA, JSON, CSV};
	private static CoreContainer container = null;

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
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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


	public static String getVersion(){
		return version;
	}

	public static String getVersionSolr(){
		return versionSolr;
	}

    
    public static void handleCommand(Namespace space) throws URISyntaxException, IOException, SolrServerException {
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
container = new CoreContainer(solrString);
            container.load();
           	solrServer = new EmbeddedSolrServer(container, COLLECTION);

			SystemInfoHandler sih = new SystemInfoHandler(container);
			versionSolr = sih.getVersion();
    	}


       final Map SCHEMA = new HashMap<String, Class>();
		SCHEMA.put("id", Integer.class);
		SCHEMA.put("defline", String.class);
		SCHEMA.put("sequence", String.class);
		//new SolrInputField

    	//handle streams

		final Errors.Rethrowing rethrow = Errors.rethrow();
		//TODO: parser.setDefault not working
		final String inFormat = (space.get("input_format") != null) ? space.get("input_format") : FASTA;
		Function<File,Stream<SolrInputDocument>> getSequences;

		if (inFormat == "fasta") {
			Function<Map.Entry<String, ProteinSequence>, SolrInputDocument> extractSequence = (Map.Entry<String, ProteinSequence> seq) ->
					//new HashMap<String, String>() {
						new SolrInputDocument() {
						{
							//TODO: At some point, figure out a way to add FASTA sequences that aren't in this format!
							//NOTE: "id" = GI Number defined by NCBI
							addField("id", seq.getKey().split("\\|")[1]); //GI number!
							addField("defline", seq.getValue().getOriginalHeader());
							addField("sequence", seq.getValue().getSequenceAsString() );
						}
					};
			getSequences =
					rethrow.wrap( (File f) ->
					FastaReaderHelper.readFastaProteinSequence(f)
					.entrySet()
					.stream()
				    .map(extractSequence)
					);
		}

		else if (inFormat == "json") {
			//StreamSupport.stream(jn.spliterator(), false /* or whatever */);
					getSequences = rethrow.wrap((File j) ->
				StreamSupport.stream( (new ObjectMapper().readValue(j, JsonNode.class)).spliterator(), false)
					.map((jn) -> new ObjectMapper().convertValue(jn, SolrInputDocument.class))); //new TypeReference<HashMap<String, Object>>() { });
		}

		else throw new NotImplementedException();//"input format " + informat + " not supported.");


		final SeqrController control = new SeqrController(solrServer);

		final List<File> inFiles =  (space.get("input_files") != null) ? space.getList("input_files") : Arrays.asList(new File[] {space.get("input_file")});

		final Stream<SolrInputDocument> docStream =
				                     inFiles.parallelStream() // will lthis cause lock problems?
                                             				.flatMap(getSequences);

		final String cmd = space.getString("command")	;
		if (cmd == INDEX) {

			int numAdded = 0;
			for (SolrInputDocument doc : (Iterable<SolrInputDocument>) docStream::iterator) {
				System.out.println(doc);
				UpdateResponse result = solrServer.add(doc);
				System.out.println(result);
				numAdded++;
				if (numAdded % COMMIT_PERIOD == 0) solrServer.commit();
			}
			solrServer.commit();
			quit(0);
		}

//			inputDocuments.zipIndex()
//					.map(p -> {
//								try {
//									if (p._2() % COMMIT_PERIOD == 0) solrServer.commit();
//
//									return solrServer.add(p._1());
//
//								} catch (Exception e) {
//									e.printStackTrace();
//								} return null;
//							}
//					)
//					.forEach(System.out::println);

		List<Map<String, ProteinSequence>> inputFastas = new ArrayList<>();
		if (space.get("input_files") != null){
    		for (File f : inFiles){
    			try {
					inputFastas.add(FastaReaderHelper.readFastaProteinSequence(f));
				} catch (IOException e) {
					System.out.println("File '" + f.toString() + "' for indexing not found.");
					quit(1);
				}
    		}
    	}
		Map<String, ProteinSequence> queryFasta;
    	if (space.get("input_file") != null){
    		try {
				queryFasta = DNASequenceStreamMap.maybeConvert((File) space.get("input_file"), space.getBoolean("is_dna"));
                inputFastas.add(queryFasta);
			} catch (IOException ee) {
				System.out.println("Query file '" + space.getString("input_file") + "' not found.");
				quit(1);
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

		Writer outstream = new PrintWriter(System.out);
		if (space.get("out") != null){
			try {
				outstream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream((File) space.get("output_file"))));
			} catch (FileNotFoundException e) {
				System.out.println("Output file '" + space.getString("output_file") + "' not found.");
				quit(1);
			}
		}

		//handle outfmt if present
		List<String> outputFields = null;
		Integer outputCode = Output.TABULAR_WITH_COMMENT_LINES;
		if (space.get("format") != null){
			outputFields = space.getList("format");
			outputCode = Integer.parseInt(outputFields.remove(0));
		}


		Output outputter = new Output(outstream, outputCode, outputFields); 

			if (inFormat == FASTA) {
				for (Map<String, ProteinSequence> fasta : inputFastas) {
					for (Map.Entry<String, ProteinSequence> contig : fasta.entrySet()) {
						ProteinSequence seq = contig.getValue();
						String protSeq = seq.getSequenceAsString();
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
				if (container != null) container.shutdown();
				System.exit(exitcode);
			}

		}

