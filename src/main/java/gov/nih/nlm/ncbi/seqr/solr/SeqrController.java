package gov.nih.nlm.ncbi.seqr.solr;
import com.sun.org.apache.xpath.internal.operations.Mod;
import gov.nih.nlm.ncbi.seqr.solr.LoadLargeFile2SolrServer;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import org.apache.solr.core.CoreContainer;

/**
 * Created by michaelpanciera on 8/4/15.
 */
public class SeqrController {

    private final SolrServer server;
    private static final int DEFAULT_ROWS = 100;

    public SeqrController(SolrServer server) {
        this.server = server;
    }

    public QueryResponse makeQuery(String q) throws SolrServerException {
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
        String commaSeparatedNumbers = ints.stream()
                .map(i -> i.toString())
                .collect(Collectors.joining(" "));
        return "matchstring:" + "\"(" + ints + "\")";

    }

    /* API Functions */
    public SolrDocumentList search(List<Integer> rawSequenceInts, Integer page_num, Integer num_rows) throws SolrServerException {
        String q = sequenceQueryFromInts(rawSequenceInts);
        QueryResponse response = null;
        if (page_num != null && num_rows != null) {
            response = makeQuery(q, page_num, num_rows);
        } else if (num_rows != null) {
            response = makeQuery(q, num_rows);
        } else {
            response = makeQuery(q, DEFAULT_ROWS);
        }
        if (response != null) {
            return response.getResults();
        }
        return null;
    }


    public SolrDocumentList index(String proteinSequence) {
        return null;
    }

    public SolrDocumentList search(String seq) {
        String query = "sequence" + ":" + seq;
        try {
            return makeQuery(query).getResults();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
