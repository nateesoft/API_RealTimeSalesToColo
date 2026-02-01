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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
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
     * ดึงค่า r_time จาก s_No โดยตรวจสอบ format ของเลขที่เอกสาร
     * รูปแบบ: XXX-N-XXXXXX (14 ตัวอักษร) โดย N = 1-9 จะใช้ strs[2]
     * รูปแบบอื่น: ใช้ strs[1]
     */
    private String extractRTime(String s_No, String[] strs) {
        if (s_No.length() == 14) {
            char middleChar = s_No.charAt(4);
            if (middleChar >= '1' && middleChar <= '9') {
                return strs[2];
            }
        }
        return strs[1];
    }

    /**
     * เปิด Connection ทั้ง Local และ Web ควรเรียกก่อนเริ่มทำงาน
     */
    public void openConnections() {
        try {
            mysqlLocal = new MySQLConnect();
            mysqlLocal.open(this.getClass());

            mysqlWebOnline = new MySQLConnectWebOnline();
            mysqlWebOnline.open();

        } catch (Exception e) {
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * ปิด Connection ทั้งหมด ควรเรียกเมื่อทำงานเสร็จ
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
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * ตรวจสอบและเปิด Connection ใหม่ถ้าจำเป็น ปิด connection
     * เก่าก่อนสร้างใหม่เพื่อป้องกัน connection leak
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
                        Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                mysqlLocal = new MySQLConnect();
                mysqlLocal.open(this.getClass());
            }

            // ตรวจสอบ Web Online Connection
            if (mysqlWebOnline == null || mysqlWebOnline.getConnection() == null || mysqlWebOnline.getConnection().isClosed()) {
                // ปิด connection เก่าก่อน (ถ้ามี)
                if (mysqlWebOnline != null) {
                    try {
                        mysqlWebOnline.close();
                    } catch (Exception e) {
                        // ignore - connection อาจปิดไปแล้ว
                        Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                mysqlWebOnline = new MySQLConnectWebOnline();
                mysqlWebOnline.open();
                System.out.println("Web connection reopened");
            }
        } catch (SQLException e) {
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void uploadCheckConfig() {
        String sendRealtimeWeb = ConfigFile.getProperties("sendRealtimeWeb");

        txtLogErr.setText(configCheck + "sendRealtimeWeb Value=" + sendRealtimeWeb);
        txtLogErr.setText(configCheck + "DB: Value=" + ConfigFile.getProperties("database"));
    }

    public void uploadBillno(String branchCode) {
        ensureConnectionsOpen();
        try {
            btnStatus.setBackground(Color.GREEN);
            String sqlSelect = "SELECT b_refno FROM billno WHERE B_SendOnline = ?";

            String sqlInsertOnline = "INSERT INTO billno VALUES("
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 1-10
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 11-20
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 21-30
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 31-40
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 41-50
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 51-60
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 61-70
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 71-80
                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " // 81-90
                    + "?, ?, ?, ?, ?, ?)";              // 91-96

            String sqlUpdateFlag = "UPDATE billno SET b_sendOnline = ? WHERE b_refno = ? AND b_macno = ?";

            try (PreparedStatement pstmtSelect = mysqlLocal.getConnection().prepareStatement(sqlSelect);
                 PreparedStatement pstmtInsert = mysqlWebOnline.getConnection().prepareStatement(sqlInsertOnline);
                 PreparedStatement pstmtUpdate = mysqlLocal.getConnection().prepareStatement(sqlUpdateFlag)) {

                pstmtSelect.setString(1, "N");
                try (ResultSet rs = pstmtSelect.executeQuery()) {
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

                    try {
                        pstmtInsert.setString(1, billbean.getB_Refno());
                        pstmtInsert.setString(2, String.valueOf(billbean.getB_CuponDiscAmt()));
                        pstmtInsert.setString(3, billbean.getB_Ontime());
                        pstmtInsert.setString(4, billbean.getB_LoginTime());
                        pstmtInsert.setString(5, String.valueOf(billbean.getB_OnDate()));
                        pstmtInsert.setString(6, String.valueOf(billbean.getB_PostDate()));
                        pstmtInsert.setString(7, billbean.getB_Table());
                        pstmtInsert.setString(8, billbean.getB_MacNo());
                        pstmtInsert.setString(9, billbean.getB_Cashier());
                        pstmtInsert.setString(10, String.valueOf(billbean.getB_Cust()));
                        pstmtInsert.setString(11, billbean.getB_ETD());
                        pstmtInsert.setString(12, String.valueOf(billbean.getB_Total()));
                        pstmtInsert.setString(13, String.valueOf(billbean.getB_Food()));
                        pstmtInsert.setString(14, String.valueOf(billbean.getB_Drink()));
                        pstmtInsert.setString(15, String.valueOf(billbean.getB_Product()));
                        pstmtInsert.setString(16, String.valueOf(billbean.getB_Service()));
                        pstmtInsert.setString(17, String.valueOf(billbean.getB_ServiceAmt()));
                        pstmtInsert.setString(18, String.valueOf(billbean.getB_ItemDiscAmt()));
                        pstmtInsert.setString(19, billbean.getB_FastDisc());
                        pstmtInsert.setString(20, String.valueOf(billbean.getB_FastDiscAmt()));
                        pstmtInsert.setString(21, billbean.getB_EmpDisc());
                        pstmtInsert.setString(22, String.valueOf(billbean.getB_EmpDiscAmt()));
                        pstmtInsert.setString(23, billbean.getB_TrainDisc());
                        pstmtInsert.setString(24, String.valueOf(billbean.getB_TrainDiscAmt()));
                        pstmtInsert.setString(25, billbean.getB_MemDisc());
                        pstmtInsert.setString(26, String.valueOf(billbean.getB_MemDiscAmt()));
                        pstmtInsert.setString(27, billbean.getB_SubDisc());
                        pstmtInsert.setString(28, String.valueOf(billbean.getB_SubDiscAmt()));
                        pstmtInsert.setString(29, String.valueOf(billbean.getB_SubDiscBath()));
                        pstmtInsert.setString(30, String.valueOf(billbean.getB_ProDiscAmt()));
                        pstmtInsert.setString(31, String.valueOf(billbean.getB_SpaDiscAmt()));
                        pstmtInsert.setString(32, String.valueOf(billbean.getB_AdjAmt()));
                        pstmtInsert.setString(33, String.valueOf(billbean.getB_PreDisAmt()));
                        pstmtInsert.setString(34, String.valueOf(billbean.getB_NetTotal()));
                        pstmtInsert.setString(35, String.valueOf(billbean.getB_NetFood()));
                        pstmtInsert.setString(36, String.valueOf(billbean.getB_NetDrink()));
                        pstmtInsert.setString(37, String.valueOf(billbean.getB_NetProduct()));
                        pstmtInsert.setString(38, String.valueOf(billbean.getB_NetVat()));
                        pstmtInsert.setString(39, String.valueOf(billbean.getB_NetNonVat()));
                        pstmtInsert.setString(40, String.valueOf(billbean.getB_Vat()));
                        pstmtInsert.setString(41, String.valueOf(billbean.getB_PayAmt()));
                        pstmtInsert.setString(42, String.valueOf(billbean.getB_Cash()));
                        pstmtInsert.setString(43, String.valueOf(billbean.getB_GiftVoucher()));
                        pstmtInsert.setString(44, String.valueOf(billbean.getB_Earnest()));
                        pstmtInsert.setString(45, String.valueOf(billbean.getB_Ton()));
                        pstmtInsert.setString(46, billbean.getB_CrCode1());
                        pstmtInsert.setString(47, billbean.getB_CardNo1());
                        pstmtInsert.setString(48, billbean.getB_AppCode1());
                        pstmtInsert.setString(49, String.valueOf(billbean.getB_CrCharge1()));
                        pstmtInsert.setString(50, String.valueOf(billbean.getB_CrChargeAmt1()));
                        pstmtInsert.setString(51, String.valueOf(billbean.getB_CrAmt1()));
                        pstmtInsert.setString(52, billbean.getB_AccrCode());
                        pstmtInsert.setString(53, String.valueOf(billbean.getB_AccrAmt()));
                        pstmtInsert.setString(54, String.valueOf(billbean.getB_AccrCr()));
                        pstmtInsert.setString(55, billbean.getB_MemCode());
                        pstmtInsert.setString(56, billbean.getB_MemName());
                        pstmtInsert.setString(57, String.valueOf(billbean.getB_MemBegin()));
                        pstmtInsert.setString(58, String.valueOf(billbean.getB_MemEnd()));
                        pstmtInsert.setString(59, String.valueOf(billbean.getB_MemCurSum()));
                        pstmtInsert.setString(60, billbean.getB_Void());
                        pstmtInsert.setString(61, billbean.getB_VoidUser());
                        pstmtInsert.setString(62, billbean.getB_VoidTime());
                        pstmtInsert.setString(63, String.valueOf(billbean.getB_BillCopy()));
                        pstmtInsert.setString(64, String.valueOf(billbean.getB_PrnCnt()));
                        pstmtInsert.setString(65, billbean.getB_PrnTime1());
                        pstmtInsert.setString(66, billbean.getB_PrnTime2());
                        pstmtInsert.setString(67, billbean.getB_InvNo());
                        pstmtInsert.setString(68, billbean.getB_InvType());
                        pstmtInsert.setString(69, branchCode);
                        pstmtInsert.setString(70, billbean.getB_BranName());
                        pstmtInsert.setString(71, billbean.getB_Tel());
                        pstmtInsert.setString(72, billbean.getB_RecTime());
                        pstmtInsert.setString(73, billbean.getMStamp());
                        pstmtInsert.setString(74, billbean.getMScore());
                        pstmtInsert.setString(75, billbean.getCurStamp());
                        pstmtInsert.setString(76, billbean.getStampRate());
                        pstmtInsert.setString(77, billbean.getB_ChkBill());
                        pstmtInsert.setString(78, billbean.getB_ChkBillTime());
                        pstmtInsert.setString(79, billbean.getB_CashTime());
                        pstmtInsert.setString(80, billbean.getB_WaitTime());
                        pstmtInsert.setString(81, String.valueOf(billbean.getB_SumScore()));
                        pstmtInsert.setString(82, billbean.getB_CrBank());
                        pstmtInsert.setString(83, String.valueOf(billbean.getB_CrCardAmt()));
                        pstmtInsert.setString(84, String.valueOf(billbean.getB_CrCurPoint()));
                        pstmtInsert.setString(85, String.valueOf(billbean.getB_CrSumPoint()));
                        pstmtInsert.setString(86, String.valueOf(billbean.getB_Entertain1()));
                        pstmtInsert.setString(87, String.valueOf(billbean.getB_VoucherDiscAmt()));
                        pstmtInsert.setString(88, String.valueOf(billbean.getB_VoucherOver()));
                        pstmtInsert.setString(89, String.valueOf(billbean.getB_NetDiff()));
                        pstmtInsert.setString(90, String.valueOf(billbean.getB_SumSetDiscAmt()));
                        pstmtInsert.setString(91, String.valueOf(billbean.getB_DetailFood()));
                        pstmtInsert.setString(92, String.valueOf(billbean.getB_DetailDrink()));
                        pstmtInsert.setString(93, String.valueOf(billbean.getB_DetailProduct()));
                        pstmtInsert.setString(94, billbean.getB_KicQue());
                        pstmtInsert.setString(95, billbean.getB_ROUNDCLOSE());
                        pstmtInsert.setString(96, branchCode);

                        pstmtInsert.executeUpdate();
                    } catch (SQLException e) {
                        ErrorText += "\n";
                        ErrorText += e.toString();
                        txtLogErr.setText(logTab + ErrorText);
                    }

                    pstmtUpdate.setString(1, "Y");
                    pstmtUpdate.setString(2, billbean.getB_Refno());
                    pstmtUpdate.setString(3, billbean.getB_MacNo());
                    pstmtUpdate.executeUpdate();
                    System.out.println("Updated billno flag: " + billbean.getB_Refno());
                    }
                }
                System.out.println("Loop Billno Finished;");
                ErrorText += "loop Billno Finished Insert";
            } catch (SQLException e) {
                Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (Exception e) {
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void uploadT_Sale(String branchCode) {
        ensureConnectionsOpen();

        String sqlSelect = "SELECT * FROM t_sale WHERE r_sendOnline = ? ORDER BY macno, r_refno, r_index";

        String sqlInsert = "INSERT INTO t_sale VALUES("
                + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 1-10
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 11-20
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 21-30
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 31-40
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 41-50
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 51-60
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 61-70
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 71-80
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 81-90
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + // 91-100
                "?, ?)";                          // 101-102

        String sqlUpdate = "UPDATE t_sale SET r_sendOnline = ? WHERE R_refno = ? AND r_index = ? AND macno = ?";

        try (PreparedStatement pstmtSelect = mysqlLocal.getConnection().prepareStatement(sqlSelect);
             PreparedStatement pstmtInsert = mysqlWebOnline.getConnection().prepareStatement(sqlInsert);
             PreparedStatement pstmtUpdate = mysqlLocal.getConnection().prepareStatement(sqlUpdate)) {

            pstmtSelect.setString(1, "N");
            try (ResultSet rs = pstmtSelect.executeQuery()) {
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
                try {
                    pstmtInsert.setString(1, bean.getR_Index());
                    pstmtInsert.setString(2, bean.getR_Refno());
                    pstmtInsert.setString(3, bean.getR_Table());
                    pstmtInsert.setString(4, String.valueOf(bean.getR_Date()));
                    pstmtInsert.setString(5, bean.getR_Time());
                    pstmtInsert.setString(6, bean.getMacNo());
                    pstmtInsert.setString(7, bean.getCashier());
                    pstmtInsert.setString(8, bean.getR_Emp());
                    pstmtInsert.setString(9, bean.getR_PluCode());
                    pstmtInsert.setString(10, bean.getR_PName());
                    pstmtInsert.setString(11, bean.getR_Unit());
                    pstmtInsert.setString(12, bean.getR_Group());
                    pstmtInsert.setString(13, bean.getR_Status());
                    pstmtInsert.setString(14, bean.getR_Normal());
                    pstmtInsert.setString(15, bean.getR_Discount());
                    pstmtInsert.setString(16, bean.getR_Service());
                    pstmtInsert.setString(17, bean.getR_Stock());
                    pstmtInsert.setString(18, bean.getR_Set());
                    pstmtInsert.setString(19, bean.getR_Vat());
                    pstmtInsert.setString(20, bean.getR_Type());
                    pstmtInsert.setString(21, bean.getR_ETD());
                    pstmtInsert.setString(22, String.valueOf(bean.getR_Quan()));
                    pstmtInsert.setString(23, String.valueOf(bean.getR_Price()));
                    pstmtInsert.setString(24, String.valueOf(bean.getR_Total()));
                    pstmtInsert.setString(25, bean.getR_Type());
                    pstmtInsert.setString(26, bean.getR_PrCode());
                    pstmtInsert.setString(27, String.valueOf(bean.getR_PrDisc()));
                    pstmtInsert.setString(28, String.valueOf(bean.getR_PrBath()));
                    pstmtInsert.setString(29, String.valueOf(bean.getR_PrAmt()));
                    pstmtInsert.setString(30, bean.getR_PrCuType());
                    pstmtInsert.setString(31, bean.getR_PrCuCode());
                    pstmtInsert.setString(32, String.valueOf(bean.getR_PrCuQuan()));
                    pstmtInsert.setString(33, String.valueOf(bean.getR_PrCuAmt()));
                    pstmtInsert.setString(34, String.valueOf(bean.getR_Redule()));
                    pstmtInsert.setString(35, String.valueOf(bean.getR_DiscBath()));
                    pstmtInsert.setString(36, String.valueOf(bean.getR_PrAdj()));
                    pstmtInsert.setString(37, String.valueOf(bean.getR_PreDisAmt()));
                    pstmtInsert.setString(38, String.valueOf(bean.getR_NetTotal()));
                    pstmtInsert.setString(39, bean.getR_Kic());
                    pstmtInsert.setString(40, bean.getR_KicPrint());
                    pstmtInsert.setString(41, bean.getR_Refund());
                    pstmtInsert.setString(42, bean.getVoidMsg());
                    pstmtInsert.setString(43, bean.getR_Void());
                    pstmtInsert.setString(44, bean.getR_VoidUser());
                    pstmtInsert.setString(45, bean.getR_VoidTime());
                    pstmtInsert.setString(46, bean.getStkCode());
                    pstmtInsert.setString(47, bean.getPosStk());
                    pstmtInsert.setString(48, String.valueOf(bean.getR_ServiceAmt()));
                    pstmtInsert.setString(49, bean.getR_PrChkType());
                    pstmtInsert.setString(50, String.valueOf(bean.getR_PrQuan()));
                    pstmtInsert.setString(51, bean.getR_PrSubType());
                    pstmtInsert.setString(52, bean.getR_PrSubCode());
                    pstmtInsert.setString(53, String.valueOf(bean.getR_PrSubQuan()));
                    pstmtInsert.setString(54, String.valueOf(bean.getR_PrSubDisc()));
                    pstmtInsert.setString(55, String.valueOf(bean.getR_PrSubBath()));
                    pstmtInsert.setString(56, String.valueOf(bean.getR_PrSubAmt()));
                    pstmtInsert.setString(57, String.valueOf(bean.getR_PrSubAdj()));
                    pstmtInsert.setString(58, String.valueOf(bean.getR_PrCuDisc()));
                    pstmtInsert.setString(59, String.valueOf(bean.getR_PrCuBath()));
                    pstmtInsert.setString(60, String.valueOf(bean.getR_PrCuAdj()));
                    pstmtInsert.setString(61, bean.getR_PrChkType2());
                    pstmtInsert.setString(62, String.valueOf(bean.getR_PrQuan2()));
                    pstmtInsert.setString(63, bean.getR_PrType2());
                    pstmtInsert.setString(64, bean.getR_PrCode2());
                    pstmtInsert.setString(65, String.valueOf(bean.getR_PrDisc2()));
                    pstmtInsert.setString(66, String.valueOf(bean.getR_PrBath2()));
                    pstmtInsert.setString(67, String.valueOf(bean.getR_PrAmt2()));
                    pstmtInsert.setString(68, String.valueOf(bean.getR_PrAdj2()));
                    pstmtInsert.setString(69, String.valueOf(bean.getR_PItemNo()));
                    pstmtInsert.setString(70, String.valueOf(bean.getR_PKicQue()));
                    pstmtInsert.setString(71, bean.getR_PrVcType());
                    pstmtInsert.setString(72, bean.getR_PrVcCode());
                    pstmtInsert.setString(73, String.valueOf(bean.getR_PrVcAmt()));
                    pstmtInsert.setString(74, String.valueOf(bean.getR_PrVcAdj()));
                    pstmtInsert.setString(75, bean.getR_MoveFlag());
                    pstmtInsert.setString(76, bean.getR_Pause());
                    pstmtInsert.setString(77, bean.getR_SPIndex());
                    pstmtInsert.setString(78, bean.getR_LinkIndex());
                    pstmtInsert.setString(79, bean.getR_VoidPause());
                    pstmtInsert.setString(80, String.valueOf(bean.getR_SetPrice()));
                    pstmtInsert.setString(81, String.valueOf(bean.getR_SetDiscAmt()));
                    pstmtInsert.setString(82, bean.getR_MoveItem());
                    pstmtInsert.setString(83, bean.getR_MoveFrom());
                    pstmtInsert.setString(84, bean.getR_MoveUser());
                    pstmtInsert.setString(85, bean.getR_Opt9());
                    pstmtInsert.setString(86, bean.getR_Opt1());
                    pstmtInsert.setString(87, bean.getR_Opt2());
                    pstmtInsert.setString(88, bean.getR_Opt3());
                    pstmtInsert.setString(89, bean.getR_Opt4());
                    pstmtInsert.setString(90, bean.getR_Opt5());
                    pstmtInsert.setString(91, bean.getR_Opt6());
                    pstmtInsert.setString(92, bean.getR_Opt7());
                    pstmtInsert.setString(93, bean.getR_Opt8());
                    pstmtInsert.setString(94, bean.getR_PrintItemBill());
                    pstmtInsert.setString(95, bean.getR_CountTime());
                    pstmtInsert.setString(96, bean.getR_Return());
                    pstmtInsert.setString(97, bean.getR_Earn());
                    pstmtInsert.setString(98, bean.getR_EarnNo());
                    pstmtInsert.setString(99, String.valueOf(bean.getR_NetDiff()));
                    pstmtInsert.setString(100, bean.getR_SendOnline());
                    pstmtInsert.setString(101, bean.getR_BranchCode());
                    pstmtInsert.setString(102, String.valueOf(bean.getR_Cost()));

                    pstmtInsert.executeUpdate();
                    System.out.println("Inserted t_sale: " + bean.getR_Refno() + " - " + bean.getR_Index());
                } catch (SQLException e) {
                    Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                }
                try {
                    pstmtUpdate.setString(1, "Y");
                    pstmtUpdate.setString(2, bean.getR_Refno());
                    pstmtUpdate.setString(3, bean.getR_Index());
                    pstmtUpdate.setString(4, bean.getMacNo());
                    pstmtUpdate.executeUpdate();
                    System.out.println("Updated t_sale flag: " + bean.getR_Refno());
                } catch (SQLException e) {
                    Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                }
                Thread.sleep(90);
                }
            }
            System.out.println("Loop T_Sale Finished;");
            Thread.sleep(10 * 1000);
        } catch (InterruptedException | SQLException e) {
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void uploadStcard(String branchCode) {
        ensureConnectionsOpen();
        List<STCardBean> listSTCardNotSend = new ArrayList<>();
        try {
            String querySTCard = "SELECT * FROM stcard WHERE s_send <> ? and Source_Data <> ?";
            try (PreparedStatement pstmtSelect = mysqlLocal.getConnection().prepareStatement(querySTCard)) {
                pstmtSelect.setString(1, "Y");
                pstmtSelect.setString(2, "WEB");
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    while (rs.next()) {
                        btnStatus.setEnabled(false);

                        STCardBean stCardBean = new STCardBean();
                        stCardBean.setS_Date(rs.getString("s_date"));
                        stCardBean.setS_No(rs.getString("s_no"));
                        stCardBean.setS_SubNo(rs.getString("s_subno"));
                        stCardBean.setS_Que(rs.getInt("s_que"));
                        stCardBean.setS_PCode(rs.getString("s_pcode"));
                        stCardBean.setS_Stk(rs.getString("s_stk"));
                        stCardBean.setS_In(rs.getDouble("s_in"));
                        stCardBean.setS_Out(rs.getDouble("s_out"));
                        stCardBean.setS_InCost(rs.getDouble("s_incost"));
                        stCardBean.setS_OutCost(rs.getDouble("s_outcost"));
                        stCardBean.setS_ACost(rs.getDouble("s_aCost"));
                        stCardBean.setS_Rem(rs.getString("s_rem"));
                        stCardBean.setS_User(rs.getString("s_User"));
                        stCardBean.setS_EntryDate(rs.getString("s_entrydate"));
                        stCardBean.setS_EntryTime(rs.getString("s_entrytime"));
                        stCardBean.setS_Link(rs.getString("s_link"));
                        stCardBean.setS_Send(rs.getString("s_send"));

                        listSTCardNotSend.add(stCardBean);
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
        }

        // PreparedStatement สำหรับ INSERT และ UPDATE
        String sqlInsertStcard = "INSERT INTO stcard ("
                + "s_date, s_no, s_subno, s_que, s_pcode, s_stk, "
                + "s_in, s_out, s_incost, s_outcost, s_acost, s_rem, "
                + "s_user, s_entrydate, s_entrytime, s_link, s_bran, "
                + "discount, nettotal, refund, refno, cashier, emp) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlUpdateLocalShort = "UPDATE stcard SET s_send='Y', LastUpdate=?, LastTimeUpdate=? "
                + "WHERE s_pcode=? AND s_date=? AND s_entrytime=? AND s_rem=? AND s_user=? AND s_send='N' AND s_no=?";

        String sqlUpdateLocalFull = "UPDATE stcard SET s_send='Y', LastUpdate=?, LastTimeUpdate=? "
                + "WHERE s_pcode=? AND s_date=? AND s_entrytime=? AND s_rem=? AND s_user=? AND s_send='N' "
                + "AND s_no=? AND s_incost=? AND s_in=? AND s_out=? AND s_outcost=? AND s_que=?";

        try (PreparedStatement pstmtInsert = mysqlWebOnline.getConnection().prepareStatement(sqlInsertStcard); 
                PreparedStatement pstmtUpdateShort = mysqlLocal.getConnection().prepareStatement(sqlUpdateLocalShort); 
                PreparedStatement pstmtUpdateFull = mysqlLocal.getConnection().prepareStatement(sqlUpdateLocalFull)) {

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
                            stcardBean = matchDiscount(stCardNotSend.getS_No(), stCardNotSend.getS_Date(), stCardNotSend.getS_PCode());
                            discount = (stcardBean.getDiscount());
                            nettotal = (stcardBean.getNettotal());
                            refund = stcardBean.getRefund();
                            refno = stcardBean.getRefNo();
                            cashier = stcardBean.getCashier();
                            emp = stcardBean.getEmp();
                        }
                        if (strCheck.equals("0")) {
                            stcardBean = matchDiscount(stCardNotSend.getS_No(), stCardNotSend.getS_Date(), stCardNotSend.getS_PCode());
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
                    if ((s_noCheck.equals("E") && stCardNotSend.getS_Rem().equals("SAL")) 
                            || (refno.equals("") && stCardNotSend.getS_Rem().equals("SAL"))) {

                        int updateStatusFromServer = 0;
                        try {
                            if (nettotal == 0 && stCardNotSend.getS_Rem().equals("SAL") 
                                    && stCardNotSend.getS_Out() != 0) {
                                stCardNotSend.setNettotal(totalCompareNettotal(stCardNotSend));
                                nettotal = stCardNotSend.getNettotal();
                            }
                            if (!stCardNotSend.getRefNo().equals("")) {
                                pstmtInsert.setString(1, stCardNotSend.getS_Date());
                                pstmtInsert.setString(2, stCardNotSend.getS_No());
                                pstmtInsert.setString(3, stCardNotSend.getS_SubNo());
                                pstmtInsert.setString(4, String.valueOf(stCardNotSend.getS_Que()));
                                pstmtInsert.setString(5, stCardNotSend.getS_PCode());
                                pstmtInsert.setString(6, stCardNotSend.getS_Stk());
                                pstmtInsert.setString(7, String.valueOf(stCardNotSend.getS_In()));
                                pstmtInsert.setString(8, String.valueOf(stCardNotSend.getS_Out()));
                                pstmtInsert.setString(9, String.valueOf(stCardNotSend.getS_InCost()));
                                pstmtInsert.setString(10, String.valueOf(stCardNotSend.getS_OutCost()));
                                pstmtInsert.setString(11, String.valueOf(stCardNotSend.getS_ACost()));
                                pstmtInsert.setString(12, stCardNotSend.getS_Rem());
                                pstmtInsert.setString(13, stCardNotSend.getS_User());
                                pstmtInsert.setString(14, stCardNotSend.getS_EntryDate());
                                pstmtInsert.setString(15, stCardNotSend.getS_EntryTime());
                                pstmtInsert.setString(16, stCardNotSend.getS_Link());
                                pstmtInsert.setString(17, branchCode);
                                pstmtInsert.setString(18, String.valueOf(discount));
                                pstmtInsert.setString(19, String.valueOf(nettotal));
                                pstmtInsert.setString(20, refund);
                                pstmtInsert.setString(21, refno);
                                pstmtInsert.setString(22, cashier);
                                pstmtInsert.setString(23, emp);
                                updateStatusFromServer = pstmtInsert.executeUpdate();

                                if (updateStatusFromServer > 0) {
                                    pstmtUpdateShort.setString(1, dateConvert.GetCurrentDate());
                                    pstmtUpdateShort.setString(2, dateConvert.GetCurrentTime());
                                    pstmtUpdateShort.setString(3, stCardNotSend.getS_PCode());
                                    pstmtUpdateShort.setString(4, stCardNotSend.getS_Date());
                                    pstmtUpdateShort.setString(5, stCardNotSend.getS_EntryTime());
                                    pstmtUpdateShort.setString(6, stCardNotSend.getS_Rem());
                                    pstmtUpdateShort.setString(7, stCardNotSend.getS_User());
                                    pstmtUpdateShort.setString(8, stCardNotSend.getS_No());
                                    pstmtUpdateShort.executeUpdate();
                                    tempText += "Inserted stcard: " + stCardNotSend.getS_No() + "\n";
                                    txtSql.setText(tempText + logTab);
                                }
                            }

                        } catch (SQLException e) {
                            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                        }
                    } else {
                        int updateStatusFromServer = 0;
                        try {
                            if (nettotal == 0 && stCardNotSend.getS_Rem().equals("SAL") 
                                    && stCardNotSend.getS_Out() != 0) {
                                stCardNotSend.setNettotal(totalCompareNettotal(stCardNotSend));
                                nettotal = stCardNotSend.getNettotal();
                            }

                            pstmtInsert.setString(1, stCardNotSend.getS_Date());
                            pstmtInsert.setString(2, stCardNotSend.getS_No());
                            pstmtInsert.setString(3, stCardNotSend.getS_SubNo());
                            pstmtInsert.setString(4, String.valueOf(stCardNotSend.getS_Que()));
                            pstmtInsert.setString(5, stCardNotSend.getS_PCode());
                            pstmtInsert.setString(6, stCardNotSend.getS_Stk());
                            pstmtInsert.setString(7, String.valueOf(stCardNotSend.getS_In()));
                            pstmtInsert.setString(8, String.valueOf(stCardNotSend.getS_Out()));
                            pstmtInsert.setString(9, String.valueOf(stCardNotSend.getS_InCost()));
                            pstmtInsert.setString(10, String.valueOf(stCardNotSend.getS_OutCost()));
                            pstmtInsert.setString(11, String.valueOf(stCardNotSend.getS_ACost()));
                            pstmtInsert.setString(12, stCardNotSend.getS_Rem());
                            pstmtInsert.setString(13, stCardNotSend.getS_User());
                            pstmtInsert.setString(14, stCardNotSend.getS_EntryDate());
                            pstmtInsert.setString(15, stCardNotSend.getS_EntryTime());
                            pstmtInsert.setString(16, stCardNotSend.getS_Link());
                            pstmtInsert.setString(17, branchCode);
                            pstmtInsert.setString(18, String.valueOf(discount));
                            pstmtInsert.setString(19, String.valueOf(nettotal));
                            pstmtInsert.setString(20, refund);
                            pstmtInsert.setString(21, refno);
                            pstmtInsert.setString(22, cashier);
                            pstmtInsert.setString(23, emp);
                            updateStatusFromServer = pstmtInsert.executeUpdate();

                            tempText += "Inserted stcard: " + stCardNotSend.getS_No() + "\n";
                            txtSql.setText(tempText + logTab);
                        } catch (SQLException e) {
                            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                        }

                        if (updateStatusFromServer > 0) {
                            pstmtUpdateFull.setString(1, dateConvert.GetCurrentDate());
                            pstmtUpdateFull.setString(2, dateConvert.GetCurrentTime());
                            pstmtUpdateFull.setString(3, stCardNotSend.getS_PCode());
                            pstmtUpdateFull.setString(4, stCardNotSend.getS_Date());
                            pstmtUpdateFull.setString(5, stCardNotSend.getS_EntryTime());
                            pstmtUpdateFull.setString(6, stCardNotSend.getS_Rem());
                            pstmtUpdateFull.setString(7, stCardNotSend.getS_User());
                            pstmtUpdateFull.setString(8, stCardNotSend.getS_No());
                            pstmtUpdateFull.setString(9, String.valueOf(stCardNotSend.getS_InCost()));
                            pstmtUpdateFull.setString(10, String.valueOf(stCardNotSend.getS_In()));
                            pstmtUpdateFull.setString(11, String.valueOf(stCardNotSend.getS_Out()));
                            pstmtUpdateFull.setString(12, String.valueOf(stCardNotSend.getS_OutCost()));
                            pstmtUpdateFull.setString(13, String.valueOf(stCardNotSend.getS_Que()));
                            pstmtUpdateFull.executeUpdate();

                            String pcode = stCardNotSend.getS_PCode();
                            uploadStkfile(pcode, branchCode);
                        }
                    }
                }

                lblDisplayStcard.setBackground(Color.green);
            }
            btnStatus.setEnabled(true);
        } catch (SQLException e) {
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void uploadStkfile(String bpcode, String branchCode) {
        ensureConnectionsOpen();
        if (!bpcode.equals("")) {
            STKFileBean stkFileBean = null;
            String sqlSelect = "SELECT * FROM stkfile WHERE bpcode=? LIMIT 1";
            try (PreparedStatement pstmtSelect = mysqlLocal.getConnection().prepareStatement(sqlSelect)) {
                pstmtSelect.setString(1, bpcode);
                try (ResultSet rs = pstmtSelect.executeQuery()) {
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
                            String sqlInsStkfile = "INSERT IGNORE INTO stkfile (bpcode, branch) VALUES (?, ?)";
                            try (PreparedStatement pstmtIns = mysqlLocal.getConnection().prepareStatement(sqlInsStkfile)) {
                                pstmtIns.setString(1, bpcode);
                                pstmtIns.setString(2, branchCode);
                                pstmtIns.executeUpdate();
                            }

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
                            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }
            } catch (SQLException e) {
                Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
            }

            String tempText = "";

            if (stkFileBean != null) {
                loadStatus();
                try {
                    String sqlCheckSTKServer = "SELECT bpcode, branch FROM stkfile WHERE branch=? AND bpcode=? LIMIT 1";
                    try (PreparedStatement pstmtCheck = mysqlWebOnline.getConnection().prepareStatement(sqlCheckSTKServer)) {
                        pstmtCheck.setString(1, stkFileBean.getBranch());
                        pstmtCheck.setString(2, bpcode);
                        try (ResultSet rsSV = pstmtCheck.executeQuery()) {
                            if (rsSV.next() && !rsSV.wasNull()) {
                                System.out.println(rsSV.getString("bpcode"));
                            } else {
                                String sqlInsStkfile = "INSERT IGNORE INTO stkfile (bpcode, branch) VALUES (?, ?)";
                                try (PreparedStatement pstmtIns = mysqlWebOnline.getConnection().prepareStatement(sqlInsStkfile)) {
                                    pstmtIns.setString(1, bpcode);
                                    pstmtIns.setString(2, stkFileBean.getBranch());
                                    pstmtIns.executeUpdate();
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                }

                try {
                    String sqlUpdateStkfilePs = "UPDATE stkfile SET bqty=?, bamt=?, btotalamt=?, "
                            + "bqty0=?, bqty1=?, bqty2=?, bqty3=?, bqty4=?, bqty5=?, "
                            + "bqty6=?, bqty7=?, bqty8=?, bqty9=?, bqty10=?, "
                            + "bqty11=?, bqty12=?, bqty13=?, bqty14=?, bqty15=?, "
                            + "bqty16=?, bqty17=?, bqty18=?, bqty19=?, bqty20=?, "
                            + "bqty21=?, bqty22=?, bqty23=?, bqty24=?, branch=?, "
                            + "lastupdate=?, lastTimeUpdate=? WHERE bpcode=? AND branch=?";
                    lblDisplayStcard1.setText("Update STKFile : " + 0);
                    try (PreparedStatement pstmtUpd = mysqlWebOnline.getConnection().prepareStatement(sqlUpdateStkfilePs)) {
                        pstmtUpd.setString(1, String.valueOf(stkFileBean.getbQty()));
                        pstmtUpd.setString(2, String.valueOf(stkFileBean.getbAmt()));
                        pstmtUpd.setString(3, String.valueOf(stkFileBean.getbTotalAmt()));
                        pstmtUpd.setString(4, String.valueOf(stkFileBean.getbQty0()));
                        pstmtUpd.setString(5, String.valueOf(stkFileBean.getbQty1()));
                        pstmtUpd.setString(6, String.valueOf(stkFileBean.getbQty2()));
                        pstmtUpd.setString(7, String.valueOf(stkFileBean.getbQty3()));
                        pstmtUpd.setString(8, String.valueOf(stkFileBean.getbQty4()));
                        pstmtUpd.setString(9, String.valueOf(stkFileBean.getbQty5()));
                        pstmtUpd.setString(10, String.valueOf(stkFileBean.getbQty6()));
                        pstmtUpd.setString(11, String.valueOf(stkFileBean.getbQty7()));
                        pstmtUpd.setString(12, String.valueOf(stkFileBean.getbQty8()));
                        pstmtUpd.setString(13, String.valueOf(stkFileBean.getbQty9()));
                        pstmtUpd.setString(14, String.valueOf(stkFileBean.getbQty10()));
                        pstmtUpd.setString(15, String.valueOf(stkFileBean.getbQty11()));
                        pstmtUpd.setString(16, String.valueOf(stkFileBean.getbQty12()));
                        pstmtUpd.setString(17, String.valueOf(stkFileBean.getbQty13()));
                        pstmtUpd.setString(18, String.valueOf(stkFileBean.getbQty14()));
                        pstmtUpd.setString(19, String.valueOf(stkFileBean.getbQty15()));
                        pstmtUpd.setString(20, String.valueOf(stkFileBean.getbQty16()));
                        pstmtUpd.setString(21, String.valueOf(stkFileBean.getbQty17()));
                        pstmtUpd.setString(22, String.valueOf(stkFileBean.getbQty18()));
                        pstmtUpd.setString(23, String.valueOf(stkFileBean.getbQty19()));
                        pstmtUpd.setString(24, String.valueOf(stkFileBean.getbQty20()));
                        pstmtUpd.setString(25, String.valueOf(stkFileBean.getbQty21()));
                        pstmtUpd.setString(26, String.valueOf(stkFileBean.getbQty22()));
                        pstmtUpd.setString(27, String.valueOf(stkFileBean.getbQty23()));
                        pstmtUpd.setString(28, String.valueOf(stkFileBean.getbQty24()));
                        pstmtUpd.setString(29, stkFileBean.getBranch());
                        pstmtUpd.setString(30, dateConvert.GetCurrentDate());
                        pstmtUpd.setString(31, dateConvert.GetCurrentTime());
                        pstmtUpd.setString(32, stkFileBean.getbPcode());
                        pstmtUpd.setString(33, stkFileBean.getBranch());
                        pstmtUpd.executeUpdate();
                        tempText += "Updated stkfile: " + stkFileBean.getbPcode() + "\n";
                        txtSql.setText(tempText + "\r\n");
                    } catch (SQLException e) {
                        Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                    }
                } catch (Exception e) {
                    Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                }

                lblDisplayStcard1.setBackground(Color.green);
            }
        } else {
            List<STKFileBean> listStkFile = new ArrayList<>();

            try {
                String sql = "SELECT * FROM stkfile";
                try (PreparedStatement pstmtSelect = mysqlLocal.getConnection().prepareStatement(sql);
                     ResultSet rs1 = pstmtSelect.executeQuery()) {
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
                Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
            }

            String tempText = "";
            if (!listStkFile.isEmpty()) {
                String sqlCheckServer = "SELECT bpcode, branch FROM stkfile WHERE branch=? AND bpcode=? LIMIT 1";
                String sqlInsServer = "INSERT IGNORE INTO stkfile (bpcode, branch) VALUES (?, ?)";
                String sqlUpdServer = "UPDATE stkfile SET bqty=?, bamt=?, btotalamt=?, "
                        + "bqty0=?, bqty1=?, bqty2=?, bqty3=?, bqty4=?, bqty5=?, "
                        + "bqty6=?, bqty7=?, bqty8=?, bqty9=?, bqty10=?, "
                        + "bqty11=?, bqty12=?, bqty13=?, bqty14=?, bqty15=?, "
                        + "bqty16=?, bqty17=?, bqty18=?, bqty19=?, bqty20=?, "
                        + "bqty21=?, bqty22=?, bqty23=?, bqty24=?, branch=?, "
                        + "lastupdate=?, lastTimeUpdate=? WHERE bpcode=? AND branch=?";
                String sqlUpdLocal = "UPDATE stkfile SET Lastupdate=?, LastTimeUpdate=? WHERE bpcode=?";

                try (PreparedStatement pstmtCheck = mysqlWebOnline.getConnection().prepareStatement(sqlCheckServer); PreparedStatement pstmtIns = mysqlWebOnline.getConnection().prepareStatement(sqlInsServer); PreparedStatement pstmtUpd = mysqlWebOnline.getConnection().prepareStatement(sqlUpdServer); PreparedStatement pstmtUpdLocal = mysqlLocal.getConnection().prepareStatement(sqlUpdLocal)) {

                    for (int i = 0; i < listStkFile.size(); i++) {
                        loadStatus();
                        STKFileBean bean = listStkFile.get(i);

                        try {
                            pstmtCheck.setString(1, bean.getBranch());
                            pstmtCheck.setString(2, bean.getbPcode());
                            try (ResultSet rsSV = pstmtCheck.executeQuery()) {
                                if (!rsSV.next()) {
                                    pstmtIns.setString(1, bean.getbPcode());
                                    pstmtIns.setString(2, bean.getBranch());
                                    pstmtIns.executeUpdate();
                                }
                            }
                        } catch (SQLException e) {
                            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                        }

                        btnStatus1.setText("Noplu STKFILE Update : " + i + " " + bean.getbPcode());

                        pstmtUpd.setString(1, String.valueOf(bean.getbQty()));
                        pstmtUpd.setString(2, String.valueOf(bean.getbAmt()));
                        pstmtUpd.setString(3, String.valueOf(bean.getbTotalAmt()));
                        pstmtUpd.setString(4, String.valueOf(bean.getbQty0()));
                        pstmtUpd.setString(5, String.valueOf(bean.getbQty1()));
                        pstmtUpd.setString(6, String.valueOf(bean.getbQty2()));
                        pstmtUpd.setString(7, String.valueOf(bean.getbQty3()));
                        pstmtUpd.setString(8, String.valueOf(bean.getbQty4()));
                        pstmtUpd.setString(9, String.valueOf(bean.getbQty5()));
                        pstmtUpd.setString(10, String.valueOf(bean.getbQty6()));
                        pstmtUpd.setString(11, String.valueOf(bean.getbQty7()));
                        pstmtUpd.setString(12, String.valueOf(bean.getbQty8()));
                        pstmtUpd.setString(13, String.valueOf(bean.getbQty9()));
                        pstmtUpd.setString(14, String.valueOf(bean.getbQty10()));
                        pstmtUpd.setString(15, String.valueOf(bean.getbQty11()));
                        pstmtUpd.setString(16, String.valueOf(bean.getbQty12()));
                        pstmtUpd.setString(17, String.valueOf(bean.getbQty13()));
                        pstmtUpd.setString(18, String.valueOf(bean.getbQty14()));
                        pstmtUpd.setString(19, String.valueOf(bean.getbQty15()));
                        pstmtUpd.setString(20, String.valueOf(bean.getbQty16()));
                        pstmtUpd.setString(21, String.valueOf(bean.getbQty17()));
                        pstmtUpd.setString(22, String.valueOf(bean.getbQty18()));
                        pstmtUpd.setString(23, String.valueOf(bean.getbQty19()));
                        pstmtUpd.setString(24, String.valueOf(bean.getbQty20()));
                        pstmtUpd.setString(25, String.valueOf(bean.getbQty21()));
                        pstmtUpd.setString(26, String.valueOf(bean.getbQty22()));
                        pstmtUpd.setString(27, String.valueOf(bean.getbQty23()));
                        pstmtUpd.setString(28, String.valueOf(bean.getbQty24()));
                        pstmtUpd.setString(29, bean.getBranch());
                        pstmtUpd.setString(30, dateConvert.GetCurrentDate());
                        pstmtUpd.setString(31, dateConvert.GetCurrentTime());
                        pstmtUpd.setString(32, bean.getbPcode());
                        pstmtUpd.setString(33, bean.getBranch());
                        pstmtUpd.executeUpdate();

                        tempText += "Updated stkfile: " + bean.getbPcode() + "\n";
                        txtSql.setText(tempText + "\r\n");

                        pstmtUpdLocal.setString(1, dateConvert.GetCurrentDate());
                        pstmtUpdLocal.setString(2, dateConvert.GetCurrentTime());
                        pstmtUpdLocal.setString(3, bean.getbPcode());
                        pstmtUpdLocal.executeUpdate();
                    }
                } catch (SQLException e) {
                    Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    public STCardBean matchDiscount(String s_No, String s_Date, String s_PCode) {
        STCardBean bean = new STCardBean();
        String indexNoCheck = s_No.substring(0, 1);

        try {
            if (s_Date.equals(dateConvert.GetCurrentDate())) {
                bean = processCurrentDate(s_No, indexNoCheck, s_PCode, s_Date);
            } else {
                bean = processNotCurrentDate(s_No, indexNoCheck, s_PCode, s_Date);
            }
        } catch (SQLException e) {
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
        }
        return bean;
    }

    private STCardBean processNotCurrentDate(String s_No, String indexNoCheck, String s_PCode, String s_Date) throws SQLException {
        STCardBean bean = new STCardBean();
        String r_time;
        String refno;

        String[] strs = s_No.split("-");

        String macno = strs[0];
        r_time = extractRTime(s_No, strs);

        //ถ้าเป็นเอกสาร คืนสินค้า
        if (indexNoCheck.equals("R")) {
            macno = s_No.substring(2, 5);
        }

        String sql = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                + "r_refno, r_refund, cashier Cashier, r_emp R_Emp, r_time, r_quan, r_plucode "
                + "FROM s_tran "
                + "WHERE macno = ? AND r_time = ? AND r_plucode = ? AND r_date = ? LIMIT 1";
        try (PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, macno);
            pstmt.setString(2, r_time);
            pstmt.setString(3, s_PCode);
            pstmt.setString(4, s_Date);
            try (ResultSet rs = pstmt.executeQuery()) {
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
                    if (indexNoCheck.equals("R")) {
                        String[] strs1 = s_No.split("/");
                        refno = strs1[1];

                        if (indexNoCheck.equals("R")) {
                            String querySTran = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                                    + "r_refno, r_refund, cashier Cashier, r_emp R_Emp, r_time "
                                    + "FROM s_tran "
                                    + "WHERE macno = ? AND r_refno = ? AND r_plucode = ? AND r_date = ? LIMIT 1";
                            try (PreparedStatement pstmt2 = mysqlLocal.getConnection().prepareStatement(querySTran)) {
                                pstmt2.setString(1, macno);
                                pstmt2.setString(2, refno);
                                pstmt2.setString(3, s_PCode);
                                pstmt2.setString(4, s_Date);
                                try (ResultSet rsNew = pstmt2.executeQuery()) {
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
                                }
                            } catch (SQLException e) {
                                Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                            }
                            if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                                bean.setNettotal(0);
                            }
                        }
                    } else {
                        DecimalFormat intFM = new DecimalFormat("00");
                        String[] strsTime = s_No.split("-");

                        macno = strsTime[0];
                        r_time = extractRTime(s_No, strsTime);

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
                        bean.setR_time(r_newTime);
                        if (indexNoCheck.equals("R")) {
                            macno = s_No.substring(2, 5);
                        }
                        String querySTran = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                                + "r_refno, r_refund, cashier Cashier, r_emp R_Emp, r_time "
                                + "FROM s_tran "
                                + "WHERE macno = ? AND r_time = ? AND r_plucode = ? AND r_date = ? LIMIT 1";
                        try (PreparedStatement pstmt3 = mysqlLocal.getConnection().prepareStatement(querySTran)) {
                            pstmt3.setString(1, macno);
                            pstmt3.setString(2, r_newTime);
                            pstmt3.setString(3, s_PCode);
                            pstmt3.setString(4, s_Date);
                            try (ResultSet rsNew = pstmt3.executeQuery()) {
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
                            }
                        } catch (SQLException e) {
                            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                        }
                        if (bean.getS_OutCost() < 0 && bean.getS_InCost() == 0 && bean.getNettotal() == -1) {
                            bean.setNettotal(0);
                        }
                    }

                }
            }
        }
        return bean;
    }

    private STCardBean processCurrentDate(String s_No, String indexNoCheck, String s_PCode, String s_Date) throws SQLException {
        STCardBean bean = new STCardBean();
        String macno;
        String refno = "";

        //ถ้าข้อมูลเป็นวันปัจจุบัน
        indexNoCheck = s_No.substring(0, 1);

        //ถ้าเป็นว่ายกเลิกบิล
        if (indexNoCheck.equals("R")) {
            macno = s_No.substring(2, 5);
            String[] strs1 = s_No.split("/");
            refno = strs1[1];

            String sql = "SELECT r_refno, R_Total, r_nettotal R_Nettotal, r_pramt, r_discbath, "
                    + "r_refno, r_refund, cashier Cashier, r_emp R_Emp, r_time "
                    + "FROM t_sale "
                    + "WHERE macno = ? AND r_refno = ? AND r_plucode = ? AND r_date = ? LIMIT 1";
            try (PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, macno);
                pstmt.setString(2, refno);
                pstmt.setString(3, s_PCode);
                pstmt.setString(4, s_Date);
                try (ResultSet rs7 = pstmt.executeQuery()) {
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
                }
            }
        } else {
            //เป็นบิลปกติ ไม่ได้ยกเลิก
            String[] strs = s_No.split("-");
            String r_time;

            macno = strs[0];
            r_time = extractRTime(s_No, strs);

            String sql = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                    + "r_refno, r_refund, cashier Cashier, r_emp R_Emp, R_Time "
                    + "FROM t_sale "
                    + "WHERE r_plucode = ? AND r_date = ? LIMIT 1";
            try (PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, s_PCode);
                pstmt.setString(2, s_Date);
                try (ResultSet rs1 = pstmt.executeQuery()) {
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
                        int mm;
                        int ss = 0;
                        String r_newTime;
                        String sqlInner;
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
                            sqlInner = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                                    + "r_refno, r_refund, cashier Cashier, r_emp R_Emp, r_time "
                                    + "FROM t_sale "
                                    + "WHERE macno = ? AND r_time = ? AND r_plucode = ? AND r_date = ? LIMIT 1";
                            try (PreparedStatement pstmt2 = mysqlLocal.getConnection().prepareStatement(sqlInner)) {
                                pstmt2.setString(1, macno);
                                pstmt2.setString(2, r_newTime);
                                pstmt2.setString(3, s_PCode);
                                pstmt2.setString(4, s_Date);
                                try (ResultSet rsNew1 = pstmt2.executeQuery()) {
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
                                }
                            } catch (SQLException e) {
                                Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                            }
                        } else {
                            sqlInner = "SELECT r_refno, r_total, r_nettotal, r_pramt, r_discbath, "
                                    + "r_refno, r_refund, cashier Cashier, r_emp R_Emp, r_time "
                                    + "FROM t_sale "
                                    + "WHERE macno = ? AND r_refno = ? AND r_plucode = ? AND r_date = ? LIMIT 1";
                            try (PreparedStatement pstmt3 = mysqlLocal.getConnection().prepareStatement(sqlInner)) {
                                pstmt3.setString(1, macno);
                                pstmt3.setString(2, refno);
                                pstmt3.setString(3, s_PCode);
                                pstmt3.setString(4, s_Date);
                                try (ResultSet rsNew1 = pstmt3.executeQuery()) {
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
                                }
                            } catch (SQLException e) {
                                Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                            }
                        }
                    }
                }
            }
        }
        return bean;
    }

    public double getCostfile(String pcode) {
        double pscost = 0;
        String sql = "SELECT pscost, pacost, plcost FROM product WHERE pcode = ?";
        try (PreparedStatement pstmt = mysqlLocal.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, pcode);
            try (ResultSet rsCostfile = pstmt.executeQuery()) {
                if (rsCostfile.next()) {
                    pscost = rsCostfile.getDouble("pscost");
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
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
                        Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }

                pbCheckUpdate.setString("Load data Complete ");
            } catch (Exception e) {
                Logger.getLogger(ProcessController.class.getName()).log(Level.SEVERE, null, e);
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
