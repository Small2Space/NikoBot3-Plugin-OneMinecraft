package com.github.smallru8.NikoBot3.MinecraftAUTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ContainerManager {

	//Containers
	private ArrayList<Container> CTs = new ArrayList<Container>();
	
	public ContainerManager() {
		//意外重啟，恢復先前Container
		recoveryContainer();
		
		//TODO
		
	}
	
	public void recoveryContainer() {
		if(!MinecraftAUTO.dockerMode) {
			File fd = new File(MinecraftAUTO.workDir);
			String[] fLs = fd.list();
			for(int i=0;i<fLs.length;i++) {
				int port = findPort();
				if(port!=-1) {
					try {
						CTs.add(new Container(Integer.parseInt(fLs[i]),port));
					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				}
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
