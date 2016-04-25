package com.gikk.streamutil.misc;

import java.awt.Desktop;
import java.net.URI;

public class OpenBrowser {
	private static Desktop desktop;
	//***********************************************************
	// 				STATIC
	//***********************************************************
	public static void open(String url){
		desktop = getDesktop();
		if (desktop != null && desktop.isSupported( Desktop.Action.BROWSE)){
			try{
				desktop.browse( URI.create(url) );
			} catch (Exception ex){
				ex.printStackTrace();
			}			
		} else {
			System.err.println("Desktop is not supported");
		}
	}
	
	private static Desktop getDesktop(){
		if(desktop == null)
			desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		return desktop;
	}
}
