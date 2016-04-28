package com.gikk.streamutil.gui.init;

import com.gikk.streamutil.misc.GikkPreferences;
import com.gikk.streamutil.misc.OpenBrowser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class InitLastController {	
	@FXML Button btn_shutdown;
	
	@FXML protected void click_ShutDown(ActionEvent e){
		btn_shutdown.setDisable( true );
		GikkPreferences.GET().firstLaunchDone();
		
		try { Thread.sleep(1000); } 
		catch (InterruptedException ignored) { } //For effects only

		//Close this stage and return the control the the stage "bellow" it
		Stage stage = (Stage) btn_shutdown.getScene().getWindow();
	    stage.close();
	}
	
	@FXML protected void click_speedment(ActionEvent e){
		OpenBrowser.open( "https://github.com/speedment/speedment" );
	}
	
	@FXML protected void click_twitchapi(ActionEvent e){
		OpenBrowser.open( "https://github.com/urgrue/Java-Twitch-Api-Wrapper" );
	}
	
	@FXML protected void click_twitchapiArctic(ActionEvent e){
		OpenBrowser.open( "https://github.com/ArcticLight/Java-Twitch-Api-Wrapper" );
	}
	
	@FXML protected void click_twitchapiGikk(ActionEvent e){
		OpenBrowser.open( "https://github.com/Gikkman/Java-Twitch-Api-Wrapper" );
	}
	
	@FXML protected void click_icons(ActionEvent e){
		OpenBrowser.open( "http://www.iconarchive.com/artist/milosz-wlazlo.html" );
	}
}
