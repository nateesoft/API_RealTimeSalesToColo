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
public class LocalSTranControl {
    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public STCardBean getDataByMacnoTimeDatePluCode(String macno, String r_time, String s_PCode, String s_Date,
            String refno, String r_quan, String r_nettotal, String checkFirstDigitSNo, String s_No) {
        
        STCardBean bean = null;

        try {
            mysqlLocal.open();
            String sql = "";

            if (refno != null && r_quan != null && r_nettotal != null) {
                sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath , "
                        + "r_refund, cashier Cashier, r_emp R_Emp,r_time, r_quan ,r_plucode "
                        + "from s_tran "
                        + "where macno='" + macno + "' "
                        + "and r_refno='" + refno + "' "
                        + "and r_plucode='" + s_PCode + "' "
                        + "and r_date='" + s_Date + "' "
                        + "and r_quan='" + r_quan + "' "
                        + "and r_nettotal = '" + r_nettotal + "' "
                        + "limit 1";
            }
            if (r_time != null) {
                sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                        + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time, r_quan, "
                        + "r_plucode, r_void  "
                        + "from s_tran "
                        + "where macno='" + macno + "' "
                        + "and r_time='" + r_time + "' "
                        + "and r_plucode='" + s_PCode + "' "
                        + "and r_date='" + s_Date + "' "
                        + "limit 1";
            }

            ResultSet rs = mysqlLocal.getConnection().createStatement().executeQuery(sql);
            if (rs.next()) {
                bean = new STCardBean();
                bean.setDiscount(rs.getDouble("R_Total") - rs.getDouble("R_Nettotal"));
                bean.setNettotal(rs.getDouble("R_Nettotal"));
                bean.setRefund(rs.getString("R_Refund"));

                if (checkFirstDigitSNo.equals("R")) {
                    bean.setRefNo(s_No);
                    bean.setNettotal(rs.getDouble("R_Nettotal") * rs.getDouble("r_quan") * -1);
                    bean.setDiscount(bean.getDiscount() * rs.getDouble("r_quan") * -1);
                } else {
                    bean.setRefNo(rs.getString("R_Refno"));
                }

                bean.setCashier(rs.getString("Cashier"));
                bean.setEmp(rs.getString("R_Emp"));
                if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                    bean.setNettotal(0);
                }
                bean.setR_time(rs.getString("r_time"));
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return bean;
    }

    public STCardBean getDataByMacnoRefnoPluCodeRDateFlagVoid(String macno, String refno, String s_PCode, String s_Date,
            String checkFirstDigitSNo, String s_No) {
        STCardBean bean = null;

        try {
            mysqlLocal.open();

            String sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath"
                    + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time,r_void "
                    + "from s_tran "
                    + "where macno=? and r_refno=? "
                    + "and r_plucode=? and r_date=? and r_void='V' "
                    + "limit 1";
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            pstmt.setString(1, macno);
            pstmt.setString(2, refno);
            pstmt.setString(3, s_PCode);
            pstmt.setString(4, s_Date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = new STCardBean();
                bean.setDiscount((rs.getDouble("R_Total") - rs.getDouble("R_Nettotal")) * -1);
                if (rs.getDouble("R_Nettotal") != 0) {
                    bean.setNettotal(rs.getDouble("R_Nettotal") * -1);
                } else {
                    bean.setNettotal(rs.getDouble("R_Nettotal"));
                }

                bean.setRefund(rs.getString("R_Refund"));
                if (checkFirstDigitSNo.equals("R")) {
                    bean.setRefNo(s_No);
                } else {
                    bean.setRefNo(rs.getString("R_Refno"));
                }

                bean.setCashier(rs.getString("Cashier"));
                bean.setEmp(rs.getString("R_Emp"));

                if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                    bean.setNettotal(0);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return bean;
    }

    public STCardBean getDataByCondition(String macno, String refno, String s_PCode, 
            String s_Date, String r_time, Boolean isVoid,
            String checkFirstDigitSNo, String s_No) {
        String sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                + " r_refno, r_refund, cashier Cashier, "
                + "r_emp R_Emp,r_time,r_void "
                + "from s_tran where macno=? and r_time=? and r_plucode=? and r_date=? ";
        if (isVoid != null) {
            if (refno != null) {
                sql += " and r_refno='" + refno + "' ";
            }
            if (isVoid == true) {
                sql += " and r_void='V' ";
            } else if (isVoid == false) {
                sql += " and r_void<>'V' ";
            }
        }
        sql += " limit 1 ";

        STCardBean bean = null;
        try {
            mysqlLocal.open();
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            pstmt.setString(1, macno);
            pstmt.setString(2, r_time);
            pstmt.setString(3, s_PCode);
            pstmt.setString(4, s_Date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = new STCardBean();

                bean.setDiscount((rs.getDouble("R_Total") - rs.getDouble("R_Nettotal")));
                bean.setNettotal(rs.getDouble("R_Nettotal"));
                bean.setRefund(rs.getString("R_Refund"));
                if (checkFirstDigitSNo.equals("R")) {
                    bean.setRefNo(s_No);
                } else {
                    bean.setRefNo(rs.getString("R_Refno"));
                }

                bean.setCashier(rs.getString("Cashier"));
                bean.setEmp(rs.getString("R_Emp"));
                
                if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                    bean.setNettotal(0);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return bean;
    }
}
