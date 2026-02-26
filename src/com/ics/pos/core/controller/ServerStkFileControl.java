/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ics.pos.core.controller;

import com.ics.bean.STKFileBean;
import database.MySQLConnectWebOnline;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nateelive
 */
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
            String sql = "insert ignore into stkfile (bpcode,branch) values('" + bpCode + "','" + branchCode + "');";
            if (mysqlServer.getConnection().createStatement().executeUpdate(sql) > 0) {
                stkFileBean = new STKFileBean();
                stkFileBean.setbPcode(bpCode);
                stkFileBean.setbStk(stockCode);
                stkFileBean.setbQty(0);
                stkFileBean.setbAmt(0);
                stkFileBean.setbTotalAmt((0));
                stkFileBean.setbQty0((0));
                stkFileBean.setbQty1((0));
                stkFileBean.setbQty2((0));
                stkFileBean.setbQty3((0));
                stkFileBean.setbQty4((0));
                stkFileBean.setbQty5((0));
                stkFileBean.setbQty6((0));
                stkFileBean.setbQty7((0));
                stkFileBean.setbQty8((0));
                stkFileBean.setbQty9((0));
                stkFileBean.setbQty10((0));
                stkFileBean.setbQty11((0));
                stkFileBean.setbQty12((0));
                stkFileBean.setbQty13((0));
                stkFileBean.setbQty14((0));
                stkFileBean.setbQty15((0));
                stkFileBean.setbQty16((0));
                stkFileBean.setbQty17((0));
                stkFileBean.setbQty18((0));
                stkFileBean.setbQty19((0));
                stkFileBean.setbQty20((0));
                stkFileBean.setbQty21((0));
                stkFileBean.setbQty22((0));
                stkFileBean.setbQty23((0));
                stkFileBean.setbQty24((0));
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
            String sqlUpdateStkfile = "update stkfile "
                    + "set bqty='" + stkFileBean.getbQty() + "',bamt='" + stkFileBean.getbAmt() + "',"
                    + "btotalamt='" + stkFileBean.getbTotalAmt() + "',"
                    + "bqty0='" + stkFileBean.getbQty0() + "',bqty1='" + stkFileBean.getbQty1() + "',"
                    + "bqty2='" + stkFileBean.getbQty2() + "',bqty3='" + stkFileBean.getbQty3() + "',"
                    + "bqty4='" + stkFileBean.getbQty4() + "',bqty5='" + stkFileBean.getbQty5() + "',"
                    + "bqty6='" + stkFileBean.getbQty6() + "',bqty7='" + stkFileBean.getbQty7() + "',"
                    + "bqty8='" + stkFileBean.getbQty8() + "',bqty9='" + stkFileBean.getbQty9() + "',"
                    + "bqty10='" + stkFileBean.getbQty10() + "',"
                    + "bqty11='" + stkFileBean.getbQty11() + "',bqty12='" + stkFileBean.getbQty12() + "',"
                    + "bqty13='" + stkFileBean.getbQty13() + "',bqty14='" + stkFileBean.getbQty14() + "',"
                    + "bqty15='" + stkFileBean.getbQty15() + "',"
                    + "bqty16='" + stkFileBean.getbQty16() + "',bqty17='" + stkFileBean.getbQty17() + "',"
                    + "bqty18='" + stkFileBean.getbQty18() + "',bqty19='" + stkFileBean.getbQty19() + "',"
                    + "bqty20='" + stkFileBean.getbQty20() + "',"
                    + "bqty21='" + stkFileBean.getbQty21() + "',bqty22='" + stkFileBean.getbQty22() + "',"
                    + "bqty23='" + stkFileBean.getbQty23() + "',bqty24='" + stkFileBean.getbQty24() + "',"
                    + "branch='" + stkFileBean.getBranch() + "',"
                    + "lastupdate='" + lastUpdate + "' ,lastTimeUpdate='" + lastTimeUpdate + "' "
                    + "where bpcode='" + stkFileBean.getbPcode() + "' "
                    + "and branch='" + stkFileBean.getBranch() + "'";
            mysqlServer.getConnection().createStatement().executeUpdate(sqlUpdateStkfile);
        } catch (SQLException ex) {
            Logger.getLogger(ServerStkFileControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mysqlServer.close();
        }
    }
}
