package main;

import io.netty.channel.Channel;

public class Sdp {
    String username;
    Object offer;
    Object candidate;
    Channel channel;

    public Sdp(String username, Channel channel){
        this.username = username;
        this.channel = channel;
    }


    public void setUsername(String username){
        this.username = username;
    }

    public void setOffer(Object offer){
        this.offer = offer;
    }

    public void setCandidate(Object candidate){
        this.candidate = candidate;
    }
    public void setChannel(Channel channel){
        this.channel = channel;
    }


}
