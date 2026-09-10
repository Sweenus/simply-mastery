package net.sweenus.simplyswordsmastery.client.mastery;

import net.sweenus.simplyswordsmastery.mastery.network.UnlockResult;

public final class MasteryClientFeedback {

    private static UnlockResult result;
    private static long actionId = -1L;
    private static long revision;

    private MasteryClientFeedback() {
    }

    public static void accept(UnlockResult newResult, long newActionId, long newRevision) {
        result = newResult;
        actionId = newActionId;
        revision = newRevision;
    }

    public static UnlockResult result() {
        return result;
    }

    public static long actionId() {
        return actionId;
    }

    public static long revision() {
        return revision;
    }
}
