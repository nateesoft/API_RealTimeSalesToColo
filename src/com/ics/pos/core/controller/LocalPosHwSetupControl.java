/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ics.pos.core.controller;

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
public class LocalPosHwSetupControl {
    private final MySQLConnect mysqlLocal = new MySQLConnect();
    
    public String getReceNo1ByTerminal(String terminal) {
        try {
            mysqlLocal.open();
            String sql = "select ReceNo1 from poshwsetup where terminal=? limit 1";
            PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql);
            psmtQuery.setString(1, terminal);
            try (ResultSet rs = psmtQuery.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ReceNo1");
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalPosHwSetupControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return null;
    }
}
