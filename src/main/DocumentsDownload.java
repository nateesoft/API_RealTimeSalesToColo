package main;

import com.ics.bean.BranchBean;
import com.ics.bean.STCardBean;
import com.ics.pos.core.controller.BranchControl;
import database.MySQLConnect;
import database.MySQLConnectWebOnline;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import util.MSG;

/**
 *
 * @author Administrator
 */
public class DocumentsDownload extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
    BranchBean branchBean = new BranchControl().getData();
    BranchControl branControl = new BranchControl();
    private MySQLConnectWebOnline mysqlWebOnline = new MySQLConnectWebOnline();
    private MySQLConnect mysql = new MySQLConnect();

    public void DocumentsDownload() {
        initComponents();
        mysql.open();
        mysqlWebOnline.open();
        try {
            receiveDocuments(mysql.getConnection(), mysqlWebOnline.getConnection());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mysql.close();
        mysqlWebOnline.close();

    }

    private void receiveDocuments(Connection mysqlLocal, Connection mysqlWeb) {
        try {
            List<STCardBean> listSTCardNotSend = new ArrayList();
            listSTCardNotSend.clear();
            String sql = "select * from stcard "
                    + "where s_bran='" + branchBean.getCode() + "' "
                    + "and data_sync='N' and "
                    + "Source_data='WEB' "
                    + "order by s_date,s_no,s_que ";
            ResultSet rs = mysqlWeb.createStatement().executeQuery(sql);
            while (rs.next()) {
                STCardBean bean = new STCardBean();
                bean.setS_No(rs.getString("S_NO"));
                bean.setS_Que(rs.getInt("S_Que"));
                bean.setS_PCode(rs.getString("S_Pcode"));
                bean.setS_In(rs.getInt("S_In"));
                bean.setS_InCost(rs.getInt("S_Incost"));
                bean.setS_Out(rs.getInt("S_Out"));
                bean.setS_OutCost(rs.getInt("S_Outcost"));
                bean.setS_Rem(rs.getString("S_Rem"));
                bean.setS_User(rs.getString("S_User"));
                bean.setS_EntryDate(rs.getString("S_Entrydate"));
                bean.setS_EntryTime(rs.getString("S_Entrytime"));
                bean.setDataSync(rs.getString("Data_Sync"));
                bean.setSource_Data(rs.getString("Source_data"));
                bean.setNettotal(rs.getInt("nettotal"));
                listSTCardNotSend.add(bean);
            }
            if (!listSTCardNotSend.isEmpty()) {
                for (int i = 0; i <= listSTCardNotSend.size(); i++) {
                    String sqlInsLoccal = "";
                    boolean checkDoc = false;
                    if (listSTCardNotSend.get(i).getS_Rem().equals("TRI_HQ")) {

                        // Check Doc Local ก่อนนะว่ามีไหม
                        checkDoc = checkDocLocal(listSTCardNotSend.get(i).getS_No(), mysql.getConnection(), "TRI_HQ");
                        sqlInsLoccal = "INSERT INTO tranin "
                                + "(R_No, R_Que, R_PCode, R_Stock, R_Pack, "
                                + "R_Qty, R_Post, R_Unit, R_Cost, R_Amount, "
                                + "R_TotalQty, R_User, R_Time, R_EntryDate, R_SendFTP, "
                                + "R_Pqty, R_Order, R_Send, R_RefCode, R_SendInterface) "
                                + "VALUES "
                                + "('124', 1, NULL, NULL, 1, "
                                + "0, 'N', NULL, 0, 0, "
                                + "0, NULL, NULL, NULL, 'N', "
                                + "NULL, 0, 0, NULL, NULL)";
                        System.out.println(sqlInsLoccal);
                    } else {
                        checkDoc = checkDocLocal(listSTCardNotSend.get(i).getS_No(), mysql.getConnection(), "TRI_HQ");
                        sqlInsLoccal = "INSERT INTO tranout "
                                + "(R_No, R_Que, R_PCode, R_Stock, R_Pack, "
                                + "R_Qty, R_Post, R_Unit, R_Cost, R_Amount, "
                                + "R_TotalQty, R_User, R_Time, R_EntryDate, R_SendFTP, "
                                + "R_Pqty, R_Order, R_Send, R_RefCode, R_SendInterface) "
                                + "VALUES "
                                + "('124', 1, NULL, NULL, 1, "
                                + "0, 'N', NULL, 0, 0, "
                                + "0, NULL, NULL, NULL, 'N', "
                                + "NULL, 0, 0, NULL, NULL)";
                        System.out.println(sqlInsLoccal);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkDocLocal(String s_no, Connection mysqlLocal, String type) {
        boolean docInCase = false;

        try {
            String sql = "select r_no from tranin where r_no='" + s_no + "';";
            ResultSet rs = mysqlLocal.createStatement().executeQuery(sql);
            if (rs.next()) {
                docInCase = true;
                MSG.NOTICE("เอกสารนี้มีอยู่ในระบบอยู่แล้ว กรุณาแจ้งสำนักงานใหญ่ ให้ออกเอกสารใหม่");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return docInCase;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jToggleButton1 = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/downlload.png"))); // NOI18N
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField1.setFont(new java.awt.Font("TH Baijam", 1, 11)); // NOI18N

        jToggleButton1.setText("X");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(505, 505, 505)
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 2, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 473, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DocumentsDownload();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DocumentsDownload.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DocumentsDownload.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DocumentsDownload.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DocumentsDownload.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DocumentsDownload().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables
}
