/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import com.ics.bean.STKFileBean;
import com.ics.bean.TraninOut;
import database.MySQLConnect;
import database.MySQLConnectWebOnline;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.MSG;

/**
 *
 * @author nateelive
 */
public class TranIOControl {

    private final MySQLConnect mysqlLocal = new MySQLConnect();
    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();
    private String Branch = "";

    public List<STCardBean> TiNDownload(String typeIO) {
        ArrayList<STCardBean> list = new ArrayList();
        list.clear();
        try {
            mysqlServer.open();
            String sql = "";
            String conditionType = "";
            if (typeIO.contains("TRI")) {
                conditionType = "TRI_HQ";
            } else {
                conditionType = "TRO_HQ";
            }
            sql = "select * from stcard "
                    + "where "
                    + "s_rem='" + conditionType + "' "
                    + "and Source_data='WEB' "
                    + "and data_sync='N' "
                    + "and s_bran='" + Branch + "';";
            ResultSet rs = mysqlServer.getConnection().createStatement().executeQuery(sql);
            while (rs.next()) {
                STCardBean bean = new STCardBean();
                bean.setS_Date(rs.getString("S_Date"));
                bean.setS_No(rs.getString("S_No"));
                bean.setS_Que(rs.getInt("S_Que"));
                bean.setS_PCode(rs.getString("S_PCode"));
                bean.setS_Stk(rs.getString("S_Stk"));
                bean.setS_In(rs.getDouble("S_In"));
                bean.setS_Out(rs.getDouble("S_Out"));
                bean.setS_InCost(rs.getDouble("S_Incost"));
                bean.setS_OutCost(rs.getDouble("S_Outcost"));
                bean.setS_Rem(rs.getString("S_Rem"));
                bean.setS_User(rs.getString("S_User"));
                bean.setS_EntryDate(rs.getString("S_Entrydate"));
                bean.setS_EntryTime(rs.getString("S_Entrytime"));
                bean.setDataSync(rs.getString("Data_Sync"));
                bean.setSource_Data(rs.getString("Source_Data"));
                bean.setDiscount(rs.getDouble("Discount"));
                bean.setNettotal(rs.getDouble("Nettotal"));
                list.add(bean);
            }
            rs.close();
        } catch (Exception e) {
            Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlServer.close();
        }
        return list;
    }

    public void TIO_Process(String branch) {
        this.Branch = branch;
        List<STCardBean> beanI = TiNDownload("TRI");
        List<STCardBean> beanO = TiNDownload("TRO");
        mysqlLocal.open();
        mysqlServer.open();
        try {
            if (beanI.size() > 0 && beanI != null) {
                for (int i = 0; i < beanI.size(); i++) {
                    try {
                        String pcode = beanI.get(i).getS_PCode();
                        String sqlGetUnit = "select punit1 from product where pcode='" + pcode + "' limit 1;";
                        ResultSet rs = mysqlLocal.getConnection().createStatement().executeQuery(sqlGetUnit);
                        String R_Unit = "";
                        double amount = 0;
                        if (rs.next()) {
                            R_Unit = rs.getString("Punit1");
                        }
                        amount = beanI.get(i).getS_In() * beanI.get(i).getS_InCost();
                        String sqlInsLocal = "insert tranin ("
                                + "R_No, R_Que, R_Pcode, R_Stock, R_Pack, "
                                + "R_Qty, R_Post, R_Unit, R_Cost, "
                                + "R_Amount, R_TotalQty, R_User, R_Time, R_Entrydate, "
                                + "R_SendFTP, R_Pqty, R_Order, R_Send, R_RefCode, "
                                + "R_SendInterface) values("
                                + "'" + beanI.get(i).getS_No() + "','" + beanI.get(i).getS_Que() + "','" + beanI.get(i).getS_PCode() + "','" + beanI.get(i).getS_Stk() + "','1',"
                                + "'" + beanI.get(i).getS_In() + "','N','" + R_Unit + "','" + beanI.get(i).getS_InCost() + "',"
                                + "'" + amount + "','0','" + beanI.get(i).getS_User() + "','" + beanI.get(i).getS_EntryTime() + "','" + beanI.get(i).getS_EntryDate() + "',"
                                + "'N','0','0','0.00','',"
                                + "'N') ";
                        mysqlLocal.getConnection().createStatement().executeUpdate(sqlInsLocal);
                        try {
                            String sql = " update stcard set"
                                    + " data_Sync='Y' "
                                    + "where "
                                    + "s_bran='" + Branch + "' "
                                    + "and s_date='" + beanI.get(i).getS_Date() + "' and s_no='" + beanI.get(i).getS_No() + "' and s_que='" + beanI.get(i).getS_Que() + "' and s_pcode='" + beanI.get(i).getS_PCode() + "' and s_rem='" + "TRI_HQ" + "'"
                                    + "and s_entrydate='" + beanI.get(i).getS_EntryDate() + "' and s_entrytime='" + beanI.get(i).getS_EntryTime() + "' and s_user='" + beanI.get(i).getS_User() + "' and s_in='" + beanI.get(i).getS_In() + "' and s_out='" + beanI.get(i).getS_Out() + "' "
                                    + "and s_outcost='" + beanI.get(i).getS_OutCost() + "' and data_Sync='N';";
                            mysqlServer.getConnection().createStatement().executeUpdate(sql);
                        } catch (Exception e) {
                            Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
                        }
                        rs.close();
                    } catch (Exception e) {
                        MSG.ERR(e.toString());
                        Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
                    }

                }
                MSG.NOTICE("ดาวน์โหลดเอกสารรับเข้า กรุณายืนยันเอกสารในระบบคลังสินค้า");
            }
            if (beanO.size() > 0 && beanO != null) {
                for (int i = 0; i < beanO.size(); i++) {
                    try {
                        String pcode = beanO.get(i).getS_PCode();
                        String sqlGetUnit = "select punit1 from product where pcode='" + pcode + "' limit 1;";
                        ResultSet rs = mysqlLocal.getConnection().createStatement().executeQuery(sqlGetUnit);
                        String R_Unit = "";
                        double amount = 0;
                        if (rs.next()) {
                            R_Unit = rs.getString("Punit1");
                        }
                        amount = beanO.get(i).getS_In() * beanO.get(i).getS_InCost();
                        String sqlInsLocal = "insert tranout ("
                                + "R_No, R_Que, R_Pcode, R_Stock, R_Pack, "
                                + "R_Qty, R_Post, R_Unit, R_Cost, "
                                + "R_Amount, R_TotalQty, R_User, R_Time, R_Entrydate) "
                                + "values("
                                + "'" + beanO.get(i).getS_No() + "','" + beanO.get(i).getS_Que() + "','" + beanO.get(i).getS_PCode() + "','" + beanO.get(i).getS_Stk() + "','1',"
                                + "'" + beanO.get(i).getS_Out() + "','N','" + R_Unit + "','" + beanO.get(i).getS_OutCost() + "',"
                                + "'" + amount + "','0','" + beanO.get(i).getS_User() + "','" + beanO.get(i).getS_EntryTime() + "','" + beanO.get(i).getS_EntryDate() + "'"
                                + ");";
                        System.out.println(sqlInsLocal);
                        mysqlLocal.getConnection().createStatement().executeUpdate(sqlInsLocal);
                        try {
                            String sql = " update stcard set"
                                    + " data_Sync='Y' "
                                    + "where "
                                    + "s_bran='" + Branch + "' "
                                    + "and s_date='" + beanO.get(i).getS_Date() + "' and s_no='" + beanO.get(i).getS_No() + "' and s_que='" + beanO.get(i).getS_Que() + "' and s_pcode='" + beanO.get(i).getS_PCode() + "' and s_rem='" + "TRO_HQ" + "'"
                                    + "and s_entrydate='" + beanO.get(i).getS_EntryDate() + "' and s_entrytime='" + beanO.get(i).getS_EntryTime() + "' and s_user='" + beanO.get(i).getS_User() + "' and s_in='" + beanO.get(i).getS_In() + "' and s_out='" + beanO.get(i).getS_Out() + "' "
                                    + "and s_outcost='" + beanO.get(i).getS_OutCost() + "' and data_Sync='N';";
                            mysqlServer.getConnection().createStatement().executeUpdate(sql);
                        } catch (Exception e) {
                            Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
                        }
                        rs.close();
                    } catch (Exception e) {
                        MSG.ERR(e.toString());
                        Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
                    }

                }
            }
            MSG.NOTICE("ดาวน์โหลดเอกสารสินค้าออกเรียบร้อย กรุณายืนยันเอกสารในระบบคลังสินค้า");
        } catch (Exception e) {
            Logger.getLogger(LocalSTranControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
            mysqlServer.close();
        }

    }

}
