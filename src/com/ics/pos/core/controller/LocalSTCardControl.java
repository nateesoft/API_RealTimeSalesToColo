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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nateelive
 */
public class LocalSTCardControl {
    private final MySQLConnect mysqlLocal = new MySQLConnect();
    
    public List<STCardBean> getListSTCardNotSend() {
        List<STCardBean> list = new ArrayList();
        try {
            mysqlLocal.open();
            String sql = "select * from stcard "
                    + "where s_send <> 'Y' ? and Source_Data <> 'WEB' "
                    + "order by s_date, s_no, s_pcode, s_entrytime;";
            PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql);
            try (ResultSet rs = psmtQuery.executeQuery()) {
                while (rs.next()) {
                    STCardBean bean = new STCardBean();
                    bean.setS_Date(rs.getString("s_date"));
                    bean.setS_No(rs.getString("s_no"));
                    bean.setS_SubNo(rs.getString("s_subno"));
                    bean.setS_Que(rs.getInt("s_que"));
                    bean.setS_PCode(rs.getString("s_pcode"));
                    bean.setS_Stk(rs.getString("s_stk"));
                    bean.setS_In(rs.getDouble("s_in"));
                    bean.setS_Out(rs.getDouble("s_out"));
                    bean.setS_InCost(rs.getDouble("s_incost"));
                    bean.setS_OutCost(rs.getDouble("s_outcost"));
                    bean.setS_ACost(rs.getDouble("s_aCost"));
                    bean.setS_Rem(rs.getString("s_rem"));
                    bean.setS_User(rs.getString("s_User"));
                    bean.setS_EntryDate(rs.getString("s_entrydate"));
                    bean.setS_EntryTime(rs.getString("s_entrytime"));
                    bean.setS_Link(rs.getString("s_link"));
                    bean.setS_Send(rs.getString("s_send"));
                    
                    list.add(bean);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return list;
    }
    
    public boolean updateSendStatus(STCardBean stCardNotSend, String LastUpdate, String LastTimeUpdate) {
        try {
            mysqlLocal.open();
            String sqlLocalUpdateY = "update stcard set "
                    + "s_send='Y' ,"
                    + "LastUpdate='" + LastUpdate + "',"
                    + "LastTimeUpdate='" + LastTimeUpdate + "' "
                    + "where s_pcode='" + stCardNotSend.getS_PCode() + "' "
                    + "and s_date='" + stCardNotSend.getS_Date() + "' "
                    + "and s_entrytime='" + stCardNotSend.getS_EntryTime() + "'"
                    + "and s_rem='" + stCardNotSend.getS_Rem() + "' "
                    + "and s_user='" + stCardNotSend.getS_User() + "' "
                    + "and s_send='N' "
                    + "and s_no='" + stCardNotSend.getS_No() + "';";
            return mysqlLocal.getConnection().createStatement().executeUpdate(sqlLocalUpdateY) > 0;
        } catch (SQLException e) {
            Logger.getLogger(LocalSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return false;
    }
    
    public boolean updateSendStatusDone(STCardBean stCardNotSend, String LastUpdate, String LastTimeUpdate) {
        try {
            mysqlLocal.open();
            
            String sqlLocalUpdateY = "update stcard set "
                + "s_send='Y' ,"
                + "LastUpdate='" + LastUpdate + "',"
                + "LastTimeUpdate='" + LastTimeUpdate + "' "
                + "where s_pcode='" + stCardNotSend.getS_PCode() + "' "
                + "and s_date='" + stCardNotSend.getS_Date() + "' "
                + "and s_entrytime='" + stCardNotSend.getS_EntryTime() + "'"
                + "and s_rem='" + stCardNotSend.getS_Rem() + "' "
                + "and s_user='" + stCardNotSend.getS_User() + "' "
                + "and s_send='N' "
                + "and s_no='" + stCardNotSend.getS_No() + "' "
                + "and s_incost='" + stCardNotSend.getS_InCost() + "' "
                + "and s_in ='" + stCardNotSend.getS_In() + "' "
                + "and s_out='" + stCardNotSend.getS_Out() + "' "
                + "and s_outcost='" + stCardNotSend.getS_OutCost() + "' "
                + "and s_que='" + stCardNotSend.getS_Que() + "';";
            return mysqlLocal.getConnection().createStatement().executeUpdate(sqlLocalUpdateY) > 0;
        } catch (SQLException e) {
            Logger.getLogger(LocalSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return false;
    }
}
