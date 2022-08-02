import db.utils.Tools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BasicClientTest {

    private static final BlockingQueue<String> requests = new ArrayBlockingQueue<>(100);
    private static final ExecutorService service = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        start(Tools.DEFAULT_HOST, Tools.DEFAULT_PORT);

        requests.add("user:system:addUser:alex,sin");

        boolean system = false;

        try (final var scanner = new Scanner(System.in)) {
            System.out.print("@system>");

            while (scanner.hasNextLine()){
                final String[] input = scanner.nextLine().split(" ");

                switch (input[0]) {
                    case "system" -> system = true;
                    case "client" -> system = false;
                    default -> sendRequest(system, input);
                }
                System.out.printf("@%s>", system ? "client" : "system");
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void start(final String host, final int port) {
        final var address = new InetSocketAddress(host, port);

        try (final var selector = Selector.open();
             final var channel = DatagramChannel.open()) {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE);

            final var phaser = new Phaser(2);

            service.submit(() -> {
                phaser.arrive();

                Tools.log("Client started");

                while (!Thread.interrupted()/* && channel.isOpen()*/) {
                    try {
                        selector.select(100);

                        final Set<SelectionKey> arrived = selector.selectedKeys();

                        if (arrived.isEmpty()) {
                            selector.keys().forEach(key -> key.interestOps(SelectionKey.OP_WRITE));
                        }

                        arrived.forEach(key -> {
                            try {
                                if (key.isValid()) {
                                    if (key.isWritable()) {
                                        try {
                                            final String request = requests.take();

                                            channel.send(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), address);
                                            key.interestOps(SelectionKey.OP_READ);
                                        } catch (final InterruptedException | IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else if (key.isReadable()) {
                                        final ByteBuffer buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
                                        channel.receive(buffer);
                                        buffer.flip();
                                        final String message = StandardCharsets.UTF_8.decode(buffer).toString();
                                        key.interestOps(SelectionKey.OP_WRITE);

                                        Tools.log("Response accepted: " + message);
                                    }
                                }
                            } catch (final IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        arrived.clear();
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            phaser.arriveAndAwaitAdvance();
        } catch (final IOException e) {
            throw new RuntimeException("Can not get access to server. Client is not registered", e);
        } catch (final RuntimeException e) {
            Tools.log(e.getMessage());
            throw new RuntimeException("Channel connection failed", e.getCause());
        }
    }

    private static void sendRequest(final boolean system, final String[] input) {
        try {
            requests.put("user:%s:%s:%s".formatted(system ? "system" : 0, input[0], Arrays.stream(input).skip(1).collect(Collectors.joining(","))));
        } catch (final InterruptedException e) {
            System.err.printf("Failed to send request [%s]%n", input[0]);
        }
    }
}
