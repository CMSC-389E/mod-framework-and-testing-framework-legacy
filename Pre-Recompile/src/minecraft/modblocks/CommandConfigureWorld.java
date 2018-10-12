package modblocks;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandConfigureWorld extends CommandBase {
	ServerCommandManager manager;
	
	public CommandConfigureWorld(ServerCommandManager backreference) {
		manager = backreference;
	}
	
	

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
	
		manager.executeCommand(sender, "/gamerule doMobSpawning false");
		manager.executeCommand(sender, "/weather clear 1000000");
		manager.executeCommand(sender, "/time set day");
		manager.executeCommand(sender, "/gamerule doDaylightCycle false");
		manager.executeCommand(sender, "/kill @e[type=!Player]");
		
		sender.sendMessage(new TextComponentString("Cleaned world."));
		
	}

	public String getName() {
		return "clean";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Cleanse the impure";
	}
}
