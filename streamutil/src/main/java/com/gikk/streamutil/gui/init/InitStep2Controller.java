package com.gikk.streamutil.gui.init;

import com.gikk.streamutil.misc.OpenBrowser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class InitStep2Controller {	
	@FXML protected void click_openLink(ActionEvent e){
		OpenBrowser.open("http://dev.mysql.com/downloads/");
	}
}
