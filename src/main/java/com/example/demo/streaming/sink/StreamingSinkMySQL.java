package com.example.demo.streaming.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * mysql sink
 *
 * by：linjie
 */
public class StreamingSinkMySQL extends RichSinkFunction<String> {


    private static final long serialVersionUID = 1L;

    private Connection connection = null;
    private PreparedStatement preparedStatement = null;

    //mysql连接信息
    private String username = "root";
    private String password = "password";
    private String drivername = "com.mysql.jdbc.Driver";
    private String dburl = "jdbc:mysql://ip:10023/wingcloud?useUnicode=true&characterEncoding=UTF-8&useSSL=true";


    /**
     * 初始化方法
     * 使得不会每次调用数据库都尝试连接
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        connection = DriverManager.getConnection(dburl,username,password);
        Class.forName(drivername);//加载数据库驱动
        String sql = "update wingcloud.shop_money set money=money+(?) where money in (select temp.money from (select money from wingcloud.shop_money)temp)";
        preparedStatement = connection.prepareStatement(sql);
        super.open(parameters);
    }


    /***
     *
     * 每次处理的业务代码
     * @param money
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Override
    public void invoke(String money) throws SQLException, ClassNotFoundException {
        if(connection==null) {
            Class.forName(drivername);
            connection = DriverManager.getConnection(dburl, username, password);
        }
        Long long_money = 0l;

        //如果money传的值为null,会报空指针异常
        if (money != null){
            long_money = Long.valueOf(money);
        }

        //
        preparedStatement.setLong(1,long_money);
        preparedStatement.executeUpdate();

    }


    /**
     * 关闭mysql数据库相关连接
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        if(preparedStatement!=null){
            preparedStatement.close();
        }
        if(connection!=null){
            connection.close();
        }
        super.close();
    }

}
