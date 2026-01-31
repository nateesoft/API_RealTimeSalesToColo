package com.ics.pos.core.controller;

import util.ThaiUtil;
import com.ics.bean.BillNoBean;
import database.MySQLConnect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import util.AppLogUtil;
import util.MSG;

public class BillControl {
    
    public BillNoBean getData(String billNo) {
        BillNoBean bean = new BillNoBean();
        MySQLConnect mysql = new MySQLConnect();
        mysql.open(BillControl.class);
        try {
            String sql = "select * from billno where B_Refno='" + billNo + "' limit 1";
            Statement stmt = mysql.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String sql1 = "select t.r_prcutype cutype,t.r_prcucode cucode,c.cuname CuName "
                        + "from t_sale t "
                        + "inner join cupon c "
                        + "on t.r_prcucode = c.cucode "
                        + "where r_refno='" + billNo + "' limit 1";
                Statement stmt2 = mysql.getConnection().createStatement();
                ResultSet rs1 = stmt2.executeQuery(sql1);
                if (rs1.next()) {
                    bean.setB_CuponName(rs1.getString(ThaiUtil.ASCII2Unicode("CuName")));
                } else {
                    bean.setB_CuponName("");
                }
                bean.setB_Refno(rs.getString("B_Refno"));
                bean.setB_CuponDiscAmt(rs.getFloat("B_CuponDiscAmt"));
                bean.setB_Ontime(rs.getString("B_Ontime"));
                bean.setB_LoginTime(rs.getString("B_LoginTime"));
                bean.setB_Table(rs.getString("B_Table"));
                bean.setB_MacNo(rs.getString("B_MacNo"));
                bean.setB_Cashier(rs.getString("B_Cashier"));
                bean.setB_Cust(rs.getInt("B_Cust"));
                bean.setB_ETD(rs.getString("B_ETD"));
                bean.setB_Total(rs.getFloat("B_Total"));
                bean.setB_Food(rs.getFloat("B_Food"));
                bean.setB_Drink(rs.getFloat("B_Drink"));
                bean.setB_Product(rs.getFloat("B_Product"));
                bean.setB_Service(rs.getFloat("B_Service"));
                bean.setB_ServiceAmt(rs.getFloat("B_ServiceAmt"));
                bean.setB_ItemDiscAmt(rs.getFloat("B_ItemDiscAmt"));
                bean.setB_FastDisc(rs.getString("B_FastDisc"));
                bean.setB_FastDiscAmt(rs.getFloat("B_FastDiscAmt"));
                bean.setB_EmpDisc(rs.getString("B_EmpDisc"));
                bean.setB_EmpDiscAmt(rs.getFloat("B_EmpDiscAmt"));
                bean.setB_TrainDisc(rs.getString("B_TrainDisc"));
                bean.setB_TrainDiscAmt(rs.getFloat("B_TrainDiscAmt"));
                bean.setB_MemDisc(rs.getString("B_MemDisc"));
                bean.setB_MemDiscAmt(rs.getFloat("B_MemDiscAmt"));
                bean.setB_SubDisc(rs.getString("B_SubDisc"));
                bean.setB_SubDiscAmt(rs.getFloat("B_SubDiscAmt"));
                bean.setB_SubDiscBath(rs.getFloat("B_SubDiscBath"));
                bean.setB_ProDiscAmt(rs.getFloat("B_ProDiscAmt"));
                bean.setB_SpaDiscAmt(rs.getFloat("B_SpaDiscAmt"));
                bean.setB_AdjAmt(rs.getFloat("B_AdjAmt"));
                bean.setB_NetTotal(rs.getFloat("B_NetTotal"));
                bean.setB_NetFood(rs.getFloat("B_NetFood"));
                bean.setB_NetDrink(rs.getFloat("B_NetDrink"));
                bean.setB_NetProduct(rs.getFloat("B_NetProduct"));
                bean.setB_NetVat(rs.getFloat("B_NetVat"));
                bean.setB_NetNonVat(rs.getFloat("B_NetNonVat"));
                bean.setB_Vat(rs.getFloat("B_Vat"));
                bean.setB_PayAmt(rs.getFloat("B_PayAmt"));
                bean.setB_Cash(rs.getFloat("B_Cash"));
                bean.setB_GiftVoucher(rs.getFloat("B_GiftVoucher"));
                bean.setB_Earnest(rs.getFloat("B_Earnest"));
                bean.setB_Ton(rs.getFloat("B_Ton"));
                bean.setB_CrCode1(rs.getString("B_CrCode1"));
                bean.setB_CardNo1(rs.getString("B_CardNo1"));
                bean.setB_AppCode1(rs.getString("B_AppCode1"));
                bean.setB_CrCharge1(rs.getFloat("B_CrCharge1"));
                bean.setB_CrChargeAmt1(rs.getFloat("B_CrChargeAmt1"));
                bean.setB_CrAmt1(rs.getFloat("B_CrAmt1"));
                bean.setB_AccrCode(rs.getString("B_AccrCode"));
                bean.setB_AccrAmt(rs.getFloat("B_AccrAmt"));
                bean.setB_AccrCr(rs.getInt("B_AccrCr"));
                bean.setB_MemCode(rs.getString("B_MemCode"));
                bean.setB_MemName(rs.getString("B_MemName"));
                bean.setB_MemCurSum(rs.getFloat("B_MemCurSum"));
                bean.setB_Void(rs.getString("B_Void"));
                bean.setB_VoidUser(rs.getString("B_VoidUser"));
                bean.setB_VoidTime(rs.getString("B_VoidTime"));
                bean.setB_BillCopy(rs.getInt("B_BillCopy"));
                bean.setB_PrnCnt(rs.getInt("B_PrnCnt"));
                bean.setB_PrnTime1(rs.getString("B_PrnTime1"));
                bean.setB_PrnTime2(rs.getString("B_PrnTime2"));
                bean.setB_InvNo(rs.getString("B_InvNo"));
                bean.setB_InvType(rs.getString("B_InvType"));
                bean.setB_Bran(rs.getString("B_Bran"));
                bean.setB_BranName(ThaiUtil.ASCII2Unicode(rs.getString("B_BranName")));
                bean.setB_Tel(rs.getString("B_Tel"));
                bean.setB_RecTime(rs.getString("B_RecTime"));
                bean.setMStamp(rs.getString("MStamp"));
                bean.setMScore(rs.getString("MScore"));
                bean.setCurStamp(rs.getString("CurStamp"));
                bean.setStampRate(rs.getString("StampRate"));
                bean.setB_ChkBill(rs.getString("B_ChkBill"));
                bean.setB_ChkBillTime(rs.getString("B_ChkBillTime"));
                bean.setB_CashTime(rs.getString("B_CashTime"));
                bean.setB_WaitTime(rs.getString("B_WaitTime"));
                bean.setB_SumScore(rs.getFloat("B_SumScore"));
                bean.setB_CrBank(rs.getString("B_CrBank"));
                bean.setB_CrCardAmt(rs.getFloat("B_CrCardAmt"));
                bean.setB_CrCurPoint(rs.getFloat("B_CrCurPoint"));
                bean.setB_CrSumPoint(rs.getFloat("B_CrSumPoint"));
//                bean.setB_KicQue(rs.getString("B_KicQue"));
                try {
                    bean.setB_NetDiff(rs.getFloat("B_NetDiff"));
                } catch (Exception e) {
                    bean.setB_NetDiff(0);
                }

                try {
                    bean.setB_OnDate(rs.getDate("B_OnDate"));
                    bean.setB_PostDate(rs.getDate("B_PostDate"));
                    bean.setB_MemBegin(rs.getDate("B_MemBegin"));
                    bean.setB_MemEnd(rs.getDate("B_MemEnd"));
                } catch (SQLException e) {
                    MSG.ERR(null, e.getMessage());
                    AppLogUtil.log(BillControl.class, "error", e);
                }

                rs1.close();
                stmt2.close();
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            MSG.ERR(null, e.getMessage());
            AppLogUtil.log(BillControl.class, "error", e);
        } finally {
            mysql.closeConnection(this.getClass());
        }

        return bean;
    }

}
