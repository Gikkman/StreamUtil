package com.gikk.streamutil.gui.init;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.irc.TwitchIRC;
import com.gikk.streamutil.misc.Callback;
import com.gikk.streamutil.misc.ExceptionDialogue;
import com.gikk.streamutil.misc.GikkPreferences;
import com.gikk.streamutil.task.OneTimeTask;
import com.gikk.streamutil.users.UserDatabaseCommunicator;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**Class for asynchronously checking that all the fields and resources specified during the initialization process
 * is correct and works.<br><br>
 * 
 * If all fields are okay, the onSuccessCallback will be called. If one field fails, the onFailCallback will be called.
 * Before calling any of these callbacks, the task will mark error the appropriate ImageView with one of the supplied
 * icons. If an error occurred, a error message will also be displayed.
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
	
	private final Callback onFailCallback;
	private final Callback onSuccessCallback;
	
	private TwitchIRC twitchIRC = null;
	
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
	 * @param onSuccessCallback Callback method which will be called if the check succeeds
	 * @param onFailCallback Callback method which will be called if the check fails
	 */
	CheckInitTask(String streamingAccName, File directory, String botAccName, String clientID, String oAuth, String apiToken, 
				  Callback onSuccessCallback, Callback onFailCallback){
		this.directory = directory;
		this.channel = "#" + streamingAccName.toLowerCase();
		this.clientID = clientID;
		this.nick = botAccName.toLowerCase();
		this.oAuth = oAuth;
		this.apiToken = apiToken;
		
		this.onSuccessCallback = onSuccessCallback;
		this.onFailCallback = onFailCallback;
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
		 if( passCheck() ){
			 onSuccessCallback.execute();
		 } else {
			 onFailCallback.execute();
		 }
	}

	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************
	
	/* 
	 * This section contains a lot of tests, which tries to catch as many possible errors as possible.
	 * 
	 * Therefore, this section is quite hard to get an overview of. The basic principle of all these 
	 * methods is that they check for several knowns errors, and if an error occures they "mark" an
	 * error and returns TRUE. 
	 * 
	 * Marking an error means that we display an error message box, and we set the testing icon for
	 * that element to ERROR. 
	 */
	
	private boolean passCheck(){
		/* 
		 * We wait a few moments between each step. 
		 * Partly so that resources are cleaned up before we initiate the next
		 * step, but also since it gives the user a feel of something "working"
		 * under the hood.
		*/ 
		try {
			Thread.sleep(500);	
			if( checkDirectory() ) return false;
			Thread.sleep(500); 
			if (checkIrc() ) return false;
			Thread.sleep(500); 
			if( checkDatabase() ) return false;
			Thread.sleep(500); 
			if (checkToken() ) return false;
			Thread.sleep(500); 
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean checkDirectory() {	
		//We check that the directory is valid
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
				/*
				 * If the directory is valid, we create a properties file in the specified directory
				 * and store the current values we've got.
				 * This might lead to a properties file that contains erroneous data, but
				 * that file will be over written by the next attempt to create a properties file
				 */
				createPropFile();
				Platform.runLater( () -> dirView.setImage(okIcon) );
				return false;
			} catch (Exception e) {
				markError(dirView, e);
			}		
		}
		System.out.println("****Directory check performed. All okay!");
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
		Preferences preferences = Preferences.userNodeForPackage( GikkPreferences.class );
		preferences.put("properties", file.getAbsolutePath() );
	}

	private boolean checkDatabase() {
		/* 
		 * We create a Speedment instance and performs some basic checks on it.
		 * 
		 * We check that the database is up and running, and that we can write and remove a
		 * temporary user called TEMPORARY_TEST_USER_FOR_TESTING.
		 */		
		try {		
			if( UserDatabaseCommunicator.checkConnection() ){
				Platform.runLater( () -> dbView.setImage(okIcon) );
				System.out.println("****Database check performed. All okay!");
				return false;
			} else {
				markError(dbView, "Database error", "An error occured when trying to connect to the database.\nAre you "
						+ "sure you followed the steps from Step 2? If not, go back and redo Step 2. Also, make sure the"
						+ "database is currently running");
				return true;
			}
		} catch (Exception e) {
			markError(dbView, e);
		}
		return true;
	}

	private boolean checkIrc() throws InterruptedException {
		twitchIRC = new TwitchIRC( GikkPreferences.GET().getPropertiesFile() );	
		
		/* 
		 * We add a listener for connect events.
		 * If we succeed connecting, we will receive a JOIN message
		 * with our bot's nick
		 */
		AtomicBoolean joinedRoom = new AtomicBoolean(false);
		twitchIRC.addIrcListener(new IrcListener() {			
			@Override
			public void onJoin(IrcUser user) {
				if( user.getNick().equals(nick) )
					joinedRoom.set(true);
			}
		});
		
		//Tells the IRC to try to connect
		try { 
			twitchIRC.connect(); 
		}
		catch (IOException e) {
			markError(ircView, e);
			return true;
		}
		
		//Wait until we either receive the JOIN message, or 10 seconds have passed
		long start = System.currentTimeMillis();
		long now = start;
		while( twitchIRC.isConnected() && !joinedRoom.get() && (now-start)<10*1000  ){
			Thread.sleep(100);
			now = System.currentTimeMillis();
		}
		
		twitchIRC.disconnect();
		
		if( joinedRoom.get() ){
			Platform.runLater( () -> ircView.setImage(okIcon) );
			System.out.println("****IRC check performed. All okay!");
			return false;
		}
		markError(ircView, "IRC connect error", "Could not connect to IRC.\n"
				+"Make sure that you have entered the oauth:token in Step 3 correct, that the bot name in Step 3 is correct"
				+ " and that the field Streaming account name in Step 1 is correctly spelled");
		return true;
	}
	
	private boolean checkToken() {
		if( apiToken != null && clientID != null ){
			Platform.runLater( () -> apiView.setImage(okIcon) );
			System.out.println("****API check performed. All okay!");
			return false;
		}
		markError(apiView, "API token missing", "Please run the API authorization in Step 4.\n"
				+ "If the authorization process fails, make sure that you have entered the correct ClientID in Step 3and that the" 
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
			view.setImage(errorIcon);
			a.showAndWait();
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
