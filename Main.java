import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Main {
  private static final Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    try {
      ServerSocket serverSocket = new ServerSocket(4221);
      serverSocket.setReuseAddress(true);

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          logger.info("Shutting down server...");
          serverSocket.close();
        } catch (IOException e) {
          logger.severe("Error closing server: " + e.getMessage());
        }
      }));

      while (true) {
        Socket clientSocket = serverSocket.accept();
        logger.info("Accepted new connection");

        // Read the request
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String requestLine = reader.readLine(); // Read the first line
        logger.info("Request: " + requestLine);

        if (requestLine != null) {
          String[] parts = requestLine.split(" ");
          if (parts.length >= 2) {
            String path = parts[1]; // Extract the path

            // Define valid paths
            if (path.equals("/") || path.equals("/hello")) {
              sendResponse(clientSocket, "HTTP/1.1 200 OK", "text/plain", "Welcome to my server!");
            } else {
              File file = new File("public" + path);
              if (file.exists() && !file.isDirectory()) {
                sendResponse(clientSocket, "HTTP/1.1 200 OK", "text/html", readFile(file.getPath()));
              } else {
                sendResponse(clientSocket, "HTTP/1.1 404 Not Found", "text/plain", "404 Not Found");
              }
            }
          }
        }
        clientSocket.close();
      }
    } catch (IOException e) {
      logger.severe("IOException: " + e.getMessage());
    }
  }

  private static void sendResponse(Socket clientSocket, String status, String contentType, String body) throws IOException {
    String response = status + "\r\n" +
                      "Content-Type: " + contentType + "\r\n" +
                      "Content-Length: " + body.length() + "\r\n" +
                      "\r\n" + body;

    clientSocket.getOutputStream().write(response.getBytes());
    logger.info("Response sent: " + status);
  }

  private static String readFile(String filePath) throws IOException {
    return new String(Files.readAllBytes(Path.of(filePath)));
  }
}
