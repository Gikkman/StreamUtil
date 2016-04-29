package com.gikk.streamutil.twitchApi;

import com.mb3364.twitch.api.handlers.UserFollowResponseHandler;

public abstract class SimpleUserFollowResponseHandler implements UserFollowResponseHandler {
	
	@Override
	public void onFailure(Throwable arg0) {
	}
	
	@Override
	public void onFailure(int statusCode, String statusMessage, String errorMessage) {
	}
}
