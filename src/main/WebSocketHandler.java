package main;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {
    private static final DefaultChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final List<Sdp> users = new ArrayList<>();
    private static final String username = null;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws ParseException {

        System.out.println(channelGroup.size());
        /*
        고쳐야 할 것 : try catch 써서 users의 찾는 것이 null일때를 예외 처리
        지금은 좀 야매로 users.get(0)을 사용했지만 findUser를 사용해서 구현할 것
        msg가 TextWebSocketFrame이 아닐때 무슨 역할을 하는지 파악하자
         */
        if (msg instanceof WebSocketFrame) {
            System.out.println("This is a WebSocket frame");
            System.out.println("Client Channel : " + ctx.channel());
            if (msg instanceof BinaryWebSocketFrame) {
                System.out.println("BinaryWebSocketFrame Received : ");
                System.out.println(((BinaryWebSocketFrame) msg).content());
            } else if (msg instanceof TextWebSocketFrame) {

                Channel incoming = ctx.channel();

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(((TextWebSocketFrame)msg).text());
                JSONObject jsonObj = (JSONObject) obj;
                channelGroup.add(incoming);

                switch(((String)jsonObj.get("type"))){
                    case "store_user":
                        users.add(new Sdp((String)jsonObj.get("username"), incoming));
                        break;
                    case "store_candidate":
                        if(users.get(0).candidate ==null){
                            users.get(0).setCandidate((Object)jsonObj.get("candidate"));
                        }
                        break;
                    case "store_offer":
                        users.get(0).setOffer((Object)jsonObj.get("offer"));
                        break;
                    case "send_answer":
                        JSONObject sendAnswerObject = new JSONObject();
                        sendAnswerObject.put("type", "answer");
                        sendAnswerObject.put("answer", (Object)jsonObj.get("answer"));
                        users.get(0).channel.writeAndFlush(new TextWebSocketFrame(sendAnswerObject.toString()));
                        break;
                    case "send_candidate":
                        JSONObject sendCandidateObject = new JSONObject();
                        sendCandidateObject.put("type", "candidate");
                        sendCandidateObject.put("answer", (Object)jsonObj.get("candidate"));
                        users.get(0).channel.writeAndFlush(new TextWebSocketFrame(sendCandidateObject.toString())); //user.get(0)고쳐보자..
                        break;
                    case "join_call":

                        JSONObject joinCallObject = new JSONObject();
                        joinCallObject.put("type", "offer");
                        joinCallObject.put("offer", users.get(0).offer);
                        incoming.writeAndFlush(new TextWebSocketFrame(joinCallObject.toString()));

                        JSONObject joinCallCandidateObject = new JSONObject();
                        joinCallCandidateObject.put("type", "candidate");
                        joinCallCandidateObject.put("candidate", users.get(0).candidate);
                        incoming.writeAndFlush(new TextWebSocketFrame(joinCallCandidateObject.toString()));


                }
            } else if (msg instanceof PingWebSocketFrame) {
                System.out.println("PingWebSocketFrame Received : ");
                System.out.println(((PingWebSocketFrame) msg).content());
            } else if (msg instanceof PongWebSocketFrame) {
                System.out.println("PongWebSocketFrame Received : ");
                System.out.println(((PongWebSocketFrame) msg).content());
            } else if (msg instanceof CloseWebSocketFrame) {
                System.out.println("CloseWebSocketFrame Received : ");
                System.out.println("ReasonText :" + ((CloseWebSocketFrame) msg).reasonText());
                System.out.println("StatusCode : " + ((CloseWebSocketFrame) msg).statusCode());
            } else {
                System.out.println("Unsupported WebSocketFrame");
            }
        }
    }

    public int findUser(String username){
        int idx = 0;
        for(Sdp sdp : users){
            if(sdp.username.equals(username)){
                break;
            }
            idx++;
        }
        return idx;
    }

}
