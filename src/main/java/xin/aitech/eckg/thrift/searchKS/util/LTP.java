package xin.aitech.eckg.thrift.searchKS.util;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.aitech.eckg.thrift.ltp.LtpPayload;
import xin.aitech.eckg.thrift.ltp.LtpService;
import xin.aitech.eckg.thrift.ltp.Srl;

import java.util.*;

/**
 * @Description: LTP class
 * @author: Hugo Ng
 * @Date: 17-5-19
 */
public class LTP {
    private static final ResourceBundle SETTING = LoadConfig.getResourceBundle();
    private static String SEARCHKS_RPC_LTP_ADDRESS;
    private static int SEARCHKS_RPC_LTP_PORT;

    private static Logger _logger = LoggerFactory.getLogger(LTP.class);
    private String sent;
    private final String traceId;
    private List<String> sub;
    private List<String> verb;
    private List<String> obj;
    private static final Set numSet = new HashSet<String>() {
        {
            add("i");
            add("j");
            add("n");
            add("nd");
            add("nh");
            add("ni");
            add("nl");
            add("ns");
            add("nt");
            add("nz");
            add("ws");
        }
    };

    public LTP(String sent, String traceId, Map<String, String> setting) {
        this.sent = sent;
        this.traceId = traceId;
        this.sub = new ArrayList<String>();
        this.verb = new ArrayList<String>();
        this.obj = new ArrayList<String>();
        SEARCHKS_RPC_LTP_ADDRESS = setting.containsKey("searchKS_rpc_ltp_address") ? setting.get("searchKS_rpc_ltp_address")
                : SETTING.getString("searchKS.rpc.ltp.address");
        SEARCHKS_RPC_LTP_PORT = setting.containsKey("searchKS_rpc_ltp_port") ? Integer.parseInt(setting.get("searchKS_rpc_ltp_port"))
                : Integer.parseInt(SETTING.getString("searchKS.rpc.ltp.port"));
        this.processSent();

        _logger.info("{} - Initialize LTP sent on address {} - {}: {}", this.traceId, SEARCHKS_RPC_LTP_ADDRESS, SEARCHKS_RPC_LTP_PORT, this.sent);
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
        this.sub.clear();
        this.verb.clear();
        this.obj.clear();
        this.processSent();
        _logger.info("{} - Initialize LTP sent: {}", this.traceId, this.sent);
    }

    public List<String> getSub() {
        return sub;
    }

    public List<String> getVerb() {
        return verb;
    }

    public List<String> getObj() {
        return obj;
    }

    @SuppressWarnings("unchecked")
    private void processSent() {
        List<LtpPayload> ltpPayloads = null;
        try {
            TTransport transport = new TSocket(SEARCHKS_RPC_LTP_ADDRESS, SEARCHKS_RPC_LTP_PORT);
            transport = new TFramedTransport(transport);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            LtpService.Client client = new LtpService.Client(protocol);

            ltpPayloads = client.process(this.sent, this.traceId);

            transport.close();

            List<String> words = ltpPayloads.get(0).getWords();
            List<String> postags = ltpPayloads.get(0).getPostags();
            List<String> ners = ltpPayloads.get(0).getNers();
            List<Integer> heads = ltpPayloads.get(0).getHeads();
            List<String> deprels = ltpPayloads.get(0).getDeprels();
            List<Srl> srls = ltpPayloads.get(0).getSrls();

            for (int i = 0; i < srls.size(); i++) {
                for (int j = 0; j < srls.get(i).getRolesSize(); j++) {
                    _logger.info("{} - type = {}, beg = {}, end = {}",
                            this.traceId,
                            srls.get(i).getRoles().get(j).getRole(),
                            srls.get(i).getRoles().get(j).getStartIndex(),
                            srls.get(i).getRoles().get(j).getEndIndex()
                    );

                    for (int k = srls.get(i).getRoles().get(j).getStartIndex(); k <= srls.get(i).getRoles().get(j).getEndIndex(); k++) {
                        String type = srls.get(i).getRoles().get(j).getRole();
                        // get A0 and A1
                        if ("A0".equals(type) && !this.sub.contains(words.get(k))) {
                            this.sub.add(words.get(k));
                        } else if ("A1".equals(type) && !this.obj.contains(words.get(k))) {
                            this.obj.add(words.get(k));
                        }
                    }
                }
            }

            // get verb
            for (int i = 0; i < words.size(); i++) {
                _logger.info("{} - word = {}, postags = {}, ners = {}, heads = {}, deprels = {}",
                        this.traceId, words.get(i), postags.get(i), ners.get(i), heads.get(i), deprels.get(i));

                if (!this.sub.contains(words.get(i)) && !this.obj.contains(words.get(i)) && "v".equals(postags.get(i))) {
                    this.verb.add(words.get(i));
                }
            }

            // if none of A0 and A1 were founded, then just get list base on their tag
            if (this.sub.isEmpty() && this.obj.isEmpty()) {
                for (int i = 0; i < words.size(); i++) {
                    if (numSet.contains(postags.get(i))) {
                        this.sub.add(words.get(i));
                        this.obj.add(words.get(i));
                    } else if ("v".equals(postags.get(i)) && !this.verb.contains(words.get(i))) {
                        this.verb.add(words.get(i));
                    }
                }
            }

        } catch (TTransportException e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        } catch (TException e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        }


        _logger.info("{} - subSet = {} verbSet = {} objSet = {}",
                this.traceId, sub.toString(), verb.toString(), obj.toString());
    }

    public static void main(String[] args) {
        LTP ltp = new LTP("李克强", "haha", new HashMap<String, String>());
//        System.out.println("*****************************");
//        ltp.setSent("美联储宣布加息");
//        System.out.println("*****************************");
//        ltp.setSent("辽宁成大拟募资53.82亿布局保险业");
//        System.out.println("*****************************");
//        ltp.setSent("终止重大资产重组");
//        System.out.println("*****************************");
//        ltp.setSent("证监会决定");
//        System.out.println("*****************************");
//        ltp.setSent("AT&T收购");
//        System.out.println("*****************************");
//        ltp.setSent("攻打伊拉克");
//        System.out.println("*****************************");
//        ltp.setSent("收购国美");
//        System.out.println("*****************************");
//        ltp.setSent("青岛海尔");
//        System.out.println("*****************************");
//        ltp.setSent("新能源汽车");
//        System.out.println("*****************************");
//        ltp.setSent("美联储");
//        System.out.println("*****************************");
//        ltp.setSent("失业率");
//        System.out.println("*****************************");
//        ltp.setSent("双方有合作");
    }
}
