package net.sweenus.simplymastery.mastery.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.state.MasteryPortfolio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormSwitchPacketRevisionTest {

    @Test
    void packetFromEarlierFormRevisionIsRejectedAfterPortfolioMutation() {
        MasteryProfile base = BuiltInFamilyProfiles.profile("wraithfang");
        MasteryProfile evolved = BuiltInFamilyProfiles.profile("wraithmaw");
        MasteryPortfolio portfolio = MasteryPortfolio.initial(base.progressionGroupId(), 9)
                .reconcile(base, 9).reconcile(evolved, 9);
        UnlockNodePacket sent = new UnlockNodePacket(4, 7, portfolio.mutationRevision(), base.id(),
                base.nodes().getFirst().id(), 1L);
        RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), DynamicRegistryManager.EMPTY);
        UnlockNodePacket decoded;
        try {
            sent.write(buffer);
            decoded = new UnlockNodePacket(buffer);
        } finally {
            buffer.release();
        }
        portfolio = portfolio.withState(evolved,
                portfolio.activeView(evolved, 9).unlock(evolved.nodes().getFirst().id()), 9);
        assertEquals(UnlockResult.STALE_STATE, UnlockRules.validate(base, portfolio.activeView(base, 9),
                base.nodes().getFirst(), decoded.expectedMutationRevision()));
    }
}
