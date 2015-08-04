package gov.nih.nlm.ncbi.seqr;

import java.net.URI;
import java.net.URISyntaxException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.core.CoreContainer;

public class Seqr {

    private static SolrServer solrServer;


    private static String outputPath;

    
    public static void main(final String[] args){

        ArgumentParser parser = ArgumentParsers.newArgumentParser("seqr")
                .defaultHelp(true)
                .description("SEQR, now for shells.")
                .version("${prog} 0.0.1a");

        //parser.addArgument("command").type(String.class).dest("command").help("Non-optional command").choices("search", "index", "load").required(true);
        Subparsers subprsrs = parser.addSubparsers().description("SEQR commands").metavar("COMMAND");
        subprsrs.addParser("search").help("look for something");
        subprsrs.addParser("index").help("create an index");
        subprsrs.addParser("load").help("load something");
        
        parser.addArgument("-d", "--db").type(String.class).dest("solr_url_or_path").help("URL of Solr server, or path to Solr directory").required(true);
        
        //add version
        parser.addArgument("--version").action(Arguments.version());
        
        //add options
        parser.addArgument("--outfmt").type(String.class).dest("format").help("options for formatted output");
        
        parser.addArgument("-o", "--out").type(String.class).dest("output_file").help("path to output directory");
        parser.addArgument("--query").type(String.class).dest("input_file").help("query file for input");
        //add flags
        parser.addArgument("--parse_deflines").help("derive metadata from FASTA deflines and comments").action(Arguments.storeTrue());;
        
        
        
        
        
        
        //add unused flags
        ArgumentGroup unused = parser.addArgumentGroup("unused").description("Unused compatibility arguments from BLASTP");
        unused.addArgument("--html").help("unused").action(Arguments.storeTrue());
        unused.addArgument("--lcase_masking").help("unused").action(Arguments.storeTrue());;
        unused.addArgument("--remote").help("unused").action(Arguments.storeTrue());;
        unused.addArgument("--show_gis").help("unused").action(Arguments.storeTrue());;
        unused.addArgument("--ungapped").help("unused").action(Arguments.storeTrue());;
        unused.addArgument("--use_sw_tback").help("unused").action(Arguments.storeTrue());;

        //add unused options
        unused.addArgument("--import_search_strategy").type(String.class).dest("filename").help("unused");
        unused.addArgument("--export_search_strategy").type(String.class).dest("filename").help("unused");
        unused.addArgument("--task").type(String.class).dest("task_name").help("unused");    
        unused.addArgument("--dbsize").type(String.class).dest("num_letters").help("unused");
        unused.addArgument("--gilist").type(String.class).dest("filename").help("unused");
        unused.addArgument("--seqidlist").type(String.class).dest("filename").help("unused");
        unused.addArgument("--negative_gilist").type(String.class).dest("filename").help("unused");
        unused.addArgument("--entrez_query").type(String.class).dest("entrez_query").help("unused");
        unused.addArgument("--db_soft_mask").type(String.class).dest("filtering_algorithm").help("unused");
        unused.addArgument("--db_hard_mask").type(String.class).dest("filtering_algorithm").help("unused");
        unused.addArgument("--subject").type(String.class).dest("subject_input_file").help("unused");
        unused.addArgument("--subject_loc").type(String.class).dest("range").help("unused"); 
        unused.addArgument("--evalue").type(String.class).dest("evalue").help("unused");
        unused.addArgument("--word_size").type(Integer.class).help("unused");
        unused.addArgument("--gapopen").type(String.class).dest("open_penalty").help("unused");
        unused.addArgument("--gapextend").type(String.class).dest("extend_penalty").help("unused");
        unused.addArgument("--qcov_hsp_perc").type(Float.class).help("unused");
        unused.addArgument("--max_hsps").type(Integer.class).help("unused");
        unused.addArgument("--xdrop_ungap").type(Float.class).help("unused");
        unused.addArgument("--xdrop_gap").type(Float.class).help("unused");
        unused.addArgument("--xdrop_gap_final").type(Float.class).help("unused");
        unused.addArgument("--searchsp").type(Integer.class).help("unused");
        unused.addArgument("--sum_stats").type(String.class).dest("bool").help("unused");
        unused.addArgument("--seg").type(String.class).dest("SEG_options").help("unused");
        unused.addArgument("--soft_masking").type(String.class).dest("soft_masking").help("unused");
        unused.addArgument("--matrix").type(String.class).dest("matrix_name").help("unused");
        unused.addArgument("--threshold").type(Float.class).help("unused");
        unused.addArgument("--culling_limit").type(Integer.class).help("unused");
        unused.addArgument("--best_hit_overhang").type(Float.class).help("unused");
        unused.addArgument("--best_hit_score_edge").type(Float.class).help("unused");
        unused.addArgument("--window_size").type(Integer.class).help("unused");
        unused.addArgument("--query_loc").type(String.class).dest("range").help("unused");
        unused.addArgument("--num_descriptions").type(Integer.class).help("unused");
        unused.addArgument("--num_alignments").type(Integer.class).help("unused");
        unused.addArgument("--line_length").type(String.class).dest("line_length").help("unused");
        unused.addArgument("--max_target_seqs").type(String.class).dest("num_sequences").help("unused");
        unused.addArgument("--num_threads").type(Integer.class).help("unused");
        unused.addArgument("--comp_based_stats").type(String.class).dest("compo").help("unused");
        
        

        try {
            Namespace space = parser.parseArgs(args);
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

    public static SolrServer getSolrServer(){
        return solrServer;
    }

    public static String getOutputPath(){
        return outputPath;
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
           	solrServer = new EmbeddedSolrServer(container, "default_collection");
    	}
    	
    	//pass on to appropriate subcommand
    	System.out.println(solrServer);
    	System.out.println("finished");
    }



}