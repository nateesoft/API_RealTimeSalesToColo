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
public class LocalPosHwSetupControl {
    private final MySQLConnect mysqlLocal = new MySQLConnect();
    
    public String getReceNo1ByTerminal(String terminal) {
        try {
            mysqlLocal.open();
            String sql = "select ReceNo1 from poshwsetup where terminal=?";
            try (PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql)) {
                psmtQuery.setString(1, terminal);
                try (ResultSet rs = psmtQuery.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("ReceNo1");
                    }
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return null;
    }
}
