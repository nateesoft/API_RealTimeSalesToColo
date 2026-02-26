package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import database.MySQLConnectWebOnline;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerSTCardControl {
    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();

    public boolean saveSTCard(STCardBean stCardNotSend, double discount, double nettotal, String refund, String refno,
            String cashier, String emp, double unitPrice, String branchCode) {
        try {
            mysqlServer.open();
            String sql = "insert into stcard ("
                    + "s_date, s_no, s_subno, s_que, s_pcode, s_stk, "
                    + "s_in, s_out, s_incost, s_outcost, s_acost, s_rem, "
                    + "s_user, s_entrydate, s_entrytime, s_link, s_bran, "
                    + "discount, nettotal, refund, refno, cashier, emp, UnitPrice) "
                    + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstmt = mysqlServer.getConnection().prepareStatement(sql);
            pstmt.setString(1, stCardNotSend.getS_Date());
            pstmt.setString(2, stCardNotSend.getS_No());
            pstmt.setString(3, stCardNotSend.getS_SubNo());
            pstmt.setInt(4, stCardNotSend.getS_Que());
            pstmt.setString(5, stCardNotSend.getS_PCode());
            pstmt.setString(6, stCardNotSend.getS_Stk());
            pstmt.setDouble(7, stCardNotSend.getS_In());
            pstmt.setDouble(8, stCardNotSend.getS_Out());
            pstmt.setDouble(9, stCardNotSend.getS_InCost());
            pstmt.setDouble(10, stCardNotSend.getS_OutCost());
            pstmt.setDouble(11, stCardNotSend.getS_ACost());
            pstmt.setString(12, stCardNotSend.getS_Rem());
            pstmt.setString(13, stCardNotSend.getS_User());
            pstmt.setString(14, stCardNotSend.getS_EntryDate());
            pstmt.setString(15, stCardNotSend.getS_EntryTime());
            pstmt.setString(16, stCardNotSend.getS_Link());
            pstmt.setString(17, branchCode);
            pstmt.setDouble(18, discount);
            pstmt.setDouble(19, nettotal);
            pstmt.setString(20, refund);
            pstmt.setString(21, refno);
            pstmt.setString(22, cashier);
            pstmt.setString(23, emp);
            pstmt.setDouble(24, unitPrice);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(ServerSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlServer.close();
        }

        return false;
    }
}
