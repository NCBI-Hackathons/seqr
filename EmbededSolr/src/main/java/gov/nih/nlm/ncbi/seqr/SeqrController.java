package gov.nih.nlm.ncbi.seqr;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import java.io.File;
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
/**
 * Created by michaelpanciera on 8/4/15.
 */
public class SeqrController {

    private static EmbeddedSolrServer server;

    public SeqrController(EmbeddedSolrServer server) { this.server = server; }

//    Function<Map, ModifiableSolrParams> makeQueryFromMap = map -> new ModifiableSolrParams(map);
//    Function<String, ModifiableSolrParams> makeQuery = makeQueryFromMap.compose(makeQueryMap);
//    Function<String, Map> makeQueryMap = s -> ImmutableMap.of( CommonParams.Q, s );
    //Function<ModifiableSolrParams, SolrDocumentList> execQuery = q -> server.query(q).getResults();

    public ModifiableSolrParams makeQuery(String q) {
        ModifiableSolrParams query = new ModifiableSolrParams();
        query.add(CommonParams.Q, q);
        return query;
    }


    public QueryResponse execQuery(String queryString) throws Exception {
//        Field f = server.getClass().getDeclaredField("_parser"); //NoSuchFieldException
//        f.setAccessible(true);
//        ResponseParser iWantThis = (ResponseParser) f.get(server); //IllegalAccessException
//        f.set(server, new NoOpResponseParser());
      //server.setParser(new NoOpResponseParser());
        return server.query(makeQuery(queryString)); //.getResults();
    }

    public QueryResponse makeQuery(String solrQuery, int pageNum, int numItemsPerPage) throws SolrServerException {
        SolrQuery query = new SolrQuery();
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

    public QueryResponse localSearch(String queryPath, List<String> outFields, String outFormat, String solrQuery) {
        return null;
    }

    public boolean index(List<File> fastas) {
        return false;
    }
    // series of function composition to get formatted output
}
//add options for search
//search.addArgument("query_file").type(Arguments.fileType().acceptSystemIn().verifyCanRead()).dest("input_file").help("query file for input").required(true);
//        search.addArgument("--solr_query").type(String.class).help("filtering query in Solr query language");
//        search.addArgument("--index_file").type(Arguments.fileType().verifyCanRead()).help("pre-calculated index file");
//        search.addArgument("--num_alignments").type(Integer.class).help("number of results to return");
//        search.addArgument("--start_alignments").type(Integer.class).help("begin output at the Nth result").metavar("N");
//
//        //add special outformat parser
        search.addArgument("--outfmt").type(String.class).dest("format").help("options for formatted output").nargs("+");
