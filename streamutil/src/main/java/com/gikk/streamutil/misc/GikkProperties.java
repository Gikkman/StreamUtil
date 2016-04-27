package com.gikk.streamutil.misc;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**<b>Singleton</b><br><br>
 * 
 * This singleton is intended to simplify access to locally stored preferences
 * 
 * @author Simon
 *
 */
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
	public static GikkProperties GET(){
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
	
	public final synchronized File getPropertiesFile(){
		String props = preferences.get("properties", "");
		
		if( props.isEmpty() ){
			ExceptionDialogue.createAndShow("Missing properties file", "The properties file is missing. You must recreate it manually");
			return null;
		}
		
		return new File(props);
	}
	
	public final synchronized void clearProperties(){
		try {
			preferences.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
}
