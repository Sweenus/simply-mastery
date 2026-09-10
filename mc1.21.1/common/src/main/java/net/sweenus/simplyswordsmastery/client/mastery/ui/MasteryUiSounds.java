package net.sweenus.simplyswordsmastery.client.mastery.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;

public final class MasteryUiSounds {

    private MasteryUiSounds() {
    }

    private static void play(SoundEvent event, float pitch, float volume) {
        float scaled = volume * MasteryConfig.CLIENT.uiSoundVolume;
        if (scaled <= 0.001F) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(event, pitch, scaled));
        }
    }

    private static void play(RegistryEntry<SoundEvent> event, float pitch, float volume) {
        play(event.value(), pitch, volume);
    }

    public static void hover() {
        play(SoundEvents.UI_BUTTON_CLICK, 1.9F, 0.16F);
    }

    public static void select() {
        play(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, 1.5F, 0.4F);
    }

    public static void unlock() {
        play(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.2F, 0.6F);
        play(SoundEvents.BLOCK_BEACON_ACTIVATE, 1.7F, 0.22F);
    }

    public static void capstone() {
        play(SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.4F, 0.4F);
        play(SoundEvents.ITEM_TRIDENT_THUNDER, 1.6F, 0.28F);
    }

    public static void denied() {
        play(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 0.7F, 0.35F);
    }

    public static void openView() {
        play(SoundEvents.BLOCK_BEACON_ACTIVATE, 2.0F, 0.18F);
    }
}
