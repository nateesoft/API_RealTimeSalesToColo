package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import database.MySQLConnect;
import database.MySQLConnectWebOnline;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import util.AppLogUtil;
import util.DateConvert;

/**
 *
 * @author nateelive
 */
public class LocalSTranControl {

    private final MySQLConnect mysqlLocal = new MySQLConnect();
    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();

    public STCardBean getDataByMacnoTimeDatePluCode(String macno, String r_time, String s_PCode, String s_Date,
            String refno, String r_quan, String r_nettotal, String checkFirstDigitSNo, String s_No) {

        STCardBean bean = null;

        try {
            mysqlLocal.open();
            PreparedStatement pstmt;

            if (r_time != null) {
                String sql = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                        + "r_refund, cashier Cashier, r_emp R_Emp, r_time, r_quan, r_plucode, r_void "
                        + "FROM s_tran "
                        + "WHERE macno=? AND r_time=? AND r_plucode=? AND r_date=? LIMIT 1";
                pstmt = mysqlLocal.getConnection().prepareStatement(sql);
                pstmt.setString(1, macno);
                pstmt.setString(2, r_time);
                pstmt.setString(3, s_PCode);
                pstmt.setString(4, s_Date);
            } else if (refno != null && r_quan != null && r_nettotal != null) {
                String sql = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                        + "r_refund, cashier Cashier, r_emp R_Emp, r_time, r_quan, r_plucode "
                        + "FROM s_tran "
                        + "WHERE macno=? AND r_refno=? AND r_plucode=? AND r_date=? "
                        + "AND r_quan=? AND r_nettotal=? LIMIT 1";
                pstmt = mysqlLocal.getConnection().prepareStatement(sql);
                pstmt.setString(1, macno);
                pstmt.setString(2, refno);
                pstmt.setString(3, s_PCode);
                pstmt.setString(4, s_Date);
                pstmt.setString(5, r_quan);
                pstmt.setString(6, r_nettotal);
            } else {
                return null;
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bean = new STCardBean();
                    bean.setDiscount(rs.getDouble("R_Total") - rs.getDouble("R_Nettotal"));
                    bean.setNettotal(rs.getDouble("R_Nettotal"));
                    bean.setRefund(rs.getString("R_Refund"));

                    if (checkFirstDigitSNo.equals("R")) {
                        bean.setRefNo(s_No);
                        bean.setNettotal(rs.getDouble("R_Nettotal") * rs.getDouble("r_quan") * -1);
                        bean.setDiscount(bean.getDiscount() * rs.getDouble("r_quan") * -1);
                    } else {
                        bean.setRefNo(rs.getString("R_Refno"));
                    }

                    bean.setCashier(rs.getString("Cashier"));
                    bean.setEmp(rs.getString("R_Emp"));
                    if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                        bean.setNettotal(0);
                    }
                    bean.setR_time(rs.getString("r_time"));
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return bean;
    }

    public STCardBean getDataByMacnoRefnoPluCodeRDateFlagVoid(String macno, String refno, String s_PCode, String s_Date,
            String checkFirstDigitSNo, String s_No) {
        STCardBean bean = null;

        try {
            mysqlLocal.open();

            String sql = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                    + "r_refund, cashier Cashier, r_emp R_Emp, r_time, r_void "
                    + "FROM s_tran "
                    + "WHERE macno=? AND r_refno=? "
                    + "AND r_plucode=? AND r_date=? AND r_void='V' "
                    + "LIMIT 1";
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            pstmt.setString(1, macno);
            pstmt.setString(2, refno);
            pstmt.setString(3, s_PCode);
            pstmt.setString(4, s_Date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = new STCardBean();
                bean.setDiscount((rs.getDouble("R_Total") - rs.getDouble("R_Nettotal")) * -1);
                if (rs.getDouble("R_Nettotal") != 0) {
                    bean.setNettotal(rs.getDouble("R_Nettotal") * -1);
                } else {
                    bean.setNettotal(rs.getDouble("R_Nettotal"));
                }

                bean.setRefund(rs.getString("R_Refund"));
                if (checkFirstDigitSNo.equals("R")) {
                    bean.setRefNo(s_No);
                } else {
                    bean.setRefNo(rs.getString("R_Refno"));
                }

                bean.setCashier(rs.getString("Cashier"));
                bean.setEmp(rs.getString("R_Emp"));

                if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                    bean.setNettotal(0);
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return bean;
    }

    public STCardBean getDataByCondition(String macno, String refno, String s_PCode,
            String s_Date, String r_time, Boolean isVoid,
            String checkFirstDigitSNo, String s_No) {
        String sql = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                + "r_refund, cashier Cashier, r_emp R_Emp, r_time, r_void "
                + "FROM s_tran WHERE macno=? AND r_time=? AND r_plucode=? AND r_date=? ";

        List<String> params = new ArrayList<>();
        params.add(macno);
        params.add(r_time);
        params.add(s_PCode);
        params.add(s_Date);

        if (isVoid != null) {
            if (refno != null) {
                sql += " AND r_refno=? ";
                params.add(refno);
            }
            if (Boolean.TRUE.equals(isVoid)) {
                sql += " AND r_void='V' ";
            } else {
                sql += " AND r_void<>'V' ";
            }
        }
        sql += " LIMIT 1";

        STCardBean bean = null;
        try {
            mysqlLocal.open();
            PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bean = new STCardBean();
                    bean.setDiscount(rs.getDouble("R_Total") - rs.getDouble("R_Nettotal"));
                    bean.setNettotal(rs.getDouble("R_Nettotal"));
                    bean.setRefund(rs.getString("R_Refund"));
                    if (checkFirstDigitSNo.equals("R")) {
                        bean.setRefNo(s_No);
                    } else {
                        bean.setRefNo(rs.getString("R_Refno"));
                    }
                    bean.setCashier(rs.getString("Cashier"));
                    bean.setEmp(rs.getString("R_Emp"));
                    if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                        bean.setNettotal(0);
                    }
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return bean;
    }

    public List<STCardBean> getSTranTransaction() {
        List<STCardBean> listBean = new ArrayList<>();

        try {
            mysqlLocal.open();

            String sql = "select * from s_tran where r_send='N' order by r_refno, r_index;";
            try (ResultSet rs = mysqlLocal.getConnection().createStatement().executeQuery(sql)) {
                while (rs.next()) {
                    STCardBean bean = new STCardBean();
                    bean.setS_Date(rs.getString("R_Date"));
                    bean.setS_No(rs.getString("R_Refno") + "/" + rs.getString("R_Time"));
                    bean.setS_Que(0);
                    bean.setS_PCode(rs.getString("R_Plucode"));
                    bean.setS_Stk("A1");
                    bean.setS_In(0);
                    bean.setS_Out(rs.getInt("R_Quan"));
                    bean.setS_InCost(0);
                    bean.setS_OutCost(rs.getInt("R_Total"));
                    bean.setS_ACost(0);
                    bean.setS_Rem("SAL");
                    bean.setS_User(rs.getString("Cashier"));
                    bean.setS_EntryDate(rs.getString("R_Date"));
                    bean.setR_time(rs.getString("R_time"));
                    bean.setS_Link("");
                    bean.setSource_Data("POS");
                    bean.setDataSync("N");

                    double discount = Math.abs(rs.getDouble("R_Nettotal") - rs.getDouble("R_Total"));
                    bean.setDiscount(discount);

                    bean.setNettotal(rs.getDouble("R_Nettotal"));
                    bean.setRefund(rs.getString("R_Refund"));
                    bean.setRefNo(rs.getString("R_Refno"));
                    bean.setCashier(rs.getString("Cashier"));
                    bean.setEmp(rs.getString("R_Emp"));
                    bean.setUnitPrice(rs.getDouble("R_Price"));
                    bean.setR_index(rs.getString("R_Index"));
                    bean.setS_EntryDate(rs.getString("R_Date"));
                    bean.setS_EntryTime(rs.getString("R_Time"));
                    bean.setS_Stk(rs.getString("STKCode"));

                    bean.setTableUpdate("s_tran");
                    listBean.add(bean);
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return listBean;
    }

    public List<STCardBean> getTransaction15DayAgo(String branch) {
        List<STCardBean> listBean = new ArrayList<>();
        DateConvert dateConvert = new DateConvert();
        String dateFrom = dateConvert.minusDate(dateConvert.GetCurrentDate(), 15);
        String dateTo = dateConvert.minusDate(dateConvert.GetCurrentDate(), 1);

        Set<String> syncedKeys = loadSyncedKeys(branch, dateFrom, dateTo);

        try {
            int i = 0;
            mysqlLocal.open();
            String sql = "SELECT * FROM s_tran WHERE r_date BETWEEN ? AND ? ORDER BY r_refno, r_index";
            try (PreparedStatement ps = mysqlLocal.getConnection().prepareStatement(sql)) {
                ps.setString(1, dateFrom);
                ps.setString(2, dateTo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String refno = rs.getString("R_Refno");
                        String rIndex = rs.getString("R_Index");
                        String rDate = rs.getString("R_Date");

                        if (syncedKeys.contains(refno + "|" + rIndex + "|" + rDate)) {
                            continue;
                        }

                        STCardBean bean = new STCardBean();
                        bean.setS_Date(rDate);
                        bean.setS_No(refno + "/" + rs.getString("R_Time"));
                        bean.setS_Que(0);
                        bean.setS_PCode(rs.getString("R_Plucode"));
                        bean.setS_Stk("A1");
                        bean.setS_In(0);
                        bean.setS_Out(rs.getInt("R_Quan"));
                        bean.setS_InCost(0);
                        bean.setS_OutCost(rs.getInt("R_Total"));
                        bean.setS_ACost(0);
                        bean.setS_Rem("SAL");
                        bean.setS_User(rs.getString("Cashier"));
                        bean.setS_EntryDate(rDate);
                        bean.setR_time(rs.getString("R_time"));
                        bean.setS_Link("");
                        bean.setSource_Data("POS");
                        bean.setDataSync("N");

                        double discount = Math.abs(rs.getDouble("R_Nettotal") - rs.getDouble("R_Total"));
                        bean.setDiscount(discount);

                        bean.setNettotal(rs.getDouble("R_Nettotal"));
                        bean.setRefund(rs.getString("R_Refund"));
                        bean.setRefNo(refno);
                        bean.setCashier(rs.getString("Cashier"));
                        bean.setEmp(rs.getString("R_Emp"));
                        bean.setUnitPrice(rs.getDouble("R_Price"));
                        bean.setR_index(rIndex);
                        bean.setS_EntryDate(rDate);
                        bean.setS_EntryTime(rs.getString("R_Time"));
                        bean.setS_Stk(rs.getString("STKCode"));

                        bean.setTableUpdate("s_tran");
                        listBean.add(bean);
                        System.out.println("Processing .... Add Bean Array " + (++i));
                    }
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return listBean;
    }

    private Set<String> loadSyncedKeys(String branch, String dateFrom, String dateTo) {
        Set<String> keys = new HashSet<>();
        mysqlServer.open();
        try {
            String sql = "SELECT refno, r_index, s_date FROM stcard "
                    + "WHERE s_bran = ? AND s_rem = 'SAL' AND s_date BETWEEN ? AND ?";
            try (PreparedStatement ps = mysqlServer.getConnection().prepareStatement(sql)) {
                ps.setString(1, branch);
                ps.setString(2, dateFrom);
                ps.setString(3, dateTo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        keys.add(rs.getString("refno") + "|"
                               + rs.getString("r_index") + "|"
                               + rs.getString("s_date"));
                    }
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlServer.close();
        }
        return keys;
    }
}
