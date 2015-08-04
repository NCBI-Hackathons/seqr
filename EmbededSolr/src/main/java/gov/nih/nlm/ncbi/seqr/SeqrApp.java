package gov.nih.nlm.ncbi.seqr;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
//import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;



public class SeqrApp {


    private final static Logger logger = LoggerFactory.getLogger(SeqrApp.class);

    public static void main(String[] args) throws Exception {
        String solrDir = "testdata/solr";
        CoreContainer container = new CoreContainer(solrDir);
        container.load();

        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        EmbeddedSolrServer server = new EmbeddedSolrServer(container, "collection1");
        Collection<File> files = FileUtils.listFiles(new File("testdata/data"),
                FileFilterUtils.suffixFileFilter(".json"),
                /*TrueFileFilter.INSTANCE,*/ TrueFileFilter.INSTANCE);
//        String[] exts = {"json"};
//        Collection<File> files = FileUtils.listFiles(new File("/home"), exts, true);


        LoadLargeFile2SolrServer fileLoader = new LoadLargeFile2SolrServer(server);
        for (File file : files) {
            String name = file.getName();
            System.out.println("loading " + name + "....");
            fileLoader.loadFile(file);
            // Let's do the file loading;
        }
        server.commit();
        Thread.sleep(5000);
        container.shutdown();
        server.shutdown();
        container = new CoreContainer(solrDir);
        container.load();
        server = new EmbeddedSolrServer(container, "collection1");
        ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.add(CommonParams.Q, "*:*");
        QueryResponse queryResponse = server.query(solrParams);
        for (SolrDocument document : queryResponse.getResults()) {
            System.out.println(document);
        }
        server.shutdown();
    }
}
