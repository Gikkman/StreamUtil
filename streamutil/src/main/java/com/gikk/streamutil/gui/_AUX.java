package com.gikk.streamutil.gui;

import java.util.prefs.Preferences;

import com.gikk.streamutil.Main;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**From experience, it is handy to have a utility class for handling memorizing of window position between sessions.
 * 
 * @author Simon
 */
public class _AUX {
	public static void applyCss(Scene scene){
		scene.getStylesheets().add( Main.class.getResource("application.css").toExternalForm());
	}
	
	public static void saveWindowPos(Stage stage, String windowName){
		Preferences prefs = Preferences.userNodeForPackage( _AUX.class );		
		prefs.putDouble(windowName + "xPos", 	stage.getX() );
    	prefs.putDouble(windowName + "yPos", 	stage.getY() );
    	prefs.putDouble(windowName + "width", 	stage.getWidth() );
    	prefs.putDouble(windowName + "height", 	stage.getHeight() );
	}
	
	public static void loadWindowPos(Stage stage, String windowName){
		Preferences prefs = Preferences.userNodeForPackage( _AUX.class );	
		stage.setX( 	prefs.getDouble(windowName + "xPos",	stage.getX() ));
		stage.setY( 	prefs.getDouble(windowName + "yPos", 	stage.getY() ));
		stage.setWidth( prefs.getDouble(windowName + "width", 	stage.getWidth() ));
		stage.setHeight(prefs.getDouble( windowName + "height",	stage.getHeight() ));
	}
}
