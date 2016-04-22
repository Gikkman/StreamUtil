package com.gikk.streamutil.gui.tabs;

import java.util.HashMap;
import java.util.Map;

import javafx.fxml.Initializable;

/**Base class for tabs that resides in the main windows tab pane.<br>
 * This class exists to that we can easily cast discovered tab controllers to a common class.
 * 
 * @author Simon
 *
 */
public abstract class _TabControllerBase implements Initializable{	
	private Map<String, Object> map = new HashMap<String, Object>();
	
	/**This method is used when sorting tabs in the main window. A lower weight means the tab is placed further to the left.
	 * In case of a tie, the order is not guaranteed
	 * 
	 * @return This tabs weight
	 */
	public abstract int getWeight();
	
	public void putData(String key, Object object){
		map.put(key, object);
	}
	
	protected Object getData(String key){
		return map.get(key);
	}
}
