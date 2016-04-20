package com.gikk.streamutil;

import java.net.URISyntaxException;
import com.gikk.gikk_stream_util.GikkStreamUtilApplication;
import com.gikk.gikk_stream_util.db0.gikk_stream_util.user.User;
import com.gikk.streamutil.irc.GikkBot;
import com.gikk.streamutil.task.Scheduler;
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
		bot = new GikkBot();
		
		// Get current classloader
		ClassLoader cl = this.getClass().getClassLoader();
		        
		try {
			Scene scene = new Scene(FXMLLoader.load( cl.getResource("MainWindow.fxml") ) );
			scene.getStylesheets().add( cl.getResource("application.css").toExternalForm() );
			
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest( (e) -> {
				bot.closeConnection();
				Scheduler.GET().onProgramExit();
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
	
	//TODO: Find a way to incorporate this....
	private void addUser(String name){
		Speedment speedment = new GikkStreamUtilApplication().build();
        Manager<User> users = speedment.managerOf(User.class);
        
        final String lcName = name.toLowerCase();
        
        if( users.stream().parallel().filter( p -> p.getUsername().matches(lcName) ).count() > 0 ){
        	System.out.println("User already present");
        	return;
        }
        
		Thread t = new Thread( () -> {
	        try {
	            User user = users.newEmptyEntity()
	                .setUsername(lcName)
	                .setIsFollower(false)
	                .setIsSubscriber(false)
	                .setIsTrusted(false)
	                .setLinesWritten(0)
	                .setTimeOnline(0)
	                .persist();
	                
	            System.out.print("Hello nr. " + user.getId() +", "
	            		+ "Name: " 		  	+ user.getUsername() + ", "
	            		+ "Time online: " 	+ user.getTimeOnline() +", "
	            		+ "Lines written: " + user.getLinesWritten() +", "
	            		+ "Is trusted: " 	+ user.getIsTrusted() +", "
	            		+ "Is follower: " 	+ user.getIsFollower() +", "
	            		+ "Is subscriber: " + user.getIsSubscriber() );
	        } catch (SpeedmentException se) {
	           System.out.println("Why are you so persistant?");
	           System.err.println( se.getLocalizedMessage() );
	        } } );
        t.start();
	}
}