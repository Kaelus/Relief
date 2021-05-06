package relief.cloud;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import relief.util.Timestamper;

public class ReliefDynamoDBHistoryKeyspaceTest {

	@Test
	public void testPutAndRangedGet() throws IOException, ParseException {
		ReliefDynamoDBHistoryManager rdhk = new ReliefDynamoDBHistoryManager("conf/relief.conf");
		String startTS = Timestamper.getTimestamp();
		String curTS = Timestamper.getTimestamp();
		rdhk.put(curTS, "Hello World 1".getBytes());
		curTS = Timestamper.getTimestamp();
		rdhk.put(curTS, "Hello World 2".getBytes());
		curTS = Timestamper.getTimestamp();
		rdhk.put(curTS, "Hello World 3".getBytes());
		curTS = Timestamper.getTimestamp();
		rdhk.put(curTS, "Hello World 4".getBytes());
		curTS = Timestamper.getTimestamp();
		rdhk.put(curTS, "Hello World 5".getBytes());
		ReliefDKVSResponse valBytes = rdhk.get(startTS, Timestamper.getTimestamp());
		System.out.println("valBytes toString: " + valBytes.toString());
		Assert.assertArrayEquals("world".getBytes(), (byte[]) valBytes.data);
		valBytes = rdhk.get("Goodbye");
		Assert.assertEquals(null, valBytes.data);
	}
	
}
