package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Properties;
import java.util.Random;

import com.github.smallru8.NikoBot.Core;

public class Container {

	public int id = 0;//Random integer
	public int port = 25565;
	public String name = null;//Map name
	public String owner = null;//username who is the container's owner
	private String version = "1.8.4";//game version
	private String workDir = MinecraftAUTO.workDir;
	
	private String CMD = "";
	
	private String RAM = "4G";
	private String JVM_ARGUMENTS = "";
	private String javaPath = "Java/Linux64/Java";
	
	private Process process;
	
	/**
	 * Create container
	 * @param mapName
	 * @param username
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Container(String mapName, String username, int port) throws FileNotFoundException, IOException {
		name = mapName;
		owner = username;
		id = new Random().nextInt();
		//非Docker mode
		if(name!=null && !MinecraftAUTO.dockerMode) {
			if(Core.osType) {//windows
				javaPath="Java/Windows64/Java";
			}
			Properties launchSetting = new Properties();
			launchSetting.load(new FileInputStream(new File("MinecraftMap/"+name+"/lanuch.yml")));
			RAM = launchSetting.getProperty("RAM", "4G");
			JVM_ARGUMENTS  = launchSetting.getProperty("JVM_ARGUMENTS", "");
			version = launchSetting.getProperty("version", "1.8.4");
			this.port = Integer.parseInt(launchSetting.getProperty("port", ""+port));
			launchSetting.clear();
			
			if(version.matches("(1.1|1.2|1.3|1.4|1.5|1.6|1.7|1.8|1.9|1.10|1.11|1.12|1.13|1.14|1.15|1.16).*")) {
				javaPath+="8/bin/java";
			}else{//1.17 later
				javaPath+="16/bin/java";
			}

			javaPath = new File(javaPath).getAbsolutePath();
			workDir += "/"+id;

			File CT_F = new File(workDir);
			CT_F.mkdir();
			
			
			CMD = javaPath+" -jar -Xms1G -Xmx"+RAM+" "+JVM_ARGUMENTS+" minecraft_server.jar --nogui";
			
			Properties status = new Properties();
			status.setProperty("id", ""+id);
			status.setProperty("CMD", CMD);
			status.setProperty("name", name);
			status.setProperty("owner", owner);
			status.setProperty("version", version);
			status.store(new FileOutputStream(new File(CT_F,"oneMinecraft")), "");		
			status.clear();
			//複製伺服器檔案到CT
			//tc.sendMessage("Copy "+version+"server file to container...").queue();
			//Listener.status2 = Util.replacetoRunning(Listener.status2);
			//Listener.updateMsg(tc);
			File serverFile = new File("Servers",version);
			Util.copy(serverFile, CT_F);
			//Listener.status2 = Util.replacetoDone(Listener.status2);
			//Listener.updateMsg(tc);
			
			//複製地圖資料
			//tc.sendMessage("Copy map data to container...").queue();
			//Listener.status3 = Util.replacetoRunning(Listener.status3);
			//Listener.updateMsg(tc);
			File CT_World = new File(CT_F,"world");
			File map = new File("MinecraftMap/"+name+"/world");
			Util.copy(map, CT_World);
			//Listener.status3 = Util.replacetoDone(Listener.status3);
			//Listener.updateMsg(tc);
			
			//伺服器設定檔
			//tc.sendMessage("Setting server properties...").queue();
			//Listener.status4 = Util.replacetoRunning(Listener.status4);
			//Listener.updateMsg(tc);
			File serverProperties = new File("MinecraftMap/"+name+"/server.properties");
			Util.copy(serverProperties, new File(CT_F,"server.properties"));
			Properties serverP = new Properties();
			serverP.load(new FileInputStream(new File(CT_F,"server.properties")));
			serverP.setProperty("server-port", ""+this.port);
			serverP.store(new FileOutputStream(new File(CT_F,"server.properties")), "");
			serverP.clear();
			//Listener.status4 = Util.replacetoDone(Listener.status4); 
			//Listener.updateMsg(tc);
			
			//啟動檔
			//tc.sendMessage("Generating launch script...").queue();
			//Listener.status5 = Util.replacetoRunning(Listener.status5);
			//Listener.updateMsg(tc);
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
			//Listener.status5 = Util.replacetoDone(Listener.status5);
			//Listener.updateMsg(tc);
		}
	}
	
	/**
	 * Recovery container
	 * @param id
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public Container(int id, int port) throws FileNotFoundException, IOException {
		workDir += "/"+id;
		File CT_F = new File(workDir);
		Properties status = new Properties();
		status.load(new FileInputStream(new File(CT_F,"oneMinecraft")));
		this.id = Integer.parseInt(status.getProperty("id"));
		CMD = status.getProperty("CMD");
		name = status.getProperty("name");
		owner = status.getProperty("owner");
		version = status.getProperty("version");
		status.clear();
		
		//Set static port
		Properties launchSetting = new Properties();
		launchSetting.load(new FileInputStream(new File("MinecraftMap/"+name+"/lanuch.yml")));
		this.port = Integer.parseInt(launchSetting.getProperty("port", ""+port));
		launchSetting.clear();
		
		//Set dynamic port
		Properties serverP = new Properties();
		serverP.load(new FileInputStream(new File(CT_F,"server.properties")));
		serverP.setProperty("server-port", ""+this.port);
		serverP.store(new FileOutputStream(new File(CT_F,"server.properties")), "");
		serverP.clear();
		
		start();
	}
	
	public String[] info() {
		return new String[] {""+id,name,owner,version,""+port};
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
			e.printStackTrace();
		}
		
		process.destroy();
	}
	
}
