package gov.nih.nlm.ncbi.seqr;

import gov.nih.nlm.ncbi.seqr.solr.SeqrController;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;

//import org.codehaus.jackson.JsonNode;


import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

import java.io.IOException;

public class TestMain {
    private SeqrController control;
    EmbeddedSolrServer solrServer;

    @Before
    public void setUp() throws SolrServerException, InterruptedException, IOException {
        String solrString = "testdata/solr";
        CoreContainer container = new CoreContainer(solrString);
        container.load();
        solrServer = new EmbeddedSolrServer(container, "sequence");
        control = new SeqrController(solrServer);
        control.loadJSONDir("testdata/data");
    }

    @Test
    public void testSolrSimple() throws Exception {


        String seq = "AGSYLLEELFEGHLEKECWEEICVYEEAREVFEDDETTDEFWRTYMGGSPCASQPCLNNGSCQDSIRGYACTCAPGYEGPNCAFAESECHPLRLDGCQHFCYPGPESYTCSCARGHKLGQDRRSCLPHDRCACGTLGPECCQRPQGSQQNLLPFPWQVKLTNSEGKDFCGGVLIQDNFVLTTATCSLLYANISVKTRSHFRLHVRGVHVHTRFEADTGHNDVALLDLARPVRCPDAGRPVCTADADFADSVLLPQPGVLGGWTLRGREMVPLRLRVTHVEPAECGRALNATVTTRTSCERGAAAGAARWVAGGAVVREHRGAWFLTGLLGAAPPEGPGPLLLIKVPRYALWLRQVTQQPSRASPRGDRGQGRDGEPVPGDRGGRWAPTALPPGPLV";
        SolrDocumentList results = control.search(seq);
        for (SolrDocument document : results) {
            System.out.println(document);
        }
        Assert.assertEquals(1, results.size());
        solrServer.shutdown();
    }
}