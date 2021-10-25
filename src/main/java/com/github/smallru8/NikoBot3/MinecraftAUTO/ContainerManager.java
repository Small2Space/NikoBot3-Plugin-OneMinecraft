package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ContainerManager extends Thread{

	//Containers
	private ArrayList<Container> CTs = new ArrayList<Container>();
	
	private Object lock = new Object();
	
	public ContainerManager() {//Need start
		//意外重啟，恢復先前Container
		recoveryContainer();		
		//TODO
		start();
	}
	
	public void recoveryContainer() {
		if(!MinecraftAUTO.dockerMode) {
			File fd = new File(MinecraftAUTO.workDir);
			String[] fLs = fd.list();
			for(int i=0;i<fLs.length;i++) {
				int port = findPort();
				if(port!=-1) {
					try {
						CTs.add(new Container(Integer.parseInt(fLs[i]),port).startContainer());
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * 新增並啟動Container
	 * @param mapName
	 * @param owner
	 */
	public void addContainer(String mapName,String owner) {
		Container ct;
		try {
			int port = findPort();
			if(port==-1)
				throw new Exception("No available port.");
			ct = new Container(mapName,owner,port).startContainer();
			CTs.add(ct);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 停止並刪除Container
	 * @param id
	 */
	public void delContainer(int id) {
		try {
			lock.wait();
			Container ct = findCTbyId(id);
			CTs.remove(ct.stopContainer().deleteContainer());
			lock.notify();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 啟動Container
	 * @param id
	 */
	public void startContainerbyId(int id) {
		findCTbyId(id).startContainer();
	}
	
	/**
	 * 停止Container
	 * @param id
	 */
	public void stopContainerbyId(int id) {
		findCTbyId(id).stopContainer();
	}
	
	public Container findCTbyId(int id) {
		for(int i=0;i<CTs.size();i++) {
			if(CTs.get(i).info()[0].equals(""+id)) {
				return CTs.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Check process output
	 */
	@Override
	public void run() {
		while(!MinecraftAUTO.dockerMode) {
			try {
				synchronized (lock) {
					for(int i=0;i<CTs.size();i++)
						if(CTs.get(i).runFlag&&CTs.get(i).processStdOutput.available()>0)
							CTs.get(i).lock.notify();
					Thread.sleep(100);
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
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
