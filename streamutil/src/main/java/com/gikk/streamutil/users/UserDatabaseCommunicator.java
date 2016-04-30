package com.gikk.streamutil.users;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.gikk.gikk_stream_util.GikkStreamUtilApplication;
import com.gikk.gikk_stream_util.db0.gikk_stream_util.users.Users;
import com.speedment.Speedment;
import com.speedment.exception.SpeedmentException;
import com.speedment.internal.util.MetadataUtil;
import com.speedment.manager.Manager;

/**This class handles all communication with the underlying database (I'm not sure if this is the correct pattern
 * for using Speedment, but due to old habbits I prefere to wrap access to the DB). <br>
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
	private final Random rng = new Random();
	
	// ***********************************************************
	// 				STATIC
	// ***********************************************************	
	/**Tests that our current MySQL database is compatible with the Speedment
	 * code we have.
	 * 
	 * @return {@code true} if the connection is compatible and we manage to write to it
	 * @throws IOException Thrown if the connection is not compatible
	 */
	public static boolean checkConnection() throws IOException{
		Speedment speedment = new GikkStreamUtilApplication().build();
        Manager<Users>userDatabase = speedment.managerOf(Users.class); 
        
		boolean init = userDatabase.isInitialized();
		boolean load = userDatabase.isLoaded();
		boolean strt = userDatabase.isStarted();
		
		//This method adds and then removes a user from the database,
		//to make sure everything works as intended.
		StringBuilder builder = new StringBuilder();
		
		try {
			userDatabase.newEmptyEntity()
				.setUsername("TEMPORARY_TEST_USER_FOR_TESTING")
				.setIsFollower(false)
				.setIsSubscriber(false)
				.setIsTrusted(false)
				.setLinesWritten(0)
				.setTimeOnline(0)
				.setStatus( UserStatus.REGULAR.toString() )
				.persist(  MetadataUtil.toText( builder::append ) )
				.remove( MetadataUtil.toText( builder::append )  );
		} catch (Exception e){
			//We find the 'throwable = <REASON>' part of the message and put that as Throw message,
			//then we throw the entire error
			String message = builder.toString();		
			int idx = message.indexOf("throwable = ") + "throwable = ".length();		
			throw new IOException("Database error: " + message.substring(idx), new Throwable(message));
		}
		
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
	
	/**Tries to fetch the user {@code userName} from the database. If no such user exists, a new entry will be 
	 * created.
	 * 
	 * @param userName Nick of the user
	 * @return {@code ObservableUser} representing user {@code userName}. This might return {@code null} if a user could
	 * not be created
	 */
	public synchronized ObservableUser getOrCreate(String userName){
		return retreiveUser(userName);
	}
	
	/**Tries to fetch the user {@code userName} from the database. If no such user exists, this method returns {@code null}.
	 * If you want to create a user is no such user exists, try the {@link #getOrCreate(String) getOrCreate} method.
	 * 
	 * @param userName Nick of the user
	 * @return {@code ObservableUser} representing user {@code userName}, or {@code null} if no such user exists in the database
	 */
	public synchronized ObservableUser getUser(String userName){
		if( isUserKnown(userName) )
			return retreiveUser(userName);
		return null;
	}
	
	/**Retrieves the first X users whom match the argument {@code comparator}.<br><br>
	 * 
	 * <b>Example:</b> {@code GetUsersWhere( Users.LINES_WRITTEN.compratator.reverse(), 5}<br>
	 * Will return a list of the 5 users with he most lines written
	 * 
	 * @param comparator The comparator to user. The comparator is insertend into a {@code stream().sorted()} statement.
	 * @param amount The maximum of users to retrieve. This value have to be 0 or higher.
	 * @return An ordered list of {@code ObservableUsers}, that matches the {@code comparator}
	 */
	public synchronized List<ObservableUser> getUsersWhere(Comparator<Users> comparator, int amount){	
		flushToDatabase();	//We have to flush cashed data before we can stream over the database
		
		List<ObservableUser> users = new LinkedList<>();
		userDatabase.stream()
					.sorted( comparator )
					.limit(amount)
					.forEach( p ->  {
						users.add( retreiveUser( p.getUsername() ) );
					} );
		return users;
	}
	
	/**Fetches a random user whom's  matches the given value.<br><br>
	 * 
	 * <b>Example:</b> {@code getRandomUserWhere( Users.TIME_ONLINE.eqals(0)}<br>
	 * Will fetch a random user whom's TIME_ONLINE field equals to 0
	 *  
	 * @param predicate The predicate to use
	 * @return A random user that matches the comparator, or {@code null} if there is none
	 */
	public synchronized ObservableUser getRandomUserWhere( Predicate<Users> predicate) {
		flushToDatabase(); //We have to flush cashed data before we can stream over the database

		/* We want to fetch a random user matching the comparator. However, my knowledge of streams are
		 * limited, so I use this work around to get both the .count() method and .findAny()
		 */
		Stream<Users> stream = userDatabase.stream().filter( predicate );		
		long count = stream.count();

		if( count > 0 ) {
			//Just calling .findAny() has a tendency to get the same user repeated times.
			//Therefore, we skip a random number of elements in the stream. This gives a more random result
			Optional<Users> opt = stream.skip( rng.nextInt( (int) stream.count() ) ).findAny();
			if( opt.isPresent() ){
				return retreiveUser( opt.get().getUsername() );
			}
		}
		return null;
	}
	
	public synchronized void onProgramExit(){
		flushToDatabase();
	}
	
	//*********************************************************************************
	//			DATABASE ACCESSORS
	//*********************************************************************************
	
	/**Fetches a User from the database, and wraps it in an ObservableUser. If no such user exists,
	 * this method will create a new entry in the database, and return an ObservableUser wrapping the
	 * newly created user.
	 * 
	 * @param userName Name of the user
	 * @return An {@code ObservableUser} wrapping the database entry for {@code userName}
	 */
	private ObservableUser retreiveUser (String userName){
		String name = userName.toLowerCase();
		
		/* 
		 * Check if the user is cached.
		 * If not, check if we can fetch it from the database (and cache it)
		 * If not, create a new user (and cache it).
		 * If we cannot, return null
		 */
		ObservableUser oUser = userCache.get(name);
		if( oUser == null ){
			oUser = fetchUserFromDatabase(name);
			if( oUser == null ){
				oUser = createNewUserInDatabase(name);	
				if( oUser == null ) //Error when trying to create the new user
					return null;
			} 
			userCache.put(name, oUser);	
		}
		return oUser;
	}
	
	private boolean isUserKnown(String userName) {
		String name = userName.toLowerCase();
		
		//No need to flush data here, since we only care about their
		//user name, and that is persisted on creation
		boolean count = userDatabase.stream()
				.filter( Users.USERNAME.equal(name) )
				.count() > 0;
				
		return count;
	}
	
	/**Creates a new User in the underlying UserDatabase, and creates a ObservableUser wrapping the user.
	 * 
	 * @param userName The user name of the new User. All other fields are default initiated.
	 * @return An ObservableUser, wrapping the underlying User
	 */
	private ObservableUser createNewUserInDatabase(String userName){
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
	private ObservableUser fetchUserFromDatabase(String userName){
		String name = userName.toLowerCase();
		Optional<Users> opt = userDatabase.stream().parallel()
						.filter( Users.USERNAME.equal(name) )
						.findFirst();
		
		if( opt.isPresent() )
			return new ObservableUser( opt.get() );
		else
			return null;
	}
	
	/**
	 * Flushes all user data to the underlying database
	 */
	private void flushToDatabase(){
		userCache.values().stream().forEach( 
				p -> p.updateUnderlyingDatabaseObject()
		);
	}
}
