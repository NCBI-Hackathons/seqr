
package gov.nih.nlm.ncbi.seqr;

import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.io.Writer;
import java.util.StringJoiner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Output {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String CSV_DELIMITER = ",";

    private Writer writer;
    private String style;
    private String[] fields;

    public void setFields(String[] fields) { this.fields = fields; }
    public String[] getFields() { return fields; }
    private void checkFields(SolrDocument sd) {
        if(fields == null) {
            setFields((String[]) sd.getFieldNames().toArray());
        }
    }

    private void writeOut(String out) throws IOException { writer.write(out); }

    private void writeCsv(SolrDocument sd) throws IOException {
        checkFields(sd);
        for(String field : getFields()) {
            writeOut(sd.getFieldValue(field).toString());
            writeOut(",");
        }
        writeOut(System.getProperty("line.separator"));
    }
    private void writeTab(SolrDocument sd) throws IOException {
        checkFields(sd);
        for(String field : getFields()) {
            writeOut(sd.getFieldValue(field).toString());
            writeOut("\t");
        }
        writeOut(System.getProperty("line.separator"));
    }
    private void writeJson(SolrDocument sd) throws IOException {
        checkFields(sd);
        JSONObject obj = new JSONObject();

        for(String field : getFields()) {
            obj.put(field, sd.getFieldValue(field).toString());
        }

        writeOut(obj.toJSONString());
    }
    private void writeXml(SolrDocument sd) {

    }

    public void writeHeader(SolrDocument sd) throws IOException {
        checkFields(sd);

        String versionSeqr  = "1.o";
        String versionSolr  = "4.10.4";
        String queryName    = "gi|584277003|ref|NP_001276862.1| ZO-2 associated speckle protein [Homo sapiens]";
        String databaseName = "refseq_protein.00";

        int hits = 6;

        writeHeader(versionSeqr, versionSolr, queryName, databaseName, getFields(), hits);
    }
    public void writeHeader(String versionSeqr, String versionSolr, String queryName, String databaseName, String[] fields, int hits) throws IOException {
        setFields(fields);
        StringJoiner joiner = new StringJoiner(CSV_DELIMITER);
        for(String s : getFields()) {
            joiner.add(s);
        }
        String fieldNames = joiner.toString();

        writeOut("# SEQR "      + versionSeqr  + "\n" +
                 "# SOLR "      + versionSolr  + "\n" +
                 "# Query: "    + queryName    + "\n" +
                 "# Database: " + databaseName + "\n" +
                 "# Fields: "   + fieldNames   + "\n" +
                 "# " + Integer.toString(hits) + " hits found" + "\n");
    }
    public void write (SolrDocument sd) throws IOException {
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
                writeCsv(sd);
                break;
        }
    }

    public Output(Writer writer, String style) {
        this.writer = writer;
        this.style = style;
    }
}