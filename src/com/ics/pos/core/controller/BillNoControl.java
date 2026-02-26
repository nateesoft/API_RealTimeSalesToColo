package com.ics.pos.core.controller;

import util.ThaiUtil;
import com.ics.bean.BillNoBean;
import database.MySQLConnect;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BillNoControl {
    
    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public BillNoBean getData(String billNo) {
        BillNoBean billNoBean = null;
        
        try {
            mysqlLocal.open();
            String sql = "select * from billno where B_Refno=? limit 1";
            PreparedStatement pstmtSelectBillno = mysqlLocal.getConnection().prepareStatement(sql);
            pstmtSelectBillno.setString(1, billNo);

            try (ResultSet rs = pstmtSelectBillno.executeQuery()) {
                if (rs.next()) {
                    billNoBean = new BillNoBean();
                    billNoBean.setB_CuponName("");
                    billNoBean.setB_Refno(rs.getString("B_Refno"));
                    billNoBean.setB_CuponDiscAmt(rs.getFloat("B_CuponDiscAmt"));
                    billNoBean.setB_Ontime(rs.getString("B_Ontime"));
                    billNoBean.setB_LoginTime(rs.getString("B_LoginTime"));
                    billNoBean.setB_Table(rs.getString("B_Table"));
                    billNoBean.setB_MacNo(rs.getString("B_MacNo"));
                    billNoBean.setB_Cashier(rs.getString("B_Cashier"));
                    billNoBean.setB_Cust(rs.getInt("B_Cust"));
                    billNoBean.setB_ETD(rs.getString("B_ETD"));
                    billNoBean.setB_Total(rs.getFloat("B_Total"));
                    billNoBean.setB_Food(rs.getFloat("B_Food"));
                    billNoBean.setB_Drink(rs.getFloat("B_Drink"));
                    billNoBean.setB_Product(rs.getFloat("B_Product"));
                    billNoBean.setB_Service(rs.getFloat("B_Service"));
                    billNoBean.setB_ServiceAmt(rs.getFloat("B_ServiceAmt"));
                    billNoBean.setB_ItemDiscAmt(rs.getFloat("B_ItemDiscAmt"));
                    billNoBean.setB_FastDisc(rs.getString("B_FastDisc"));
                    billNoBean.setB_FastDiscAmt(rs.getFloat("B_FastDiscAmt"));
                    billNoBean.setB_EmpDisc(rs.getString("B_EmpDisc"));
                    billNoBean.setB_EmpDiscAmt(rs.getFloat("B_EmpDiscAmt"));
                    billNoBean.setB_TrainDisc(rs.getString("B_TrainDisc"));
                    billNoBean.setB_TrainDiscAmt(rs.getFloat("B_TrainDiscAmt"));
                    billNoBean.setB_MemDisc(rs.getString("B_MemDisc"));
                    billNoBean.setB_MemDiscAmt(rs.getFloat("B_MemDiscAmt"));
                    billNoBean.setB_SubDisc(rs.getString("B_SubDisc"));
                    billNoBean.setB_SubDiscAmt(rs.getFloat("B_SubDiscAmt"));
                    billNoBean.setB_SubDiscBath(rs.getFloat("B_SubDiscBath"));
                    billNoBean.setB_ProDiscAmt(rs.getFloat("B_ProDiscAmt"));
                    billNoBean.setB_SpaDiscAmt(rs.getFloat("B_SpaDiscAmt"));
                    billNoBean.setB_AdjAmt(rs.getFloat("B_AdjAmt"));
                    billNoBean.setB_NetTotal(rs.getFloat("B_NetTotal"));
                    billNoBean.setB_NetFood(rs.getFloat("B_NetFood"));
                    billNoBean.setB_NetDrink(rs.getFloat("B_NetDrink"));
                    billNoBean.setB_NetProduct(rs.getFloat("B_NetProduct"));
                    billNoBean.setB_NetVat(rs.getFloat("B_NetVat"));
                    billNoBean.setB_NetNonVat(rs.getFloat("B_NetNonVat"));
                    billNoBean.setB_Vat(rs.getFloat("B_Vat"));
                    billNoBean.setB_PayAmt(rs.getFloat("B_PayAmt"));
                    billNoBean.setB_Cash(rs.getFloat("B_Cash"));
                    billNoBean.setB_GiftVoucher(rs.getFloat("B_GiftVoucher"));
                    billNoBean.setB_Earnest(rs.getFloat("B_Earnest"));
                    billNoBean.setB_Ton(rs.getFloat("B_Ton"));
                    billNoBean.setB_CrCode1(rs.getString("B_CrCode1"));
                    billNoBean.setB_CardNo1(rs.getString("B_CardNo1"));
                    billNoBean.setB_AppCode1(rs.getString("B_AppCode1"));
                    billNoBean.setB_CrCharge1(rs.getFloat("B_CrCharge1"));
                    billNoBean.setB_CrChargeAmt1(rs.getFloat("B_CrChargeAmt1"));
                    billNoBean.setB_CrAmt1(rs.getFloat("B_CrAmt1"));
                    billNoBean.setB_AccrCode(rs.getString("B_AccrCode"));
                    billNoBean.setB_AccrAmt(rs.getFloat("B_AccrAmt"));
                    billNoBean.setB_AccrCr(rs.getInt("B_AccrCr"));
                    billNoBean.setB_MemCode(rs.getString("B_MemCode"));
                    billNoBean.setB_MemName(rs.getString("B_MemName"));
                    billNoBean.setB_MemCurSum(rs.getFloat("B_MemCurSum"));
                    billNoBean.setB_Void(rs.getString("B_Void"));
                    billNoBean.setB_VoidUser(rs.getString("B_VoidUser"));
                    billNoBean.setB_VoidTime(rs.getString("B_VoidTime"));
                    billNoBean.setB_BillCopy(rs.getInt("B_BillCopy"));
                    billNoBean.setB_PrnCnt(rs.getInt("B_PrnCnt"));
                    billNoBean.setB_PrnTime1(rs.getString("B_PrnTime1"));
                    billNoBean.setB_PrnTime2(rs.getString("B_PrnTime2"));
                    billNoBean.setB_InvNo(rs.getString("B_InvNo"));
                    billNoBean.setB_InvType(rs.getString("B_InvType"));
                    billNoBean.setB_Bran(rs.getString("B_Bran"));
                    billNoBean.setB_BranName(ThaiUtil.ASCII2Unicode(rs.getString("B_BranName")));
                    billNoBean.setB_Tel(rs.getString("B_Tel"));
                    billNoBean.setB_RecTime(rs.getString("B_RecTime"));
                    billNoBean.setMStamp(rs.getString("MStamp"));
                    billNoBean.setMScore(rs.getString("MScore"));
                    billNoBean.setCurStamp(rs.getString("CurStamp"));
                    billNoBean.setStampRate(rs.getString("StampRate"));
                    billNoBean.setB_ChkBill(rs.getString("B_ChkBill"));
                    billNoBean.setB_ChkBillTime(rs.getString("B_ChkBillTime"));
                    billNoBean.setB_CashTime(rs.getString("B_CashTime"));
                    billNoBean.setB_WaitTime(rs.getString("B_WaitTime"));
                    billNoBean.setB_SumScore(rs.getFloat("B_SumScore"));
                    billNoBean.setB_CrBank(rs.getString("B_CrBank"));
                    billNoBean.setB_CrCardAmt(rs.getFloat("B_CrCardAmt"));
                    billNoBean.setB_CrCurPoint(rs.getFloat("B_CrCurPoint"));
                    billNoBean.setB_CrSumPoint(rs.getFloat("B_CrSumPoint"));
                    billNoBean.setB_NetDiff(rs.getFloat("B_NetDiff"));
                    billNoBean.setB_OnDate(rs.getDate("B_OnDate"));
                    billNoBean.setB_PostDate(rs.getDate("B_PostDate"));
                    billNoBean.setB_MemBegin(rs.getDate("B_MemBegin"));
                    billNoBean.setB_MemEnd(rs.getDate("B_MemEnd"));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(BillNoControl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mysqlLocal.close();
        }

        return billNoBean;
    }

}
