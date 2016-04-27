package com.gikk.streamutil.gui.init;

import com.gikk.streamutil.misc.Callback;
import com.gikk.streamutil.task.OneTimeTask;
import com.gikk.streamutil.task.RepeatedTask;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.twitch.api.auth.grants.implicit.AuthenticationError;

/**Asynchronous task for fetching the TwitchAPI token. <br><br>
 * 
 * This class uses two thread so look for the API token: one for waiting for
 * Incoming messages from the Twitch server, and one for updating UI components
 * so the user can see that something is happening under the hood.<br><br>
 * 
 * Once the server responds with a token, or 60 seconds have passed, the waiting
 * thread finishes and and returns either the token or the error that occurred.<br><br>
 * 
 * The class needs 3 callbacks: a success callback, a failure callback and a tick callback.
 * The tick callback is called once per seconds while we wait for the Twitch server to respond,
 * the failure callback is called if we didn't receive a API token and the success callback
 * is called if we got a token.
 * 
 * @author Simon
 *
 */
class FetchTwitchTokenTask extends RepeatedTask{
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private Object LOCK = new Object();
	private AwaitTask task = new AwaitTask();
	
	private final Twitch twitch;
	
	private boolean results = false;
	private String token = null;
	private AuthenticationError error = null;
	
	private Callback tickCallback;
	private Callback successCallback;
	private Callback failureCallback;

	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	/**This task will try to retrieve a Twitch API token asynchronously. <br><br>
	 * 
	 * Prior to passing a Twitch object to this class, the user is responsible for calling the 
	 * {@code .getAuthenticationUrl()} method. The user is also responsible for opening a browser-prompt
	 * so that the responder has the ability to authorize access.<br><br>
	 * 
	 * The 3 callback methods will be called whenever certain events occur:<ul> 
	 * <li>The {@code tickCallback} will be called whenever
	 * this task executes, and no results have been received from Twitch.
	 *  <li>The {@code successCallback} will be called when this task executes and we have received a 
	 * token from Twitch. The {@code getTwitchToken()} method will not return {@code null} from inside this callback.
	 *  <li>The {@code failureCallback} will be called when this task executes and we have received a 
	 * token from Twitch. The {@code getError()} method will not return {@code null} from inside this callback.
	 * </ul>
	 * @param twitch
	 * @param successCallback
	 * @param failureCallback
	 * @param tickCallback
	 */
	public FetchTwitchTokenTask(Twitch twitch, Callback successCallback, Callback failureCallback, Callback tickCallback) {
		this.twitch = twitch;	
		this.successCallback = successCallback;
		this.failureCallback = failureCallback;
		this.tickCallback = tickCallback;
	}

	//***********************************************************
	// 				PUBLIC
	//***********************************************************		
	@Override
	public void onExecute() {				
		synchronized (LOCK) {
			if(results){
				if(token != null)
					successCallback.execute();
				else
					failureCallback.execute();
				stopTask( false );
				
				//Reset all values, so that the task can be reused
				results = false;
				token = null;
				error = null;
			}
			else
				tickCallback.execute();
		}
	}
	
	/**Fetches the Twitch API token. If no token's been received, this will
	 * return {@code null}
	 */
	public String getTwitchToken(){
		return token;
	}
	
	/**Fetches the error we received while waiting for the Twitch API token. 
	 * If no error has occured, this will return {@code null}
	 */
	public AuthenticationError getError(){
		return error;
	}
	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
	@Override
	protected void onScheduled() {
		//Start waiting for input almost immediately. This number is kinda magic, but I noticed that
		//start waiting imidiatley could cause problems.
		task.schedule(10);
	}
	
	private class AwaitTask extends OneTimeTask{

		@Override
		public void onExecute() {
			boolean success = twitch.auth().awaitAccessToken();	
			synchronized (LOCK) {
				results = true;
				if( success ){			
					token = twitch.auth().getAccessToken();
				} else {
					error = twitch.auth().getAuthenticationError();	
				}				
			}
		}		
	}
}
