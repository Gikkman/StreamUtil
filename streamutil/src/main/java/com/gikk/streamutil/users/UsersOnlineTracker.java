package com.gikk.streamutil.users;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UsersOnlineTracker {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private final ObservableList<ObservableUser> usersOnline =  FXCollections.observableArrayList();	
	private final UserDatabaseCommunicator database;
	
	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************
	public UsersOnlineTracker(UserDatabaseCommunicator databaseCommunicator){
		this.database = databaseCommunicator;
	}	

	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	public final ObservableList<ObservableUser> getUserOnlineList(){
		return usersOnline;
	}
	
	public void partUser(String user) {
		ObservableUser u = database.getOrCreate(user );
		Platform.runLater( () -> {
			usersOnline.remove(u);
		});
	}
	
	public void joinUser(String user ){
		ObservableUser oUser = database.getOrCreate( user );		
		
		Platform.runLater( () -> {
			//Twitch sometimes drops PART messages, and we don't want to rejoin an already present user
			if( !usersOnline.contains(oUser) )
				usersOnline.add(oUser);
		});
	}
	
	public void incrementUsersOnlinetime(int minutes) {
		Platform.runLater(() -> {
			usersOnline.stream().forEach( p -> p.addTimeOnline(minutes) );				
		} );				
	}
	
	public void clear(){
		Platform.runLater( () -> {
			usersOnline.clear();
		} );
	}
}
