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

enum EVENT_TYPE {
	ERROR,
	SUCCESSFUL,
	ATTACK,
	GAME_OVER
}

class MoveOutcome {
	EVENT_TYPE eventType;
	int[] attack = new int[3];
	int winningPlayer;
	String errorMessage;
}

class AttackMeGame {	
	
	// Conceptual Game constants
	public static final int PLAYERS = 2;
	public static final int GRIDS   = 3;
	public static final int POINTS  = 8;
	public static final int EMPTY   = 0;

	// Game specific variables/settings and their defaults
	private PHASES currentPhase  = PHASES.PLACE;
	private int[][] board        = new int[GRIDS][POINTS];
	private int totalPawns       = 11;
	private boolean hasXes = false;
	private int currentPlayer = 1;
	private int moves;
	private GAME_STATUS status = GAME_STATUS.NOT_STARTED;
	private int player1Pawns;
	private int player2Pawns;
	private int[] attack = new int[3];
	
	public AttackMeGame(int numberOfPawns, boolean hasXes) {
		// Game defaults
		this.currentPhase = PHASES.PLACE;
		this.board = new int[GRIDS][POINTS];
		this.status = GAME_STATUS.STARTED;
		
		this.totalPawns = numberOfPawns;
		this.hasXes = hasXes;
		this.currentPlayer = 1;
		this.moves = 0;
		this.player1Pawns = this.totalPawns;
		this.player2Pawns = this.totalPawns;
	}

	private void advancePhase() {
		if(currentPhase == PHASES.PLACE) {
			currentPhase = PHASES.MOVE;
			System.out.println("Moving on to MOVE phase.");
		}
		else {
			throw new IllegalArgumentException("No phase to advance further!!!");
		}
	}
	
	private boolean hasMoves(int x, int y) {
		if(board[x][(y+9)%8] == EMPTY || board[x][(y+7)%8] == EMPTY) {
			return true;
		}

		if(y%2 == 0 && hasXes) {
			return (((x==0 || x==2) && (board[1][y] == EMPTY)) ||
					(x==1 && (board[0][y] == EMPTY || board[2][y] == EMPTY)));
		}
		
		return false;
	}
	
	private boolean isStuck(int p) {
		if(currentPhase == PHASES.PLACE)
			return false;
		
		for(int i=0; i<3; i++) {
			for(int j=0; j<8; j++) {
				if(board[i][j] == p && hasMoves(i, j))
					return false;
			}
		}
		
		System.out.println("Player#" + p + " is stuck!");
		return true;
	}
	
	private void switchPlayers() {
		int nextPlayer = currentPlayer % 2 + 1;
		if(currentPhase == PHASES.MOVE && isStuck(nextPlayer)) {
			status = (nextPlayer == 1) ? GAME_STATUS.PLAYER1_STUCK : GAME_STATUS.PLAYER2_STUCK;
		}
		else {
			currentPlayer = nextPlayer;
			moves++;
			if(moves == this.totalPawns*2) {
				status = GAME_STATUS.ADVANCE_PHASE_NEXT_PLAYER;
				advancePhase();
			}
			else {
				status = GAME_STATUS.NEXT_PLAYER;
			}
		}
	}
	
	private boolean isValidPos(int x, int y) {
		return (x >= 0 && x < GRIDS) && (y >= 0 && y< POINTS);
	}
	
	private boolean isPlaceable(int x, int y) {
		if(isValidPos(x, y)) {
			return board[x][y] == EMPTY;
		}
		else {
			throw new IllegalArgumentException("Bad board position given!");
		}
	}
	
	private boolean isInAttack(int posX, int posY) {
		if(isValidPos(posX, posY) && board[posX][posY] != EMPTY) {
			int a = board[posX][posY];
			int b, c;
			attack[0] = posX*8+posY;
			
			if(posY % 2 == 0) {
				attack[1] = ((posX+2)%3)*8+posY;
				attack[2] = ((posX+4)%3)*8+posY;
				b = board[(posX+2)%3][posY];
				c = board[(posX+4)%3][posY];
				
				if(a == b && b == c) {
					System.out.println("(Same Even Y) In attack!!!");
					return true;
				}

				attack[1] = posX*8+((posY+9)%8);
				attack[2] = posX*8+((posY+7)%8);
				b = board[posX][(posY+9)%8];
				c = board[posX][(posY+7)%8];
				if(a == b && b == c) {
					System.out.println("(Even Y, Same X) In attack!!!");
					return true;
				}
			}
			else {
				if(this.hasXes) {
					attack[1] = ((posX+2)%3)*8+posY;
					attack[2] = ((posX+4)%3)*8+posY;
					b = board[(posX+2)%3][posY];
					c = board[(posX+4)%3][posY];
					if(a == b && b == c) {
						System.out.println("(Same Odd Y + hasXes) In attack!!!");
						return true;
					}
				}
				
				attack[1] = posX*8+((posY+6)%8);
				attack[2] = posX*8+((posY+7)%8);
				b = board[posX][(posY+6)%8];
				c = board[posX][(posY+7)%8];
				if(a == b && b == c) {
					System.out.println("(Odd Y, Same X) In attack!!!");
					return true;
				}
				attack[1] = posX*8+((posY+9)%8);
				attack[2] = posX*8+((posY+10)%8);
				b = board[posX][(posY+9)%8];
				c = board[posX][(posY+10)%8];
				if(a == b && b == c) {
					System.out.println("(Odd Y, Same X) In attack!!!");
					return true;
				}
			}
			return false;
		}
		else {
			throw new IllegalArgumentException("Bad input for isInAttack()!");
		}
	}
	
	private int getOpponent(int player) {
		if(player >= 1 && player <= PLAYERS) {
			return (player % PLAYERS) + 1;
		}
		else {
			throw new IllegalArgumentException("Illegal number of players!");
		}
	}
	
	private boolean isPickable(int byPlayer, int posX, int posY) {
		if(isValidPos(posX, posY) && board[posX][posY] == getOpponent(byPlayer)) {
			return (isInAttack(posX, posY) != true);
		}
		else {
			throw new IllegalArgumentException("Bad inputs to isPickable()!");
		}
	}
	
	private GAME_STATUS placePawn(int x, int y) {
		if(status == GAME_STATUS.SHOULD_PICK) {
			throw new IllegalArgumentException("Pick before next one PLA(YS)CE.!");	
		}
		
		if(currentPhase != PHASES.PLACE) {
			throw new IllegalArgumentException("Not in PLACE phase!!!");
		}
		
		if(isPlaceable(x, y) == false) {
			throw new IllegalArgumentException("Board position not empty!!!");
		}
		
		board[x][y] = currentPlayer;
		System.out.println("Player#" + currentPlayer + " placed pick in " + (x*10+y));

		if(isInAttack(x, y)) {
			System.out.println("Successful attack by player#" + currentPlayer);
			status = GAME_STATUS.SHOULD_PICK;
		}
		else {
			switchPlayers();
		}
		
		return status;
	}
	
	private boolean isValidMove(int srcX, int srcY, int destX, int destY) {
		if(isValidPos(srcX, srcY) && isValidPos(destX, destY) && 
				board[srcX][srcY] == currentPlayer && 
				board[destX][destY] == EMPTY) { // (src != dest) is covered
			
			if(srcX == destX) {
				return ((srcY+9)%8 == destY || (srcY+7)%8 == destY);
			}
			else if(srcY == destY && (srcY % 2 == 0 || hasXes)) {
				switch(srcX) {
				case 0:
					return (destX == 1);
				case 1:
					return (destX == 0 || destX == 2);
				case 2:
					return (destX == 1);
				default:
					break;
				}
			}
		}
		throw new IllegalArgumentException("That's a bad bad messed-up move!!!");
	}
		
	private GAME_STATUS makeMove(int srcX, int srcY, int destX, int destY) {
		if(status == GAME_STATUS.SHOULD_PICK) {
			throw new IllegalArgumentException("Pick before MOVE!");	
		}
		
		if(currentPhase != PHASES.MOVE) {
			throw new IllegalArgumentException("Not in MOVE phase!!!");
		}

		if(isValidMove(srcX, srcY, destX, destY)) {
			board[destX][destY] = board[srcX][srcY];
			board[srcX][srcY]= EMPTY;
			System.out.println("Successful move from " + (srcX*10+srcY) + " to " + (destX*10+destY));
			
			if(isInAttack(destX, destY)) {
				System.out.println("Successful attack by player#" + currentPlayer);
				status = GAME_STATUS.SHOULD_PICK;
			}
			else {
				switchPlayers();
			}
		}
		
		return status;
	}
	
	private GAME_STATUS pickOpponent(int x, int y) {
		if(isPickable(currentPlayer, x, y)) {
			board[x][y] = EMPTY;
			System.out.println("Player#" + currentPlayer + " picked opponent's pick from " + (x*10+y));
			
			int opponent = (currentPlayer % 2 + 1);
			if(opponent == 1) {
				player1Pawns--;
				if(player1Pawns == 0) {
					status = GAME_STATUS.PLAYER2_WON;
				}
			}
			else {
				player2Pawns--;
				if(player2Pawns == 0) {
					status = GAME_STATUS.PLAYER1_WON;
				}
			}
			
			switchPlayers();
		}
		else {
			throw new IllegalArgumentException("Wrong pick!!!");
		}
		
		return status;
	}
	
	private MoveOutcome getOutcome(GAME_STATUS status) {
		MoveOutcome result = new MoveOutcome();

		switch(status) {
		case NEXT_PLAYER:
		case ADVANCE_PHASE_NEXT_PLAYER:
			result.eventType = EVENT_TYPE.SUCCESSFUL;
			break;
		case SHOULD_PICK:
			result.eventType = EVENT_TYPE.ATTACK;
			result.attack = this.attack;
			break;
		case PLAYER1_WON: 
		case PLAYER2_STUCK:
			result.eventType = EVENT_TYPE.GAME_OVER;
			result.winningPlayer = currentPlayer;
			break;
		case PLAYER2_WON: 
		case PLAYER1_STUCK:
			result.eventType = EVENT_TYPE.GAME_OVER;
			result.winningPlayer = currentPlayer;
			break;
		default:
			result.eventType = EVENT_TYPE.ERROR;
			break;
		}
		
		return result;
	}
	
	public MoveOutcome movePawn(int src, int dest) {
		MoveOutcome result = new MoveOutcome();
		
		try {
			if(src < 24) {
				if(currentPhase == PHASES.PLACE) {
					throw new Exception("Move during PLACE phase detected!!!");
				}

				GAME_STATUS status = makeMove(src/8, src%8, dest/8, dest%8);
				result = getOutcome(status);
			}
			else {
				if(currentPhase == PHASES.MOVE) {
					throw new Exception("Place during MOVE phase detected!!!");
				}

				GAME_STATUS status = placePawn(dest/8, dest%8);
				result = getOutcome(status);
			}
		}
		catch(Exception e) {
			result.eventType = EVENT_TYPE.ERROR;
			result.errorMessage = e.getMessage();
		}
		return result;
	}

	public MoveOutcome removePawn(int loc) {
		MoveOutcome result = new MoveOutcome();
		
		try {
			GAME_STATUS status = pickOpponent(loc/8, loc%8);

			if(status == GAME_STATUS.SHOULD_PICK) {
				throw new Exception("Impossible state detected!!!");
			}
			else {
				result = getOutcome(status);
			}
		}
		catch(Exception e) {
			result.eventType = EVENT_TYPE.ERROR;
			result.errorMessage = e.getMessage();
		}
		
		return result;
	}
}

public class AttackMe {
	static AttackMeGame game;
	
	public static AttackMeGame startNewGame(int numberOfPawns, boolean hasXes) {
		game = new AttackMeGame(11, false);
		
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

				if(outcome.eventType == EVENT_TYPE.ATTACK && i<moves.length) {
					src = i++;
					outcome = game.removePawn((moves[src]/10)*8+moves[src]%10); 
				}
			}
			else {
				if(i < moves.length - 1) {
					src = i++; dest = i++;
					outcome = game.movePawn((moves[src]/10)*8+moves[src]%10, (moves[dest]/10)*8+moves[dest]%10);
					
					if(outcome.eventType == EVENT_TYPE.ATTACK) {
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
