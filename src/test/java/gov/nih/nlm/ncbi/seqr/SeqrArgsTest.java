package gov.nih.nlm.ncbi.seqr;

import gov.nih.nlm.ncbi.seqr.solr.FastaStreamParser;
import gov.nih.nlm.ncbi.seqr.solr.LoadLargeFile2SolrServer;
import gov.nih.nlm.ncbi.seqr.solr.SeqrController;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;

import java.io.File;

//import org.codehaus.jackson.JsonNode;


import org.junit.*;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SeqrArgsTest extends TestCase {
    private static SeqrController control;
    static EmbeddedSolrServer solrServer;
    static CoreContainer container;

    Lock sequential = new ReentrantLock();


    protected void setUp() throws Exception {
        super.setUp();
        sequential.lock();
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        sequential.unlock();

    }

    private static void setUpController() throws SolrServerException, InterruptedException, IOException {
        String solrString = "testdata/solr";
        container = new CoreContainer(solrString);
        container.load();
        solrServer = new EmbeddedSolrServer(container, "sequence");
        control = new SeqrController(solrServer);
    }

    public static void tearDownController() {
        solrServer.shutdown();
        container.shutdown();
    }

    public static void ClearData() throws SolrServerException, InterruptedException, IOException {
        solrServer.deleteByQuery("*:*");
    }

    public static void loadSampleData() throws SolrServerException, InterruptedException, IOException {
        ClearData();
        control.loadJSONDir("testdata/data");
    }

    @Test
    public void testFastaIndexingIntegration() throws SolrServerException, InterruptedException, IOException {
        // verify this is not yet in solr
        setUpController();
        ClearData();
        String seq = "MIKLFCVLAAFISINSACQSSHQQREEFTVATYHSSSICTTYCYSNCVVASQHKGLNVES" +
                "YTCDKPDPYGRETVCKCTLIKCHDI";
        SolrDocumentList results = control.search(seq);
        Assert.assertEquals(0, results.size());
        tearDownController();

        //and we now index the fasta

        String[] args = {"index", "testdata/data/short.fasta", "--db", "testdata/solr/", "--input_format", "fasta"};
        Seqr.main(args);

        //the record should be in solr
        setUpController();
        results = control.search(seq);
        Assert.assertEquals(1, results.size());
        tearDownController();
    }

    @Test
    public void testArgParser() {
        String[] args = {"search", "testdata/data/test.fasta", "--db", "testdata/solr/"};
        Seqr.main(args);
    }
}
