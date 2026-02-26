package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import database.MySQLConnect;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalSTCardControl {
    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public List<STCardBean> getListSTCardNotSend() {
        List<STCardBean> list = new ArrayList<>();
        try {
            mysqlLocal.open();
            String sql = "select * from stcard "
                    + "where s_send <> 'Y' and Source_Data <> 'WEB' "
                    + "order by s_date, s_no, s_pcode, s_entrytime";
            PreparedStatement psmtQuery = mysqlLocal.getConnection().prepareStatement(sql);
            try (ResultSet rs = psmtQuery.executeQuery()) {
                while (rs.next()) {
                    STCardBean bean = new STCardBean();
                    bean.setS_Date(rs.getString("s_date"));
                    bean.setS_No(rs.getString("s_no"));
                    bean.setS_SubNo(rs.getString("s_subno"));
                    bean.setS_Que(rs.getInt("s_que"));
                    bean.setS_PCode(rs.getString("s_pcode"));
                    bean.setS_Stk(rs.getString("s_stk"));
                    bean.setS_In(rs.getDouble("s_in"));
                    bean.setS_Out(rs.getDouble("s_out"));
                    bean.setS_InCost(rs.getDouble("s_incost"));
                    bean.setS_OutCost(rs.getDouble("s_outcost"));
                    bean.setS_ACost(rs.getDouble("s_aCost"));
                    bean.setS_Rem(rs.getString("s_rem"));
                    bean.setS_User(rs.getString("s_User"));
                    bean.setS_EntryDate(rs.getString("s_entrydate"));
                    bean.setS_EntryTime(rs.getString("s_entrytime"));
                    bean.setS_Link(rs.getString("s_link"));
                    bean.setS_Send(rs.getString("s_send"));
                    list.add(bean);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(LocalSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return list;
    }

    public boolean updateSendStatus(STCardBean stCardNotSend, String lastUpdate, String lastTimeUpdate) {
        try {
            mysqlLocal.open();
            String sql = "update stcard set s_send='Y', LastUpdate=?, LastTimeUpdate=? "
                    + "where s_pcode=? and s_date=? and s_entrytime=? "
                    + "and s_rem=? and s_user=? and s_send='N' and s_no=?";
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            pstmt.setString(1, lastUpdate);
            pstmt.setString(2, lastTimeUpdate);
            pstmt.setString(3, stCardNotSend.getS_PCode());
            pstmt.setString(4, stCardNotSend.getS_Date());
            pstmt.setString(5, stCardNotSend.getS_EntryTime());
            pstmt.setString(6, stCardNotSend.getS_Rem());
            pstmt.setString(7, stCardNotSend.getS_User());
            pstmt.setString(8, stCardNotSend.getS_No());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(LocalSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return false;
    }

    public boolean updateSendStatusDone(STCardBean stCardNotSend, String lastUpdate, String lastTimeUpdate) {
        try {
            mysqlLocal.open();
            String sql = "update stcard set s_send='Y', LastUpdate=?, LastTimeUpdate=? "
                    + "where s_pcode=? and s_date=? and s_entrytime=? "
                    + "and s_rem=? and s_user=? and s_send='N' and s_no=? "
                    + "and s_incost=? and s_in=? and s_out=? and s_outcost=? and s_que=?";
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            pstmt.setString(1, lastUpdate);
            pstmt.setString(2, lastTimeUpdate);
            pstmt.setString(3, stCardNotSend.getS_PCode());
            pstmt.setString(4, stCardNotSend.getS_Date());
            pstmt.setString(5, stCardNotSend.getS_EntryTime());
            pstmt.setString(6, stCardNotSend.getS_Rem());
            pstmt.setString(7, stCardNotSend.getS_User());
            pstmt.setString(8, stCardNotSend.getS_No());
            pstmt.setDouble(9, stCardNotSend.getS_InCost());
            pstmt.setDouble(10, stCardNotSend.getS_In());
            pstmt.setDouble(11, stCardNotSend.getS_Out());
            pstmt.setDouble(12, stCardNotSend.getS_OutCost());
            pstmt.setInt(13, stCardNotSend.getS_Que());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(LocalSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return false;
    }
}
