package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import database.MySQLConnect;
import database.MySQLConnectWebOnline;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.AppLogUtil;

/**
 *
 * @author nateelive
 */
public class TranIOControl {

    private final MySQLConnect mysqlLocal = new MySQLConnect();
    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();
    private String Branch = "";

    public List<STCardBean> TiNDownload(String typeIO) {
        List<STCardBean> list = new ArrayList<>();
        try {
            mysqlServer.open();
            String conditionType = typeIO.contains("TRI") ? "TRI_HQ" : "TRO_HQ";
            String sql = "SELECT * FROM stcard "
                    + "WHERE s_rem=? AND Source_data='WEB' "
                    + "AND data_sync='N' AND s_bran=?";
            try (PreparedStatement ps = mysqlServer.getConnection().prepareStatement(sql)) {
                ps.setString(1, conditionType);
                ps.setString(2, Branch);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        STCardBean bean = new STCardBean();
                        bean.setS_Date(rs.getString("S_Date"));
                        bean.setS_No(rs.getString("S_No"));
                        bean.setS_Que(rs.getInt("S_Que"));
                        bean.setS_PCode(rs.getString("S_PCode"));
                        bean.setS_Stk(rs.getString("S_Stk"));
                        bean.setS_In(rs.getDouble("S_In"));
                        bean.setS_Out(rs.getDouble("S_Out"));
                        bean.setS_InCost(rs.getDouble("S_Incost"));
                        bean.setS_OutCost(rs.getDouble("S_Outcost"));
                        bean.setS_Rem(rs.getString("S_Rem"));
                        bean.setS_User(rs.getString("S_User"));
                        bean.setS_EntryDate(rs.getString("S_Entrydate"));
                        bean.setS_EntryTime(rs.getString("S_Entrytime"));
                        bean.setDataSync(rs.getString("Data_Sync"));
                        bean.setSource_Data(rs.getString("Source_Data"));
                        bean.setDiscount(rs.getDouble("Discount"));
                        bean.setNettotal(rs.getDouble("Nettotal"));
                        list.add(bean);
                    }
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlServer.close();
        }
        return list;
    }

    public void TIO_Process(String branch) {
        this.Branch = branch;
        List<STCardBean> beanI = TiNDownload("TRI");
        List<STCardBean> beanO = TiNDownload("TRO");
        mysqlLocal.open();
        mysqlServer.open();
        try {
            if (!beanI.isEmpty()) {
                Map<String, String> unitMap = loadProductUnits(beanI);
                batchInsertTranIn(beanI, unitMap);
                batchUpdateSyncStatus(beanI, "TRI_HQ");
                System.out.println("ดาวน์โหลดเอกสารรับเข้า กรุณายืนยันเอกสารในระบบคลังสินค้า");
            }
            if (!beanO.isEmpty()) {
                Map<String, String> unitMap = loadProductUnits(beanO);
                batchInsertTranOut(beanO, unitMap);
                batchUpdateSyncStatus(beanO, "TRO_HQ");
            }
            System.out.println("ดาวน์โหลดเอกสารสินค้าออกเรียบร้อย กรุณายืนยันเอกสารในระบบคลังสินค้า");
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
            mysqlServer.close();
        }
    }

    private Map<String, String> loadProductUnits(List<STCardBean> beans) throws SQLException {
        Map<String, String> unitMap = new HashMap<>();
        Set<String> pcodes = new HashSet<>();
        for (STCardBean bean : beans) {
            pcodes.add(bean.getS_PCode());
        }
        if (pcodes.isEmpty()) return unitMap;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < pcodes.size(); i++) {
            if (i > 0) placeholders.append(", ");
            placeholders.append("?");
        }
        String sql = "SELECT pcode, punit1 FROM product WHERE pcode IN (" + placeholders + ")";
        try (PreparedStatement ps = mysqlLocal.getConnection().prepareStatement(sql)) {
            int idx = 1;
            for (String pcode : pcodes) {
                ps.setString(idx++, pcode);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    unitMap.put(rs.getString("pcode"), rs.getString("punit1"));
                }
            }
        }
        return unitMap;
    }

    private void batchInsertTranIn(List<STCardBean> beans, Map<String, String> unitMap) throws SQLException {
        String sql = "INSERT INTO tranin (R_No, R_Que, R_Pcode, R_Stock, R_Pack, "
                + "R_Qty, R_Post, R_Unit, R_Cost, R_Amount, R_TotalQty, "
                + "R_User, R_Time, R_Entrydate, "
                + "R_SendFTP, R_Pqty, R_Order, R_Send, R_RefCode, R_SendInterface) "
                + "VALUES (?, ?, ?, ?, '1', ?, 'N', ?, ?, ?, '0', ?, ?, ?, 'N', '0', '0', '0.00', '', 'N')";
        try (PreparedStatement ps = mysqlLocal.getConnection().prepareStatement(sql)) {
            for (STCardBean bean : beans) {
                String unit = unitMap.getOrDefault(bean.getS_PCode(), "");
                double amount = bean.getS_In() * bean.getS_InCost();
                ps.setString(1, bean.getS_No());
                ps.setInt(2, bean.getS_Que());
                ps.setString(3, bean.getS_PCode());
                ps.setString(4, bean.getS_Stk());
                ps.setDouble(5, bean.getS_In());
                ps.setString(6, unit);
                ps.setDouble(7, bean.getS_InCost());
                ps.setDouble(8, amount);
                ps.setString(9, bean.getS_User());
                ps.setString(10, bean.getS_EntryTime());
                ps.setString(11, bean.getS_EntryDate());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void batchInsertTranOut(List<STCardBean> beans, Map<String, String> unitMap) throws SQLException {
        String sql = "INSERT INTO tranout (R_No, R_Que, R_Pcode, R_Stock, R_Pack, "
                + "R_Qty, R_Post, R_Unit, R_Cost, R_Amount, R_TotalQty, "
                + "R_User, R_Time, R_Entrydate) "
                + "VALUES (?, ?, ?, ?, '1', ?, 'N', ?, ?, ?, '0', ?, ?, ?)";
        try (PreparedStatement ps = mysqlLocal.getConnection().prepareStatement(sql)) {
            for (STCardBean bean : beans) {
                String unit = unitMap.getOrDefault(bean.getS_PCode(), "");
                double amount = bean.getS_In() * bean.getS_InCost();
                ps.setString(1, bean.getS_No());
                ps.setInt(2, bean.getS_Que());
                ps.setString(3, bean.getS_PCode());
                ps.setString(4, bean.getS_Stk());
                ps.setDouble(5, bean.getS_Out());
                ps.setString(6, unit);
                ps.setDouble(7, bean.getS_OutCost());
                ps.setDouble(8, amount);
                ps.setString(9, bean.getS_User());
                ps.setString(10, bean.getS_EntryTime());
                ps.setString(11, bean.getS_EntryDate());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void batchUpdateSyncStatus(List<STCardBean> beans, String remType) throws SQLException {
        String sql = "UPDATE stcard SET data_Sync='Y' "
                + "WHERE s_bran=? AND s_date=? AND s_no=? AND s_que=? AND s_pcode=? AND s_rem=? "
                + "AND s_entrydate=? AND s_entrytime=? AND s_user=? AND s_in=? AND s_out=? "
                + "AND s_outcost=? AND data_Sync='N'";
        try (PreparedStatement ps = mysqlServer.getConnection().prepareStatement(sql)) {
            for (STCardBean bean : beans) {
                ps.setString(1, Branch);
                ps.setString(2, bean.getS_Date());
                ps.setString(3, bean.getS_No());
                ps.setInt(4, bean.getS_Que());
                ps.setString(5, bean.getS_PCode());
                ps.setString(6, remType);
                ps.setString(7, bean.getS_EntryDate());
                ps.setString(8, bean.getS_EntryTime());
                ps.setString(9, bean.getS_User());
                ps.setDouble(10, bean.getS_In());
                ps.setDouble(11, bean.getS_Out());
                ps.setDouble(12, bean.getS_OutCost());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
