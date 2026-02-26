/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ics.pos.core.controller;

import com.ics.bean.STKFileBean;
import database.MySQLConnect;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nateelive
 */
public class LocalStkFileControl {

    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public List<STKFileBean> getAllData() {
        List<STKFileBean> list = new ArrayList<>();
        try {
            mysqlLocal.open();
            String sql = "select * from stkfile";
            PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql);
            try (ResultSet rs = psmtQuery.executeQuery()) {
                while (rs.next()) {
                    STKFileBean bean = new STKFileBean();
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

                    list.add(bean);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalStkFileControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return list;
    }

    public STKFileBean getDataByBPCode(String bpCode) {
        STKFileBean bean = null;

        try {
            mysqlLocal.open();
            String sql = "select * from stkfile where bpcode=? limit 1";
            PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql);
            psmtQuery.setString(1, bpCode);
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
            Logger.getLogger(LocalStkFileControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return bean;
    }

    public STKFileBean saveNewData(String bpCode, String branchCode, String stockCode) {
        STKFileBean stkFileBean = null;
        try {
            mysqlLocal.open();
            String sql = "insert ignore into stkfile (bpcode,branch) values('" + bpCode + "','" + branchCode + "');";
            if (mysqlLocal.getConnection().createStatement().executeUpdate(sql) > 0) {
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
        } catch (SQLException e) {
            Logger.getLogger(LocalStkFileControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return stkFileBean;
    }

    public void updateTimeData(String bPcode, String currentData, String currentTime) {
        try {
            mysqlLocal.open();
            String sqlUpdateSendWeb = "update stkfile set "
                    + "Lastupdate='" + currentData + "',"
                    + "LastTimeUpdate='" + currentTime + "' "
                    + "where bpcode='" + bPcode + "'";
            mysqlLocal.getConnection().createStatement().executeUpdate(sqlUpdateSendWeb);
        } catch (SQLException e) {
            Logger.getLogger(LocalStkFileControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }
    }

}
