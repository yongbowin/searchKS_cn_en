package xin.aitech.eckg.thrift.searchKS;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.aitech.eckg.thrift.searchKS.util.LoadConfig;

import java.net.InetSocketAddress;
import java.util.ResourceBundle;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-6-12
 */
public class RpcSearchKSServer {
    private static Logger _logger = LoggerFactory.getLogger(RpcSearchKSServer.class);
    private static final ResourceBundle SETTING = LoadConfig.getResourceBundle();
    private static String SEARCHKS_RPC_ADDRESS = SETTING.getString("searchKS.rpc.address");
    private static int SEARCHKS_RPC_PORT = Integer.parseInt(SETTING.getString("searchKS.rpc.port"));
    private static int SEARCHKS_RPC_SELECTOR_THREAD = Integer.parseInt(SETTING.getString("searchKS.rpc.thrift.selectorThreads"));
    private static int SEARCHKS_RPC_WORKER_THREAD = Integer.parseInt(SETTING.getString("searchKS.rpc.thrift.workerThreads"));

    public static void StartServer(SearchKSService.Processor<SearchKSServerHandler> processor) {
        try {
            // non-blocking
            TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(new InetSocketAddress(SEARCHKS_RPC_ADDRESS, SEARCHKS_RPC_PORT));

            // multi-thread half-sync/half-async service model
            TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(serverTransport);
            args.processor(processor);
            args.selectorThreads(SEARCHKS_RPC_SELECTOR_THREAD);
            args.workerThreads(SEARCHKS_RPC_WORKER_THREAD);
            args.transportFactory(new TFramedTransport.Factory(32768000));

            // binary protocol
            args.protocolFactory(new TBinaryProtocol.Factory());

            // Use this for a server
            TServer server = new TThreadedSelectorServer(args);

            _logger.info("Starting the server at: {}:{} ...", SEARCHKS_RPC_ADDRESS, SEARCHKS_RPC_PORT);
            server.serve();
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        _logger.info(System.getProperty("user.dir").toString() + "/config/searchKS.properties");
        StartServer(new SearchKSService.Processor<SearchKSServerHandler>(new SearchKSServerHandler()));
    }
}
