package relief.cloud;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ReliefDynamoDBDataKeyspaceTest {

	@Test
	public void testPutAndGet() throws IOException {
		ReliefDynamoDBDataManager rddk = new ReliefDynamoDBDataManager("conf/relief.conf");
		rddk.put("Hello", "world".getBytes());
		ReliefDKVSResponse valBytes = rddk.get("Hello");
		String respString = "version=" + valBytes.version + " data=" + new String((byte[]) valBytes.data);
		System.out.println("valBytes toString: " + respString);
		Assert.assertArrayEquals("world".getBytes(), (byte[]) valBytes.data);
		valBytes = rddk.get("Goodbye");
		Assert.assertEquals(null, valBytes.data);
	}
	
	
	
}
