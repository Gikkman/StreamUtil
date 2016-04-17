package com.gikk.streamutil.gui.tabs;

import javafx.fxml.Initializable;

/**Base class for tabs that resides in the main windows tab pane.<br>
 * This class exists to that we can easily cast discovered tab controllers to a common class.
 * 
 * @author Simon
 *
 */
public abstract class _TabControllerBase implements Initializable{	
	
	/**This method is used when sorting tabs in the main window. A lower weight means the tab is placed further to the left.
	 * In case of a tie, the order is not guaranteed
	 * 
	 * @return This tabs weight
	 */
	public abstract int getWeight();
}
