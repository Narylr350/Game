package rule.landlord;

import game.enumtype.GamePhase;
import game.flow.GameFlow;
import game.model.GameRoom;
import game.state.LandlordState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandlordRuleCheckerTest {

    @Test
    void should_redeal_when_three_players_pass_call_landlord() {
        LandlordState state = new LandlordState();
        state.incrementCallPassCount();
        state.incrementCallPassCount();
        state.incrementCallPassCount();

        assertTrue(LandlordRuleChecker.shouldRedealAfterCallPass(state));
    }

    @Test
    void should_not_redeal_when_less_than_three_players_pass_call_landlord() {
        LandlordState state = new LandlordState();
        state.incrementCallPassCount();
        state.incrementCallPassCount();

        assertFalse(LandlordRuleChecker.shouldRedealAfterCallPass(state));
    }

    @Test
    void should_force_rob_pass_for_player_who_passed_call_landlord() {
        LandlordState state = new LandlordState();
        state.addCallPassPlayerId(2);

        assertTrue(LandlordRuleChecker.shouldForceRobPass(state, 2));
        assertFalse(LandlordRuleChecker.shouldForceRobPass(state, 3));
    }

    @Test
    void should_detect_when_rob_flow_returns_to_first_caller() {
        LandlordState state = new LandlordState();
        state.setFirstCallerId(1);

        assertTrue(LandlordRuleChecker.hasReturnedToFirstCaller(state, 1));
        assertFalse(LandlordRuleChecker.hasReturnedToFirstCaller(state, 2));
    }

    @Test
    void should_detect_when_next_player_is_first_caller_and_still_candidate() {
        LandlordState state = new LandlordState();
        state.setFirstCallerId(1);
        state.setLandlordCandidateId(1);

        assertTrue(LandlordRuleChecker.nextIsFirstCaller(state, 1));
        assertTrue(LandlordRuleChecker.firstCallerIsCurrentCandidate(state));
    }

    @Test
    void should_reject_landlord_operation_after_landlord_has_been_decided() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        room.setCurrentPhase(GamePhase.ROB_LANDLORD);
        room.setLandlordPlayerId(1);

        assertTrue(LandlordRuleChecker.validateCanCallLandlord(room)
                == LandlordCheckResult.LANDLORD_ALREADY_DECIDED);
    }
}
