package scripting;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

public class FilterCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "filter";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/filter <script> or /filter for help";
	}

	//TODO 
	@Override
	public void processCommand(ICommandSender icommandsender, String[] args) {
		if (args.length == 0) {
			List<String> filters = ScriptingMod.instance.getServerCore().getFilterScripts();
			String str = (filters.isEmpty()) ? "No filter scripts available" : "Available filters: ";
			for (Iterator<String> it = filters.iterator(); it.hasNext();) {
				str += it.next();
				if (it.hasNext())
					str += ", ";
			}
			icommandsender.sendChatToPlayer(ChatMessageComponent.func_111077_e(str));
		}
		else if (args.length == 1) 
			ScriptingMod.instance.getServerCore().runFilter((EntityPlayer)icommandsender, args[0]);
		else 
			throw new WrongUsageException(getCommandUsage(icommandsender), new Object[0]);
	}

	public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] arr)	{
		return (arr.length == 1) ?  ScriptingMod.instance.getServerCore().getFilterScripts() : null;
	}
	
	public boolean canCommandSenderUseCommand(ICommandSender s){
		return s instanceof EntityPlayer && super.canCommandSenderUseCommand(s);
	}

}