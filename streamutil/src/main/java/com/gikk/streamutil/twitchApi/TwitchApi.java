package com.gikk.streamutil.twitchApi;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.gikk.streamutil.misc.StackTrace;
import com.mb3364.http.RequestParams;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.twitch.api.auth.Scopes;
import com.mb3364.twitch.api.handlers.ChannelFollowsResponseHandler;
import com.mb3364.twitch.api.handlers.ChannelResponseHandler;
import com.mb3364.twitch.api.handlers.ChannelSubscriptionsResponseHandler;
import com.mb3364.twitch.api.handlers.StreamResponseHandler;
import com.mb3364.twitch.api.models.Channel;

/**<b>Singleton</b><br><br>
 * 
 * This class handles all communication with Twitch. That includes retrieving information and assigning information. <br>
 * Most of the communication to Twitch is done asynchronously and handles response via different ResponeaHandlers.<br><br>
 * 
 * This class uses a <a href="https://github.com/ArcticLight/Java-Twitch-Api-Wrapper">https://github.com/ArcticLight/Java-Twitch-Api-Wrapper</a>
 * which is a fork of <a href="https://github.com/urgue/Java-Twitch-Api-Wrapper">https://github.com/urgue/Java-Twitch-Api-Wrapper</a>
 * 
 * @author Simon
 *
 */
public class TwitchApi {
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private static class Holder {static final TwitchApi INSTANCE = new TwitchApi();  }
	
	private final Twitch twitch;

	private final String token;
	private final String channel;
	private final String clientID;	
	
	//***********************************************************************************************
	//											STATIC
	//***********************************************************************************************
	public static TwitchApi GET(){
		return Holder.INSTANCE;
	}
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************	
	private TwitchApi(){
		twitch = new Twitch();
		
		//TODO: Load info from the .ini file
		File file = Paths.get( System.getProperty("user.home"), "gikk.ini" ).toFile();
	   	PropertiesConfiguration prop = new PropertiesConfiguration();
    	prop.setDelimiterParsingDisabled(true);
    	try {
			prop.load(file);
		} catch (ConfigurationException e1) {
			System.err.println("Could not lode the properties file! Make sure you have a valid 'gikk.ini' in your User/ folder");
		}
    	clientID = prop.getString("ClientID");
    	channel = prop.getString("Channel").substring(1); //Since the channel has a # in the front, we need to remove that one.
		
		token = getToken();	
		if( token.matches("" ) ){
			System.err.println("Authentication token error. No token obtained. " + StackTrace.getStackPos());
			System.exit(-1);
		}
		
		twitch.setClientId( clientID );
		twitch.auth().setAccessToken(token);
	}

	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************		
	public void setGame(String game){
		makeUpdate("game", game);
	}
	
	public void setTitle(String title){
		makeUpdate("status", title);
	}
	
	public void setStatus(String title, String game) {
		RequestParams params = new RequestParams();
		params.put("status", title);
		params.put("game", game);
		
		makeUpdate(params);
	}
	
	public void setBroadcastDelay(int seconds) {
		makeUpdate("delay", String.valueOf(seconds) );
	}
	
	public void getStreamInfo(ChannelResponseHandler handler){
		twitch.channels().get(channel, handler);
	}
	
	public void getViewers(StreamResponseHandler handler){
		twitch.streams().get(channel, handler);
	}
	
	public void getFollowers(ChannelFollowsResponseHandler handler){
		twitch.channels().getFollows(channel, handler);
	}
	
	public void getSubscribers(ChannelSubscriptionsResponseHandler handler){
		twitch.channels().getSubscriptions(channel, handler);
	}
	
	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************		
	private void makeUpdate(String cathegory, String data){
		RequestParams params = new RequestParams();
		params.put(cathegory, data);
		
		makeUpdate(params);
	}
	
	private void makeUpdate(RequestParams params){
			twitch.channels().put(channel, params, new ChannelResponseHandler() {
			
			@Override
			public void onFailure(int statusCode, String statusMessage, String errorMessage) {
				System.err.println("Twitch API responded with an error. The update was probably not performed");
				System.err.println(statusCode +" " + errorMessage);
				System.err.println(statusMessage);
				
			}
			
			@Override
			public void onFailure(Throwable throwable) {
				System.err.println("Error accessing twitch or error parsing response. The update was probably not performed");
				
			}
			
			@Override
			public void onSuccess(Channel channel) {
				params.stringEntrySet().stream()
					.forEach( 
						p -> System.out.println( "Sucessfully updated " + p.getKey() +" to " + p.getValue() )
					);
			}
		});	
	}

	// ****************************************** ACCESS TOKEN *****************************************
	
	//TODO: This should be part of the initial INIT chain
	private String getToken() {
		File file = new File("res/twitch.token");	
		String path = file.getAbsolutePath();
		
		//If a file "token" already exists, we read that token and return it
		if( file.exists() ){
			try {				
				return new String(Files.readAllBytes( Paths.get(path) ) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		//Otherwise, we have to create a new authentication token
		} else {
			try {
				URI callbackUri = new URI("http://127.0.0.1:23522");
				String authUrl = twitch.auth().getAuthenticationUrl(clientID, callbackUri, Scopes.CHANNEL_EDITOR, Scopes.CHANNEL_COMMERCIAL, Scopes.CHANNEL_SUBSCRIPTIONS, Scopes.CHANNEL_CHECK_SUBSCRIPTION );
				//Open a browser and show a twitch-authentication prompt
				promptForAuthentication(authUrl);	//TODO: Ask the user if they want this prompt
				
				boolean authSuccess = twitch.auth().awaitAccessToken();
				if (authSuccess){
					String token = twitch.auth().getAccessToken();
					
					//Create a file for the received token, so we don't have to go through this process again
					file.createNewFile();
					Files.write(Paths.get(path), token.getBytes());
					
					return token;
				} else {
					System.err.println( twitch.auth().getAuthenticationError() );
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//If we failed to authenticate or an error occurred
		return "";
	}

	private void promptForAuthentication(String url) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported( Desktop.Action.BROWSE)){
			try{
				desktop.browse( URI.create(url) );
			} catch (Exception e){
				e.printStackTrace();
			}			
		} else {
			System.err.println("Desktop is not supported. Cannot create Auth-prompt");
		}
	}
}
