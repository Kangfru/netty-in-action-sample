package com.kangfru.chap4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class NettyNioServer {

    public void server(int port) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", StandardCharsets.UTF_8));
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 연결이 수락될 때마다 호출될 ChannelInitializer
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                // 이벤트를 가르채고 처리할 ChannelInboundHandlerAdapter
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    // 클라이언트로 메시지를 출력하고 Channel Future Listener를 추가해 메시지가 출력되면 연결을 닫음
                                    ctx.writeAndFlush(buf.duplicate())
                                            .addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture f = b.bind().sync();
            // 바인드가 완료될 때 까지 이 다음을 실행하지 않음.
            f.channel().closeFuture().sync();
            // close 가 되기 전까지 이 다음 코드들은 실행이 안되게 막힌다.
        } finally {
            group.shutdownGracefully().sync();
        }

    }

}
//ChannelFuture bindFuture = boot.bind(8888);
//// 8888번 포트로 바인드하는 "비동기 bind 메서드"를 호출하였다.
//// bind 메서드는 바인딩이 완료되기 전에 ChannelFuture 를 반환한다.
//
//bindFuture.sync();
//// ChannelFuture 인터페이스의 sync() 메소드는 주어진 ChannelFuture 객체의 작업이 완료될 때까지
//// "블로킹"하는 메서드다. 즉 bind 메서드의 처리가 완전히 끝나기 전까지 이 다음은 실행되지 않는다.
//
//Channel serverSocketChannel = bindFuture.channel();
//// 8888번 포트에 바인딩된 서버 소켓 채널을 가져온다.
//
//ChannelFuture closeFuture = serverSocketChannel.closeFuture();
//// 바인드가 완료된 서버 소켓 채널의 CloseFuture 객체를 가져오고,
//// 이 객체는 네티 내부에서 채널이 생성될 때 같이 생성된다.
//// 하나의 채널을 통해서 closeFuture() 메소드를 여러번 호출해도 항상 동일한 CloseFuture 객체를 반환 받는다는 것이다.
//
//closeFuture.sync();
//// CloseFuture 의 완료시점은 바로 채널이 close 가 완료된 시점이다.
//// 그리고 앞에서 말했지만, sync() 는 ChannelFuture 객체의 작업이 완료될 때까지 "블로킹"하는 메서드다.
//// 그러므로 close 가 되기 전까지 이 다음 코드들은 실행이 안되게 막힌다.