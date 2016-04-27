package com.gikk.streamutil.misc;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**Class for creating a window that will show an exception to a user
 * 
 * Code from <a href=http://code.makery.ch/blog/javafx-dialogs-official/>http://code.makery.ch/blog/javafx-dialogs-official/</a>
 *
 */
public class ExceptionDialogue {
	//***********************************************************
	// 				STATIC
	//***********************************************************
	public static void createAndShow(String header, String message) {
		Platform.runLater( () -> {
			Alert alert = create(header, message);
			alert.showAndWait();
		} );
	}
	
	public static void createAndShow(Exception e) {
		Platform.runLater( () -> {
			Alert alert = create(e);
			alert.showAndWait();
		} );
	}
	
	public static Alert create(String header, String content){
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(header);
		alert.setContentText(content);

		return alert;
	}
	
	public static Alert create(Exception ex){
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception Dialog");
		alert.setHeaderText(ex.getMessage());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		return alert;
	}
}
