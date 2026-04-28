package server.flow;

import game.enumtype.GamePhase;

public record PlayerInput(int playerId, String message, GamePhase phase) {
}
