package com.gikk.streamutil.misc;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**<b>Singleton</b><br><br>
 * 
 * This singleton is intended to simplify access to locally stored preferences and properties
 * 
 * @author Simon
 *
 */
public class GikkPreferences {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private static class HOLDER{ static final GikkPreferences INSTANCE = new GikkPreferences(); }
	private static final String FIRST_LAUNCH = "first_launch";
	
	private final Preferences preferences;
	
	private File properties = null;
	//***********************************************************
	// 				STATIC
	//***********************************************************
	public static GikkPreferences GET(){
		return HOLDER.INSTANCE;
	}
	
	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	private GikkPreferences(){
		preferences = Preferences.userNodeForPackage( GikkPreferences.class );
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
	
	/**Fetches the properties File.<br>
	 * Actually, this method fetches the file's location from preferences, and
	 * then creates a file from that path.
	 * 
	 * @return File, representing the properties file or {@code null} if the file could not be found
	 */
	public final synchronized File getPropertiesFile(){
		if( this.properties != null )
			return this.properties;
		
		String propertiesLocation = preferences.get("properties", "");
		
		if( propertiesLocation.isEmpty() ){
			System.err.println("Missing properties file: No file path regiestered");
			return null;
		}
		
		File properties = new File(propertiesLocation);
		if( properties.exists() && properties.isFile() && properties.canRead() ){
			this.properties = properties;	
			return properties;
		}
		System.err.println("Missing properties file: The properties file is missing.");
		return null;
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
