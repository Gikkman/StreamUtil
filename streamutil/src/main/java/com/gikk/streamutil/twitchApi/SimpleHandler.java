package com.gikk.streamutil.twitchApi;

import com.mb3364.twitch.api.handlers.BaseFailureHandler;

public abstract class SimpleHandler implements BaseFailureHandler {
	
	@Override
	public void onFailure(int statusCode, String statusMessage, String errorMessage) {
		System.err.println("\tTwitch API responded with the following error: \n\t" +errorMessage + " " +statusMessage +" " +statusCode);
		
	}
	
	@Override
	public void onFailure(Throwable throwable) {
		System.err.println("Error accessing twitch or error parsing response");
		throwable.printStackTrace();		
	}
}
