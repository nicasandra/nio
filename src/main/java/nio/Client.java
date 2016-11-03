package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        new Client().startClient();
    }

    public void startClient() throws IOException, InterruptedException {

        InetSocketAddress hostAddress = new InetSocketAddress(Server.HOST, Server.PORT);
        SocketChannel client = SocketChannel.open(hostAddress);
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");

        while (true) {
            String message = scanner.nextLine();
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            client.write(buffer);
        }

    }
}