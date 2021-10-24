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

	public static String chId = "";
	public static String msgId = "";
	
	@Override
	public void onDisable() {
		
	}

	@Override
	public void onEnable() {
		Core.botAPI.addEventListener(new Listener());
		if(!new File("conf.d/MinecraftAUTO").exists()) {
			try {
				FileWriter fw = new FileWriter(new File("conf.d/MinecraftAUTO"));
				fw.write("channel = \n");
				fw.write("msg = \n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(new File("conf.d/MinecraftAUTO")));
				chId = p.getProperty("channel","0");
				msgId = p.getProperty("msg", "0");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String pluginsName() {
		return "MinecraftAUTO";
	}
	
	public static void setChannel(String id) {
		chId = id;
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(new File("conf.d/MinecraftAUTO")));
			p.setProperty("channel", id);
			p.store(new FileOutputStream(new File("conf.d/MinecraftAUTO")), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setMsg(String id) {
		msgId = id;
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(new File("conf.d/MinecraftAUTO")));
			p.setProperty("msg", id);
			p.store(new FileOutputStream(new File("conf.d/MinecraftAUTO")), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
