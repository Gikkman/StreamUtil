package com.gikk.streamutil.irc.listeners;

import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.twitchApi.SimpleUserFollowResponseHandler;
import com.gikk.streamutil.twitchApi.TwitchApi;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;
import com.mb3364.twitch.api.models.UserFollow;

/**This listener makes sure that if someone JOIN our chat and
 * is a follower, that person is marked as a follower.<br><br>
 * 
 * There is a risk that we miss follows, if someone follow us
 * while we are offline. But this listener will catch that and 
 * correct the users FOLLOWER status
 * 
 * @author Simon
 *
 */
public class CheckFollowListener implements IrcListener{
	private final UserDatabaseCommunicator uDBc;
	
	public CheckFollowListener(UserDatabaseCommunicator uDBc) {
		this.uDBc = uDBc;
	}
	
	@Override
	public void onJoin(IrcUser user) {
		final String nick = user.getNick();
		
		TwitchApi.GET().checkIfFollower(nick, new SimpleUserFollowResponseHandler() {
			@Override
			public void onSuccess(UserFollow follow) {
				ObservableUser oUser = uDBc.getOrCreate(nick);
				if( !oUser.getFollower() )
					oUser.setFollower(true);
			}
		});
	}
}
