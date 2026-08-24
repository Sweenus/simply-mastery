package net.sweenus.simplymastery.mastery.state;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.RegistryKeys;
import net.sweenus.simplymastery.SimplyMastery;

public final class MasteryComponents {

    private static final DeferredRegister<ComponentType<?>> TYPES =
            DeferredRegister.create(SimplyMastery.MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);
    public static final RegistrySupplier<ComponentType<MasteryState>> MASTERY_STATE = TYPES.register(
            "mastery_state",
            () -> ComponentType.<MasteryState>builder()
                    .codec(MasteryState.CODEC)
                    .packetCodec(MasteryState.PACKET_CODEC)
                    .build()
    );
    public static final RegistrySupplier<ComponentType<MasteryCooldownState>> MASTERY_COOLDOWNS = TYPES.register(
            "mastery_cooldowns",
            () -> ComponentType.<MasteryCooldownState>builder()
                    .codec(MasteryCooldownState.CODEC)
                    .packetCodec(MasteryCooldownState.PACKET_CODEC)
                    .build()
    );

    private MasteryComponents() {
    }

    public static void register() {
        TYPES.register();
    }
}
