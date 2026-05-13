package com.ics.pos.core.controller;

import database.MySQLConnect;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.AppLogUtil;

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
            try (PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql)) {
                psmtQuery.setString(1, pCode);
                try (ResultSet rs = psmtQuery.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("pscost");
                    }
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return 0.00;
    }
}
