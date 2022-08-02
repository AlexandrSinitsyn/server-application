package db;

import db.utils.Tools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public class Server {
    private final ExecutorService processor;
    private final ExecutorService receivers;
    private final ExecutorService service;
    public final int port;

    public Server(final int port, final int threadsIn, final int threadsOut) {
        processor = Executors.newSingleThreadExecutor();
        receivers = Executors.newFixedThreadPool(threadsIn);
        service = Executors.newFixedThreadPool(threadsOut);

        this.port = port;
    }

    public static void main(String... args) {
        final Server server = parseArgs(args);

        server.start();
    }

    private static IllegalArgumentException error(final String fail) {
        final String instructionToUse = "Arguments to be optionally passed:\n<port> <threads-in> <threads-out>";

        return new IllegalArgumentException(fail + "\n" + instructionToUse);
    }

    private static Server parseArgs(final String[] args) {
        if (args == null || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw error("Null arguments");
        } else if (args.length > 3) {
            throw error("Too many arguments passed");
        }

        final BiFunction<Integer, Integer, Integer> parse = (index, dflt) -> {
            try {
                return args.length > index ? Integer.parseInt(args[index]) : dflt;
            } catch (final NumberFormatException e) {
                throw error(e.getMessage());
            }
        };

        final int port = parse.apply(0, Tools.DEFAULT_PORT);
        final int threadsIn = parse.apply(1, Tools.DEFAULT_THREADS_IN);
        final int threadsOut = parse.apply(1, Tools.DEFAULT_THREADS_OUT);

        return new Server(port, threadsIn, threadsOut);
    }

    private void start() {
        Tools.log("Server started...");

        final var address = new InetSocketAddress(port);

        try (final var selector = Selector.open();
             final var channel = DatagramChannel.open()) {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            channel.bind(address);

            Tools.log("Listener is ready");

            final var cd = new CountDownLatch(1);

            processor.submit(() -> {
                while (!Thread.interrupted() && channel.isOpen()) {
                    try {
                        selector.select(key -> {
                            try {
                                if (key.isValid() && key.isReadable()) {
                                    final ByteBuffer buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
                                    final SocketAddress addr = channel.receive(buffer);
                                    buffer.flip();

                                    receivers.submit(() -> processRequest(buffer, channel, addr));
                                }
                            } catch (final IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                cd.countDown();
            });

            cd.await();
        } catch (final IOException e) {
            throw new ServerException("Can not get access to client. Server failed", e);
        } catch (final RuntimeException | InterruptedException e) {
            Tools.log(e.getMessage());
            throw new ServerException("Channel connection failed", e.getCause());
        } finally {
            processor.shutdownNow();
            receivers.shutdownNow();
            service.shutdownNow();
        }
    }

    private void processRequest(final ByteBuffer buffer, final DatagramChannel channel, final SocketAddress address) {
        final String request = StandardCharsets.UTF_8.decode(buffer).toString();

        Tools.log("Request arrived: %s", request);

        service.submit(() -> sendResponse(request, buffer, channel, address));
    }

    public void sendResponse(final String request, final ByteBuffer buffer,
                              final DatagramChannel channel, final SocketAddress address) {
        final String response = RequestParser.parse(request);

        if (response == null) {
            return;
        }

        buffer.clear();
        buffer.put(response.getBytes(StandardCharsets.UTF_8));
        buffer.limit(response.length());
        buffer.rewind();

        try {
            Tools.log("Response send: %s", response);
            channel.send(buffer, address);
        } catch (final IOException e) {
            throw new ServerException("I/O error occurred while responding", e);
        }
    }
}
