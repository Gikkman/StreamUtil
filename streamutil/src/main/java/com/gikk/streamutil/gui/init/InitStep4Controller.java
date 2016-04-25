package com.gikk.streamutil.gui.init;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.gikk.streamutil.misc.ExceptionDialogue;
import com.gikk.streamutil.misc.OpenBrowser;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.twitch.api.auth.Scopes;
import com.mb3364.twitch.api.auth.grants.implicit.AuthenticationError;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class InitStep4Controller {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private InitStep1Controller stp1Controller;
	private InitStep3Controller stp3Controller;
	
	private Twitch twitch;
	private FetchTwitchTokenTask task;
	
	private String token = null;
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************
	public void setOtherControllers(InitStep1Controller stp1, InitStep3Controller stp3){
		this.stp1Controller = stp1;
		this.stp3Controller = stp3;
		
		twitch = new Twitch();
		
		//This task is for asynchronously fetching the API token. We want to make sure we do not end up in a dead-lock
		task = new FetchTwitchTokenTask(twitch);
		task.setSuccessCallback( () -> {
			token = task.getTwitchToken();
		} );	
		task.setFailCallback( () -> {
			AuthenticationError error = task.getError();
			String name = error.getName();
			String desc = error.getDescription();
			Platform.runLater( () -> {
				Alert a = ExceptionDialogue.create( name, desc );
				a.showAndWait(); 
			} );
		});
	}
	
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************
	@FXML protected void click_openAuthPrompt(ActionEvent e){
		
	}
	
	@FXML protected void click_complete(ActionEvent e){
		File dir = stp1Controller.getDirectory();
		
		String name = stp3Controller.getName();
		String clientID = stp3Controller.getClientID();
		String oauth = stp3Controller.getAuth();
		
		getToken(clientID);
	}
	
	// ****************************************** ACCESS TOKEN *****************************************	
	private void getToken(String clientID) {
		//If we've already got a token, and the user presses the "Fetch token" button again, we assume it was a mistake and just do nothing
		if( token != null )
			return;
		
		try {
			//Set up the URI for requesting Twitch API access
			URI callbackUri = new URI("http://127.0.0.1:23522");
			String authUrl = twitch.auth().getAuthenticationUrl(clientID, callbackUri, Scopes.CHANNEL_EDITOR, Scopes.CHANNEL_COMMERCIAL, Scopes.CHANNEL_SUBSCRIPTIONS, Scopes.CHANNEL_CHECK_SUBSCRIPTION );
			
			//Open a browser to ask the user to authenticate
			OpenBrowser.open(authUrl); 
			
			//Start the task that will look for the incoming API token.
			task.forceStopIfRunning();
			task.schedule(5 * 1000);
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
