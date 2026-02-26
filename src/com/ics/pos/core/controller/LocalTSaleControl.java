/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import database.MySQLConnect;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nateelive
 */
public class LocalTSaleControl {
    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public STCardBean getDataByMacNoRefNoPluCodeFlagRVoid(String macno, String refno, String s_PCode,
            String s_Date, String r_Time) {
        
        STCardBean bean = null;

        try {
            mysqlLocal.open();
            String sql = "select r_refno, R_Total, r_nettotal R_Nettotal, r_pramt, r_discbath"
                    + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time,r_void "
                    + "from t_sale "
                    + "where macno=? and r_refno=? "
                    + "and r_plucode=? and r_date=? "
                    + "and r_time=? "
                    + "and r_void='V' limit 1;";
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            pstmt.setString(1, macno);
            pstmt.setString(2, refno);
            pstmt.setString(3, s_PCode);
            pstmt.setString(4, s_Date);
            pstmt.setString(5, r_Time);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = new STCardBean();
                if (rs.getDouble("R_Total") != rs.getDouble("R_Nettotal")) {
                    bean.setDiscount((rs.getDouble("R_Total") - rs.getDouble("R_Nettotal")));
                } else {
                    bean.setDiscount(0);
                }
                bean.setNettotal(rs.getDouble("R_Nettotal"));
                bean.setRefund(rs.getString("R_Refund"));
                bean.setRefNo(rs.getString("R_Refno"));
                bean.setCashier(rs.getString("Cashier"));
                bean.setEmp(rs.getString("R_Emp"));
                if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                    bean.setNettotal(0);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalTSaleControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return bean;
    }
    
    public STCardBean getDataByPluCodeRdateRTimeRVoid(String s_PCode, String s_Date, String r_time) {
        STCardBean bean = null;
        
        try {
            mysqlLocal.open();
            String sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath,"
                    + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,R_Time,r_void "
                    + "from t_sale where r_plucode=? "
                    + "and r_date=? and r_time=? and r_void<>'V' "
                    + "limit 1;";
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            pstmt.setString(1, s_PCode);
            pstmt.setString(2, s_Date);
            pstmt.setString(3, r_time);
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                bean = new STCardBean();
                if (rs.getDouble("R_Total") != rs.getDouble("R_Nettotal")) {
                    bean.setDiscount((rs.getDouble("R_Total") - rs.getDouble("R_Nettotal")));
                } else {
                    bean.setDiscount(0);
                }
                bean.setNettotal(rs.getDouble("R_Nettotal"));
                bean.setRefund(rs.getString("R_Refund"));
                bean.setRefNo(rs.getString("R_Refno"));
                bean.setCashier(rs.getString("Cashier"));
                bean.setEmp(rs.getString("R_Emp"));
                if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                    bean.setNettotal(0);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalTSaleControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }
        
        return bean;
    }
    
    public STCardBean getDataByMacnoRTimeRDatePluCodeRVoid(String macno, String r_newTime, 
            String s_PCode, String s_Date, boolean voidFlag) {

        STCardBean bean = null;
        try {
            mysqlLocal.open();
            String sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath"
                    + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time "
                    + "from t_sale "
                    + "where macno=? and r_time=? "
                    + "and r_plucode=? and r_date=? ";
            if (voidFlag) {
                sql += " and r_void = 'V' ";
            } else {
                sql += " and r_void <> 'V' ";
            }
            sql += " limit 1";
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            pstmt.setString(1, macno);
            pstmt.setString(2, r_newTime);
            pstmt.setString(3, s_PCode);
            pstmt.setString(4, s_Date);
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                bean = new STCardBean();
                if (rs.getDouble("R_Total") < 0) {
                    bean.setDiscount((rs.getDouble("R_Total") + rs.getDouble("R_Nettotal")));
                } else {
                    bean.setDiscount((rs.getDouble("R_Total") - rs.getDouble("R_Nettotal")));
                }

                bean.setNettotal(rs.getDouble("R_Nettotal"));
                bean.setRefund(rs.getString("R_Refund"));
                bean.setRefNo(rs.getString("R_Refno"));
                bean.setCashier(rs.getString("Cashier"));
                bean.setEmp(rs.getString("R_Emp"));
                if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                    bean.setNettotal(0);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalTSaleControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }
        
        return bean;
    }
}
