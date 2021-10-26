package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import com.github.smallru8.NikoBot.Core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class Container{
	private boolean runFlag = false;
	
	private Message msgInterface;
	public long id = 0;//Random Long
	public int port = 0;
	public String name = null;//Map name
	public String owner = null;//username who is the container's owner
	private String version = "1.8.4";//game version
	private String workDir = MinecraftAUTO.workDir;
	
	private String CMD = "";
	
	private String RAM = "4G";
	private String JVM_ARGUMENTS = "";
	private String javaPath = "Java/Linux64/Java";
	
	private Process process;
	
	public OutputStream processStdInput;
	
	/**
	 * Create container
	 * @param mapName
	 * @param username
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Container(Message msg, String mapName, String username) throws FileNotFoundException, IOException {
		name = mapName;
		owner = username;
		this.msgInterface = msg;
		this.id = msg.getIdLong();
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
			
			//Save starting option
			Properties status = new Properties();
			status.setProperty("id", ""+id);
			status.setProperty("CMD", CMD);
			status.setProperty("name", name);
			status.setProperty("owner", owner);
			status.setProperty("version", version);
			status.store(new FileOutputStream(new File(CT_F,"oneMinecraft")), "");		
			status.clear();
			
			//複製伺服器檔案到CT
			setTitle(":arrows_counterclockwise: Copy server file to container...");
			File serverFile = new File("Servers",version);
			Util.copy(serverFile, CT_F);
			
			//複製地圖資料
			setTitle(":arrows_counterclockwise: Copy map data to container...");
			File CT_World = new File(CT_F,"world");
			File map = new File("MinecraftMap/"+name+"/world");
			Util.copy(map, CT_World);

			
			//伺服器設定檔
			setTitle(":arrows_counterclockwise: Copy server.properties...");
			File serverProperties = new File("MinecraftMap/"+name+"/server.properties");
			Util.copy(serverProperties, new File(CT_F,"server.properties"));
			
			//啟動檔
			setTitle(":arrows_counterclockwise: Copy launch file...");
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
			
		}
		updateStatustoDone();
	}
	
	/**
	 * Recovery container
	 * @param id
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public Container(Message msg) throws FileNotFoundException, IOException {
		this.msgInterface = msg;
		this.id = msg.getIdLong();
		workDir += "/"+id;
		File CT_F = new File(workDir);
		Properties status = new Properties();
		status.load(new FileInputStream(new File(CT_F,"oneMinecraft")));
		this.id = Long.parseLong(status.getProperty("id"));
		CMD = status.getProperty("CMD");
		name = status.getProperty("name");
		owner = status.getProperty("owner");
		version = status.getProperty("version");
		status.clear();
			
		updateStatustoDone();
	}
	
	public String[] info() {
		return new String[] {""+id,name,owner,version,""+port};
	}
	
	private void setTitle(String t) {
		msgInterface.editMessageEmbeds(new EmbedBuilder().setTitle(t).setDescription(msgInterface.getEmbeds().get(0).getDescription()).build()).queue();
	}
	private void updateStatustoDone() {
		msgInterface.editMessageEmbeds(new EmbedBuilder().setTitle(":stop_button:").setDescription(msgInterface.getEmbeds().get(0).getDescription()).build()).queue(message -> {
			message.addReaction("▶").queue();
			message.addReaction("⏹").queue();
			message.addReaction("🚫").queue();
		});
	}
	private void updateStatustoRun() {
		runFlag = true;
		msgInterface.editMessageEmbeds(new EmbedBuilder().setTitle(":arrow_forward:").setDescription(msgInterface.getEmbeds().get(0).getDescription()).build()).queue();
	}
	private void updateStatustoStop() {
		runFlag = false;
		msgInterface.editMessageEmbeds(new EmbedBuilder().setTitle(":stop_button:").setDescription(msgInterface.getEmbeds().get(0).getDescription()).build()).queue();
	}
	
	public boolean isRunning() {
		return runFlag&&process.isAlive();
	}
	
	public Container startContainer(int port) {
		if(name==null)
			return null;
		else if(isRunning())//Already start
			return this;
		else if(!MinecraftAUTO.dockerMode) {
			try {
				if(port!=-1) {
					//Set static port
					Properties launchSetting = new Properties();
					launchSetting.load(new FileInputStream(new File("MinecraftMap/"+name+"/lanuch.yml")));
					this.port = Integer.parseInt(launchSetting.getProperty("port", ""+port));
					launchSetting.clear();
					//Set dynamic port
					Properties serverP = new Properties();
					serverP.load(new FileInputStream(new File(workDir,"server.properties")));
					serverP.setProperty("server-port", ""+this.port);
					serverP.store(new FileOutputStream(new File(workDir,"server.properties")), "");
					serverP.clear();
				}
				
				String output = "Map: " + name + "\nOwner: " + owner + "\nPort: "+this.port;
				msgInterface.editMessageEmbeds(new EmbedBuilder().setTitle(msgInterface.getEmbeds().get(0).getTitle()).setDescription(output).build()).queue();
				
				
				File f;
				if(Core.osType) {//windows
					f = new File(workDir+"/launch.bat");
					process = new ProcessBuilder(f.getAbsolutePath()).directory(new File(workDir)).redirectError(new File(workDir,"error.log")).redirectOutput(new File(workDir,"Output.log")).start();
					//process = new ProcessBuilder(f.getAbsolutePath()).directory(new File(workDir)).redirectError(new File(workDir,"error.log")).start();
					
				}else{//linux
					f = new File(workDir+"/launch.sh");
					process = new ProcessBuilder("sh",f.getAbsolutePath()).directory(new File(workDir)).redirectError(new File(workDir,"error.log")).redirectOutput(new File(workDir,"Output.log")).start();
					//process = new ProcessBuilder("sh",f.getAbsolutePath()).directory(new File(workDir)).redirectError(new File(workDir,"error.log")).start();
				}
				//processStdOutput = process.getInputStream();
				processStdInput = process.getOutputStream();
				processStdInput.write("reload\n".getBytes());
				processStdInput.write("save-on\n".getBytes());
				processStdInput.flush();
				
				
			} catch (IOException e) {
				e.printStackTrace();
				process.destroy();
				return null;
			}
			updateStatustoRun();
			return this;
		}
		return null;
	}
	
	public Container stopContainer() {
		if(!isRunning())//Already stop
			return this;
		if(!MinecraftAUTO.dockerMode) {
			OutputStream processOutputStream = process.getOutputStream();
			try {
				processOutputStream.write("stop\n".getBytes());
				processOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			process.destroy();
		}
		updateStatustoStop();
		return this;
	}
	
	/**
	 * Delete container file
	 * @return
	 */
	public Container deleteContainer() {
		//Delete file
		File dir = new File(workDir);
		String[] fileLs = dir.list();
		for(int i=0;i<fileLs.length;i++)
			Util.deleteFile(new File(workDir));
		
		msgInterface.delete().queue();
		return this;
	}
	
}
