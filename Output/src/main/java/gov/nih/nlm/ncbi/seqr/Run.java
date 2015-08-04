
package gov.nih.nlm.ncbi.seqr;

class Run {

    public static void main(String[] args){

        Result res = new Result();
        res.setQueryId("foo");
        res.setSubjectId("bar");
        res.setPercentIdentity(5.5);
        res.setAlignment(12);
        res.setLength(500);
        res.setMismatches(4);
        res.setGapOpens(0);
        res.setQueryStart(0);
        res.setQueryEnd(1024);
        res.setSubjectStart(24);
        res.setSubjectEnd(1048);
        res.setEvalue(8.0);
        res.setBitScore(42.42);

        Output out = new Output(Output.TABULAR_WITH_COMMENT_LINES);
        out.openFile();
        out.write(res);
        out.closeFile();

    }

}