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
	private final int index;
	
	public RecursiveFollowerHandler(ChannelFollowsResponseHandler finalHandle) {
		this.finalHandler = finalHandle;
		index = 0;
		followers = new LinkedList<>();
	}
	
	private RecursiveFollowerHandler(int currnentIndex, LinkedList<ChannelFollow> followersSeenThusFar, ChannelFollowsResponseHandler finalHandle) {
		this.followers = followersSeenThusFar;
		this.index = currnentIndex;
		this.finalHandler = finalHandle;
	}
	
	@Override
	public void onSuccess(int totalFollowers, List<ChannelFollow> receivedFollowers) {
		this.followers.addAll(receivedFollowers);
		int gotten = index + 25;
		if(  gotten < totalFollowers && gotten < 1600 ){
			RecursiveFollowerHandler next = new RecursiveFollowerHandler(gotten, followers, finalHandler);
			TwitchApi.GET().getFollowers(gotten, next);
		} else {
			//Call the actual response handler
			finalHandler.onSuccess(totalFollowers, followers);
		}
			
	}
	
}
