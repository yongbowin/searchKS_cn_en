package xin.aitech.eckg.thrift.searchKS.util;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-6-12
 */
public class QueryProcessor {
    private static final Logger _logger = LoggerFactory.getLogger(QueryProcessor.class);
    private static final ResourceBundle SETTING = LoadConfig.getResourceBundle();
    private static String SEARCHKS_RPC_SPARQL_CN_SERVICE;
    private static String SEARCHKS_RPC_SPARQL_CN_SERVICE_DAILYNEWS;
    private static String SEARCHKS_RPC_SPARQL_EN_SERVICE;

    public static ResultSet queryProcess(String queryString, String language, Map<String, String> setting) {

        long startTime = System.currentTimeMillis();
        _logger.info("QueryProcessor.queryProcess()---start;[queryString]:"+queryString);

        SEARCHKS_RPC_SPARQL_CN_SERVICE = setting.containsKey("searchKS_rpc_sparql_cn_service") ? setting.get("searchKS_rpc_sparql_cn_service")
                : SETTING.getString("searchKS.rpc.sparql.cn.service");
        SEARCHKS_RPC_SPARQL_EN_SERVICE = setting.containsKey("searchKS_rpc_sparql_en_service") ? setting.get("searchKS_rpc_sparql_en_service")
                : SETTING.getString("searchKS.rpc.sparql.en.service");

        Query query = QueryFactory.create(queryString);
        QueryEngineHTTP qexec = null;
        if (language.equals("cn")) {
            qexec = QueryExecutionFactory.createServiceRequest(SEARCHKS_RPC_SPARQL_CN_SERVICE, query);
        } else {
            qexec = QueryExecutionFactory.createServiceRequest(SEARCHKS_RPC_SPARQL_EN_SERVICE, query);
        }
        ResultSet resultSet = qexec.execSelect();

        long endTime = System.currentTimeMillis();//记录结束时间
        float excTime = (float)(endTime - startTime) / 1000;
        _logger.info("QueryProcessor.queryProcess()---end;[执行时间]：" + excTime + "s");

        return resultSet;
    }

    public static ResultSet queryProcessDailynews(String queryString, String language, Map<String, String> setting) {
        SEARCHKS_RPC_SPARQL_CN_SERVICE_DAILYNEWS = setting.containsKey("searchKS_rpc_sparql_cn_service_dailynews") ? setting.get("searchKS_rpc_sparql_cn_service_dailynews")
                : SETTING.getString("searchKS.rpc.sparql.cn.service.dailynews");

        Query query = QueryFactory.create(queryString);
        QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest(SEARCHKS_RPC_SPARQL_CN_SERVICE_DAILYNEWS, query);
        ResultSet resultSet = qexec.execSelect();

        return resultSet;
    }

    public static void main(String[] args) {
//        try {
//            URL url = new URL(SEARCHKS_RPC_SPARQL_CN_SERVICE);
//            System.out.println(url.getHost());
//            InetAddress address = InetAddress.getByName(url.getHost());
//            if (address.isReachable(5000)) {
//                System.out.println("SUCCESS - ping " + SEARCHKS_RPC_SPARQL_CN_SERVICE + " with no interface specified");
//            } else {
//                System.out.println("FAILURE - ping " + SEARCHKS_RPC_SPARQL_CN_SERVICE + " with no interface specified");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        long startTime = System.currentTimeMillis();//记录开始时间
        GenerateQuery generateQuery = new GenerateQuery("特朗普", "cn", "haha", new HashMap<String, String>());
        String queryStr = generateQuery.getQueryStrCn_without_verb();
        _logger.info("queryStr: " + queryStr);
        ResultSet results = QueryProcessor.queryProcess(queryStr, "cn", new HashMap<String, String>());
        long endTime = System.currentTimeMillis();//记录结束时间
        float excTime = (float)(endTime - startTime) / 1000;
        _logger.info("执行时间：" + excTime + "s");
    }
}
