package ai.datagym.application.labelTask.service;

import ai.datagym.application.labelTask.entity.LabelTaskState;

import java.util.List;
import java.util.Map;

public final class LabelTaskStateMachine {

    /*
     * The 'state machine' with current state and a list of all possible target states.
     *
     * E.g. LabelTaskState.BACKLOG can be moved to LabelTaskState.WAITING
     */
    private static final Map<LabelTaskState, List<LabelTaskState>> supportedDirections = Map.of(
            LabelTaskState.BACKLOG, List.of(LabelTaskState.WAITING),
            LabelTaskState.WAITING, List.of(LabelTaskState.BACKLOG, LabelTaskState.IN_PROGRESS),
            LabelTaskState.IN_PROGRESS, List.of(LabelTaskState.WAITING_CHANGED, LabelTaskState.SKIPPED, LabelTaskState.COMPLETED),
            LabelTaskState.COMPLETED, List.of(LabelTaskState.WAITING_CHANGED, LabelTaskState.REVIEWED),
            LabelTaskState.REVIEWED, List.of(LabelTaskState.WAITING_CHANGED),
            LabelTaskState.SKIPPED, List.of(LabelTaskState.WAITING_CHANGED, LabelTaskState.REVIEWED_SKIP),
            LabelTaskState.REVIEWED_SKIP, List.of(LabelTaskState.WAITING_CHANGED),
            LabelTaskState.WAITING_CHANGED, List.of(LabelTaskState.BACKLOG, LabelTaskState.IN_PROGRESS)
    );

    private LabelTaskStateMachine() {}

    public static boolean isStateChangePossible(LabelTaskState currentState, LabelTaskState targetState) {

        if (currentState == null || targetState == null) {
            return false;
        }

        if (currentState == targetState) {
            return true;
        }

        if (!supportedDirections.containsKey(currentState)) {
            return false;
        }

        List<LabelTaskState> possibleTargets = supportedDirections.get(currentState);

        return possibleTargets.contains(targetState);
    }

    /**
     * Some states require admin privileges.
     */
    public static boolean requiresAdmin(LabelTaskState targetState) {
        List<LabelTaskState> requiresAdmin = List.of(
                LabelTaskState.BACKLOG,
                LabelTaskState.WAITING,
                LabelTaskState.WAITING_CHANGED,
                LabelTaskState.REVIEWED,
                LabelTaskState.REVIEWED_SKIP
        );

        return requiresAdmin.contains(targetState);
    }
}
