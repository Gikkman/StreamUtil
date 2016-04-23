package com.gikk.streamutil.users;

import com.gikk.gikk_stream_util.db0.gikk_stream_util.user.User;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class for making an instance of a Users object observable for JavaFX. Since
 * each field has to be wrapped in a TypeProperty, we create an instance of
 * ObservableUser for each user we want to make visible for JavaFX. <br>
 * <br>
 * 
 * The ideal solution would be to be able to listen to changes to under
 * underlying Speedment database object. That would remove the potential risk of
 * this design that a single database object has several different
 * ObservableUser objects making it observable.<br>
 * However, since that is not possible, as far as I know, we have to rely on all
 * changes to the underlying database to go through instances of this class, for
 * now.<br>
 * <br>
 * 
 * 
 * @author Simon
 *
 */
public class ObservableUser {
	private User user;
	private final SimpleStringProperty userName = new SimpleStringProperty();
	private final SimpleStringProperty status = new SimpleStringProperty(UserStatus.REGULAR.toString());
	private final SimpleIntegerProperty timeOnline = new SimpleIntegerProperty(0);
	private final SimpleIntegerProperty linesWritten = new SimpleIntegerProperty(0);
	private final SimpleBooleanProperty trusted = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty follower = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty subscriber = new SimpleBooleanProperty(false);


	// ***********************************************************
	// CONSTRUCTORS
	// ***********************************************************
	public ObservableUser(User user) {
		this.user = user;
		this.userName.set(user.getUsername());
		this.status.set(user.getStatus());
		this.follower.set(user.getIsFollower());
		this.subscriber.set(user.getIsSubscriber());
		this.timeOnline.set(user.getTimeOnline());
		this.linesWritten.set(user.getLinesWritten());
		this.trusted.set(user.getIsTrusted());
	}

	// ***********************************************************
	// PUBLIC
	// ***********************************************************
	public synchronized String getStatus() {
		return status.get();
	}

	public synchronized void setStatus(UserStatus status) {
		this.user.setStatus( status.toString() );
		this.status.setValue( status.toString() );
	}
	
	public synchronized Boolean isTrusted() {
		return trusted.getValue();
	}
	
	public synchronized void setTrusted(Boolean trusted){
		this.user.setIsTrusted(trusted);
		this.trusted.setValue(trusted);
	}

	public synchronized Boolean isFollower() {
		return follower.getValue();
	}
	
	public synchronized void setFollower(Boolean follower) {
		this.user.setIsFollower(follower);
		this.follower.setValue(follower);
	}

	public synchronized Boolean isSubscriber() {
		return subscriber.getValue();
	}
	
	public synchronized void setSubscriber(Boolean subscriber){
		this.user.setIsSubscriber(subscriber);
		this.subscriber.setValue(subscriber);
	}

	public synchronized Integer getTimeOnline() {
		return timeOnline.getValue();
	}

	public String getTimeOnlineFormated() {
		int t = getTimeOnline();
		int h = t / 60;
		int m = t % 60;

		String out = "";
		out += (h > 0 ? h + "h " : "");
		out += m + "m";
		return out;
	}

	public synchronized void addTimeOnline(int min) {
		int newTime = timeOnline.get() + min;

		this.timeOnline.set(newTime);
		this.user.setTimeOnline(newTime);
	}

	public synchronized void setTimeOnline(Integer newMin) {
		this.timeOnline.set(newMin);
		this.user.setTimeOnline(newMin);

	}

	public synchronized Integer getLinesWritten() {
		return linesWritten.getValue();
	}

	public synchronized void addLinesWritten(Integer amount) {
		int newLinesWritten = this.linesWritten.get() + amount;

		this.linesWritten.set(newLinesWritten);
		this.user.setLinesWritten(newLinesWritten);

	}

	public synchronized void setLinesWritten(Integer newAmount) {
		this.linesWritten.set(newAmount);
		this.user.setLinesWritten(newAmount);

	}

	public synchronized String getUserName() {
		return userName.get();
	}
	
	synchronized void updateUnderlyingDatabaseObject(){
		this.user = this.user.update();
	}

	@Override
	public synchronized String toString() {
		String out = getUserName();
		
		if( getStatus().matches( UserStatus.ADMIN.toString() ) )
			out += " (ADMIN)";
		else if( getStatus().matches( UserStatus.MODERATOR.toString() ))
			out += " (MOD)";

		if (isSubscriber())
			out += " [Subscriber]";
		else if (isFollower())
			out += " [Follower]";
		else
			out += " [Guest]";
		
		if( isTrusted() )
			out += " {Trusted}";
		
		out += " Time online: " + getTimeOnlineFormated();
		out += " Lines written: " + getLinesWritten();
		return out;
	}

	// ***********************************************************
	// JAVA FX REQUIREMENTS
	// ***********************************************************
	public StringProperty userNameProperty() {
		return userName;
	}

	public StringProperty statusProperty() {
		return status;
	}

	public IntegerProperty timeOnlineProperty() {
		return timeOnline;
	}

	public IntegerProperty linesWrittenProperty() {
		return linesWritten;
	}

	public BooleanProperty followerProperty() {
		return follower;
	}

	public BooleanProperty subscriberProperty() {
		return subscriber;
	}
}
