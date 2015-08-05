package gov.nih.nlm.ncbi.seqr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;


/**
 * for testing only
 */

public class SeqrApp {


    private final static Logger logger = LoggerFactory.getLogger(SeqrApp.class);


    public static void main(String[] args) throws Exception {
        String solrDir = "testdata/solr";
        final CoreContainer container = new CoreContainer(solrDir);
        container.load();
        final EmbeddedSolrServer server = new EmbeddedSolrServer(container, "collection1");
        final SeqrController controller = new SeqrController(server);
        QueryResponse queryResponse = controller.makeQuery("*:*");
        for (SolrDocument document : queryResponse.getResults()) {
            System.out.println(document);
        }
        server.shutdown();
        container.shutdown();
    }
}
