package com.ics.pos.core.controller;

import util.ThaiUtil;
import com.ics.bean.BranchBean;
import database.MySQLConnect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BranchControl {

    private static BranchBean branchBean = null;

    public static BranchBean getData() {
        System.out.println("Into Method BranchBean getData()");
        if (branchBean != null) {
            return branchBean;
        }
        MySQLConnect mysql = new MySQLConnect();
        try {
            mysql.open(BranchControl.class);
            String sql = "select * from branch limit 1";
            try (ResultSet rs = mysql.getConnection().createStatement().executeQuery(sql)) {
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
                    branchBean.setAddressNo(rs.getString("AddressNo"));
                    branchBean.setLocality(rs.getString("Locality"));
                    branchBean.setSubProvince(rs.getString("SubProvince"));
                    branchBean.setProvince(rs.getString("Province"));
                    branchBean.setPost(rs.getString("Post"));
                    branchBean.setTel_No(rs.getString("Tel_No"));
                    branchBean.setFax_No(rs.getString("Fax_No"));
                    branchBean.setE_Mail(rs.getString("E_Mail"));
                    branchBean.setManager(rs.getString("Manager"));
                    branchBean.setLocation_Area(rs.getString("Location_Area"));
                    branchBean.setSer_Area(rs.getFloat("Ser_Area"));
                    branchBean.setCou_Area(rs.getFloat("Cou_Area"));
                    branchBean.setKic_Area(rs.getFloat("Kic_Area"));
                    branchBean.setTot_Area(rs.getFloat("Tot_Area"));
                    branchBean.setCost(rs.getFloat("Cost"));
                    branchBean.setCharge(rs.getFloat("Charge"));
                    branchBean.setFlageCost(rs.getString("FlageCost"));
                    branchBean.setGp(rs.getFloat("Gp"));
                    branchBean.setFlageGp(rs.getString("FlageGp"));
                    branchBean.setRemark(rs.getString("Remark"));
                    branchBean.setArBillNo(rs.getFloat("ArBillNo"));
                    branchBean.setEarneatBillNo(rs.getFloat("EarneatBillNo"));
                    branchBean.setReturnBillNo(rs.getFloat("ReturnBillNo"));
                    try {
                        branchBean.setPrintAutoSumDate(rs.getDate("PrintAutoSumDate"));
                    } catch (SQLException e) {
                        System.out.println("Error Date: " + e.getMessage());
                    }
                    branchBean.setSaveOrder(rs.getString("SaveOrder"));
                    branchBean.setSaveOrderCopy(rs.getString("SaveOrderCopy"));
                    branchBean.setSaveOrderChk(rs.getString("SaveOrderChk"));
                    branchBean.setKIC1(rs.getString("KIC1"));
                    branchBean.setKIC2(rs.getString("KIC2"));
                    branchBean.setKIC3(rs.getString("KIC3"));
                    branchBean.setKIC4(rs.getString("KIC4"));
                    branchBean.setKIC5(rs.getString("KIC5"));
                    branchBean.setKIC6(rs.getString("KIC6"));
                    branchBean.setKIC7(rs.getString("KIC7"));
                    branchBean.setKIC8(rs.getString("KIC8"));
                    branchBean.setKIC9(rs.getString("KIC9"));
                    branchBean.setKIC10(rs.getString("KIC10"));
                    branchBean.setKIC11(rs.getString("KIC11"));
                    branchBean.setKIC12(rs.getString("KIC12"));
                    branchBean.setKIC13(rs.getString("KIC13"));
                    branchBean.setKIC14(rs.getString("KIC14"));
                    branchBean.setKIC15(rs.getString("KIC15"));
                    branchBean.setKIC16(rs.getString("KIC16"));
                    branchBean.setKIC17(rs.getString("KIC17"));
                    branchBean.setKIC18(rs.getString("KIC18"));
                    branchBean.setKIC19(rs.getString("KIC19"));
                    branchBean.setKIC20(rs.getString("KIC20"));

                    branchBean.setSmartCard(rs.getString("SmartCard"));
                    branchBean.setGetFile(rs.getString("GetFile"));
                    branchBean.setRetFile(rs.getString("RetFile"));
                    branchBean.setPointFile(rs.getString("PointFile"));
                    branchBean.setCntLoop(rs.getInt("CntLoop"));
                    branchBean.setInvNo(rs.getFloat("InvNo"));
                    branchBean.setInvCashNo(rs.getFloat("InvCashNo"));
                    branchBean.setInvCash(rs.getFloat("InvCash"));
                    branchBean.setInvActive(rs.getString("InvActive"));
                    branchBean.setCreditAct(rs.getString("CreditAct"));
                    branchBean.setPromotionGP(rs.getString("PromotionGP"));
                    branchBean.setLockTime(rs.getInt("LockTime"));
                    branchBean.setKicItemNo(rs.getInt("KicItemNo"));
                    branchBean.setPT1(rs.getString("PT1"));
                    branchBean.setPT2(rs.getString("PT2"));
                    branchBean.setPT3(rs.getString("PT3"));
                    branchBean.setPT4(rs.getString("PT4"));
                    branchBean.setPT5(rs.getString("PT5"));
                    branchBean.setPONO(rs.getInt("PONO"));
                    branchBean.setPrintKicForm(rs.getString("PrintKicForm"));
                    branchBean.setPrintInvForm(rs.getString("PrintInvForm"));
                    branchBean.setPSelectStk(rs.getString("PSelectStk"));
                    branchBean.setPStkChk(rs.getString("PStkChk"));
                    branchBean.setPMinStkChk(rs.getString("PMinStkChk"));
                    branchBean.setRoundUpTime(rs.getFloat("RoundUpTime"));
                    branchBean.setGiftStatusChk(rs.getString("GiftStatusChk"));
                    branchBean.setKICCopy1(rs.getString("KICCopy1"));
                    branchBean.setKICCopy2(rs.getString("KICCopy2"));
                    branchBean.setKICCopy3(rs.getString("KICCopy3"));
                    branchBean.setKICCopy4(rs.getString("KICCopy4"));
                    branchBean.setKICCopy5(rs.getString("KICCopy5"));
                    branchBean.setKICCopy6(rs.getString("KICCopy6"));
                    branchBean.setKICCopy7(rs.getString("KICCopy7"));
                    branchBean.setKICCopy8(rs.getString("KICCopy8"));
                    branchBean.setKICCopy9(rs.getString("KICCopy9"));
                    branchBean.setKICChk1(rs.getString("KICChk1"));
                    branchBean.setKICChk2(rs.getString("KICChk2"));
                    branchBean.setKICChk3(rs.getString("KICChk3"));
                    branchBean.setKICChk4(rs.getString("KICChk4"));
                    branchBean.setKICChk5(rs.getString("KICChk5"));
                    branchBean.setKICChk6(rs.getString("KICChk6"));
                    branchBean.setKICChk7(rs.getString("KICChk7"));
                    branchBean.setKICChk8(rs.getString("KICChk8"));
                    branchBean.setKICChk9(rs.getString("KICChk9"));
                    branchBean.setUpdateBranchPoint(rs.getString("UpdateBranchPoint"));
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(BranchControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mysql.closeConnection(BranchControl.class);
        }

        return branchBean;
    }

    public static String[] getKicData20() {
        String[] kic;
        kic = new String[]{
            branchBean.getKIC1(), branchBean.getKIC2(), branchBean.getKIC3(), branchBean.getKIC4(), branchBean.getKIC5(),
            branchBean.getKIC6(), branchBean.getKIC7(), branchBean.getKIC8(), branchBean.getKIC9(), branchBean.getKIC10(),
            branchBean.getKIC11(), branchBean.getKIC12(), branchBean.getKIC13(), branchBean.getKIC14(), branchBean.getKIC15(),
            branchBean.getKIC16(), branchBean.getKIC17(), branchBean.getKIC18(), branchBean.getKIC19(), branchBean.getKIC20()
        };
        return kic;
    }

    public static String getForm(String kicNo) {
        String form = "1";

        MySQLConnect mysql = new MySQLConnect();
        try {
            mysql.open(BranchControl.class);
            String sql = "select KICCopy" + kicNo + " from branch limit 1";
            try (
                ResultSet rs = mysql.getConnection().createStatement().executeQuery(sql)) {
                if (rs.next()) {
                    form = rs.getString(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BranchControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mysql.closeConnection(BranchControl.class);
        }

        return form;
    }

}
