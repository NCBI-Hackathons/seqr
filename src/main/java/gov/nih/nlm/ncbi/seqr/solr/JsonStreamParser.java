package gov.nih.nlm.ncbi.seqr.solr;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;

import java.io.File;
import java.io.IOException;

public class JsonStreamParser {
    private JsonFactory factory;
    private JsonParser parser;

    public interface Callback {
        void processSingleJSONRecord(JsonNode node);
    }

    public JsonStreamParser(File jsonFile) throws IOException {
        factory = new MappingJsonFactory();
        parser = factory.createJsonParser(jsonFile);
    }

    public JsonStreamParser(String jsonFileName) throws IOException {
        this(new File(jsonFileName));
    }

    public void processing(Callback callback) throws IOException {
        JsonToken current = parser.nextToken();

        /** the following read from a big json array **/
        if (current == JsonToken.START_ARRAY) {
            // For each of the records in the array
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                // read the record into a tree model,
                // this moves the parsing position to the end of it
                JsonNode node = parser.readValueAsTree();
                callback.processSingleJSONRecord(node);
            }
        }
    }
}


