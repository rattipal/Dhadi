package com.rm;

enum PHASES {
	PLACE, MOVE;
}

enum GAME_STATUS {
	NOT_STARTED,
	STARTED,
	NEXT_PLAYER, 
	SHOULD_PICK,
	PLAYER1_WON, 
	PLAYER2_STUCK, 
	PLAYER2_WON, 
	PLAYER1_STUCK, 
	ADVANCE_PHASE_NEXT_PLAYER;
}

public class AttackMe {
	static AttackMeGame game;

	public static AttackMeGame startNewGame(int numberOfPawns, boolean hasXes) {
		game = new AttackMeGame(numberOfPawns, hasXes);
		return game;
	}

	public static void main(String[] args) throws Exception {	
		int[] moves = {15, 22, 12, 13, 06, 16, 0, 7, 10, 20, 24, 21, 23, 27, 
				24, 24, 25, 26, 1, 4, 14, 3, 11, 6, 5, 7, 16, 6, 15, 16, 14, 
				15, 4, 14, 1, 2, 14, 4, 6, 15, 14, 16, 15, 2, 1, 3, 2};
		startNewGame(11, false);
		// TODO: Detect when one of the players is stuck (#2 here)
		int i = 0, src, dest, currentPlayer = 1, moveNo = 0;
		MoveOutcome outcome;

		//		if(status != GAME_STATUS.STARTED) {
		//			throw new Exception("Unable to initialize the game!!!");
		//		}

		while(i < moves.length) {
			if(moveNo < 22) {
				dest = i++;
				outcome = game.movePawn(24, (moves[dest]/10)*8+moves[dest]%10); 

				if(outcome.eventType == EventType.ATTACK_DETECTED && i<moves.length) {
					src = i++;
					outcome = game.removePawn((moves[src]/10)*8+moves[src]%10); 
				}
			}
			else {
				if(i < moves.length - 1) {
					src = i++; dest = i++;
					outcome = game.movePawn((moves[src]/10)*8+moves[src]%10, (moves[dest]/10)*8+moves[dest]%10);

					if(outcome.eventType == EventType.ATTACK_DETECTED) {
						System.out.println("Following positions are in ATTACK: " 
								+ outcome.attack[0] + ", " 
								+ outcome.attack[1] + ", " 
								+ outcome.attack[2]);
						if(i<moves.length) {
							src = i++;
							outcome = game.removePawn((moves[src]/10)*8+moves[src]%10);
						}
						else {
							throw new IllegalArgumentException("Not enough moves!!!");
						}
					}
				}
				else {
					throw new IllegalArgumentException("Not enough moves!!!");
				}
			}

			switch(outcome.eventType) {
			case SUCCESSFUL:
				currentPlayer = (currentPlayer % 2) + 1;
				break;
			case GAME_OVER:
				System.out.println("Player#" + outcome.winningPlayer + " won. Congratulations!");
				if(i<moves.length) {
					System.out.println("Excess moves specified, though!!!");
				}
				return;
			case ERROR:
				System.out.println("FATAL: " + outcome.errorMessage);
				return;
			default:
				throw new Exception("Unknown status!!!");
			}
			moveNo++;
		}

		System.out.println("Not enough moves given. Game is adandoned in the middle.");
	}
}
