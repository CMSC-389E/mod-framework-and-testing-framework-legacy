package modblocks;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandAbort extends CommandBase {

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (CommandTest.runningTest != null) {
			CommandTest.runningTest.stop();
			
			sender.sendMessage(new TextComponentString("Current test aborted"));
		}
		else {
			sender.sendMessage(new TextComponentString("There is no test currently running"));
		}

	}

	public String getName() {
		return "abort";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Halts any currently running tests.";
	}
}
