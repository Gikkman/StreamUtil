package com.gikk.streamutil;

import java.net.URISyntaxException;
import com.gikk.streamutil.irc.GikkBot;
import com.gikk.streamutil.task.Scheduler;
import com.gikk.streamutil.users.UserManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** 
 *
 * @author Your name
 */
public class Main extends Application{
	
	@FXML TextField txt_field;
	@FXML TextField txt_field2;
	
	public void start(Stage primaryStage) throws URISyntaxException {
		
		// Get current classloader
		ClassLoader cl = this.getClass().getClassLoader();
		        
		try {
			Scene scene = new Scene(FXMLLoader.load( cl.getResource("MainWindow.fxml") ) );
			scene.getStylesheets().add( cl.getResource("application.css").toExternalForm() );
			
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest( (e) -> {
				//TODO: Consider this: Should we use a MethodMapper instead of this?
				GikkBot.GET().onProgramExit();
				Scheduler.GET().onProgramExit();
				UserManager.GET().onProgramExit();
			} );
			
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//TODO: If FirsTime, do InitChain
		
		//A very ugly way of making sure the Singletons work and are initiated
		try{ 
			GikkBot.GET(); 
			UserManager.GET();
			Scheduler.GET();
		}
		catch( Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}		
		
		launch(args);
	}
	
	@FXML protected void onClick(ActionEvent e){
		GikkBot.GET().channelMessage( txt_field.getText() );
	}
	
	@FXML protected void onClick2(ActionEvent e){
		GikkBot.GET().serverMessage( txt_field2.getText() );
	}
}