package com.gikk.streamutil.users;

import java.util.LinkedList;
import java.util.List;

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
	
	public void joinUser(String ... users ){
		List<ObservableUser> oUsers = new LinkedList<>();
		for( int i = 0; i < users.length; i++ )
			oUsers.add( database.getOrCreate( users[i] ) );		
		
		Platform.runLater( () -> {
			//TODO: If I find time, I want to make this better
			for( ObservableUser oUser : oUsers )
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
