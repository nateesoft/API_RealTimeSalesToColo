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
public class LocalProductControl {
    private final MySQLConnect mysqlLocal = new MySQLConnect();
    
    public double getProductPSCostByPCode(String pCode) {        
        try {
            mysqlLocal.open();
            String sql = "select pscost, pacost, plcost from product where pcode = ?";
            PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql);
            psmtQuery.setString(1, pCode);
            try (ResultSet rs = psmtQuery.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("pscost");
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalProductControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return 0.00;
    }
}
