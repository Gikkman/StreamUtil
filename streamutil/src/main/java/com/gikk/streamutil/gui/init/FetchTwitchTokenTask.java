package com.gikk.streamutil.gui.init;

import com.gikk.streamutil.misc.Callback;
import com.gikk.streamutil.task.OneTimeTask;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.twitch.api.auth.grants.implicit.AuthenticationError;

class FetchTwitchTokenTask extends OneTimeTask{
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private Object LOCK = new Object();
	
	private final Twitch twitch;

	private long startTime;
	private boolean results = false;
	private String token = null;
	private AuthenticationError error = null;
	
	private Callback successCallback;
	private Callback failCallback;

	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	public FetchTwitchTokenTask(Twitch twitch) {
		this.twitch = twitch;	
	}

	//***********************************************************
	// 				PUBLIC
	//***********************************************************		
	@Override
	public void onExecute() {
		//TODO: Send the request manually and see what we get back. If it is an error, we tell them that the adress is wrong
		boolean success = twitch.auth().awaitAccessToken();	
		if( success ){					
			token = twitch.auth().getAccessToken();
			successCallback.execute();
		} else {
			System.err.println("Twitch token error!");
			error = twitch.auth().getAuthenticationError();	
			failCallback.execute();
		}
	}
	
	public void setSuccessCallback(Callback callback){
		this.successCallback = callback;
	}
	public void setFailCallback(Callback callback){
		this.failCallback = callback;
	}
	
	public String getTwitchToken(){
		return token;
	}
	public AuthenticationError getError(){
		return error;
	}
	//***********************************************************
	// 				PRIVATE
	//***********************************************************
}
