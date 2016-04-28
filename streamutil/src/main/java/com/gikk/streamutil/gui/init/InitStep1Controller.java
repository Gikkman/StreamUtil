package com.gikk.streamutil.gui.init;

import java.io.File;

import com.gikk.streamutil.misc.OpenBrowser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

public class InitStep1Controller {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	@FXML Text txt_directory;
	@FXML TextField txt_accName;
	private File directory = null;


	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	
	public File getDirectory(){		
		return directory;
	}
	
	public String getAccName(){
		return txt_accName.getText();	
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
	
	@FXML protected void click_openGithub(ActionEvent e){
		OpenBrowser.open("https://www.github.com/gikkman/streamutil");
	}
}
