package com.gikk.streamutil.gui.tabs;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.task.OneTimeTask;
import com.gikk.streamutil.task.RepeatedTask;
import com.gikk.streamutil.twitchApi.SimpleChannelHandler;
import com.gikk.streamutil.twitchApi.SimpleChannelSubscriptionHandler;
import com.gikk.streamutil.twitchApi.SimpleStreamHandler;
import com.gikk.streamutil.twitchApi.TwitchApi;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserStatus;
import com.mb3364.twitch.api.models.Channel;
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
 * 
 * @author Simon
 *
 */
public class StreamSettingsController extends _TabControllerBase{
	//*************************************************************************************************************
	//									VARIABLES
	//*************************************************************************************************************
	@FXML TextField txt_streamStatus;
	@FXML TextField txt_streamGame;
	@FXML Button btn_streamStatusApply;
	@FXML Label lbl_viewerCount; 	@FXML Label lbl_followerCount; 	@FXML Label lbl_subscriberCount;
	@FXML Label lbl_latestFollower; @FXML Label lbl_lastestSubscriber; @FXML Label lbl_alltimeViews;
	
	@FXML TableView<ObservableUser> Tbl_UsersOnline;
	private ObservableList<ObservableUser> usersOnline;
	private TwitchApi api;
	
	private String onlineStreamStatus = "DEFAULT_STRING_WILL_BE_REPLACED", 
				   onlineStreamGame   = "DEFAULT_STRING_WILL_BE_REPLACED", 
				   onlineSubCount     = "DEFAULT_STRING_WILL_BE_REPLACED",
				   onlineViewCount    = "DEFAULT_STRING_WILL_BE_REPLACED", 	
				   onlineAlltimeViewCount = "DEFAULT_STRING_WILL_BE_REPLACED";
	
	
	@Override
	public int getWeight() {
		return -1;
	};
	
	//*************************************************************************************************************
	//									INITIALIZER
	//*************************************************************************************************************
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.api = TwitchApi.GET();
		
		//Fetch the online list
		usersOnline = GikkBot.GET().getUsersOnlineList();

		//Setup the UsersOnline table
		Tbl_UsersOnline.getColumns().addAll( getColumns() );
		Tbl_UsersOnline.setItems(usersOnline);
		
		//Poll the API for stream details (viewer count and such)
		RepeatedTask pollApiService = new PollApiService(api, this);
		pollApiService.schedule(100, 60 * 1000 );	//Poll the api almost immediately, then 1 time per minute
		
		//Make sure that the latest follower is kept updated
		api.addOnNewFollowerListener( (follower, total, isNew) -> setFollowers(follower, total) );
		lbl_followerCount.setText( Integer.toString( api.getFollowerCount() ) );
		lbl_latestFollower.setText( api.getLatestFollower() );
			
		//Make the apply button fire on ENTER strokes too
		btn_streamStatusApply.defaultButtonProperty().bind(btn_streamStatusApply.focusedProperty());
	}
	
	//*************************************************************************************************************
	//									FXML METHODS
	//*************************************************************************************************************
	
	/**Pushes the current stream settings to Twitch. After the buttons been pushed, there is a delay until the button
	 * reactivates again, so that the user doesn't spam Twitch with requests.
	 * 
	 * @param e Ignored
	 */
	@FXML protected void applyStreamStatus(ActionEvent e){
		btn_streamStatusApply.setDisable(true);
		api.setStatus( txt_streamStatus.getText(), txt_streamGame.getText() );
		
		OneTimeTask t = new OneTimeTask() {			
			@Override
			public void onExecute() {
				Platform.runLater( () -> btn_streamStatusApply.setDisable(false) );
			}
		};
		t.schedule(5 * 1000);
		
		txt_streamStatus.requestFocus();
	}
	
	@FXML protected void titleFieldAction(ActionEvent e){
		txt_streamGame.requestFocus();
	}
	
	@FXML protected void gameFieldAction(ActionEvent e){
		btn_streamStatusApply.requestFocus();
	}
	
	/**************************************************************************************************
	 * 
	 * These different set-methods are split up since they will receive answers from the API
	 * at different times, and sometimes only few of them will actually be invoked. 
	 * 
	 **************************************************************************************************/
	//************************************************************************************************** 
	//									PRIVATE
	//************************************************************************************************** 
	private void setStatuses(String status, String game, String viewerCount, String alltimeViewCount){
		//Status and Game can be null, in case the user hasn't set one for his/her stream
		if( status == null )
			status = "Connected! Try setting a status!";
		if( game == null )
			game = "Connected! Try setting a game!";
		
		final String newStatus = status;
		final String newGame   = game;
		Platform.runLater( () -> {
			if( !onlineStreamStatus.matches(newStatus) ){
				onlineStreamStatus = newStatus;
				txt_streamStatus.setText(newStatus);
			}
			if( !onlineStreamGame.matches(newGame) ){
				onlineStreamGame = newGame;
				txt_streamGame.setText(newGame);
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
	
	private void setFollowers(String latestFollower, int newTotal){
		String newTotalString = Integer.toString(newTotal);
		Platform.runLater( () -> {
			lbl_followerCount.setText(newTotalString);
			lbl_latestFollower.setText(latestFollower);
		});	
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
		TableColumn[] cols = new TableColumn[7];
		
		cols[0] = new TableColumn<User, String>("UserName");
		cols[1] = new TableColumn<User, UserStatus>("Status");
		cols[2] = new TableColumn<User, Boolean>("Follower");
		cols[3]	= new TableColumn<User, Boolean>("Sub");
		cols[4] = new TableColumn<User, Boolean>("Trusted");
		cols[5] = new TableColumn<User, Integer>("Time");
		cols[6]	= new TableColumn<User, Integer>("Lines");
		
		cols[0].setCellValueFactory( new PropertyValueFactory<User, String>("userName") );
		cols[1].setCellValueFactory( new PropertyValueFactory<User, UserStatus>("status") );
		cols[2].setCellValueFactory( new PropertyValueFactory<User, Boolean>("follower") );
		cols[3].setCellValueFactory( new PropertyValueFactory<User, Boolean>("subscriber") );
		cols[4].setCellValueFactory( new PropertyValueFactory<User, Boolean>("trusted") );
		cols[5].setCellValueFactory( new PropertyValueFactory<User, Integer>("timeOnline") );
		cols[6].setCellValueFactory( new PropertyValueFactory<User, Integer>("linesWritten") );
		
        cols[0].setPrefWidth(200); cols[0].setEditable(false); 
        cols[1].setPrefWidth(110); cols[1].setEditable(false); cols[1].getStyleClass().add( "center-align" );
		cols[2].setPrefWidth(60);  cols[2].setEditable(false); cols[2].getStyleClass().add( "center-align" );  
		cols[3].setPrefWidth(60);  cols[3].setEditable(false); cols[3].getStyleClass().add( "center-align" );  
		cols[4].setPrefWidth(60);  cols[4].setEditable(false); cols[4].getStyleClass().add( "center-align" );  
		cols[5].setPrefWidth(60);  cols[5].setEditable(false); cols[5].getStyleClass().add( "center-align" );  
		cols[6].setPrefWidth(60);  cols[6].setEditable(false); cols[6].getStyleClass().add( "center-align" );  
				
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
			api.getStreamInfo( new SimpleChannelHandler() {
				
				@Override
				public void onSuccess(Channel channel) {
					ssc.setStatuses(channel.getStatus(), 
								    channel.getGame(), 
								    "OFFLINE",
								    String.valueOf( channel.getViews() ) );
					
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
