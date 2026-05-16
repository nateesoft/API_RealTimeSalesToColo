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
import java.sql.Connection;
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

    public static final String TERMINAL_FIXED = "001";
    public static final String STOCK_CODE = "A1";
    public static final String STCARD_SALE_TYPE = "SAL";
    public static final String POS_SOURCE_DATA = "POS";
    public static final String WEB_SOURCE_DATA = "WEB";
    public static final String DATA_NOT_SYNC = "Y";
    public static final String BRANCH_FIX = "999";
    public static final String BRANCH_FIX_DEFAULT = "sss";

    private final TranIOControl TIO = new TranIOControl();

    private final MySQLConnectWebOnline mysqlServer = new MySQLConnectWebOnline();
    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public Api_RealTimeSalesToColoServer() {
        initComponents();

        AppLogUtil.info(getClass(), "=== Api_RealTimeSalesToColoServer เริ่มต้นระบบ ===");
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
        new Thread(() -> {
            int attempt = 0;
            while (true) {
                attempt++;
                final int currentAttempt = attempt;
                SwingUtilities.invokeLater(()
                        -> btnStatus.setText("ตรวจสอบ MySQL... ครั้งที่ " + currentAttempt)
                );

                boolean localOk = testLocalConnection();
                boolean webOk = testWebConnection();

                final boolean localStatus = localOk;
                final boolean webStatus = webOk;
                String connLog = "Attempt " + currentAttempt
                        + " | Local: " + (localOk ? "OK" : "FAIL")
                        + " | Web: " + (webOk ? "OK" : "FAIL");
                if (localOk && webOk) {
                    AppLogUtil.info(getClass(), connLog);
                } else {
                    AppLogUtil.warning(getClass(), connLog);
                }

                if (localOk && webOk) {
                    branchBean = new BranchControl().getData(BRANCH_FIX, BRANCH_FIX_DEFAULT);
                    final BranchBean bean = branchBean;

                    SwingUtilities.invokeLater(() -> {
                        if (bean != null) {
                            lblBranch.setText("รหัสสาขา : " + bean.getCode());
                            btnStatus.setText("MySQL เชื่อมต่อสำเร็จ : " + getCurrentTime());

                            initializeAndStartScheduler();
                        } else {
                            lblBranch.setText("ไม่พบข้อมูลสาขา");
                            btnStatus.setText("เกิดข้อผิดพลาด: ไม่พบข้อมูลสาขา");
                        }
                    });
                    break;
                }

                SwingUtilities.invokeLater(()
                        -> btnStatus.setText("Local: " + (localStatus ? "OK" : "FAIL")
                                + " | Web: " + (webStatus ? "OK" : "FAIL")
                                + " | Retry in 5s...")
                );
            }
        }, "MySQL-ConnectionCheck").start();
    }

    private boolean testLocalConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + MySQLConnect.HostName + ":" + MySQLConnect.PortNumber
                    + "/" + MySQLConnect.DbName
                    + "?useUnicode=true&characterEncoding=" + MySQLConnect.CharSet
                    + "&serverTimezone=Asia/Bangkok&useSSL=false";
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    url, MySQLConnect.UserName, MySQLConnect.Password)) {
                return conn != null;
            }
        } catch (ClassNotFoundException | SQLException e) {
            AppLogUtil.error(getClass(), "Local MySQL connect ล้มเหลว: " + e.getMessage(), e);
            return false;
        }
    }

    private boolean testWebConnection() {
        MySQLConnectWebOnline testConn = new MySQLConnectWebOnline();
        try {
            testConn.open();
            boolean ok = testConn.getConnection() != null;
            if (!ok) {
                AppLogUtil.warning(getClass(), "Web MySQL connect ล้มเหลว: connection เป็น null");
            }
            return ok;
        } catch (Exception e) {
            AppLogUtil.error(getClass(), "Web MySQL connect ล้มเหลว: " + e.getMessage(), e);
            return false;
        } finally {
            testConn.close();
        }
    }

    /**
     * เริ่มต้น ProcessController และตั้งเวลา upload ทุกๆ 5 นาที
     */
    private void initializeAndStartScheduler() {
        // สร้าง Scheduler ด้วย daemon thread เพื่อให้ JVM exit ได้เมื่อปิดหน้าต่าง
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SchedulerUploadTask");
            t.setDaemon(true);
            return t;
        });

        AppLogUtil.info(getClass(), "Scheduler เริ่มต้น: upload ทุก " + UPLOAD_INTERVAL_MINUTES + " นาที | branch=" + branchBean.getCode());

        // Task ที่จะ run ทุกๆ x นาที
        Runnable uploadTask = () -> {
            String cycleTime = getCurrentTime();
            AppLogUtil.info(getClass(), "--- Upload cycle เริ่มต้น [" + cycleTime + "] ---");
            SwingUtilities.invokeLater(() -> txtLogMSG.setText("กำลังเข้าสู่รอบการส่งข้อมูล [" + cycleTime + "]"));

            try {
                // update Refno
                uploadLastRefno(TERMINAL_FIXED, getCurrentDate(), cycleTime, branchBean.getCode());

                uploadStcardNotSendNotSALtype();
                updateStcardInSALtype();
                uploadUpdateVoid(branchBean.getCode());

                String doneTime = getCurrentTime();
                AppLogUtil.info(getClass(), "--- Upload cycle เสร็จสิ้น [" + doneTime + "] ---");
                SwingUtilities.invokeLater(() -> btnStatus.setText("Last upload: " + doneTime));
            } catch (Exception e) {
                AppLogUtil.error(getClass(), "Upload cycle ล้มเหลว: " + e.getMessage(), e);
            }
        };

        // เริ่ม schedule: run ครั้งแรกทันที (0), แล้ว repeat ทุก 5 นาที
        scheduler.scheduleAtFixedRate(uploadTask, 0, UPLOAD_INTERVAL_MINUTES, TimeUnit.MINUTES);
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
            uploadAllStkfile();
        }).start();
    }//GEN-LAST:event_btnStatus1ActionPerformed

    private void btnStatus2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatus2ActionPerformed
        new Thread(() -> {
            uploadStcardNotSendNotSALtype();
        }).start();
    }//GEN-LAST:event_btnStatus2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        AppLogUtil.info(getClass(), "=== ระบบปิดตัว (Exit) ===");
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

    private void uploadStcardNotSendNotSALtype() {
        List<STCardBean> listSTCardNotSend = localStCard.getListSTCardNotSendNotSALtype(STCARD_SALE_TYPE);
        if (listSTCardNotSend.isEmpty()) {
            AppLogUtil.info(getClass(), "uploadStcard: ไม่มีรายการ STCard ที่ยังไม่ส่ง");
            SwingUtilities.invokeLater(() -> btnStatus.setEnabled(true));
            return;
        }

        AppLogUtil.info(getClass(), "uploadStcard: พบ " + listSTCardNotSend.size() + " รายการที่ยังไม่ส่ง");

        // Phase 1: collect batch params
        List<ServerSTCardControl.STCardUploadParam> batchParams = new ArrayList<>();
        for (STCardBean stCardNotSend : listSTCardNotSend) {
            batchParams.add(new ServerSTCardControl.STCardUploadParam(
                    stCardNotSend, 0, 0, stCardNotSend.getRefund(), stCardNotSend.getRefNo(),
                    stCardNotSend.getCashier(), stCardNotSend.getEmp(),
                    stCardNotSend.getUnitPrice(), branchBean.getCode(), false));
        }

        // Phase 2: batch insert to server
        boolean[] results = serverSTCardControl.saveSTCardBatch(batchParams);

        // Phase 3: update local flags
        int successCount = 0;
        for (int i = 0; i < batchParams.size(); i++) {
            if (results[i]) {
                successCount++;
                ServerSTCardControl.STCardUploadParam p = batchParams.get(i);
                final int idx = i + 1;
                final int total = batchParams.size();
                AppLogUtil.info(getClass(), "STCard ส่งสำเร็จ [" + idx + "/" + total + "] pcode=" + p.bean.getS_PCode() + " refno=" + p.bean.getRefNo());
                SwingUtilities.invokeLater(() -> txtLogMSG.setText("ส่ง STCard [" + idx + "/" + total + "] pcode=" + p.bean.getS_PCode()));

                // update local flag
                boolean updateLocalResult = localStCard.updateSendStatus(p.bean, getCurrentDate(), getCurrentTime());
                if (updateLocalResult) {
                    AppLogUtil.info(getClass(), "Update local s_send='Y' stcard pcode=" + batchParams.get(i).bean.getS_PCode() + " สำเร็จ");
                } else {
                    AppLogUtil.warning(getClass(), "Update local s_send='Y' stcard pcode=" + batchParams.get(i).bean.getS_PCode() + " ไม่สำเร็จ");
                }

                // update stkfile table
                uploadStkfile(p.bean.getS_PCode());
            } else {
                AppLogUtil.warning(getClass(), "STCard ส่งไม่สำเร็จ index=" + i + " pcode=" + batchParams.get(i).bean.getS_PCode());
            }
        }

        AppLogUtil.info(getClass(), "uploadStcard เสร็จสิ้น: สำเร็จ " + successCount + "/" + batchParams.size() + " รายการ");
        SwingUtilities.invokeLater(() -> btnStatus.setEnabled(true));
    }

    private final LocalStkFileControl localStkFile = new LocalStkFileControl();
    private final ServerStkFileControl serverStkFileControl = new ServerStkFileControl();

    private void uploadStkfile(String bpcode) {
        if (bpcode == null || bpcode.isEmpty()) {
            AppLogUtil.warning(getClass(), "ไม่พบข้อมูล pcode=" + bpcode);
            return;
        }

        AppLogUtil.info(getClass(), "uploadStkfile: pcode=" + bpcode);
        SwingUtilities.invokeLater(() -> txtSql.setText("STKFILE pcode=" + bpcode));

        STKFileBean stkFileBean = localStkFile.getDataByBPCode(bpcode);
        if (stkFileBean == null) {
            AppLogUtil.info(getClass(), "StkFile ไม่พบใน local สร้างใหม่ pcode=" + bpcode);
            stkFileBean = localStkFile.saveNewData(bpcode, branchBean.getCode(), STOCK_CODE);
        }

        if (stkFileBean != null) {
            STKFileBean serverStkFileBean = serverStkFileControl.getDataByBPCodeBranchCode(bpcode, branchBean.getCode());
            if (serverStkFileBean == null) {
                AppLogUtil.info(getClass(), "StkFile insert server ใหม่ pcode=" + bpcode);
                serverStkFileControl.saveNewData(stkFileBean);
            } else {
                AppLogUtil.info(getClass(), "StkFile update server pcode=" + bpcode);
                serverStkFileControl.updateData(stkFileBean, getCurrentDate(), getCurrentTime());
            }
        } else {
            AppLogUtil.warning(getClass(), "uploadStkfile: ไม่สามารถสร้าง stkFileBean pcode=" + bpcode);
        }
    }

    private void uploadAllStkfile() {
        branchBean = new BranchControl().getData(BRANCH_FIX, BRANCH_FIX_DEFAULT);

        List<STKFileBean> listStkFile = localStkFile.getAllData();
        AppLogUtil.info(getClass(), "uploadStkfile (all): พบ " + listStkFile.size() + " รายการ");
        SwingUtilities.invokeLater(() -> txtSql.setText("STKFILE Upload All: " + listStkFile.size() + " รายการ"));

        for (STKFileBean stkFileBean : listStkFile) {
            STKFileBean serverStkFileBean = serverStkFileControl.getDataByBPCodeBranchCode(stkFileBean.getbPcode(), stkFileBean.getBranch());
            try {
                if (serverStkFileBean == null) {
                    AppLogUtil.info(getClass(), "StkFile insert server ใหม่ pcode=" + stkFileBean.getbPcode());
                    serverStkFileControl.saveNewData(stkFileBean);
                }
            } catch (Exception e) {
                AppLogUtil.error(getClass(), "StkFile saveNewData ล้มเหลว pcode=" + stkFileBean.getbPcode(), e);
            }

            serverStkFileControl.updateData(stkFileBean, getCurrentDate(), getCurrentTime());
            localStkFile.updateTimeData(stkFileBean.getbPcode(), getCurrentDate(), getCurrentTime());
        }

        AppLogUtil.info(getClass(), "uploadStkfile (all) เสร็จสิ้น");
    }

    private final LocalPosHwSetupControl poshwControl = new LocalPosHwSetupControl();
    private final ServerPosHwSetupControl poshwServerControl = new ServerPosHwSetupControl();

    private void uploadLastRefno(String terminal, String currentDate, String currentTime, String branchCode) {
        String receNo1 = poshwControl.getReceNo1ByTerminal(terminal);
        if (receNo1 != null) {
            AppLogUtil.info(getClass(), "uploadLastRefno: terminal=" + terminal + " receNo1=" + receNo1);
            poshwServerControl.updateTime(receNo1, currentDate, currentTime, terminal, branchCode);
        } else {
            AppLogUtil.warning(getClass(), "uploadLastRefno: ไม่พบ receNo1 สำหรับ terminal=" + terminal);
        }
    }

    private void uploadSaleToSTCardSection(List<STCardBean> listBean) {
        AppLogUtil.info(getClass(), "uploadSaleToSTCardSection: " + listBean.size() + " รายการ");
        if (listBean.isEmpty()) {
            return;
        }
        try {
            mysqlServer.open();
            mysqlLocal.open();

            String sqlInsert = "INSERT INTO stcard ("
                    + "s_date, s_no, s_subNo, s_Que, s_pcode,"
                    + " s_stk, s_in, s_out, s_incost, s_outcost,"
                    + " s_acost, s_rem, s_user, s_entrydate, s_entrytime,"
                    + " s_link, s_bran, data_Sync, Source_data, Discount,"
                    + " Nettotal, Refund, Refno, Cashier, EMP,"
                    + " UnitPrice, R_index)"
                    + " VALUES(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?)";
            String sqlUpdateSTran = "UPDATE s_tran SET r_send='Y' "
                    + "WHERE r_refno=? AND r_index=? AND r_plucode=?";
            String sqlUpdateTSale = "UPDATE t_sale SET r_send='Y' "
                    + "WHERE r_refno=? AND r_index=? AND r_plucode=?";
            final int totalItems = listBean.size();
            SwingUtilities.invokeLater(() -> {
                pbCheckUpdate.setStringPainted(true);
                pbCheckUpdate.setMinimum(0);
                pbCheckUpdate.setMaximum(totalItems);
                pbCheckUpdate.setValue(0);
                pbCheckUpdate.setString("เริ่มส่งข้อมูล ...");
            });

            Connection serverConn = mysqlServer.getConnection();
            Connection localConn = mysqlLocal.getConnection();
            serverConn.setAutoCommit(false);
            localConn.setAutoCommit(false);
            try (PreparedStatement psInsert = serverConn.prepareStatement(sqlInsert); PreparedStatement psUpdateSTran = localConn.prepareStatement(sqlUpdateSTran); PreparedStatement psUpdateTSale = localConn.prepareStatement(sqlUpdateTSale)) {
                for (int i = 0; i < listBean.size(); i++) {
                    STCardBean stcardBean = listBean.get(i);
                    
                    final int current = i + 1;
                    final String sqlMsg = "กำลังส่งข้อมูล " + stcardBean.getTableUpdate() + " "
                            + "รหัส " + stcardBean.getS_PCode() + " "
                            + "ลำดับที่ " + current + "/" + totalItems;
                    SwingUtilities.invokeLater(() -> {
                        txtSql.setText(sqlMsg);
                        pbCheckUpdate.setValue(current);
                        pbCheckUpdate.setString(current + " / " + totalItems);
                    });

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
                    psInsert.setString(19, POS_SOURCE_DATA);
                    psInsert.setDouble(20, stcardBean.getDiscount());
                    psInsert.setDouble(21, stcardBean.getNettotal());
                    psInsert.setString(22, stcardBean.getRefund());
                    psInsert.setString(23, stcardBean.getRefNo());
                    psInsert.setString(24, stcardBean.getCashier());
                    psInsert.setString(25, stcardBean.getEmp());
                    psInsert.setDouble(26, stcardBean.getUnitPrice());
                    psInsert.setString(27, stcardBean.getR_index());
                    psInsert.addBatch();

                    if ("s_tran".equals(stcardBean.getTableUpdate())) {
                        addLocalSendStatusBatch(psUpdateSTran, stcardBean);
                    } else {
                        addLocalSendStatusBatch(psUpdateTSale, stcardBean);
                    }
                }
                psInsert.executeBatch();
                serverConn.commit();

                psUpdateSTran.executeBatch();
                psUpdateTSale.executeBatch();
                localConn.commit();
            } catch (SQLException e) {
                serverConn.rollback();
                localConn.rollback();
                throw e;
            } finally {
                serverConn.setAutoCommit(true);
                localConn.setAutoCommit(true);
            }

            AppLogUtil.info(getClass(), "uploadSaleToSTCardSection เสร็จสิ้น: " + totalItems + " รายการ");
            SwingUtilities.invokeLater(() -> {
                pbCheckUpdate.setValue(totalItems);
                pbCheckUpdate.setString("ส่งข้อมูล เสร็จสิ้น (" + totalItems + " รายการ)");
            });

            for (int i = 0; i < listBean.size(); i++) {
                uploadStkfile(listBean.get(i).getS_PCode());
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), "uploadSaleToSTCardSection SQL error: " + e.getMessage(), e);
        } finally {
            mysqlServer.close();
            mysqlLocal.close();
        }
    }

    private void addLocalSendStatusBatch(PreparedStatement ps, STCardBean bean) throws SQLException {
        ps.setString(1, bean.getRefNo());
        ps.setString(2, bean.getR_index());
        ps.setString(3, bean.getS_PCode());
        ps.addBatch();
    }

    private void uploadUpdateVoid(String branchCode) {
        AppLogUtil.info(getClass(), "uploadUpdateVoid: เริ่มตรวจสอบรายการ Void");
        try {
            mysqlLocal.open();
            mysqlServer.open();

            String sql = "select * from t_sale where r_refund='V';";
            try (java.sql.Statement stmt = mysqlLocal.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                List<STCardBean> listBean = new ArrayList<>();
                while (rs.next()) {
                    STCardBean bean = new STCardBean();
                    bean.setS_Date(rs.getString("R_Date"));
                    bean.setS_PCode(rs.getString("R_Plucode"));
                    bean.setS_Stk(STOCK_CODE);
                    bean.setS_Out(rs.getInt("R_Quan"));
                    bean.setS_OutCost(rs.getInt("R_Total"));
                    bean.setS_Rem(STCARD_SALE_TYPE);
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
                AppLogUtil.info(getClass(), "uploadUpdateVoid: พบรายการ Void " + totalVoid + " รายการ");
                SwingUtilities.invokeLater(() -> {
                    pbCheckUpdate.setStringPainted(true);
                    pbCheckUpdate.setMinimum(0);
                    pbCheckUpdate.setMaximum(totalVoid > 0 ? totalVoid : 1);
                    pbCheckUpdate.setValue(0);
                    pbCheckUpdate.setString("อัพเดต Void (" + totalVoid + " รายการ)");
                });
                String sqlRefund = "UPDATE stcard "
                        + "SET refund=? "
                        + "WHERE s_bran=? AND refno=? "
                        + "AND r_index=? AND s_pcode=? "
                        + "AND s_out=? AND emp=? "
                        + "AND cashier=? "
                        + "AND refund<>'V'";
                try (PreparedStatement psRefund = mysqlServer.getConnection().prepareStatement(sqlRefund)) {
                    for (int i = 0; i < listBean.size(); i++) {
                        STCardBean b = listBean.get(i);
                        final int currentVoid = i + 1;
                        AppLogUtil.info(getClass(), "Void update [" + currentVoid + "/" + totalVoid + "] pcode=" + b.getS_PCode() + " refno=" + b.getRefNo());
                        final String voidMsg = "Processing Void='V' " + b.getS_PCode() + " (" + currentVoid + "/" + totalVoid + ")";
                        SwingUtilities.invokeLater(() -> {
                            txtSql.setText(voidMsg);
                            pbCheckUpdate.setValue(currentVoid);
                            pbCheckUpdate.setString(currentVoid + " / " + totalVoid);
                        });

                        psRefund.setString(1, b.getRefund());
                        psRefund.setString(2, branchCode);
                        psRefund.setString(3, b.getRefNo());
                        psRefund.setString(4, b.getR_index());
                        psRefund.setString(5, b.getS_PCode());
                        psRefund.setDouble(6, b.getS_Out());
                        psRefund.setString(7, b.getEmp());
                        psRefund.setString(8, b.getCashier());
                        psRefund.executeUpdate();

                        uploadStkfile(b.getS_PCode());
                    }
                    AppLogUtil.info(getClass(), "uploadUpdateVoid เสร็จสิ้น: " + totalVoid + " รายการ");
                    SwingUtilities.invokeLater(() -> {
                        pbCheckUpdate.setValue(totalVoid);
                        pbCheckUpdate.setString("อัพเดต Void เสร็จสิ้น (" + totalVoid + " รายการ)");
                    });
                }
            }

        } catch (SQLException e) {
            AppLogUtil.error(getClass(), "uploadUpdateVoid SQL error: " + e.getMessage(), e);
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

    private void updateStcardInSALtype() {
        // รายการที่ยังไม่ปิดสิ้นวัน
        List<STCardBean> list1 = localTSaleControl.getTSaleTransaction(STOCK_CODE, STCARD_SALE_TYPE, POS_SOURCE_DATA, DATA_NOT_SYNC);
        uploadSaleToSTCardSection(list1);

        List<STCardBean> list2 = localStranControl.getSTranTransaction(STOCK_CODE, STCARD_SALE_TYPE, POS_SOURCE_DATA, DATA_NOT_SYNC);
        uploadSaleToSTCardSection(list2);

        List<STCardBean> list3 = localStranControl.getTransaction15DayAgo(branchBean.getCode(), STOCK_CODE, STCARD_SALE_TYPE, POS_SOURCE_DATA, DATA_NOT_SYNC);
        uploadSaleToSTCardSection(list3);
    }

}
