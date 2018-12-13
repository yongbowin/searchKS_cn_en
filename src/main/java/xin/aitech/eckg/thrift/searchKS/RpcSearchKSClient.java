package xin.aitech.eckg.thrift.searchKS;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import xin.aitech.eckg.thrift.searchKS.util.LoadConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-6-12
 */
public class RpcSearchKSClient {
    private static Logger _logger = LoggerFactory.getLogger(RpcSearchKSServer.class);
    private static final ResourceBundle SETTING = LoadConfig.getResourceBundle();
    private static final String SEARCHKS_RPC_ADDRESS = SETTING.getString("searchKS.rpc.address");
    private static final int SEARCHKS_RPC_PORT = Integer.parseInt(SETTING.getString("searchKS.rpc.port"));

    public static List<EventPayload> getKSResult(ReceivePayload receivePayload) throws IOException {
        List<EventPayload> eventPayload = null;
        try {
            TTransport transport = new TSocket(SEARCHKS_RPC_ADDRESS, SEARCHKS_RPC_PORT);
            transport = new TFramedTransport(transport,32768000);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            SearchKSService.Client client = new SearchKSService.Client(protocol);

            eventPayload = client.process(receivePayload);

            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException x) {
            x.printStackTrace();
        }

        return eventPayload;
    }

    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();//记录开始时间
            ReceivePayload receivePayload = new ReceivePayload(UUID.randomUUID().toString(), "cn", "特朗普", new HashMap<String, String>());
//            ReceivePayload receivePayload = new ReceivePayload(UUID.randomUUID().toString(), "cn", "十九大", new HashMap<String, String>());
//            ReceivePayload receivePayload = new ReceivePayload(UUID.randomUUID().toString(), "en", "The next generation Apple product is generated by Steve Jobs", new HashMap<String, String>());
            _logger.info(RpcSearchKSClient.getKSResult(receivePayload).toString());
            long endTime = System.currentTimeMillis();//记录结束时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            _logger.info(df.format(new Date()));// new Date()为获取当前系统时间
            float excTime = (float)(endTime - startTime) / 1000;
            _logger.info("执行时间：" + excTime + "s");
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
//        ResourceBundle rb;
//        BufferedInputStream inputStream;
//        String proFilePath = System.getProperty("user.dir").toString() + "/config/searchKS.properties";
//        try {
//            inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
//            rb = new PropertyResourceBundle(inputStream);
//            inputStream.close();
//            System.out.println(rb.getString("searchKS.rpc.sparql.en.service"));
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
}
