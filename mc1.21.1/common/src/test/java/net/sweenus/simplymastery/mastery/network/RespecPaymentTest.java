package net.sweenus.simplymastery.mastery.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class RespecPaymentTest {

    @Test
    void missingPaymentDoesNotPartiallyConsumeInventory() {
        assertNull(UnlockService.paymentPlan(new int[]{2, 0}, 3));
    }

    @Test
    void completePaymentConsumesAcrossSlots() {
        assertArrayEquals(new int[]{2, 1}, UnlockService.paymentPlan(new int[]{2, 2}, 3));
    }
}
