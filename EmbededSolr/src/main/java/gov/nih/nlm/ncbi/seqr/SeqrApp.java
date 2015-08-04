package gov.nih.nlm.ncbi.seqr;

/**
 * Created by hanl on 4/16/2015.
 */

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

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        EmbeddedSolrServer server = new EmbeddedSolrServer(container, "collection1");
        Collection<File> files = FileUtils.listFiles(new File("testdata/data"),
                FileFilterUtils.suffixFileFilter(".json"),
                /*TrueFileFilter.INSTANCE,*/ TrueFileFilter.INSTANCE);
//        String[] exts = {"json"};
//        Collection<File> files = FileUtils.listFiles(new File("/home"), exts, true);

        for (File file : files) {
            String name = file.getName();
            System.out.println("loading " + name + "....");
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
///
//public class SeqrApp {
//    // private final static Logger logger = LoggerFactory.getLogger(SeqrApp.class);
//
//    private static String solrHome = (System.getenv().get("USER") == "UBUNTU") ? "/home/ubuntu/solr-home" : "/opt/solr";
//    public static void main(String[] args) throws Exception {
//
//
//
//        System.setProperty("solr.solr.home", solrHome);
//        String solrDir = "testdata/solr";
//        CoreContainer container = new CoreContainer(solrDir);
//        container.load();
//        EmbeddedSolrServer server = new EmbeddedSolrServer(container, "");
//
//        for(int i=0;i<10;++i) {
//            SolrInputDocument doc = new SolrInputDocument();
//            doc.addField("cat", "book");
//            doc.addField("id", "book-" + i);
//            doc.addField("name", "The Legend of the Hobbit part " + i);
//            server.add(doc);
//            if(i%100==0) server.commit();  // periodically flush
//        }
//        server.commit();
//
//        SolrQuery query = new SolrQuery();
//        query.setQuery("sony digital camera");
//        query.addFilterQuery("cat:electronics","store:amazon.com");
//        query.setFields("id","price","merchant","cat","store");
//        query.setStart(0);
//        query.set("defType", "edismax");


        //Collection<File> files = FileUtils.listFiles(new File("testdata/data"), FileFilterUtils.suffixFileFilter(".json"), /*TrueFileFilter.INSTANCE,*/ TrueFileFilter.INSTANCE);
