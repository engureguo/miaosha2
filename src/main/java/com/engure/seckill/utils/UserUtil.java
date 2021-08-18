package com.engure.seckill.utils;

import com.engure.seckill.pojo.User;
import com.engure.seckill.vo.RespBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 向数据库插入指定个数用户，同时使用它们访问 login/doLogin 接口
 * 如果 ticket 失效，需要注释 insertDb(users); 之后重新获取 ticket
 * 注意：运行时需要打开后端服务
 * <p>
 * 运行时出现的问题：忘记开redis了qwq
 */

public class UserUtil {
    private static void createUser(int count) throws Exception {
        List<User> users = new ArrayList<>(count);
        //生成用户
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(13000000000L + i);
            user.setLoginCount(1);
            user.setNickname("user" + i);
            user.setRegisterDate(new Date());
            user.setSalt("123456789123456");//随机盐
            user.setPassword(MD5Util.inputToDb("123456", user.getSalt()));
            users.add(user);
        }
        System.out.println("create user");

//        insertDb(users);

        //访问 login/doLogin 接口，拿到 ticket 保存起来
        String urlString = "http://localhost:8080/login/doLogin";
        File file = new File("C:\\Users\\HiWin10\\Desktop\\ticket.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (User user : users) {
            //创建post连接，写入方法体中表单参数
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();//方法体写入流
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputToForm("123456");
            out.write(params.getBytes());
            out.flush();

            //读取返回的respBean
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = bout.toString();
            ObjectMapper mapper = new ObjectMapper();
            RespBean respBean = mapper.readValue(response, RespBean.class);
            String userTicket = ((String) respBean.getData());// userServiceImpl.doLogin
            System.out.println("create userTicket : " + user.getId() + ", " + userTicket);
            String row = user.getId() + "," + userTicket;

            // 写入文件
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
        }
        raf.close();

        System.out.println("over");
    }

    //插入数据库
    public static void insertDb(List<User> users) throws Exception {
        Connection conn = getConn();
        String sql = "insert into t_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            pstmt.setInt(1, user.getLoginCount());
            pstmt.setString(2, user.getNickname());
            pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));//时间戳类型
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getPassword());
            pstmt.setLong(6, user.getId());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        System.out.println("insert to db");
    }

    //获取连接
    private static Connection getConn() throws Exception {
        String url = "jdbc:mysql://localhost:3306/miaosha?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public static void main(String[] args) throws Exception {
        createUser(1000);
    }
}
