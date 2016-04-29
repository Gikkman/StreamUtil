package com.gikk.streamutil.twitchApi.tasks;

public interface FollowerListener {
	public void onFollower(String follower, int total, boolean isNew);
}
