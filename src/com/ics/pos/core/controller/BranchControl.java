package com.ics.pos.core.controller;

import util.ThaiUtil;
import com.ics.bean.BranchBean;
import database.MySQLConnect;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BranchControl {

    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public BranchBean getData() {

        BranchBean branchBean = null;
        try {
            mysqlLocal.open();
            String sql = "select * from branch limit 1";
            PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql);
            try (ResultSet rs = psmtQuery.executeQuery()) {
                if (rs.next()) {
                    branchBean = new BranchBean();
                    String bCheck;
                    bCheck = rs.getString("Code");
                    if (bCheck.equals("999")) {
                        branchBean.setCode("sss");
                    } else {
                        branchBean.setCode(rs.getString("Code"));
                    }
                    branchBean.setName(ThaiUtil.ASCII2Unicode(rs.getString("Name")));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(BranchControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return branchBean;
    }

}
