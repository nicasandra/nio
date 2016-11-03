package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server {

    public static final int PORT = 8090;
    public static final String HOST = "localhost";
    Map<String, String> map = new HashMap<String, String>();

    private Selector selector;
    private Map<SocketChannel, List> dataMapper;
    private InetSocketAddress listenAddress;

    public static void main(String[] args) throws Exception {
        new Server(HOST, PORT).startServer();
    }

    public Server(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
        dataMapper = new HashMap<SocketChannel, List>();
    }

    /**
     * Create server channel
     *
     * @throws IOException
     */
    private void startServer() throws IOException {

        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started.");

        while (true) {

            // wait for events
            selector.select();

            // work on selected keys
            Iterator keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();

                // this is necessary to prevent the same key from coming up again the next time around
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    /**
     * Accept a connection made to this channel's socket
     *
     * @param key
     * @throws IOException
     */

    private void accept(SelectionKey key) throws IOException {

        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteSocketAddress);

        // register channel with selector for further IO
        dataMapper.put(channel, new ArrayList());
        channel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Read from the socket channel
     *
     * @param key
     * @throws IOException
     */

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            dataMapper.remove(channel);
            Socket socket = channel.socket();
            SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteSocketAddress);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        int current = 0;
        if (map.containsKey(key.toString())) {
            current++;
        }
        if (current == 0) {
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            if (!(map.containsValue(new String(data)))) {
                map.put(key.toString(), new String(data));
            } else {
                channel.write(ByteBuffer.wrap("Already taken!".getBytes()));
            }
        } else {
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            System.out.println(map.get(key.toString()) + ": " + new String(data));
        }
    }
}