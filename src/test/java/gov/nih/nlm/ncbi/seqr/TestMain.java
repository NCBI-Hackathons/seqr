package gov.nih.nlm.ncbi.seqr;

import gov.nih.nlm.ncbi.seqr.solr.FastaStreamParser;
import gov.nih.nlm.ncbi.seqr.solr.LoadLargeFile2SolrServer;
import gov.nih.nlm.ncbi.seqr.solr.SeqrController;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import java.io.File;

//import org.codehaus.jackson.JsonNode;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import java.io.IOException;

public class TestMain {
    private static SeqrController control;
    static EmbeddedSolrServer solrServer;
    static CoreContainer container ;

    private static void setUpController() {
        String solrString = "testdata/solr";
        container = new CoreContainer(solrString);
        container.load();
        solrServer = new EmbeddedSolrServer(container, "sequence");
        control = new SeqrController(solrServer);
    }

    @BeforeClass
    public static void beforeEverything() throws SolrServerException, InterruptedException, IOException {
        setUpController();
        solrServer.deleteByQuery("*:*");
        control.loadJSONDir("testdata/data");
        tearDownController();
//        LoadLargeFile2SolrServer server = new LoadLargeFile2SolrServer(solrServer);
//        server.loadFastaFile(new File("testdata/data/short.fasta"));

    }
    public static void tearDownController() {
        solrServer.shutdown();
        container.shutdown();
    }

    @Test
    public void testFastaSolrImport() throws Exception {
        LoadLargeFile2SolrServer server = new LoadLargeFile2SolrServer(solrServer);
        server.loadFastaFile(new File("testdata/data/short.fasta"));
    }

    @Test
    public void testFastaIndexingIntegration() throws SolrServerException {
        // check that it's not already in solr
        String seq = "MAFSAEDVLKEYDRRRRMEALLLSLYYPNDRKLLDYKEWSPPRVQVECPKAPVEWNNPPS" +
                "EKGLIVGHFSGIKYKGEKAQASEVDVNKMCCWVSKFKDAMRRYQGIQTCKIPGKVLSDLD" +
                "AKIKAYNLTVEGVEGFVRYSRVTKQHVAAFLKELRHSKQYENVNLIHYILTDKRVDIQHL" +
                "EKDLVKDFKALVESAHRMRQGHMINVKYILYQLLKKHGHGPDGPDILTVKTGSKGVLYDD" +
                "SFRKIYTDLGWKFTPL";
        SolrDocumentList results = control.search(seq);
        Assert.assertEquals(0, results.size());

        String[] args = {"index",  "testdata/data/short.fasta", "--db", "testdata/solr/", "--input_format", "fasta"};
        Seqr.main(args);
        setUpController();
        results = control.search(seq);
        Assert.assertEquals(1, results.size());
        tearDownController();
    }

    @Test
    public void testAbSolrSimple() throws Exception {
        setUpController();
        //String seq = "MAFSAEDVLKEYDRRRRMEALLLSLYYPNDRKLLDYKEWSPPRVQVECPKAPVEWNNPPSEKGLIVGHFSGIKYKGEKAQASEVDVNKMCCWVSKFKDAMRRYQGIQTCKIPGKVLSDLDAKIKAYNLTVEGVEGFVRYSRVTKQHVAAFLKELRHSKQYENVNLIHYILTDKRVDIQHLEKDLVKDFKALVESAHRMRQGHMINVKYILYQLLKKHGHGPDGPDILTVKTGSKGVLYDDSFRKIYTDLGWKFTPL";
        String seq = "MSFKVYDPIAELIATQFPTSNPDLQIINNDVLVVSPHKITLPMGPQNAGDVTNKAYVDQAVMSAAVPVASSTTVGTIQMAGDLEGSSGTNPIIAANKITLNKLQKIGPKMVIGNPNSDWNNTQEIELDSSFRIVDNRLNAGIVPISSTDPNKSNTVIPAPQQNGLFYLDSSGRVWVWAEHYYKCITPSRYISKWMGVGDFQELTVGQSVMWDSGRPSIETVSTQGLEVEWISSTNFTLSSLYLIPIVVKVTICIPLLGQPDQMAKFVLYSVSSAQQPRTGIVLTTDSSRSSAPIVSEYITVNWFEPKSYSVQLKEVNSDSGTTVTICSDKWLANPFLDCWITIEEVG";
        //String seq = "AGSYLLEELFEGHLEKECWEEICVYEEAREVFEDDETTDEFWRTYMGGSPCASQPCLNNGSCQDSIRGYACTCAPGYEGPNCAFAESECHPLRLDGCQHFCYPGPESYTCSCARGHKLGQDRRSCLPHDRCACGTLGPECCQRPQGSQQNLLPFPWQVKLTNSEGKDFCGGVLIQDNFVLTTATCSLLYANISVKTRSHFRLHVRGVHVHTRFEADTGHNDVALLDLARPVRCPDAGRPVCTADADFADSVLLPQPGVLGGWTLRGREMVPLRLRVTHVEPAECGRALNATVTTRTSCERGAAAGAARWVAGGAVVREHRGAWFLTGLLGAAPPEGPGPLLLIKVPRYALWLRQVTQQPSRASPRGDRGQGRDGEPVPGDRGGRWAPTALPPGPLV";
        SolrDocumentList results = control.search(seq);
        for (SolrDocument document : results) {
            System.out.println(document);
        }
        Assert.assertEquals(1, results.size());
        tearDownController();
    }


    @Test
    public void testArgParse() {
        String[] args = {"search",  "testdata/data/test.fasta", "--db", "testdata/solr/"};
        Seqr.main(args);
        }

    @Test
    //TODO: check this works with an assertion
    public void testFastaToJsonFileConversion() throws Exception {
        FastaStreamParser converter = new FastaStreamParser("testdata/data/short.fasta");
        converter.convertToJsonFile("testdata/data/short.json");
        new File("testdata/data/short.json").delete();
    }

}
