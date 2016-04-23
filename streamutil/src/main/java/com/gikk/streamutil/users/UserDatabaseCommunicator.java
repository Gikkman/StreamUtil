package com.gikk.streamutil.users;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.gikk.gikk_stream_util.GikkStreamUtilApplication;
import com.gikk.gikk_stream_util.db0.gikk_stream_util.user.User;
import com.speedment.Speedment;
import com.speedment.exception.SpeedmentException;
import com.speedment.manager.Manager;

import javafx.application.Platform;

/**This class handles all communication with the underlying database (I'm not sure if this is the correct pattern
 * for using Speedment, but due to old habbits I prefere to wrap access to the DB). <br>
 * It also keeps tracks of all users that are currently online in the Twitch channel, by listening for JOIN/PART
 * messages from the IrcConnection.
 * 
 * 
 * @author Simon
 *
 */
public class UserDatabaseCommunicator {
	// ***********************************************************
	// 				VARIABLES
	// ***********************************************************	
	private final HashMap<String, ObservableUser> userCache = new HashMap<>(); 
	private final Manager<User> userDatabase;
	
	// ***********************************************************
	// 				CONSTRUCTOR
	// ***********************************************************	
	public UserDatabaseCommunicator(){
		Speedment speedment = new GikkStreamUtilApplication().build();
        userDatabase = speedment.managerOf(User.class);  
	}
	
	// ***********************************************************
	// 				PUBLIC
	// ***********************************************************	
	
	/**Fetch a list of users and sets their status
	 * 
	 * @param users Users whose status should be changed
	 * @param status The new status they should have
	 */
	public synchronized void updateStatus(UserStatus status, String ...users) {
		if( users.length <= 0)
			return;
		
		for(String user : users)
			retreiveUser(user).setStatus(status);	
	}
	
	public synchronized void incrementWrittenRows(String userName, int amount){
		ObservableUser user = retreiveUser(userName);
		Platform.runLater(() -> {
			user.addLinesWritten(amount);
		} );
	}

	/**Check the status of the user.
	 * 
	 * @param userName
	 * @return
	 */
	public synchronized String checkUserStatus(String userName){
		String out = "User " + userName + " unknown. Please check your spelling";
		
		if( isUserKnown(userName) )
			out = retreiveUser(userName).getStatus();
		
		return out;
	}
	
	/**Fetches all information about a certain user
	 * 
	 * @param userName The user we're looking for
	 * @return A string with all info about the user.
	 */
	public synchronized String getUserStatistics(String userName) {
		String out = "User " + userName + " unknown. Please check your spelling";
		
		if( isUserKnown(userName) )
			out = retreiveUser(userName).toString();
		
		return out;
	}

	public synchronized void resetDatabase() {
		userCache.clear();
		clearUserDatabase();
	}
	
	public synchronized void flushToDatabase(){
		flushUsers();
	}
	
	public synchronized void onProgramExit(){
		flushToDatabase();
	}

	// ***********************************************************
	// 				PACKAGE
	// ***********************************************************	
	
	/**Either fetches the ObservebleUser for this user name from the cache, or creates
	 * a new User in the underlying database and returns a ObservableUser wrapping the 
	 * new User
	 * 
	 * @param userName
	 * @return
	 */
	synchronized ObservableUser retreiveUser (String userName){
		String name = userName.toLowerCase();
		
		ObservableUser oUser = userCache.get(name);
		if( oUser == null ){
			oUser = fetchUserFromDatabase(name);
			if( oUser == null ){
				oUser = createNewUserInDatabase(name);			
			}
			userCache.put(name, oUser);	
		}
		return oUser;
	}
	
	//*********************************************************************************
	//			DATABASE ACCESSORS
	//*********************************************************************************
	private synchronized void flushUsers(){
		userCache.values().stream().forEach( 
				p -> p.updateUnderlyingDatabaseObject()
		);
	}
	
	private synchronized boolean isUserKnown(String userName) {
		String name = userName.toLowerCase();
		
		return userDatabase.stream().parallel()
					.filter( User.USERNAME.equal(name) )
					.count() > 0;
	}
	
	/**Removes all Users from the UserDatabase
	 * 
	 */
	private synchronized void clearUserDatabase() {
		Set<User> users = userDatabase.stream().parallel().collect( Collectors.toSet() );
		for( User u : users )
			userDatabase.remove(u);
	}
	
	/**Fetches all users of a certain UserStatus from the underlying database
	 * 
	 * @param status
	 * @return List of all users with the given UserStatus. The list might be empty
	 */
	private synchronized List<User> fetchUsersOfStatus(UserStatus status){
		String statusName = status.toString();
		return userDatabase.stream()
							.filter( User.STATUS.equal(statusName) )
							.collect( Collectors.toList() );
	}
	
	/**Creates a new User in the underlying UserDatabase, and creates a ObservableUser wrapping the user.
	 * 
	 * @param userName The user name of the new User. All other fields are default initiated.
	 * @return An ObservableUser, wrapping the underlying User
	 */
	private synchronized ObservableUser createNewUserInDatabase(String userName){
		String name = userName.toLowerCase();
		User user = null;
		try {
            user = userDatabase.newEmptyEntity()
                .setUsername(name)
                .setStatus( UserStatus.REGULAR.toString() ) //Really messy way to write "Regular"
                .setIsFollower(false)
                .setIsSubscriber(false)
                .setIsTrusted(false)
                .setLinesWritten(0)
                .setTimeOnline(0)
                .persist();
                
            System.out.println("***Added nr. " + user.getId() +", "
            		+ "Name: " 		  	+ user.getUsername() + ", "
            		+ "Status: "		+ user.getStatus() +", "
            		+ "Time online: " 	+ user.getTimeOnline() +", "
            		+ "Lines written: " + user.getLinesWritten() +", "
            		+ "Is trusted: " 	+ user.getIsTrusted() +", "
            		+ "Is follower: " 	+ user.getIsFollower() +", "
            		+ "Is subscriber: " + user.getIsSubscriber() );
        } catch (SpeedmentException se) {
           System.out.println("***Could not create new user: " + userName);
           System.err.println( se.getLocalizedMessage() );
           return null;
        }
		
		return new ObservableUser(user);
	}
	
	/**This method fetches all data about a certain user from the database and parses it to a ObservableUser object
	 * 
	 * @param userName The user name you want to look up
	 * @return A ObservableUser-object, representing that user, on NULL, if the user doesn't exist
	 */
	private synchronized ObservableUser fetchUserFromDatabase(String userName){
		String name = userName.toLowerCase();
		Optional<User> opt = userDatabase.stream().parallel()
						.filter( User.USERNAME.equal(name) )
						.findFirst();
		
		if( opt.isPresent() )
			return new ObservableUser( opt.get() );
		else
			return null;
	}
}
