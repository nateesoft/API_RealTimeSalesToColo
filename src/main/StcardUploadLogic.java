package main;

import com.ics.bean.STCardBean;

/**
 * Business logic for computing STCard upload parameters.
 * Extracted from Api_RealTimeSalesToColoServer.uploadStcard() for testability.
 */
class StcardUploadLogic {

    static final String S_REM_SAL = "SAL";

    @FunctionalInterface
    interface MatchDiscountStrategy {
        STCardBean match(String s_No, String s_Date, String s_PCode,
                String checkFirstDigit, STCardBean bean);
    }

    static class ComputedParams {
        final double discount;
        final double nettotal;
        final String refund;
        final String refno;
        final String cashier;
        final String emp;
        final double unitPrice;
        final boolean isSalType;
        final boolean shouldSkip;

        ComputedParams(double discount, double nettotal, String refund, String refno,
                String cashier, String emp, double unitPrice,
                boolean isSalType, boolean shouldSkip) {
            this.discount = discount;
            this.nettotal = nettotal;
            this.refund = refund;
            this.refno = refno;
            this.cashier = cashier;
            this.emp = emp;
            this.unitPrice = unitPrice;
            this.isSalType = isSalType;
            this.shouldSkip = shouldSkip;
        }
    }

    /**
     * Computes upload parameters for a single STCardBean.
     *
     * @param bean             the STCard record to process
     * @param matchDiscountFn  strategy to resolve discount/nettotal for SAL R/0 types
     * @return computed parameters including a shouldSkip flag
     */
    static ComputedParams computeParams(STCardBean bean, MatchDiscountStrategy matchDiscountFn) {
        double discount = 0;
        double nettotal = 0;
        String refund = "";
        String refno = "";
        String cashier = "";
        String emp = "";

        String checkFirstDigitSNo = bean.getS_No().substring(0, 1);

        if (bean.getS_Rem().equals(S_REM_SAL)) {
            if (checkFirstDigitSNo.equals("E")) {
                nettotal = bean.getS_OutCost();
                refund = "-";
                refno = bean.getS_No();
                cashier = bean.getEmp();
                emp = cashier;
            }
            if (checkFirstDigitSNo.equals("R") || checkFirstDigitSNo.equals("0")) {
                STCardBean matched = matchDiscountFn.match(
                        bean.getS_No(), bean.getS_Date(), bean.getS_PCode(), checkFirstDigitSNo, bean);
                if(matched == null){
                    matched = bean;
                }
                
                discount = matched.getDiscount();
                nettotal = matched.getNettotal();
                refund = matched.getRefund();
                refno = matched.getRefNo();
                cashier = matched.getCashier();
                emp = matched.getEmp();
            }
        } else {
            if (bean.getS_In() != 0) {
                nettotal = bean.getS_InCost();
            } else if (bean.getS_OutCost() != 0) {
                nettotal = bean.getS_OutCost();
            }
            refund = "-";
            cashier = bean.getCashier();
            emp = cashier;
        }

        boolean isSalType = bean.getS_Rem().equals(S_REM_SAL)
                && (checkFirstDigitSNo.equals("E") || refno.equals(""));

        if (nettotal == 0 && bean.getS_Rem().equals(S_REM_SAL) && bean.getS_Out() != 0) {
            bean.setNettotal(totalCompareNettotal(bean));
            nettotal = bean.getNettotal();
        }

        double unitPrice = 0;
        if (bean.getS_In() != 0) {
            unitPrice = bean.getS_InCost() / bean.getS_In();
        }
        if (bean.getS_Out() != 0) {
            unitPrice = bean.getS_OutCost() / bean.getS_Out();
        }

        boolean shouldSkip = isSalType && bean.getRefNo().equals("");

        return new ComputedParams(discount, nettotal, refund, refno,
                cashier, emp, unitPrice, isSalType, shouldSkip);
    }

    /**
     * Fallback nettotal for SAL-type beans with zero nettotal and non-zero OutCost.
     */
    static double totalCompareNettotal(STCardBean bean) {
        if (bean.getS_OutCost() != 0 && bean.getS_Rem().equals(S_REM_SAL) && bean.getNettotal() == 0) {
            bean.setNettotal(bean.getS_OutCost());
        }
        return bean.getNettotal();
    }
}
