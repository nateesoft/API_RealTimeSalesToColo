/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import database.MySQLConnectWebOnline;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nateelive
 */
public class ServerSTCardControl {
    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();
    
    public boolean saveSTCard(STCardBean stCardNotSend, double discount, double nettotal, String refund, String refno, 
            String cashier, String emp, double unitPrice, String branchCode) {
        try {
            mysqlServer.open();
            String sql = "insert into stcard ("
                    + "s_date, s_no, s_subno, s_que, s_pcode, s_stk, "
                    + "s_in, s_out, s_incost, s_outcost, s_acost, s_rem, "
                    + "s_user, s_entrydate, s_entrytime, s_link, s_bran,"
                    + "discount,nettotal,refund,refno,cashier,"
                    + "emp,UnitPrice) "
                    + "values ("
                    + "'" + stCardNotSend.getS_Date() + "','" + stCardNotSend.getS_No() + "','" + stCardNotSend.getS_SubNo() + "',"
                    + "'" + stCardNotSend.getS_Que() + "','" + stCardNotSend.getS_PCode() + "','" + stCardNotSend.getS_Stk() + "',"
                    + "'" + stCardNotSend.getS_In() + "','" + stCardNotSend.getS_Out() + "','" + stCardNotSend.getS_InCost() + "',"
                    + "'" + stCardNotSend.getS_OutCost() + "','" + stCardNotSend.getS_ACost() + "','" + stCardNotSend.getS_Rem() + "',"
                    + "'" + stCardNotSend.getS_User() + "','" + stCardNotSend.getS_EntryDate() + "','" + stCardNotSend.getS_EntryTime() + "',"
                    + "'" + stCardNotSend.getS_Link() + "','" + branchCode + "',"
                    + "'" + discount + "','" + nettotal + "','" + refund + "','" + refno + "','" + cashier + "',"
                    + "'" + emp + "','" + unitPrice + "');";
            return mysqlServer.getConnection().createStatement().executeUpdate(sql) > 0;
        } catch (SQLException e) {
            Logger.getLogger(ServerSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlServer.close();
        }
        
        return false;
    }
}
