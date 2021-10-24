package com.github.smallru8.NikoBot3.MinecraftAUTO;

import org.apache.commons.io.FileUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {

	public static void deleteFile(File file) {
		try {
			if(file.isDirectory())
				FileUtils.deleteDirectory(file);
			else if(file.isFile())
				file.delete();
			
			while(file.exists())
				Thread.sleep(100);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	* @param oldpath 要複製的資料夾
	* @param newpath 複製到新的路徑
	*/
	public static void copy(File oldpath,File newpath){
		if(oldpath.isDirectory()){//複製資料夾
			newpath.mkdir();
			File[] oldList = oldpath.listFiles();
			if(oldList!=null){
				for (File file : oldList) {
					copy(file, new File(newpath,file.getName()));
				}
			}
		}else if(oldpath.isFile()){//複製檔案
			File f=new File(newpath.getAbsolutePath());
			try {
				f.createNewFile();
				copyFile(oldpath,f.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	* @param filepath 要複製的檔案
	* @param path  複製到哪去
	*/
	public static void copyFile(File filepath,String path){
		try {
			InputStream is=new FileInputStream(filepath);
			OutputStream os=new FileOutputStream(path);
		
			BufferedInputStream bis=new BufferedInputStream(is);
			BufferedOutputStream bos=new BufferedOutputStream(os);
		
		
			byte[] bs=new byte[1024];
			int len=-1;
			while((len=bis.read(bs))!=-1){
				bos.write(bs, 0, len);
			}
			bos.close();
			bis.close();
		} catch (IOException e) {
			System.out.println(filepath+" "+path);
			e.printStackTrace();
		}
	}
	
	public static boolean isDigitOnly(String str) {
		return str.matches("[0-9]*");
	}
	
	public static String replacetoX(String str) {
		String[] strLs = str.split(":");
		return strLs[0]+":heavy_multiplication_x:"+strLs[2];
	}
	
	public static String replacetoDone(String str) {
		String[] strLs = str.split(":");
		return strLs[0]+":ballot_box_with_check:"+strLs[2];
	}
	
	public static String replacetoRunning(String str) {
		String[] strLs = str.split(":");
		return strLs[0]+":arrows_counterclockwise:"+strLs[2];
	}
}
