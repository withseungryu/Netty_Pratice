package main.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.io.*;

public class ServerClient {
    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        try{
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception{
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());
                            p.addLast(new ChannelClientHandler());
                        }

                    });

            Channel channel = b.connect("localhost", 8888).sync().channel();
//            ChannelFuture f = b.connect("localhost", 8888).sync();
//            f.channel().closeFuture().sync();
            ChannelFuture f = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for(;;){
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                // Sends the received line to the server.
                f = channel.writeAndFlush(line + "\r\n");

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equals(line.toLowerCase())) {
                    channel.closeFuture().sync();
                    break;
                }

            }

        }
        finally{
            group.shutdownGracefully();
        }
    }
}
