package xin.aitech.eckg.thrift.searchKS;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.aitech.eckg.thrift.searchKS.util.GenerateQuery;
import xin.aitech.eckg.thrift.searchKS.util.QueryProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-6-12
 */
public class SearchKSServerHandler implements SearchKSService.Iface {
    private static Logger _logger = LoggerFactory.getLogger(SearchKSServerHandler.class);

    @Override
    public List<EventPayload> process(ReceivePayload receivePayload) throws TException {
        String traceId = receivePayload.getTraceId();
        String language = receivePayload.getLanguage();
        String content = receivePayload.getContent();
        Map<String, String> setting = receivePayload.getSettingVar();
        List<EventPayload> eventPayloads = new ArrayList<EventPayload>();
        _logger.info("{} - {} - Received request: {}", traceId, language, content);
        if (language.equals("cn") || language.equals("en")) {
            GenerateQuery generateQuery = new GenerateQuery(content, language, traceId, setting);
            boolean with_verb = generateQuery.getWithVerb();
            if (with_verb) {
                String queryStrCn = generateQuery.getQueryStrCn();
                eventPayloads = queryKS(queryStrCn, language, setting, traceId);
                _logger.info("{} - {} - # 1 Finished processing: {}", traceId, language, eventPayloads.size());
//                ResultSet results = QueryProcessor.queryProcess(queryStrCn, language, setting);
//                _logger.info("{} - Finished query from KS", traceId);
//                if (results != null) {
//                    while (results.hasNext()) {
//                        EventPayload eventPayload = new EventPayload();
//                        QuerySolution row = results.nextSolution();
////                        eventPayload.subjectUrl = row.get("s_url").toString();
//                        eventPayload.subjectUrl = new String("");
//                        eventPayload.subjectLabel = row.getLiteral("s_label").toString();
//                        eventPayload.verbUrl = row.get("v_url").toString();
//                        eventPayload.verbLabel = row.getLiteral("v_label").toString();
////                        eventPayload.objectUrl = row.get("o_url").toString();
//                        eventPayload.objectUrl = new String("");
//                        eventPayload.objectLabel = row.getLiteral("o_label").toString();
//                        eventPayload.inDateTimeUrl = row.contains("t_inDateTime") ? row.get("t_inDateTime").toString() : "";
//                        eventPayload.beginningTimeUrl = row.contains("t_beginning") ? row.get("t_beginning").toString() : "";
//                        eventPayload.endTimeUrl = row.contains("t_end") ? row.get("t_end").toString() : "";
//                        eventPayloads.add(eventPayload);
//                    }
                if (eventPayloads.size() == 0) {
                    String queryStrCn_without_verb = generateQuery.getQueryStrCn_without_verb();
                    eventPayloads = queryKS(queryStrCn_without_verb, language, setting, traceId);
                }
                _logger.info("{} - {} - # 2 Finished processing: {}", traceId, language, eventPayloads.size());
            } else {
                String queryStrCn_without_verb = generateQuery.getQueryStrCn_without_verb();
                if (queryStrCn_without_verb != null) {
                    eventPayloads = queryKS(queryStrCn_without_verb, language, setting, traceId);
                }
                _logger.info("{} - {} - Finished processing: {}", traceId, language, eventPayloads.size());
            }
        } else {
            _logger.error("{} - Please specific language type in a particular way(cn, en)", traceId);
        }
        return eventPayloads;
    }

    private List<EventPayload> queryKS(String queryStr, String language, Map<String, String> setting, String traceId) {
        List<EventPayload> eventPayloads = new ArrayList<EventPayload>();
        ResultSet results = QueryProcessor.queryProcess(queryStr, language, setting);
        _logger.info("{} - Finished query from KS", traceId);
        if (results != null) {
            while (results.hasNext()) {
                EventPayload eventPayload = new EventPayload();
                QuerySolution row = results.nextSolution();
//                        eventPayload.subjectUrl = row.get("s_url").toString();
                eventPayload.subjectUrl = new String("");
                eventPayload.subjectLabel = row.getLiteral("s_label").toString();
                eventPayload.verbUrl = row.get("v_url").toString();
                eventPayload.verbLabel = row.getLiteral("v_label").toString();
//                        eventPayload.objectUrl = row.get("o_url").toString();
                eventPayload.objectUrl = new String("");
                eventPayload.objectLabel = row.getLiteral("o_label").toString();
                eventPayload.inDateTimeUrl = row.contains("t_inDateTime") ? row.get("t_inDateTime").toString() : "";
                eventPayload.beginningTimeUrl = row.contains("t_beginning") ? row.get("t_beginning").toString() : "";
                eventPayload.endTimeUrl = row.contains("t_end") ? row.get("t_end").toString() : "";
                eventPayloads.add(eventPayload);
            }
        }
        return eventPayloads;
    }
}
