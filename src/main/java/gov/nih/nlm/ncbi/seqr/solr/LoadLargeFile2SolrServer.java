package gov.nih.nlm.ncbi.seqr.solr;

/**
 * Created by michaelpanciera on 8/4/15.
 */


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadLargeFile2SolrServer {

    private final static Logger logger = LoggerFactory.getLogger(LoadLargeFile2SolrServer.class);
    private SolrServer server;
    private int counter = 0;

    public LoadLargeFile2SolrServer(SolrServer server) {
        this.server = server;
    }

    class CallBackImpl implements JsonStreamParser.Callback {
        /**
         * This function convert Jackson JSON parser's JsonNode into SolrInputDocument
         *
         * @param node - JsonNode
         * @return doc -SolrInputDocument
         */
        public SolrInputDocument JsonToken2SolrInputDocument(JsonNode node) throws ParseException {
            SolrInputDocument doc = new SolrInputDocument();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Iterator<String> itr = node.getFieldNames();
            while (itr.hasNext()) {
                String filedName = itr.next();
                if (!node.get(filedName).isNull()) {
                    if (filedName.contains("date")) {
                        doc.addField(filedName, format.parse(node.get(filedName).getTextValue()));
                    } else if (node.get(filedName).isTextual()) {
                        doc.addField(filedName, node.get(filedName).getTextValue());
                    } else if (node.get(filedName).isNumber()) {
                        doc.addField(filedName, node.get(filedName).getNumberValue());
                    } else if (node.get(filedName).isArray()) {
                        for (final JsonNode objNode : node.get(filedName)) {
                            // this works?
                            doc.addField(filedName, objNode.getTextValue());
                        }
                    } else {
                        doc.addField(filedName, node.get(filedName).getTextValue());
                    }
                }
            }
            return doc;
        }

        //@Override
        public void processSingleJSONRecord(JsonNode node) {
            try {
                if (server != null) {
                    server.add(this.JsonToken2SolrInputDocument(node));
                    counter++;
                    if (counter % 100000 == 0) {
                        server.commit();
                    }
                } else {
                    System.out.println(this.JsonToken2SolrInputDocument(node));
                }
            } catch (Exception e) {
                //logger.error("parsing error " + e.getStackTrace());
                e.printStackTrace();;
            }
        }
    }

    public boolean loadFile(File jsonFile) throws IOException {
        if (!jsonFile.exists()) {
            logger.warn(jsonFile.getName() + " do not exists, skip loading");
            return false;
        }
        JsonStreamParser parser = new JsonStreamParser(jsonFile);
        parser.processing(new CallBackImpl());
        try {
            if (server != null) {
                server.commit();
            }
        } catch (SolrServerException e) {
            logger.warn("solr server exception:" + e.getStackTrace());
        }
        return true;
    }

    public boolean loadFile(String jsonFileName) throws IOException {
        return loadFile(new File(jsonFileName));
    }
}
