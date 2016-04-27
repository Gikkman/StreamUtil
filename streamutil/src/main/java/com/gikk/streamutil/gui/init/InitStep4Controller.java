package com.gikk.streamutil.gui.init;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import com.gikk.streamutil.misc.ExceptionDialogue;
import com.gikk.streamutil.misc.OpenBrowser;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.twitch.api.auth.Scopes;
import com.mb3364.twitch.api.auth.grants.implicit.AuthenticationError;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Icons: <a href=http://www.iconarchive.com/artist/milosz-wlazlo.html>Milosz Wlazl</a>
 * 
 * @author Simon
 *
 */
public class InitStep4Controller implements Initializable{
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	@FXML Button btn_Authorize;
	@FXML Button btn_Finish;
	
	@FXML Label lbl_Countdown; 
	
	@FXML ImageView img_Dir; 
	@FXML ImageView img_Db; 
	@FXML ImageView img_Irc; 
	@FXML ImageView img_Api; 	
	@FXML ImageView img_Status;
	
	private InitStep1Controller stp1Controller;
	private InitStep3Controller stp3Controller;
	
	private Twitch twitch;
	private FetchTwitchTokenTask task;
	
	private String token = null;
	private String clientID = null;
	
	private Image unknownIcon, okIcon, errorIcon;
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************
	public void setOtherControllers(InitStep1Controller stp1, InitStep3Controller stp3){
		this.stp1Controller = stp1;
		this.stp3Controller = stp3;
		
		twitch = new Twitch();
		
		//This task is for asynchronously fetching the API token
		task = new FetchTwitchTokenTask(twitch, 
										() -> onSuccess(),
										() -> onFail(),
										() -> onTick() );
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ClassLoader cl = getClass().getClassLoader();
		try {
			unknownIcon = new Image( cl.getResource("img/unknown-icon.png").openStream() );
			okIcon = new Image( cl.getResource("img/ok-icon.png").openStream() );
			errorIcon = new Image( cl.getResource("img/error-icon.png") .openStream() );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		img_Status.setImage(unknownIcon);
		img_Api.setImage(unknownIcon);
		img_Db.setImage(unknownIcon);
		img_Dir.setImage(unknownIcon);
		img_Irc.setImage(unknownIcon);
	}	
	
	//***********************************************************
	// 				FXML BUTTON METHODS
	//***********************************************************
	@FXML protected void click_finish(ActionEvent e){
		CheckInitTask initTask = new CheckInitTask(
				stp1Controller.getAccName(),
				stp1Controller.getDirectory(), 
				stp3Controller.getName(),
				clientID, 
				stp3Controller.getAuth(), 
				token);
		initTask.setDirectoryView(img_Dir);
		initTask.setDbView(img_Db);
		initTask.setIrcView(img_Irc);
		initTask.setApiView(img_Api);
		initTask.setErrorIcon(errorIcon);
		initTask.setOkIcon(okIcon);
		initTask.schedule(0);
	}
	
	@FXML protected void click_authorize(ActionEvent e){
		/* We store a copy of the clientID in case the user were to change the clientID field
		 * in step 3 before pressing the Finish button. By caching it, we make sure that the token
		 * we have is related to this clientID 
		 */
		clientID = stp3Controller.getClientID();	
		
		/* We disable the button whilst the authorization process runs, so
		 * the user cannot send more than one request at a time. Doing so would
		 * produce a socket error.
		 * We also set the count down label to 60, and start counting down (it might be something else
		 * than 60, since this isn't necessarily our first authorization attempt
		 */		
		btn_Authorize.setDisable(true);
		lbl_Countdown.setText("60");
		
		getToken(clientID);
	}
	
	//***********************************************************
	// 				CALLBACKS
	//***********************************************************
	private void onSuccess() {
		/* On success, we change to the success-icon and we fetch the token. We do not re-activate
		 * the button, since it will not be needed anymore 
		 */
		
		token = task.getTwitchToken();
		Platform.runLater( () -> {
			img_Status.setImage(okIcon);
		} );
	}

	private void onFail() {
		/* On fail, we show an error dialogue, telling the user what went wrong.
		 * 
		 * We also update the UI to display the error-icon, and reactivate the Authenticate button
		 */
		AuthenticationError error = task.getError();
		String name = error.getName();
		String description = error.getDescription();		
		Platform.runLater(() -> {
			Alert a = ExceptionDialogue.create(name, description);
			a.showAndWait();
			btn_Authorize.setDisable(false);
			img_Status.setImage(errorIcon);
		});
	}

	private void onTick() {
		int seconds = Integer.parseInt( lbl_Countdown.getText() ) - 1;
		/* Count down the time that is left until we time out. Also, if the image is
		 * not the "unknown" icon, change to it
		 */
		if( seconds >= 0 ){
			Platform.runLater( () -> {
				lbl_Countdown.setText( Integer.toString(seconds) );
				if( !img_Status.getImage().equals(unknownIcon) )
					img_Status.setImage(unknownIcon);
			});
		}			
	}
	
	//***********************************************************
	// 				ACCESS TOKEN
	//***********************************************************	
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
			
			//Start the task that will look for the incoming API token. Will poll once every 1 seconds
			task.schedule(1 * 1000, 1 * 1000);
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	
}
