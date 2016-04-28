package com.gikk.streamutil.gui.init;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class InitWindowController implements Initializable {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private final String DIRECTORY = "init/";
	private ClassLoader cl;
	
	@FXML Accordion acn_box;
	@FXML TitledPane tp_1; @FXML TitledPane tp_2; @FXML TitledPane tp_3; @FXML TitledPane tp_4;
	@FXML Pane pane_1; @FXML Pane pane_2; @FXML Pane pane_3; @FXML Pane pane_4; 
	
	private Parent pane_last;
	
	private InitStep1Controller stp1;
	private InitStep3Controller stp3;
	
	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cl = this.getClass().getClassLoader();
		loadPane1();
		loadPane2();
		loadPane3();
		loadPane4();
		loadPaneLast();
		
		acn_box.setExpandedPane(tp_1);
		tp_1.requestFocus();
	}

	//***********************************************************
	// 				PRIVATE
	//***********************************************************
	private void onInitSuccess() {
		//Since this callback can be called from any thread, we need to schedule it on the Platform
		Platform.runLater(() -> {
			Stage stage = new Stage( );
			Scene scene = new Scene( pane_last );
			
			stage.setScene(scene);
			stage.setResizable(false);
			stage.setTitle("Initialization process - Complete");
			stage.initModality( Modality.APPLICATION_MODAL );
			stage.showAndWait();
			
			//Close this stage. Since we are closing it programmatically, we need to fire the CloseRequest manually
			Stage primaryStage = (Stage) acn_box.getScene().getWindow();
				  primaryStage.fireEvent( new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST) );
				  primaryStage.close();
		} );
	}	
	
	private void loadPane1() {
		try {					
			FXMLLoader loader = new FXMLLoader( cl.getResource( DIRECTORY + "Step 1.fxml" ) );
			Parent p = loader.load();		
			pane_1.getChildren().add(p);		
			stp1 = loader.getController();
		} catch (IOException e) {
			System.err.println("Could not load FXML document");
			e.printStackTrace();
		}		
	}	
	
	private void loadPane2() {
		try {					
			FXMLLoader loader = new FXMLLoader( cl.getResource( DIRECTORY + "Step 2.fxml" ) );
			Parent p = loader.load();		
			pane_2.getChildren().add(p);
		} catch (IOException e) {
			System.err.println("Could not load FXML document");
			e.printStackTrace();
		}
	}	
	
	private void loadPane3() {
		try {					
			FXMLLoader loader = new FXMLLoader( cl.getResource( DIRECTORY + "Step 3.fxml" ) );
			Parent p = loader.load();		
			pane_3.getChildren().add(p);
			
			stp3 = loader.getController();
		} catch (IOException e) {
			System.err.println("Could not load FXML document");
			e.printStackTrace();
		}
	}	
	
	private void loadPane4() {
		try {					
			FXMLLoader loader = new FXMLLoader( cl.getResource( DIRECTORY + "Step 4.fxml" ) );
			Parent p = loader.load();		
			pane_4.getChildren().add(p);
			
			InitStep4Controller stp4 = loader.getController();
			stp4.setOtherControllers(stp1, stp3);
			stp4.setSuccessCallback( () -> onInitSuccess() );
		} catch (IOException e) {
			System.err.println("Could not load FXML document");
			e.printStackTrace();
		}
	}
	
	private void loadPaneLast() {
		try {					
			FXMLLoader loader = new FXMLLoader( cl.getResource( DIRECTORY + "Step Last.fxml" ) );
			pane_last = loader.load();		
		} catch (IOException e) {
			System.err.println("Could not load FXML document");
			e.printStackTrace();
		}
	}
}
