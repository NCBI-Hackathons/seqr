package gov.ncbi.seqr.Application;

import org.apache.commons.*;

public class Seqr extends Object{

    private static SolrServer solrServer;

    private static Path outputPath;


    public static void main(String[] args){

    }

    public static SolrServer getSolrServer(){
        return solrServer;
    }

    public static Path getOutputPath(){
        return outputPath;
    }



}