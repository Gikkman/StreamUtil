package com.gikk.streamutil.gui.tabs;

import java.net.URL;
import java.util.ResourceBundle;

import com.gikk.streamutil.irc.GikkBot;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class BotDebugTabController extends _TabControllerBase {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	@FXML protected TextField txt_RawMessage;
	
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
}
