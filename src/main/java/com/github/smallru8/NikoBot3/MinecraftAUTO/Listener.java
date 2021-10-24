package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;

import com.github.smallru8.NikoBot.Embed;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter{
	
	private static Container CT = null;
	public static String username = "";
	
	public static String title = ":bookmark_tabs:\n";
	public static String head = "";
	
	public static String status1 = "[:heavy_multiplication_x:]Clear container...\n";
	public static String status2 = "[:heavy_multiplication_x:]Copy server file to container...\n";
	public static String status3 = "[:heavy_multiplication_x:]Copy map data to container...\n";
	public static String status4 = "[:heavy_multiplication_x:]Setting server properties...\n";
	public static String status5 = "[:heavy_multiplication_x:]Generating launch script...\n";
	public static String status6 = "[:heavy_multiplication_x:]Server started!\n";
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent gmre) {
		if(gmre.getChannel().getId().equalsIgnoreCase(MinecraftAUTO.chId)) {
			String rawCMD = gmre.getMessage().getContentRaw();
			if(rawCMD.startsWith("/minecraft")) {
				String[] cmds = rawCMD.split(" ");
				
				if(cmds.length==2&&cmds[1].equalsIgnoreCase("help")) {//help
					String output = "Start server : /minecraft start <number>\nClose server : /minecraft stop\n";
					Embed.EmbedSender(Color.PINK, gmre.getChannel(), ":regional_indicator_h::regional_indicator_e::regional_indicator_l::regional_indicator_p:", output);
				}
				
				if(cmds.length==2&&cmds[1].equalsIgnoreCase("list")) {//顯示小遊戲列表
					showLs(gmre.getChannel());
					gmre.getChannel().sendMessage(title).queue(m -> {
						MinecraftAUTO.setMsg(m.getId());
					});
				}
				
				if(cmds.length==3&&cmds[1].equalsIgnoreCase("start")&&Util.isDigitOnly(cmds[2])) {//啟動
					username = gmre.getAuthor().getName();
					createCT(gmre.getChannel(),Integer.parseInt(cmds[2]));				
				}
				
				if(cmds.length==2&&cmds[1].equalsIgnoreCase("stop")) {//關閉
					CT.stop();
					//gmre.getChannel().sendMessage("Close server.").queue();
					CT = null;
					head = "";
					status1 = Util.replacetoX(status1);
					status2 = Util.replacetoX(status2);
					status3 = Util.replacetoX(status3);
					status4 = Util.replacetoX(status4);
					status5 = Util.replacetoX(status5);
					status6 = Util.replacetoX(status6);
					updateMsg(gmre.getChannel());
				}
				
				
				gmre.getMessage().delete().queue();
			}
		}
	}
	
	/**
	 * 顯示小遊戲列表
	 * @param tc
	 */
	public void showLs(TextChannel tc) {
		File dir = new File("MinecraftMap");
		String[] fileLs = dir.list();
		Arrays.sort(fileLs);
		String output = "";
		for(int i=0;i<fileLs.length;i++) {
			output+=fileLs[i]+"\n";
		}
		Embed.EmbedSender(Color.PINK, tc, ":page_facing_up:", output);
	}
	
	public static void updateMsg(TextChannel tc) {
		String statusOutput = title+head+status1+status2+status3+status4+status5+status6;
		tc.editMessageById(MinecraftAUTO.msgId, statusOutput).queue();
	}
	
	public void createCT(TextChannel tc,int num) {
		if(CT==null) {
			CT = new Container(tc,num);
			Listener.status6 = Util.replacetoRunning(Listener.status6);
			Listener.updateMsg(tc);
			if(!CT.start()) {
				CT=null;
				//tc.sendMessage("Server starting error.").queue();
			}else {
				//tc.sendMessage("Server started!").queue();
				Listener.status6 = Util.replacetoDone(Listener.status6);
				Listener.updateMsg(tc);
			}
		}else {
			//tc.sendMessage("Server is busy: Now running "+CT.name).queue();
		}
	}
	
	
}
