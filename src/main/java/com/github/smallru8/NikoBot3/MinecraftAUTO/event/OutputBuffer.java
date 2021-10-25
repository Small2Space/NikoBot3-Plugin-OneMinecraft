package com.github.smallru8.NikoBot3.MinecraftAUTO.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class OutputBuffer {
	
	private class BUF{
		public Queue<String> q = new LinkedList<String>();
		
		public void push(String output) {
			q.add(output);
			while(q.size()>15)
				q.poll();
		}
		
		
	}
	
	private ArrayList<BUF> buffers;
}
