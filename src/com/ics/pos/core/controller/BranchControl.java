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

    private BranchBean branchBean = null;

    public BranchBean getData() {
        MySQLConnect mysql = new MySQLConnect();
        try {
            mysql.open(BranchControl.class);
            String sql = "select * from branch limit 1";
            PreparedStatement psmtQuery = mysql.getConnection().prepareStatement(sql);
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
        } catch (SQLException ex) {
            Logger.getLogger(BranchControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mysql.closeConnection(BranchControl.class);
        }

        return branchBean;
    }

}
