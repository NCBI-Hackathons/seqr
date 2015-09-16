package gov.nih.nlm.ncbi.seqr.solr;

import org.codehaus.jackson.JsonNode;

import java.io.IOException;

/**
 * Created by yub5 on 9/16/15.
 */
public abstract class StreamParser {
    public interface Callback {
        void processSingleJSONRecord(JsonNode node);
    }

    public abstract void processing(Callback callback) throws IOException;

}
