package com.rm;

public class MoveOutcome {

		EventType eventType;
		public EventType getEventType() {
			return eventType;
		}
		public void setEventType(EventType eventType) {
			this.eventType = eventType;
		}
		public int[] getAttack() {
			return attack;
		}
		public void setAttack(int[] attack) {
			this.attack = attack;
		}
		public int getWinningPlayer() {
			return winningPlayer;
		}
		public void setWinningPlayer(int winningPlayer) {
			this.winningPlayer = winningPlayer;
		}
		public String getErrorMessage() {
			return errorMessage;
		}
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
		int[] attack = new int[3];
		int winningPlayer;
		String errorMessage;

}
