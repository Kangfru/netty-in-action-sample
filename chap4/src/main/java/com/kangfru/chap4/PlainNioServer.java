package com.kangfru.chap4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class PlainNioServer {

    public void serve(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket serverSocket = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        serverSocket.bind(address);
        Selector selector = Selector.open(); // 채널을 처리할 셀렉터를 염
        serverChannel.register(selector, SelectionKey.OP_ACCEPT); // 연결을 수락할 ServerSocket을 셀렉터에 등록
        final ByteBuffer msg = ByteBuffer.wrap("HI!\r\n".getBytes(StandardCharsets.UTF_8));
        for (;;) {
            try {
                selector.select(); // 처리할 이벤트를 기다리며 다음 들어오는 이벤트까지 블로킹
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            Set<SelectionKey> readKeys = selector.selectedKeys(); // 이벤트를 수신한 모든 selectionKey 인스턴스를 얻음
            Iterator<SelectionKey> iterator = readKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) { // 이벤트가 수락할 수 있는 새로운 연결이 있는 지 확인.
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);

                        // 클라이언트를 수락하고 셀렉터에 등록
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());
                        System.out.println("Accepted connection from " + client);
                    }
                    if (key.isWritable()) { // 소켓에 데이터를 기록할 수 있는지 확인
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) { // 연결된 클라이언트로 데이터 출력
                                break;
                            }
                        }
                        client.close();
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ignore) {
                        // 무시
                    }
                }
            }
        }
    }

}
