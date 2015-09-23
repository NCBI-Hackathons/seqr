package gov.nih.nlm.ncbi.seqr;

import gov.nih.nlm.ncbi.seqr.solr.FastaStreamParser;
import gov.nih.nlm.ncbi.seqr.solr.SeqrController;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hanl on 9/23/15.
 */
@net.jcip.annotations.NotThreadSafe
public class SeqrTest extends TestCase {


    private static SeqrController control;
    static EmbeddedSolrServer solrServer;
    static CoreContainer container ;

    Lock sequential = new ReentrantLock();


    public void setUp() throws Exception {
        super.setUp();
        sequential.lock();
        String solrString = "testdata/solr";
        container = new CoreContainer(solrString);
        container.load();
        solrServer = new EmbeddedSolrServer(container, "sequence");
        control = new SeqrController(solrServer);
        solrServer.deleteByQuery("*:*");
        control.loadJSONDir("testdata/data");

    }

    public void tearDown() throws Exception {
        this.tearDownController();
        sequential.unlock();
        //LoadLargeFile2SolrServer server = new LoadLargeFile2SolrServer(solrServer);
        //server.loadFastaFile(new File("testdata/data/short.fasta"));
    }


    @BeforeClass
    public static void beforeEverything() throws SolrServerException, InterruptedException, IOException {
        //setUpController();
        tearDownController();
    }
    public static void tearDownController() {
        solrServer.shutdown();
        container.shutdown();
    }

//    @Test
//    public void testFastaSolrImport() throws Exception {
//        setUpController();
//        LoadLargeFile2SolrServer server = new LoadLargeFile2SolrServer(solrServer);
//        server.loadFastaFile(new File("testdata/data/short.fasta"));
//        tearDownController();
//    }



    @Test
    public void testAbSolrSimple() throws Exception {

        //String seq = "MAFSAEDVLKEYDRRRRMEALLLSLYYPNDRKLLDYKEWSPPRVQVECPKAPVEWNNPPSEKGLIVGHFSGIKYKGEKAQASEVDVNKMCCWVSKFKDAMRRYQGIQTCKIPGKVLSDLDAKIKAYNLTVEGVEGFVRYSRVTKQHVAAFLKELRHSKQYENVNLIHYILTDKRVDIQHLEKDLVKDFKALVESAHRMRQGHMINVKYILYQLLKKHGHGPDGPDILTVKTGSKGVLYDDSFRKIYTDLGWKFTPL";
        //String seq = "MSFKVYDPIAELIATQFPTSNPDLQIINNDVLVVSPHKITLPMGPQNAGDVTNKAYVDQAVMSAAVPVASSTTVGTIQMAGDLEGSSGTNPIIAANKITLNKLQKIGPKMVIGNPNSDWNNTQEIELDSSFRIVDNRLNAGIVPISSTDPNKSNTVIPAPQQNGLFYLDSSGRVWVWAEHYYKCITPSRYISKWMGVGDFQELTVGQSVMWDSGRPSIETVSTQGLEVEWISSTNFTLSSLYLIPIVVKVTICIPLLGQPDQMAKFVLYSVSSAQQPRTGIVLTTDSSRSSAPIVSEYITVNWFEPKSYSVQLKEVNSDSGTTVTICSDKWLANPFLDCWITIEEVG";
        String seq = "MNTLYLGSNSPRRMEILTQLGYRVIQLPAGIDESVKAGETPFAYVQRMAEEKNRTALTLFCETNGTMPDFPLITADTCVVSDGIILGKPRSQAEAIEFLNRLSGKQHTVLTAVCIHYRGKTSSRVQTNRVVFKPLSSEEISAYVQSGEPMDKAGAYAVQGIGGIFIQSIEGSFSGIMGLPVYETVSMLQDLGYRSPLSALKP";
        //String seq = "AGSYLLEELFEGHLEKECWEEICVYEEAREVFEDDETTDEFWRTYMGGSPCASQPCLNNGSCQDSIRGYACTCAPGYEGPNCAFAESECHPLRLDGCQHFCYPGPESYTCSCARGHKLGQDRRSCLPHDRCACGTLGPECCQRPQGSQQNLLPFPWQVKLTNSEGKDFCGGVLIQDNFVLTTATCSLLYANISVKTRSHFRLHVRGVHVHTRFEADTGHNDVALLDLARPVRCPDAGRPVCTADADFADSVLLPQPGVLGGWTLRGREMVPLRLRVTHVEPAECGRALNATVTTRTSCERGAAAGAARWVAGGAVVREHRGAWFLTGLLGAAPPEGPGPLLLIKVPRYALWLRQVTQQPSRASPRGDRGQGRDGEPVPGDRGGRWAPTALPPGPLV";
        SolrDocumentList results = control.search(seq);
        for (SolrDocument document : results) {
            System.out.println(document);
        }
        Assert.assertEquals(1, results.size());
    }

    @Test
    //TODO: check this works with an assertion
    public void testFastaToJsonFileConversion() throws Exception {
        FastaStreamParser converter = new FastaStreamParser("testdata/data/short.fasta");
        converter.convertToJsonFile("testdata/data/short.json");
        new File("testdata/data/short.json").delete();
    }
}