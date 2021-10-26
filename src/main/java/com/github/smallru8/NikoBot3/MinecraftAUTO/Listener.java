package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.github.smallru8.NikoBot.Core;
import com.github.smallru8.NikoBot.Embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter{

	public static String username = "";
	
	private Role managerRole = Core.botAPI.getRoleById(MinecraftAUTO.managerGroup);
	private ArrayList<String> gameFileList;
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent gmre) {
		if(gmre.getChannel().getIdLong()==MinecraftAUTO.chId) {
			String rawCMD = gmre.getMessage().getContentRaw();
			if(rawCMD.startsWith("/minecraft")) {
				String[] cmds = rawCMD.split(" ");
				
				//管理員限定
				//Show help
				if(cmds.length==2&&cmds[1].equalsIgnoreCase("help")&&Core.ADMINS.isAdmin(gmre.getGuild().getId(), gmre.getAuthor().getId())) {//help
					String output = "Create container : /minecraft create <number>\n";
					Embed.EmbedSender(Color.PINK, gmre.getChannel(), ":regional_indicator_h::regional_indicator_e::regional_indicator_l::regional_indicator_p:", output);
				}
				
				//管理員限定
				//Show game list
				else if(cmds.length==2&&cmds[1].equalsIgnoreCase("list")&&Core.ADMINS.isAdmin(gmre.getGuild().getId(), gmre.getAuthor().getId())) {//顯示小遊戲列表
					gmre.getChannel().sendMessageEmbeds(new EmbedBuilder().setTitle(":page_facing_up:").setDescription(showLs()).build()).queue(m -> {
						MinecraftAUTO.setMsg(m.getIdLong());
						MinecraftAUTO.setChannel(m.getChannel().getIdLong());
					});
				}
				
				//managerGroup或管理員
				//Create container
				else if(cmds.length==3&&cmds[1].equalsIgnoreCase("create")&&Util.isDigitOnly(cmds[2])&&(gmre.getMember().getRoles().contains(managerRole)||Core.ADMINS.isAdmin(gmre.getGuild().getId(), gmre.getAuthor().getId()))) {//Create
					MinecraftAUTO.CM.addContainer(gameFileList.get(Integer.parseInt(cmds[2])), gmre.getAuthor().getName());
				}
			}
			//Direct command
			else if(gmre.getMessage().getReferencedMessage()!=null&&(gmre.getMember().getRoles().contains(managerRole)||Core.ADMINS.isAdmin(gmre.getGuild().getId(), gmre.getAuthor().getId()))) {
				long targetMsgId = gmre.getMessage().getReferencedMessage().getIdLong();
				Container ct = MinecraftAUTO.CM.findCTbyId(targetMsgId);
				if(ct != null && ct.isRunning())
					try {
						ct.processStdInput.write((gmre.getMessage().getContentRaw()+"\n").getBytes());
						ct.processStdInput.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			if(gmre.getAuthor().getIdLong()!=Core.botAPI.getSelfUser().getIdLong())//不是自己送出的訊息都刪除
				gmre.getMessage().delete().queue();
		}
	}
	
	/**
	 * 讀取小遊戲列表
	 * @param tc
	 */
	private String showLs() {
		File dir = new File("MinecraftMap");
		String[] fileLs = dir.list();
		gameFileList = new ArrayList<String>(Arrays.asList(fileLs));
		
		String output = "";
		for(int i=0;i<gameFileList.size();i++) {
			output+="["+i+"]"+gameFileList.get(i)+"\n";
		}
		return output;
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(event.getMember().getRoles().contains(managerRole)||Core.ADMINS.isAdmin(event.getGuild().getId(),event.getUserId())&&event.getUserIdLong()!=Core.botAPI.getSelfUser().getIdLong()&&event.getTextChannel().getIdLong()==MinecraftAUTO.chId) {
			if(event.getMessageIdLong()==MinecraftAUTO.msgId&&event.getReactionEmote().getEmoji().equals("🔄")){//刷新game list
				event.getReaction().removeReaction(event.getUser()).queue();
				event.getChannel().editMessageEmbedsById(MinecraftAUTO.msgId, new EmbedBuilder().setTitle(":page_facing_up:").setDescription(showLs()).build()).queue();
			}
			else if(MinecraftAUTO.CM.findCTbyId(event.getMessageIdLong())!=null) {
				long msgId = event.getMessageIdLong();
				if(event.getReactionEmote().getEmoji().equals("▶")) {
					event.getReaction().removeReaction(event.getUser()).queue();
					MinecraftAUTO.CM.startContainerbyId(msgId);
				}else if(event.getReactionEmote().getEmoji().equals("⏹")) {
					event.getReaction().removeReaction(event.getUser()).queue();
					MinecraftAUTO.CM.stopContainerbyId(msgId);
				}else if(event.getReactionEmote().getEmoji().equals("🚫")&&!MinecraftAUTO.CM.findCTbyId(msgId).isRunning()) {
					event.getReaction().removeReaction(event.getUser()).queue();
					MinecraftAUTO.CM.delContainer(msgId);
				}
			}
		}else if(event.getTextChannel().getIdLong()==MinecraftAUTO.chId) {
			event.getReaction().removeReaction(event.getUser()).queue();
		}
	}
	
}
