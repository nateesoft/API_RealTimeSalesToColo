package com.ics.pos.core.controller;

import com.ics.bean.STKFileBean;
import database.MySQLConnectWebOnline;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerStkFileControl {
    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();

    public STKFileBean getDataByBPCodeBranchCode(String bpCode, String branchCode) {
        STKFileBean bean = null;

        try {
            mysqlServer.open();
            String sql = "select * from stkfile where bpcode=? and Branch=? limit 1";
            PreparedStatement psmtQuery = mysqlServer.getConnection().prepareStatement(sql);
            psmtQuery.setString(1, bpCode);
            psmtQuery.setString(2, branchCode);

            try (ResultSet rs = psmtQuery.executeQuery()) {
                if (rs.next()) {
                    bean = new STKFileBean();
                    bean.setbPcode(rs.getString("BPCode"));
                    bean.setbStk(rs.getString("BStk"));
                    bean.setbQty(rs.getDouble("BQty"));
                    bean.setbAmt(rs.getDouble("BAmt"));
                    bean.setbTotalAmt(rs.getDouble("BTotalAmt"));
                    bean.setbQty0(rs.getDouble("BQty0"));
                    bean.setbQty1(rs.getDouble("BQty1"));
                    bean.setbQty2(rs.getDouble("BQty2"));
                    bean.setbQty3(rs.getDouble("BQty3"));
                    bean.setbQty4(rs.getDouble("BQty4"));
                    bean.setbQty5(rs.getDouble("BQty5"));
                    bean.setbQty6(rs.getDouble("BQty6"));
                    bean.setbQty7(rs.getDouble("BQty7"));
                    bean.setbQty8(rs.getDouble("BQty8"));
                    bean.setbQty9(rs.getDouble("BQty9"));
                    bean.setbQty10(rs.getDouble("BQty10"));
                    bean.setbQty11(rs.getDouble("BQty11"));
                    bean.setbQty12(rs.getDouble("BQty12"));
                    bean.setbQty13(rs.getDouble("BQty13"));
                    bean.setbQty14(rs.getDouble("BQty14"));
                    bean.setbQty15(rs.getDouble("BQty15"));
                    bean.setbQty16(rs.getDouble("BQty16"));
                    bean.setbQty17(rs.getDouble("BQty17"));
                    bean.setbQty18(rs.getDouble("BQty18"));
                    bean.setbQty19(rs.getDouble("BQty19"));
                    bean.setbQty20(rs.getDouble("BQty20"));
                    bean.setbQty21(rs.getDouble("BQty21"));
                    bean.setbQty22(rs.getDouble("BQty22"));
                    bean.setbQty23(rs.getDouble("BQty23"));
                    bean.setbQty24(rs.getDouble("BQty24"));
                    bean.setBranch(rs.getString("Branch"));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(ServerStkFileControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlServer.close();
        }

        return bean;
    }

    public STKFileBean saveNewData(String bpCode, String branchCode, String stockCode) {
        STKFileBean stkFileBean = null;
        try {
            mysqlServer.open();
            String sql = "insert ignore into stkfile (bpcode, branch) values(?, ?)";
            PreparedStatement pstmt = mysqlServer.getConnection().prepareStatement(sql);
            pstmt.setString(1, bpCode);
            pstmt.setString(2, branchCode);
            if (pstmt.executeUpdate() > 0) {
                stkFileBean = new STKFileBean();
                stkFileBean.setbPcode(bpCode);
                stkFileBean.setbStk(stockCode);
                stkFileBean.setbQty(0);
                stkFileBean.setbAmt(0);
                stkFileBean.setbTotalAmt(0);
                stkFileBean.setbQty0(0);
                stkFileBean.setbQty1(0);
                stkFileBean.setbQty2(0);
                stkFileBean.setbQty3(0);
                stkFileBean.setbQty4(0);
                stkFileBean.setbQty5(0);
                stkFileBean.setbQty6(0);
                stkFileBean.setbQty7(0);
                stkFileBean.setbQty8(0);
                stkFileBean.setbQty9(0);
                stkFileBean.setbQty10(0);
                stkFileBean.setbQty11(0);
                stkFileBean.setbQty12(0);
                stkFileBean.setbQty13(0);
                stkFileBean.setbQty14(0);
                stkFileBean.setbQty15(0);
                stkFileBean.setbQty16(0);
                stkFileBean.setbQty17(0);
                stkFileBean.setbQty18(0);
                stkFileBean.setbQty19(0);
                stkFileBean.setbQty20(0);
                stkFileBean.setbQty21(0);
                stkFileBean.setbQty22(0);
                stkFileBean.setbQty23(0);
                stkFileBean.setbQty24(0);
                stkFileBean.setBranch(branchCode);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerStkFileControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mysqlServer.close();
        }

        return stkFileBean;
    }

    public void updateData(STKFileBean stkFileBean, String lastUpdate, String lastTimeUpdate) {
        try {
            mysqlServer.open();
            String sql = "update stkfile set "
                    + "bqty=?, bamt=?, btotalamt=?, "
                    + "bqty0=?, bqty1=?, bqty2=?, bqty3=?, bqty4=?, bqty5=?, "
                    + "bqty6=?, bqty7=?, bqty8=?, bqty9=?, bqty10=?, "
                    + "bqty11=?, bqty12=?, bqty13=?, bqty14=?, bqty15=?, "
                    + "bqty16=?, bqty17=?, bqty18=?, bqty19=?, bqty20=?, "
                    + "bqty21=?, bqty22=?, bqty23=?, bqty24=?, "
                    + "branch=?, lastupdate=?, lastTimeUpdate=? "
                    + "where bpcode=? and branch=?";
            PreparedStatement pstmt = mysqlServer.getConnection().prepareStatement(sql);
            pstmt.setDouble(1, stkFileBean.getbQty());
            pstmt.setDouble(2, stkFileBean.getbAmt());
            pstmt.setDouble(3, stkFileBean.getbTotalAmt());
            pstmt.setDouble(4, stkFileBean.getbQty0());
            pstmt.setDouble(5, stkFileBean.getbQty1());
            pstmt.setDouble(6, stkFileBean.getbQty2());
            pstmt.setDouble(7, stkFileBean.getbQty3());
            pstmt.setDouble(8, stkFileBean.getbQty4());
            pstmt.setDouble(9, stkFileBean.getbQty5());
            pstmt.setDouble(10, stkFileBean.getbQty6());
            pstmt.setDouble(11, stkFileBean.getbQty7());
            pstmt.setDouble(12, stkFileBean.getbQty8());
            pstmt.setDouble(13, stkFileBean.getbQty9());
            pstmt.setDouble(14, stkFileBean.getbQty10());
            pstmt.setDouble(15, stkFileBean.getbQty11());
            pstmt.setDouble(16, stkFileBean.getbQty12());
            pstmt.setDouble(17, stkFileBean.getbQty13());
            pstmt.setDouble(18, stkFileBean.getbQty14());
            pstmt.setDouble(19, stkFileBean.getbQty15());
            pstmt.setDouble(20, stkFileBean.getbQty16());
            pstmt.setDouble(21, stkFileBean.getbQty17());
            pstmt.setDouble(22, stkFileBean.getbQty18());
            pstmt.setDouble(23, stkFileBean.getbQty19());
            pstmt.setDouble(24, stkFileBean.getbQty20());
            pstmt.setDouble(25, stkFileBean.getbQty21());
            pstmt.setDouble(26, stkFileBean.getbQty22());
            pstmt.setDouble(27, stkFileBean.getbQty23());
            pstmt.setDouble(28, stkFileBean.getbQty24());
            pstmt.setString(29, stkFileBean.getBranch());
            pstmt.setString(30, lastUpdate);
            pstmt.setString(31, lastTimeUpdate);
            pstmt.setString(32, stkFileBean.getbPcode());
            pstmt.setString(33, stkFileBean.getBranch());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ServerStkFileControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mysqlServer.close();
        }
    }
}
