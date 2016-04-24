package com.gikk.streamutil.gui.init;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

public class InitStep1Controller {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	@FXML Text txt_directory;
	private File directory = null;
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	public File getDirectory(){
		if( directory != null && directory.exists() && directory.isDirectory() )
			return directory;

		return null;
	}
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
	@FXML protected void click_choseLocation(ActionEvent e){
		 DirectoryChooser dc = new DirectoryChooser();
		 dc.setTitle("Choose local data directory");
		 dc.setInitialDirectory( new File("../") );
		 directory = dc.showDialog( ((Node) e.getTarget()).getScene().getWindow() );	 
		 
		 if( directory != null )
		 	txt_directory.setText( directory.getAbsolutePath() );
	}
}
