package com.gikk.streamutil.misc;

import java.util.prefs.Preferences;

public class GikkProperties {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private static class HOLDER{ static final GikkProperties INSTANCE = new GikkProperties(); }
	
	private static final String FIRST_LAUNCH = "first_launch";
	
	private final Preferences preferences;
	//***********************************************************
	// 				STATIC
	//***********************************************************
	public GikkProperties GET(){
		return HOLDER.INSTANCE;
	}
	
	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	private GikkProperties(){
		preferences = Preferences.userNodeForPackage( GikkProperties.class );
	}
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	public final synchronized boolean isFirstLaunch(){
		return preferences.getBoolean(FIRST_LAUNCH, true);
	}
	
	public final synchronized void firstLaunchDone(){
		preferences.putBoolean(FIRST_LAUNCH, false);
	}
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
}
