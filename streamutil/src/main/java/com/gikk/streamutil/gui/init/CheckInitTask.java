package com.gikk.streamutil.gui.init;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.irc.TwitchIRC;
import com.gikk.streamutil.misc.ExceptionDialogue;
import com.gikk.streamutil.misc.GikkProperties;
import com.gikk.streamutil.task.OneTimeTask;
import com.gikk.streamutil.users.UserDatabaseCommunicator;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**Class for asynchronously checking that all the fields and resources specified during the initialization process
 * is correct and works.
 * 
 * @author Simon
 *
 */
class CheckInitTask extends OneTimeTask {
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final File directory;
	private final String channel, nick, oAuth, apiToken, clientID;
	private ImageView dirView, dbView, ircView, apiView;
	private Image okIcon, errorIcon;
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	/**Creates a task for checking that all the fields in the init-process is that the underlying 
	 * resources (database and IRC) works as intended.
	 * 
	 * @param streamingAccName The name of the account you use to stream
	 * @param directory The directory you want to store local files in
	 * @param botAccName The name of your bot's Twitch account
	 * @param clientID The bot's clientID
	 * @param oAuth The Twitch IRC oAuth token
	 * @param apiToken The Twitch API token
	 */
	CheckInitTask(String streamingAccName, File directory, String botAccName, String clientID, String oAuth, String apiToken){
		this.directory = directory;
		this.channel = "#" + streamingAccName.toLowerCase();
		this.clientID = clientID;
		this.nick = botAccName.toLowerCase();
		this.oAuth = oAuth;
		this.apiToken = apiToken;
	}
	
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	public void setDirectoryView(ImageView view) {
		this.dirView = view;
	}

	public void setDbView(ImageView view) {
		this.dbView = view;
	}

	public void setIrcView(ImageView view) {
		this.ircView = view;
	}
	
	public void setApiView(ImageView view){
		this.apiView = view;
	}

	public void setOkIcon(Image okIcon) {
		this.okIcon = okIcon;
	}

	public void setErrorIcon(Image errorIcon) {
		this.errorIcon = errorIcon;
	}
	
	@Override
	public void onExecute() {
		try {
			Thread.sleep(500);	//Add a short wait to give the user some suspense
			if( checkDirectory() ) return;
			Thread.sleep(500); //Add a short wait to give the user some suspense
			if( checkDatabase() ) return;
			Thread.sleep(500); //Add a short wait to give the user some suspense
			if (checkIrc() ) return;
			Thread.sleep(500); //Add a short wait to give the user some suspense
			if (checkToken() ) return;
			Thread.sleep(500); //Add a short wait to give the user some suspense
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************
	private boolean checkDirectory() {	
		//We check that the directory is vaild
		if( directory == null )
			markError(dirView, "Directory not chosen", "Please chose a directory for storing local data in Step 1");
		else if( !directory.exists() )
			markError(dirView, "Directory not found", "The chosen directory in Step 1 cannot be found. Make sure the chosen directory still exists");			
		else if( !directory.isDirectory() )
			markError(dirView, "Directory error", "The chosen directory in Step 1 is not recognized as a directory. Please chose another directory");
		else if( !( directory.canWrite() && directory.canRead() ) )
			markError(dirView, "Directory read/write error", "The application is not granted read/write access to the directory. Make sure that the directory has proper access priviliges");
		else {			
			try {
				//If the directory is valid, we create a properties file in the specified directory
				createPropFile();
				Platform.runLater( () -> dirView.setImage(okIcon) );
				return false;
			} catch (Exception e) {
				markError(dirView, e);
			}		
		}
		return true;
	}

	private void createPropFile() throws Exception {
		ClassLoader cl = getClass().getClassLoader();
		
		//Load the default properties file
		Properties prop = new Properties();
		prop.load( cl.getResourceAsStream("files/default.gikk") );
		
		//Set relevant properties fields
		prop.setProperty("nick", nick);
		prop.setProperty("password", oAuth);
		prop.setProperty("channel", channel);
		prop.setProperty("token", apiToken);
		prop.setProperty("clientid", clientID);
		
		//Create a new properties file and write to disk
		File file = new File( directory.getPath() + "/properties.gikk" );
		file.createNewFile();		
		FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
		prop.store(fos, null);
		fos.close();
		
		//Store location of the properties file
		Preferences preferences = Preferences.userNodeForPackage( GikkProperties.class );
		preferences.put("properties", file.getAbsolutePath() );
	}

	private boolean checkDatabase() {
		//We create a Speedment instance and performs some basic checks on it
		try {
			boolean connection = UserDatabaseCommunicator.checkConnection();
			if( connection ){
				Platform.runLater( () -> dbView.setImage(okIcon) );
				return false;
			} else {
				markError(dbView, "Database error", "An error occured when trying to connect to the database. Are you "
						+ "sure you followed the steps from Step 2? If not, go back and redo Step 2");
				return true;
			}
		} catch (Exception e) {
			markError(dbView, e);
		}
		return true;
	}

	private boolean checkIrc() throws InterruptedException {
		TwitchIRC irc = new TwitchIRC( GikkProperties.GET().getPropertiesFile() );	
		
		//We add a listener for connect events.
		//If we fail to connect, we will receive a "Error logging in" notice.
		//If we succeed connecting, we will receiv a JOIN message
		//
		//The atomic integer solution is a bit hacky, but it gets the job done
		AtomicInteger connectStatus = new AtomicInteger(0);
		irc.addIrcListener(new IrcListener() {
			@Override
			public void onNotice(IrcMessage message) {
				if( message.getContent().equalsIgnoreCase("Error logging in") );
					connectStatus.set(-1);
			}			
			@Override
			public void onJoin(IrcUser user) {
				if( user.getNick().equalsIgnoreCase(nick) )
					connectStatus.set(1);
			}
		});
		irc.connect();
		
		//What for the connectStatus to change. 
		// 0 = no status, 1 = connected, -1 = not connected
		while( connectStatus.get() == 0 ){
			Thread.sleep(100);
		}
		if( connectStatus.get() == 1 ){
			Platform.runLater( () -> ircView.setImage(okIcon) );
			return false;
		}
		markError(ircView, "IRC connect error", "Could not connect to IRC. "
				+"Make sure that you have entered the oauth:token correct and that the field Streaming account name is correctly spelled");
		return true;
	}
	
	private boolean checkToken() {
		if( apiToken != null && clientID != null ){
			Platform.runLater( () -> apiView.setImage(okIcon) );
			return false;
		}
		markError(apiView, "API token missing", "Please run the API authorization in Step 4. "
				+ "If the authorization process fails, make sure that you have entered the correct ClientID and that the" 
				+ "redirect URI on the Twitch-page where you created the Developer Application is EXACTLY http://127.0.0.1:23522");
		return true;		
	}
	
	/**Convenience methods for changing the icon to an error icon and showing an error window
	 * 
	 * @param view The ImageView that should get the error token
	 * @param header The error message's header
	 * @param content The error message's content
	 */
	private void markError(ImageView view, String header, String content){
		Platform.runLater( () -> {
			Alert a = ExceptionDialogue.create(header, content);
			a.showAndWait();
			view.setImage(errorIcon);
		});
		
	}
	
	/**Convenience methods for changing the icon to an error icon and showing an error window
	 * 
	 * @param view The ImageView that should get the error token
	 * @param e The exception that should be showed in the error message window
	 */
	private void markError(ImageView view, Exception e) {
		Platform.runLater( () -> {
			Alert a = ExceptionDialogue.create(e);
			a.showAndWait();
			view.setImage(errorIcon);				
		});
	}
}
