package com.gikk.streamutil.misc;

import java.util.prefs.Preferences;

import javafx.stage.Stage;

/**From experience, it is handy to have a utility class for handling memorizing of window position between sessions.
 * 
 * @author Simon
 */
public class WindowSettings {	
	public static void saveWindowPos(Stage stage, String windowName){
		Preferences prefs = Preferences.userNodeForPackage( WindowSettings.class );		
		prefs.putDouble(windowName + "xPos", 	stage.getX() );
    	prefs.putDouble(windowName + "yPos", 	stage.getY() );
    	prefs.putDouble(windowName + "width", 	stage.getWidth() );
    	prefs.putDouble(windowName + "height", 	stage.getHeight() );
	}
	
	public static void loadWindowPos(Stage stage, String windowName){
		Preferences prefs = Preferences.userNodeForPackage( WindowSettings.class );	
		stage.setX( 	prefs.getDouble(windowName + "xPos",	stage.getX() ));
		stage.setY( 	prefs.getDouble(windowName + "yPos", 	stage.getY() ));
		stage.setWidth( prefs.getDouble(windowName + "width", 	stage.getWidth() ));
		stage.setHeight(prefs.getDouble( windowName + "height",	stage.getHeight() ));
	}
}
