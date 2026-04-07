 package server.net;
 
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 /**
  * 玩家连接类。
  * <p>
  * 封装单个客户端与服务端之间的连接信息,包括玩家ID、名称、Socket连接
  * 以及输入输出流。提供便捷的消息发送方法。
  * </p>
  */
 public class PlayerConnection{
     private final int playerId;              // 玩家唯一标识
     private final String name;               // 玩家名称
     private final Socket socket;             // Socket连接
     private final BufferedReader reader;     // 输入流,用于接收客户端消息
     private final PrintWriter writer;        // 输出流,用于向客户端发送消息
 
     /**
      * 创建玩家连接对象。
      *
      * @param playerId 玩家唯一标识ID
      * @param name     玩家名称
      * @param socket   客户端Socket连接
      * @param reader   缓冲输入流
      * @param writer   打印输出流
      */
     public PlayerConnection(int playerId, String name, Socket socket,
                             BufferedReader reader, PrintWriter writer) {
         this.playerId = playerId;
         this.name = name;
         this.socket = socket;
         this.reader = reader;
         this.writer = writer;
     }
 
     /**
      * 获取玩家ID。
      *
      * @return 玩家唯一标识ID
      */
     public int getPlayerId() {
         return playerId;
     }
 
     /**
      * 获取玩家名称。
      *
      * @return 玩家名称
      */
     public String getName() {
         return name;
     }
 
     /**
      * 获取Socket连接。
      *
      * @return 客户端Socket连接
      */
     public Socket getSocket() {
         return socket;
     }
 
     /**
      * 获取缓冲输入流。
      *
      * @return 用于接收客户端消息的缓冲输入流
      */
     public BufferedReader getReader() {
         return reader;
     }
 
     /**
      * 获取打印输出流。
      *
      * @return 用于向客户端发送消息的打印输出流
      */
     public PrintWriter getWriter() {
         return writer;
     }
 
     /**
      * 向客户端发送消息。
      *
      * @param message 要发送的消息内容
      */
     public void send(String message) {
         writer.println(message);
     }
 }
