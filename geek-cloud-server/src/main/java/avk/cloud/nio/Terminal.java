package avk.cloud.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class Terminal {

    // TODO: 19.04.2022 implement commands: cat, cd, mkdir, touch
    private Path dir;
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(256);

    public Terminal() throws IOException {

        dir = Path.of("server-files");

        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8189));
        serverChannel.configureBlocking(false);

        selector = Selector.open();

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port: 8189");

        while (serverChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            try {
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        String message = readMessageFromChannel(channel).trim();
        System.out.println("Received: " + message);
        if (message.equals("ls")) {
            channel.write(ByteBuffer.wrap(
                            getLsResultString().getBytes(StandardCharsets.UTF_8)
                    )
            );
        }

        if (message.equals("mkdir")) {
            Path path = Paths.get("newDir");
            try{
                Path newDir = Files.createDirectory(path);
            }catch (FileAlreadyExistsException e) {
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (message.equals("touch")) {
            Path path = Paths.get("newDir/123.txt");
            try{
                Path touch = Files.createFile(path);
            }catch (FileAlreadyExistsException e) {
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            channel.write(ByteBuffer.wrap("Unknown command\n\r".getBytes(StandardCharsets.UTF_8)));
        }
        channel.write(ByteBuffer.wrap("-> ".getBytes(StandardCharsets.UTF_8)));
    }

    private String getLsResultString() throws IOException {
        return Files.list(dir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.joining("\n\r")) + "\n\r";
    }

    private String readMessageFromChannel(SocketChannel channel) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int readCount = channel.read(buffer);
            if (readCount == -1) {
                channel.close();
                break;
            }
            if (readCount == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                sb.append((char) buffer.get());
            }
            buffer.clear();
        }
        return sb.toString();
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted...");
        channel.write(ByteBuffer.wrap("Welcome in Mike terminal!\n\r-> ".getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws IOException {
        new Terminal();
    }

}