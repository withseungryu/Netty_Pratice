package main;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;
import java.nio.charset.Charset;

public class ChatServerHandler extends ChannelInboundHandlerAdapter {  //Discard와 다른점 ,,, 상속받은 함수가 다르다.

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

//    @Override
//    public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
//        System.out.println("handlerAdded of [SERVER]");
//        Channel incoming = ctx.channel();
//        for(Channel channel : channelGroup){
//            channel.write("[SERVER] -" + incoming.remoteAddress()+ "has joined\n");
//        }
//        System.out.println(incoming);
//        channelGroup.add(incoming);
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        System.out.println("User Access");

        ctx.writeAndFlush("Welcome to " + InetAddress.getLocalHost().getHostName() + "secure chat service!\n");
        ctx.writeAndFlush("Your session is proted\n");
        channelGroup.add(ctx.channel());



    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception{
        System.out.println("handlerRemoved of [SERVER]");
        Channel incoming = ctx.channel();
        for(Channel channel : channelGroup){
            channel.write("[SERVER] - " + incoming.remoteAddress() + " has left\n");
        }
        channelGroup.remove(incoming);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){


        String message = null;
        message = (String) msg;
//        System.out.println("channelRead of [SERVER]" + message);
        Channel incoming = ctx.channel();
        ctx.writeAndFlush("finish");
        for(Channel channel : channelGroup){
            if(channel != incoming){
                channel.writeAndFlush("[" + incoming.remoteAddress() + "]" + message + "\n");
            }
        }
//        ChannelFuture channelFuture = ctx.writeAndFlush(msg);
//        channelFuture.addListener(ChannelFutureListener.CLOSE);
//        System.out.println("실행??");
//        ctx.flush();
//        ctx.close();

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
