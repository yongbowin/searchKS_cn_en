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
import xin.aitech.eckg.thrift.nlp.NlpPayload;
import xin.aitech.eckg.thrift.nlp.NlpService;
import xin.aitech.eckg.thrift.nlp.PosNerChunk;
import xin.aitech.eckg.thrift.nlp.Srl;

import java.util.*;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-6-27
 */
public class NLP {
    private static final ResourceBundle SETTING = LoadConfig.getResourceBundle();
    private static String SEARCHKS_RPC_NLP_ADDRESS;
    private static int SEARCHKS_RPC_NLP_PORT;

    private static Logger _logger = LoggerFactory.getLogger(NLP.class);
    private String sent;
    private final String traceId;
    private List<NlpPayload> nlpPayloads;
    private List<String> sub;
    private List<String> verb;
    private List<String> obj;

    public NLP(String sent, String traceId, Map<String, String> setting) {
        this.sent = sent;
        this.traceId = traceId;
        this.sub = new ArrayList<String>();
        this.verb = new ArrayList<String>();
        this.obj = new ArrayList<String>();
        SEARCHKS_RPC_NLP_ADDRESS = setting.containsKey("searchKS_rpc_nlp_address") ? setting.get("searchKS_rpc_nlp_address")
                : SETTING.getString("searchKS.rpc.nlp.address");
        SEARCHKS_RPC_NLP_PORT = setting.containsKey("searchKS_rpc_nlp_port") ? Integer.parseInt(setting.get("searchKS_rpc_nlp_port"))
                : Integer.parseInt(SETTING.getString("searchKS.rpc.nlp.port"));
        this.processSent();

        _logger.info("{} - Initialize LTP sent on address {} - {}: {}", this.traceId, SEARCHKS_RPC_NLP_ADDRESS, SEARCHKS_RPC_NLP_PORT, this.sent);
    }

    private void processSent() {
        this.nlpPayloads = null;
        try {
            TTransport transport = new TSocket(SEARCHKS_RPC_NLP_ADDRESS, SEARCHKS_RPC_NLP_PORT);
            transport = new TFramedTransport(transport);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            NlpService.Client client = new NlpService.Client(protocol);

            this.nlpPayloads = client.process(this.sent, this.traceId);

            transport.close();
        } catch (TTransportException e) {
            _logger.error(e.getMessage(),e);
            e.printStackTrace();
        } catch (TException e) {
            _logger.error(e.getMessage(),e);
            e.printStackTrace();
        } catch (Exception e) {
            _logger.error(e.getMessage(),e);
            e.printStackTrace();
        }

//        System.out.println(nlpPayloads.get(0).getWords().toString());
//        System.out.println(nlpPayloads.get(0).getPos().toString());
//        System.out.println(nlpPayloads.get(0).getNer().toString());
//        System.out.println(nlpPayloads.get(0).getChunk().toString());
//        System.out.println(nlpPayloads.get(0).getVerbs().toString());
//        System.out.println(nlpPayloads.get(0).getSrl().toString());
//        System.out.println(concatanateChunk(nlpPayloads.get(0).getChunk()).toString());
        parseNlpPayloads();
    }

    private void parseNlpPayloads() {
        List<String> chunkList = concatanateChunk(this.nlpPayloads.get(0).getChunk());

        if (this.nlpPayloads.get(0).getSrl().size() == 0) {
            this.sub = chunkList;
            this.verb = this.nlpPayloads.get(0).getVerbs();
            this.obj = chunkList;
        } else {
            for (Srl srl : this.nlpPayloads.get(0).getSrl()) {
                this.sub.add(srl.a0);
                this.verb.add(srl.v);
                this.obj.add(srl.a1);
            }
        }

        _logger.info("{} - sub: {}", this.traceId, this.sub.toString());
        _logger.info("{} - verb: {}", this.traceId, this.verb.toString());
        _logger.info("{} - obj: {}", this.traceId, this.obj.toString());
    }

    private List<String> concatanateChunk(List<PosNerChunk> chunks) {
        List<String> res = new ArrayList<String>();
        StringBuilder stringBuilder = new StringBuilder();

        for (PosNerChunk chunk : chunks) {
            if (chunk.getTag().equals("B-NP")) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(chunk.getWord());
            } else if (chunk.getTag().equals("I-NP")) {
                stringBuilder.append(" ");
                stringBuilder.append(chunk.getWord());
            } else if (chunk.getTag().equals("E-NP")) {
                stringBuilder.append(" ");
                stringBuilder.append(chunk.getWord());
                res.add(stringBuilder.toString());
            } else if (chunk.getTag().equals("S-NP")) {
                res.add(chunk.getWord());
            }
        }

        return res;
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

    public static void main(String[] args) {
//        NLP nlp = new NLP("Apple debt raises", UUID.randomUUID().toString());
//        NLP nlp = new NLP("Carl Icahn Apple investment", UUID.randomUUID().toString());
        NLP nlp = new NLP("investor expect bailout", UUID.randomUUID().toString(), new HashMap<String, String>());
    }
}
