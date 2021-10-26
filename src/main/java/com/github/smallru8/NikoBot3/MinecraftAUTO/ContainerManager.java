package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class ContainerManager{

	//Containers
	private ArrayList<Container> CTs = new ArrayList<Container>();
	
	private TextChannel tc;
	
	public ContainerManager(TextChannel tc) {//Need start
		//意外重啟，恢復先前Container
		this.tc = tc;
		recoveryContainer();		
	}
	
	public void recoveryContainer() {
		if(!MinecraftAUTO.dockerMode) {
			File fd = new File(MinecraftAUTO.workDir);
			String[] fLs = fd.list();
			for(int i=0;i<fLs.length;i++) {
				int port = findPort();
				if(port!=-1) {
					tc.retrieveMessageById(fLs[i]).queue(message -> {
						try {
							CTs.add(new Container(message,port).startContainer());
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
			}
		}
	}
	
	/**
	 * 新增Container
	 * @param mapName
	 * @param owner
	 */
	public void addContainer(String mapName,String owner) {
		try {
			int port = findPort();
			if(port==-1)
				throw new Exception("No available port.");
			//run: :arrow_forward:
			//stop: :stop_button:
			//loading: :arrows_counterclockwise:
			String status = ":arrows_counterclockwise:";
			String output = "Map: " + mapName + "\nOwner: " + owner + "\nPort: " + port;
			EmbedBuilder embed = new EmbedBuilder().setColor(java.awt.Color.PINK).setTitle(status).setDescription(output);
			tc.sendMessageEmbeds(embed.build()).queue(message -> {
				new Thread(()->{
					Container ct;
					try {
						ct = new Container(message,mapName,owner,port);
						CTs.add(ct);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 停止並刪除Container
	 * @param id
	 */
	public void delContainer(Long id) {
		Container ct = findCTbyId(id);
		CTs.remove(ct.stopContainer().deleteContainer());
	}
	
	/**
	 * 啟動Container
	 * @param id
	 */
	public void startContainerbyId(Long id) {
		findCTbyId(id).startContainer();
	}
	
	/**
	 * 停止Container
	 * @param id
	 */
	public void stopContainerbyId(Long id) {
		findCTbyId(id).stopContainer();
	}
	
	public Container findCTbyId(Long id) {
		for(int i=0;i<CTs.size();i++) {
			if(CTs.get(i).id==id) {
				return CTs.get(i);
			}
		}
		return null;
	}
	
	private int findPort() {
		for(int i=0;i<MinecraftAUTO.portMax-MinecraftAUTO.portMin+1;i++) {
			if(Util.portisAvailable(MinecraftAUTO.portMin+i)) {
				return MinecraftAUTO.portMin+i;
			}
		}
		return -1;
	}
	
}
