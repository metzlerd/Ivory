package ivory.sqe.querygenerator;

import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;
import org.json.JSONObject;

public interface QueryGenerator {
	  public JSONObject parseQuery(String query);
	  public void init(FileSystem fs, String[] args) throws IOException;
	  public int getQueryLength();
}
