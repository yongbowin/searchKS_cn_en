package xin.aitech.eckg.thrift.searchKS.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.aitech.eckg.thrift.word2vector.RpcW2vClient;
import xin.aitech.eckg.thrift.word2vector.similarWords;
import xin.aitech.eckg.thrift.word2vector.w2vPayload;

import java.io.IOException;
import java.util.*;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-6-12
 */
public class GenerateQuery {
    private static Logger _logger = LoggerFactory.getLogger(GenerateQuery.class);
    private static final ResourceBundle SETTING = LoadConfig.getResourceBundle();
    private static String SEARCHKS_RPC_SPARQL_CN_TEMPLACE_WITHOUT_VERB = SETTING.getString("searchKS.rpc.sparql.cn.template_without_verb");
    private static String SEARCHKS_RPC_SPARQL_CN_TEMPLACE = SETTING.getString("searchKS.rpc.sparql.cn.template");
    private static String SEARCHKS_RPC_SPARQL_EN_TEMPLACE_WITHOUT_VERB = SETTING.getString("searchKS.rpc.sparql.en.template_without_verb");
    private static String SEARCHKS_RPC_SPARQL_EN_TEMPLACE = SETTING.getString("searchKS.rpc.sparql.en.template");

    private final String content;
    private final String language;
    private final String traceId;
    private final Map<String, String> setting;
    private LTP ltp;
    private NLP nlp;
    private String queryStrCn;
    private String queryStrCn_without_verb;
    private String queryStrEn;
    private boolean with_verb = true;

    public GenerateQuery(String content, String language, String traceId, Map<String, String> setting) {
        this.content = content;
        this.language = language;
        this.traceId = traceId;
        this.setting = setting;
        if (this.language.equals("cn")) {
            processContentCn();
            generateQueryCn();
        } else if (this.language.equals("en")) {
            processContentEn();
            generateQueryEn();
        } else {
            _logger.error("{} - Didn't specific language parameter or specific in the wrong way.", this.traceId);
        }
    }

    private void processContentCn() {
        // check input is null or empty
        if (this.content == null || this.content.equals("")) {
            _logger.error("{} - The sentence is null or empty", this.traceId);
        }

        // process input using ltp(Segmentation, POS, DP, SRL)
        this.ltp = new LTP(this.content, this.traceId, this.setting);
    }

    private void processContentEn() {
        // check input is null or empty
        if (this.content == null || this.content.equals("")) {
            _logger.error("{} - The sentence is null or empty", this.traceId);
        }

        // process input using ltp(Segmentation, POS, DP, SRL)
        this.nlp = new NLP(this.content, this.traceId, this.setting);
    }

    private void generateQueryCn() {
        String subStr;
        String verbStr;
        String objStr;
        List<similarWords> w2vVerbList = new ArrayList<similarWords>();
        Set w2vVerbSet = new HashSet();
        List subList = ltp.getSub();
        List objList = ltp.getObj();

        // w2v rpc
        if (ltp.getVerb().size() > 0) {
            w2vVerbList = w2v(ltp.getVerb(), 5, "cn");
        }

        for (similarWords simWords : w2vVerbList) {
            w2vVerbSet.addAll(simWords.getWords());
        }

        List<String> w2vVerbSetTemp = new ArrayList<String>(w2vVerbSet);

        if (subList.size() >= 1) {
            subStr = String.format("{?s_label bif:contains '%s' .}", list2str(subList, "\"%s\"", "and"));
        } else {
            subStr = new String("");
        }

        if (w2vVerbSetTemp.size() >= 1) {
            verbStr = String.format("{?v_label bif:contains '%s' .}", list2str(w2vVerbSetTemp, "\"%s\"", "or"));
        } else {
            verbStr = new String("");
        }

        if (objList.size() >= 1) {
            objStr = String.format("{?o_label bif:contains '%s' .}", list2str(objList, "\"%s\"", "and"));
        } else {
            objStr = new String("");
        }

        _logger.info("subStr: " + subStr);
        _logger.info("verbStr: " + verbStr);
        _logger.info("objStr: " + objStr);

        if ((subStr == null || subStr.equals("")) && (verbStr == null || verbStr.equals("")) && (objStr == null || objStr.equals(""))) {
            this.queryStrCn_without_verb = null;
            this.with_verb = false;
            _logger.info("{} - The query string: {}", this.traceId, this.queryStrCn_without_verb);
        } else if (verbStr == null || verbStr.equals("")) {
            this.queryStrCn_without_verb = String.format(SEARCHKS_RPC_SPARQL_CN_TEMPLACE_WITHOUT_VERB, subStr, objStr);
            this.with_verb = false;
            _logger.info("{} - The query string: {}", this.traceId, this.queryStrCn_without_verb);
        } else {
            this.queryStrCn = String.format(SEARCHKS_RPC_SPARQL_CN_TEMPLACE, subStr, verbStr, objStr);
//            if (subStr == null || subStr.equals("")) {
//                subStr = String.format("{?s_label bif:contains '%s' .}", list2str(objList, "\"%s\"", "and"));
//            } else if (objStr == null || objStr.equals("")) {
//                objStr = String.format("{?o_label bif:contains '%s' .}", list2str(subList, "\"%s\"", "and"));
//            }
            this.queryStrCn_without_verb = String.format(SEARCHKS_RPC_SPARQL_CN_TEMPLACE_WITHOUT_VERB, subStr, objStr);
            this.with_verb = true;
            _logger.info("{} - The query string: {}", this.traceId, this.queryStrCn);
        }
    }

    private void generateQueryEn() {
        String subStr;
        String verbStr;
        String objStr;
        List<similarWords> w2vVerbList = new ArrayList<similarWords>();
        Set w2vVerbSet = new HashSet();

        if (this.nlp.getSub().size() > 1) {
            subStr = String.format("FILTER (%s)", list2str(this.nlp.getSub(), "regex(?s_label, '%s')", "||"));
        } else if (this.nlp.getSub().size() == 1) {
            subStr = String.format("FILTER (%s)", String.format("regex(?s_label, '%s')", this.nlp.getSub().get(0)));
        } else {
            subStr = new String("");
        }

        if (nlp.getVerb().size() > 0) {
            w2vVerbList = w2v(nlp.getVerb(), 5, "en");
        }

        for (similarWords simWords : w2vVerbList) {
            w2vVerbSet.addAll(simWords.getWords());
        }

        List<String> w2vVerbSetTemp = new ArrayList<String>(w2vVerbSet);

        if (w2vVerbSetTemp.size() > 1) {
            verbStr = String.format("FILTER (%s)", list2str(w2vVerbSetTemp, "regex(?v_label, '%s')", "||"));
        } else if (w2vVerbSetTemp.size() == 1) {
            verbStr = String.format("FILTER (%s)", String.format("regex(?v_label, '%s')", w2vVerbSetTemp.get(0)));
        } else {
            verbStr = new String("");
        }

        if (this.nlp.getObj().size() > 1) {
            objStr = String.format("FILTER (%s)", list2str(nlp.getObj(), "regex(?o_label, '%s')", "||"));
        } else if (this.nlp.getObj().size() == 1) {
            objStr = String.format("FILTER (%s)", String.format("regex(?o_label, '%s')", this.nlp.getObj().get(0)));
        } else {
            objStr = new String("");
        }

        _logger.info("subStr: " + subStr);
        _logger.info("verbStr: " + verbStr);
        _logger.info("objStr: " + objStr);

        if (verbStr == null || verbStr.equals("")) {
            this.queryStrEn = String.format(SEARCHKS_RPC_SPARQL_EN_TEMPLACE_WITHOUT_VERB, subStr, objStr);
        } else {
            this.queryStrEn = String.format(SEARCHKS_RPC_SPARQL_EN_TEMPLACE, subStr, verbStr, objStr);
        }

        _logger.info("{} - The query string: {}", this.traceId, this.queryStrEn);
    }

    public boolean getWithVerb() {
        return this.with_verb;
    }

    public String getQueryStrCn() {
        return this.queryStrCn;
    }

    public String getQueryStrCn_without_verb() {
        return this.queryStrCn_without_verb;
    }

    private List<similarWords> w2v(List<String> strList, int topn, String language) {
        List<similarWords> result = new ArrayList<similarWords>();

        try {
            w2vPayload lyn = new w2vPayload();
            lyn.words = strList;
            lyn.topn = topn;
            lyn.language = language;
            result = RpcW2vClient.getKSResult(lyn);
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    private String list2str(List<String> strList, String strFormat, String separator) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < strList.size(); i++) {
            sb.append(String.format(strFormat, strList.get(i)));
            if (i < strList.size() - 1) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        new GenerateQuery("特朗普", "cn", "haha", new HashMap<String, String>());
//        new GenerateQuery("dollar", "en", "haha", new HashMap<String, String>()).getQueryStr();
//        new GenerateQuery("The next generation Apple product is generated by Steve Jobs", "en", "haha", new HashMap<String, String>()).getQueryStr();
    }
}
