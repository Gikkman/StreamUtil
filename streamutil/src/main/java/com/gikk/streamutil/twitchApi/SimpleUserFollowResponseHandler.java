package com.gikk.streamutil.twitchApi;

import com.mb3364.twitch.api.handlers.UserFollowResponseHandler;

public abstract class SimpleUserFollowResponseHandler implements UserFollowResponseHandler {
	
	@Override
	public void onFailure(Throwable arg0) {
		arg0.printStackTrace();
		onFailure(-1, "Error thrown", "An error occured when accessing Twitch API");
	}
}
