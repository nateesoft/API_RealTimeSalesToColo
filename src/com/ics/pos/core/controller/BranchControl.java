package com.ics.pos.core.controller;

import util.ThaiUtil;
import com.ics.bean.BranchBean;
import database.MySQLConnect;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.AppLogUtil;

public class BranchControl {

    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public BranchBean getData(String branchFix, String branchFixDefault) {
        BranchBean branchBean = null;
        try {
            mysqlLocal.open();
            String sql = "select * from branch";
            try (PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql); ResultSet rs = psmtQuery.executeQuery()) {
                if (rs.next()) {
                    branchBean = new BranchBean();
                    String bCheck;
                    bCheck = rs.getString("Code");
                    if (bCheck.equals(branchFix)) {
                        branchBean.setCode(branchFixDefault);
                    } else {
                        branchBean.setCode(rs.getString("Code"));
                    }
                    branchBean.setName(ThaiUtil.ASCII2Unicode(rs.getString("Name")));
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return branchBean;
    }

}
