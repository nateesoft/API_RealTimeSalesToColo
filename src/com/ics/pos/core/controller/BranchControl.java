package com.ics.pos.core.controller;

import util.ThaiUtil;
import com.ics.bean.BranchBean;
import database.MySQLConnect;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.AppLogUtil;
import util.MSG;

public class BranchControl {

    private static BranchBean branchBean = null;

    public static void updateKicItemNo() {
        /**
         * * OPEN CONNECTION **
         */
        MySQLConnect mysql = new MySQLConnect();
        try {
            mysql.open(BranchControl.class);
            String sql = "update branch set KicItemNo=KicItemNo+1";
            mysql.getConnection().createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            MSG.ERR(null, e.getMessage());
            AppLogUtil.log(BranchControl.class, "error", e);
        } finally {
            mysql.closeConnection(BranchControl.class);
        }
    }

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
                    String bCheck = "";
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
//                branchBean.setKicName1(rs.getString("KicName1"));
//                branchBean.setKicName2(rs.getString("KicName2"));
//                branchBean.setKicName3(rs.getString("KicName3"));
//                branchBean.setKicName4(rs.getString("KicName4"));
//                branchBean.setKicName5(rs.getString("KicName5"));
//                branchBean.setKicName6(rs.getString("KicName6"));
//                branchBean.setKicName7(rs.getString("KicName7"));
//                branchBean.setKicName8(rs.getString("KicName8"));
//                branchBean.setKicName9(rs.getString("KicName9"));
//                branchBean.setKicPrintOnReceipt1(rs.getString("KicPrintOnReceipt1"));
//                branchBean.setKicPrintOnReceipt2(rs.getString("KicPrintOnReceipt2"));
//                branchBean.setKicPrintOnReceipt3(rs.getString("KicPrintOnReceipt3"));
//                branchBean.setKicPrintOnReceipt4(rs.getString("KicPrintOnReceipt4"));
//                branchBean.setKicPrintOnReceipt5(rs.getString("KicPrintOnReceipt5"));
//                branchBean.setKicPrintOnReceipt6(rs.getString("KicPrintOnReceipt6"));
//                branchBean.setKicPrintOnReceipt7(rs.getString("KicPrintOnReceipt7"));
//                branchBean.setKicPrintOnReceipt8(rs.getString("KicPrintOnReceipt8"));
//                branchBean.setKicPrintOnReceipt9(rs.getString("KicPrintOnReceipt9"));
//                branchBean.setKicQue(rs.getInt("KicQue"));
//                branchBean.setKic10(rs.getString("Kic10"));
//                branchBean.setKic11(rs.getString("Kic11"));
//                branchBean.setKic12(rs.getString("Kic12"));
//                branchBean.setKic13(rs.getString("Kic13"));
//                branchBean.setKic14(rs.getString("Kic14"));
//                branchBean.setKic15(rs.getString("Kic15"));
//                branchBean.setKic16(rs.getString("Kic16"));
//                branchBean.setKic17(rs.getString("Kic17"));
//                branchBean.setKic18(rs.getString("Kic18"));
//                branchBean.setKic19(rs.getString("Kic19"));
//                branchBean.setKic20(rs.getString("Kic20"));
//                branchBean.setKicCopy10(rs.getString("KicCopy10"));
//                branchBean.setKicCopy11(rs.getString("KicCopy11"));
//                branchBean.setKicCopy12(rs.getString("KicCopy12"));
//                branchBean.setKicCopy13(rs.getString("KicCopy13"));
//                branchBean.setKicCopy14(rs.getString("KicCopy14"));
//                branchBean.setKicCopy15(rs.getString("KicCopy15"));
//                branchBean.setKicCopy16(rs.getString("KicCopy16"));
//                branchBean.setKicCopy17(rs.getString("KicCopy17"));
//                branchBean.setKicCopy18(rs.getString("KicCopy18"));
//                branchBean.setKicCopy19(rs.getString("KicCopy19"));
//                branchBean.setKicCopy20(rs.getString("KicCopy20"));
//                branchBean.setKicChk10(rs.getString("KicChk10"));
//                branchBean.setKicChk11(rs.getString("KicChk11"));
//                branchBean.setKicChk12(rs.getString("KicChk12"));
//                branchBean.setKicChk13(rs.getString("KicChk13"));
//                branchBean.setKicChk14(rs.getString("KicChk14"));
//                branchBean.setKicChk15(rs.getString("KicChk15"));
//                branchBean.setKicChk16(rs.getString("KicChk16"));
//                branchBean.setKicChk17(rs.getString("KicChk17"));
//                branchBean.setKicChk18(rs.getString("KicChk18"));
//                branchBean.setKicChk19(rs.getString("KicChk19"));
//                branchBean.setKicChk20(rs.getString("KicChk20"));

//                branchBean.setImageHomePath(rs.getString("IMG_HOME_PATH"));
                }
            }

        } catch (SQLException e) {
            MSG.ERR(e.getMessage());
            AppLogUtil.log(BranchControl.class, "error", e);
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
        /**
         * * OPEN CONNECTION **
         */
        MySQLConnect mysql = new MySQLConnect();
        try {
            mysql.open(BranchControl.class);
            String sql = "select KICCopy" + kicNo + " from branch limit 1";
            try (//            Statement stmt = mysql.getConnection().createStatement();
                    ResultSet rs = mysql.getConnection().createStatement().executeQuery(sql)) {
                if (rs.next()) {
                    form = rs.getString(1);
                }
//            stmt.close();
            }
        } catch (SQLException e) {
            MSG.ERR(e.getMessage());
            AppLogUtil.log(BranchControl.class, "error", e);
        } finally {
            mysql.closeConnection(BranchControl.class);
        }

        return form;
    }

}
