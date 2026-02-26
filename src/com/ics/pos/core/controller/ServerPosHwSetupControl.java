/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ics.pos.core.controller;

import database.MySQLConnectWebOnline;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nateelive
 */
public class ServerPosHwSetupControl {
    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();
    
    public String updateTime(String receNo1, String Lastupdate, String LastTimeUpdate, String terminal, String branchCode) {
        try {
            mysqlServer.open();
            String sqlUpdateRecNo = "update poshwsetup "
                    + "set receno1='" + receNo1 + "' ,"
                    + "Lastupdate='" + Lastupdate + "' , LastTimeUpdate='" + LastTimeUpdate + "' "
                    + "where branch='" + branchCode + "' and terminal='" + terminal + "'";
            mysqlServer.getConnection().createStatement().executeUpdate(sqlUpdateRecNo);
        } catch (SQLException e) {
            Logger.getLogger(ServerPosHwSetupControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlServer.close();
        }

        return null;
    }
}
