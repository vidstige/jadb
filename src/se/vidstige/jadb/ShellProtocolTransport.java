package se.vidstige.jadb;

import java.io.*;


class ShellProtocolTransport implements Closeable {
    private final DataOutputStream output;
    private final DataInputStream input;

    ShellProtocolTransport(DataOutputStream outputStream, DataInputStream inputStream) {
        output = outputStream;
        input = inputStream;
    }

    private ShellMessageType readMessageType() throws IOException {
        return ShellMessageType.fromId(input.readByte());
    }

    private long readDataLength() throws IOException {
        return integerToUnsignedLong(Integer.reverseBytes(input.readInt()));
    }

    private void readDataTo(OutputStream out, long dataLength, byte[] buffer) throws IOException {
        long remaining = dataLength;
        while (remaining > 0) {
            int len = (int) Math.min(remaining, buffer.length);
            input.readFully(buffer, 0, len);
            out.write(buffer, 0, len);
            remaining -= len;
        }
        out.flush();
    }

    int demuxOutput(OutputStream outputStream, OutputStream errorStream) throws JadbException, IOException {
        int exitCode = 0;
        byte[] buf = new byte[256 * 1024];

        try {
            while (true) {
                ShellMessageType messageType = readMessageType();
                long length = readDataLength();
                switch (messageType) {
                    case STDOUT:
                        readDataTo(outputStream, length, buf);
                        break;
                    case STDERR:
                        readDataTo(errorStream, length, buf);
                        break;
                    case EXIT:
                        if (length != 1) {
                            throw new JadbException("Expected only one byte for exitCode");
                        }
                        exitCode = byteToUnsignedInt(input.readByte());
                        break;
                    default:
                        // ignore;
                        break;
                }
            }
        } catch (EOFException e) {
            // ignore
        }

        return exitCode;
    }

    private void writeData(ShellMessageType type, byte[] buf, int off, int len) throws IOException {
        output.writeByte(byteToUnsignedInt(type.getId()));
        output.writeInt(Integer.reverseBytes(len));
        output.write(buf, off, len);
    }

    OutputStream getOutputStream() {
        return new ShellProtocolOutputStream(this);
    }

    @Override
    public void close() throws IOException {
        output.close();
        input.close();
    }

    // replace with Integer.toUnsignedLong in Java 8
    private static long integerToUnsignedLong(int i) {
        return ((long) (int) i) & 0xffffffffL;
    }

    // replace with Byte.toUnsignedInt in Java 8
    private static int byteToUnsignedInt(byte b) {
        return ((int) b) & 0xff;
    }

    enum ShellMessageType {
        STDIN((byte) 0),
        STDOUT((byte) 1),
        STDERR((byte) 2),
        EXIT((byte) 3),
        CLOSE_STDIN((byte) 4),
        WINDOW_SIZE_CHANGE((byte) 5),
        UNKNOWN(Byte.MIN_VALUE);

        private final byte id;

        ShellMessageType(byte id) {
            this.id = id;
        }

        public static ShellMessageType fromId(byte b) {
            switch (b) {
                case 0:
                    return STDIN;
                case 1:
                    return STDOUT;
                case 2:
                    return STDERR;
                case 3:
                    return EXIT;
                case 4:
                    return CLOSE_STDIN;
                case 5:
                    // unused
                    return WINDOW_SIZE_CHANGE;
                default:
                    return UNKNOWN;
            }
        }

        public byte getId() {
            return id;
        }
    }

    private static class ShellProtocolOutputStream extends OutputStream {

        private final ShellProtocolTransport transport;

        ShellProtocolOutputStream(ShellProtocolTransport transport) {
            this.transport = transport;
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[]{(byte) b});
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            transport.writeData(ShellMessageType.STDIN, b, off, len);
        }

        @Override
        public void flush() throws IOException {
            transport.output.flush();
        }

        @Override
        public void close() throws IOException {
            transport.writeData(ShellMessageType.CLOSE_STDIN, new byte[0], 0, 0);
        }
    }
}
