package com.gikk.streamutil.twitchApi;

import java.util.LinkedList;
import java.util.List;

import com.mb3364.twitch.api.handlers.ChannelFollowsResponseHandler;
import com.mb3364.twitch.api.models.ChannelFollow;

/**Since we are only given followers in bunches of 25 from Twitch, we have to poll the API several times with an 
 * followers-offset. <br><br>
 * 
 * This class handles those recursive calls to the Twitch API, in order to access as many followers as possible. 
 * Twitch however has a cap at the number of followers which can be retrieved, at 1600.
 * 
 * @author Simon
 *
 */
class RecursiveFollowerHandler extends SimpleChannelFollowerHandler{
	private final ChannelFollowsResponseHandler finalHandler;
	private final LinkedList<ChannelFollow> followers;
	private final int target;
	private final int index;
	
	public RecursiveFollowerHandler(int targetNumber, ChannelFollowsResponseHandler finalHandle) {
		this.finalHandler = finalHandle;
		this.target = targetNumber;
		index = 0;
		followers = new LinkedList<>();
	}
	
	private RecursiveFollowerHandler(int currnentIndex, int targetNumber, LinkedList<ChannelFollow> followersSeenThusFar, ChannelFollowsResponseHandler finalHandle) {
		this.followers = followersSeenThusFar;
		this.index = currnentIndex;
		this.target = targetNumber;
		this.finalHandler = finalHandle;
	}
	
	@Override
	public void onSuccess(int totalFollowers, List<ChannelFollow> receivedFollowers) {
		this.followers.addAll(receivedFollowers);
		int gotten = index + 100;
		if( gotten < target && gotten < totalFollowers && gotten < 1600 ){
			RecursiveFollowerHandler next = new RecursiveFollowerHandler(gotten, target, followers, finalHandler);
			TwitchApi.GET().getFollowers(gotten, Math.min(100, target-gotten), next);
		} else {
			//Call the actual response handler
			finalHandler.onSuccess(totalFollowers, followers);
		}
			
	}
	
}
