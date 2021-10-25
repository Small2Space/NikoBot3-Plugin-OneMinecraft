package com.github.smallru8.NikoBot3.MinecraftAUTO.event;

import org.greenrobot.eventbus.EventBus;

public class EventSender {

	public static void containerOutput(int id,String msg) {
		if(EventBus.getDefault().hasSubscriberForEvent(Event.ContainerMessageEvent.class))
			EventBus.getDefault().post(new Event.ContainerMessageEvent(id,msg));
	}
	
}
