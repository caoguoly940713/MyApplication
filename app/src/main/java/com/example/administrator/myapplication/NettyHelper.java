package com.example.administrator.myapplication;

import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyHelper {

    /**
     * 添加自定义handler,生成自定义Bootstrap的工厂方法
     * 每个窗口只调用一次防止频繁new
     *
     * @param channelHandlers
     * @return Bootstrap
     */
    public static Bootstrap makeBootstrap(final List<ChannelHandler> channelHandlers) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        if (channelHandlers != null) {
                            for (ChannelHandler channelHandler : channelHandlers) {
                                ch.pipeline().addLast(channelHandler);
                            }
                        }
                    }
                });
        return bootstrap;
    }

    /**
     * 绑定连接的参数
     *
     * @param bootstrap
     * @return ChannelFuture
     */
    public static ChannelFuture connect(Bootstrap bootstrap) {
        return bootstrap.connect(Config.ADDRESS, Config.PORT);
    }

    /**
     * 封装上面两个方法
     *
     * @param channelHandlers
     * @return ChannelFuture
     */
    public static ChannelFuture makeConnect(final List<ChannelHandler> channelHandlers) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                            for (ChannelHandler channelHandler : channelHandlers) {
                                ch.pipeline().addLast(channelHandler);
                            }
                    }
                });

        return bootstrap.connect(Config.ADDRESS, Config.PORT);
    }
}
