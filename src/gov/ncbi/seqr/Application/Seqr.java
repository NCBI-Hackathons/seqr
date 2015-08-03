package gov.ncbi.seqr.Application;

import net.sourceforge.argparse4j.*;

public class Seqr extends Object{

    private static SolrServer solrServer;

    private static Path outputPath;


    public static void main(String[] args){

        ArgumentParser parser = ArgumentParsers.newArgumentParser("seqr")
                .defaultHelp(true)
                .description("SEQR, now for shells.")
                .version("0.0.1a");

        //add help

        //add version


        //add flags
        parser.addArgument("html").type(Boolean.class).help("eventually, a description");
        parser.addArgument("lcase_masking").type(Boolean.class).help("eventually, a description");
        parser.addArgument("parse_deflines").type(Boolean.class).help("eventually, a description");
        parser.addArgument("remote").type(Boolean.class).help("eventually, a description");
        parser.addArgument("show_gis").type(Boolean.class).help("eventually, a description");
        parser.addArgument("ungapped").type(Boolean.class).help("eventually, a description");
        parser.addArgument("use_sw_tback").type(Boolean.class).help("eventually, a description");
        parser.addArgument("version").type(Boolean.class).help("eventually, a description");


        //add options
        parser.addArgument("import_search_strategy").type(String.class).dest("filename").help("eventually, a description");
        parser.addArgument("export_search_strategy").type(String.class).dest("filename").help("eventually, a description");
        parser.addArgument("task").type(String.class).dest("task_name").help("eventually, a description");
        parser.addArgument("db").type(String.class).dest("database_name").help("eventually, a description");
        parser.addArgument("dbsize").type(String.class).dest("num_letters").help("eventually, a description");
        parser.addArgument("gilist").type(String.class).dest("filename").help("eventually, a description");
        parser.addArgument("seqidlist").type(String.class).dest("filename").help("eventually, a description");
        parser.addArgument("negative_gilist").type(String.class).dest("filename").help("eventually, a description");
        parser.addArgument("entrez_query").type(String.class).dest("entrez_query").help("eventually, a description");
        parser.addArgument("db_soft_mask").type(String.class).dest("filtering_algorithm").help("eventually, a description");
        parser.addArgument("db_hard_mask").type(String.class).dest("filtering_algorithm").help("eventually, a description");
        parser.addArgument("subject").type(String.class).dest("subject_input_file").help("eventually, a description");
        parser.addArgument("subject_loc").type(String.class).dest("range").help("eventually, a description");
        parser.addArgument("query").type(String.class).dest("input_file").help("eventually, a description");
        parser.addArgument("out").type(String.class).dest("output_file").help("eventually, a description");
        parser.addArgument("evalue").type(String.class).dest("evalue").help("eventually, a description");
        parser.addArgument("word_size").type(Integer.class).help("eventually, a description");
        parser.addArgument("gapopen").type(String.class).dest("open_penalty").help("eventually, a description");
        parser.addArgument("gapextend").type(String.class).dest("extend_penalty").help("eventually, a description");
        parser.addArgument("qcov_hsp_perc").type(Float.class).help("eventually, a description");
        parser.addArgument("max_hsps").type(Integer.class).help("eventually, a description");
        parser.addArgument("xdrop_ungap").type(Float.class).help("eventually, a description");
        parser.addArgument("xdrop_gap").type(Float.class).help("eventually, a description");
        parser.addArgument("xdrop_gap_final").type(Float.class).help("eventually, a description");
        parser.addArgument("searchsp").type(Integer.class).help("eventually, a description");
        parser.addArgument("sum_stats").type(String.class).dest("bool").help("eventually, a description");
        parser.addArgument("seg").type(String.class).dest("SEG_options").help("eventually, a description");
        parser.addArgument("soft_masking").type(String.class).dest("soft_masking").help("eventually, a description");
        parser.addArgument("matrix").type(String.class).dest("matrix_name").help("eventually, a description");
        parser.addArgument("threshold").type(Float.class).help("eventually, a description");
        parser.addArgument("culling_limit").type(Integer.class).help("eventually, a description");
        parser.addArgument("best_hit_overhang").type(Float.class).help("eventually, a description");
        parser.addArgument("best_hit_score_edge").type(Float.class).help("eventually, a description");
        parser.addArgument("window_size").type(Integer.class).help("eventually, a description");
        parser.addArgument("query_loc").type(String.class).dest("range").help("eventually, a description");
        parser.addArgument("outfmt").type(String.class).dest("format").help("eventually, a description");
        parser.addArgument("num_descriptions").type(Integer.class).help("eventually, a description");
        parser.addArgument("num_alignments").type(Integer.class).help("eventually, a description");
        parser.addArgument("line_length").type(String.class).dest("line_length").help("eventually, a description");
        parser.addArgument("max_target_seqs").type(String.class).dest("num_sequences").help("eventually, a description");
        parser.addArgument("num_threads").type(Integer.class).help("eventually, a description");
        parser.addArgument("comp_based_stats").type(String.class).dest("compo").help("eventually, a description");

        Namespace space = parser.parseArgs(args);

    }

    public static SolrServer getSolrServer(){
        return solrServer;
    }

    public static Path getOutputPath(){
        return outputPath;
    }



}