package avk.cloud.nio;

import java.nio.ByteBuffer;

public class BufferExamples {

    public static void main(String[] args) {

        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) 65);
        buffer.put((byte) 66);

        buffer.flip();

        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }

        buffer.rewind();

        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
    }
}
