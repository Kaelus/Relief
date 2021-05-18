package relief.cloud;

import java.io.IOException;
import java.text.ParseException;

public interface ReliefDKVS {
	// close the key value store
	public void finish() throws IOException;

	public ReliefDKVSResponse get(String key) throws IOException;

	public ReliefDKVSResponse get(String start, String end) throws IOException, ParseException;
	
	public ReliefDKVSResponse put(String key, byte[] value) throws IOException;

	public ReliefDKVSResponse remove(String key);

	public void clear();
	
}
