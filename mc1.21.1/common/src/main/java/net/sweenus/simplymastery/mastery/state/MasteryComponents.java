package net.sweenus.simplymastery.mastery.state;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Uuids;
import net.sweenus.simplymastery.SimplyMastery;

import java.util.UUID;

public final class MasteryComponents {

    private static final DeferredRegister<ComponentType<?>> TYPES =
            DeferredRegister.create(SimplyMastery.MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);
    public static final RegistrySupplier<ComponentType<UUID>> WEAPON_ID = TYPES.register(
            "weapon_id", () -> ComponentType.<UUID>builder()
                    .codec(Uuids.CODEC)
                    .packetCodec(Uuids.PACKET_CODEC).build());
    public static final RegistrySupplier<ComponentType<MasteryState>> MASTERY_STATE = TYPES.register(
            "mastery_state",
            () -> ComponentType.<MasteryState>builder()
                    .codec(MasteryState.CODEC)
                    .packetCodec(MasteryState.PACKET_CODEC)
                    .build()
    );
    public static final RegistrySupplier<ComponentType<MasteryPortfolio>> MASTERY_PORTFOLIO = TYPES.register(
            "mastery_portfolio",
            () -> ComponentType.<MasteryPortfolio>builder()
                    .codec(MasteryPortfolio.CODEC)
                    .packetCodec(MasteryPortfolio.PACKET_CODEC)
                    .build()
    );
    public static final RegistrySupplier<ComponentType<MasteryCooldownState>> MASTERY_COOLDOWNS = TYPES.register(
            "mastery_cooldowns",
            () -> ComponentType.<MasteryCooldownState>builder()
                    .codec(MasteryCooldownState.CODEC)
                    .packetCodec(MasteryCooldownState.PACKET_CODEC)
                    .build()
    );
    public static final RegistrySupplier<ComponentType<MasteryRuntimeState>> MASTERY_RUNTIME = TYPES.register(
            "mastery_runtime",
            () -> ComponentType.<MasteryRuntimeState>builder()
                    .codec(MasteryRuntimeState.CODEC)
                    .packetCodec(MasteryRuntimeState.PACKET_CODEC)
                    .build()
    );

    private MasteryComponents() {
    }

    public static void register() {
        TYPES.register();
    }
}
