import db.Server;
import db.utils.Tools;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerTest {

    public static void main(String[] args) {
        final var server = new Server(Tools.DEFAULT_PORT, Tools.DEFAULT_THREADS_IN, Tools.DEFAULT_THREADS_OUT);

        final var request = "user:system:addUser:alex,sin".getBytes(StandardCharsets.UTF_8);
        final var buffer = ByteBuffer.allocate(request.length).put(request);
        buffer.rewind();

        server.sendResponse(StandardCharsets.UTF_8.decode(buffer).toString(), buffer, null, null);
    }
}
