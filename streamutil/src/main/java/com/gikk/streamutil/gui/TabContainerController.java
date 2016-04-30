package com.gikk.streamutil.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.gikk.streamutil.gui.tabs._TabControllerBase;
import com.gikk.streamutil.misc.KeyPriorityQueue_Min;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**This class instantiates all the different tabs that are visible to the user.<br><br>
 * 
 * Currently, it only loads one tab, but the design is intended to be usable for loading 
 * custom made plug-in tabs in the future
 * 
 * @author Simon
 *
 */
public class TabContainerController implements Initializable{
	private static final String DIRECTORY = "tabs/";
	@FXML TabPane tab_pane;
	
	@Override
	public void initialize(URL location, ResourceBundle resources){
		//First, we fetch all files from the tabs folder and store them in the fxmlFiles array list
		ClassLoader cl = this.getClass().getClassLoader();					
		String[] fxmlFiles = {"StreamSettingsTab.fxml", "BotDebugTab.fxml"};		
		
		//Then, we load all the tabs and add them to a priority queue, sorted by tab weight
		KeyPriorityQueue_Min<Tab> tabOrder = new KeyPriorityQueue_Min<>();
		for( String tab : fxmlFiles ){
			try {					
				FXMLLoader loader = new FXMLLoader( cl.getResource( DIRECTORY + tab ) );
				Parent p = loader.load();
				
				Tab t = new Tab( tab.substring(0, tab.indexOf(".") ));
				t.setContent(p);
				
				_TabControllerBase controller = loader.getController();
				if( controller != null)
					tabOrder.add( controller.getWeight() , t );		
				else
					tabOrder.add( Integer.MAX_VALUE, t );
				
			} catch (IOException e) {
				System.err.println("Could not load FXML document");
				e.printStackTrace();
			}
		}
		
		//Then, we add the tabs to the tab pane
		while( !tabOrder.isEmpty() )
			tab_pane.getTabs().add( tabOrder.poll() );	

	}	
}
