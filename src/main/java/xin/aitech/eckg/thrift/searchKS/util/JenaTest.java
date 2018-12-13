package xin.aitech.eckg.thrift.searchKS.util;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

import java.util.HashMap;

/**
 * @Description:
 * @author: Hugo Ng
 * @Date: 17-6-12
 */
public class JenaTest {
    public static void main(String[] args) {
        String sparqlService = "http://192.168.100.249:50053/sparql";
        String queryString =
                "PREFIX skos:  <http://www.w3.org/2004/02/skos/core#>" +
                        "PREFIX nwrontology: <http://www.newsreader-project.eu/ontologies/>" +
                        "PREFIX pb:    <http://www.newsreader-project.eu/ontologies/propbank/>" +
                        "PREFIX sem:   <http://semanticweb.cs.vu.nl/2009/11/sem/>" +
                        "PREFIX time:  <http://www.w3.org/TR/owl-time#>" +
                        "select distinct ?s_label ?s ?p ?p_label ?o ?o_label ?time" +
                        "WHERE" +
                        "{" +
                        "{?s a nwrontology:O} UNION {?s a nwrontology:GPE} UNION {?s a nwrontology:ORG} UNION {?s a nwrontology:PERSON}." +
                        "{?o a nwrontology:O} UNION {?o a nwrontology:GPE} UNION {?o a nwrontology:ORG} UNION {?o a nwrontology:PERSON}." +
                        "?s skos:prefLabel ?s_label. " +
                        "?o skos:prefLabel ?o_label." +
                        "?p a sem:Event." +
                        "?p skos:prefLabel ?p_label." +
                        "?p sem:hasActor ?s." +
                        "?p pb:A0 ?s." +
                        "?p pb:A1 ?o." +
                        "?p sem:hasTime ?t." +
                        "?t time:inDateTime ?time." +
                        "FILTER ( regex(?s_label, '英国'))" +
                        "} limit 100";
        GenerateQuery generateQuery = new GenerateQuery("英国欧盟", "cn", "haha", new HashMap<String, String>());
//        String queryStr = generateQuery.getQueryStr();
//        ResultSet results = QueryProcessor.queryProcess(queryStr, "cn", new HashMap<String, String>());
//
//        for (; results.hasNext(); ) {
//            QuerySolution soln = results.nextSolution();
//            RDFNode oUri = soln.get("s");
//            Literal ontUri = soln.getLiteral("s_label");
//            Literal name = soln.getLiteral("p_label");
//            Literal acr = soln.getLiteral("o_label");
//            System.out.println(oUri + " ---- " + ontUri + " ---- " + name + " ---- " + acr);
//        }
    }
}
