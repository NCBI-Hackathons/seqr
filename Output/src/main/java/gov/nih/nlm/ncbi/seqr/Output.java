
package gov.nih.nlm.ncbi.seqr;

import org.apache.solr.common.SolrDocument;

import java.io.Writer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Output {

    private Writer writer;
    private String style;

    private void writeOut(String out) {
        try {
            writer.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeCsv(SolrDocument sd) {
        for(String field : sd.getFieldNames()) {
            writeOut(sd.getFieldValue(field).toString());
            writeOut(",");
        }
        writeOut(System.getProperty("line.separator"));
    }
    private void writeTab(SolrDocument sd) {
        for(String field : sd.getFieldNames()) {
            writeOut(sd.getFieldValue(field).toString());
            writeOut("\t");
        }
        writeOut(System.getProperty("line.separator"));
    }
    private void writeJson(SolrDocument sd) {
        JSONObject obj = new JSONObject();

        for(String field : sd.getFieldNames()) {
            obj.put(field, sd.getFieldValue(field).toString());
        }

        writeOut(obj.toJSONString());
    }
    private void writeXml(SolrDocument sd) {

    }

    public void write (SolrDocument sd) {
        try {
            switch(style) {
                case "csv":
                    writeCsv(sd);
                    break;
                case "tab":
                    writeTab(sd);
                    break;
                case "json":
                    writeJson(sd);
                    break;
                case "xml":
                    writeXml(sd);
                    break;
                default:
                    System.err.println("42");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Output(Writer writer, String style) {
        this.writer = writer;
        this.style = style;
    }
}