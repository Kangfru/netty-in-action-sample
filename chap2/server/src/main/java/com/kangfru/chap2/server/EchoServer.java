package com.kangfru.chap2.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * EchoServerHandler.class 에선 비즈니스 로직을 구현한다.
 * main() 메서드에서 서버를 부트스트랩한다.
 * -- 부트스트랩의 과정
 * 서버를 부트스트랩하고 바인딩하는데 이용할 ServerBootstrap 인스턴스를 생성
 * 새로운 연결 수락 및 데이터 읽기/쓰기와 같은 이벤트 처리를 수행할 NioEventLoopGroup 그룹을 생성하고 할당
 * 서버가 바인딩하는 로컬 InetSocketAddress 지정
 * EchoServerHandler 인스턴스를 이용해 새로운 각 Channel을 초기화.
 * ServerBootstrap.bind() 를 호출해 서버에 바인딩한다.
 */
public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
        }
        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    public void start() throws Exception {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // ServerBootstrap
            // 1. 서버가 수신할 포트를 바인딩하고 들어오는 연결 요청 수락.
            // 2. EchoServerHandler 인스턴스에 인바운드 메시지에 대해 알리도록 Channel 구성.
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(serverHandler);
                        }
                    });
            // server를 비동기로 바인딩 ->sync()는 바인딩이 완료되기를 대기.
            ChannelFuture f = b.bind().sync();
            // 채널의 closeFuture를 얻고 완료될 때까지 현재 스레드를 블로킹함.
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
