 package server.net; // ICERainbow666
  // ICERainbow666
 import java.io.BufferedReader; // ICERainbow666
 import java.io.PrintWriter; // ICERainbow666
 import java.net.Socket; // ICERainbow666
  // ICERainbow666
 /** // ICERainbow666
  * 玩家连接类。 // ICERainbow666
  * <p> // ICERainbow666
  * 封装单个客户端与服务端之间的连接信息,包括玩家ID、名称、Socket连接 // ICERainbow666
  * 以及输入输出流。提供便捷的消息发送方法。 // ICERainbow666
  * </p> // ICERainbow666
  */ // ICERainbow666
 public class PlayerConnection{ // ICERainbow666
     private final int playerId;              // 玩家唯一标识 // ICERainbow666
     private final String name;               // 玩家名称 // ICERainbow666
     private final Socket socket;             // Socket连接 // ICERainbow666
     private final BufferedReader reader;     // 输入流,用于接收客户端消息 // ICERainbow666
     private final PrintWriter writer;        // 输出流,用于向客户端发送消息 // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 创建玩家连接对象。 // ICERainbow666
      * // ICERainbow666
      * @param playerId 玩家唯一标识ID // ICERainbow666
      * @param name     玩家名称 // ICERainbow666
      * @param socket   客户端Socket连接 // ICERainbow666
      * @param reader   缓冲输入流 // ICERainbow666
      * @param writer   打印输出流 // ICERainbow666
      */ // ICERainbow666
     public PlayerConnection(int playerId, String name, Socket socket, // ICERainbow666
                             BufferedReader reader, PrintWriter writer) { // ICERainbow666
         this.playerId = playerId; // ICERainbow666
         this.name = name; // ICERainbow666
         this.socket = socket; // ICERainbow666
         this.reader = reader; // ICERainbow666
         this.writer = writer; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取玩家ID。 // ICERainbow666
      * // ICERainbow666
      * @return 玩家唯一标识ID // ICERainbow666
      */ // ICERainbow666
     public int getPlayerId() { // ICERainbow666
         return playerId; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取玩家名称。 // ICERainbow666
      * // ICERainbow666
      * @return 玩家名称 // ICERainbow666
      */ // ICERainbow666
     public String getName() { // ICERainbow666
         return name; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取Socket连接。 // ICERainbow666
      * // ICERainbow666
      * @return 客户端Socket连接 // ICERainbow666
      */ // ICERainbow666
     public Socket getSocket() { // ICERainbow666
         return socket; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取缓冲输入流。 // ICERainbow666
      * // ICERainbow666
      * @return 用于接收客户端消息的缓冲输入流 // ICERainbow666
      */ // ICERainbow666
     public BufferedReader getReader() { // ICERainbow666
         return reader; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取打印输出流。 // ICERainbow666
      * // ICERainbow666
      * @return 用于向客户端发送消息的打印输出流 // ICERainbow666
      */ // ICERainbow666
     public PrintWriter getWriter() { // ICERainbow666
         return writer; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 向客户端发送消息。 // ICERainbow666
      * // ICERainbow666
      * @param message 要发送的消息内容 // ICERainbow666
      */ // ICERainbow666
     public void send(String message) { // ICERainbow666
         writer.println(message); // ICERainbow666
     } // ICERainbow666
 } // ICERainbow666
