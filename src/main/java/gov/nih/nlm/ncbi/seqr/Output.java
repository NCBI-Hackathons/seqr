
package gov.nih.nlm.ncbi.seqr;

import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.apache.solr.common.SolrDocumentList;
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

    // Help information
    public static final String OUTPUTHELP =
            "Blast+ style output format codes are used. Include one number from below:\n" +
            "     0 = pairwise,\n" +
            "     1 = query-anchored showing identities,\n" +
            "     2 = query-anchored no identities,\n" +
            "     3 = flat query-anchored, show identities,\n" +
            "     4 = flat query-anchored, no identities,\n" +
            "     5 = XML Blast output,\n" +
            "     6 = tabular,\n" +
            "     7 = tabular with comment lines,\n" +
            "     8 = Text ASN.1,\n" +
            "     9 = Binary ASN.1,\n" +
            "    10 = Comma-separated values,\n" +
            "    11 = BLAST archive format (ASN.1),\n" +
            "    12 = JSON Seqalign output,\n" +
            "    13 = JSON Blast output,\n" +
            "    14 = XML2 Blast output,\n" +
            "    15 = SAM Blast output\n" +
            "Optionally, include space delimited list of fields to include in output after the number. Order will be respected. e.g.\n" +
            "-outfmt \"42 all_the_fish thanks_for so_long_and\"";

    // Blast+ style output format codes
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

    // Delimiters used in output
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String COMMA_DELIMITER = ",";
    private static final String COMMA_SPACE_DELIMITER = ", ";
    private static final String TAB_DELIMITER = "\t";

    // Locate varibles
    private Writer writer;
    private int style = COMMA_SEPARATED_VALUES;
    private List<String> fields;
    private boolean wroteHeader = false;
    private int total_hits;

    // Public get/set methods for locate varibles
    public void setWriter(Writer writer) { this.writer = writer; }
    public Writer getWriter() { return writer; }

    public void setStyle(int style) { this.style = style; }
    public int getStyle() { return style; }

    public void setFields(List<String> fields) { this.fields = fields; }
    public List<String> getFields() { return fields; }

    public void setWroteHeader(boolean wroteHeader) { this.wroteHeader = wroteHeader; }
    public boolean getWroteHeader() { return wroteHeader; }

    public void setTotalHits(int total_hits) { this.total_hits = total_hits; }
    public int getTotalHits() { return total_hits; }

    // Save initial ordering of fields for later use
    private void checkFields(SolrDocument sd) {
        if(fields == null || fields.isEmpty())
            setFields( new ArrayList<String>(sd.getFieldNames()));
    }

    // Write String to writer, moved here in case you want to do error handling here in the future
    private void writeOut(String out) throws IOException { writer.write(out); }

    // Write SolrDocument in CSV format
    private void writeCsv(SolrDocument sd) throws IOException {
        checkFields(sd);

        StringJoiner joiner = new StringJoiner(COMMA_DELIMITER);
        for(String field : getFields()) {
            joiner.add(sd.getFieldValue(field).toString());
        }

        writeOut(joiner.toString());
        writeOut(NEW_LINE);
    }
    // Write SolrDocument in Tabular format
    private void writeTab(SolrDocument sd) throws IOException {
        checkFields(sd);

        StringJoiner joiner = new StringJoiner(TAB_DELIMITER);
        for(String field : getFields()) {
            joiner.add(sd.getFieldValue(field).toString());
        }

        writeOut(joiner.toString());
        writeOut(NEW_LINE);
    }
    // Write SolrDocument in JSON format
    private void writeJson(SolrDocument sd) throws IOException {
        checkFields(sd);
        JSONObject obj = new JSONObject();

        for(String field : getFields()) {
            obj.put(field, sd.getFieldValue(field).toString());
        }

        writeOut(obj.toJSONString());
    }
    // Write SolrDocument in XML format
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

    /*  Make up random header. This test method can be removed if deciding if to output a header
     *  is handled up stream. Otherwise it will need to be rewritten.
     *  TODO: Figure this out.
     */
    public void writeHeader(SolrDocument sd) throws IOException {
        if(wroteHeader) { return; }

        checkFields(sd);

        String versionSeqr  = Seqr.getVersion();
        String versionSolr  = Seqr.getVersionSolr();
        String queryName    = sd.getFieldValue("defline").toString();
        String databaseName = "refseq_protein.00";

        int hits = total_hits;

        writeHeader(versionSeqr, versionSolr, queryName, databaseName, getFields(), hits);
    }
    /*  Write out file header. This method requires everything to be provided by the caller. The previous
     *  method just made up all these things.
     */
    public void writeHeader(String versionSeqr, String versionSolr, String queryName, String databaseName, List<String> fields, int hits) throws IOException {
        if(wroteHeader) { return; }

        setFields(fields);
        StringJoiner joiner = new StringJoiner(COMMA_SPACE_DELIMITER);
        for(String s : getFields()) {
            joiner.add(s);
        }
        String fieldNames = joiner.toString();

        writeOut("# SEQR " + versionSeqr + NEW_LINE +
                "# SOLR " + versionSolr + NEW_LINE +
                "# Query: " + queryName + NEW_LINE +
                "# Database: " + databaseName + NEW_LINE +
                "# Fields: " + fieldNames + NEW_LINE +
                "# " + Integer.toString(hits) + " hits found" + NEW_LINE);

        wroteHeader = !wroteHeader;
    }

    /*  Primary method for this class. This takes in one SolrDocument object at a time and redicects it
     *  to an internal method for formating as determined by the style int. The result is then writer to
     *  the writer object. The writer object and style int are give in the constructor. If a field list
     *  is provided, only those fields in that order will be outputed. If no field list is given, all
     *  fields in SolrDocument will be outputed, likily in a random order that is consistent between
     *  SolrDocument objects.
     */
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

    /*  Constructors for this object class. Various things can be provided or defaults used. Must
     *  provide a Writer object at a minimum.
     */
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
