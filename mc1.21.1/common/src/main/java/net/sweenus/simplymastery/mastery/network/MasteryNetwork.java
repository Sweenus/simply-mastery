package net.sweenus.simplymastery.mastery.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import net.sweenus.simplymastery.SimplyMastery;

public final class MasteryNetwork {

    public static final int PROTOCOL_VERSION = 1;
    public static final SimpleNetworkManager NETWORK = SimpleNetworkManager.create(SimplyMastery.MOD_ID);
    public static final MessageType UNLOCK_NODE = NETWORK.registerC2S("unlock_node", UnlockNodePacket::new);
    public static final MessageType UNLOCK_RESULT = NETWORK.registerS2C("unlock_result", UnlockResultPacket::new);

    private MasteryNetwork() {
    }

    public static void init() {
    }
}
