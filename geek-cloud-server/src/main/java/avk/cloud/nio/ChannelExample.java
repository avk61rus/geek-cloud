package avk.cloud.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChannelExample {

    public static void main(String[] args) throws IOException {

        SeekableByteChannel channel = Files.newByteChannel(
                Path.of("server-files", "texts", "sample.txt")
        );

        RandomAccessFile raf = new RandomAccessFile(
                Path.of("server-files", "texts", "sample.txt").toFile(), "rw"
        );

        raf.getChannel().position(10);
        raf.getChannel().write(ByteBuffer.wrap("New message".getBytes(StandardCharsets.UTF_8)));

        ByteBuffer buffer = ByteBuffer.allocate(5);

        while (true) {
            int readCount = channel.read(buffer);
            if (readCount <= 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                System.out.print((char) buffer.get());
            }
            buffer.clear();
        }
    }

}