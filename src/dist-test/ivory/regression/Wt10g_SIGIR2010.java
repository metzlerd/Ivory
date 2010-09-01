package ivory.regression;

import static org.junit.Assert.assertEquals;
import ivory.eval.Qrels;
import ivory.eval.RankedListEvaluator;
import ivory.smrf.retrieval.Accumulator;
import ivory.smrf.retrieval.BatchQueryRunner;

import java.util.HashMap;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;
import org.junit.Test;

import edu.umd.cloud9.collection.DocnoMapping;

public class Wt10g_SIGIR2010 {

	private static final Logger sLogger = Logger.getLogger(Wt10g_SIGIR2010.class);

	private static String[] wt10g_ql_rawAP = new String[] { 
		"501","0.3504","502","0.0950","503","0.1494","504","0.4456","505","0.3715",
		"506","0.1279","507","0.2721","508","0.2428","509","0.3795","510","0.5734",
		"511","0.3559","512","0.1816","513","0.0954","514","0.1943","515","0.2135",
		"516","0.0945","517","0.0265","518","0.1751","519","0.1184","520","0.1003",
		"521","0.0201","522","0.3166","523","0.4673","524","0.0830","525","0.1283",
		"526","0.0965","527","0.4087","528","0.5203","529","0.3263","530","0.5479",
		"531","0.0154","532","0.2257","533","0.1060","534","0.0153","535","0.0438",
		"536","0.2023","537","0.0507","538","0.3095","539","0.0698","540","0.1286",
		"541","0.2878","542","0.0308","543","0.0073","544","0.5858","545","0.1833",
		"546","0.1123","547","0.1632","548","0.4167","549","0.2251","550","0.0964"};

	private static String[] wt10g_sd_rawAP = new String[] { 
		"501","0.3413","502","0.1259","503","0.1322","504","0.4283","505","0.4073",
		"506","0.1281","507","0.2723","508","0.2713","509","0.3800","510","0.6576",
		"511","0.3831","512","0.1852","513","0.0954","514","0.1857","515","0.2286",
		"516","0.0945","517","0.0182","518","0.1929","519","0.1188","520","0.0975",
		"521","0.0364","522","0.3502","523","0.4673","524","0.0773","525","0.1564",
		"526","0.0965","527","0.4779","528","0.5203","529","0.2933","530","0.5115",
		"531","0.0316","532","0.2257","533","0.1506","534","0.0127","535","0.0587",
		"536","0.2232","537","0.0550","538","0.3095","539","0.0500","540","0.1218",
		"541","0.3247","542","0.0304","543","0.0124","544","0.5718","545","0.2655",
		"546","0.1222","547","0.1540","548","0.4167","549","0.2456","550","0.0991"};

	private static String[] wt10g_wsd_sd_rawAP = new String[] { 
		"501","0.3173","502","0.1441","503","0.1311","504","0.5022","505","0.4450",
		"506","0.1285","507","0.2481","508","0.2555","509","0.3770","510","0.7147",
		"511","0.4034","512","0.1705","513","0.0954","514","0.1618","515","0.2229",
		"516","0.0945","517","0.0284","518","0.2521","519","0.1309","520","0.1085",
		"521","0.0780","522","0.3917","523","0.4673","524","0.0702","525","0.1763",
		"526","0.0965","527","0.5218","528","0.5222","529","0.3056","530","0.6282",
		"531","0.1483","532","0.2257","533","0.2270","534","0.0102","535","0.0548",
		"536","0.2290","537","0.0454","538","0.3095","539","0.0403","540","0.1068",
		"541","0.3383","542","0.0180","543","0.0112","544","0.5576","545","0.3895",
		"546","0.1398","547","0.1544","548","0.5000","549","0.2557","550","0.1030"};

	private static Qrels sQrels;
	private static DocnoMapping sMapping;

	@Test
	public void runRegression() throws Exception {

		Map<String, Map<String, Float>> AllModelsAPScores = new HashMap<String, Map<String, Float>>();

		AllModelsAPScores.put("wt10g-ql", loadScoresIntoMap(wt10g_ql_rawAP));
		AllModelsAPScores.put("wt10g-sd", loadScoresIntoMap(wt10g_sd_rawAP));
		AllModelsAPScores.put("wt10g-wsd-sd", loadScoresIntoMap(wt10g_wsd_sd_rawAP));

		sQrels = new Qrels("docs/data/wt10g/qrels.wt10g");

		String[] params = new String[] { "docs/data/wt10g/run.wt10g.SIGIR2010.xml",
				"docs/data/wt10g/wt10g_queries_501-550.xml" };

		FileSystem fs = FileSystem.getLocal(new Configuration());

		BatchQueryRunner qr = new BatchQueryRunner(params, fs);

		long start = System.currentTimeMillis();
		qr.runQueries();
		long end = System.currentTimeMillis();

		sLogger.info("Total query time: " + (end - start) + "ms");

		sMapping = qr.getDocnoMapping();

		for (String model : qr.getModels()) {
			sLogger.info("Verifying results of model \"" + model + "\"");

			Map<String, Accumulator[]> results = qr.getResults(model);

			verifyResults(model, results, AllModelsAPScores.get(model));

			sLogger.info("Done!");
		}

	}

	private static Map<String, Float> loadScoresIntoMap(String[] arr) {
		Map<String, Float> scores = new HashMap<String, Float>();
		for (int i = 0; i < arr.length; i += 2) {
			scores.put(arr[i], Float.parseFloat(arr[i + 1]));
		}

		return scores;
	}

	private static void verifyResults(String model, Map<String, Accumulator[]> results,
			Map<String, Float> apScores) {
		float apSum = 0;
		for (String qid : results.keySet()) {
			float ap = (float) RankedListEvaluator.computeAP(results.get(qid), sMapping, sQrels
					.getReldocsForQid(qid));

			apSum += ap;

			sLogger.info("verifying qid " + qid + " for model " + model);

			if (qid.trim().equals("541")){
				assertEquals(apScores.get(qid), ap, 10e-3);
			}
			else if (qid.trim().equals("547")){
				assertEquals(apScores.get(qid), ap, 10e-4);
			}
			else{
				assertEquals(apScores.get(qid), ap, 10e-6);
			}
		}

		float MAP = (float) RankedListEvaluator.roundTo4SigFigs(apSum / 50.0f);

		if (model.equals("wt10g-dir-base")) {
			assertEquals(0.2151, MAP, 10e-5);
		} else if (model.equals("wt10g-dir-sd")) {
			assertEquals(0.2242, MAP, 10e-5);
		} else if (model.equals("wt10g-wsd-sd")) {
			assertEquals(0.2411, MAP, 10e-5);
		}
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(Wt10g_SIGIR2010.class);
	}
}
