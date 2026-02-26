package com.ics.pos.core.controller;

import database.MySQLConnectWebOnline;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerPosHwSetupControl {
    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();

    public void updateTime(String receNo1, String lastupdate, String lastTimeUpdate, String terminal, String branchCode) {
        try {
            mysqlServer.open();
            String sql = "update poshwsetup set receno1=?, Lastupdate=?, LastTimeUpdate=? "
                    + "where branch=? and terminal=?";
            PreparedStatement pstmt = mysqlServer.getConnection().prepareStatement(sql);
            pstmt.setString(1, receNo1);
            pstmt.setString(2, lastupdate);
            pstmt.setString(3, lastTimeUpdate);
            pstmt.setString(4, branchCode);
            pstmt.setString(5, terminal);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(ServerPosHwSetupControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlServer.close();
        }
    }
}
