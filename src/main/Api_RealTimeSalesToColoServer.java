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
import com.ics.pos.core.controller.TranIOControl;
import database.MySQLConnect;
import database.MySQLConnectWebOnline;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import util.AppLogUtil;
import util.DateConvert;

/**
 *
 * @author Delllist
 */
public class Api_RealTimeSalesToColoServer extends javax.swing.JFrame {

    private BranchBean branchBean;
    private final DateConvert dateConvert = new DateConvert();

    // Scheduler สำหรับ run task ทุกๆ 5 นาที
    private ScheduledExecutorService scheduler;
    private static final int UPLOAD_INTERVAL_MINUTES = 5; // fix 5 for every 5 minutes

    private final String TERMINAL_FIXED = "001";
    private final String STOCK_CODE = "A1";
    private final TranIOControl TIO = new TranIOControl();

    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();
    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public Api_RealTimeSalesToColoServer() {
        initComponents();
        setState(JFrame.ICONIFIED);

        System.out.println("Loop For Upload Stcard/Stkfile Update");
        btnUpload.setText(getCurrentTime());
        lblBranch.setText("กำลังตรวจสอบการเชื่อมต่อ MySQL...");
        btnStatus.setText("รอการเชื่อมต่อ...");

        startConnectionCheck();

    }

    /**
     * ตรวจสอบการเชื่อมต่อ MySQL ทั้ง Local และ Web ก่อนเริ่มระบบ Retry ทุก 5
     * วินาที จนกว่าจะ connect ได้ทั้งคู่
     */
    private void startConnectionCheck() {
        jButton1.setEnabled(false);
        new Thread(new Thread(() -> {
            int attempt = 0;
            while (true) {
                attempt++;
                final int currentAttempt = attempt;
                javax.swing.SwingUtilities.invokeLater(()
                        -> btnStatus.setText("ตรวจสอบ MySQL... ครั้งที่ " + currentAttempt)
                );

                boolean localOk = testLocalConnection();
                boolean webOk = testWebConnection();

                final boolean localStatus = localOk;
                final boolean webStatus = webOk;
                System.out.println("Attempt " + currentAttempt
                        + " | Local MySQL: " + (localOk ? "OK" : "FAIL")
                        + " | Web MySQL: " + (webOk ? "OK" : "FAIL"));

                if (localOk && webOk) {
                    branchBean = new BranchControl().getData();
                    final BranchBean bean = branchBean;
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (bean != null) {
                            lblBranch.setText("รหัสสาขา : " + bean.getCode());
                            btnStatus.setText("MySQL เชื่อมต่อสำเร็จ : " + getCurrentTime());
                        } else {
                            lblBranch.setText("ไม่พบข้อมูลสาขา");
                            btnStatus.setText("เกิดข้อผิดพลาด: ไม่พบข้อมูลสาขา");
                        }

                        initializeAndStartScheduler();
                    });
                    break;
                }

                javax.swing.SwingUtilities.invokeLater(()
                        -> btnStatus.setText("Local: " + (localStatus ? "OK" : "FAIL")
                                + " | Web: " + (webStatus ? "OK" : "FAIL")
                                + " | Retry in 5s...")
                );
            }
        }, "MySQL-ConnectionCheck")::start).start();
    }

    private boolean testLocalConnection() {
        MySQLConnect testConn = new MySQLConnect();
        try {
            testConn.open();
            return testConn.getConnection() != null;
        } finally {
            testConn.close();
        }
    }

    private boolean testWebConnection() {
        MySQLConnectWebOnline testConn = new MySQLConnectWebOnline();
        try {
            testConn.open();
            return testConn.getConnection() != null;
        } finally {
            testConn.close();
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
            SwingUtilities.invokeLater(() -> txtLogMSG.setText("กำลังเข้าสู่ ลูบ การส่งข้อมูลครับ 5 นาที"));

            // update last refno
            uploadLastRefno(TERMINAL_FIXED, getCurrentDate(), getCurrentTime(), branchBean.getCode());

            try {
                System.out.println("=== Scheduled upload started at: " + getCurrentTime() + " ===");

                // upate stcard ไม่เอา S_Rem==SAL เนื่องจาก S_Rem==SAL จะต้องไปหาข้อมูล discount เพิ่มในตาราง T_Sale
                uploadStcard();

                updateStcardServerWithoutSALtype();

                // อัพเดตย้อนหลังสำหรับรายการที่มีการ Void
                uploadUpdateVoid();

                SwingUtilities.invokeLater(() -> btnStatus.setText("Last upload: " + getCurrentTime()));
                System.out.println("=== Scheduled upload completed ===");
            } catch (Exception e) {
                AppLogUtil.log(this.getClass(), "error", e);
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
        txtLogMSG = new javax.swing.JTextArea();
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
        jLabel1.setText("API-AutoSales Online V8.0 20260512");

        txtLogMSG.setColumns(20);
        txtLogMSG.setRows(5);
        jScrollPane1.setViewportView(txtLogMSG);

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

        jButton2.setFont(new java.awt.Font("Angsana New", 0, 18)); // NOI18N
        jButton2.setText("เอกสารแก้ไข จากสำนักงานใหญ่");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnUpload, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnStatus1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDisplayStcard1, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
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
                .addGap(0, 14, Short.MAX_VALUE))
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
        if (scheduler != null) {
            scheduler.shutdown();
        }
        System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            TIO.TIO_Process(branchBean.getCode());
        } catch (Exception e) {
            Logger.getLogger(Api_RealTimeSalesToColoServer.class.getName()).log(Level.SEVERE, null, e);
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private final LocalSTCardControl localStCard = new LocalSTCardControl();
    private final ServerSTCardControl serverSTCardControl = new ServerSTCardControl();

    private void uploadStcard() {
        if (branchBean == null) {
            return;
        }

        // prepare stcard to send data
        List<STCardBean> listSTCardNotSend = localStCard.getListSTCardNotSend();
        if (listSTCardNotSend.isEmpty()) {
            javax.swing.SwingUtilities.invokeLater(() -> btnStatus.setEnabled(true));
            return;
        }

        // Phase 1: compute all values and collect valid items for batch insert
        List<ServerSTCardControl.STCardUploadParam> batchParams = new ArrayList<>();
        for (STCardBean stCardNotSend : listSTCardNotSend) {
            batchParams.add(new ServerSTCardControl.STCardUploadParam(
                    stCardNotSend, 0, 0, stCardNotSend.getRefund(), stCardNotSend.getRefNo(),
                    stCardNotSend.getCashier(), stCardNotSend.getEmp(),
                    stCardNotSend.getUnitPrice(), branchBean.getCode(), false));
        }

        // Phase 2: batch insert all valid items to server (single connection)
        boolean[] results = serverSTCardControl.saveSTCardBatch(batchParams);

        // Phase 3: update local flags based on batch results
        for (int i = 0; i < batchParams.size(); i++) {
            if (results[i]) {
                ServerSTCardControl.STCardUploadParam p = batchParams.get(i);
                txtLogMSG.setText("กำลังดำเนินการส่งข้อมูล stcard i = " + i);

                localStCard.updateSendStatus(p.bean, getCurrentDate(), getCurrentTime());
                uploadStkfile(p.bean.getS_PCode());
            }
        }

        javax.swing.SwingUtilities.invokeLater(() -> btnStatus.setEnabled(true));
    }

    private final LocalStkFileControl localStkFile = new LocalStkFileControl();
    private final ServerStkFileControl serverStkFileControl = new ServerStkFileControl();

    private void uploadStkfile(String bpcode) {
        branchBean = new BranchControl().getData();
        javax.swing.SwingUtilities.invokeLater(() -> txtSql.setText("STKFILE Upload Process"));

        if (bpcode != null && !bpcode.equals("")) {
            STKFileBean stkFileBean = localStkFile.getDataByBPCode(bpcode);
            if (stkFileBean == null) {
                stkFileBean = localStkFile.saveNewData(bpcode, branchBean.getCode(), STOCK_CODE);
            }

            // check update server
            if (stkFileBean != null) {
                STKFileBean serverStkFileBean = serverStkFileControl.getDataByBPCodeBranchCode(bpcode, branchBean.getCode());
                if (serverStkFileBean == null) {
                    serverStkFileControl.saveNewData(serverStkFileBean);
                } else {
                    serverStkFileControl.updateData(stkFileBean, getCurrentDate(), getCurrentTime());
                }
            }
        } else {
            List<STKFileBean> listStkFile = localStkFile.getAllData();
            if (!listStkFile.isEmpty()) {
                for (int i = 0; i < listStkFile.size(); i++) {
                    STKFileBean stkFileBean = (STKFileBean) listStkFile.get(i);
                    STKFileBean serverStkFileBean = serverStkFileControl.getDataByBPCodeBranchCode(stkFileBean.getbPcode(), stkFileBean.getBranch());
                    try {
                        if (serverStkFileBean == null) {
                            serverStkFileControl.saveNewData(stkFileBean);
                        }
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }

                    serverStkFileControl.updateData(stkFileBean, getCurrentDate(), getCurrentTime());

                    // update local time for stkfile
                    localStkFile.updateTimeData(stkFileBean.getbPcode(), getCurrentDate(), getCurrentTime());
                }
            }
        }
    }

    private final LocalPosHwSetupControl poshwControl = new LocalPosHwSetupControl();
    private final ServerPosHwSetupControl poshwServerControl = new ServerPosHwSetupControl();

    private void uploadLastRefno(String terminal, String currentDate, String currentTime, String branchCode) {
        String receNo1 = poshwControl.getReceNo1ByTerminal(terminal);
        if (receNo1 != null) {
            poshwServerControl.updateTime(receNo1, currentDate, currentTime, terminal, branchCode);
        }
    }

    private void uploadSaleToSTCardSection(List<STCardBean> listBean) {
        try {
            mysqlServer.open();
            mysqlLocal.open();

            if (!listBean.isEmpty()) {
                String sqlInsert = "INSERT INTO stcard ("
                        + "s_date, s_no, s_subNo, s_Que, s_pcode,"
                        + " s_stk, s_in, s_out, s_incost, s_outcost,"
                        + " s_acost, s_rem, s_user, s_entrydate, s_entrytime,"
                        + " s_link, s_bran, data_Sync, Source_data, Discount,"
                        + " Nettotal, Refund, Refno, Cashier, EMP,"
                        + " UnitPrice, R_index)"
                        + " VALUES(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?)";
                String sqlUpdate = "UPDATE ? "
                        + "SET r_send='Y' "
                        + "WHERE r_refno=? AND r_index=? AND r_plucode=? "
                        + "AND r_date=? AND r_time=? AND r_emp=? AND r_refund=?";
                final int totalItems = listBean.size();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    pbCheckUpdate.setStringPainted(true);
                    pbCheckUpdate.setMinimum(0);
                    pbCheckUpdate.setMaximum(totalItems);
                    pbCheckUpdate.setValue(0);
                    pbCheckUpdate.setString("เริ่มส่งข้อมูล ...");
                });

                try (PreparedStatement psInsert = mysqlServer.getConnection().prepareStatement(sqlInsert); PreparedStatement psUpdate = mysqlLocal.getConnection().prepareStatement(sqlUpdate)) {
                    for (int i = 0; i < listBean.size(); i++) {
                        STCardBean stcardBean = listBean.get(i);
                        final int current = i + 1;
                        final String sqlMsg = "กำลังส่งข้อมูล " + stcardBean.getTableUpdate() + " "
                                + "รหัส " + stcardBean.getS_PCode() + " "
                                + "ลำดับที่ " + current + "/" + totalItems;
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            txtSql.setText(sqlMsg);
                            pbCheckUpdate.setValue(current);
                            pbCheckUpdate.setString(current + " / " + totalItems);
                        });

                        // insert stcard server data
                        psInsert.setString(1, stcardBean.getS_Date());
                        psInsert.setString(2, stcardBean.getS_No());
                        psInsert.setString(3, "");
                        psInsert.setString(4, "0");
                        psInsert.setString(5, stcardBean.getS_PCode());
                        psInsert.setString(6, stcardBean.getS_Stk());
                        psInsert.setString(7, "0");
                        psInsert.setDouble(8, stcardBean.getS_Out());
                        psInsert.setString(9, "0");
                        psInsert.setDouble(10, stcardBean.getS_OutCost());
                        psInsert.setString(11, "0");
                        psInsert.setString(12, stcardBean.getS_Rem());
                        psInsert.setString(13, stcardBean.getS_User());
                        psInsert.setString(14, stcardBean.getS_EntryDate());
                        psInsert.setString(15, stcardBean.getS_EntryTime());
                        psInsert.setString(16, "N");
                        psInsert.setString(17, branchBean.getCode());
                        psInsert.setString(18, "N");
                        psInsert.setString(19, "POS");
                        psInsert.setDouble(20, stcardBean.getDiscount());
                        psInsert.setDouble(21, stcardBean.getNettotal());
                        psInsert.setString(22, stcardBean.getRefund());
                        psInsert.setString(23, stcardBean.getRefNo());
                        psInsert.setString(24, stcardBean.getCashier());
                        psInsert.setString(25, stcardBean.getEmp());
                        psInsert.setDouble(26, stcardBean.getUnitPrice());
                        psInsert.setString(27, stcardBean.getR_index());
                        psInsert.executeUpdate();

                        // update status stcard local flag
                        psUpdate.setString(1, stcardBean.getTableUpdate());
                        psUpdate.setString(2, stcardBean.getRefNo());
                        psUpdate.setString(3, stcardBean.getR_index());
                        psUpdate.setString(4, stcardBean.getS_PCode());
                        psUpdate.setString(5, stcardBean.getS_Date());
                        psUpdate.setString(6, stcardBean.getS_EntryTime());
                        psUpdate.setString(7, stcardBean.getEmp());
                        psUpdate.setString(8, stcardBean.getRefund());

                        psUpdate.executeUpdate();
                    }
                }

                javax.swing.SwingUtilities.invokeLater(() -> {
                    pbCheckUpdate.setValue(totalItems);
                    pbCheckUpdate.setString("ส่งข้อมูล เสร็จสิ้น (" + totalItems + " รายการ)");
                });

                for (int i = 0; i < listBean.size(); i++) {
                    uploadStkfile(listBean.get(i).getS_PCode());
                }
            }
        } catch (SQLException e) {
            AppLogUtil.log(this.getClass(), "error", e);
        } finally {
            mysqlServer.close();
            mysqlLocal.close();
        }
    }

    private void uploadUpdateVoid() {
        try {
            mysqlLocal.open();
            mysqlServer.open();

            String sql = "select * from t_sale where r_refund='V';";
            try (ResultSet rs = mysqlLocal.getConnection().createStatement().executeQuery(sql)) {
                List<STCardBean> listBean = new ArrayList();
                while (rs.next()) {
                    STCardBean bean = new STCardBean();
                    bean.setS_Date(rs.getString("R_Date"));
                    bean.setS_PCode(rs.getString("R_Plucode"));
                    bean.setS_Stk("A1");
                    bean.setS_Out(rs.getInt("R_Quan"));
                    bean.setS_OutCost(rs.getInt("R_Total"));
                    bean.setS_Rem("SAL");
                    bean.setS_User(rs.getString("Cashier"));
                    bean.setS_EntryDate(rs.getString("R_Date"));
                    bean.setR_time(rs.getString("R_time"));
                    bean.setNettotal(rs.getDouble("R_Nettotal"));
                    bean.setRefund(rs.getString("R_Refund"));
                    bean.setRefNo(rs.getString("R_Refno"));
                    bean.setCashier(rs.getString("Cashier"));
                    bean.setEmp(rs.getString("R_Emp"));
                    bean.setUnitPrice(rs.getDouble("R_Price"));
                    bean.setR_index(rs.getString("R_Index"));
                    bean.setS_EntryTime(rs.getString("R_Time"));

                    listBean.add(bean);
                }
                final int totalVoid = listBean.size();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    pbCheckUpdate.setStringPainted(true);
                    pbCheckUpdate.setMinimum(0);
                    pbCheckUpdate.setMaximum(totalVoid > 0 ? totalVoid : 1);
                    pbCheckUpdate.setValue(0);
                    pbCheckUpdate.setString("อัพเดต Void (" + totalVoid + " รายการ)");
                });
                String sqlRefund = "UPDATE stcard SET refund=?"
                        + " WHERE refno=? AND r_index=? AND s_pcode=?"
                        + " AND s_entrydate=? AND s_entrytime=? AND s_out=?"
                        + " AND emp=? AND cashier=? AND s_date=? AND refund<>'V'";
                try (PreparedStatement psRefund = mysqlServer.getConnection().prepareStatement(sqlRefund)) {
                    for (int i = 0; i < listBean.size(); i++) {
                        STCardBean b = listBean.get(i);
                        final int currentVoid = i + 1;
                        final String voidMsg = "Processing Void='V' " + b.getS_PCode() + " (" + currentVoid + "/" + totalVoid + ")";
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            txtSql.setText(voidMsg);
                            pbCheckUpdate.setValue(currentVoid);
                            pbCheckUpdate.setString(currentVoid + " / " + totalVoid);
                        });
                        psRefund.setString(1, b.getRefund());
                        psRefund.setString(2, b.getRefNo());
                        psRefund.setString(3, b.getR_index());
                        psRefund.setString(4, b.getS_PCode());
                        psRefund.setString(5, b.getS_EntryDate());
                        psRefund.setString(6, b.getS_EntryTime());
                        psRefund.setDouble(7, b.getS_Out());
                        psRefund.setString(8, b.getEmp());
                        psRefund.setString(9, b.getCashier());
                        psRefund.setString(10, b.getS_Date());
                        psRefund.executeUpdate();
                        uploadStkfile(b.getS_PCode());
                    }
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        pbCheckUpdate.setValue(totalVoid);
                        pbCheckUpdate.setString("อัพเดต Void เสร็จสิ้น (" + totalVoid + " รายการ)");
                    });
                }
            }

        } catch (SQLException e) {
            AppLogUtil.log(this.getClass(), "error", e);
        } finally {
            mysqlLocal.close();
            mysqlServer.close();
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            Api_RealTimeSalesToColoServer dialog = new Api_RealTimeSalesToColoServer();
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (dialog.scheduler != null) {
                        dialog.scheduler.shutdown();
                    }
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
    private javax.swing.JTextArea txtLogMSG;
    private javax.swing.JTextArea txtSql;
    // End of variables declaration//GEN-END:variables

    private final LocalTSaleControl localTSaleControl = new LocalTSaleControl();

    private final LocalSTranControl localStranControl = new LocalSTranControl();

    private String getCurrentDate() {
        return dateConvert.GetCurrentDate();
    }

    private String getCurrentTime() {
        return dateConvert.GetCurrentTime();
    }

    private void updateStcardServerWithoutSALtype() {
        // รายการที่ยังไม่ปิดสิ้นวัน
        List<STCardBean> list1 = localTSaleControl.getTSaleTransaction();
        List<STCardBean> list2 = localStranControl.getSTranTransaction();
        List<STCardBean> list3 = localStranControl.getTransaction15DayAgo(branchBean.getCode());

        List<STCardBean> result = Stream.of(list1, list2, list3).flatMap(List::stream).collect(Collectors.toList());
        uploadSaleToSTCardSection(result);
    }

}
