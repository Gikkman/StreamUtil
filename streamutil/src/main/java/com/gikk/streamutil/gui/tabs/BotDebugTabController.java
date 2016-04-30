package com.gikk.streamutil.gui.tabs;

import java.net.URL;
import java.util.ResourceBundle;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.misc.GikkPreferences;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;
import com.gikk.streamutil.users.UserStatus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

public class BotDebugTabController extends _TabControllerBase {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	@FXML protected TextField txt_RawMessage;
	@FXML protected TextField txt_UserName;
	@FXML protected RadioButton rad_Sub; @FXML protected RadioButton rad_Fol; @FXML protected RadioButton rad_Trust;
	@FXML protected Spinner<Integer> spn_time; @FXML protected Spinner<Integer> spn_lines;
	@FXML protected ComboBox<UserStatus> cbb_state;
	
	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cbb_state.getItems().addAll( UserStatus.values() );
		cbb_state.setValue( cbb_state.getItems().get(2) );
		
		spn_lines.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000) );
		spn_time.setValueFactory(  new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000) );
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
	@FXML protected void clearProperties(ActionEvent e){
		GikkPreferences.GET().clearProperties();
	}
	
	@FXML protected void addUser(ActionEvent e){
		if( txt_UserName.getText().isEmpty() ){
			System.err.println("A name is required");
			return;
		}
		
		UserDatabaseCommunicator db = GikkBot.GET().getDB();
		ObservableUser user = db.getOrCreate( txt_UserName.getText() );
		
		if( user == null ){
			System.err.println("The name was not unique");
			return;
		}
		
		user.setFollower( rad_Fol.isSelected() );
		user.setTrusted( rad_Trust.isSelected() );
		user.setSubscriber( rad_Sub.isSelected() );
		user.setLinesWritten( spn_lines.getValue());
		user.setTimeOnline( spn_time.getValue() );
		user.setStatus( cbb_state.getValue() );
	}
}
