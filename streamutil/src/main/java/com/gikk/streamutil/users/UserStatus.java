package com.gikk.streamutil.users;

public enum UserStatus {
	
	ADMIN("admin"), 
	MODERATOR("moderator"), 
	REGULAR("regular");
	
	private String name;
	
	private UserStatus(String val){
		this.name = val;
	}
	
	@Override
	public String toString() {
		return name;
	}	
	
	public static UserStatus toUserStatus(String status){
		String val = status.toLowerCase();
		for( UserStatus e : UserStatus.values() )
			if( val.matches(e.name) ) return e;
		return null;
	}
}
