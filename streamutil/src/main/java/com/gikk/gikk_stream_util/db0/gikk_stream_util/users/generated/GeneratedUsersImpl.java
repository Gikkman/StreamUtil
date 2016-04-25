package com.gikk.gikk_stream_util.db0.gikk_stream_util.users.generated;

import com.gikk.gikk_stream_util.db0.gikk_stream_util.users.Users;
import com.gikk.gikk_stream_util.db0.gikk_stream_util.users.UsersImpl;
import com.speedment.Speedment;
import com.speedment.internal.core.code.AbstractBaseEntity;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Generated;

/**
 * The generated base implementation representing an entity (for example, a
 * row) in the Table gikk_stream_util.db0.gikk_stream_util.users.
 * <p>
 * This file has been automatically generated by Speedment. Any changes made
 * to it will be overwritten.
 * 
 * @author Speedment
 */
@Generated("Speedment")
public abstract class GeneratedUsersImpl extends AbstractBaseEntity<Users> implements Users {
    
    private Integer id;
    private String username;
    private String status;
    private Integer timeOnline;
    private Integer linesWritten;
    private Boolean isTrusted;
    private Boolean isFollower;
    private Boolean isSubscriber;
    
    protected GeneratedUsersImpl() {
        
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getStatus() {
        return status;
    }
    
    @Override
    public Integer getTimeOnline() {
        return timeOnline;
    }
    
    @Override
    public Integer getLinesWritten() {
        return linesWritten;
    }
    
    @Override
    public Boolean getIsTrusted() {
        return isTrusted;
    }
    
    @Override
    public Boolean getIsFollower() {
        return isFollower;
    }
    
    @Override
    public Boolean getIsSubscriber() {
        return isSubscriber;
    }
    
    @Override
    public final Users setId(Integer id) {
        this.id = id;
        return this;
    }
    
    @Override
    public final Users setUsername(String username) {
        this.username = username;
        return this;
    }
    
    @Override
    public final Users setStatus(String status) {
        this.status = status;
        return this;
    }
    
    @Override
    public final Users setTimeOnline(Integer timeOnline) {
        this.timeOnline = timeOnline;
        return this;
    }
    
    @Override
    public final Users setLinesWritten(Integer linesWritten) {
        this.linesWritten = linesWritten;
        return this;
    }
    
    @Override
    public final Users setIsTrusted(Boolean isTrusted) {
        this.isTrusted = isTrusted;
        return this;
    }
    
    @Override
    public final Users setIsFollower(Boolean isFollower) {
        this.isFollower = isFollower;
        return this;
    }
    
    @Override
    public final Users setIsSubscriber(Boolean isSubscriber) {
        this.isSubscriber = isSubscriber;
        return this;
    }
    
    @Override
    public Users copy() {
        final Users users = new UsersImpl() {
            @Override
            protected final Speedment speedment() {
                return GeneratedUsersImpl.this.speedment();
            }
        };
        
        setId(users.getId());
        setUsername(users.getUsername());
        setStatus(users.getStatus());
        setTimeOnline(users.getTimeOnline());
        setLinesWritten(users.getLinesWritten());
        setIsTrusted(users.getIsTrusted());
        setIsFollower(users.getIsFollower());
        setIsSubscriber(users.getIsSubscriber());
        
        return users;
    }
    
    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", "{ ", " }");
        sj.add("id = "+Objects.toString(getId()));
        sj.add("username = "+Objects.toString(getUsername()));
        sj.add("status = "+Objects.toString(getStatus()));
        sj.add("timeOnline = "+Objects.toString(getTimeOnline()));
        sj.add("linesWritten = "+Objects.toString(getLinesWritten()));
        sj.add("isTrusted = "+Objects.toString(getIsTrusted()));
        sj.add("isFollower = "+Objects.toString(getIsFollower()));
        sj.add("isSubscriber = "+Objects.toString(getIsSubscriber()));
        return "UsersImpl "+sj.toString();
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) { return true; }
        if (!(that instanceof Users)) { return false; }
        final Users thatUsers = (Users)that;
        if (!Objects.equals(this.getId(), thatUsers.getId())) {return false; }
        if (!Objects.equals(this.getUsername(), thatUsers.getUsername())) {return false; }
        if (!Objects.equals(this.getStatus(), thatUsers.getStatus())) {return false; }
        if (!Objects.equals(this.getTimeOnline(), thatUsers.getTimeOnline())) {return false; }
        if (!Objects.equals(this.getLinesWritten(), thatUsers.getLinesWritten())) {return false; }
        if (!Objects.equals(this.getIsTrusted(), thatUsers.getIsTrusted())) {return false; }
        if (!Objects.equals(this.getIsFollower(), thatUsers.getIsFollower())) {return false; }
        if (!Objects.equals(this.getIsSubscriber(), thatUsers.getIsSubscriber())) {return false; }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(getId());
        hash = 31 * hash + Objects.hashCode(getUsername());
        hash = 31 * hash + Objects.hashCode(getStatus());
        hash = 31 * hash + Objects.hashCode(getTimeOnline());
        hash = 31 * hash + Objects.hashCode(getLinesWritten());
        hash = 31 * hash + Objects.hashCode(getIsTrusted());
        hash = 31 * hash + Objects.hashCode(getIsFollower());
        hash = 31 * hash + Objects.hashCode(getIsSubscriber());
        return hash;
    }
    
    @Override
    public Class<Users> entityClass() {
        return Users.class;
    }
}