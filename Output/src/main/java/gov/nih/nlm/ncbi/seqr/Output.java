
package gov.nih.nlm.ncbi.seqr;

import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
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
    public static final int PAIRWISE                            = 0;
    public static final int QUERY_ANCHORED_SHOWING_IDENTITIES   = 1;
    public static final int QUERY_ANCHORED_NO_IDENTITIES        = 2;
    public static final int FLAT_QUERY_ANCHORED_SHOW_IDENTITIES = 3;
    public static final int FLAT_QUERY_ANCHORED_NO_IDENTITIES   = 4;
    public static final int XML_BLAST_OUTPUT                    = 5;
    public static final int TABULAR                             = 6;
    public static final int TABULAR_WITH_COMMENT_LINES          = 7;
    public static final int TEXT_ASN_1                          = 8;
    public static final int BINARY_ASN_1                        = 9;
    public static final int COMMA_SEPARATED_VALUES              = 10;
    public static final int BLAST_ARCHIVE_FORMAT_ASN_1          = 11;
    public static final int JSON_SEQALIGN_OUTPUT                = 12;
    public static final int JSON_BLAST_OUTPUT                   = 13;
    public static final int XML2_BLAST_OUTPUT                   = 14;
    public static final int SAM_BLAST_OUTPUT                    = 15;

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String CSV_DELIMITER = ",";

    private Writer writer;
    private int style = COMMA_SEPARATED_VALUES;
    private List<String> fields;

    public void setStyle (int style) { this.style = style; }
    public int getStyle () { return style; }

    public void setFields(List<String> fields) { this.fields = fields; }
    public List<String> getFields() { return fields; }
    private void checkFields(SolrDocument sd) {
        if(fields == null) {
            setFields((List<String>) sd.getFieldNames());
        }
    }

    private void writeOut(String out) throws IOException { writer.write(out); }

    private void writeCsv(SolrDocument sd) throws IOException {
        checkFields(sd);
        for(String field : getFields()) {
            writeOut(sd.getFieldValue(field).toString());
            writeOut(CSV_DELIMITER);
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
    private void writeXml(SolrDocument sd) throws ParserConfigurationException, TransformerException {
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
    public void writeHeader(String versionSeqr, String versionSolr, String queryName, String databaseName, List<String> fields, int hits) throws IOException {
        setFields(fields);
        StringJoiner joiner = new StringJoiner(CSV_DELIMITER);
        for(String s : getFields()) {
            joiner.add(s);
        }
        String fieldNames = joiner.toString();

        writeOut("# SEQR " + versionSeqr + "\n" +
                "# SOLR " + versionSolr + "\n" +
                "# Query: " + queryName + "\n" +
                "# Database: " + databaseName + "\n" +
                "# Fields: " + fieldNames + "\n" +
                "# " + Integer.toString(hits) + " hits found" + "\n");
    }
    public void write (SolrDocument sd) throws IOException, ParserConfigurationException, TransformerException {
        switch (getStyle()) {
            case PAIRWISE:
                throw new UnsupportedOperationException("Pairwise is unimplemented");
            case QUERY_ANCHORED_SHOWING_IDENTITIES:
                throw new UnsupportedOperationException("Query anchored showing identities is unimplemented");
            case QUERY_ANCHORED_NO_IDENTITIES:
                throw new UnsupportedOperationException("Query anchored, no identities is unimplemented");
            case FLAT_QUERY_ANCHORED_SHOW_IDENTITIES:
                throw new UnsupportedOperationException("Flat query anchored showing identities is unimplemented");
            case FLAT_QUERY_ANCHORED_NO_IDENTITIES:
                throw new UnsupportedOperationException("Flat query anchored, no identities is unimplemented");
            case XML_BLAST_OUTPUT:
                writeXml(sd);
                break;
            case TABULAR:
                writeTab(sd);
                break;
            case TABULAR_WITH_COMMENT_LINES:
                writeHeader(sd);
                writeTab(sd);
                break;
            case TEXT_ASN_1:
                throw new UnsupportedOperationException("Text ASN.1 is unimplemented");
            case BINARY_ASN_1:
                throw new UnsupportedOperationException("Binary ASN.1 is unimplemented");
            case COMMA_SEPARATED_VALUES:
                writeCsv(sd);
                break;
            case BLAST_ARCHIVE_FORMAT_ASN_1:
                throw new UnsupportedOperationException("Blast archive format ASN.1 is unimplemented");
            case JSON_SEQALIGN_OUTPUT:
                writeJson(sd);
                break;
            case JSON_BLAST_OUTPUT:
                writeJson(sd);
                break;
            case XML2_BLAST_OUTPUT:
                writeXml(sd);
                break;
            case SAM_BLAST_OUTPUT:
                throw new UnsupportedOperationException("SAM output is unimplemented");
            default:
                writeCsv(sd);
                break;
        }
    }

    public Output(Writer writer, int style, List<String> fields) {
        this.writer = writer;
        this.style = style;
        this.fields = fields;
    }
    public Output(Writer writer, List<String> fields) {
        this.writer = writer;
        this.fields = fields;
    }
    public Output(Writer writer, int style) {
        this.writer = writer;
        this.style = style;
    }
    public Output(Writer writer) {
        this.writer = writer;
    }
}