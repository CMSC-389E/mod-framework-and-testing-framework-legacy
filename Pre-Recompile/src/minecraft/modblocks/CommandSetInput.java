package modblocks;

import java.util.List;
import com.unittest.tileentity.TileEntityInput;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

public class CommandSetInput extends CommandBase {

	public String getName() {
		return "set";
	}
	
    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
    
    
    
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {	
    
    	
    	List<TileEntity> entities = sender.getEntityWorld().loadedTileEntityList;
    	List<TileEntityInput> inputs = CommandTest.getInputs(entities);
    	
    	if (args.length != 2  && !(args.length == 1 && args[0].equals("off"))) {
    		throw new CommandException("You must provide the desired input block as well as the desired state");
    	}
    	
    	boolean state = false;
    	if (args.length == 2) state = Boolean.parseBoolean(args[1]);
    	
    	if ("off".equals(args[0])) {
    		for (TileEntityInput input: inputs) {
    			input.setPowerState(false);
    		}
    		return;
    	}
    	List<TileEntityInput> matches = CommandTest.getInputMatches("i" + args[0].toUpperCase(), inputs);
    	for (TileEntityInput match: matches) {
    		match.setPowerState(state);
    	}
    	
    	
    	
    	
    }




	@Override
	public String getUsage(ICommandSender sender) {
		return "Set an input block to a desired state";
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
