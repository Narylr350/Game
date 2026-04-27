package client;

import client.JDBC.JDBCUtil;
import client.domain.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Start {
    //这个方法表示的就是登录注册的主页面
    public String start() {

        System.out.println("游戏的登录注册页面打开了");

        ArrayList<User> list = new ArrayList<>();
        //启动立刻读取数据库所有用户
        loadUserFromDB(list);

        //ctrl+alt+t  选择对应的语句包裹代码
        while (true) {
            System.out.println("╔════════════════════════════════╗");
            System.out.println("    🎮 欢迎来到三人斗地主 🎮   ");
            System.out.println("╚════════════════════════════════╝");
            System.out.println("请选择操作：1登录 2注册 3退出");
            Scanner sc = new Scanner(System.in);
            String choose = sc.next();
            switch (choose) {
                case "1" ->{
                    // ====================== 登录成功就返回用户名 ======================
                    String name = login(list);
                    if (name != null) {
                        return name; // 登录成功 → 返回名字
                    }
                }
                case "2" -> register(list);
                case "3" -> {
                    System.out.println("用户选择了退出操作");

                    System.exit(0);//虚拟机停止运行
                }
                default -> System.out.println("输入有误请重新输入");
            }
        }
    }



    //登录操作
    public String login(ArrayList<User> list) {
        System.out.println("用户选择了登录操作");
        //1.判断用户是否存在
        //不存在：提示未注册
        //存在：禁用，提示联系客服
        //存在：验证码验证
        //验证码是否正确
        //1.键盘录入用户名
        Scanner sc = new Scanner(System.in);
        System.out.println("输入用户名");
        String username = sc.next();
        //不存在提示未注册
        if(!contains(list,username)){
            System.out.println("用户名"+username+"未注册，请先注册再登录");
            return null;//写return的目的是直接终止login方法的运行，回到选择界面去注册
        }
        //3.存在：禁用，提示联系客服
        //通过username,获取到当前的用户对象，再查看账户的状态
        int index = findIndex(list,username);
        User u = list.get(index);
        if(!u.isStatus()){
            System.out.println("用户"+username+"已禁用，请联系客服乌鲁鲁：18000000000");
            //如果用户名禁用，结束登录的行为，回到选择界面当中去主注册
            return null;
        }
        //4.用户继续键盘录入验证码和密码
        //验证密码是否正确
        String rightpassword = u.getPassword();
        for (int i = 0; i < 3; i++) {
            System.out.println("请输入密码：");
            String password = sc.next();
            //每次验证密码的时候，都要输入验证码
            while (true) {
                //先生成一个正确的验证码
                String rightCode = getCode();
                System.out.println("正确的验证码为："+rightCode);
                System.out.println("请输入验证码：");
                String code = sc.next();
                if (rightCode.equalsIgnoreCase(code)){
                    System.out.println("验证码输入正确:");
                    //如果验证码输入正确，跳出循环，继续判断密码
                    break;
                }else {
                    System.out.println("验证码输入错误：");
                    //如果验证码输入错误，需要重新生成一个新的验证码，并且让用户重新输入
                    continue;
                }
            }

            //密码
            if(rightpassword.equals(password)){
                System.out.println("登录成功,游戏启动~");
                return username;
            }else{
                System.out.println("登录失败，密码输入错误~");
                if (i==2){
                    //三次机会都用完了
                    u.setStatus(false);
                    System.out.println("当前账户"+username+"已锁定，请联系客服乌鲁鲁：18000000000");
                    return null;
                }else{
                    //三次机会没有用完
                    System.out.println("密码错误，还剩下"+(2-i)+"次机会~");
                }
            }
        }
        return null;
    }
    //注册操作
    public void register(ArrayList<User> list) {
        System.out.println("用户选择了注册操作");
        //1.创建USER对象
        User u = new User();

        //2.键盘录入
        //校验用户名是否复合要求
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("请输入用户名：");
            String username = sc.next();
            //1.长度必须在3-16位
            if (!checklen(3, 16, username)) {
                System.out.println("用户名长度必须是3-16位之间");
                continue;
            }
            //2.只能由字母，数字组成，不能是纯数字
            if (!checkusername(username)) {
                System.out.println("只能由字母，数字组成，不能是纯数字");
                continue;
            }
            //3.用户名唯一
            //username到list当中判断是否包含
            if (contains(list,username)){
                System.out.println("用户名已经存在请重新输入");
                continue;
            }

            //当代码执行到这里，表示用户名username长度，内容，符合要求，而且是唯一的
            u.setUsername(username);
            break;
        }

        //3.键盘录入密码
        //校验密码是否符合要求
        while (true) {
            System.out.println("请输入密码：");
            String password1 = sc.next();
            System.out.println("请再次输入密码：");
            String password2 = sc.next();
            //1.长度必须在3-8位
            if (!checklen(3, 8, password1)) {
                System.out.println("密码长度必须是3-8位之间");
                continue;
            }
            //只能是字母加数字组合，不能有其他字母
            if (!checkPassword(password1)) {
                System.out.println("只能由字母+数字组成，不能有其他");
                continue;
            }
            //校验两次密码输入是否一致
            if (!password1.equals(password2)){
                System.out.println("两次密码输入不一致请重新输入");
                continue;
            }
            //到这一行密码验证通过
            //把密码设置到对象中去
            u.setPassword(password1);
            break;
        }
        //4.把USER对象添加到集合中
        list.add(u);
        //提示注册成功
        System.out.println("用户"+u.getUsername()+"注册成功！");
        // 在这里加一行：批量保存
        batchSaveUsers(list);
    }
    //作用：判断长度是否符合要求
    public boolean checklen(int min, int max, String username) {
        return username.length() >= min && username.length() <= max;
    }
    //作用：判断用户名格式是否符合要求
    public boolean checkusername(String usernamne) {
        int[] arr= getCount(usernamne);
        return arr[0] > 0 && arr[1] >=0 && arr[2] == 0;
    }
    //作用：判断密码格式是否符合要求
    public boolean checkPassword(String Password) {
        int[] arr= getCount(Password);
        return arr[0] > 0 && arr[1] > 0 && arr[2] == 0;
    }
    //作用：在集合中去找username所在的索引
    public int findIndex(ArrayList<User> list,String username){
        for (int i = 0; i < list.size(); i++) {
            User u = list.get(i);//用普通 for 循环遍历 ArrayList<User>
            if (u.getUsername().equals(username)){
                return i;
            }
        }
        return -1;
    }
    //作用：判断用户名在集合中是否包含
    public boolean contains(ArrayList<User> list,String username){
        for (int i = 0; i < list.size(); i++) {
            User u = list.get(i);//用普通 for 循环遍历 ArrayList<User>
            if (u.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }

    //统计字符串当中，字母，数字，其他字符分别有多少个？
    public int[] getCount(String userInfo){
        int charCount = 0;
        int numCount = 0;
        int otherCount = 0;
        for (int i = 0; i < userInfo.length(); i++) {
            char c = userInfo.charAt(i);
            if (Character.isLetter(c)) {
                charCount++;
            } else if (Character.isDigit(c)) {
                numCount++;
            } else {
                otherCount++;
            }
        }
        return new int[]{charCount,numCount,otherCount};
    }
    //获取验证码
    public static String getCode(){
        //把大小写都装进集合里面
        ArrayList<Character> list = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            list.add((char)('a'+i));
            list.add((char)('A'+i));
        }
        //2.从集合中随机抽取字母（抽4次）
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            int index=r.nextInt(list.size());
            char c=list.get(index);
            sb.append(c);
        }
        //3.生成一个0-9的随机数字
        int num=r.nextInt(10);
        //4.数字的位置可以是任意的
        int index = r.nextInt(sb.length() + 1);
        // 5. 插入数字
        sb.insert(index, num);
        return sb.toString();
    }
    //程序启动 加载数据库所有用户到集合
    public void loadUserFromDB(ArrayList<User> list){
        Connection con = JDBCUtil.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "select id,username,password,status from user";
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                User u = new User();
                u.setId(rs.getString("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setStatus(rs.getInt("status") == 1);
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            JDBCUtil.close(rs,stmt,con);
        }
    }
    // 批量把 ArrayList 里的所有用户存进 MySQL
    public void batchSaveUsers(ArrayList<User> list) {
        Connection conn = JDBCUtil.getConnection();
        PreparedStatement pstmt = null;

        try {
            if(conn == null){
                System.out.println("数据库连接失败");
                return;
            }
            conn.setAutoCommit(false);

            for (User u : list) {
                // 去数据库查当前用户
                User dbUser = findUserByDb(u.getUsername());
                if(dbUser != null){
                    // 存在：执行更新
                    String updateSql = "update user set password=?,status=? where username=?";
                    pstmt = conn.prepareStatement(updateSql);
                    pstmt.setString(1,u.getPassword());
                    pstmt.setInt(2,u.isStatus()?1:0);
                    pstmt.setString(3,u.getUsername());
                }else{
                    // 不存在：执行新增
                    String insertSql = "insert into user(id,username,password,status) values(?,?,?,?)";
                    pstmt = conn.prepareStatement(insertSql);
                    pstmt.setString(1,u.getId());
                    pstmt.setString(2,u.getUsername());
                    pstmt.setString(3,u.getPassword());
                    pstmt.setInt(4,u.isStatus()?1:0);
                }
                pstmt.executeUpdate();
            }
            conn.commit();
            System.out.println("用户数据同步数据库完成");

        } catch (SQLException e) {
            try {
                if(conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JDBCUtil.close(null,pstmt,conn);
        }
    }
    // 根据用户名查询数据库是否存在该用户
    public User findUserByDb(String username){
        Connection con = JDBCUtil.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "select * from user where username = ?";
        User user = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,username);
            rs = pstmt.executeQuery();
            if(rs.next()){
                user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setStatus(rs.getInt("status") == 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            JDBCUtil.close(rs,pstmt,con);
        }
        return user;
    }
}
