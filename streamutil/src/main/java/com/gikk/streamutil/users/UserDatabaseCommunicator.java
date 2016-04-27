package com.gikk.streamutil.users;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.gikk.gikk_stream_util.GikkStreamUtilApplication;
import com.gikk.gikk_stream_util.db0.gikk_stream_util.users.Users;
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
	private final Manager<Users> userDatabase;
	
	// ***********************************************************
	// 				STATIC
	// ***********************************************************	
	public static boolean checkConnection() throws Exception{
		Speedment speedment = new GikkStreamUtilApplication().build();
        Manager<Users> userDatabase = speedment.managerOf(Users.class);  
		boolean init = userDatabase.isInitialized();
		boolean load = userDatabase.isLoaded();
		boolean strt = userDatabase.isStarted();
		
		System.out.println("UserDatabase: " + init + " " + load + " " + strt);
		return init&&load&&strt;
	}
	
	// ***********************************************************
	// 				CONSTRUCTOR
	// ***********************************************************	
	public UserDatabaseCommunicator(){
		Speedment speedment = new GikkStreamUtilApplication().build();
        userDatabase = speedment.managerOf(Users.class);  
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

	/**Fetch the status of the user.
	 * 
	 * @param userName
	 * @return This users {@link UserStatus}, or <code>null</code> if the user is not known
	 */
	public synchronized UserStatus checkUserStatus(String userName){		
		if( isUserKnown(userName) )
			return UserStatus.toUserStatus( retreiveUser(userName).getStatus() );
		
		return null;
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
					.filter( Users.USERNAME.equal(name) )
					.count() > 0;
	}
	
	/**Removes all Users from the UserDatabase
	 * 
	 */
	private synchronized void clearUserDatabase() {
		Set<Users> users = userDatabase.stream().parallel().collect( Collectors.toSet() );
		for( Users u : users )
			userDatabase.remove(u);
	}
	
	/**Fetches all users of a certain UserStatus from the underlying database
	 * 
	 * @param status
	 * @return List of all users with the given UserStatus. The list might be empty
	 */
	@SuppressWarnings("unused")
	private synchronized List<Users> fetchUsersOfStatus(UserStatus status){
		String statusName = status.toString();
		return userDatabase.stream()
							.filter( Users.STATUS.equal(statusName) )
							.collect( Collectors.toList() );
	}
	
	/**Creates a new User in the underlying UserDatabase, and creates a ObservableUser wrapping the user.
	 * 
	 * @param userName The user name of the new User. All other fields are default initiated.
	 * @return An ObservableUser, wrapping the underlying User
	 */
	private synchronized ObservableUser createNewUserInDatabase(String userName){
		String name = userName.toLowerCase();
		Users user = null;
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
		Optional<Users> opt = userDatabase.stream().parallel()
						.filter( Users.USERNAME.equal(name) )
						.findFirst();
		
		if( opt.isPresent() )
			return new ObservableUser( opt.get() );
		else
			return null;
	}
}
