package main;

import com.ics.bean.STCardBean;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for StcardUploadLogic — covers the per-item computation logic
 * that was extracted from Api_RealTimeSalesToColoServer.uploadStcard().
 */
public class StcardUploadLogicTest {

    // =========================================================================
    // totalCompareNettotal
    // =========================================================================

    @Test
    public void totalCompareNettotal_salZeroNettotalNonZeroOutCost_returnsOutCost() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_OutCost(150.0);
        bean.setNettotal(0);

        double result = StcardUploadLogic.totalCompareNettotal(bean);

        assertEquals(150.0, result, 0.001);
    }

    @Test
    public void totalCompareNettotal_salNonZeroNettotal_keepsExistingNettotal() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_OutCost(150.0);
        bean.setNettotal(90.0);

        double result = StcardUploadLogic.totalCompareNettotal(bean);

        assertEquals(90.0, result, 0.001);
    }

    @Test
    public void totalCompareNettotal_nonSalType_returnsZero() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("PUR");
        bean.setS_OutCost(150.0);
        bean.setNettotal(0);

        double result = StcardUploadLogic.totalCompareNettotal(bean);

        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void totalCompareNettotal_salZeroOutCost_returnsZero() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_OutCost(0);
        bean.setNettotal(0);

        double result = StcardUploadLogic.totalCompareNettotal(bean);

        assertEquals(0.0, result, 0.001);
    }

    // =========================================================================
    // computeParams — SAL + prefix "E"
    // =========================================================================

    @Test
    public void computeParams_salEPrefix_nettotalFromOutCost() {
        STCardBean bean = salBean("E001-123456", 200.0, "cashier1", "emp1");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals(200.0, p.nettotal, 0.001);
    }

    @Test
    public void computeParams_salEPrefix_refundAndRefnoAndEmp() {
        STCardBean bean = salBean("E001-123456", 200.0, "cashier1", "emp1");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals("-", p.refund);
        assertEquals("E001-123456", p.refno);
        assertEquals("emp1", p.cashier);
        assertEquals("emp1", p.emp);
        assertEquals(0.0, p.discount, 0.001);
    }

    @Test
    public void computeParams_salEPrefix_isSalTypeTrue() {
        STCardBean bean = salBean("E001-123456", 200.0, "cashier1", "emp1");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertTrue(p.isSalType);
    }

    @Test
    public void computeParams_salEPrefix_beanRefNoEmpty_shouldSkipTrue() {
        STCardBean bean = salBean("E001-123456", 200.0, "cashier1", "emp1");
        // bean.getRefNo() defaults to ""

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertTrue(p.shouldSkip);
    }

    @Test
    public void computeParams_salEPrefix_beanRefNoSet_shouldSkipFalse() {
        STCardBean bean = salBean("E001-123456", 200.0, "cashier1", "emp1");
        bean.setRefNo("REF001");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertFalse(p.shouldSkip);
    }

    // =========================================================================
    // computeParams — SAL + prefix "R"
    // =========================================================================

    @Test
    public void computeParams_salRPrefix_delegatesToMatchDiscount() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_No("R001/REF001");
        bean.setS_Date("2026-01-01");
        bean.setS_PCode("P001");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, matchReturning(
                10.0, 90.0, "R", "REF001", "cashier1", "emp1"));

        assertEquals(10.0, p.discount, 0.001);
        assertEquals(90.0, p.nettotal, 0.001);
        assertEquals("R", p.refund);
        assertEquals("REF001", p.refno);
        assertEquals("cashier1", p.cashier);
        assertEquals("emp1", p.emp);
    }

    @Test
    public void computeParams_salRPrefix_withNonEmptyRefno_isSalTypeFalse() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_No("R001/REF001");
        bean.setS_Date("2026-01-01");
        bean.setS_PCode("P001");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, matchReturning(
                0, 0, "", "REF001", "", ""));

        assertFalse(p.isSalType);
        assertFalse(p.shouldSkip);
    }

    @Test
    public void computeParams_salRPrefix_emptyRefnoFromMatch_isSalTypeTrue() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_No("R001/REF001");
        bean.setS_Date("2026-01-01");
        bean.setS_PCode("P001");

        // matchDiscount returns empty refNo → isSalType = true
        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertTrue(p.isSalType);
    }

    // =========================================================================
    // computeParams — SAL + prefix "0"
    // =========================================================================

    @Test
    public void computeParams_salZeroPrefix_delegatesToMatchDiscount() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_No("001-124500");
        bean.setS_Date("2026-01-01");
        bean.setS_PCode("P002");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, matchReturning(
                5.0, 95.0, "-", "REF002", "cashier2", "emp2"));

        assertEquals(5.0, p.discount, 0.001);
        assertEquals(95.0, p.nettotal, 0.001);
        assertEquals("REF002", p.refno);
        assertFalse(p.isSalType);
    }

    // =========================================================================
    // computeParams — Non-SAL types
    // =========================================================================

    @Test
    public void computeParams_nonSalWithSIn_nettotalFromInCost() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("PUR");
        bean.setS_No("P001");
        bean.setS_In(5.0);
        bean.setS_InCost(500.0);
        bean.setCashier("cashier1");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals(500.0, p.nettotal, 0.001);
        assertEquals("-", p.refund);
        assertEquals("cashier1", p.cashier);
        assertEquals("cashier1", p.emp);
        assertFalse(p.isSalType);
        assertFalse(p.shouldSkip);
    }

    @Test
    public void computeParams_nonSalNoSInWithOutCost_nettotalFromOutCost() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("TRF");
        bean.setS_No("T001");
        bean.setS_In(0);
        bean.setS_OutCost(300.0);
        bean.setCashier("cashier2");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals(300.0, p.nettotal, 0.001);
        assertEquals("-", p.refund);
        assertEquals("cashier2", p.emp);
        assertFalse(p.isSalType);
    }

    @Test
    public void computeParams_nonSalBothZero_nettotalZero() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("ADJ");
        bean.setS_No("A001");
        bean.setS_In(0);
        bean.setS_OutCost(0);
        bean.setCashier("cashier3");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals(0.0, p.nettotal, 0.001);
    }

    // =========================================================================
    // computeParams — unit price calculation
    // =========================================================================

    @Test
    public void computeParams_unitPriceFromSIn_whenNoSOut() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("PUR");
        bean.setS_No("P001");
        bean.setS_In(4.0);
        bean.setS_InCost(200.0);
        bean.setS_Out(0);

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals(50.0, p.unitPrice, 0.001);
    }

    @Test
    public void computeParams_unitPriceFromSOut_overridesSIn() {
        // When both S_In and S_Out are non-zero, S_Out is checked last → wins
        STCardBean bean = new STCardBean();
        bean.setS_Rem("PUR");
        bean.setS_No("P001");
        bean.setS_In(4.0);
        bean.setS_InCost(200.0);
        bean.setS_Out(5.0);
        bean.setS_OutCost(500.0);

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals(100.0, p.unitPrice, 0.001);
    }

    @Test
    public void computeParams_unitPriceZero_whenBothSInAndSOutAreZero() {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("ADJ");
        bean.setS_No("A001");
        bean.setS_In(0);
        bean.setS_Out(0);

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals(0.0, p.unitPrice, 0.001);
    }

    // =========================================================================
    // computeParams — SAL nettotal=0 fallback via totalCompareNettotal
    // =========================================================================

    @Test
    public void computeParams_salEPrefixZeroOutCostNonZeroSOut_fallbackStillZero() {
        // S_OutCost=0 → "E" block sets nettotal=0
        // nettotal==0 && SAL && S_Out!=0 → calls totalCompareNettotal
        // totalCompareNettotal: S_OutCost==0 → returns 0
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_No("E001-123456");
        bean.setS_OutCost(0);
        bean.setS_Out(3.0);
        bean.setEmp("emp1");

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean, noMatch());

        assertEquals(0.0, p.nettotal, 0.001);
    }

    @Test
    public void computeParams_salRPrefixMatchReturnsZeroNettotalNonZeroSOut_fallbackFromOutCost() {
        // matchDiscount returns nettotal=0, but bean has S_Out!=0 and S_OutCost=200
        // → totalCompareNettotal kicks in and returns 200
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_No("R001/REF001");
        bean.setS_Date("2026-01-01");
        bean.setS_PCode("P001");
        bean.setS_Out(2.0);
        bean.setS_OutCost(200.0);

        StcardUploadLogic.ComputedParams p = StcardUploadLogic.computeParams(bean,
                matchReturning(0, 0, "", "REF001", "", ""));

        assertEquals(200.0, p.nettotal, 0.001);
    }

    // =========================================================================
    // helpers
    // =========================================================================

    private STCardBean salBean(String sNo, double outCost, String cashier, String emp) {
        STCardBean bean = new STCardBean();
        bean.setS_Rem("SAL");
        bean.setS_No(sNo);
        bean.setS_OutCost(outCost);
        bean.setCashier(cashier);
        bean.setEmp(emp);
        return bean;
    }

    /** Strategy that returns a blank STCardBean (simulates no-match / DB not needed). */
    private StcardUploadLogic.MatchDiscountStrategy noMatch() {
        return (s_No, s_Date, s_PCode, firstDigit, bean) -> new STCardBean();
    }

    /** Strategy that returns a pre-filled STCardBean. */
    private StcardUploadLogic.MatchDiscountStrategy matchReturning(
            double discount, double nettotal, String refund,
            String refNo, String cashier, String emp) {
        return (s_No, s_Date, s_PCode, firstDigit, bean) -> {
            STCardBean result = new STCardBean();
            result.setDiscount(discount);
            result.setNettotal(nettotal);
            result.setRefund(refund);
            result.setRefNo(refNo);
            result.setCashier(cashier);
            result.setEmp(emp);
            return result;
        };
    }
}
