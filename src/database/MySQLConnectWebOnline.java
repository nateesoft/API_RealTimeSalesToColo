package database;

import com.ics.constants.Value;
import java.io.BufferedReader;
import java.io.DataInputStream;
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
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + HostName + ":" + PortNumber + "/" + DbName + "?characterEncoding=utf-8", UserName, Password);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(MySQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() {
        return con;
    }

    public void close() {
        if (con != null) {
            try {
                con.close();
                //System.out.println("Database Closed.");
            } catch (SQLException ex) {
                Logger.getLogger(MySQLConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void getDbVar() {
        if (HostName == null) {
            try {
                FileInputStream fs = new FileInputStream(Value.FILE_CONFIGOnline);
                DataInputStream ds = new DataInputStream(fs);
                BufferedReader br = new BufferedReader(new InputStreamReader(ds));
                String tmp;
                while ((tmp = br.readLine()) != null) {
                    String[] data = tmp.split(",", tmp.length());
                    if (data[0].equalsIgnoreCase("webServer")) {
                        HostName = data[1];
                    } else if (data[0].equalsIgnoreCase("webDatabase")) {
                        DbName = data[1];
                        Value.DATABASE = data[1];
                    } else if (data[0].equalsIgnoreCase("user")) {
                        UserName = data[1];
                    } else if (data[0].equalsIgnoreCase("pass")) {
                        Password = data[1];
                    } else if (data[0].equalsIgnoreCase("port")) {
                        PortNumber = data[1];
                    } else if (data[0].equalsIgnoreCase("macno")) {
                        Value.MACNO = data[1];
                    }
                }
                br.close();
                ds.close();
                fs.close();
            } catch (IOException ex) {
                Logger.getLogger(MySQLConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
