package database;

import com.ics.constants.Value;
import util.AppLogUtil;
import util.MSG;

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

public class MySQLConnect {

    private Connection con = null;

    public static String HostName = null;
    public static String DbName = null;
    public static String UserName = null;
    public static String Password = null;
    public static String PortNumber = null;
    public static String CharSet = "utf-8";

    private String msgError =
            "พบการเชื่อมต่อมีปัญหา ไม่สามารถดำเนินการต่อได้\nท่านต้องการปิดโปรแกรมอัตโนมัติหรือไม่ ?";

    private Class<?> clazz = null;

    /* ===================== Load Config ===================== */
    static {
        try {
            FileInputStream fs = new FileInputStream(Value.FILE_CONFIG);
            DataInputStream ds = new DataInputStream(fs);
            BufferedReader br = new BufferedReader(new InputStreamReader(ds, "UTF-8"));

            String tmp;
            while ((tmp = br.readLine()) != null) {
                String[] data = tmp.split(",", -1);
                if (data.length < 2) continue;

                switch (data[0].toLowerCase()) {
                    case "server":
                        HostName = data[1];
                        break;
                    case "database":
                        DbName = data[1];
                        Value.DATABASE = data[1];
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
                    case "charset":
                        CharSet = data[1];
                        break;
                    case "macno":
                        Value.MACNO = data[1];
                        break;
                    case "language":
                        Value.LANG = data[1];
                        break;
                    case "db_member":
                        Value.db_member = data[1];
                        break;
                    case "useprint":
                        Value.useprint = Boolean.parseBoolean(data[1]);
                        break;
                    case "printkic":
                        Value.printkic = Boolean.parseBoolean(data[1]);
                        break;
                    case "autoqty":
                        Value.autoqty = Boolean.parseBoolean(data[1]);
                        break;
                    case "printdriver":
                        Value.printdriver = Boolean.parseBoolean(data[1]);
                        break;
                    case "printername":
                        Value.printerDriverName = data[1];
                        break;
                }
            }

            br.close();
            ds.close();
            fs.close();

        } catch (IOException e) {
            e.printStackTrace();
//            MSG.ERR(e.getMessage());
            AppLogUtil.log(MySQLConnect.class, "error", e);
        }
    }

    /* ===================== Getter / Setter ===================== */
    public String getMsgError() {
        return msgError;
    }

    public void setMsgError(String msgError) {
        this.msgError = msgError;
    }

    /* ===================== Open Connection ===================== */
    public void open(Class<?> clazz) {
        this.clazz = clazz;
        open();
    }

    public void open() {
        close();

        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + HostName + ":" + PortNumber + "/" + DbName
                    + "?useUnicode=true"
                    + "&characterEncoding=" + CharSet
                    + "&serverTimezone=Asia/Bangkok"
                    + "&useSSL=false";

            con = DriverManager.getConnection(url, UserName, Password);
            MySQLConstants.MYSQL_CONNECT.put(con.hashCode(), clazz);

        } catch (ClassNotFoundException | SQLException e) {
            MSG.ERR("Database Connection Error !!!\n" + e.getMessage());
            AppLogUtil.log(MySQLConnect.class, "error", e);
            System.exit(0);
        }
    }

    /* ===================== Get Connection ===================== */
    public Connection getConnection() {
        return con;
    }

    /* ===================== Close Connection ===================== */
    public void close() {
        if (con != null) {
            try {
                con.close();
                MySQLConstants.MYSQL_CONNECT.remove(con.hashCode());
                con = null;
            } catch (SQLException ex) {
                Logger.getLogger(MySQLConnect.class.getName()).log(Level.SEVERE, null, ex);
                AppLogUtil.log(MySQLConnect.class, "error", ex);
            }
        }
    }

    public void closeConnection(Class<?> clazz) {
        this.clazz = clazz;
        close();
    }
}
