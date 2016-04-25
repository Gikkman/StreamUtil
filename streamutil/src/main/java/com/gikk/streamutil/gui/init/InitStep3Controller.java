package com.gikk.streamutil.gui.init;

import com.gikk.streamutil.misc.OpenBrowser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class InitStep3Controller {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	@FXML TextField txt_name;
	@FXML TextField txt_clientID;
	@FXML TextField txt_oauth;

	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	public String getName(){
		return txt_name.getText().trim();
	}
	
	public String getClientID(){
		return txt_clientID.getText().trim();
	}
	
	public String getAuth(){
		return txt_oauth.getText().trim();
	}	
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
	@FXML protected void click_openSignup(ActionEvent e){
		OpenBrowser.open("https://www.twitch.tv/signup");
	}
	
	@FXML protected void click_openRegisterApp(ActionEvent e){
		OpenBrowser.open("https://www.twitch.tv/kraken/oauth2/clients/new");
	}
	
	@FXML protected void click_openChatReg(ActionEvent e){
		OpenBrowser.open("https://twitchapps.com/tmi/");
	}
}
