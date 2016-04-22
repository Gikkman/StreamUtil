package com.gikk.streamutil;

import java.net.URISyntaxException;
import com.gikk.gikk_stream_util.GikkStreamUtilApplication;
import com.gikk.gikk_stream_util.db0.gikk_stream_util.user.User;
import com.gikk.streamutil.irc.GikkBot;
import com.gikk.streamutil.task.Scheduler;
import com.gikk.streamutil.users.UserManager;
import com.speedment.Speedment;
import com.speedment.exception.SpeedmentException;
import com.speedment.manager.Manager;

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
	
	private static GikkBot bot;
	
	public void start(Stage primaryStage) throws URISyntaxException {
		bot = GikkBot.GET();
		
		// Get current classloader
		ClassLoader cl = this.getClass().getClassLoader();
		        
		try {
			Scene scene = new Scene(FXMLLoader.load( cl.getResource("MainWindow.fxml") ) );
			scene.getStylesheets().add( cl.getResource("application.css").toExternalForm() );
			
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest( (e) -> {
				bot.closeConnection();
				Scheduler.GET().onProgramExit();
				UserManager.GET().onProgramExit();
			} );
			
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
        
        System.out.println();
        
        
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@FXML protected void onClick(ActionEvent e){
		bot.channelMessage( txt_field.getText() );
	}
	
	@FXML protected void onClick2(ActionEvent e){
		bot.serverMessage( txt_field2.getText() );
	}
}