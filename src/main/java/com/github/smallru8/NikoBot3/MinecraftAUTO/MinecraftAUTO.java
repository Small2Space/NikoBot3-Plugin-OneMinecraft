package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import com.github.smallru8.NikoBot.Core;
import com.github.smallru8.NikoBot.plugins.PluginsInterface;

public class MinecraftAUTO implements PluginsInterface {

	public static boolean dockerMode = false;
	public static String workDir = "CT";
	public static long chId;
	public static long msgId;
	public static int portMin = 25565;
	public static int portMax = 25575;
	public static long managerGroup;
	
	public static ContainerManager CM;
	
	@Override
	public void onDisable() {
		
	}

	@Override
	public void onEnable() {
		Core.botAPI.addEventListener(new Listener());
		if(!new File("conf.d/MinecraftAUTO").exists()) {
			try {
				FileWriter fw = new FileWriter(new File("conf.d/MinecraftAUTO"));
				fw.write("docker = false\n");
				fw.write("#Don't edit this line");
				fw.write("channel = \n");
				fw.write("#Don't edit this line");
				fw.write("msg = \n");
				fw.write("workDir = CT\n");
				fw.write("portMin = 25565\n");
				fw.write("portMax = 25575\n");
				fw.write("#Discord role id, user who has this role can create a container.");
				fw.write("managerGroup = ");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(new File("conf.d/MinecraftAUTO")));
				dockerMode = p.getProperty("docker").equalsIgnoreCase("true");
				chId = Long.parseLong(p.getProperty("channel","0"));
				msgId = Long.parseLong(p.getProperty("msg","0"));
				workDir = p.getProperty("workDir", "CT");
				portMin = Integer.parseInt(p.getProperty("portMin", "25565"));
				portMax = Integer.parseInt(p.getProperty("portMax", "25575"));
				managerGroup = Long.parseLong(p.getProperty("managerGroup"));
				
				CM = new ContainerManager(Core.botAPI.getTextChannelById(chId));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String pluginsName() {
		return "MinecraftAUTO";
	}
	
	public static void setChannel(long id) {
		chId = id;
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(new File("conf.d/MinecraftAUTO")));
			p.setProperty("channel", ""+id);
			p.store(new FileOutputStream(new File("conf.d/MinecraftAUTO")), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setMsg(long id) {
		msgId = id;
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(new File("conf.d/MinecraftAUTO")));
			p.setProperty("msg", ""+id);
			p.store(new FileOutputStream(new File("conf.d/MinecraftAUTO")), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
