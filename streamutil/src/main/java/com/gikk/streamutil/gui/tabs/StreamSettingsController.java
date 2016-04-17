package com.gikk.streamutil.gui.tabs;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.gikk.streamutil.task.OneTimeTask;
import com.gikk.streamutil.task.RepeatedTask;
import com.gikk.streamutil.twitchApi.SimpleChannelFollowerHandler;
import com.gikk.streamutil.twitchApi.SimpleChannelHandler;
import com.gikk.streamutil.twitchApi.SimpleChannelSubscriptionHandler;
import com.gikk.streamutil.twitchApi.SimpleStreamHandler;
import com.gikk.streamutil.twitchApi.TwitchApi;
import com.mb3364.twitch.api.models.Channel;
import com.mb3364.twitch.api.models.ChannelFollow;
import com.mb3364.twitch.api.models.ChannelSubscription;
import com.mb3364.twitch.api.models.Stream;
import com.mb3364.twitch.api.models.User;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**Probably the most monolithic class in the entire project.<br>
 * This class handles the Stream settings and statistics. It displays the number of viewers we currently have,
 * number of followers and subs, latest follower and subs and more. It also allows the user to change the stream
 * title and game on Twitch.<br><br>
 * 
 * TODO: Add support for showing people online, and right click actions on them
 * 
 * @author Simon
 *
 */
public class StreamSettingsController extends _TabControllerBase{
	@FXML TextField txt_streamStatus;
	@FXML TextField txt_streamGame;
	@FXML Button btn_streamStatusApply;
	@FXML Label lbl_viewerCount; 	@FXML Label lbl_followerCount; 	@FXML Label lbl_subscriberCount;
	@FXML Label lbl_latestFollower; @FXML Label lbl_lastestSubscriber; @FXML Label lbl_alltimeViews;
	
	@FXML TableView<User> Tbl_UsersOnline;
	private ObservableList<User> usersOnline;
	
	private String onlineStreamStatus = "", onlineStreamGame = "", 	onlineFollowerCount = "", onlineSubCount = "",
				   onlineViewCount = "", 	onlineAlltimeViewCount = "";
	
	private TwitchApi api;
	
	@Override
	public int getWeight() {
		return -1;
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.api = TwitchApi.GET();
		
		//Fetch the online list from the UserManager. 
		//First, add all currently online listed users to our visible list.
		//Then, add a change listener, so we see when users are added or removed
		//TODO: usersOnline = UserManager.GET().getUserOnlineList();

		Tbl_UsersOnline.getColumns().addAll( getColumns() );
		Tbl_UsersOnline.setItems(usersOnline);
		
		RepeatedTask pollApiService = new PollApiService(api, this);
		pollApiService.schedule(100, 15 * 1000 );	//Poll the api almost immediately, then once every 15 seconds
	}
	
	@FXML protected void updateUsersOnline(){	
		
	}
	
	/**Pushes the current stream settings to Twitch. After the buttons been pushed, there is a delay until the button
	 * reactivates again, so that the user doesn't spam Twitch with requests.
	 * 
	 */
	@FXML protected void applyStreamStatus(ActionEvent e){
		btn_streamStatusApply.setDisable(true);
		api.setStatus( txt_streamStatus.getText(), txt_streamGame.getText() );
		
		OneTimeTask t = new OneTimeTask() {			
			@Override
			public void onExecute() {
				btn_streamStatusApply.setDisable(false);
			}
		};
		t.schedule(15);
	}
	
	/**************************************************************************************************
	 * 
	 * These different set-methods are split up since they will receive answers from the API
	 * at different times, and sometimes only few of them will acutally be invoked. 
	 * 
	 **************************************************************************************************/
	private void setStatuses(String status, String game, String viewerCount, String alltimeViewCount){
		Platform.runLater( () -> {
			if( !onlineStreamStatus.matches(status) ){
				onlineStreamStatus = status;
				txt_streamStatus.setText(status);
			}
			if( !onlineStreamGame.matches(game) ){
				onlineStreamGame = game;
				txt_streamGame.setText(game);
			}
			if( !onlineAlltimeViewCount.matches(alltimeViewCount)){
				onlineAlltimeViewCount = alltimeViewCount;
				lbl_alltimeViews.setText(alltimeViewCount);
			}
			
		} );
	}

	private void setViewers(String viewers){	
		if( !onlineViewCount.matches(viewers)){
			Platform.runLater( () ->{
				onlineViewCount = viewers;
				lbl_viewerCount.setText(viewers);
			} );			
		}
	}
	
	private void setFollowers(String followerCount, String latestFollower){
		if( !onlineFollowerCount.matches(followerCount) ){
			Platform.runLater( () -> {
				lbl_followerCount.setText(followerCount);
				lbl_latestFollower.setText(latestFollower);
				
				onlineFollowerCount = followerCount;
			});
		}		
	}
	
	private void setSubscribers(String subCount, String latestSub){
		if( !onlineSubCount.matches(subCount) ){
			Platform.runLater( () -> {
				lbl_subscriberCount.setText(subCount);
				lbl_lastestSubscriber.setText(latestSub);
				
				onlineSubCount = subCount;
			});
		}		
	}
	
	/**
	 * Creates table columns for displaying the currently online users
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private TableColumn[] getColumns(){
		TableColumn[] cols = new TableColumn[6];
		
		cols[0] = new TableColumn<User, String>("UserName");
		cols[1] = new TableColumn<User, String>("Status");
		cols[2] = new TableColumn<User, String>("Follower");
		cols[3]	= new TableColumn<User, String>("Subscriber");
		cols[4] = new TableColumn<User, Integer>("TimeOnline");
		cols[5]	= new TableColumn<User, Integer>("LinesWritten");
		
		cols[0].setCellValueFactory( new PropertyValueFactory<User, String>("userName") );
		cols[1].setCellValueFactory( new PropertyValueFactory<User, String>("status") );
		cols[2].setCellValueFactory( new PropertyValueFactory<User, String>("follower") );
		cols[3].setCellValueFactory( new PropertyValueFactory<User, String>("subscriber") );
		cols[4].setCellValueFactory( new PropertyValueFactory<User, Integer>("timeOnline") );
		cols[5].setCellValueFactory( new PropertyValueFactory<User, Integer>("linesWritten") );
		
        cols[0].setPrefWidth(200); cols[0].setEditable(false);
        cols[1].setPrefWidth(110); 
		cols[2].setPrefWidth(80); 
		cols[3].setPrefWidth(80); 
		cols[4].setPrefWidth(80); 
		cols[5].setPrefWidth(80); 
				
		return cols;
	}
	
	//*************************************************************************************************************
	//									INNER CLASS : Poll API
	//*************************************************************************************************************
	private class PollApiService extends RepeatedTask{
		private final TwitchApi api;
		private final StreamSettingsController ssc;
		
		protected PollApiService(TwitchApi api, StreamSettingsController ssc) {	
			this.api = api;
			this.ssc = ssc;
		}

		/**This method is called periodically. It polls the Twitch API to retrieve how many viewers we have, how many followers,
		 * which game we have on Twitch's side and so on.
		 * 
		 * It also updates the list of our most recent followers and subscribers
		 */
		@Override
		public void onExecute() {
			System.out.println("Polling channel");
			
			api.getStreamInfo( new SimpleChannelHandler() {
				
				@Override
				public void onSuccess(Channel channel) {
					ssc.setStatuses(channel.getStatus(), 
								    channel.getGame(), 
								    "OFFLINE",
								    String.valueOf( channel.getViews() ) );
				
					if( channel.getFollowers() > 0 )
						getFollowers();
					else 
						ssc.setFollowers("0", "-");
					
					if( channel.isPartner() )
						getSubs();
					else
						ssc.setSubscribers("0", "-");
				}
			} );
			
			api.getViewers( new SimpleStreamHandler() {
				
				@Override
				public void onSuccess(Stream stream) {
					String viewers = stream == null ? "OFFLINE" : String.valueOf( stream.getViewers() );	
					ssc.setViewers(viewers);
				}
			});			
		}
		
		private void getFollowers(){
			api.getFollowers( new SimpleChannelFollowerHandler() {				
				@Override
				public void onSuccess(int total, List<ChannelFollow> follows) {
					String count = String.valueOf(total);
					String latest = total == 0 ? "-" : follows.get(0).getUser().getDisplayName();
					
					ssc.setFollowers( count, latest );
				}
			} );
		}
		
		private void getSubs(){
			api.getSubscribers( new SimpleChannelSubscriptionHandler() {				
				@Override
				public void onSuccess(int total, List<ChannelSubscription> subscriptions) {
					String count = String.valueOf(total);
					String latest = total == 0 ? "-" : subscriptions.get(0).getUser().getDisplayName();
					
					ssc.setSubscribers(count, latest);
				}
			} );
		}
	}
}
