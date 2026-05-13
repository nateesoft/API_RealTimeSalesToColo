package com.ics.pos.core.controller;

import com.ics.bean.STKFileBean;
import database.MySQLConnectWebOnline;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.AppLogUtil;
import util.DateConvert;

public class ServerStkFileControl {

    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();

    public STKFileBean getDataByBPCodeBranchCode(String bpCode, String branchCode) {
        STKFileBean bean = null;

        try {
            mysqlServer.open();
            String sql = "select * from stkfile where bpcode=? and Branch=?";
            try (PreparedStatement psmtQuery = mysqlServer.getConnection().prepareStatement(sql)) {
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
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlServer.close();
        }

        return bean;
    }

    public STKFileBean saveNewData(STKFileBean bean) {
        try {
            DateConvert dc = new DateConvert();
            if (bean != null) {
                mysqlServer.open();
                String sql = "insert ignore into stkfile (bpcode,bstk,bqty,bamt,"
                        + "bqty0,bqty1,bqty2,bqty3,bqty4,"
                        + "bqty5,bqty6,bqty7,bqty8,bqty9,"
                        + "bqty10,bqty11,bqty12,bqty13,bqty14,"
                        + "bqty15,bqty16,bqty17,bqty18,bqty19,"
                        + "bqty20,bqty21,bqty22,bqty23,bqty24,"
                        + "branch,lastupdate,lasttimeupdate,sendtopos)values("
                        + "?, ?, ?, ?, ?,"
                        + "?, ?, ?, ?, ?,"
                        + "?, ?, ?, ?, ?,"
                        + "?, ?, ?, ?, ?,"
                        + "?, ?, ?, ?, ?,"
                        + "?, ?, ?, ?, ?,"
                        + "?, ?, ?"
                        + ")";
                try (PreparedStatement pstmt = mysqlServer.getConnection().prepareStatement(sql)) {
                    pstmt.setString(1, bean.getbPcode());
                    pstmt.setString(2, bean.getbStk());
                    pstmt.setDouble(3, bean.getbQty());
                    pstmt.setDouble(4, bean.getbAmt());
                    pstmt.setDouble(5, bean.getbQty0());
                    pstmt.setDouble(6, bean.getbQty1());
                    pstmt.setDouble(7, bean.getbQty2());
                    pstmt.setDouble(8, bean.getbQty3());
                    pstmt.setDouble(9, bean.getbQty4());
                    pstmt.setDouble(10, bean.getbQty5());
                    pstmt.setDouble(11, bean.getbQty6());
                    pstmt.setDouble(12, bean.getbQty7());
                    pstmt.setDouble(13, bean.getbQty8());
                    pstmt.setDouble(14, bean.getbQty9());
                    pstmt.setDouble(15, bean.getbQty10());
                    pstmt.setDouble(16, bean.getbQty11());
                    pstmt.setDouble(17, bean.getbQty12());
                    pstmt.setDouble(18, bean.getbQty13());
                    pstmt.setDouble(19, bean.getbQty14());
                    pstmt.setDouble(20, bean.getbQty15());
                    pstmt.setDouble(21, bean.getbQty16());
                    pstmt.setDouble(22, bean.getbQty17());
                    pstmt.setDouble(23, bean.getbQty18());
                    pstmt.setDouble(24, bean.getbQty19());
                    pstmt.setDouble(25, bean.getbQty20());
                    pstmt.setDouble(26, bean.getbQty21());
                    pstmt.setDouble(27, bean.getbQty22());
                    pstmt.setDouble(28, bean.getbQty23());
                    pstmt.setDouble(29, bean.getbQty24());
                    pstmt.setString(30, bean.getBranch());
                    pstmt.setString(31, dc.GetCurrentDate());
                    pstmt.setString(32, dc.GetCurrentTime());
                    pstmt.setString(33, "N");
                    if (pstmt.executeUpdate() > 0) {
                        AppLogUtil.info(getClass(), "Save New Stkfle : bpcode = " + bean.getbPcode() + " And Branch = " + bean.getBranch());
                    } else {
                        AppLogUtil.info(getClass(), "Cannot Add new Stkfile Server Because : bean is null");
                    }
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlServer.close();
        }

        return bean;
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
            try (PreparedStatement pstmt = mysqlServer.getConnection().prepareStatement(sql)) {
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
                
                AppLogUtil.info(getClass(), "Processing UIpdate stkfileServer bpcode : " + stkFileBean.getbPcode());
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlServer.close();
        }
    }
}
