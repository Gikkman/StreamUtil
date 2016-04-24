package com.gikk.streamutil.gui.init;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;

public class InitWindowController implements Initializable {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	@FXML Accordion acn_box;
	@FXML TitledPane tp_1; @FXML TitledPane tp_2; @FXML TitledPane tp_3; @FXML TitledPane tp_4; @FXML TitledPane tp_5;
	
	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************
	@Override
	public void initialize(URL location, ResourceBundle resources) {		
		acn_box.setExpandedPane(tp_1);
		tp_1.requestFocus();
	}	
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************	

	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
}
