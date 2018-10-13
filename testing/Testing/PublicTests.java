import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;

import org.junit.BeforeClass;
import org.junit.Test;

public class PublicTests {
	public static final String submit = "submit.txt";
	public static final String results = "results.txt";
	public static final int rowWidth = 4 + 3 + 3 + 4;

	public static String[] testoutputs;
	
	
	@BeforeClass
	public static void setup() {
		try {
			File key1 = new File(submit);
			FileInputStream keyin = new FileInputStream(submit);
			byte key[] = new byte[(int) key1.length()];
			keyin.read(key);
			File data1 = new File(results);
			FileInputStream datain = new FileInputStream(results);
			//byte[] data = datain.readAllBytes();
			byte data[] = new byte[(int)data1.length()];
			datain.read(data);

			keyin.close();
			datain.close();

			for (int i = 0; i < data.length; i++) {
				data[i] = (byte) (data[i] ^ key[i % key.length]);
			}

			String[] results = new String(data).split("\nEOF\n"); // {test results\nEOF\nplayers}
			
			String[] testoutputstemp = results[0].split("\n");
			
			//remove the extra outputs for proc'ing tests.
			testoutputs = new String[testoutputstemp.length/2];
			for (int row = 0; row < testoutputstemp.length / rowWidth; row +=2) {
				for (int col = 0; col < rowWidth; col += 1) {
					int index = row*rowWidth + col;
					testoutputstemp[index] = testoutputstemp[index];
				}
			}
			
			testoutputs = results[0].split("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRow1() {
		testRow(0);
	}
	
	@Test
	public void testRow2() {
		testRow(1);
	}
	
	@Test
	public void testRow3() {
		testRow(2);
	}
	
	@Test
	public void testRow4() {
		testRow(3);
	}
	
	@Test
	public void testRow5() {
		testRow(4);
	}
	
	
	
	private void testColumn(int colNum) {
		for(int i = colNum; i < testoutputs.length; i += rowWidth) {
			assertEquals("true", testoutputs[i]);
		}
	}
	
	private void testRow(int rowNum) {
		for (int i = rowNum*rowWidth; i < rowWidth* (rowNum+1); i+= 1) {
			assertEquals("true", testoutputs[i]);
		}
	}

}
