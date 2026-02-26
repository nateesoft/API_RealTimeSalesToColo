package main;

import com.ics.bean.BranchBean;
import com.ics.bean.STCardBean;
import com.ics.bean.STKFileBean;
import com.ics.pos.core.controller.BranchControl;
import com.ics.pos.core.controller.LocalPosHwSetupControl;
import com.ics.pos.core.controller.LocalSTCardControl;
import com.ics.pos.core.controller.LocalSTranControl;
import com.ics.pos.core.controller.LocalStkFileControl;
import com.ics.pos.core.controller.ServerPosHwSetupControl;
import com.ics.pos.core.controller.ServerSTCardControl;
import com.ics.pos.core.controller.ServerStkFileControl;
import com.ics.pos.core.controller.LocalTSaleControl;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import util.DateConvert;

/**
 *
 * @author Dell
 */
public class Api_RealTimeSalesToColoServer extends javax.swing.JFrame {

    private final BranchBean branchBean = new BranchControl().getData();
    private final DateConvert dateConvert = new DateConvert();

    // Scheduler สำหรับ run task ทุกๆ 5 นาที
    private ScheduledExecutorService scheduler;
    private static final int UPLOAD_INTERVAL_MINUTES = 5; // fix 5 for every 5 minutes

    private final String TERMINAL_FIXED = "001";
    private final String STOCK_CODE = "A1";
    private final String S_REM_SAL = "SAL";

    public Api_RealTimeSalesToColoServer() {
        initComponents();
        setState(JFrame.ICONIFIED);

        System.out.println("Loop For Upload Stcard/Stkfile Update");
        btnUpload.setText(getCurrentTime());

        if (branchBean != null) {
            lblBranch.setText("รหัสสาขา : " + branchBean.getCode());
            btnStatus.setText("Finsished time : " + getCurrentTime());

            // เริ่มต้น ProcessController และ Scheduler
            initializeAndStartScheduler();
        }
    }

    /**
     * เริ่มต้น ProcessController และตั้งเวลา upload ทุกๆ 5 นาที
     */
    private void initializeAndStartScheduler() {
        // สร้าง Scheduler ด้วย single thread
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Task ที่จะ run ทุกๆ 5 นาที
        Runnable uploadTask = () -> {
            try {
                System.out.println("=== Scheduled upload started at: " + getCurrentTime() + " ===");
                uploadStcard();
                javax.swing.SwingUtilities.invokeLater(() -> btnStatus.setText("Last upload: " + getCurrentTime()));
                System.out.println("=== Scheduled upload completed ===");
            } catch (Exception e) {
                Logger.getLogger(Api_RealTimeSalesToColoServer.class.getName()).log(Level.SEVERE, null, e);
            }
        };

        // เริ่ม schedule: run ครั้งแรกทันที (0), แล้ว repeat ทุก 5 นาที
        scheduler.scheduleAtFixedRate(uploadTask, 0, UPLOAD_INTERVAL_MINUTES, TimeUnit.MINUTES);

        System.out.println("Scheduler started: Upload every " + UPLOAD_INTERVAL_MINUTES + " minutes");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnUpload = new javax.swing.JToggleButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLogErr = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jToggleButton2 = new javax.swing.JToggleButton();
        btnStatus = new javax.swing.JToggleButton();
        btnStatus1 = new javax.swing.JToggleButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtSql = new javax.swing.JTextArea();
        btnStatus2 = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        lblBranch = new javax.swing.JLabel();
        pbCheckUpdate = new javax.swing.JProgressBar();
        lblDisplayStcard = new javax.swing.JLabel();
        lblDisplayStcard1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("API_RealtimeOnline");
        setUndecorated(true);

        btnUpload.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnUpload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/upload-icon-18.png"))); // NOI18N
        btnUpload.setText("   Click here");

        jToggleButton1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButton1.setForeground(new java.awt.Color(255, 102, 102));
        jToggleButton1.setText("X");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(204, 204, 204));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 102, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("API-AutoSales Online V5.14 2026022/17:14");

        txtLogErr.setColumns(20);
        txtLogErr.setRows(5);
        jScrollPane1.setViewportView(txtLogErr);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 102, 0));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("***webconnect Please check Before Run!");

        jToggleButton2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButton2.setForeground(new java.awt.Color(255, 102, 102));
        jToggleButton2.setText("-");
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        btnStatus.setText("Status");

        btnStatus1.setText("Upload Stkfile");
        btnStatus1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatus1ActionPerformed(evt);
            }
        });

        txtSql.setColumns(20);
        txtSql.setRows(5);
        jScrollPane2.setViewportView(txtSql);

        btnStatus2.setText("Upload STCard");
        btnStatus2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatus2ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jButton1.setText("Exit");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        lblBranch.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblBranch.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBranch.setText("jLabel3");

        pbCheckUpdate.setBackground(new java.awt.Color(255, 153, 153));
        pbCheckUpdate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        pbCheckUpdate.setForeground(new java.awt.Color(255, 255, 255));

        lblDisplayStcard.setText("DisplaySTCard");

        lblDisplayStcard1.setText("DisplaySTKFile");

        jButton2.setFont(new java.awt.Font("Angsana New", 0, 36)); // NOI18N
        jButton2.setText("เอกสารแก้ไข จากสำนักงานใหญ่");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnUpload, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pbCheckUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblBranch, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 507, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(btnStatus1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnStatus2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblDisplayStcard1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblDisplayStcard, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(125, 125, 125)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUpload)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(pbCheckUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnStatus1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDisplayStcard1, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStatus2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDisplayStcard, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblBranch, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 5, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void btnStatus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatus1ActionPerformed
        new Thread(() -> {
            uploadStkfile("");
        }).start();
    }//GEN-LAST:event_btnStatus1ActionPerformed

    private void btnStatus2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatus2ActionPerformed
        new Thread(() -> {
            uploadStcard();
        }).start();
    }//GEN-LAST:event_btnStatus2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        scheduler.shutdown();
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private final LocalSTCardControl localStCard = new LocalSTCardControl();
    private final ServerSTCardControl serverSTCardControl = new ServerSTCardControl();

    private void uploadStcard() {
        // update last refno
        uploadLastRefno(TERMINAL_FIXED, getCurrentDate(), getCurrentTime(), branchBean.getCode());

        // prepare stcard to send data
        List<STCardBean> listSTCardNotSend = localStCard.getListSTCardNotSend();
        if (!listSTCardNotSend.isEmpty()) {
            for (int i = 0; i < listSTCardNotSend.size(); i++) {
                double discount = 0;
                double nettotal = 0;
                String refund = "";
                String refno = "";
                String cashier = "";
                String emp = "";
                STCardBean stCardNotSend = (STCardBean) listSTCardNotSend.get(i);
                STCardBean stcardBean;

                // check S_No digit
                String checkFirstDigitSNo = stCardNotSend.getS_No().substring(0, 1);
                if (stCardNotSend.getS_Rem().equals(S_REM_SAL)) {
                    if (checkFirstDigitSNo.equals("E")) {
                        discount = 0;
                        nettotal = stCardNotSend.getS_OutCost();
                        refund = "-";
                        refno = stCardNotSend.getS_No();
                        cashier = stCardNotSend.getEmp();
                        emp = cashier;
                    }
                    if (checkFirstDigitSNo.equals("R") || checkFirstDigitSNo.equals("0")) {
                        stcardBean = matchDiscount(stCardNotSend.getS_No(), stCardNotSend.getS_Date(), stCardNotSend.getS_PCode(), checkFirstDigitSNo, stCardNotSend);
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

                //ถ้า s_rem=  SAL และ E
                if (stCardNotSend.getS_Rem().equals(S_REM_SAL) && (checkFirstDigitSNo.equals("E") || refno.equals(""))) {
                    boolean updateStatusFromServer;
                    if (nettotal == 0 && stCardNotSend.getS_Rem().equals(S_REM_SAL) && stCardNotSend.getS_Out() != 0) {
                        stCardNotSend.setNettotal(totalCompareNettotal(stCardNotSend));
                        nettotal = stCardNotSend.getNettotal();
                    }

                    double unitPrice = 0;
                    if (stCardNotSend.getS_In() != 0) {
                        unitPrice = stCardNotSend.getS_InCost() / stCardNotSend.getS_In();
                    }
                    if (stCardNotSend.getS_Out() != 0) {
                        unitPrice = stCardNotSend.getS_OutCost() / stCardNotSend.getS_Out();
                    }

                    if (!stCardNotSend.getRefNo().equals("")) {
                        // update server
                        updateStatusFromServer = serverSTCardControl.saveSTCard(stCardNotSend, discount, nettotal, refund, refno, cashier, emp, unitPrice, branchBean.getCode());

                        if (updateStatusFromServer) {
                            // update flag local
                            localStCard.updateSendStatus(stCardNotSend, getCurrentDate(), getCurrentTime());
                        }
                    }
                } else {
                    //ถ้า s_rem ไม่เท่ากับ SAL
                    boolean updateStatusFromServer;
                    if (nettotal == 0 && stCardNotSend.getS_Rem().equals(S_REM_SAL) && stCardNotSend.getS_Out() != 0) {
                        stCardNotSend.setNettotal(totalCompareNettotal(stCardNotSend));
                        nettotal = stCardNotSend.getNettotal();
                    }
                    double unitPrice = 0;
                    if (stCardNotSend.getS_In() != 0) {
                        unitPrice = stCardNotSend.getS_InCost() / stCardNotSend.getS_In();
                    }
                    if (stCardNotSend.getS_Out() != 0) {
                        unitPrice = stCardNotSend.getS_OutCost() / stCardNotSend.getS_Out();
                    }

                    // update server
                    updateStatusFromServer = serverSTCardControl.saveSTCard(stCardNotSend, discount, nettotal, refund, refno, cashier, emp, unitPrice, branchBean.getCode());

                    if (updateStatusFromServer) {
                        // update flag local
                        localStCard.updateSendStatusDone(stCardNotSend, getCurrentDate(), getCurrentTime());

                        // next step to update stkfile
                        uploadStkfile(stCardNotSend.getS_PCode());
                    }
                }
            }
        }

        javax.swing.SwingUtilities.invokeLater(() -> btnStatus.setEnabled(true));
    }

    private final LocalStkFileControl localStkFile = new LocalStkFileControl();
    private final ServerStkFileControl serverStkFileControl = new ServerStkFileControl();

    private void uploadStkfile(String bpcode) {
        if (bpcode != null && !bpcode.equals("")) {

            STKFileBean stkFileBean = localStkFile.getDataByBPCode(bpcode);
            if (stkFileBean == null) {
                stkFileBean = localStkFile.saveNewData(bpcode, branchBean.getCode(), STOCK_CODE);
            }

            // check update server
            if (stkFileBean != null) {
                STKFileBean serverStkFileBean = serverStkFileControl.getDataByBPCodeBranchCode(bpcode, branchBean.getCode());
                if (serverStkFileBean == null) {
                    serverStkFileControl.saveNewData(bpcode, stkFileBean.getBranch(), STOCK_CODE);
                }

                // update server stkfile
                serverStkFileControl.updateData(stkFileBean, getCurrentDate(), getCurrentTime());
            }
        } else {
            List<STKFileBean> listStkFile = localStkFile.getAllData();
            if (!listStkFile.isEmpty()) {
                for (int i = 0; i < listStkFile.size(); i++) {
                    STKFileBean stkFileBean = (STKFileBean) listStkFile.get(i);
                    STKFileBean serverStkFileBean = serverStkFileControl.getDataByBPCodeBranchCode(stkFileBean.getbPcode(), stkFileBean.getBranch());
                    if (serverStkFileBean == null) {
                        // insert server StkFile
                        serverStkFileControl.saveNewData(stkFileBean.getbPcode(), stkFileBean.getBranch(), STOCK_CODE);
                    }

                    serverStkFileControl.updateData(stkFileBean, getCurrentDate(), getCurrentTime());

                    // update local time for stkfile
                    localStkFile.updateTimeData(stkFileBean.getbPcode(), getCurrentDate(), getCurrentTime());
                }
            }
        }
    }

    private STCardBean matchDiscount(String s_No, String s_Date, String s_PCode,
            String checkFirstDigitSNo, STCardBean stCardNotSend) {
        STCardBean bean = new STCardBean();
        try {
            if (s_Date.equals(getCurrentDate())) {
                bean = processCurrentDate(s_No, checkFirstDigitSNo, s_PCode, s_Date, stCardNotSend);
            } else {
                bean = processNotCurrentDate(s_No, checkFirstDigitSNo, s_PCode, s_Date, stCardNotSend.getS_Out(), stCardNotSend.getS_OutCost());
            }
        } catch (SQLException e) {
            Logger.getLogger(Api_RealTimeSalesToColoServer.class.getName()).log(Level.SEVERE, null, e);
        }

        return bean;
    }

    private double totalCompareNettotal(STCardBean bean) {
        if (bean.getS_OutCost() != 0 && bean.getS_Rem().equals(S_REM_SAL) && bean.getNettotal() == 0) {
            bean.setNettotal(bean.getS_OutCost());
        }
        return bean.getNettotal();

    }

    private final LocalPosHwSetupControl poshwControl = new LocalPosHwSetupControl();
    private final ServerPosHwSetupControl poshwServerControl = new ServerPosHwSetupControl();

    private void uploadLastRefno(String terminal, String currentDate, String currentTime, String branchCode) {
        String receNo1 = poshwControl.getReceNo1ByTerminal(terminal);
        if (receNo1 != null) {
            poshwServerControl.updateTime(receNo1, currentDate, currentTime, terminal, branchCode);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            Api_RealTimeSalesToColoServer dialog = new Api_RealTimeSalesToColoServer();
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dialog.scheduler.shutdown();
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnStatus;
    private javax.swing.JToggleButton btnStatus1;
    private javax.swing.JToggleButton btnStatus2;
    private javax.swing.JToggleButton btnUpload;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JLabel lblBranch;
    private javax.swing.JLabel lblDisplayStcard;
    private javax.swing.JLabel lblDisplayStcard1;
    private javax.swing.JProgressBar pbCheckUpdate;
    private javax.swing.JTextArea txtLogErr;
    private javax.swing.JTextArea txtSql;
    // End of variables declaration//GEN-END:variables

    private final LocalTSaleControl tSaleControl = new LocalTSaleControl();
    private final DecimalFormat intFM = new DecimalFormat("00");

    private STCardBean processCurrentDate(String s_No, String checkFirstDigitSNo, String s_PCode,
            String s_Date, STCardBean bean) {
        STCardBean beanMapping;
        String macno, refno;

        //ถ้าเป็นว่ายกเลิกบิล
        if (checkFirstDigitSNo.equals("R")) {
            macno = s_No.substring(2, 5);
            String[] strs1 = s_No.split("/");
            refno = strs1[1];
            beanMapping = tSaleControl.getDataByMacNoRefNoPluCodeFlagRVoid(macno, refno, s_PCode, s_Date, bean.getR_time());
        } else {
            //เป็นบิลปกติ ไม่ได้ยกเลิก
            String[] strs = s_No.split("-");
            String r_time;

            macno = strs[0];
            //ดักไว้ หากมีผิดพลาดเรื่อง Index 001-1-124451 หาไม่เจอว่ามาจากอะไร
            if(s_No.length() == 14) {
                String chkSNO = s_No.substring(3, 6);
                if(chkSNO.equals("-1-")||chkSNO.equals("-2-")||chkSNO.equals("-3-")||chkSNO.equals("-4-")
                        ||chkSNO.equals("-5-")||chkSNO.equals("-6-")||chkSNO.equals("-7-")
                        ||chkSNO.equals("-8-")||chkSNO.equals("-9-")) {
                    r_time = strs[2];
                } else {
                    r_time = strs[1];
                }
            } else {
                r_time = strs[1];
            }

            beanMapping = tSaleControl.getDataByPluCodeRdateRTimeRVoid(s_PCode, s_Date, r_time);
            if (beanMapping != null) {
                return beanMapping;
            }

            int hh = 0;
            int mm = 0;
            int ss = 0;
            String r_newTime = "";

            //ถ้าเป็นยกเลิกบิล
            if (!checkFirstDigitSNo.equals("R")) {
                hh = Integer.parseInt(r_time.substring(0, 2));
                mm = Integer.parseInt(r_time.substring(3, 5));
                ss = Integer.parseInt(r_time.substring(6, 8));
                ss = ss - 1;
                if (ss == -1) {
                    ss = 59;
                    mm = mm - 1;
                }
                if (mm == -1) {
                    mm = 59;
                    hh = hh - 1;
                }
                r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss);
                beanMapping = tSaleControl.getDataByMacnoRTimeRDatePluCodeRVoid(macno, r_newTime, s_PCode, s_Date, true);
            } else {
                //ถ้าไม่ใช่การยกเลิกบิล
                beanMapping = tSaleControl.getDataByMacnoRTimeRDatePluCodeRVoid(macno, r_newTime, s_PCode, s_Date, false);
            }

            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 1);
            beanMapping = tSaleControl.getDataByMacnoRTimeRDatePluCodeRVoid(macno, r_newTime, s_PCode, s_Date, false);
            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 2);
            beanMapping = tSaleControl.getDataByMacnoRTimeRDatePluCodeRVoid(macno, r_newTime, s_PCode, s_Date, false);
            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 3);
            beanMapping = tSaleControl.getDataByMacnoRTimeRDatePluCodeRVoid(macno, r_newTime, s_PCode, s_Date, false);
            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 4);
            beanMapping = tSaleControl.getDataByMacnoRTimeRDatePluCodeRVoid(macno, r_newTime, s_PCode, s_Date, false);
            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 5);
            beanMapping = tSaleControl.getDataByMacnoRTimeRDatePluCodeRVoid(macno, r_newTime, s_PCode, s_Date, false);
        }

        return beanMapping;
    }

    private final LocalSTranControl localStranControl = new LocalSTranControl();
    private STCardBean processNotCurrentDate(String s_No, String checkFirstDigitSNo, String s_PCode,
            String s_Date, double s_out, double s_outCost) throws SQLException {
        DecimalFormat df = new DecimalFormat("#0");
        String macno;
        String r_time;
        String refno;

        String[] strs = s_No.split("-");
        macno = strs[0];
        if (s_No.length() == 14 && s_No.substring(3, 6).equals("-1-")) {
            r_time = strs[2];
        } else {
            r_time = strs[1];
        }

        STCardBean beanMapping;

        //ถ้าเป็นเอกสาร คืนสินค้า
        if (checkFirstDigitSNo.equals("R")) {
            macno = s_No.substring(2, 5);
            String[] strs1 = s_No.split("/");
            refno = strs1[1];
            beanMapping = localStranControl.getDataByMacnoTimeDatePluCode(macno, null, s_PCode, s_Date,
                    refno, df.format(s_out), df.format(s_outCost), checkFirstDigitSNo, s_No);
        } else {
            beanMapping = localStranControl.getDataByMacnoTimeDatePluCode(macno, r_time, s_PCode, s_Date,
                    null, null, null, checkFirstDigitSNo, s_No);
        }

        if (beanMapping != null) {
            return beanMapping;
        }

        if (checkFirstDigitSNo.equals("R")) {
            String[] strs1 = s_No.split("/");
            refno = strs1[1];
            beanMapping = localStranControl.getDataByMacnoRefnoPluCodeRDateFlagVoid(macno, refno, s_PCode, s_Date, checkFirstDigitSNo, s_No);
        } else {
            String[] strsTime = s_No.split("-");
            macno = strsTime[0];
            //ดักไว้ หากมีผิดพลาดเรื่อง Index 001-1-124451 หาไม่เจอว่ามาจากอะไร
            if (s_No.length() == 14) {
                String chkSNO = s_No.substring(3, 6);
                if (chkSNO.equals("-1-") || chkSNO.equals("-2-") || chkSNO.equals("-3-") || chkSNO.equals("-4-")
                        || chkSNO.equals("-5-") || chkSNO.equals("-6-") || chkSNO.equals("-7-")
                        || chkSNO.equals("-8-") || chkSNO.equals("-9-")) {
                    r_time = strsTime[2];
                } else {
                    r_time = strsTime[1];
                }
            } else {
                r_time = strsTime[1];
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
                mm = 59;
                hh = hh - 1;
            }

            if (checkFirstDigitSNo.equals("R")) {
                macno = s_No.substring(2, 5);
                String[] strs1 = s_No.split("/");
                refno = strs1[1];
                beanMapping = localStranControl.getDataByCondition(macno, refno, s_PCode, s_Date, r_time, true, checkFirstDigitSNo, s_No);
            } else {
                String r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss);
                beanMapping = localStranControl.getDataByCondition(macno, null, s_PCode, s_Date, r_newTime, null, checkFirstDigitSNo, s_No);
            }

            if (beanMapping != null) {
                return beanMapping;
            }

            String r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 1);
            beanMapping = localStranControl.getDataByCondition(macno, null, s_PCode, s_Date, r_newTime, false, checkFirstDigitSNo, s_No);
            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 2);
            beanMapping = localStranControl.getDataByCondition(macno, null, s_PCode, s_Date, r_newTime, false, checkFirstDigitSNo, s_No);
            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 3);
            beanMapping = localStranControl.getDataByCondition(macno, null, s_PCode, s_Date, r_newTime, false, checkFirstDigitSNo, s_No);
            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 4);
            beanMapping = localStranControl.getDataByCondition(macno, null, s_PCode, s_Date, r_newTime, false, checkFirstDigitSNo, s_No);
            if (beanMapping != null) {
                return beanMapping;
            }

            r_newTime = intFM.format(hh) + ":" + intFM.format(mm) + ":" + intFM.format(ss - 5);
            beanMapping = localStranControl.getDataByCondition(macno, null, s_PCode, s_Date, r_newTime, false, checkFirstDigitSNo, s_No);
        }

        return beanMapping;
    }

    private String getCurrentDate() {
        return dateConvert.GetCurrentDate();
    }

    private String getCurrentTime() {
        return dateConvert.GetCurrentTime();
    }
}
