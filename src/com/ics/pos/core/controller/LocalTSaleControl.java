package com.ics.pos.core.controller;

import com.ics.bean.STCardBean;
import database.MySQLConnect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.AppLogUtil;

/**
 *
 * @author nateelive
 */
public class LocalTSaleControl {

    private final MySQLConnect mysqlLocal = new MySQLConnect();

    public List<STCardBean> getTSaleTransaction(String stkCode, String saleType, String sourceData, String dataSync) {
        List<STCardBean> listBean = new ArrayList<>();

        try {
            mysqlLocal.open();

            String sql = "select * from t_sale where r_send='N' order by r_refno, r_index;";
            try (java.sql.Statement stmt = mysqlLocal.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    STCardBean bean = new STCardBean();
                    bean.setS_Date(rs.getString("R_Date"));
                    bean.setS_No(rs.getString("R_Refno") + "/" + rs.getString("R_Time"));
                    bean.setS_Que(0);
                    bean.setS_PCode(rs.getString("R_Plucode"));
                    bean.setS_Stk(stkCode);
                    bean.setS_In(0);
                    bean.setS_Out(rs.getInt("R_Quan"));
                    bean.setS_InCost(0);
                    bean.setS_OutCost(rs.getInt("R_Total"));
                    bean.setS_ACost(0);
                    bean.setS_Rem(saleType);
                    bean.setS_User(rs.getString("Cashier"));
                    bean.setS_EntryDate(rs.getString("R_Date"));
                    bean.setR_time(rs.getString("R_time"));
                    bean.setS_Link("");
                    bean.setSource_Data(sourceData);
                    bean.setDataSync(dataSync);

                    double discount = Math.abs(rs.getDouble("R_Nettotal") - rs.getDouble("R_Total"));
                    bean.setDiscount(discount);

                    bean.setNettotal(rs.getDouble("R_Nettotal"));
                    bean.setRefund(rs.getString("R_Refund"));
                    bean.setRefNo(rs.getString("R_Refno"));
                    bean.setCashier(rs.getString("Cashier"));
                    bean.setEmp(rs.getString("R_Emp"));
                    bean.setUnitPrice(rs.getDouble("R_Price"));
                    bean.setR_index(rs.getString("R_Index"));
                    bean.setS_EntryDate(rs.getString("R_Date"));
                    bean.setS_EntryTime(rs.getString("R_Time"));
                    bean.setTableUpdate("t_sale");

                    listBean.add(bean);
                }
            }
        } catch (SQLException e) {
            AppLogUtil.error(getClass(), e.getMessage(), e);
        } finally {
            mysqlLocal.close();
        }

        return listBean;
    }
}
