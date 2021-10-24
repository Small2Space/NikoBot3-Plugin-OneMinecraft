package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Properties;

import com.github.smallru8.NikoBot.Core;

import net.dv8tion.jda.api.entities.TextChannel;

public class Container {

	private String workDir = "CT";
	private String version = "1.8.4";
	private String CMD = "";
	public String name = null;
	private String RAM = "4G";
	private String JVM_ARGUMENTS = "";
	private String javaPath = "Java/Linux64/Java";
	
	private Process process;
	
	public Container(TextChannel tc,int num) {		
		File dir = new File("MinecraftMap");
		for(int i=0;i<dir.list().length;i++) {
			if(dir.list()[i].startsWith(num+"-")) {
				name = dir.list()[i];
				Listener.head = "Map: "+name+"\nUser: "+Listener.username+"\n";
				break;
			}
		}
		if(name!=null) {
			if(Core.osType) {//windows
				javaPath="Java/Windows64/Java";
			}
			
			if((version = name.split("-")[1]).startsWith("1.17")) {//要用Java16
				javaPath+="16/bin/java";
			}else {
				javaPath+="8/bin/java";
			}

			javaPath = new File(javaPath).getAbsolutePath();
			
			try {
				Properties p = new Properties();
				p.load(new FileInputStream("launch.yml"));
				RAM = p.getProperty("RAM", RAM);
				JVM_ARGUMENTS = p.getProperty("JVM_ARGUMENTS", JVM_ARGUMENTS);
				workDir = p.getProperty("WORKDIR", workDir);
				p.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			File CT_F = new File(workDir);
			CMD = javaPath+" -jar -Xms1G -Xmx"+RAM+" "+JVM_ARGUMENTS+" minecraft_server.jar --nogui";
			
			//清空CT
			//tc.sendMessage("Clear container...").queue();
			Listener.status1 = Util.replacetoRunning(Listener.status1);
			Listener.updateMsg(tc);
			String[] ls = CT_F.list();
			for(int i=0;i<ls.length;i++)
				Util.deleteFile(new File(workDir+"/"+ls[i]));
			Listener.status1 = Util.replacetoDone(Listener.status1);
			Listener.updateMsg(tc);
			
			
			//複製伺服器檔案到CT
			//tc.sendMessage("Copy "+version+"server file to container...").queue();
			Listener.status2 = Util.replacetoRunning(Listener.status2);
			Listener.updateMsg(tc);
			File serverFile = new File("Servers",version);
			Util.copy(serverFile, CT_F);
			Listener.status2 = Util.replacetoDone(Listener.status2);
			Listener.updateMsg(tc);
			
			//複製地圖資料
			//tc.sendMessage("Copy map data to container...").queue();
			Listener.status3 = Util.replacetoRunning(Listener.status3);
			Listener.updateMsg(tc);
			File CT_World = new File(CT_F,"world");
			File map = new File("MinecraftMap/"+name+"/world");
			Util.copy(map, CT_World);
			Listener.status3 = Util.replacetoDone(Listener.status3);
			Listener.updateMsg(tc);
			
			//伺服器設定檔
			//tc.sendMessage("Setting server properties...").queue();
			Listener.status4 = Util.replacetoRunning(Listener.status4);
			Listener.updateMsg(tc);
			File serverProperties = new File("MinecraftMap/"+name+"/server.properties");
			Util.copy(serverProperties, new File(CT_F,"server.properties"));
			Listener.status4 = Util.replacetoDone(Listener.status4);
			Listener.updateMsg(tc);
			
			//啟動檔
			//tc.sendMessage("Generating launch script...").queue();
			Listener.status5 = Util.replacetoRunning(Listener.status5);
			Listener.updateMsg(tc);
			try {
				File f = new File(workDir+"/launch.sh");
				if(Core.osType)
					f = new File(workDir+"/launch.bat");
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				fw.write(CMD);
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Listener.status5 = Util.replacetoDone(Listener.status5);
			Listener.updateMsg(tc);
		}
	}
	
	public boolean start() {
		if(name==null)
			return false;
		try {
			File f;
			if(Core.osType) {//windows
				f = new File(workDir+"/launch.bat");
				process = new ProcessBuilder(f.getAbsolutePath()).directory(new File(workDir)).redirectError(new File(workDir,"error.log")).redirectOutput(new File(workDir,"Output.log")).start();
			}else{//linux
				f = new File(workDir+"/launch.sh");
				process = new ProcessBuilder("sh",f.getAbsolutePath()).directory(new File(workDir)).redirectError(new File(workDir,"error.log")).redirectOutput(new File(workDir,"Output.log")).start();

			}
			
			OutputStream processOutputStream = process.getOutputStream();
			processOutputStream.write("reload\n".getBytes());
			processOutputStream.write("save-on\n".getBytes());
			processOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			process.destroy();
			return false;
		}
		return true;
	}
	
	public void stop() {
		OutputStream processOutputStream = process.getOutputStream();
		try {
			processOutputStream.write("stop\n".getBytes());
			processOutputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		process.destroy();
	}
	
}
