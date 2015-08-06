package gov.nih.nlm.ncbi.seqr.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

//import org.codehaus.jackson.JsonNode;



public class Test {



    public static void main(String[] args) throws Exception {
        String solrString = "testdata/solr";
        CoreContainer container = new CoreContainer(solrString);
        container.load();
        EmbeddedSolrServer solrServer = new EmbeddedSolrServer(container, "sequence");
        gov.nih.nlm.ncbi.seqr.SeqrController control = new gov.nih.nlm.ncbi.seqr.SeqrController(solrServer);


        control.loadJSONDir("testdata/data");
        String seq = "AGSYLLEELFEGHLEKECWEEICVYEEAREVFEDDETTDEFWRTYMGGSPCASQPCLNNGSCQDSIRGYACTCAPGYEGPNCAFAESECHPLRLDGCQHFCYPGPESYTCSCARGHKLGQDRRSCLPHDRCACGTLGPECCQRPQGSQQNLLPFPWQVKLTNSEGKDFCGGVLIQDNFVLTTATCSLLYANISVKTRSHFRLHVRGVHVHTRFEADTGHNDVALLDLARPVRCPDAGRPVCTADADFADSVLLPQPGVLGGWTLRGREMVPLRLRVTHVEPAECGRALNATVTTRTSCERGAAAGAARWVAGGAVVREHRGAWFLTGLLGAAPPEGPGPLLLIKVPRYALWLRQVTQQPSRASPRGDRGQGRDGEPVPGDRGGRWAPTALPPGPLV";
        SolrDocumentList results = control.search(seq);
        for (SolrDocument document : results) {
            System.out.println(document);
        }
        assert results.size() == 1;
        solrServer.shutdown();
    }
}