package ivory.regression.basic;

import ivory.core.eval.Qrels;
import ivory.regression.GroundTruth;
import ivory.regression.GroundTruth.Metric;
import ivory.smrf.retrieval.Accumulator;
import ivory.smrf.retrieval.BatchQueryRunner;

import java.util.Map;
import java.util.Set;

import junit.framework.JUnit4TestAdapter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.common.collect.Maps;

import edu.umd.cloud9.collection.DocnoMapping;

public class Gov2_Basic {
  private static final Logger LOG = Logger.getLogger(Gov2_Basic.class);

  private static String[] sDirBaseRawAP = new String[] {
    "701", "0.1037", "702", "0.0562", "703", "0.0000", "704", "0.2736", "705", "0.2713",
    "706", "0.1503", "707", "0.4268", "708", "0.2231", "709", "0.7930", "710", "0.4838",
    "711", "0.1072", "712", "0.5719", "713", "0.3321", "714", "0.1248", "715", "0.1467",
    "716", "0.0791", "717", "0.3586", "718", "0.3391", "719", "0.0740", "720", "0.2969",
    "721", "0.0483", "722", "0.4724", "723", "0.1220", "724", "0.2018", "725", "0.2755",
    "726", "0.5275", "727", "0.4602", "728", "0.4438", "729", "0.0015", "730", "0.5210",
    "731", "0.1218", "732", "0.2384", "733", "0.3154", "734", "0.2396", "735", "0.2312",
    "736", "0.5789", "737", "0.5672", "738", "0.4744", "739", "0.1212", "740", "0.2608",
    "741", "0.0939", "742", "0.3503", "743", "0.0333", "744", "0.0157", "745", "0.4999",
    "746", "0.4649", "747", "0.1050", "748", "0.2025", "749", "0.2929", "750", "0.0593",
    "751", "0.3576", "752", "0.4960", "753", "0.3361", "754", "0.0887", "755", "0.4876",
    "756", "0.3338", "757", "0.2148", "758", "0.6835", "759", "0.2145", "760", "0.3245",
    "761", "0.4779", "762", "0.0204", "763", "0.1732", "764", "0.1724", "765", "0.4796",
    "766", "0.5336", "767", "0.4826", "768", "0.1242", "769", "0.0114", "770", "0.3710",
    "771", "0.5266", "772", "0.3892", "773", "0.5778", "774", "0.3555", "775", "0.1019",
    "776", "0.2608", "777", "0.3142", "778", "0.1082", "779", "0.4978", "780", "0.3421",
    "781", "0.3598", "782", "0.6516", "783", "0.2095", "784", "0.4571", "785", "0.4402",
    "786", "0.4279", "787", "0.5753", "788", "0.5235", "789", "0.2194", "790", "0.5108",
    "791", "0.4538", "792", "0.1647", "793", "0.2988", "794", "0.0645", "795", "0.0246",
    "796", "0.1733", "797", "0.6315", "798", "0.1932", "799", "0.1891", "800", "0.1998",
    "801", "0.4514", "802", "0.4094", "803", "0.0000", "804", "0.4933", "805", "0.0443",
    "806", "0.0141", "807", "0.5632", "808", "0.7256", "809", "0.3118", "810", "0.3130",
    "811", "0.2744", "812", "0.5479", "813", "0.3021", "814", "0.7217", "815", "0.1544",
    "816", "0.7935", "817", "0.3378", "818", "0.2183", "819", "0.5572", "820", "0.7611",
    "821", "0.2489", "822", "0.1110", "823", "0.3301", "824", "0.3342", "825", "0.0533",
    "826", "0.2922", "827", "0.4429", "828", "0.2497", "829", "0.0922", "830", "0.0335",
    "831", "0.6072", "832", "0.1760", "833", "0.5381", "834", "0.3873", "835", "0.0484",
    "836", "0.2219", "837", "0.0788", "838", "0.2846", "839", "0.5712", "840", "0.1665",
    "841", "0.3718", "842", "0.1087", "843", "0.4271", "844", "0.0504", "845", "0.3979",
    "846", "0.1982", "847", "0.3106", "848", "0.1165", "849", "0.2089", "850", "0.2177" };

  private static String[] sDirBaseRawP10 = new String[] {
    "701", "0.4000", "702", "0.2000", "703", "0.0000", "704", "0.5000", "705", "0.4000",
    "706", "0.1000", "707", "0.9000", "708", "0.8000", "709", "0.9000", "710", "0.8000",
    "711", "0.3000", "712", "0.9000", "713", "0.5000", "714", "0.2000", "715", "0.2000",
    "716", "0.1000", "717", "0.9000", "718", "1.0000", "719", "0.4000", "720", "0.6000",
    "721", "0.1000", "722", "1.0000", "723", "0.2000", "724", "0.3000", "725", "0.5000",
    "726", "0.9000", "727", "0.7000", "728", "0.8000", "729", "0.0000", "730", "0.8000",
    "731", "0.1000", "732", "0.4000", "733", "0.8000", "734", "0.4000", "735", "0.6000",
    "736", "1.0000", "737", "0.9000", "738", "1.0000", "739", "0.6000", "740", "0.4000",
    "741", "0.0000", "742", "0.6000", "743", "0.1000", "744", "0.0000", "745", "0.4000",
    "746", "1.0000", "747", "0.9000", "748", "0.2000", "749", "0.7000", "750", "0.1000",
    "751", "0.5000", "752", "1.0000", "753", "0.6000", "754", "0.4000", "755", "0.7000",
    "756", "0.5000", "757", "0.5000", "758", "1.0000", "759", "0.5000", "760", "0.7000",
    "761", "1.0000", "762", "0.1000", "763", "0.5000", "764", "0.4000", "765", "1.0000",
    "766", "1.0000", "767", "0.7000", "768", "0.5000", "769", "0.1000", "770", "0.8000",
    "771", "0.8000", "772", "0.9000", "773", "1.0000", "774", "0.8000", "775", "0.5000",
    "776", "0.3000", "777", "0.5000", "778", "0.4000", "779", "0.9000", "780", "0.7000",
    "781", "0.7000", "782", "0.8000", "783", "0.5000", "784", "0.2000", "785", "0.9000",
    "786", "0.8000", "787", "1.0000", "788", "0.9000", "789", "0.4000", "790", "0.9000",
    "791", "0.7000", "792", "0.6000", "793", "0.5000", "794", "0.1000", "795", "0.0000",
    "796", "0.8000", "797", "1.0000", "798", "0.5000", "799", "0.2000", "800", "0.0000",
    "801", "0.9000", "802", "0.6000", "803", "0.0000", "804", "0.6000", "805", "0.0000",
    "806", "0.2000", "807", "1.0000", "808", "1.0000", "809", "0.9000", "810", "0.3000",
    "811", "0.7000", "812", "0.7000", "813", "1.0000", "814", "0.8000", "815", "0.2000",
    "816", "0.9000", "817", "0.9000", "818", "0.6000", "819", "1.0000", "820", "0.6000",
    "821", "0.4000", "822", "0.2000", "823", "0.7000", "824", "0.9000", "825", "0.1000",
    "826", "0.3000", "827", "0.5000", "828", "0.9000", "829", "0.4000", "830", "0.1000",
    "831", "0.9000", "832", "0.5000", "833", "0.8000", "834", "0.6000", "835", "0.0000",
    "836", "0.2000", "837", "0.1000", "838", "0.8000", "839", "0.9000", "840", "0.1000",
    "841", "0.9000", "842", "0.0000", "843", "0.8000", "844", "0.1000", "845", "0.7000",
    "846", "0.8000", "847", "0.6000", "848", "0.5000", "849", "0.5000", "850", "0.3000" };

  private static String[] sDirSDRawAP = new String[] {
    "701", "0.1334", "702", "0.0725", "703", "0.0000", "704", "0.3658", "705", "0.2838",
    "706", "0.1247", "707", "0.4357", "708", "0.2128", "709", "0.7952", "710", "0.4903",
    "711", "0.1790", "712", "0.5709", "713", "0.3537", "714", "0.1493", "715", "0.1512",
    "716", "0.0772", "717", "0.3493", "718", "0.3234", "719", "0.1005", "720", "0.2787",
    "721", "0.0524", "722", "0.4778", "723", "0.2790", "724", "0.2062", "725", "0.3181",
    "726", "0.5028", "727", "0.4368", "728", "0.3845", "729", "0.0012", "730", "0.5265",
    "731", "0.1166", "732", "0.3127", "733", "0.3135", "734", "0.2373", "735", "0.2193",
    "736", "0.5666", "737", "0.5546", "738", "0.5348", "739", "0.3366", "740", "0.2736",
    "741", "0.0984", "742", "0.4078", "743", "0.0286", "744", "0.0191", "745", "0.4894",
    "746", "0.4059", "747", "0.1393", "748", "0.3314", "749", "0.3346", "750", "0.1527",
    "751", "0.3715", "752", "0.5837", "753", "0.2958", "754", "0.1165", "755", "0.4764",
    "756", "0.3255", "757", "0.2148", "758", "0.6949", "759", "0.2485", "760", "0.3232",
    "761", "0.4795", "762", "0.0259", "763", "0.2332", "764", "0.2040", "765", "0.4434",
    "766", "0.5451", "767", "0.4765", "768", "0.1224", "769", "0.0254", "770", "0.3361",
    "771", "0.5500", "772", "0.4697", "773", "0.5257", "774", "0.3827", "775", "0.1033",
    "776", "0.2516", "777", "0.2833", "778", "0.1522", "779", "0.4696", "780", "0.3818",
    "781", "0.3296", "782", "0.6560", "783", "0.2324", "784", "0.4571", "785", "0.5624",
    "786", "0.4286", "787", "0.5695", "788", "0.5757", "789", "0.2228", "790", "0.4777",
    "791", "0.4256", "792", "0.1689", "793", "0.3365", "794", "0.1415", "795", "0.0257",
    "796", "0.1733", "797", "0.6027", "798", "0.1720", "799", "0.1351", "800", "0.1943",
    "801", "0.4518", "802", "0.4829", "803", "0.0003", "804", "0.5113", "805", "0.0688",
    "806", "0.0690", "807", "0.5949", "808", "0.6908", "809", "0.3288", "810", "0.3806",
    "811", "0.3021", "812", "0.5988", "813", "0.3097", "814", "0.7128", "815", "0.1622",
    "816", "0.7636", "817", "0.4217", "818", "0.2115", "819", "0.5763", "820", "0.7706",
    "821", "0.3505", "822", "0.0938", "823", "0.4169", "824", "0.3294", "825", "0.1074",
    "826", "0.3519", "827", "0.4385", "828", "0.3407", "829", "0.1291", "830", "0.0745",
    "831", "0.6338", "832", "0.1715", "833", "0.4465", "834", "0.3916", "835", "0.1256",
    "836", "0.2195", "837", "0.0680", "838", "0.2533", "839", "0.5949", "840", "0.1665",
    "841", "0.3948", "842", "0.1744", "843", "0.4942", "844", "0.0837", "845", "0.3752",
    "846", "0.1997", "847", "0.2352", "848", "0.1117", "849", "0.3618", "850", "0.2083" };

  private static String[] sDirSDRawP10 = new String[] {
    "701", "0.5000", "702", "0.3000", "703", "0.0000", "704", "0.6000", "705", "0.6000",
    "706", "0.2000", "707", "0.8000", "708", "1.0000", "709", "0.9000", "710", "0.8000",
    "711", "0.6000", "712", "0.9000", "713", "0.6000", "714", "0.5000", "715", "0.2000",
    "716", "0.1000", "717", "0.9000", "718", "1.0000", "719", "0.5000", "720", "0.6000",
    "721", "0.2000", "722", "0.9000", "723", "1.0000", "724", "0.3000", "725", "0.5000",
    "726", "0.9000", "727", "0.8000", "728", "0.7000", "729", "0.0000", "730", "0.8000",
    "731", "0.0000", "732", "0.4000", "733", "0.8000", "734", "0.4000", "735", "0.8000",
    "736", "1.0000", "737", "0.9000", "738", "1.0000", "739", "0.8000", "740", "0.6000",
    "741", "0.0000", "742", "0.6000", "743", "0.1000", "744", "0.0000", "745", "0.4000",
    "746", "1.0000", "747", "0.8000", "748", "0.4000", "749", "0.8000", "750", "0.4000",
    "751", "0.6000", "752", "1.0000", "753", "0.4000", "754", "0.2000", "755", "0.7000",
    "756", "0.7000", "757", "0.5000", "758", "1.0000", "759", "0.7000", "760", "0.6000",
    "761", "1.0000", "762", "0.0000", "763", "0.9000", "764", "0.5000", "765", "0.9000",
    "766", "1.0000", "767", "0.7000", "768", "0.5000", "769", "0.1000", "770", "0.8000",
    "771", "0.9000", "772", "0.9000", "773", "1.0000", "774", "0.8000", "775", "0.6000",
    "776", "0.2000", "777", "0.5000", "778", "0.4000", "779", "0.9000", "780", "0.7000",
    "781", "0.8000", "782", "0.9000", "783", "0.6000", "784", "0.2000", "785", "1.0000",
    "786", "0.6000", "787", "1.0000", "788", "0.9000", "789", "0.3000", "790", "0.8000",
    "791", "0.8000", "792", "0.3000", "793", "0.7000", "794", "0.4000", "795", "0.0000",
    "796", "0.8000", "797", "0.8000", "798", "0.4000", "799", "0.2000", "800", "0.1000",
    "801", "0.9000", "802", "1.0000", "803", "0.0000", "804", "0.6000", "805", "0.1000",
    "806", "0.3000", "807", "1.0000", "808", "1.0000", "809", "1.0000", "810", "0.6000",
    "811", "0.7000", "812", "0.7000", "813", "1.0000", "814", "0.9000", "815", "0.3000",
    "816", "0.8000", "817", "1.0000", "818", "0.7000", "819", "0.9000", "820", "0.6000",
    "821", "0.6000", "822", "0.3000", "823", "0.7000", "824", "0.9000", "825", "0.2000",
    "826", "0.4000", "827", "0.6000", "828", "0.9000", "829", "0.5000", "830", "0.1000",
    "831", "0.9000", "832", "0.6000", "833", "0.6000", "834", "0.3000", "835", "0.2000",
    "836", "0.2000", "837", "0.0000", "838", "0.8000", "839", "1.0000", "840", "0.1000",
    "841", "1.0000", "842", "0.1000", "843", "0.9000", "844", "0.1000", "845", "0.5000",
    "846", "0.8000", "847", "0.4000", "848", "0.4000", "849", "0.8000", "850", "0.3000" };

  private static String[] sDirFDRawAP = new String[] {
    "701", "0.1428", "702", "0.0725", "703", "0.0000", "704", "0.3071", "705", "0.4043",
    "706", "0.1105", "707", "0.4216", "708", "0.1915", "709", "0.8071", "710", "0.5147",
    "711", "0.1890", "712", "0.5709", "713", "0.3211", "714", "0.1523", "715", "0.1512",
    "716", "0.0775", "717", "0.3184", "718", "0.3065", "719", "0.0506", "720", "0.2129",
    "721", "0.0581", "722", "0.4778", "723", "0.2790", "724", "0.2062", "725", "0.3844",
    "726", "0.4735", "727", "0.4368", "728", "0.4028", "729", "0.0010", "730", "0.5289",
    "731", "0.1166", "732", "0.3127", "733", "0.3135", "734", "0.2373", "735", "0.2651",
    "736", "0.5475", "737", "0.5384", "738", "0.5348", "739", "0.3366", "740", "0.2587",
    "741", "0.0984", "742", "0.4115", "743", "0.0262", "744", "0.0189", "745", "0.4894",
    "746", "0.4099", "747", "0.1223", "748", "0.3439", "749", "0.3621", "750", "0.1597",
    "751", "0.3721", "752", "0.5837", "753", "0.2715", "754", "0.1004", "755", "0.4938",
    "756", "0.3255", "757", "0.2148", "758", "0.7060", "759", "0.2802", "760", "0.2890",
    "761", "0.4593", "762", "0.0172", "763", "0.2332", "764", "0.2193", "765", "0.4144",
    "766", "0.5451", "767", "0.4764", "768", "0.1725", "769", "0.0231", "770", "0.2914",
    "771", "0.5659", "772", "0.4965", "773", "0.4738", "774", "0.3827", "775", "0.1075",
    "776", "0.2592", "777", "0.2441", "778", "0.1522", "779", "0.4327", "780", "0.3818",
    "781", "0.3313", "782", "0.6530", "783", "0.2111", "784", "0.4571", "785", "0.6228",
    "786", "0.4286", "787", "0.5695", "788", "0.5757", "789", "0.2285", "790", "0.4862",
    "791", "0.4143", "792", "0.2138", "793", "0.3365", "794", "0.1415", "795", "0.0265",
    "796", "0.0774", "797", "0.5745", "798", "0.1747", "799", "0.1216", "800", "0.2079",
    "801", "0.4483", "802", "0.4781", "803", "0.0003", "804", "0.5213", "805", "0.0822",
    "806", "0.0690", "807", "0.6476", "808", "0.6952", "809", "0.3493", "810", "0.3806",
    "811", "0.3021", "812", "0.5842", "813", "0.3153", "814", "0.7128", "815", "0.1891",
    "816", "0.7751", "817", "0.3726", "818", "0.2229", "819", "0.5763", "820", "0.7747",
    "821", "0.3516", "822", "0.0871", "823", "0.3960", "824", "0.3251", "825", "0.2117",
    "826", "0.3609", "827", "0.3294", "828", "0.3407", "829", "0.1867", "830", "0.0745",
    "831", "0.6490", "832", "0.1685", "833", "0.4465", "834", "0.3666", "835", "0.1258",
    "836", "0.2325", "837", "0.0680", "838", "0.2854", "839", "0.5686", "840", "0.1665",
    "841", "0.4073", "842", "0.1744", "843", "0.4942", "844", "0.0837", "845", "0.3786",
    "846", "0.1997", "847", "0.1811", "848", "0.1141", "849", "0.4099", "850", "0.2344" };

  private static String[] sDirFDRawP10 = new String[] {
    "701", "0.4000", "702", "0.3000", "703", "0.0000", "704", "0.5000", "705", "0.8000",
    "706", "0.3000", "707", "0.8000", "708", "0.8000", "709", "0.9000", "710", "0.8000",
    "711", "0.6000", "712", "0.9000", "713", "0.6000", "714", "0.5000", "715", "0.2000",
    "716", "0.2000", "717", "0.9000", "718", "1.0000", "719", "0.5000", "720", "0.5000",
    "721", "0.3000", "722", "0.9000", "723", "1.0000", "724", "0.3000", "725", "0.7000",
    "726", "0.7000", "727", "0.8000", "728", "0.9000", "729", "0.0000", "730", "0.8000",
    "731", "0.0000", "732", "0.4000", "733", "0.8000", "734", "0.4000", "735", "1.0000",
    "736", "1.0000", "737", "0.9000", "738", "1.0000", "739", "0.8000", "740", "0.5000",
    "741", "0.0000", "742", "0.5000", "743", "0.1000", "744", "0.0000", "745", "0.4000",
    "746", "1.0000", "747", "0.7000", "748", "0.6000", "749", "0.8000", "750", "0.4000",
    "751", "0.6000", "752", "1.0000", "753", "0.3000", "754", "0.4000", "755", "0.8000",
    "756", "0.7000", "757", "0.5000", "758", "0.9000", "759", "0.6000", "760", "0.6000",
    "761", "1.0000", "762", "0.0000", "763", "0.9000", "764", "0.5000", "765", "0.9000",
    "766", "1.0000", "767", "0.8000", "768", "0.5000", "769", "0.1000", "770", "0.6000",
    "771", "0.8000", "772", "0.9000", "773", "0.9000", "774", "0.8000", "775", "0.6000",
    "776", "0.2000", "777", "0.3000", "778", "0.4000", "779", "0.9000", "780", "0.7000",
    "781", "0.7000", "782", "0.8000", "783", "0.6000", "784", "0.2000", "785", "1.0000",
    "786", "0.6000", "787", "1.0000", "788", "0.9000", "789", "0.3000", "790", "0.8000",
    "791", "0.8000", "792", "0.5000", "793", "0.7000", "794", "0.4000", "795", "0.0000",
    "796", "0.4000", "797", "0.6000", "798", "0.4000", "799", "0.1000", "800", "0.2000",
    "801", "0.9000", "802", "1.0000", "803", "0.0000", "804", "0.5000", "805", "0.1000",
    "806", "0.3000", "807", "1.0000", "808", "1.0000", "809", "1.0000", "810", "0.6000",
    "811", "0.7000", "812", "0.7000", "813", "0.9000", "814", "0.9000", "815", "0.6000",
    "816", "0.9000", "817", "0.8000", "818", "0.8000", "819", "0.9000", "820", "0.6000",
    "821", "0.6000", "822", "0.2000", "823", "0.7000", "824", "0.9000", "825", "0.3000",
    "826", "0.4000", "827", "0.5000", "828", "0.9000", "829", "0.5000", "830", "0.1000",
    "831", "0.8000", "832", "0.6000", "833", "0.6000", "834", "0.3000", "835", "0.2000",
    "836", "0.1000", "837", "0.0000", "838", "0.7000", "839", "0.9000", "840", "0.1000",
    "841", "1.0000", "842", "0.1000", "843", "0.9000", "844", "0.1000", "845", "0.4000",
    "846", "0.8000", "847", "0.5000", "848", "0.5000", "849", "0.8000", "850", "0.3000" };

  private static String[] sBm25BaseRawAP = new String[] {
    "701", "0.0626", "702", "0.0451", "703", "0.0000", "704", "0.1469", "705", "0.1499",
    "706", "0.0418", "707", "0.2478", "708", "0.2371", "709", "0.8223", "710", "0.5348",
    "711", "0.0504", "712", "0.5609", "713", "0.3321", "714", "0.1455", "715", "0.1463",
    "716", "0.1115", "717", "0.3542", "718", "0.3385", "719", "0.0839", "720", "0.2693",
    "721", "0.0169", "722", "0.3861", "723", "0.1402", "724", "0.2843", "725", "0.2888",
    "726", "0.5141", "727", "0.4446", "728", "0.3672", "729", "0.0043", "730", "0.5027",
    "731", "0.2256", "732", "0.2486", "733", "0.3176", "734", "0.2198", "735", "0.1977",
    "736", "0.6023", "737", "0.5916", "738", "0.5923", "739", "0.0795", "740", "0.2191",
    "741", "0.0970", "742", "0.2480", "743", "0.0179", "744", "0.0138", "745", "0.4222",
    "746", "0.4066", "747", "0.0536", "748", "0.2089", "749", "0.3274", "750", "0.0272",
    "751", "0.3510", "752", "0.5228", "753", "0.3016", "754", "0.0558", "755", "0.5064",
    "756", "0.3689", "757", "0.1960", "758", "0.6989", "759", "0.3019", "760", "0.2400",
    "761", "0.5056", "762", "0.0234", "763", "0.1036", "764", "0.1027", "765", "0.4701",
    "766", "0.4865", "767", "0.4680", "768", "0.1305", "769", "0.0076", "770", "0.4511",
    "771", "0.5409", "772", "0.2170", "773", "0.5560", "774", "0.2928", "775", "0.3234",
    "776", "0.3062", "777", "0.3666", "778", "0.1278", "779", "0.5469", "780", "0.4076",
    "781", "0.3381", "782", "0.6500", "783", "0.0778", "784", "0.4589", "785", "0.3853",
    "786", "0.4403", "787", "0.5716", "788", "0.5701", "789", "0.2253", "790", "0.4652",
    "791", "0.4844", "792", "0.1218", "793", "0.4170", "794", "0.0692", "795", "0.0281",
    "796", "0.2339", "797", "0.5376", "798", "0.1734", "799", "0.3011", "800", "0.2497",
    "801", "0.4360", "802", "0.3865", "803", "0.0000", "804", "0.4788", "805", "0.0659",
    "806", "0.0201", "807", "0.5896", "808", "0.6556", "809", "0.2885", "810", "0.3456",
    "811", "0.2532", "812", "0.6084", "813", "0.3057", "814", "0.6841", "815", "0.2420",
    "816", "0.8159", "817", "0.3985", "818", "0.4326", "819", "0.5179", "820", "0.7725",
    "821", "0.1856", "822", "0.0928", "823", "0.2419", "824", "0.3279", "825", "0.0091",
    "826", "0.2273", "827", "0.3649", "828", "0.2725", "829", "0.0529", "830", "0.0418",
    "831", "0.2782", "832", "0.1672", "833", "0.4185", "834", "0.4305", "835", "0.0422",
    "836", "0.2115", "837", "0.1243", "838", "0.2687", "839", "0.5133", "840", "0.1548",
    "841", "0.3301", "842", "0.1407", "843", "0.4670", "844", "0.0500", "845", "0.3339",
    "846", "0.1627", "847", "0.3731", "848", "0.1340", "849", "0.1671", "850", "0.2759" };

  private static String[] sBm25BaseRawP10 = new String[] {
    "701", "0.3000", "702", "0.2000", "703", "0.0000", "704", "0.4000", "705", "0.4000",
    "706", "0.2000", "707", "1.0000", "708", "0.8000", "709", "0.9000", "710", "0.9000",
    "711", "0.1000", "712", "0.9000", "713", "0.7000", "714", "0.3000", "715", "0.2000",
    "716", "0.2000", "717", "1.0000", "718", "1.0000", "719", "0.4000", "720", "0.6000",
    "721", "0.1000", "722", "0.6000", "723", "0.2000", "724", "0.4000", "725", "0.4000",
    "726", "0.9000", "727", "0.7000", "728", "0.9000", "729", "0.0000", "730", "0.8000",
    "731", "0.2000", "732", "0.5000", "733", "0.8000", "734", "0.5000", "735", "0.6000",
    "736", "1.0000", "737", "1.0000", "738", "1.0000", "739", "0.5000", "740", "0.6000",
    "741", "0.1000", "742", "0.5000", "743", "0.0000", "744", "0.0000", "745", "0.5000",
    "746", "1.0000", "747", "0.7000", "748", "0.3000", "749", "0.8000", "750", "0.0000",
    "751", "0.7000", "752", "1.0000", "753", "0.7000", "754", "0.3000", "755", "0.8000",
    "756", "0.6000", "757", "0.5000", "758", "0.9000", "759", "0.8000", "760", "0.7000",
    "761", "1.0000", "762", "0.1000", "763", "0.2000", "764", "0.6000", "765", "1.0000",
    "766", "1.0000", "767", "1.0000", "768", "0.7000", "769", "0.0000", "770", "1.0000",
    "771", "1.0000", "772", "0.8000", "773", "1.0000", "774", "0.6000", "775", "0.6000",
    "776", "0.5000", "777", "0.5000", "778", "0.4000", "779", "1.0000", "780", "0.8000",
    "781", "0.9000", "782", "0.8000", "783", "0.2000", "784", "0.3000", "785", "0.9000",
    "786", "0.7000", "787", "1.0000", "788", "0.9000", "789", "0.4000", "790", "0.9000",
    "791", "0.8000", "792", "0.5000", "793", "0.6000", "794", "0.2000", "795", "0.0000",
    "796", "0.7000", "797", "0.7000", "798", "0.2000", "799", "0.4000", "800", "0.1000",
    "801", "0.8000", "802", "0.9000", "803", "0.0000", "804", "0.4000", "805", "0.1000",
    "806", "0.2000", "807", "1.0000", "808", "1.0000", "809", "0.8000", "810", "0.5000",
    "811", "0.7000", "812", "0.9000", "813", "0.9000", "814", "0.9000", "815", "0.6000",
    "816", "0.9000", "817", "1.0000", "818", "0.9000", "819", "1.0000", "820", "0.6000",
    "821", "0.4000", "822", "0.2000", "823", "0.6000", "824", "0.9000", "825", "0.0000",
    "826", "0.5000", "827", "0.3000", "828", "0.8000", "829", "0.1000", "830", "0.1000",
    "831", "0.7000", "832", "0.6000", "833", "0.5000", "834", "1.0000", "835", "0.0000",
    "836", "0.4000", "837", "0.1000", "838", "0.7000", "839", "1.0000", "840", "0.1000",
    "841", "0.8000", "842", "0.0000", "843", "0.9000", "844", "0.2000", "845", "0.7000",
    "846", "0.8000", "847", "0.9000", "848", "0.5000", "849", "0.5000", "850", "0.6000" };

  private static String[] sBm25SDRawAP = new String[] {
    "701", "0.0947", "702", "0.1041", "703", "0.0000", "704", "0.2464", "705", "0.1407",
    "706", "0.0292", "707", "0.3339", "708", "0.2503", "709", "0.7988", "710", "0.4273",
    "711", "0.1520", "712", "0.5604", "713", "0.3628", "714", "0.1443", "715", "0.1066",
    "716", "0.1203", "717", "0.2985", "718", "0.2933", "719", "0.1546", "720", "0.1321",
    "721", "0.0254", "722", "0.3264", "723", "0.4938", "724", "0.3036", "725", "0.3478",
    "726", "0.4526", "727", "0.4212", "728", "0.3292", "729", "0.0038", "730", "0.5308",
    "731", "0.2186", "732", "0.3604", "733", "0.3275", "734", "0.2161", "735", "0.2679",
    "736", "0.5939", "737", "0.5901", "738", "0.6511", "739", "0.4209", "740", "0.3068",
    "741", "0.1005", "742", "0.3362", "743", "0.0191", "744", "0.0170", "745", "0.5077",
    "746", "0.4016", "747", "0.0838", "748", "0.2934", "749", "0.1988", "750", "0.2575",
    "751", "0.3760", "752", "0.5958", "753", "0.3239", "754", "0.0702", "755", "0.4896",
    "756", "0.3323", "757", "0.1960", "758", "0.7009", "759", "0.3384", "760", "0.2877",
    "761", "0.5259", "762", "0.0258", "763", "0.2187", "764", "0.1725", "765", "0.4389",
    "766", "0.5185", "767", "0.3609", "768", "0.0661", "769", "0.0293", "770", "0.4497",
    "771", "0.5514", "772", "0.3271", "773", "0.4971", "774", "0.4169", "775", "0.3318",
    "776", "0.2231", "777", "0.3296", "778", "0.2316", "779", "0.5552", "780", "0.4189",
    "781", "0.3085", "782", "0.5783", "783", "0.1622", "784", "0.4673", "785", "0.6209",
    "786", "0.4156", "787", "0.5810", "788", "0.6061", "789", "0.2217", "790", "0.3763",
    "791", "0.4792", "792", "0.0902", "793", "0.4577", "794", "0.3021", "795", "0.0283",
    "796", "0.2562", "797", "0.5209", "798", "0.1535", "799", "0.2679", "800", "0.2388",
    "801", "0.4591", "802", "0.4569", "803", "0.0030", "804", "0.4967", "805", "0.0758",
    "806", "0.2549", "807", "0.6754", "808", "0.7079", "809", "0.2949", "810", "0.4173",
    "811", "0.2907", "812", "0.6366", "813", "0.3316", "814", "0.6794", "815", "0.2785",
    "816", "0.8100", "817", "0.5664", "818", "0.3726", "819", "0.5737", "820", "0.7743",
    "821", "0.2856", "822", "0.0794", "823", "0.3889", "824", "0.3311", "825", "0.0426",
    "826", "0.3580", "827", "0.3795", "828", "0.3875", "829", "0.1453", "830", "0.1663",
    "831", "0.2474", "832", "0.0534", "833", "0.3507", "834", "0.4697", "835", "0.1529",
    "836", "0.1819", "837", "0.1162", "838", "0.1838", "839", "0.5793", "840", "0.1548",
    "841", "0.4686", "842", "0.2706", "843", "0.5699", "844", "0.1530", "845", "0.4175",
    "846", "0.1913", "847", "0.3883", "848", "0.0938", "849", "0.4251", "850", "0.2113" };

  private static String[] sBm25SDRawP10 = new String[] {
    "701", "0.5000", "702", "0.7000", "703", "0.0000", "704", "0.4000", "705", "0.3000",
    "706", "0.2000", "707", "0.8000", "708", "0.9000", "709", "0.9000", "710", "0.8000",
    "711", "0.5000", "712", "0.9000", "713", "0.7000", "714", "0.5000", "715", "0.2000",
    "716", "0.3000", "717", "0.8000", "718", "1.0000", "719", "0.5000", "720", "0.3000",
    "721", "0.2000", "722", "0.4000", "723", "0.9000", "724", "0.4000", "725", "0.6000",
    "726", "0.6000", "727", "0.8000", "728", "0.4000", "729", "0.0000", "730", "0.7000",
    "731", "0.0000", "732", "0.5000", "733", "0.8000", "734", "0.6000", "735", "1.0000",
    "736", "1.0000", "737", "1.0000", "738", "1.0000", "739", "0.9000", "740", "0.6000",
    "741", "0.0000", "742", "0.6000", "743", "0.0000", "744", "0.0000", "745", "0.7000",
    "746", "1.0000", "747", "0.7000", "748", "0.5000", "749", "0.7000", "750", "0.4000",
    "751", "0.7000", "752", "1.0000", "753", "0.6000", "754", "0.3000", "755", "0.7000",
    "756", "0.8000", "757", "0.5000", "758", "0.9000", "759", "0.9000", "760", "0.7000",
    "761", "1.0000", "762", "0.0000", "763", "0.8000", "764", "0.7000", "765", "0.8000",
    "766", "1.0000", "767", "1.0000", "768", "0.3000", "769", "0.1000", "770", "0.9000",
    "771", "0.9000", "772", "0.8000", "773", "1.0000", "774", "0.9000", "775", "0.7000",
    "776", "0.6000", "777", "0.5000", "778", "0.4000", "779", "1.0000", "780", "0.7000",
    "781", "0.7000", "782", "0.7000", "783", "0.6000", "784", "0.3000", "785", "1.0000",
    "786", "0.3000", "787", "1.0000", "788", "0.9000", "789", "0.2000", "790", "0.4000",
    "791", "0.8000", "792", "0.2000", "793", "0.6000", "794", "0.9000", "795", "0.0000",
    "796", "0.9000", "797", "0.5000", "798", "0.3000", "799", "0.1000", "800", "0.5000",
    "801", "0.8000", "802", "0.8000", "803", "0.0000", "804", "0.6000", "805", "0.1000",
    "806", "0.8000", "807", "1.0000", "808", "1.0000", "809", "0.8000", "810", "0.7000",
    "811", "0.6000", "812", "0.8000", "813", "0.9000", "814", "0.9000", "815", "0.6000",
    "816", "0.9000", "817", "1.0000", "818", "0.8000", "819", "0.9000", "820", "0.6000",
    "821", "0.5000", "822", "0.2000", "823", "0.6000", "824", "0.9000", "825", "0.3000",
    "826", "0.7000", "827", "0.3000", "828", "0.9000", "829", "0.2000", "830", "0.3000",
    "831", "0.5000", "832", "0.4000", "833", "0.5000", "834", "0.9000", "835", "0.0000",
    "836", "0.1000", "837", "0.0000", "838", "0.6000", "839", "1.0000", "840", "0.1000",
    "841", "0.9000", "842", "0.5000", "843", "1.0000", "844", "0.1000", "845", "0.6000",
    "846", "0.8000", "847", "1.0000", "848", "0.2000", "849", "0.9000", "850", "0.7000" };

  private static String[] sBm25FDRawAP = new String[] {
    "701", "0.0868", "702", "0.0823", "703", "0.0000", "704", "0.2212", "705", "0.2105",
    "706", "0.0329", "707", "0.3407", "708", "0.2491", "709", "0.7914", "710", "0.4862",
    "711", "0.1231", "712", "0.5648", "713", "0.3338", "714", "0.1472", "715", "0.1344",
    "716", "0.1140", "717", "0.2940", "718", "0.2926", "719", "0.1187", "720", "0.1091",
    "721", "0.0296", "722", "0.3682", "723", "0.4247", "724", "0.3032", "725", "0.3690",
    "726", "0.4570", "727", "0.4314", "728", "0.3822", "729", "0.0031", "730", "0.5502",
    "731", "0.2196", "732", "0.3425", "733", "0.3223", "734", "0.2433", "735", "0.3053",
    "736", "0.6087", "737", "0.6000", "738", "0.6394", "739", "0.3350", "740", "0.2997",
    "741", "0.1002", "742", "0.3186", "743", "0.0192", "744", "0.0150", "745", "0.4811",
    "746", "0.4574", "747", "0.0718", "748", "0.2897", "749", "0.2187", "750", "0.1604",
    "751", "0.3651", "752", "0.5986", "753", "0.3176", "754", "0.0644", "755", "0.5184",
    "756", "0.3455", "757", "0.1960", "758", "0.7010", "759", "0.3139", "760", "0.2687",
    "761", "0.5237", "762", "0.0300", "763", "0.1949", "764", "0.1757", "765", "0.4631",
    "766", "0.5094", "767", "0.3949", "768", "0.1416", "769", "0.0157", "770", "0.4809",
    "771", "0.5600", "772", "0.3552", "773", "0.4732", "774", "0.4141", "775", "0.3417",
    "776", "0.2837", "777", "0.2729", "778", "0.1931", "779", "0.5525", "780", "0.4201",
    "781", "0.3421", "782", "0.6104", "783", "0.1638", "784", "0.4589", "785", "0.6088",
    "786", "0.4313", "787", "0.5804", "788", "0.6083", "789", "0.2213", "790", "0.4603",
    "791", "0.4969", "792", "0.1379", "793", "0.4363", "794", "0.1586", "795", "0.0280",
    "796", "0.2301", "797", "0.5244", "798", "0.1559", "799", "0.3051", "800", "0.2899",
    "801", "0.4560", "802", "0.4681", "803", "0.0014", "804", "0.5073", "805", "0.0774",
    "806", "0.1147", "807", "0.7332", "808", "0.7126", "809", "0.3216", "810", "0.3859",
    "811", "0.2766", "812", "0.6120", "813", "0.3364", "814", "0.6879", "815", "0.2833",
    "816", "0.8105", "817", "0.5537", "818", "0.3421", "819", "0.6004", "820", "0.7746",
    "821", "0.2535", "822", "0.0750", "823", "0.3716", "824", "0.3308", "825", "0.0946",
    "826", "0.3808", "827", "0.3614", "828", "0.3387", "829", "0.1341", "830", "0.1204",
    "831", "0.3918", "832", "0.0807", "833", "0.3711", "834", "0.4671", "835", "0.0901",
    "836", "0.2253", "837", "0.1194", "838", "0.2555", "839", "0.5376", "840", "0.1548",
    "841", "0.4409", "842", "0.2159", "843", "0.5341", "844", "0.1042", "845", "0.4087",
    "846", "0.1845", "847", "0.3986", "848", "0.1033", "849", "0.4182", "850", "0.2514" };

  private static String[] sBm25FDRawP10 = new String[] {
    "701", "0.4000", "702", "0.7000", "703", "0.0000", "704", "0.3000", "705", "0.3000",
    "706", "0.2000", "707", "1.0000", "708", "0.9000", "709", "0.9000", "710", "0.8000",
    "711", "0.3000", "712", "0.9000", "713", "0.9000", "714", "0.3000", "715", "0.2000",
    "716", "0.2000", "717", "0.9000", "718", "1.0000", "719", "0.6000", "720", "0.4000",
    "721", "0.2000", "722", "0.5000", "723", "0.9000", "724", "0.4000", "725", "0.7000",
    "726", "0.6000", "727", "0.8000", "728", "0.7000", "729", "0.0000", "730", "0.7000",
    "731", "0.0000", "732", "0.5000", "733", "0.8000", "734", "0.5000", "735", "1.0000",
    "736", "1.0000", "737", "1.0000", "738", "1.0000", "739", "0.7000", "740", "0.6000",
    "741", "0.1000", "742", "0.6000", "743", "0.0000", "744", "0.0000", "745", "0.6000",
    "746", "1.0000", "747", "0.6000", "748", "0.5000", "749", "0.8000", "750", "0.4000",
    "751", "0.7000", "752", "1.0000", "753", "0.6000", "754", "0.3000", "755", "0.9000",
    "756", "0.7000", "757", "0.5000", "758", "0.9000", "759", "0.6000", "760", "0.7000",
    "761", "1.0000", "762", "0.0000", "763", "0.7000", "764", "0.7000", "765", "1.0000",
    "766", "1.0000", "767", "0.5000", "768", "0.5000", "769", "0.0000", "770", "0.9000",
    "771", "0.9000", "772", "0.8000", "773", "0.9000", "774", "0.9000", "775", "0.6000",
    "776", "0.6000", "777", "0.5000", "778", "0.4000", "779", "1.0000", "780", "0.7000",
    "781", "0.7000", "782", "0.7000", "783", "0.4000", "784", "0.3000", "785", "1.0000",
    "786", "0.4000", "787", "1.0000", "788", "0.9000", "789", "0.2000", "790", "0.7000",
    "791", "0.8000", "792", "0.3000", "793", "0.5000", "794", "0.5000", "795", "0.0000",
    "796", "1.0000", "797", "0.5000", "798", "0.3000", "799", "0.2000", "800", "0.4000",
    "801", "0.8000", "802", "0.8000", "803", "0.0000", "804", "0.6000", "805", "0.0000",
    "806", "0.6000", "807", "1.0000", "808", "1.0000", "809", "0.8000", "810", "0.6000",
    "811", "0.7000", "812", "0.8000", "813", "0.9000", "814", "0.9000", "815", "0.7000",
    "816", "0.9000", "817", "1.0000", "818", "0.7000", "819", "1.0000", "820", "0.6000",
    "821", "0.5000", "822", "0.2000", "823", "0.6000", "824", "0.9000", "825", "0.4000",
    "826", "0.6000", "827", "0.5000", "828", "0.9000", "829", "0.3000", "830", "0.3000",
    "831", "0.8000", "832", "0.6000", "833", "0.5000", "834", "0.7000", "835", "0.0000",
    "836", "0.3000", "837", "0.0000", "838", "0.6000", "839", "1.0000", "840", "0.1000",
    "841", "1.0000", "842", "0.6000", "843", "1.0000", "844", "0.1000", "845", "0.6000",
    "846", "0.8000", "847", "0.9000", "848", "0.3000", "849", "0.9000", "850", "0.7000" };

  @Test
  public void runRegression() throws Exception {
    String[] params = new String[] {
        "data/gov2/run.gov2.basic.xml",
        "data/gov2/gov2.title.701-775",
        "data/gov2/gov2.title.776-850" };

    FileSystem fs = FileSystem.getLocal(new Configuration());

    BatchQueryRunner qr = new BatchQueryRunner(params, fs);

    long start = System.currentTimeMillis();
    qr.runQueries();
    long end = System.currentTimeMillis();

    LOG.info("Total query time: " + (end - start) + "ms");

    verifyAllResults(qr.getModels(), qr.getAllResults(), qr.getDocnoMapping(),
        new Qrels("data/gov2/qrels.gov2.all"));
  }

  public static void verifyAllResults(Set<String> models,
      Map<String, Map<String, Accumulator[]>> results, DocnoMapping mapping, Qrels qrels) {

    Map<String, GroundTruth> g = Maps.newHashMap();
    // One topic didn't contain qrels, so trec_eval only picked up 149.
    g.put("gov2-dir-base", new GroundTruth(Metric.AP, 149, sDirBaseRawAP, 0.3077f));
    g.put("gov2-dir-sd", new GroundTruth(Metric.AP, 149, sDirSDRawAP, 0.3239f));
    g.put("gov2-dir-fd", new GroundTruth(Metric.AP, 149, sDirFDRawAP, 0.3237f));
    g.put("gov2-bm25-base", new GroundTruth(Metric.AP, 149, sBm25BaseRawAP, 0.2999f));
    g.put("gov2-bm25-sd", new GroundTruth(Metric.AP, 149, sBm25SDRawAP, 0.3294f));
    g.put("gov2-bm25-fd", new GroundTruth(Metric.AP, 149, sBm25FDRawAP, 0.3295f));

    Map<String, GroundTruth> h = Maps.newHashMap();
    // One topic didn't contain qrels, so trec_eval only picked up 149.
    h.put("gov2-dir-base", new GroundTruth(Metric.P10, 149, sDirBaseRawP10, 0.5631f));
    h.put("gov2-dir-sd", new GroundTruth(Metric.P10, 149, sDirSDRawP10, 0.6007f));
    h.put("gov2-dir-fd", new GroundTruth(Metric.P10, 149, sDirFDRawP10, 0.5933f));
    h.put("gov2-bm25-base", new GroundTruth(Metric.P10, 149, sBm25BaseRawP10, 0.5846f));
    h.put("gov2-bm25-sd", new GroundTruth(Metric.P10, 149, sBm25SDRawP10, 0.6081f));
    h.put("gov2-bm25-fd", new GroundTruth(Metric.P10, 149, sBm25FDRawP10, 0.6094f));

    for (String model : models) {
      LOG.info("Verifying results of model \"" + model + "\"");

      Map<String, Accumulator[]> r = results.get(model);
      g.get(model).verify(r, mapping, qrels);
      h.get(model).verify(r, mapping, qrels);

      LOG.info("Done!");
    }
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(Gov2_Basic.class);
  }
}
