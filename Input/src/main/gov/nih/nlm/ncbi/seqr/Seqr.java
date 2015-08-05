package gov.nih.nlm.ncbi.seqr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.core.CoreContainer;

public class Seqr {

    private static SolrServer solrServer;

    
    public static void main(final String[] args){

        ArgumentParser parser = buildParser();

        try {
            Namespace space = parser.parseArgs(args);
            System.out.println(space);
            handleCommand(space);
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
                .version("${prog} 0.0.1a");

        ArgumentGroup unused = parser.addArgumentGroup("unused").description("Unused compatibility arguments from BLASTP");
        //parser.addArgument("command").type(String.class).dest("command").help("Non-optional command").choices("search", "index", "load").required(true);
        Subparsers subprsrs = parser.addSubparsers().description("SEQR commands").metavar("COMMAND");
        Subparser search = subprsrs.addParser("search").help("look for something");
        Subparser index = subprsrs.addParser("index").help("create an index");
        
        //add options for search
        search.addArgument("query_file").type(Arguments.fileType().acceptSystemIn().verifyCanRead()).dest("input_file").help("query file for input").required(true);
        search.addArgument("--solr_query").type(String.class).help("filtering query in Solr query language");
        search.addArgument("--index_file").type(Arguments.fileType().verifyCanRead()).help("pre-calculated index file");
        search.addArgument("--num_alignments").type(Integer.class).help("number of results to return");
        search.addArgument("--start_alignments").type(Integer.class).help("begin output at the Nth result").metavar("N");
        
        //add special outformat parser
        search.addArgument("--outfmt").type(String.class).dest("format").help("options for formatted output").nargs("+");
        
        //add required, options for index
        index.addArgument("input_files").type(Arguments.fileType().acceptSystemIn().verifyCanRead()).nargs("+").required(true);
        
        parser.addArgument("-d", "--db").type(String.class).dest("solr_url_or_path").help("URL of Solr server, or path to Solr directory").required(true);
        
        //add version
        parser.addArgument("--version").action(Arguments.version());
        parser.addArgument("--citation").action(Arguments.storeTrue());
        
        
        
        //add options
        parser.addArgument("-o", "--out").type(Arguments.fileType()).dest("output_file").help("path to output directory");
        
        //add flags
        parser.addArgument("--parse_deflines").help("derive metadata from FASTA deflines and comments").action(Arguments.storeTrue());;
        
        
        
        
        
        
        //add unused flags
       
        unused.addArgument("--html").help("unused").action(Arguments.storeTrue());
        unused.addArgument("--lcase_masking").help("unused").action(Arguments.storeTrue());;
        unused.addArgument("--remote").help("unused").action(Arguments.storeTrue());;
        unused.addArgument("--show_gis").help("unused").action(Arguments.storeTrue());;
        unused.addArgument("--ungapped").help("unused").action(Arguments.storeTrue());;
        unused.addArgument("--use_sw_tback").help("unused").action(Arguments.storeTrue());;

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

    
    public static void handleCommand(Namespace space) throws URISyntaxException{
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
           	solrServer = new EmbeddedSolrServer(container, "Collection1");
    	}
    	
    	//handle outfmt if present
    	List<String> outputFields;
    	Integer outputCode;
    	if (space.get("format") != null){
    		outputFields = space.getList("format");
    		outputCode = Integer.parseInt(outputFields.remove(0));
    	}
    	
    	//handle streams
    	Writer outstream;
    	List<Reader> inputFastas = new ArrayList<Reader>();
    	Reader queryFasta;
    	if (space.get("out") != null){
    		try {
				outstream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream((File) space.get("output_file"))));
			} catch (FileNotFoundException e) {
				System.out.println("Output file '" + space.getString("output_file") + "' not found.");
				System.exit(1);
			}
    	}
    	if (space.get("input_files") != null){
    		List<File> l = space.getList("input_files");
    		for (File f : l){
    			try {
					inputFastas.add(new BufferedReader(new InputStreamReader(new FileInputStream(f))));
				} catch (FileNotFoundException e) {
					System.out.println("File '" + f.toString() + "' for indexing not found.");
					System.exit(1);
				}
    		}
    	}
    	if (space.get("input_file") != null){
    		try {
				queryFasta = new BufferedReader(new InputStreamReader(new FileInputStream((File) space.get("input_file"))));
			} catch (FileNotFoundException e) {
				System.out.println("Query file '" + space.getString("input_file") + "' not found.");
				System.exit(1);
			}
    	}
    	
    	//pass on to appropriate subcommand
    	System.out.println(solrServer);
    	System.out.println("finished");
    }



}