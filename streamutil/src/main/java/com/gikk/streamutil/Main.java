package com.gikk.streamutil;



import com.gikk.speedment.test.gikk_stream_util.GikkStreamUtilApplication;
import com.gikk.speedment.test.gikk_stream_util.db0.gikk_stream_util.users.Users;
import com.gikk.streamutil.irc.GikkBot;
import com.speedment.Manager;
import com.speedment.Speedment;
import com.speedment.exception.SpeedmentException;

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
	
	public void start(Stage primaryStage) {
		bot = new GikkBot();
		        
		try {
			Scene scene = new Scene (FXMLLoader.load( getClass().getResource("Main.fxml") ) );
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			
			primaryStage.setOnCloseRequest( (e) -> bot.closeConnection() );
			
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
	
	private void addUser(String name){
		Speedment speedment = new GikkStreamUtilApplication().build();
        Manager<Users> users = speedment.managerOf(Users.class);
        
        final String lcName = name.toLowerCase();
        
        if( users.stream().parallel().filter( p -> p.getUsername().matches(lcName) ).count() > 0 ){
        	System.out.println("User already present");
        	return;
        }
        
		Thread t = new Thread( () -> {
	        try {
	            Users user = users.newInstance()
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