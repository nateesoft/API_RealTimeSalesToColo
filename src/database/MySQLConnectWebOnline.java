package database;

import com.ics.constants.Value;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnectWebOnline {

    private Connection con = null;
    private static String HostName = null;
    private static String DbName = null;
    private static String UserName = null;
    private static String Password = null;
    private static String PortNumber = null;

    static {
        getDbVar();
    }

    public void open() {
        close();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + HostName + ":" + PortNumber + "/" + DbName
                    + "?characterEncoding=utf-8"
                    + "&serverTimezone=Asia/Bangkok"
                    + "&useSSL=false";
            con = DriverManager.getConnection(url, UserName, Password);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(MySQLConnectWebOnline.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() {
        return con;
    }

    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(MySQLConnectWebOnline.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                con = null;
            }
        }
    }

    public static void getDbVar() {
        if (HostName == null) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(Value.FILE_CONFIGOnline), "UTF-8"))) {
                String tmp;
                while ((tmp = br.readLine()) != null) {
                    String[] data = tmp.split(",", 2);
                    if (data.length < 2) continue;

                    switch (data[0].toLowerCase()) {
                        case "webserver":
                            HostName = data[1];
                            break;
                        case "webdatabase":
                            DbName = data[1];
                            break;
                        case "user":
                            UserName = data[1];
                            break;
                        case "pass":
                            Password = data[1];
                            break;
                        case "port":
                            PortNumber = data[1];
                            break;
                        case "macno":
                            Value.MACNO = data[1];
                            break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MySQLConnectWebOnline.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
