
package gov.nih.nlm.ncbi.seqr;

public class Result {

    private String value = "value";

    private String query_id;
    private String subject_id;
    private double percent_identity;
    private int alignment;
    private int length;
    private int mismatches;
    private int gap_opens;
    private int query_start;
    private int query_end;
    private int subject_start;
    private int subject_end;
    private double evalue;
    private double bit_score;

    public void setValue (String value) { this.value = value; }
    public String getValue () { return value; }

    public void   setQueryId             (String query_id)         { this.query_id = query_id; }
    public String getQueryId         () { return query_id; }
    public void   setSubjectId           (String subject_id)       { this.subject_id = subject_id; }
    public String getSubjectId       () { return subject_id; }
    public void   setPercentIdentity     (double percent_identity) { this.percent_identity = percent_identity; }
    public double getPercentIdentity () { return percent_identity; }
    public void   setAlignment           (int    alignment)        { this.alignment = alignment; }
    public int    getAlignment       () { return alignment; }
    public void   setLength              (int    length)           { this.length = length; }
    public int    getLength          () { return length; }
    public void   setMismatches          (int    mismatches)       { this.mismatches = mismatches; }
    public int    getMismatches      () { return mismatches; }
    public void   setGapOpens            (int    gap_opens)        { this.gap_opens = gap_opens; }
    public int    getGapOpens        () { return gap_opens; }
    public void   setQueryStart          (int    query_start)      { this.query_start = query_start; }
    public int    getQueryStart      () { return query_start; }
    public void   setQueryEnd            (int    query_end)        { this.query_end = query_end; }
    public int    getQueryEnd        () { return query_end; }
    public void   setSubjectStart        (int    subject_start)    { this.subject_start = subject_start; }
    public int    getSubjectStart    () { return subject_start; }
    public void   setSubjectEnd          (int    subject_end)      { this.subject_end = subject_end; }
    public int    getSubjectEnd      () { return subject_end; }
    public void   setEvalue              (double evalue)           { this.evalue = evalue; }
    public double getEvalue          () { return evalue; }
    public void   setBitScore            (double bit_score)        { this.bit_score = bit_score; }
    public double getBitScore        () { return bit_score; }

    public Result (String query_id, String subject_id, double percent_identity, int    alignment, int    length, int    mismatches, int    gap_opens, int    query_start, int    query_end, int    subject_start, int    subject_end, double evalue, double bit_score) {
        this.query_id = query_id;
        this.subject_id = subject_id;
        this.percent_identity = percent_identity;
        this.alignment = alignment;
        this.length = length;
        this.mismatches = mismatches;
        this.gap_opens = gap_opens;
        this.query_start = query_start;
        this.query_end = query_end;
        this.subject_start = subject_start;
        this.subject_end = subject_end;
        this.evalue = evalue;
        this.bit_score = bit_score;
    }
    public Result () {

    }
}