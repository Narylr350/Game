package client.domain;

import java.util.Random;

public class User {
    //id、用户名、密码、状态
     private String id;
     private String username;
     private String password;
     private boolean status;//fales 禁用  true 可用
    public User(){
        id = createID();
        status = true;
    }
    public User(String username, String password){
        id = createID();
        this.username = username;
        this.password = password;
        status = true;
    }
    //用户无法设置，是自动生成的，格式为：user+5位数字的随机数
// 定义一个方法，方法名叫 createID，返回值是字符串 String
    public String createID(){
        // 1. 创建一个字符串拼接工具 StringBuilder
        // 初始内容是："user"
        StringBuilder sb = new StringBuilder("user");
        // 2. 创建随机数工具 Random
        Random r = new Random();
        // 3. 循环 5 次，生成 5 个随机数字
        for (int i=0;i<5;i++) {
            // 生成 0~9 之间的随机整数
            int num = r.nextInt(10);
            // 把生成的数字 追加到 "user" 后面
            sb.append(num);
        }
        // 4. 把拼接好的最终字符串（user+5位数字）返回
        return sb.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
