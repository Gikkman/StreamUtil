package com.gikk.streamutil.twitchApi;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.gikk.streamutil.misc.GikkPreferences;
import com.gikk.streamutil.twitchApi.tasks.FollowerListener;
import com.gikk.streamutil.twitchApi.tasks.UpdateFollowersTask;
import com.mb3364.http.RequestParams;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.twitch.api.handlers.ChannelFollowsResponseHandler;
import com.mb3364.twitch.api.handlers.ChannelResponseHandler;
import com.mb3364.twitch.api.handlers.ChannelSubscriptionsResponseHandler;
import com.mb3364.twitch.api.handlers.StreamResponseHandler;
import com.mb3364.twitch.api.handlers.UserFollowResponseHandler;
import com.mb3364.twitch.api.models.Channel;

/**<b>Singleton</b><br><br>
 * 
 * This class handles all communication with Twitch. That includes retrieving information and assigning information. <br>
 * Most of the communication to Twitch is done asynchronously and handles response via different ResponeaHandlers.<br><br>
 * 
 * This class uses <a href="https://github.com/gikkman/Java-Twitch-Api-Wrapper">https://github.com/gikkman/Java-Twitch-Api-Wrapper</a><br>
 * which is a fork of <a href="https://github.com/ArcticLight/Java-Twitch-Api-Wrapper">https://github.com/ArcticLight/Java-Twitch-Api-Wrapper</a><br>
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
	
	private final UpdateFollowersTask updateFollowerTask;
	
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
		
		File file = GikkPreferences.GET().getPropertiesFile();
	   	PropertiesConfiguration prop = new PropertiesConfiguration();
    	prop.setDelimiterParsingDisabled(true);
    	try {
			prop.load(file);
		} catch (ConfigurationException e1) {
			System.err.println("Could not load the properties file! Make sure you have a valid 'gikk.properties' in your assigned data folder");
		}
    	
    	clientID = prop.getString("clientid");
    	channel = prop.getString("channel").substring(1); //Since the channel has a # in the front, we need to remove that one.
		token = prop.getString("token");
		
		twitch.setClientId( clientID );
		twitch.auth().setAccessToken(token);
		
		/*
		 * Create a task for firing onNewFollower events. 
		 * Have it poll Twitch 4 times a minute
		 */
		updateFollowerTask = new UpdateFollowersTask();
		updateFollowerTask.schedule(0, 15 * 1000);
	}

	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	public void addOnNewFollowerListener(FollowerListener listener){
		updateFollowerTask.addListener(listener);
	}
	public boolean removeOnNewFollowerListener(FollowerListener listener){
		return updateFollowerTask.removeListener(listener);
	}
	
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
	
	/**Fetches up to {@code maxAmount} of followers from your channel, ordered by follow-date
	 * 
	 * @param maxAmount The amount you want to fetch at most
	 * @param handler Twitch response handle
	 */
	public void getLatestFollowers(int maxAmount, ChannelFollowsResponseHandler handler){
		/* You can only fetch up to 100 followers per request, so if the maxAmount is larger
		 * than 100, we need to call the API several times recursively 
		 */
		if( maxAmount > 100 ){
			RecursiveFollowerHandler recursive = new RecursiveFollowerHandler(maxAmount, handler);
			RequestParams params = new RequestParams();
			params.put("limit", 100);
			twitch.channels().getFollows(channel, params ,recursive);
			
		} else {
			RequestParams params = new RequestParams();
			params.put("limit", maxAmount);
			twitch.channels().getFollows(channel, params ,handler);
		}
	}
	
	public int getFollowerCount(){
		return updateFollowerTask.getTotalFollowers();
	}
	
	public String getLatestFollower(){
		return updateFollowerTask.getLatestFollower();
	}
	
	/**Checks if {@code userName} is following {@code channel}. The channel <b>should not</b> have a #
	 * 
	 * @param userName The user whoms status we want to check
	 * @param channel The channel for which we are checking
	 * @param handler The result handler. It will call {@code onSuccess()} if the user is a follower and if the <i>CHANNEL DOES NOT EXIST</i>.
	 * In case the channel does not exist, the follow object in the {@code onSuccess()} method will be null.
	 */
	public void checkIfFollower(String userName, UserFollowResponseHandler handler){
		twitch.users().getFollow(userName, channel, handler);
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
	
	void getFollowers(int offset, int limit, ChannelFollowsResponseHandler handler){
		RequestParams params = new RequestParams();
		params.put("offset", offset);
		params.put("limit", limit);
		twitch.channels().getFollows(channel, params, handler);
	}
}
