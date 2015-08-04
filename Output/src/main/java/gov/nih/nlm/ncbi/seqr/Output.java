
package gov.nih.nlm.ncbi.seqr;

import java.io.*;
import java.util.StringJoiner;

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

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String CSV_DELIMITER = ",";

    private String filename = "stdout";
    private int style = COMMA_SEPARATED_VALUES;
    private Writer writer;

    private void setWriter (Writer writer) { this.writer = writer; }
    private Writer getWriter () { return writer; }

    public void setFilename (String filename) { this.filename = filename; }
    public String getFilename () { return filename; }

    public void setStyle (int style) { this.style = style; }
    public int getStyle () { return style; }

    public void openFile () {
        try {
            if(filename == "stdout") {
                writer = new BufferedWriter(new OutputStreamWriter(System.out));
            } else {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFilename()), "utf-8"));
            }
        } catch (IOException ex) {
            System.err.println("10 This is a helpful error message~");
        }
    }
    public void writeFile (String str) {
        try {
            writer.write(str);
            writer.write(NEW_LINE);
        } catch (Exception ex) {
            System.err.println("20 This is a helpful error message~");
            System.err.println(ex.toString());
        }
    }
    public void closeFile () {
        try {
            writer.close();
        } catch (Exception ex) {
            System.err.println("30 This is a helpful error message~");
        }
    }

    public void write(Result res) {
        switch (getStyle()) {
            case PAIRWISE:

                break;
            case QUERY_ANCHORED_SHOWING_IDENTITIES:

                break;
            case QUERY_ANCHORED_NO_IDENTITIES:

                break;
            case FLAT_QUERY_ANCHORED_SHOW_IDENTITIES:

                break;
            case FLAT_QUERY_ANCHORED_NO_IDENTITIES:

                break;
            case XML_BLAST_OUTPUT:

                break;
            case TABULAR:
                writeTab(res);
                break;
            case TABULAR_WITH_COMMENT_LINES:
                writeTabHeader();
                writeTab(res);
                break;
            case TEXT_ASN_1:

                break;
            case BINARY_ASN_1:

                break;
            case COMMA_SEPARATED_VALUES:
                writeCsv(res);
                break;
            case BLAST_ARCHIVE_FORMAT_ASN_1:

                break;
            case JSON_SEQALIGN_OUTPUT:

                break;
            case JSON_BLAST_OUTPUT:

                break;
            case XML2_BLAST_OUTPUT:

                break;
        }
    }

    private void writeCsvHeader () {

        StringJoiner joiner = new StringJoiner(CSV_DELIMITER);

        joiner.add("#QueryId");
        joiner.add("SubjectId");
        joiner.add("PercentIdentity");
        joiner.add("Alignment");
        joiner.add("Length");
        joiner.add("Mismatches");
        joiner.add("GapOpens");
        joiner.add("QueryStart");
        joiner.add("QueryEnd");
        joiner.add("SubjectStart");
        joiner.add("SubjectEnd");
        joiner.add("Evalue");
        joiner.add("BitScore");

        writeFile(joiner.toString());
    }
    private void writeCsv (Result res) {

        StringJoiner joiner = new StringJoiner(CSV_DELIMITER);

        joiner.add(res.getQueryId());
        joiner.add(res.getSubjectId());
        joiner.add(Double.toString(res.getPercentIdentity()));
        joiner.add(Integer.toString(res.getAlignment()));
        joiner.add(Integer.toString(res.getLength()));
        joiner.add(Integer.toString(res.getMismatches()));
        joiner.add(Integer.toString(res.getGapOpens()));
        joiner.add(Integer.toString(res.getQueryStart()));
        joiner.add(Integer.toString(res.getQueryEnd()));
        joiner.add(Integer.toString(res.getSubjectStart()));
        joiner.add(Integer.toString(res.getSubjectEnd()));
        joiner.add(Double.toString(res.getEvalue()));
        joiner.add(Double.toString(res.getBitScore()));

        writeFile(joiner.toString());

    }
    private void writeTabHeader () {

        StringJoiner joiner = new StringJoiner("\t");

        joiner.add("#QueryId");
        joiner.add("SubjectId");
        joiner.add("PercentIdentity");
        joiner.add("Alignment");
        joiner.add("Length");
        joiner.add("Mismatches");
        joiner.add("GapOpens");
        joiner.add("QueryStart");
        joiner.add("QueryEnd");
        joiner.add("SubjectStart");
        joiner.add("SubjectEnd");
        joiner.add("Evalue");
        joiner.add("BitScore");

        writeFile(joiner.toString());
    }
    private void writeTab (Result res) {

        StringJoiner joiner = new StringJoiner("\t");

        joiner.add(res.getQueryId());
        joiner.add(res.getSubjectId());
        joiner.add(Double.toString(res.getPercentIdentity()));
        joiner.add(Integer.toString(res.getAlignment()));
        joiner.add(Integer.toString(res.getLength()));
        joiner.add(Integer.toString(res.getMismatches()));
        joiner.add(Integer.toString(res.getGapOpens()));
        joiner.add(Integer.toString(res.getQueryStart()));
        joiner.add(Integer.toString(res.getQueryEnd()));
        joiner.add(Integer.toString(res.getSubjectStart()));
        joiner.add(Integer.toString(res.getSubjectEnd()));
        joiner.add(Double.toString(res.getEvalue()));
        joiner.add(Double.toString(res.getBitScore()));

        writeFile(joiner.toString());

    }
    private void writeJson (Result res) {
        // TODO: do this
    }
    private void writeAsn (Result res) {

    }

    public Output (String filename, int style) {
        this.filename = filename;
        this.style = style;
        this.openFile();
    }
    public Output (int style) {
        this.style = style;
        this.openFile();
    }
    public Output () {

    }

}