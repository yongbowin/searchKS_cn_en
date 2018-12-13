package xin.aitech.eckg.thrift.word2vector;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.aitech.eckg.thrift.searchKS.util.LoadConfig;
import xin.aitech.eckg.thrift.searchKS.util.QueryProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-6-13
 */
public class RpcW2vClient {
    private static final Logger _logger = LoggerFactory.getLogger(RpcW2vClient.class);

    private static final ResourceBundle SETTING = LoadConfig.getResourceBundle();
    private static final String WORD2VECTOR_RPC_ADDRESS = SETTING.getString("word2vector.rpc.address");
    private static final int WORD2VECTOR_RPC_PORT = Integer.parseInt(SETTING.getString("word2vector.rpc.port"));

    public static List<similarWords> getKSResult(w2vPayload content) throws IOException {
        List<similarWords> similarWordsList = null;
        try {
            _logger.info(WORD2VECTOR_RPC_ADDRESS + "-" + WORD2VECTOR_RPC_PORT);
            TTransport transport = new TSocket(WORD2VECTOR_RPC_ADDRESS, WORD2VECTOR_RPC_PORT);
            transport = new TFramedTransport(transport);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            w2vCnEnService.Client client = new w2vCnEnService.Client(protocol);

            similarWordsList = client.process(content);

            transport.close();
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

        return similarWordsList;
    }

    public static void main(String[] args) {
        try {
            w2vPayload lyn = new w2vPayload();
            lyn.words = new ArrayList<String>();
            lyn.words.add("工程师");
            lyn.words.add("国王");
            lyn.topn = 5;
            lyn.language = "cn";
            _logger.info(String.valueOf(RpcW2vClient.getKSResult(lyn)));


            w2vPayload lynen = new w2vPayload();
            lynen.words = new ArrayList<String>();
            lynen.words.add("america");
            lynen.words.add("china");
            lynen.topn = 5;
            lynen.language = "en";
            _logger.info(String.valueOf(RpcW2vClient.getKSResult(lynen)));
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
