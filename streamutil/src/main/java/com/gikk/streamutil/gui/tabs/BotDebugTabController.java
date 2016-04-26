package com.gikk.streamutil.gui.tabs;

import java.net.URL;
import java.util.ResourceBundle;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.twitchApi.TwitchApi;
import com.mb3364.twitch.api.handlers.UserFollowResponseHandler;
import com.mb3364.twitch.api.models.UserFollow;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class BotDebugTabController extends _TabControllerBase {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	@FXML protected TextField txt_RawMessage;
	@FXML protected TextField txt_UserName;
	
	private GikkBot bot;
	
	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		bot = GikkBot.GET();
	}
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	
	@Override
	public int getWeight() {
		return 1;
	}

	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
	
	@FXML protected void sendRawMessage(ActionEvent e){
		bot.serverMessage( txt_RawMessage.getText() );
	}
	
	@FXML protected void testFollower(ActionEvent e){
		TwitchApi.GET().checkIfFollower(txt_UserName.getText(), "#gikkman", new UserFollowResponseHandler() {
			
			@Override
			public void onFailure(int arg0, String arg1, String arg2) {
				System.out.println("Fail: " + arg0 + " " + arg1 + " " + arg2);		
				
			}
			
			@Override
			public void onFailure(Throwable arg0) {
				System.out.println("Fail: Throw");		
				
			}
			
			@Override
			public void onSuccess(UserFollow arg0) {
				System.out.println("Success: Is follower");				
			}
		});
	}
}
