package gov.nih.nlm.ncbi.seqr;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
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
import java.util.Collection;
import java.util.function.Function;
import com.google.common.collect.ImmutableMap;
import org.apache.solr.core.CoreContainer;

/**
 * Created by michaelpanciera on 8/4/15.
 */
public class SeqrController {

    private final EmbeddedSolrServer server;
    private static final int DEFAULT_ROWS = 100;

    public SeqrController(EmbeddedSolrServer server) {
        this.server = server;
    }

    public QueryResponse makeQuery(String q) throws Exception {
        return makeQuery(q, DEFAULT_ROWS);
    }


//    public QueryResponse execQuery(String queryString) throws Exception {
////        Field f = server.getClass().getDeclaredField("_parser"); //NoSuchFieldException
////        f.setAccessible(true)q;
////        ResponseParser iWantThis = (ResponseParser) f.get(server); //IllegalAccessException
////        f.set(server, new NoOpResponseParser());
//      //server.setParser(new NoOpResponseParser());
//        return server.query(makeQuery(queryString)); //.getResults();
//    }

    public QueryResponse makeQuery(String solrQuery, int pageNum, int numItemsPerPage) throws SolrServerException {
        SolrQuery query = new SolrQuery();
        // should also set MM field
        query.setQuery(solrQuery);
        query.setStart((pageNum - 1) * numItemsPerPage);
        query.setRows(numItemsPerPage);
        return server.query(query);
    }

    public QueryResponse makeQuery(String solrQuery, int numResults) throws SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery(solrQuery);
        query.setRows(numResults);
        //optionally set pagination
        return server.query(query);
    }

    public ModifiableSolrParams queryFromFasta(String fastaPath) {
        return null;
    }

    public Collection<File> getJSON(String dir) {
        return FileUtils.listFiles(new File(dir),
                FileFilterUtils.suffixFileFilter(".json"),
                /*TrueFileFilter.INSTANCE,*/ TrueFileFilter.INSTANCE);
    }

    public boolean loadJSONDir(String dir) throws SolrServerException, InterruptedException, IOException {
        return loadJSON(getJSON(dir));
    }

    public boolean loadJSON(Collection<File> jsonFiles) throws IOException, SolrServerException, InterruptedException {
        LoadLargeFile2SolrServer fileLoader = new LoadLargeFile2SolrServer(server);
        for (File file : jsonFiles) {
            String name = file.getName();
            System.out.println("loading " + name + "....");
            fileLoader.loadFile(file);
        }
        server.commit();
        return true;
    }

    public String sequenceQueryFromInts(List<Integer> ints) {
        return "matchstring:" + ints;

    }

    public boolean index(List<File> fastas) {
        return false;
    }
}
