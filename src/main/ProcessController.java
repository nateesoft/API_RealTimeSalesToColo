package main;

import com.ics.bean.BillNoBean;
import com.ics.bean.STCardBean;
import com.ics.bean.STKFileBean;
import com.ics.bean.TSaleBean;
import com.ics.pos.core.controller.BillControl;
import database.ConfigFile;
import database.MySQLConnect;
import database.MySQLConnectWebOnline;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import util.AppLogUtil;
import util.DateConvert;
import util.ThaiUtil;

/**
 *
 * @author nateelive
 */
public class ProcessController {

    // Connection instances - ไม่ควรเก็บไว้เป็น field ถ้าไม่จำเป็น
    // แต่ถ้าต้องใช้บ่อย สามารถเก็บไว้และจัดการ lifecycle ได้
    private MySQLConnectWebOnline mysqlWebOnline;
    private MySQLConnect mysqlLocal;

    private final JTextArea txtLogErr;
    private final JToggleButton btnStatus;
    private final JToggleButton btnStatus1;
    private final JLabel lblDisplayStcard;
    private final JLabel lblDisplayStcard1;
    private final JTextArea txtSql;
    private final JProgressBar pbCheckUpdate;

    private final String configCheck = "Config check..." + "\r\n";
    private String ErrorText = "Log Error.." + "\r\n";
    private final String logTab = "Log Check..." + "\r\n";
    private final String businessType = ConfigFile.getProperties("businessType");

    private final DateConvert dateConvert = new DateConvert();

    public ProcessController(JTextArea txtLogErr, JToggleButton btnStatus, JLabel lblDisplayStcard,
            JTextArea txtSql, JLabel lblDisplayStcard1, JToggleButton btnStatus1,
            JProgressBar pbCheckUpdate) {
        this.txtLogErr = txtLogErr;
        this.btnStatus = btnStatus;
        this.lblDisplayStcard = lblDisplayStcard;
        this.txtSql = txtSql;
        this.lblDisplayStcard1 = lblDisplayStcard1;
        this.btnStatus1 = btnStatus1;
        this.pbCheckUpdate = pbCheckUpdate;
    }

    /**
     * เปิด Connection ทั้ง Local และ Web
     * ควรเรียกก่อนเริ่มทำงาน
     */
    public void openConnections() {
        try {
            mysqlLocal = new MySQLConnect();
            mysqlLocal.open(this.getClass());

            mysqlWebOnline = new MySQLConnectWebOnline();
            mysqlWebOnline.open();

            AppLogUtil.log(this.getClass(), "info", null);
            System.out.println("Connections opened successfully");
        } catch (Exception e) {
            AppLogUtil.log(this.getClass(), "error", e);
            ErrorText += "Failed to open connections: " + e.getMessage() + "\n";
            txtLogErr.setText(logTab + ErrorText);
        }
    }

    /**
     * ปิด Connection ทั้งหมด
     * ควรเรียกเมื่อทำงานเสร็จ
     */
    public void closeConnections() {
        try {
            if (mysqlLocal != null) {
                mysqlLocal.close();
                mysqlLocal = null;
            }
            if (mysqlWebOnline != null) {
                mysqlWebOnline.close();
                mysqlWebOnline = null;
            }
            System.out.println("Connections closed successfully");
        } catch (Exception e) {
            AppLogUtil.log(this.getClass(), "error", e);
        }
    }

    /**
     * ตรวจสอบและเปิด Connection ใหม่ถ้าจำเป็น
     * ปิด connection เก่าก่อนสร้างใหม่เพื่อป้องกัน connection leak
     */
    private void ensureConnectionsOpen() {
        try {
            // ตรวจสอบ Local Connection
            if (mysqlLocal == null || mysqlLocal.getConnection() == null || mysqlLocal.getConnection().isClosed()) {
                // ปิด connection เก่าก่อน (ถ้ามี)
                if (mysqlLocal != null) {
                    try {
                        mysqlLocal.close();
                    } catch (Exception e) {
                        // ignore - connection อาจปิดไปแล้ว
                    }
                }
                mysqlLocal = new MySQLConnect();
                mysqlLocal.open(this.getClass());
                System.out.println("Local connection reopened");
            }

            // ตรวจสอบ Web Online Connection
            if (mysqlWebOnline == null || mysqlWebOnline.getConnection() == null || mysqlWebOnline.getConnection().isClosed()) {
                // ปิด connection เก่าก่อน (ถ้ามี)
                if (mysqlWebOnline != null) {
                    try {
                        mysqlWebOnline.close();
                    } catch (Exception e) {
                        // ignore - connection อาจปิดไปแล้ว
                    }
                }
                mysqlWebOnline = new MySQLConnectWebOnline();
                mysqlWebOnline.open();
                System.out.println("Web connection reopened");
            }
        } catch (SQLException e) {
            AppLogUtil.log(this.getClass(), "error", e);
            ErrorText += "Connection error: " + e.getMessage() + "\n";
            txtLogErr.setText(logTab + ErrorText);
        }
    }
    
    public void uploadCheckConfig() {
        System.out.println("uploadCheckConfig() Process");
        String sendRealtimeWeb = ConfigFile.getProperties("sendRealtimeWeb");

        txtLogErr.setText(configCheck + "sendRealtimeWeb Value=" + sendRealtimeWeb);
        txtLogErr.setText(configCheck + "DB: Value=" + ConfigFile.getProperties("database"));
    }

    public void uploadBillno(String branchCode) {
        ensureConnectionsOpen();
        try {
            btnStatus.setBackground(Color.GREEN);
            String sql = "select b_refno from billno where B_SendOnline='N';";
            try (Statement stmt = mysqlLocal.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    BillControl billControl = new BillControl();
                    String refno = rs.getString("b_refno");
                    BillNoBean billbean = billControl.getData(refno);
                    if (billbean.getB_PostDate() == null || billbean.getB_PostDate().equals("null")) {
                        billbean.setB_PostDate(new Date());
                    }
                    if (billbean.getB_InvType() == null || billbean.getB_InvType().equals("null")) {
                        billbean.setB_InvType("");
                    }
                    String sqlUpdateOnline = "INSERT INTO billno VALUES("
                            + "'" + billbean.getB_Refno() + "', '" + billbean.getB_CuponDiscAmt() + "', '" + billbean.getB_Ontime() + "', '" + billbean.getB_LoginTime() + "', '" + billbean.getB_OnDate() + "',"
                            + " '" + billbean.getB_PostDate() + "', '" + billbean.getB_Table() + "', '" + billbean.getB_MacNo() + "', '" + billbean.getB_Cashier() + "', '" + billbean.getB_Cust() + "',"
                            + " '" + billbean.getB_ETD() + "', '" + billbean.getB_Total() + "', '" + billbean.getB_Food() + "', '" + billbean.getB_Drink() + "', '" + billbean.getB_Product() + "',"
                            + " '" + billbean.getB_Service() + "', '" + billbean.getB_ServiceAmt() + "', '" + billbean.getB_ItemDiscAmt() + "', '" + billbean.getB_FastDisc() + "', '" + billbean.getB_FastDiscAmt() + "',"
                            + " '" + billbean.getB_EmpDisc() + "', '" + billbean.getB_EmpDiscAmt() + "', '" + billbean.getB_TrainDisc() + "', '" + billbean.getB_TrainDiscAmt() + "', '" + billbean.getB_MemDisc() + "',"
                            + " '" + billbean.getB_MemDiscAmt() + "', '" + billbean.getB_SubDisc() + "', '" + billbean.getB_SubDiscAmt() + "', '" + billbean.getB_SubDiscBath() + "', '" + billbean.getB_ProDiscAmt() + "',"
                            + " '" + billbean.getB_SpaDiscAmt() + "', '" + billbean.getB_AdjAmt() + "', '" + billbean.getB_PreDisAmt() + "', '" + billbean.getB_NetTotal() + "', '" + billbean.getB_NetFood() + "',"
                            + " '" + billbean.getB_NetDrink() + "', '" + billbean.getB_NetProduct() + "', '" + billbean.getB_NetVat() + "', '" + billbean.getB_NetNonVat() + "',"
                            + " '" + billbean.getB_Vat() + "', '" + billbean.getB_PayAmt() + "', '" + billbean.getB_Cash() + "', '" + billbean.getB_GiftVoucher() + "', '" + billbean.getB_Earnest() + "',"
                            + " '" + billbean.getB_Ton() + "', '" + billbean.getB_CrCode1() + "', '" + billbean.getB_CardNo1() + "', '" + billbean.getB_AppCode1() + "', '" + billbean.getB_CrCharge1() + "',"
                            + " '" + billbean.getB_CrChargeAmt1() + "', '" + billbean.getB_CrAmt1() + "', '" + billbean.getB_AccrCode() + "', '" + billbean.getB_AccrAmt() + "', '" + billbean.getB_AccrCr() + "',"
                            + " '" + billbean.getB_MemCode() + "', '" + billbean.getB_MemName() + "', '" + billbean.getB_MemBegin() + "', '" + billbean.getB_MemEnd() + "', '" + billbean.getB_MemCurSum() + "',"
                            + " '" + billbean.getB_Void() + "', '" + billbean.getB_VoidUser() + "', '" + billbean.getB_VoidTime() + "', '" + billbean.getB_BillCopy() + "', '" + billbean.getB_PrnCnt() + "',"
                            + " '" + billbean.getB_PrnTime1() + "', '" + billbean.getB_PrnTime2() + "', '" + billbean.getB_InvNo() + "', '" + billbean.getB_InvType() + "', '" + branchCode + "',"
                            + " '" + billbean.getB_BranName() + "', '" + billbean.getB_Tel() + "', '" + billbean.getB_RecTime() + "', '" + billbean.getMStamp() + "', '" + billbean.getMScore() + "',"
                            + " '" + billbean.getCurStamp() + "', '" + billbean.getStampRate() + "', '" + billbean.getB_ChkBill() + "', '" + billbean.getB_ChkBillTime() + "', '" + billbean.getB_CashTime() + "',"
                            + " '" + billbean.getB_WaitTime() + "', '" + billbean.getB_SumScore() + "', '" + billbean.getB_CrBank() + "', '" + billbean.getB_CrCardAmt() + "', '" + billbean.getB_CrCurPoint() + "',"
                            + " '" + billbean.getB_CrSumPoint() + "', '" + billbean.getB_Entertain1() + "', '" + billbean.getB_VoucherDiscAmt() + "', '" + billbean.getB_VoucherOver() + "', '" + billbean.getB_NetDiff() + "',"
                            + " '" + billbean.getB_SumSetDiscAmt() + "', '" + billbean.getB_DetailFood() + "', '" + billbean.getB_DetailDrink() + "', '" + billbean.getB_DetailProduct() + "', '" + billbean.getB_KicQue() + "',"
                            + " '" + billbean.getB_ROUNDCLOSE() + "', '', '', '', '',"
                            + " '', '', '', '', '',"
                            + " '', '', '','', 'Y','" + branchCode + "');";
                    try {
                        System.out.println(sqlUpdateOnline);
                        mysqlWebOnline.getConnection().createStatement().executeUpdate(sqlUpdateOnline);
                    } catch (SQLException e) {
                        System.out.println(e);
                        ErrorText += "\n";
                        ErrorText += e.toString();
                        txtLogErr.setText(logTab + ErrorText);
                    }

                    String sqlUpdateBillnoSendOnlineFlag = "update billno set b_sendOnline='Y' where b_refno='" + billbean.getB_Refno() + "' and b_macno='" + billbean.getB_MacNo() + "';";
                    mysqlLocal.getConnection().createStatement().executeUpdate(sqlUpdateBillnoSendOnlineFlag);
                    System.out.println(sqlUpdateBillnoSendOnlineFlag);
                }
                System.out.println("Loop Billno Finished;");
                ErrorText += "loop Billno Finished Insert";
            } catch (SQLException e) {
                System.out.println(e);
                ErrorText += e.toString();
                txtLogErr.setText(logTab + ErrorText);
            }
        } catch (Exception e) {
            ErrorText += e.toString();
            txtLogErr.setText(logTab + ErrorText);
            btnStatus.setBackground(Color.RED);
        }
    }

    public void uploadT_Sale(String branchCode) {
        ensureConnectionsOpen();
        try (Statement stmtLocal = mysqlLocal.getConnection().createStatement();
             Statement stmtServer = mysqlWebOnline.getConnection().createStatement();
             Statement stmtLocalUpdate = mysqlLocal.getConnection().createStatement()) {

            String sql = "select * from t_sale where r_sendOnline='N' order by macno,r_refno,r_index;";
            ResultSet rs = stmtLocal.executeQuery(sql);
            TSaleBean bean = new TSaleBean();
            while (rs.next()) {
                bean.setR_Index(rs.getString("R_Index"));
                bean.setR_Refno(rs.getString("R_Refno"));
                bean.setR_Table(rs.getString("R_Table"));
                bean.setR_Date(rs.getDate("R_Date"));
                bean.setR_Time(rs.getString("R_Time"));
                bean.setMacNo(rs.getString("MacNo"));
                bean.setCashier((rs.getString("Cashier")));
                bean.setR_Emp((rs.getString("R_Emp")));
                bean.setR_PluCode(rs.getString("R_PluCode"));

                bean.setR_Cost(getCostfile(rs.getString("R_PluCode")));

                bean.setR_PName((rs.getString("R_PName")));
                bean.setR_Unit((rs.getString("R_Unit")));
                bean.setR_Group((rs.getString("R_Group")));
                bean.setR_Status(rs.getString("R_Status"));
                bean.setR_Normal(rs.getString("R_Normal"));
                bean.setR_Discount(rs.getString("R_Discount"));
                bean.setR_Service(rs.getString("R_Service"));
                bean.setR_Stock(rs.getString("R_Stock"));
                bean.setR_Set(rs.getString("R_Set"));
                bean.setR_Vat(rs.getString("R_Vat"));
                bean.setR_Type(rs.getString("R_Type"));
                if (bean.getR_Type() == null || bean.getR_Type().equals("null")) {
                    bean.setR_Type("");
                }
                bean.setR_ETD(rs.getString("R_ETD"));
                bean.setR_Quan(rs.getDouble("R_Quan"));
                bean.setR_Price(rs.getDouble("R_Price"));
                bean.setR_Total(rs.getDouble("R_Total"));
                bean.setR_Type(rs.getString("R_PrType"));
                bean.setR_PrCode(rs.getString("R_PrCode"));
                bean.setR_PrDisc(rs.getDouble("R_PrDisc"));
                bean.setR_PrBath(rs.getDouble("R_PrBath"));
                bean.setR_PrAmt(rs.getDouble("R_PrAmt"));
                bean.setR_PrCuType(rs.getString("R_PrCuType"));
                bean.setR_PrCuCode(rs.getString("R_PrCuCode"));
                bean.setR_PrCuQuan(rs.getDouble("R_PrCuQuan"));
                bean.setR_PrCuAmt(rs.getDouble("R_PrCuAmt"));
                bean.setR_Redule(rs.getDouble("R_Redule"));
                bean.setR_DiscBath(rs.getDouble("R_DiscBath"));
                bean.setR_PrAdj(rs.getDouble("R_PrAdj"));

                bean.setR_NetTotal(rs.getDouble("R_NetTotal"));

                bean.setR_Refund(rs.getString("R_Refund"));
                bean.setVoidMsg((rs.getString("VoidMsg")));
                bean.setR_Void(rs.getString("R_Void"));
                bean.setR_VoidUser((rs.getString("R_VoidUser")));
                bean.setR_VoidTime(rs.getString("R_VoidTime"));
                bean.setStkCode(rs.getString("StkCode"));
                bean.setPosStk(rs.getString("PosStk"));
                bean.setR_ServiceAmt(rs.getDouble("R_ServiceAmt"));
                bean.setR_PrChkType(rs.getString("R_PrChkType"));
                bean.setR_PrQuan(rs.getDouble("R_PrQuan"));
                bean.setR_PrSubType(rs.getString("R_PrSubType"));
                bean.setR_PrSubCode(rs.getString("R_PrSubCode"));
                bean.setR_PrSubQuan(rs.getDouble("R_PrSubQuan"));
                bean.setR_PrSubDisc(rs.getDouble("R_PrSubDisc"));
                bean.setR_PrSubBath(rs.getDouble("R_PrSubBath"));
                bean.setR_PrSubAmt(rs.getDouble("R_PrSubAmt"));
                bean.setR_PrSubAdj(rs.getDouble("R_PrSubAdj"));
                bean.setR_PrCuDisc(rs.getDouble("R_PrCuDisc"));
                bean.setR_PrCuBath(rs.getDouble("R_PrCuBath"));
                bean.setR_PrCuAdj(rs.getDouble("R_PrCuAdj"));
                bean.setR_PrChkType2(rs.getString("R_PrChkType2"));
                bean.setR_PrQuan2(rs.getDouble("R_PrQuan2"));
                bean.setR_PrType2(rs.getString("R_PrType2"));
                bean.setR_PrCode2(rs.getString("R_PrCode2"));
                bean.setR_PrDisc2(rs.getDouble("R_PrDisc2"));
                bean.setR_PrBath2(rs.getDouble("R_PrBath2"));
                bean.setR_PrAmt2(rs.getDouble("R_PrAmt2"));
                bean.setR_PrAdj2(rs.getDouble("R_PrAdj2"));

                bean.setR_SendOnline("Y");

                //check businessType
                if (!businessType.equals("retail")) {
                    bean.setR_PreDisAmt(rs.getDouble("R_PreDisAmt"));
                    bean.setR_Kic(rs.getString("R_Kic"));
                    bean.setR_KicPrint(rs.getString("R_KicPrint"));
                    bean.setR_PItemNo(rs.getInt("R_PItemNo"));
                    bean.setR_PKicQue(rs.getInt("R_PKicQue"));
                    bean.setR_PrVcType(rs.getString("R_PrVcType"));
                    bean.setR_PrVcCode(rs.getString("R_PrVcCode"));
                    bean.setR_PrVcAmt(rs.getDouble("R_PrVcAmt"));
                    bean.setR_PrVcAdj(rs.getDouble("R_PrVcAdj"));
                    bean.setR_MoveFlag(rs.getString("R_MoveFlag"));
                    bean.setR_Pause(rs.getString("R_Pause"));
                    bean.setR_SPIndex(rs.getString("R_SPIndex"));
                    bean.setR_LinkIndex(rs.getString("R_LinkIndex"));
                    bean.setR_VoidPause(rs.getString("R_VoidPause"));
                    bean.setR_SetPrice(rs.getDouble("R_SetPrice"));
                    bean.setR_SetDiscAmt(rs.getDouble("R_SetDiscAmt"));
                    bean.setR_MoveItem(rs.getString("R_MoveItem"));
                    bean.setR_MoveFrom(rs.getString("R_MoveFrom"));
                    bean.setR_MoveUser(ThaiUtil.ASCII2Unicode(rs.getString("R_MoveUser")));
                    bean.setR_PrintItemBill(rs.getString("R_PrintItemBill"));
                    bean.setR_CountTime(rs.getString("R_CountTime"));
                    bean.setR_Return(rs.getString("R_Return"));
                    bean.setR_Earn(rs.getString("R_Earn"));
                    bean.setR_EarnNo(rs.getString("R_EarnNo"));
                    bean.setR_Opt1((rs.getString("R_Opt1")));
                    bean.setR_Opt2((rs.getString("R_Opt2")));
                    bean.setR_Opt3((rs.getString("R_Opt3")));
                    bean.setR_Opt4((rs.getString("R_Opt4")));
                    bean.setR_Opt5((rs.getString("R_Opt5")));
                    bean.setR_Opt6((rs.getString("R_Opt6")));
                    bean.setR_Opt7((rs.getString("R_Opt7")));
                    bean.setR_Opt8((rs.getString("R_Opt8")));
                    bean.setR_Opt9((rs.getString("R_Opt9")));

                    bean.setR_NetDiff(rs.getDouble("R_NetDiff"));
                } else {
                    bean.setR_PreDisAmt(0.00);
                    bean.setR_Kic("");
                    bean.setR_KicPrint("");
                    bean.setR_PItemNo(0);
                    bean.setR_PKicQue(0);
                    bean.setR_PrVcType("");
                    bean.setR_PrVcCode("");
                    bean.setR_PrVcAmt(0);
                    bean.setR_PrVcAdj(0);
                    bean.setR_MoveFlag("");
                    bean.setR_Pause("");
                    bean.setR_SPIndex("");
                    bean.setR_LinkIndex("");
                    bean.setR_VoidPause("");
                    bean.setR_SetPrice(0);
                    bean.setR_SetDiscAmt(0);
                    bean.setR_MoveItem("");
                    bean.setR_MoveFrom("");
                    bean.setR_MoveUser("");
                    bean.setR_PrintItemBill("");
                    bean.setR_CountTime("");
                    bean.setR_Return("");
                    bean.setR_Earn("");
                    bean.setR_EarnNo("");
                    bean.setR_Opt1("");
                    bean.setR_Opt2("");
                    bean.setR_Opt3("");
                    bean.setR_Opt4("");
                    bean.setR_Opt5("");
                    bean.setR_Opt6("");
                    bean.setR_Opt7("");
                    bean.setR_Opt8("");
                    bean.setR_Opt9("");
                    bean.setR_NetDiff(0.00);
                }
                bean.setR_BranchCode(branchCode);

                if (bean.getR_PrVcType() == null) {
                    bean.setR_PrVcType("");
                }
                if (bean.getR_PrintItemBill() == null) {
                    bean.setR_PrintItemBill("");
                }
                if (bean.getR_CountTime() == null) {
                    bean.setR_CountTime("");
                }
                String sqlUploadTSaleOnline = "INSERT INTO t_sale VALUES("
                        + "'" + bean.getR_Index() + "',"
                        + "'" + bean.getR_Refno() + "',"
                        + "'" + bean.getR_Table() + "',"
                        + "'" + bean.getR_Date() + "',"
                        + "'" + bean.getR_Time() + "',"
                        + "'" + bean.getMacNo() + "',"
                        + "'" + bean.getCashier() + "',"
                        + "'" + bean.getR_Emp() + "',"
                        + "'" + bean.getR_PluCode() + "',"
                        + "'" + bean.getR_PName().replace("'", " ") + "',"
                        + "'" + bean.getR_Unit() + "',"
                        + "'" + bean.getR_Group() + "',"
                        + "'" + bean.getR_Status() + "',"
                        + "'" + bean.getR_Normal() + "',"
                        + "'" + bean.getR_Discount() + "',"
                        + "'" + bean.getR_Service() + "',"
                        + "'" + bean.getR_Stock() + "',"
                        + "'" + bean.getR_Set() + "',"
                        + "'" + bean.getR_Vat() + "',"
                        + "'" + bean.getR_Type() + "',"
                        + "'" + bean.getR_ETD() + "',"
                        + "'" + bean.getR_Quan() + "',"
                        + "'" + bean.getR_Price() + "',"
                        + "'" + bean.getR_Total() + "',"
                        + "'" + bean.getR_Type() + "',"
                        + "'" + bean.getR_PrCode() + "',"
                        + "'" + bean.getR_PrDisc() + "',"
                        + "'" + bean.getR_PrBath() + "',"
                        + "'" + bean.getR_PrAmt() + "',"
                        + "'" + bean.getR_PrCuType() + "',"
                        + "'" + bean.getR_PrCuCode() + "',"
                        + "'" + bean.getR_PrCuQuan() + "',"
                        + "'" + bean.getR_PrCuAmt() + "',"
                        + "'" + bean.getR_Redule() + "',"
                        + "'" + bean.getR_DiscBath() + "',"
                        + "'" + bean.getR_PrAdj() + "',"
                        + "'" + bean.getR_PreDisAmt() + "',"
                        + "'" + bean.getR_NetTotal() + "',"
                        + "'" + bean.getR_Kic() + "',"
                        + "'" + bean.getR_KicPrint() + "',"
                        + "'" + bean.getR_Refund() + "',"
                        + "'" + bean.getVoidMsg() + "',"
                        + "'" + bean.getR_Void() + "',"
                        + "'" + bean.getR_VoidUser() + "',"
                        + "'" + bean.getR_VoidTime() + "',"
                        + "'" + bean.getStkCode() + "',"
                        + "'" + bean.getPosStk() + "',"
                        + "'" + bean.getR_ServiceAmt() + "',"
                        + "'" + bean.getR_PrChkType() + "',"
                        + "'" + bean.getR_PrQuan() + "',"
                        + "'" + bean.getR_PrSubType() + "',"
                        + "'" + bean.getR_PrSubCode() + "',"
                        + "'" + bean.getR_PrSubQuan() + "',"
                        + "'" + bean.getR_PrSubDisc() + "',"
                        + "'" + bean.getR_PrSubBath() + "',"
                        + "'" + bean.getR_PrSubAmt() + "',"
                        + "'" + bean.getR_PrSubAdj() + "',"
                        + "'" + bean.getR_PrCuDisc() + "',"
                        + "'" + bean.getR_PrCuBath() + "',"
                        + "'" + bean.getR_PrCuAdj() + "',"
                        + "'" + bean.getR_PrChkType2() + "',"
                        + "'" + bean.getR_PrQuan2() + "',"
                        + "'" + bean.getR_PrType2() + "',"
                        + "'" + bean.getR_PrCode2() + "',"
                        + "'" + bean.getR_PrDisc2() + "',"
                        + "'" + bean.getR_PrBath2() + "',"
                        + "'" + bean.getR_PrAmt2() + "',"
                        + "'" + bean.getR_PrAdj2() + "',"
                        + "'" + bean.getR_PItemNo() + "',"
                        + "'" + bean.getR_PKicQue() + "',"
                        + "'" + bean.getR_PrVcType() + "',"
                        + "'" + bean.getR_PrVcCode() + "',"
                        + "'" + bean.getR_PrVcAmt() + "',"
                        + "'" + bean.getR_PrVcAdj() + "',"
                        + "'" + bean.getR_MoveFlag() + "',"
                        + "'" + bean.getR_Pause() + "',"
                        + "'" + bean.getR_SPIndex() + "',"
                        + "'" + bean.getR_LinkIndex() + "',"
                        + "'" + bean.getR_VoidPause() + "',"
                        + "'" + bean.getR_SetPrice() + "',"
                        + "'" + bean.getR_SetDiscAmt() + "',"
                        + "'" + bean.getR_MoveItem() + "',"
                        + "'" + bean.getR_MoveFrom() + "',"
                        + "'" + bean.getR_MoveUser() + "',"
                        + "'" + bean.getR_Opt9() + "',"
                        + "'" + bean.getR_Opt1() + "',"
                        + "'" + bean.getR_Opt2() + "',"
                        + "'" + bean.getR_Opt3() + "',"
                        + "'" + bean.getR_Opt4() + "',"
                        + "'" + bean.getR_Opt5() + "',"
                        + "'" + bean.getR_Opt6() + "',"
                        + "'" + bean.getR_Opt7() + "',"
                        + "'" + bean.getR_Opt8() + "',"
                        + "'" + bean.getR_PrintItemBill() + "',"
                        + "'" + bean.getR_CountTime() + "',"
                        + "'" + bean.getR_Return() + "',"
                        + "'" + bean.getR_Earn() + "',"
                        + "'" + bean.getR_EarnNo() + "',"
                        + "'" + bean.getR_NetDiff() + "',"
                        + "'" + bean.getR_SendOnline() + "',"
                        + "'" + bean.getR_BranchCode() + "',"
                        + "'" + bean.getR_Cost() + "')";
                try {
                    stmtServer.executeUpdate(sqlUploadTSaleOnline);
                    System.out.println(sqlUploadTSaleOnline);
                } catch (SQLException e) {
                    AppLogUtil.log(this.getClass(), "error", e);
                    ErrorText += e.toString();
                    txtLogErr.setText(logTab + ErrorText);
                }
                try {
                    String sqlUpdateTSaleSendOnlineFlag = "update t_sale "
                            + "set r_sendOnline='Y' "
                            + "where R_refno='" + bean.getR_Refno() + "' "
                            + "and r_index='" + bean.getR_Index() + "' "
                            + "and macno='" + bean.getMacNo() + "';";
                    stmtLocalUpdate.executeUpdate(sqlUpdateTSaleSendOnlineFlag);
                    System.out.println(sqlUpdateTSaleSendOnlineFlag);
                } catch (SQLException e) {
                    AppLogUtil.log(this.getClass(), "error", e);
                    ErrorText += e.toString();
                    txtLogErr.setText(logTab + ErrorText);
                }
                Thread.sleep(90);
            }
            System.out.println("Loop T_Sale Finished;");
            Thread.sleep(10 * 1000);
        } catch (InterruptedException | SQLException e) {
            System.out.println(e);
            ErrorText += e.toString();
            txtLogErr.setText(logTab + ErrorText);
        }
    }

    public void uploadStcard(String branchCode) {
        ensureConnectionsOpen();
        List<STCardBean> listSTCardNotSend = new ArrayList<>();
        String sql;
        try {
            sql = "select * from stcard where s_send<>'Y';";
            try (Statement stmt = mysqlLocal.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    btnStatus.setEnabled(false);

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

                    listSTCardNotSend.add(bean);
                }
            }
        } catch (SQLException e) {
            AppLogUtil.log(this.getClass(), "error", e);
        }

        try {
            int size = listSTCardNotSend.size() - 1;
            if (!listSTCardNotSend.isEmpty()) {
                String tempText = "";
                for (int i = 0; i < listSTCardNotSend.size(); i++) {
                    lblDisplayStcard.setText("stcard List = : " + (size - i) + " From Total =" + listSTCardNotSend.size());

                    double discount = 0;
                    double nettotal = 0;
                    String refund = "";
                    String refno = "";
                    String cashier = "";
                    String emp = "";

                    STCardBean stCardNotSend = (STCardBean) listSTCardNotSend.get(i);
                    STCardBean stcardBean;
                    if (stCardNotSend.getS_Rem().equals("SAL")) {
                        String strCheck = stCardNotSend.getS_No().substring(0, 1);
                        if (strCheck.equals("E")) {
                            discount = 0;
                            nettotal = stCardNotSend.getS_OutCost();
                            refund = "-";
                            refno = stCardNotSend.getS_No();
                            cashier = stCardNotSend.getEmp();
                            emp = cashier;
                        }
                        if (strCheck.equals("R")) {
                            stcardBean = matchDiscount(stCardNotSend.getS_No(), stCardNotSend.getS_Date(), stCardNotSend.getS_PCode(), stCardNotSend.getS_Out(), stCardNotSend.getS_OutCost());
                            discount = (stcardBean.getDiscount());
                            nettotal = (stcardBean.getNettotal());
                            refund = stcardBean.getRefund();
                            refno = stcardBean.getRefNo();
                            cashier = stcardBean.getCashier();
                            emp = stcardBean.getEmp();
                        }
                        if (strCheck.equals("0")) {
                            stcardBean = matchDiscount(stCardNotSend.getS_No(), stCardNotSend.getS_Date(), stCardNotSend.getS_PCode(), stCardNotSend.getS_Out(), stCardNotSend.getS_OutCost());
                            discount = stcardBean.getDiscount();
                            nettotal = stcardBean.getNettotal();
                            refund = stcardBean.getRefund();
                            refno = stcardBean.getRefNo();
                            cashier = stcardBean.getCashier();
                            emp = stcardBean.getEmp();
                        }
                    } else {
                        discount = 0;
                        if (stCardNotSend.getS_In() != 0) {
                            nettotal = stCardNotSend.getS_InCost();
                        } else if (stCardNotSend.getS_OutCost() != 0) {
                            nettotal = stCardNotSend.getS_OutCost();
                        }
                        refund = "-";
                        refno = "";
                        cashier = stCardNotSend.getCashier();
                        emp = cashier;
                    }

                    String s_noCheck = stCardNotSend.getS_No().substring(0, 1);
                    String sqlInsServerSTCard;
                    if ((s_noCheck.equals("E") && stCardNotSend.getS_Rem().equals("SAL")) || (refno.equals("") && stCardNotSend.getS_Rem().equals("SAL"))) {

                        int updateStatusFromServer = 0;
                        try {
                            if (nettotal == 0 && stCardNotSend.getS_Rem().equals("SAL") && stCardNotSend.getS_Out() != 0) {
                                stCardNotSend.setNettotal(totalCompareNettotal(stCardNotSend));
                                nettotal = stCardNotSend.getNettotal();
                            }
                            if (!stCardNotSend.getRefNo().equals("")) {
                                sqlInsServerSTCard = "insert into stcard ("
                                        + "s_date, s_no, s_subno, s_que, s_pcode, s_stk, "
                                        + "s_in, s_out, s_incost, s_outcost, s_acost, s_rem, "
                                        + "s_user, s_entrydate, s_entrytime, s_link, s_bran,"
                                        + "discount,nettotal,refund,refno,cashier,"
                                        + "emp) "
                                        + "values ("
                                        + "'" + stCardNotSend.getS_Date() + "','" + stCardNotSend.getS_No() + "','" + stCardNotSend.getS_SubNo() + "','" + stCardNotSend.getS_Que() + "','" + stCardNotSend.getS_PCode() + "','" + stCardNotSend.getS_Stk() + "',"
                                        + "'" + stCardNotSend.getS_In() + "','" + stCardNotSend.getS_Out() + "','" + stCardNotSend.getS_InCost() + "','" + stCardNotSend.getS_OutCost() + "','" + stCardNotSend.getS_ACost() + "','" + stCardNotSend.getS_Rem() + "',"
                                        + "'" + stCardNotSend.getS_User() + "','" + stCardNotSend.getS_EntryDate() + "','" + stCardNotSend.getS_EntryTime() + "','" + stCardNotSend.getS_Link() + "','" + branchCode + "',"
                                        + "'" + discount + "','" + nettotal + "','" + refund + "','" + refno + "','" + cashier + "',"
                                        + "'" + emp + "');";
                                updateStatusFromServer = mysqlWebOnline.getConnection().createStatement().executeUpdate(sqlInsServerSTCard);

                                if (updateStatusFromServer > 0) {
                                    String sqlLocalUpdateY = "update stcard set "
                                            + "s_send='Y' ,LastUpdate='" + dateConvert.GetCurrentDate() + "',LastTimeUpdate='" + dateConvert.GetCurrentTime() + "' "
                                            + "where "
                                            + "s_pcode='" + stCardNotSend.getS_PCode() + "' "
                                            + "and s_date='" + stCardNotSend.getS_Date() + "' "
                                            + "and s_entrytime='" + stCardNotSend.getS_EntryTime() + "'"
                                            + "and s_rem='" + stCardNotSend.getS_Rem() + "' "
                                            + "and s_user='" + stCardNotSend.getS_User() + "' "
                                            + "and s_send='N' "
                                            + "and s_no='" + stCardNotSend.getS_No() + ";";
                                    mysqlLocal.getConnection().createStatement().executeUpdate(sqlLocalUpdateY);
                                    tempText += sqlInsServerSTCard;
                                    txtSql.setText(tempText + logTab);
                                }
                            }

                        } catch (SQLException e) {
                            AppLogUtil.log(this.getClass(), "error", e);
                        }
                    } else {
                        // send to mysql server
                        int updateStatusFromServer = 0;
                        try {
                            if (nettotal == 0 && stCardNotSend.getS_Rem().equals("SAL") && stCardNotSend.getS_Out() != 0) {
                                stCardNotSend.setNettotal(totalCompareNettotal(stCardNotSend));
                                nettotal = stCardNotSend.getNettotal();
                            }

                            sqlInsServerSTCard = "insert into stcard ("
                                    + "s_date, s_no, s_subno, s_que, s_pcode, s_stk, "
                                    + "s_in, s_out, s_incost, s_outcost, s_acost, s_rem, "
                                    + "s_user, s_entrydate, s_entrytime, s_link, s_bran,"
                                    + "discount,nettotal,refund,refno,cashier,"
                                    + "emp) "
                                    + "values ("
                                    + "'" + stCardNotSend.getS_Date() + "','" + stCardNotSend.getS_No() + "','" + stCardNotSend.getS_SubNo() + "','" + stCardNotSend.getS_Que() + "','" + stCardNotSend.getS_PCode() + "','" + stCardNotSend.getS_Stk() + "',"
                                    + "'" + stCardNotSend.getS_In() + "','" + stCardNotSend.getS_Out() + "','" + stCardNotSend.getS_InCost() + "','" + stCardNotSend.getS_OutCost() + "','" + stCardNotSend.getS_ACost() + "','" + stCardNotSend.getS_Rem() + "',"
                                    + "'" + stCardNotSend.getS_User() + "','" + stCardNotSend.getS_EntryDate() + "','" + stCardNotSend.getS_EntryTime() + "','" + stCardNotSend.getS_Link() + "','" + branchCode + "',"
                                    + "'" + discount + "','" + nettotal + "','" + refund + "','" + refno + "','" + cashier + "',"
                                    + "'" + emp + "');";
                            updateStatusFromServer = mysqlWebOnline.getConnection().createStatement().executeUpdate(sqlInsServerSTCard);

                            tempText += sqlInsServerSTCard;
                            txtSql.setText(tempText + logTab);
                        } catch (SQLException e) {
                            AppLogUtil.log(this.getClass(), "error", e);
                        }

                        if (updateStatusFromServer > 0) {
                            String sqlLocalUpdateY = "update stcard set "
                                    + "s_send='Y' ,LastUpdate='" + dateConvert.GetCurrentDate() + "',LastTimeUpdate='" + dateConvert.GetCurrentTime() + "' "
                                    + "where 1=1 "
                                    + "and s_pcode='" + stCardNotSend.getS_PCode() + "' "
                                    + "and s_date='" + stCardNotSend.getS_Date() + "' "
                                    + "and s_entrytime='" + stCardNotSend.getS_EntryTime() + "'"
                                    + "and s_rem='" + stCardNotSend.getS_Rem() + "' "
                                    + "and s_user='" + stCardNotSend.getS_User() + "' "
                                    + "and s_send='N' "
                                    + "and s_no='" + stCardNotSend.getS_No() + "' "
                                    + "and s_incost='" + stCardNotSend.getS_InCost() + "' "
                                    + "and s_in ='" + stCardNotSend.getS_In() + "' "
                                    + "and s_out='" + stCardNotSend.getS_Out() + "' "
                                    + "and s_outcost='" + stCardNotSend.getS_OutCost() + "' "
                                    + "and s_que='" + stCardNotSend.getS_Que() + "';";
                            mysqlLocal.getConnection().createStatement().executeUpdate(sqlLocalUpdateY);

                            // next step to update stkfile
                            String pcode = stCardNotSend.getS_PCode();
                            uploadStkfile(pcode, branchCode);
                        }
                    }
                }

                lblDisplayStcard.setBackground(Color.green);
            }
            btnStatus.setEnabled(true);
        } catch (SQLException e) {
            AppLogUtil.log(this.getClass(), "error", e);
        }
    }

    public void uploadStkfile(String bpcode, String branchCode) {
        ensureConnectionsOpen();
        if (!bpcode.equals("")) {
            STKFileBean stkFileBean = null;
            String sql;
            try {
                sql = "select * from stkfile where bpcode='" + bpcode + "' limit 1";
                try (ResultSet rs = mysqlLocal.getConnection().createStatement().executeQuery(sql)) {
                    if (rs.next()) {
                        stkFileBean = new STKFileBean();
                        stkFileBean.setbPcode(rs.getString("BPCode"));
                        stkFileBean.setbStk(rs.getString("BStk"));
                        stkFileBean.setbQty(rs.getDouble("BQty"));
                        stkFileBean.setbAmt(rs.getDouble("BAmt"));
                        stkFileBean.setbTotalAmt(rs.getDouble("BTotalAmt"));
                        stkFileBean.setbQty0(rs.getDouble("BQty0"));
                        stkFileBean.setbQty1(rs.getDouble("BQty1"));
                        stkFileBean.setbQty2(rs.getDouble("BQty2"));
                        stkFileBean.setbQty3(rs.getDouble("BQty3"));
                        stkFileBean.setbQty4(rs.getDouble("BQty4"));
                        stkFileBean.setbQty5(rs.getDouble("BQty5"));
                        stkFileBean.setbQty6(rs.getDouble("BQty6"));
                        stkFileBean.setbQty7(rs.getDouble("BQty7"));
                        stkFileBean.setbQty8(rs.getDouble("BQty8"));
                        stkFileBean.setbQty9(rs.getDouble("BQty9"));
                        stkFileBean.setbQty10(rs.getDouble("BQty10"));
                        stkFileBean.setbQty11(rs.getDouble("BQty11"));
                        stkFileBean.setbQty12(rs.getDouble("BQty12"));
                        stkFileBean.setbQty13(rs.getDouble("BQty13"));
                        stkFileBean.setbQty14(rs.getDouble("BQty14"));
                        stkFileBean.setbQty15(rs.getDouble("BQty15"));
                        stkFileBean.setbQty16(rs.getDouble("BQty16"));
                        stkFileBean.setbQty17(rs.getDouble("BQty17"));
                        stkFileBean.setbQty18(rs.getDouble("BQty18"));
                        stkFileBean.setbQty19(rs.getDouble("BQty19"));
                        stkFileBean.setbQty20(rs.getDouble("BQty20"));
                        stkFileBean.setbQty21(rs.getDouble("BQty21"));
                        stkFileBean.setbQty22(rs.getDouble("BQty22"));
                        stkFileBean.setbQty23(rs.getDouble("BQty23"));
                        stkFileBean.setbQty24(rs.getDouble("BQty24"));
                        stkFileBean.setBranch(branchCode);
                    } else {
                        try {
                            String sqlInsStkfile = "insert ignore into stkfile (bpcode,branch) values('" + bpcode + "','" + branchCode + "');";
                            mysqlLocal.getConnection().createStatement().executeUpdate(sqlInsStkfile);

                            stkFileBean = new STKFileBean();
                            stkFileBean.setbPcode(bpcode);
                            stkFileBean.setbStk("A1");
                            stkFileBean.setbQty(0);
                            stkFileBean.setbAmt(0);
                            stkFileBean.setbTotalAmt((0));
                            stkFileBean.setbQty0((0));
                            stkFileBean.setbQty1((0));
                            stkFileBean.setbQty2((0));
                            stkFileBean.setbQty3((0));
                            stkFileBean.setbQty4((0));
                            stkFileBean.setbQty5((0));
                            stkFileBean.setbQty6((0));
                            stkFileBean.setbQty7((0));
                            stkFileBean.setbQty8((0));
                            stkFileBean.setbQty9((0));
                            stkFileBean.setbQty10((0));
                            stkFileBean.setbQty11((0));
                            stkFileBean.setbQty12((0));
                            stkFileBean.setbQty13((0));
                            stkFileBean.setbQty14((0));
                            stkFileBean.setbQty15((0));
                            stkFileBean.setbQty16((0));
                            stkFileBean.setbQty17((0));
                            stkFileBean.setbQty18((0));
                            stkFileBean.setbQty19((0));
                            stkFileBean.setbQty20((0));
                            stkFileBean.setbQty21((0));
                            stkFileBean.setbQty22((0));
                            stkFileBean.setbQty23((0));
                            stkFileBean.setbQty24((0));
                            stkFileBean.setBranch(branchCode);
                        } catch (SQLException e) {
                            AppLogUtil.log(this.getClass(), "error", e);
                            ErrorText += "\n";
                            ErrorText += e.toString();
                            txtLogErr.setText(logTab + ErrorText);
                        }
                    }
                }
            } catch (SQLException e) {
                AppLogUtil.log(this.getClass(), "error", e);
                ErrorText += "\n";
                ErrorText += e.toString();
                txtLogErr.setText(logTab + ErrorText);
            }

            String sqlUpdateStkfile;
            String tempText = "";

            if (stkFileBean != null) {
                loadStatus();
                try {
                    String sqlCheckSTKServer = "select bpcode,branch from stkfile "
                            + "where branch='" + stkFileBean.getBranch() + "' and bpcode='" + bpcode + "' limit 1;";
                    try (ResultSet rsSV = mysqlWebOnline.getConnection().createStatement().executeQuery(sqlCheckSTKServer)) {
                        if (rsSV.next() && !rsSV.wasNull()) {
                            System.out.println(rsSV.getString("bpcode"));
                        } else {
                            String sqlInsStkfile = "insert ignore into stkfile (bpcode,branch) values('" + bpcode + "','" + stkFileBean.getBranch() + "');";
                            mysqlWebOnline.getConnection().createStatement().executeUpdate(sqlInsStkfile);
                        }
                    }
                } catch (SQLException e) {
                    AppLogUtil.log(this.getClass(), "error", e);

                    ErrorText += "\n";
                    ErrorText += e.toString();
                    txtLogErr.setText(logTab + ErrorText);
                }

                try {
                    sqlUpdateStkfile = "update stkfile set bqty='" + stkFileBean.getbQty() + "',bamt='" + stkFileBean.getbAmt() + "',btotalamt='" + stkFileBean.getbTotalAmt() + "',"
                            + "bqty0='" + stkFileBean.getbQty0() + "',bqty1='" + stkFileBean.getbQty1() + "',bqty2='" + stkFileBean.getbQty2() + "',bqty3='" + stkFileBean.getbQty3() + "',bqty4='" + stkFileBean.getbQty4() + "',bqty5='" + stkFileBean.getbQty5() + "',"
                            + "bqty6='" + stkFileBean.getbQty6() + "',bqty7='" + stkFileBean.getbQty7() + "',bqty8='" + stkFileBean.getbQty8() + "',bqty9='" + stkFileBean.getbQty9() + "',bqty10='" + stkFileBean.getbQty10() + "',"
                            + "bqty11='" + stkFileBean.getbQty11() + "',bqty12='" + stkFileBean.getbQty12() + "',bqty13='" + stkFileBean.getbQty13() + "',bqty14='" + stkFileBean.getbQty14() + "',bqty15='" + stkFileBean.getbQty15() + "',"
                            + "bqty16='" + stkFileBean.getbQty16() + "',bqty17='" + stkFileBean.getbQty17() + "',bqty18='" + stkFileBean.getbQty18() + "',bqty19='" + stkFileBean.getbQty19() + "',bqty20='" + stkFileBean.getbQty20() + "',"
                            + "bqty21='" + stkFileBean.getbQty21() + "',bqty22='" + stkFileBean.getbQty22() + "',bqty23='" + stkFileBean.getbQty23() + "',bqty24='" + stkFileBean.getbQty24() + "',branch='" + stkFileBean.getBranch() + "',"
                            + "lastupdate='" + dateConvert.GetCurrentDate() + "' ,lastTimeUpdate='" + dateConvert.GetCurrentTime() + "'"
                            + " where bpcode='" + stkFileBean.getbPcode() + "' and branch='" + stkFileBean.getBranch() + "';";
                    lblDisplayStcard1.setText("Update STKFile : " + 0);
                    try {
                        mysqlWebOnline.getConnection().createStatement().executeUpdate(sqlUpdateStkfile);
                        tempText += sqlUpdateStkfile;
                        txtSql.setText(tempText + "\r\n");
                    } catch (SQLException e) {
                        AppLogUtil.log(this.getClass(), "error", e);
                        ErrorText += "\n";
                        ErrorText += e.toString();
                        txtLogErr.setText(logTab + ErrorText);
                    }
                } catch (Exception e) {
                    AppLogUtil.log(this.getClass(), "error", e);
                }

                lblDisplayStcard1.setBackground(Color.green);
            }
        } else {
            List<STKFileBean> listStkFile = new ArrayList<>();

            try {
                String sql = "select * from stkfile;";
                try (ResultSet rs1 = mysqlLocal.getConnection().createStatement().executeQuery(sql)) {
                    loadStatus();

                    while (rs1.next()) {
                        STKFileBean stkFileBean = new STKFileBean();
                        stkFileBean.setbPcode(rs1.getString("BPCode"));
                        stkFileBean.setbStk(rs1.getString("BStk"));
                        stkFileBean.setbQty(rs1.getDouble("BQty"));
                        stkFileBean.setbAmt(rs1.getDouble("BAmt"));
                        stkFileBean.setbTotalAmt(rs1.getDouble("BTotalAmt"));
                        stkFileBean.setbQty0(rs1.getDouble("BQty0"));
                        stkFileBean.setbQty1(rs1.getDouble("BQty1"));
                        stkFileBean.setbQty2(rs1.getDouble("BQty2"));
                        stkFileBean.setbQty3(rs1.getDouble("BQty3"));
                        stkFileBean.setbQty4(rs1.getDouble("BQty4"));
                        stkFileBean.setbQty5(rs1.getDouble("BQty5"));
                        stkFileBean.setbQty6(rs1.getDouble("BQty6"));
                        stkFileBean.setbQty7(rs1.getDouble("BQty7"));
                        stkFileBean.setbQty8(rs1.getDouble("BQty8"));
                        stkFileBean.setbQty9(rs1.getDouble("BQty9"));
                        stkFileBean.setbQty10(rs1.getDouble("BQty10"));
                        stkFileBean.setbQty11(rs1.getDouble("BQty11"));
                        stkFileBean.setbQty12(rs1.getDouble("BQty12"));
                        stkFileBean.setbQty13(rs1.getDouble("BQty13"));
                        stkFileBean.setbQty14(rs1.getDouble("BQty14"));
                        stkFileBean.setbQty15(rs1.getDouble("BQty15"));
                        stkFileBean.setbQty16(rs1.getDouble("BQty16"));
                        stkFileBean.setbQty17(rs1.getDouble("BQty17"));
                        stkFileBean.setbQty18(rs1.getDouble("BQty18"));
                        stkFileBean.setbQty19(rs1.getDouble("BQty19"));
                        stkFileBean.setbQty20(rs1.getDouble("BQty20"));
                        stkFileBean.setbQty21(rs1.getDouble("BQty21"));
                        stkFileBean.setbQty22(rs1.getDouble("BQty22"));
                        stkFileBean.setbQty23(rs1.getDouble("BQty23"));
                        stkFileBean.setbQty24(rs1.getDouble("BQty24"));
                        stkFileBean.setBranch(branchCode);

                        listStkFile.add(stkFileBean);
                    }
                }
            } catch (SQLException e) {
                AppLogUtil.log(this.getClass(), "error", e);
                ErrorText += "\n";
                ErrorText += e.toString();
                txtLogErr.setText(logTab + ErrorText);
            }

            String sqlUpdateStkfile;
            String tempText = "";
            if (!listStkFile.isEmpty()) {
                try {
                    for (int i = 0; i < listStkFile.size(); i++) {

                        loadStatus();
                        try {
                            String sqlCheckSTKServer = "select bpcode,branch from stkfile "
                                    + "where branch='" + listStkFile.get(i).getBranch() + "' "
                                    + "and bpcode='" + listStkFile.get(i).getbPcode() + "' limit 1;";
                            try (ResultSet rsSV = mysqlWebOnline.getConnection().createStatement().executeQuery(sqlCheckSTKServer)) {
                                if (!rsSV.next()) {
                                    String sqlInsStkfile = "insert ignore into "
                                            + "stkfile (bpcode,branch) values('" + listStkFile.get(i).getbPcode() + "','" + listStkFile.get(i).getBranch() + "');";
                                    mysqlWebOnline.getConnection().createStatement().executeUpdate(sqlInsStkfile);
                                }
                            }
                        } catch (SQLException e) {
                            AppLogUtil.log(this.getClass(), "error", e);
                        }

                        sqlUpdateStkfile = "update stkfile set bqty='" + listStkFile.get(i).getbQty() + "',bamt='" + listStkFile.get(i).getbAmt() + "',"
                                + "btotalamt='" + listStkFile.get(i).getbTotalAmt() + "',"
                                + "bqty0='" + listStkFile.get(i).getbQty0() + "',bqty1='" + listStkFile.get(i).getbQty1() + "',"
                                + "bqty2='" + listStkFile.get(i).getbQty2() + "',bqty3='" + listStkFile.get(i).getbQty3() + "',"
                                + "bqty4='" + listStkFile.get(i).getbQty4() + "',bqty5='" + listStkFile.get(i).getbQty5() + "',"
                                + "bqty6='" + listStkFile.get(i).getbQty6() + "',bqty7='" + listStkFile.get(i).getbQty7() + "',"
                                + "bqty8='" + listStkFile.get(i).getbQty8() + "',bqty9='" + listStkFile.get(i).getbQty9() + "',"
                                + "bqty10='" + listStkFile.get(i).getbQty10() + "',"
                                + "bqty11='" + listStkFile.get(i).getbQty11() + "',bqty12='" + listStkFile.get(i).getbQty12() + "',"
                                + "bqty13='" + listStkFile.get(i).getbQty13() + "',bqty14='" + listStkFile.get(i).getbQty14() + "',"
                                + "bqty15='" + listStkFile.get(i).getbQty15() + "',"
                                + "bqty16='" + listStkFile.get(i).getbQty16() + "',bqty17='" + listStkFile.get(i).getbQty17() + "',"
                                + "bqty18='" + listStkFile.get(i).getbQty18() + "',bqty19='" + listStkFile.get(i).getbQty19() + "',"
                                + "bqty20='" + listStkFile.get(i).getbQty20() + "',"
                                + "bqty21='" + listStkFile.get(i).getbQty21() + "',bqty22='" + listStkFile.get(i).getbQty22() + "',"
                                + "bqty23='" + listStkFile.get(i).getbQty23() + "',bqty24='" + listStkFile.get(i).getbQty24() + "',"
                                + "branch='" + listStkFile.get(i).getBranch() + "',"
                                + "lastupdate='" + dateConvert.GetCurrentDate() + "' ,lastTimeUpdate='" + dateConvert.GetCurrentTime() + "'"
                                + " where bpcode='" + listStkFile.get(i).getbPcode() + "' and branch='" + listStkFile.get(i).getBranch() + "';";
                        btnStatus1.setText("Noplu STKFILE Update : " + i + " " + listStkFile.get(i).getbPcode());

                        mysqlWebOnline.getConnection().createStatement().executeUpdate(sqlUpdateStkfile);
                        tempText += sqlUpdateStkfile;
                        txtSql.setText(tempText + "\r\n");

                        String sqlUpdateSendWeb = "update stkfile set "
                                + "Lastupdate='" + dateConvert.GetCurrentDate() + "',"
                                + "LastTimeUpdate='" + dateConvert.GetCurrentTime() + "' "
                                + "where bpcode='" + listStkFile.get(i).getbPcode() + "';";
                        mysqlLocal.getConnection().createStatement().executeUpdate(sqlUpdateSendWeb);
                    }
                } catch (SQLException e) {
                    AppLogUtil.log(this.getClass(), "error", e);
                    ErrorText += "\n";
                    ErrorText += e.toString();
                    txtLogErr.setText(logTab + ErrorText);
                }
            }
        }
    }

    public STCardBean matchDiscount(String s_No, String s_Date, String s_PCode, double s_out, double s_outCost) {
        STCardBean bean = new STCardBean();
        String sql;
        String refno = " ";
        String macno = "";
        String r_time = "";
        String indexNoCheck = "";
        if (s_outCost < 0 && s_outCost != 0) {
            s_outCost = s_outCost * -1;
            s_out = s_out * -1;
        }
        DecimalFormat df = new DecimalFormat("#0");
        try {
            indexNoCheck = s_No.substring(0, 1);
        } catch (Exception e) {
        }

        try {
            //ถ้าวันที่ไม่ใช่วันปัจจุบัน
            if (!s_Date.equals(dateConvert.GetCurrentDate())) {
                String[] strs = s_No.split("-");

                for (String data : strs) {
                    macno = strs[0];
                    if (s_No.length() == 14 && s_No.substring(3, 6).equals("-1-")) {
                        r_time = strs[2];
                    } else {
                        r_time = strs[1];
                    }
                }

                sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                        + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time, r_quan, r_plucode  "
                        + "from s_tran "
                        + "where macno='" + macno + "' and r_time='" + r_time + "' "
                        + "and r_plucode='" + s_PCode + "' and r_date='" + s_Date + "' limit 1;";
                //ถ้าเป็นเอกสาร คืนสินค้า
                if (indexNoCheck.equals("R")) {

                    macno = s_No.substring(2, 5);
                    String[] strs1 = s_No.split("/");
                    for (String data : strs1) {
                        refno = strs1[1];
                    }
                    sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath , "
                            + "r_refund, cashier Cashier, r_emp R_Emp,r_time, r_quan ,r_plucode "
                            + "from s_tran "
                            + "where macno='" + macno + "' "
                            + "and r_refno='" + refno + "' "
                            + "and r_plucode='" + s_PCode + "' "
                            + "and r_date='" + s_Date + "' "
                            + "and r_quan='" + df.format(s_out) + "' "
                            + "and r_nettotal = '" + df.format(s_outCost) + "' "
                            + "limit 1;";
                }
                ResultSet rs = mysqlLocal.getConnection().createStatement().executeQuery(sql);
                if (rs.next()) {
                    if (rs.getDouble("R_Total") < 0) {
                        bean.setDiscount((rs.getDouble("R_Total") - rs.getDouble("R_Nettotal")));
                    } else {
                        bean.setDiscount((rs.getDouble("R_Total") - rs.getDouble("R_Nettotal")));
                    }
                    bean.setNettotal(rs.getDouble("R_Nettotal"));
                    bean.setRefund(rs.getString("R_Refund"));
                    if (indexNoCheck.equals("R")) {
                        bean.setRefNo(s_No);
                        bean.setNettotal(rs.getDouble("R_Nettotal") * rs.getDouble("r_quan") * -1);

                        bean.setDiscount(bean.getDiscount() * rs.getDouble("r_quan") * -1);
                    } else {
                        bean.setRefNo(rs.getString("R_Refno"));
                    }

                    bean.setCashier("Cashier");
                    bean.setEmp(rs.getString("R_Emp"));
                    if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                        bean.setNettotal(0);
                    }
                    bean.setR_time(rs.getString("r_time"));
                } else {
                    try {
                        if (indexNoCheck.equals("R")) {
                            String[] strs1 = s_No.split("/");
                            for (String data : strs1) {
                                refno = strs1[1];
                            }
                            if (indexNoCheck.equals("R")) {
                                sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath"
                                        + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time "
                                        + "from s_tran "
                                        + "where macno='" + macno + "' and r_refno='" + refno + "' "
                                        + "and r_plucode='" + s_PCode + "' and r_date='" + s_Date + "' limit 1;";
                                try {
                                    ResultSet rsNew = mysqlLocal.getConnection().createStatement().executeQuery(sql);
                                    if (rsNew.next()) {
                                        bean.setDiscount((rsNew.getDouble("R_Total") - rsNew.getDouble("R_Nettotal")) * -1);
                                        if (rsNew.getDouble("R_Nettotal") != 0) {
                                            bean.setNettotal(rsNew.getDouble("R_Nettotal") * -1);
                                        } else {
                                            bean.setNettotal(rsNew.getDouble("R_Nettotal"));
                                        }

                                        bean.setRefund(rsNew.getString("R_Refund"));
                                        if (indexNoCheck.equals("R")) {
                                            bean.setRefNo(s_No);
                                        } else {
                                            bean.setRefNo(rsNew.getString("R_Refno"));
                                        }

                                        bean.setCashier("Cashier");
                                        bean.setEmp(rsNew.getString("R_Emp"));
                                    }
                                } catch (SQLException e) {
                                    AppLogUtil.log(this.getClass(), "error", e);
                                }
                                if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                                    bean.setNettotal(0);
                                }
                            }
                        } else {
                            System.out.println("Time is : " + r_time);
                            System.out.println("S_No: " + s_No + " S_Date: " + s_Date + "S_Pcode " + s_PCode);
                            DecimalFormat intFM = new DecimalFormat("00");
                            String[] strsTime = s_No.split("-");

                            for (String data : strsTime) {
                                macno = strsTime[0];
                                //ดักไว้ หากมีผิดพลาดเรื่อง Index 001-1-124451 หาไม่เจอว่ามาจากอะไร
                                if (s_No.length() == 14 && s_No.substring(3, 6).equals("-1-")) {
                                    r_time = strsTime[2];
                                } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-2-")) {
                                    r_time = strsTime[2];
                                } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-3-")) {
                                    r_time = strsTime[2];
                                } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-4-")) {
                                    r_time = strsTime[2];
                                } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-5-")) {
                                    r_time = strsTime[2];
                                } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-6-")) {
                                    r_time = strsTime[2];
                                } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-7-")) {
                                    r_time = strsTime[2];
                                } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-8-")) {
                                    r_time = strsTime[2];
                                } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-9-")) {
                                    r_time = strsTime[2];
                                } else {
                                    r_time = strsTime[1];
                                }
                                System.out.println(r_time);
                            }
                            int hh = Integer.parseInt(r_time.substring(0, 2));
                            int mm = Integer.parseInt(r_time.substring(3, 5));
                            int ss = Integer.parseInt(r_time.substring(6, 8));
                            ss = ss - 1;
                            if (ss == -1) {
                                ss = 59;
                                mm = mm - 1;
                            }
                            if (mm == -1) {
                                hh = hh - 1;
                            }
                            String r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss);
                            System.out.println("NewTime= " + r_newTime);

                            sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath"
                                    + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time "
                                    + "from s_tran "
                                    + "where macno='" + macno + "' and r_time='" + r_newTime + "' "
                                    + "and r_plucode='" + s_PCode + "' and r_date='" + s_Date + "' limit 1;";
                            bean.setR_time(r_newTime);
                            if (indexNoCheck.equals("R")) {
                                macno = s_No.substring(2, 5);
                                String[] strs1 = s_No.split("/");
                                for (String data : strs1) {
                                    refno = strs1[1];
                                }
                                sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                                        + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time "
                                        + "from s_tran "
                                        + "where macno='" + macno + "' and r_refno='" + refno + "' "
                                        + "and r_plucode='" + s_PCode + "' and r_date='" + s_Date + "' limit 1;";
                            }
                            try {
                                ResultSet rsNew = mysqlLocal.getConnection().createStatement().executeQuery(sql);
                                if (rsNew.next()) {
                                    if (rsNew.getDouble("R_Total") < 0) {
                                        bean.setDiscount((rsNew.getDouble("R_Total") - rsNew.getDouble("R_Nettotal")));
                                    } else {
                                        bean.setDiscount((rsNew.getDouble("R_Total") - rsNew.getDouble("R_Nettotal")));
                                    }
                                    bean.setNettotal(rsNew.getDouble("R_Nettotal"));
                                    bean.setRefund(rsNew.getString("R_Refund"));
                                    if (indexNoCheck.equals("R")) {
                                        bean.setRefNo(s_No);
                                    } else {
                                        bean.setRefNo(rsNew.getString("R_Refno"));
                                    }

                                    bean.setCashier("Cashier");
                                    bean.setEmp(rsNew.getString("R_Emp"));
                                }
                            } catch (SQLException e) {
                                AppLogUtil.log(this.getClass(), "error", e);
                            }
                            if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                                bean.setNettotal(0);
                            }
                        }

                    } catch (NumberFormatException e) {
                    }

                }
            } else {
                try {
                    //ถ้าข้อมูลเป็นวันปัจจุบัน
                    indexNoCheck = s_No.substring(0, 1);

                    //ถ้าเป็นว่ายกเลิกบิล
                    if (indexNoCheck.equals("R")) {
                        macno = s_No.substring(2, 5);
                        String[] strs1 = s_No.split("/");
                        try {
                            for (String data : strs1) {
                                refno = strs1[1];
                            }
                        } catch (Exception e) {
                        }

                        sql = "select r_refno, R_Total, r_nettotal R_Nettotal, r_pramt, r_discbath"
                                + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time "
                                + "from t_sale "
                                + "where macno='" + macno + "' and r_refno='" + refno + "' "
                                + "and r_plucode='" + s_PCode + "' and r_date='" + s_Date + "' limit 1;";
                        ResultSet rs7 = mysqlLocal.getConnection().createStatement().executeQuery(sql);
                        if (rs7.next()) {
                            if (rs7.getDouble("R_Total") != rs7.getDouble("R_Nettotal")) {
                                bean.setDiscount((rs7.getDouble("R_Total") - rs7.getDouble("R_Nettotal")));
                            } else {
                                bean.setDiscount(0);
                            }
                            bean.setNettotal(rs7.getDouble("R_Nettotal"));
                            bean.setRefund(rs7.getString("R_Refund"));
                            bean.setRefNo(rs7.getString("R_Refno"));
                            bean.setCashier(rs7.getString("Cashier"));
                            bean.setEmp(rs7.getString("R_Emp"));
                            if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                                bean.setNettotal(0);
                            }
                        }
                    } else {
                        //เป็นบิลปกติ ไม่ได้ยกเลิก
                        String[] strs = s_No.split("-");

                        for (String data : strs) {
                            macno = strs[0];
                            //ดักไว้ หากมีผิดพลาดเรื่อง Index 001-1-124451 หาไม่เจอว่ามาจากอะไร
                            if (s_No.length() == 14 && s_No.substring(3, 6).equals("-1-")) {
                                r_time = strs[2];
                            } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-2-")) {
                                r_time = strs[2];
                            } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-3-")) {
                                r_time = strs[2];
                            } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-4-")) {
                                r_time = strs[2];
                            } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-5-")) {
                                r_time = strs[2];
                            } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-6-")) {
                                r_time = strs[2];
                            } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-7-")) {
                                r_time = strs[2];
                            } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-8-")) {
                                r_time = strs[2];
                            } else if (s_No.length() == 14 && s_No.substring(3, 6).equals("-9-")) {
                                r_time = strs[2];
                            } else {
                                r_time = strs[1];
                            }
                            System.out.println(r_time);
                        }
                        sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath,"
                                + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,R_Time "
                                + "from t_sale "
                                + "where "
                                + "r_plucode='" + s_PCode + "' "
                                + "and r_date='" + s_Date + "' "
                                + "limit 1;";
                        ResultSet rs1 = mysqlLocal.getConnection().createStatement().executeQuery(sql);
                        if (rs1.next()) {
                            if (rs1.getDouble("R_Total") != rs1.getDouble("R_Nettotal")) {
                                bean.setDiscount((rs1.getDouble("R_Total") - rs1.getDouble("R_Nettotal")));
                            } else {
                                bean.setDiscount(0);
                            }
                            bean.setNettotal(rs1.getDouble("R_Nettotal"));
                            bean.setRefund(rs1.getString("R_Refund"));
                            bean.setRefNo(rs1.getString("R_Refno"));
                            bean.setCashier("Cashier");
                            bean.setEmp(rs1.getString("R_Emp"));
                            if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                                bean.setNettotal(0);
                            }
                        } else {

                            DecimalFormat intFM = new DecimalFormat("00");
                            int hh = 0;
                            int mm = 0;
                            int ss = 0;
                            String r_newTime = "";
                            if (!indexNoCheck.equals("R")) {
                                hh = Integer.parseInt(r_time.substring(0, 2));
                                mm = Integer.parseInt(r_time.substring(3, 5));
                                ss = Integer.parseInt(r_time.substring(6, 8));
                                ss = ss - 1;
                                if (ss == -1) {
                                    ss = 59;
                                    mm = mm - 1;
                                }
                                if (mm == -1) {
                                    hh = hh - 1;
                                }
                                r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss);
                                sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath"
                                        + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time "
                                        + "from t_sale "
                                        + "where macno='" + macno + "' and r_time='" + r_newTime + "' "
                                        + "and r_plucode='" + s_PCode + "' and r_date='" + s_Date + "' limit 1;";
                            } else {
                                sql = "select r_refno, r_total, r_nettotal, r_pramt, r_discbath"
                                        + " r_refno, r_refund, cashier Cashier, r_emp R_Emp,r_time "
                                        + "from t_sale "
                                        + "where macno='" + macno + "' and r_refno='" + refno + "' "
                                        + "and r_plucode='" + s_PCode + "' and r_date='" + s_Date + "' limit 1;";
                            }

                            try {
                                ResultSet rsNew1 = mysqlLocal.getConnection().createStatement().executeQuery(sql);
                                if (rsNew1.next()) {
                                    if (rsNew1.getDouble("R_Total") < 0) {
                                        bean.setDiscount((rsNew1.getDouble("R_Total") + rsNew1.getDouble("R_Nettotal")));
                                    } else {
                                        bean.setDiscount((rsNew1.getDouble("R_Total") - rsNew1.getDouble("R_Nettotal")));
                                    }

                                    bean.setNettotal(rsNew1.getDouble("R_Nettotal"));
                                    bean.setRefund(rsNew1.getString("R_Refund"));
                                    bean.setRefNo(rsNew1.getString("R_Refno"));
                                    bean.setCashier("Cashier");
                                    bean.setEmp(rsNew1.getString("R_Emp"));
                                    if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                                        bean.setNettotal(0);
                                    }
                                }
                            } catch (SQLException e) {
                                AppLogUtil.log(this.getClass(), "error", e);
                            }

                        }
                    }
                } catch (NumberFormatException | SQLException e) {
                }

            }

        } catch (SQLException e) {
            AppLogUtil.log(this.getClass(), "error", e);
            System.out.println(e);
            ErrorText += e.toString();
            txtLogErr.setText(ErrorText);

        }
        return bean;
    }

    public double getCostfile(String pcode) {
        double pscost = 0;
        try {
            Statement stmtLocal = mysqlLocal.getConnection().createStatement();
            String sqlGetCostfile = "select pscost,pacost,plcost from product where pcode ='" + pcode + "';";
            try (ResultSet rsCostfile = stmtLocal.executeQuery(sqlGetCostfile)) {
                if (rsCostfile.next()) {
                    pscost = rsCostfile.getDouble("pscost");
                }
            }

        } catch (SQLException e) {
            System.out.println(e);
            ErrorText += e.toString();
            txtLogErr.setText(ErrorText);
        }
        return pscost;
    }

    public void loadStatus() {
        new Thread(() -> {
            //check ftp file date
            try {
                pbCheckUpdate.setStringPainted(true);
                pbCheckUpdate.setMinimum(0);
                pbCheckUpdate.setMaximum(100);
                for (int i = 1; i <= 100; i++) {
                    pbCheckUpdate.setValue(i);
                    pbCheckUpdate.setString("LOADDING Data: (" + i + " %)");
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                    }
                }
                
                pbCheckUpdate.setString("Load data Complete ");
            } catch (Exception e) {
            }
        }).start();
    }

    public double totalCompareNettotal(STCardBean bean) {
        if (bean.getS_OutCost() != 0 && bean.getS_Rem().equals("SAL") && bean.getNettotal() == 0) {
            bean.setNettotal(bean.getS_OutCost());
        }
        return bean.getNettotal();

    }

}
