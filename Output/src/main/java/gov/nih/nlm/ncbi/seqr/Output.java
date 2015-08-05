
package gov.nih.nlm.ncbi.seqr;

import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.io.Writer;
import java.util.StringJoiner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
        writeOut(NEW_LINE);
    }
    private void writeTab(SolrDocument sd) throws IOException {
        checkFields(sd);
        for(String field : getFields()) {
            writeOut(sd.getFieldValue(field).toString());
            writeOut("\t");
        }
        writeOut(NEW_LINE);
    }
    private void writeJson(SolrDocument sd) throws IOException {
        checkFields(sd);
        JSONObject obj = new JSONObject();

        for(String field : getFields()) {
            obj.put(field, sd.getFieldValue(field).toString());
        }

        writeOut(obj.toJSONString());
    }
    private void writeXml(SolrDocument sd) throws IOException, ParserConfigurationException, TransformerException {
        checkFields(sd);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("seqr");
        doc.appendChild(rootElement);

        for(String field : getFields()) {
            Element fieldElement = doc.createElement(field);
            fieldElement.appendChild(doc.createTextNode(sd.getFieldValue(field).toString()));
            rootElement.appendChild(fieldElement);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
    }

    public void writeHeader(SolrDocument sd) throws IOException {
        checkFields(sd);

        String versionSeqr  = "1.0";
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
    public void write (SolrDocument sd) throws IOException, ParserConfigurationException, TransformerException {
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