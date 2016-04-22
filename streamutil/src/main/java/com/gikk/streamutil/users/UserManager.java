package com.gikk.streamutil.users;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.gikk.gikk_stream_util.GikkStreamUtilApplication;
import com.gikk.gikk_stream_util.db0.gikk_stream_util.user.User;
import com.speedment.Speedment;
import com.speedment.exception.SpeedmentException;
import com.speedment.manager.Manager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**<b>Singleton</b><br><br>
 * 
 * This class handles all communication with the underlying database (I'm not sure if this is the correct pattern
 * for using Speedment, but due to old habbits I prefere to wrap access to the DB). <br>
 * It also keeps tracks of all users that are currently online in the Twitch channel, by listening for JOIN/PART
 * messages from the IrcConnection.
 * 
 *TODO: Split this into two classes (one for DB communication and one for keeping track of users online).
 * 
 * @author Simon
 *
 */
public class UserManager {
	// ***********************************************************
	// 				VARIABLES
	// ***********************************************************
	private static class HOLDER {	static final UserManager INSTANCE = new UserManager();	 }
	
	private final ObservableList<ObservableUser> usersOnline =  FXCollections.observableArrayList();	
	private final HashMap<String, ObservableUser> userCache = new HashMap<>(); 
	private final Manager<User> userDatabase;
	
	// ***********************************************************
	// 				STATIC
	// ***********************************************************
	public static UserManager GET(){
		return HOLDER.INSTANCE;
	}
	
	// ***********************************************************
	// 				CONSTRUCTOR
	// ***********************************************************	
	private UserManager(){
		Speedment speedment = new GikkStreamUtilApplication().build();
        userDatabase = speedment.managerOf(User.class);  
	}
	
	// ***********************************************************
	// 				PUBLIC
	// ***********************************************************	
	public final ObservableList<ObservableUser> getUserOnlineList(){
		return usersOnline;
	}
	
	public synchronized void incrementWrittenRows(String userName, int amount){
		ObservableUser user = retreiveUser(userName);
		Platform.runLater(() -> {
			user.addLinesWritten(amount);
		} );
	}
	
	public void incrementUsersOnlinetime(int minutes) {
		Platform.runLater(() -> {
			usersOnline.stream().forEach( p -> p.addTimeOnline(minutes) );				
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
	
	public synchronized void joinPresentUsers(String[] presentUsers){
		for(String user : presentUsers)
			joinUser(user);
	}
	
	public void partUser(String user) {
		ObservableUser u = retreiveUser(user );
		Platform.runLater( () -> {
			usersOnline.remove(u);
		});
	}	
	
	public void joinUser(String user) {
		ObservableUser u = retreiveUser(user);
		Platform.runLater( () -> { 
			usersOnline.add(u);
		});		
	}

	public synchronized void resetDatabase() {
		userCache.clear();
		clearUserDatabase();
		Platform.runLater( () -> {
			usersOnline.clear();
		} );
	}
	
	public synchronized void onProgramExit(){
		flushUsers();
	}

	// ***********************************************************
	// 				PRIVATE
	// ***********************************************************	
	
	/**Either fetches the ObservebleUser for this user name from the cache, or creates
	 * a new User in the underlying database and returns a ObservableUser wrapping the 
	 * new User
	 * 
	 * @param userName
	 * @return
	 */
	private ObservableUser retreiveUser (String userName){
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
                .setStatus( ObservableUser.parseStatus( ObservableUser.Status.Regular) ) //Really messy way to write "Regular"
                .setIsFollower(false)
                .setIsSubscriber(false)
                .setIsTrusted(false)
                .setLinesWritten(0)
                .setTimeOnline(0)
                .persist();
                
            System.out.println("Added nr. " + user.getId() +", "
            		+ "Name: " 		  	+ user.getUsername() + ", "
            		+ "Time online: " 	+ user.getTimeOnline() +", "
            		+ "Lines written: " + user.getLinesWritten() +", "
            		+ "Is trusted: " 	+ user.getIsTrusted() +", "
            		+ "Is follower: " 	+ user.getIsFollower() +", "
            		+ "Is subscriber: " + user.getIsSubscriber() );
        } catch (SpeedmentException se) {
           System.out.println("\tCould not create new user: " + userName);
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
