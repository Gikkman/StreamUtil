package com.gikk.streamutil;


import com.gikk.gikk_stream_util.db0.gikk_stream_util.user.User;
import com.gikk.streamutil.misc.StackTrace;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**Class for making an instance of a Users object observable for JavaFX. Since each field has to be wrapped in
 * a TypeProperty, we create an instance of ObservableUser for each user we want to make visible for JavaFX. <br><br>
 * 
 * The ideal solution would be to be able to listen to changes to under underlying Speedment database object. That would
 * remove the potential risk of this design that a single database object has several different ObservableUser objects 
 * making it observable.<br>
 * However, since that is not possible, as far as I know, we have to rely on all changes to the underlying database to go 
 * through instances of this class, for now.<br><br>
 * 
 * TODO: Create a centralized ObservableUser dispatcher/cache, so that no more than one instance of an ObservableUser
 * can exist at the same time for an underlying Users object. This would also allow for a more natural way of using the
 * NewUser method, which feels very of to have in this class.
 * 
 * @author Simon
 *
 */
public class ObservableUser {
	public enum Status { Regular, Moderator, Admin };	
	private static final String REGULAR = "regular", MODERATOR = "moderator", ADMIN = "admin";
	
	private final User user;
	private final SimpleStringProperty userName = new SimpleStringProperty();
//TODO:	private final SimpleStringProperty status = new SimpleStringProperty(REGULAR);
	private final SimpleIntegerProperty timeOnline = new SimpleIntegerProperty(0);
	private final SimpleIntegerProperty linesWritten = new SimpleIntegerProperty(0);
	private final SimpleBooleanProperty trusted = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty follower = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty subscriber = new SimpleBooleanProperty(false);
	
	//***********************************************************
	//		STATIC
	//***********************************************************
	public static String parseStatus(Status status){
		switch (status){
		case Admin:
			return ADMIN;
		case Moderator:
			return MODERATOR;
		case Regular:
		default:
			return REGULAR;
		}
	}
	
	public static Status parseStatus(String status){
		String temp = status.trim();
		if( temp.equalsIgnoreCase(ADMIN) )
			return Status.Admin;
		else if( temp.equalsIgnoreCase(MODERATOR) )
			return Status.Moderator;
		else if ( temp.equalsIgnoreCase(REGULAR) )
			return Status.Regular;
		
		System.err.println("\tError! Unable to parse status " + status + " " + StackTrace.getStackPos() );
		return null;
	}
	
	public static ObservableUser addNewUser(String userName){
		//TODO: Create new user
		return null;
	}
	
	//***********************************************************
	//		CONSTRUCTORS
	//***********************************************************
	public ObservableUser(User user){
		this.user = user;
		this.userName.set( user.getUsername() );
		//TODO: this.status.set( user.getStatus() );
		this.follower.set( user.getIsFollower() );
		this.subscriber.set( user.getIsSubscriber() );
		this.timeOnline.set( user.getTimeOnline() );
		this.linesWritten.set( user.getLinesWritten() );
		this.trusted.set( user.getIsTrusted() );
	}
	
	//***********************************************************
	//		PUBLIC
	//***********************************************************
//	TODO:	public String getStatus() {
//		return status.get();
//	}
	
//	TODO:	public void setStatus(Status status) {
//		this.user.setStatus(status);
//		this.status.setValue( parseStatus(status) );
//	}

	public boolean isFollower() {
		return follower.get();
	}
	
	public boolean isSubscriber() {
		return subscriber.get();
	}

	public void setFollower(boolean follower) {
		this.user.setIsFollower(follower);
		this.follower.setValue(follower);
	}

	/**Returns the number of minutes we've seen this user online.
	 */
	public long getTimeOnline() {
		return timeOnline.get();
	}
	
	public String getTimeOnlineFormated() {
		long t = timeOnline.get();
		long h = t / 60;
		long m = t % 60;
		
		String out = "";
		out += (h > 0 ? h + "h " : "");
		out += m + "m";
		return out;
	}

	public void addTimeOnline(int min){
		int newTime = timeOnline.get() + min;
		
		this.user.setTimeOnline( newTime );
		this.timeOnline.set( newTime );
	}
	
	public void setTimeOnline(int newMin) {
		this.user.setTimeOnline(newMin);
		this.timeOnline.set(newMin);
	}

	public long getLinesWritten() {
		return linesWritten.get();
	}

	public void addLinesWritten(int amount){
		int newLinesWritten = this.linesWritten.get() + 1;
		
		this.user.setLinesWritten(newLinesWritten);
		this.linesWritten.set( newLinesWritten );
	}
	
	public void setLinesWritten(int newAmount) {		
		this.user.setLinesWritten( newAmount );
		this.linesWritten.set( newAmount );
	}

	public String getUserName() {
		return userName.get();
	}
	
	@Override
	public String toString(){
		String out = getUserName() + " (" /*TODO: + getStatus()*/ +")";
		if( isSubscriber() )
			out += " [Subscriber]";
		else if( isFollower() )
			out += " [Follower]";
		else
			out += " [ :'-( ]";
		out += " Time online: " + getTimeOnlineFormated();
		out += " Lines written: " + getLinesWritten();
		return out;
	}
	
	//***********************************************************
	//		JAVA FX REQUIREMENTS
	//***********************************************************
	public StringProperty userNameProperty(){
		return userName;
	}
//TODO:	public StringProperty statusProperty(){
//		return status;
//	}
	public IntegerProperty timeOnlineProperty(){
		return timeOnline;
	}
	public IntegerProperty linesWrittenProperty(){
		return linesWritten;
	}
	public BooleanProperty followerProperty(){
		return follower;
	}
	public BooleanProperty subscriberProperty(){
		return subscriber;
	}
}
