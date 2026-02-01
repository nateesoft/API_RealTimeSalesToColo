package main;

import com.ics.pos.core.controller.BranchControl;
import com.ics.bean.BranchBean;
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

    private ProcessController processController = null;
    private final BranchBean branchBean = BranchControl.getData();
    private final DateConvert dateConvert = new DateConvert();

    // Scheduler สำหรับ run task ทุกๆ 5 นาที
    private ScheduledExecutorService scheduler;
    private static final int UPLOAD_INTERVAL_MINUTES = 1; // fix 5 for every 5 minutes

    public String ErrorText = "Log Error.." + "\r\n";
    public String LogQuery = "Log SQL.." + "\r\n";

    public Api_RealTimeSalesToColoServer() {
        initComponents();
        setState(JFrame.ICONIFIED);

        System.out.println("Loop For Upload Stcard/Stkfile Update");
        btnUpload.setText(dateConvert.GetCurrentTime());
        if (branchBean != null) {
            lblBranch.setText("รหัสสาขา : " + branchBean.getCode());
            btnStatus.setText("Finsished time : " + dateConvert.GetCurrentTime());

            // เริ่มต้น ProcessController และ Scheduler
            initializeAndStartScheduler();
        }
    }

    /**
     * เริ่มต้น ProcessController และตั้งเวลา upload ทุกๆ 5 นาที
     */
    private void initializeAndStartScheduler() {
        // สร้าง ProcessController ครั้งเดียว
        processController = new ProcessController(txtLogErr, btnStatus, lblDisplayStcard,
                txtSql, lblDisplayStcard1, btnStatus1, pbCheckUpdate);
        processController.openConnections();

        // สร้าง Scheduler ด้วย single thread
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Task ที่จะ run ทุกๆ 5 นาที
        Runnable uploadTask = () -> {
            try {
                System.out.println("=== Scheduled upload started at: " + dateConvert.GetCurrentTime() + " ===");
                processController.uploadStcard(branchBean.getCode());
                btnStatus.setText("Last upload: " + dateConvert.GetCurrentTime());
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("API_RealtimeOnline");
        setUndecorated(true);

        btnUpload.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnUpload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/upload-icon-18.png"))); // NOI18N
        btnUpload.setText("   Click here");
        btnUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadActionPerformed(evt);
            }
        });

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
        jLabel1.setText("API-AutoSales Online V5.10 20260129/14:02");

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
        btnStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatusActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnUpload, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addComponent(pbCheckUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnStatus1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnStatus2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDisplayStcard1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDisplayStcard, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBranch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(124, 124, 124)
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
                .addComponent(pbCheckUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnStatus1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDisplayStcard1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStatus2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDisplayStcard, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lblBranch, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadActionPerformed
        if (processController != null) {
            processController.uploadCheckConfig();
        }
    }//GEN-LAST:event_btnUploadActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void btnStatus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatus1ActionPerformed
        if (processController != null) {
            new Thread(() -> {
                processController.uploadStkfile("", branchBean.getCode());
            }).start();
        }
    }//GEN-LAST:event_btnStatus1ActionPerformed

    private void btnStatus2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatus2ActionPerformed
        if (processController != null) {
            new Thread(() -> {
                processController.uploadStcard(branchBean.getCode());
            }).start();
        }
    }//GEN-LAST:event_btnStatus2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        shutdown();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * ปิด scheduler, connection และจบโปรแกรมอย่างถูกต้อง
     */
    private void shutdown() {
        try {
            // หยุด Scheduler ก่อน
            if (scheduler != null && !scheduler.isShutdown()) {
                System.out.println("Stopping scheduler...");
                scheduler.shutdown();
                // รอให้ task ที่กำลังทำงานอยู่เสร็จ (timeout 10 วินาที)
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                System.out.println("Scheduler stopped");
            }

            // ปิด Connection
            if (processController != null) {
                processController.closeConnections();
            }

            System.out.println("Application shutdown gracefully");
        } catch (InterruptedException e) {
            Logger.getLogger(Api_RealTimeSalesToColoServer.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            System.exit(0);
        }
    }

    private void btnStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnStatusActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            Api_RealTimeSalesToColoServer dialog = new Api_RealTimeSalesToColoServer();

            // เพิ่ม Shutdown Hook สำหรับปิด scheduler และ connection เมื่อ JVM shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown hook triggered");
                if (dialog.scheduler != null && !dialog.scheduler.isShutdown()) {
                    dialog.scheduler.shutdownNow();
                    System.out.println("Shutdown hook: Scheduler stopped");
                }
                if (dialog.processController != null) {
                    dialog.processController.closeConnections();
                    System.out.println("Shutdown hook: Connections closed");
                }
            }));

            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dialog.shutdown();
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
}
