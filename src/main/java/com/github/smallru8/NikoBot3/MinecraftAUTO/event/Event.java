package com.github.smallru8.NikoBot3.MinecraftAUTO.event;

public class Event {
	public static class ContainerMessageEvent { 
		public String msg;
		public int id;
		public ContainerMessageEvent(int id,String msg){
			this.msg = msg;
			this.id = id;
		}
	}
}
