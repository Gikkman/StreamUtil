package com.gikk.streamutil.twitchApi.tasks;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.gikk.streamutil.task.OneTimeTask;
import com.gikk.streamutil.task.RepeatedTask;
import com.gikk.streamutil.twitchApi.SimpleChannelFollowerHandler;
import com.gikk.streamutil.twitchApi.TwitchApi;
import com.mb3364.twitch.api.models.ChannelFollow;

/**This class polls Twitch once a minute for followers, and fires {@code onNewFollower} events 
 * whenever we see a follower that we haven't seen before.<br><br>
 * 
 * Follower alerts are spaced out over  minute
 * 
 * @author Simon
 *
 */
public class UpdateFollowersTask extends RepeatedTask{
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private final LinkedList<FollowerListener> listeners = new LinkedList<>();
	
	private Set<ChannelFollow> previousFollows = null;
	private int previousTotal = -1;
	private final AtomicReference<String> latestFollower = new AtomicReference<String>("None");
	
	private int amountToGet = 50;
	//***********************************************************
	// 				PUBLIC
	//***********************************************************
	public synchronized void addListener(FollowerListener listener){
		listeners.add(listener);
	}
	
	public synchronized boolean removeListener(FollowerListener listener){
		return listeners.remove(listener);
	}
	
	public int getTotalFollowers(){
		return previousTotal;
	}
	
	public String getLatestFollower(){
		return latestFollower.get();
	}
	
	@Override
	public void onExecute() {
		/* Twitch uses a caching policy where they cache responses from previous requests for up 
		 * to 5 minutes. So if we ask for the same amount of users again and again, we won't get
		 * updates particularly frequent. 
		 * So by changing the size of the request, we force Twitch to generate new responses, and
		 * thus we get more up-to-date responses quicker. 
		 */
		amountToGet = --amountToGet <= 15 ? amountToGet : 50; 
		TwitchApi.GET().getLatestFollowers( amountToGet, new MyResponseHandle() );
	}
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************
	/**We need to synchronize the call to all the listeners, to avoid concurrent 
	 * modification to the list
	 * 
	 * @param follower The new follower
	 */
	private synchronized void callListeners(String follower, int total, boolean isNew){
		try{
			for(FollowerListener listener : listeners )
				listener.onFollower(follower, total, isNew);
		} catch (Exception e) {
			System.err.println("Error executing a onNewFollower listener");
			e.printStackTrace();
		}
	}
	
	//***********************************************************
	// 				PRIVATE CLASS : RESPONSE HANDLE
	//***********************************************************
	private class MyResponseHandle extends SimpleChannelFollowerHandler{
		@Override
		public void onSuccess(int total, List<ChannelFollow> follows) {
			/* By design, we do not collect users who unfollow and re-follow again during the same
			 * session. This is to avoid that users spam our onFollow notifications
			 */
			if( previousTotal != -1){
				/*
				 * Collect the follows that are new.
				 * While it might be possible to just check the new total against the old
				 * total, that does not cover the case of one user de-following us and a new
				 * user following us in the same time frame. This solution covers that case
				 */
				List<ChannelFollow> newFollows = follows.stream()
													    .filter( p -> !previousFollows.contains(p) )
													    .collect( Collectors.toList() );
				if( newFollows.size() > 0 ){
					previousFollows.addAll(newFollows);
					previousTotal = total;
					/* 
					 * We want to space the newFollower calls out over a minute, so we
					 * create one task for each new follower and schedule them over the
					 * span of a minute, with the first one occurring instantly.
					 */
					int delaySeconds = newFollows.size() > 1 ? 60 / newFollows.size() : 0;
					for( int i = 0; i < newFollows.size(); i++){
						String follower = newFollows.get(i).getUser().getDisplayName().toLowerCase(Locale.ENGLISH);
						
						//Twitch's total follower count is not sync'd the new followers becoming visible, so we just pass the current total they give us
						OneTimeTask task = new MyNewFollowerTask( follower, total, true );  
						task.schedule( delaySeconds * i * 1000 );
					}
				}
			} else {
				String follower = follows.size() == 0 ? latestFollower.get() : follows.get(0).getUser().getDisplayName().toLowerCase(Locale.ENGLISH);
				OneTimeTask task = new MyNewFollowerTask( follower, total, false );  
				task.schedule(0);
				previousFollows = follows.stream().collect( Collectors.toSet() );
				previousTotal = total;
			}
						
		}		
	}
	
	//***********************************************************
	// 				PRIVATE CLASS : FOLLOW ALERT TASK
	//***********************************************************
	private class MyNewFollowerTask extends OneTimeTask{
		private final String follower;
		private final int total;
		private final boolean isNew;
		
		public MyNewFollowerTask(String follower, int total, boolean isNew) {
			this.follower = follower;
			this.total = total;
			this.isNew = isNew;
		}
		
		@Override
		public void onExecute() {
			latestFollower.set(follower);
			callListeners(follower, total, isNew);
		}		
	}
}
