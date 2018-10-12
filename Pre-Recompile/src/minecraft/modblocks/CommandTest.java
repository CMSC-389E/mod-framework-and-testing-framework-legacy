package modblocks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.unittest.tileentity.TileEntityIOBase;
import com.unittest.tileentity.TileEntityInput;
import com.unittest.tileentity.TileEntityOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandTest extends CommandBase {
	public static int testCount = 0;
	
	public static List<String[]> runs = null;
	public static String[] keys = null;
//	public static String[] labels = null;
	public static byte[] testFile = null;
	public static String options = null;
	public static boolean lock = false;
	
	public static Thread runningTest = null;

	public String getName() {
		return "test";
	}
	
    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
    
    private static class Task implements Runnable {
    	MinecraftServer server;
    	ICommandSender sender;
    	String[] args;
    	
    	public Task(MinecraftServer server, ICommandSender sender, String[] args) {
    		this.server = server;
    		this.sender = sender;
    		this.args = args;
    	}

		@Override
		public void run() {
			while (lock)
				try {
					Thread.sleep(500);
				} catch(InterruptedException e1) {
					e1.printStackTrace();
				}
			
			sender.sendMessage(new TextComponentString("\n===============================\n=========Running Test "+testCount + ((testCount < 10) ? " " : "") + "=========\n==============================="));
			lock = true;
			try {
				execute2(server, sender, args, new TestRun());
				lock = false;
			} catch (CommandException e) {
				testCount--;
				sender.sendMessage(new TextComponentString(e.getLocalizedMessage()));
				lock = false;
			}
			finally {
				runningTest = null;
			}
		}
    	
    }
    
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    	Task t = new Task(server, sender, args);
        
        Thread th = new Thread(t);
    	runningTest = th;
        th.start();
		testCount++;
    	
    	
    }
    
    public static void execute2(MinecraftServer server, ICommandSender sender, String[] args, TestRun test) throws CommandException
    {	
    	if (runs == null) {
    		//set up 
    		FileReader in;
    		try {
    			in = new FileReader("tests.txt");
    			testFile = Files.readAllBytes(Paths.get("tests.txt"));
    		} catch (IOException e) {
    			throw new CommandException("initialize the test framework with \"/load test <path-to-tests>, or /load <proj#>\"", new Object[0]);
    		}

    		BufferedReader i = new BufferedReader(in);
    		runs = new LinkedList<String[]>();
    		try {
    			options = i.readLine();
//    			labels = i.readLine().split("\t"); //get the input/output group labels
    			keys = i.readLine().split("\t");
    			String line = null;
    			while ((line = i.readLine()) != null) {
    				if (line.isEmpty() || line.charAt(0) == '#') continue;
    				String[] map = line.split("\t");
    				if (map.length == 0) continue;
        			runs.add(map);
    			}
    			
    		}
    		catch (Exception e) {
    			try {
					i.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			keys = null;
    			runs = null;
    			throw new CommandException("Test framework was improperly formatted", new Object[0]);
    		}
    		
    		try {
				i.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		

    	}
    	
    	if (args.length > 2 && !Arrays.asList(args).contains("-norepeat") && !Arrays.asList(args).contains("-nr")) {
    		throw new CommandException("/test [delay] [gate] [-nr/-norepeat]", new Object[0]);
    	}
    	
    	double delay = 0;
    	
    	if (args.length >= 1 && args[0].matches("\\d*\\.?\\d+")) {
    		try {
    			delay = Double.valueOf(args[0]);
    		} catch (NumberFormatException e) {
    			throw new CommandException("You must pass a valid time length (or none at all).", new Object[0]);
    		}
    	}
    	List<TileEntity> entities = sender.getEntityWorld().loadedTileEntityList;
    	List<TileEntityInput> inputs = getInputs(entities);
    	List<TileEntityOutput> outputs = getOutputs(entities);
    	boolean fullrun = true;
    	String results = "";
    	sender.sendMessage(new TextComponentString(Arrays.toString(keys)));
    	boolean filter = Arrays.asList(args).contains("-norepeat") || Arrays.asList(args).contains("-nr");
    	for (String[] run: runs) {
    		boolean testPassed = true;
    		Map<String, Boolean> expectedOutputs = new HashMap<String, Boolean>(); //label vs what was expected
    		List<String> entriesInOrder = new LinkedList<String>();
    		for (int i = 0; i < run.length; i++) {
    			String dest = keys[i]; //get the current tag
    			if (dest.charAt(0) == 'i') {
    				List<TileEntityInput> matches = getInputMatches(dest, inputs);
    				if ("n".equals(options) && matches.size() > 1) throw new CommandException("Duplicate inputs are not allowed for this project.");
    				if (matches.size() == 0) {
    					throw new CommandException("You are missing input block: " + dest, new Object[0]);
    				}
    				//turn all blocks to appropriate states
    				for (TileEntityInput t : matches) {
    					t.setPowerState(run[i].equals("1"));
    				}
    			}
    			else {
    				if (!run[i].equals("o")) {
    					expectedOutputs.put(dest, run[i].equals("1"));
    					entriesInOrder.add(dest);
    				}
    			}

    		}
    		
    		lock = false;
    		try {
    			if (delay == 0 ) Thread.sleep(500);
    			else Thread.sleep((int) (delay * 1000));
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    		
    		while (lock || Minecraft.getMinecraft().isGamePaused())
				try {
					Thread.sleep(500);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
    		lock = true;
    		int target = 0;
    		String actual= "";
    		int j =-1;
    		for (String next: entriesInOrder) {
    			j++;
    			if (args.length >= 1) {
    				if (args.length == 1 && !args[0].matches("\\d*.?\\d+") && args[0].charAt(0) != '-') {
    					fullrun = false;
    					if (!next.equals("o" + args[0].toUpperCase())) continue;
    					else target = j;
    				}
    				else if (args.length == 2 && args[1].charAt(0) != '-') {
    					fullrun = false;
    					if (!next.equals("o" + args[1].toUpperCase())) continue;
    					else target = j;
    				}
    				else if (args.length == 3) {
    					fullrun = false;
    					if(!next.equals("o" + args[1].toUpperCase())) continue;
    					else target=j;
    				}
    			}
    			boolean key = expectedOutputs.get(next);
    			List<TileEntityOutput> matches = getOutputMatches(next, outputs);
				if (matches.size() == 0) {
					throw new CommandException("You are missing output block: " + next, new Object[0]);
				}
    			boolean state = false;
    			boolean set = false;
    			boolean consistent = true;
    			
    			while(Minecraft.getMinecraft().isGamePaused()) {
    				try {
						Thread.sleep(500);
					} catch(InterruptedException e) {
						sender.sendMessage(new TextComponentString("Test interrupted while game paused."));
					}
    			}
    			
    			for (TileEntityIOBase match: matches) {
    				boolean curstate = match.isPowered();
    				if (!set) {
    					set = true;
    					state = curstate;
    				}
    				if (curstate != state) {
    					consistent = false;
    					break;
    				}
    			}
    			
    			if (consistent) {
    				if (!(matches.get(0).isPowered() == key)) {
    					testPassed = false;
    					results += "false\n";
    				}
    				else {
    					results += "true\n";
    				}
    				actual += (matches.get(0).isPowered() ? 1 : 0) + " ";
    			}
    			else {
    				testPassed = false;
					actual += "e ";
					results += "false \n";
    			}
    			
    		}
    		
    		String outTest = Arrays.toString(Arrays.copyOfRange(run, CommandLoad.inputs.length, run.length));
    		if (!fullrun) {
    			outTest = run[target + CommandLoad.inputs.length];
    		}
    		TextComponentString out = new TextComponentString(Arrays.toString(Arrays.copyOf(run, CommandLoad.inputs.length)) + " | " + outTest + "  |  " + actual + " | " + ((testPassed) ? " P" : " F"));
    		if (!testPassed) {
    			out.getStyle().setColor(TextFormatting.DARK_RED);
    		}
    		
    		if (!(filter && testPassed))
    			sender.sendMessage(out);
    		
    		
    		
    		for (TileEntityIOBase i: inputs) {
    			i.setPowerState(false);
    		}
    		
    	}

    	test.setResult(results);
    	if (fullrun)
    		CommandSubmit.mostRecentTestRun = test;
		sender.sendMessage(new TextComponentString("Test finished successfully."));
    }

	private static List<TileEntityOutput> getOutputMatches(String key, List<TileEntityOutput> l) {
		List<TileEntityOutput> matches = new LinkedList<TileEntityOutput>(); 
		
		for (TileEntityOutput t: l) {
			if (key.equals("" + t.getTag())) {
				matches.add(t);
			}
		}
		
		return matches;
	}

	public static List<TileEntityInput> getInputMatches(String key, List<TileEntityInput> l) {
		List<TileEntityInput> matches = new LinkedList<TileEntityInput>(); 
		
		for (TileEntityInput t: l) {
			if (key.equals(t.getTag())) {
				matches.add(t);
			}
		}
		
		return matches;
	}

	private static List<TileEntityOutput> getOutputs(List<TileEntity> entities) {
		LinkedList<TileEntityOutput> valids = new LinkedList<TileEntityOutput>();

		for (TileEntity t : entities) {
			if(t instanceof TileEntityOutput) {
				valids.add((TileEntityOutput) t);
			}
		}

		return valids;
	}

	static List<TileEntityInput> getInputs(List<TileEntity> entities) {
		LinkedList<TileEntityInput> valids = new LinkedList<TileEntityInput>();
		
		for (TileEntity t : entities) {
			if(t instanceof TileEntityInput) {
				valids.add((TileEntityInput) t);
			}
		}
		
		return valids;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Run the passed project's test build";
	}
	
	public static class TestRun {
		String testResults;
		
		public TestRun() {
			testResults = null;
		}
		
		public void setResult(String result) {
			testResults = result;
		}
	}
}
