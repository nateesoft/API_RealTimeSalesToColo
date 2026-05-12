package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import database.MySQLConnectWebOnline;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerSTCardControl {

    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();

    private static final int BATCH_CHUNK_SIZE = 100;
    private static final String INSERT_SQL = "insert into stcard ("
            + "s_date, s_no, s_subno, s_que, s_pcode, s_stk, "
            + "s_in, s_out, s_incost, s_outcost, s_acost, s_rem, "
            + "s_user, s_entrydate, s_entrytime, s_link, s_bran, "
            + "discount, nettotal, refund, refno, cashier, emp, UnitPrice) "
            + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static class STCardUploadParam {
        public final STCardBean bean;
        public final double discount;
        public final double nettotal;
        public final String refund;
        public final String refno;
        public final String cashier;
        public final String emp;
        public final double unitPrice;
        public final String branchCode;
        public final boolean isSalType;

        public STCardUploadParam(STCardBean bean, double discount, double nettotal,
                String refund, String refno, String cashier, String emp,
                double unitPrice, String branchCode, boolean isSalType) {
            this.bean = bean;
            this.discount = discount;
            this.nettotal = nettotal;
            this.refund = refund;
            this.refno = refno;
            this.cashier = cashier;
            this.emp = emp;
            this.unitPrice = unitPrice;
            this.branchCode = branchCode;
            this.isSalType = isSalType;
        }
    }

    public boolean[] saveSTCardBatch(List<STCardUploadParam> params) {
        boolean[] results = new boolean[params.size()];
        if (params.isEmpty()) {
            return results;
        }

        try {
            mysqlServer.open();
            Connection conn = mysqlServer.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL);
            int chunkStart = 0;

            for (int i = 0; i < params.size(); i++) {
                STCardUploadParam p = params.get(i);
                STCardBean bean = p.bean;
                pstmt.setString(1, bean.getS_Date());
                pstmt.setString(2, bean.getS_No());
                pstmt.setString(3, bean.getS_SubNo());
                pstmt.setInt(4, bean.getS_Que());
                pstmt.setString(5, bean.getS_PCode());
                pstmt.setString(6, bean.getS_Stk());
                pstmt.setDouble(7, bean.getS_In());
                pstmt.setDouble(8, bean.getS_Out());
                pstmt.setDouble(9, bean.getS_InCost());
                pstmt.setDouble(10, bean.getS_OutCost());
                pstmt.setDouble(11, bean.getS_ACost());
                pstmt.setString(12, bean.getS_Rem());
                pstmt.setString(13, bean.getS_User());
                pstmt.setString(14, bean.getS_EntryDate());
                pstmt.setString(15, bean.getS_EntryTime());
                pstmt.setString(16, bean.getS_Link());
                pstmt.setString(17, p.branchCode);
                pstmt.setDouble(18, p.discount);
                pstmt.setDouble(19, p.nettotal);
                pstmt.setString(20, p.refund);
                pstmt.setString(21, p.refno);
                pstmt.setString(22, p.cashier);
                pstmt.setString(23, p.emp);
                pstmt.setDouble(24, p.unitPrice);
                pstmt.addBatch();

                boolean flushNow = ((i - chunkStart + 1) >= BATCH_CHUNK_SIZE) || (i == params.size() - 1);
                if (flushNow) {
                    try {
                        int[] batchResults = pstmt.executeBatch();
                        conn.commit();
                        for (int j = 0; j < batchResults.length; j++) {
                            results[chunkStart + j] = batchResults[j] != Statement.EXECUTE_FAILED;
                        }
                    } catch (BatchUpdateException bue) {
                        conn.rollback();
                        // all rows in this chunk are rolled back → leave results[chunkStart..i] as false
                        Logger.getLogger(ServerSTCardControl.class.getName()).log(Level.SEVERE, null, bue);
                    }
                    pstmt.clearBatch();
                    chunkStart = i + 1;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(ServerSTCardControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlServer.close();
        }

        return results;
    }
}
