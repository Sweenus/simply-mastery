package net.sweenus.simplyswordsmastery.client.mastery.ui;

import net.minecraft.text.Text;

public record MasteryPrompt(Text title, Text message, Runnable onConfirm) {
}
