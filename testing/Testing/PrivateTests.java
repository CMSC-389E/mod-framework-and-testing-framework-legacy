import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

public class PrivateTests {
	public static final String submit = "submit.txt";
	public static final String results = "results.txt";
	public static final String WorldName = "project2";
	public static final Map<String,String> associations = createAssociations();
	public static final Map<String,String> partners = createPartners();
	private static Map<String, String> createAssociations() {
		HashMap<String, String> assocs = new HashMap<String, String>();
		assocs.put("abrassel", "bsprouts");
		assocs.put("jklein17","failplus");
		
		assocs.put("abhinav", "ahbinav97");
		assocs.put("bbock1", "114772534");
		assocs.put("kdohert3", "Sky79731");
		assocs.put("rodneyduff1994", "");
		assocs.put("ahall128", "Thulgrimm");
		assocs.put("ahuang10", "ahuang10");
		assocs.put("dkelly18", "Danielemur");
		assocs.put("dkline1", "");
		assocs.put("jnengel", "");
		assocs.put("anolan19", "N01an");
		assocs.put("pasquino", "Wurfel_");
		assocs.put("kqiao", "");
		assocs.put("naterad", "naterad");
		assocs.put("kschlee", "Katatonica");
		assocs.put("sshiao", "srshiao");
		assocs.put("mitchs", "");
		return assocs;
	}
	
	private static Map<String, String> createPartners() {
		HashMap<String, String> partners = new HashMap<String, String>();
		partners.put("abrassel", "Player10");
		
		return partners;
	}
	
	public static String[] cheating;
	public static Set<String> people_involved = new HashSet<String>();


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
			
			cheating = new String(data).split("\nEOF\n"); // {test results\nEOF\nplayers}
			
			//results[0] = public test stuff
			//results[1] = uname
			//results[2] = player \t world list
			System.out.println(cheating[0].length());
			System.out.println(cheating[1]);
			System.out.println(cheating[2]);
			
			for (String entry : cheating[2].split("\n")) {
				String[] parts = entry.split("\t");
				if (parts[1].toLowerCase().replace(" ", "").equals(WorldName)) {
					people_involved.add(parts[0]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testParticipation() {
		System.out.println(people_involved);
		assertNotEquals(0,people_involved.size());
	}
	
	@Test
	public void testNotTooManyPeople() {
		assertTrue(Arrays.toString(people_involved.toArray()), people_involved.size() <=2);
	}
	
	@Test
	public void wrongPerson() {
		System.out.println(people_involved);
		assertTrue(Arrays.toString(people_involved.toArray()), people_involved.contains(associations.get(cheating[1])));
		if (people_involved.size() == 2) {
			assertTrue(Arrays.toString(people_involved.toArray()), people_involved.contains(associations.get(partners.get(cheating[1]))));
		}
	}

}
