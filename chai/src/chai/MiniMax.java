//Author: James Jia

package chai;

import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;

public class MiniMax implements ChessAI {
	
	public static int WORSTCASE = -Integer.MAX_VALUE;
	public static int BESTCASE = Integer.MAX_VALUE;
	public int maxD;
	public int color;
	public short bestMove;
	public int nodes;

	
	//tell the AI what color it is - color will become its color (1 for black, 0 for white)
	public MiniMax(int col, int depth){
		color = col;
		maxD = depth;
		nodes = 0;
		bestMove = 0;
	}
	
	//returns the actual move by calling idmm
	public short getMove(Position pos) {

		Position position = new Position(pos);
		short move = idmm(position);
		System.out.println("Minimax nodes searched: " + nodes);
		return move;
	}
	
	public short idmm(Position pos) {
		
		//This loop will run minimax search for depth 1 up to depth maxD
		for (int i = maxD-1; i >= 0; i--) {
			
			//save results from each loop in bestMove instance variable
			bestMove = miniMax(pos,i);
			
			//if found checkmate, no need to go deeper
			try{
				pos.doMove(bestMove);
				if(utility(pos)==BESTCASE){
					return bestMove;
				}
				pos.undoMove();
			}
			catch (IllegalMoveException e){
			}
		}
		return bestMove;
	}
	
	public short miniMax(Position pos, int startDepth) {

		//initialize variables for minimax, assuming maximizer
		short bestMove = 0;
		int bestValue = WORSTCASE;
		int currentValue;

		//get set of possible moves and try each one out
		for (short move : pos.getAllMoves()) {

			try {
				pos.doMove(move);
				currentValue = mini(pos, 0);

				//if value is better, then update bestValue
				if (currentValue > bestValue) {
					bestValue = currentValue;
					bestMove = move;
				}

				//undo move to try more moves
				pos.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		return bestMove;
	}
	
	//returns value of min player's move choice
	public int mini(Position pos, int depth) {
		
		nodes++;
		int currentDepth = depth+1;
		
		//Base case: pass cutoff
		if (cutOff(pos, currentDepth)){
			return utility(pos);
		}
		//initially, minimizer's bestValue is +infinity
		int bestValue = BESTCASE;
		
		//get moves and try each one out
		for (short move : pos.getAllMoves()) {
			try {
				pos.doMove(move);
				//if move is better, then bestValue is updated
				bestValue = Math.min(bestValue, max(pos, currentDepth));
				pos.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		return bestValue;
	}
	
	//returns value of max player's move choice
	public int max(Position pos, int depth) {

		nodes++;
		int currentDepth = depth+1;
		
		//Base Case: Pass Cutoff
		if (cutOff(pos, currentDepth)){
			return utility(pos);
		}
		
		int bestValue = WORSTCASE;
		for (short move : pos.getAllMoves()) {
			try {
				pos.doMove(move);
				bestValue = Math.max(bestValue, mini(pos, currentDepth));
				pos.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		return bestValue;
	}
	
	
	//utility function
	public int utility(Position pos) {
		
		int value = 0;

		//checkmate
		if(pos.isMate()){
			
			//ai checkmated opponent
			if (pos.getToPlay() != color) {
				value = BESTCASE;
			}
			//ai got checkmated
			if (pos.getToPlay() == color) {
				value = WORSTCASE;
			}
		}
		
		else if (pos.isStaleMate())
			value = 0;
		
		else {
			value = evaluate(pos);
		}
		return value;
	}
	
	
	//evaluate non terminal position
	public int evaluate(Position position) {
		
		int value = 0;
		//If the turn is the AI's, then the getMaterial score is correct
		if (position.getToPlay()==color) {
			
			//getMaterial gets material score
			//getDomination weighs occupation of center squares more, not as important as material score
			value += position.getMaterial();
			value += 0.5*position.getDomination();
			
		//If the turn is the other person's, then negate score to get AI's score
		}
		if(position.getToPlay()!=color) {
			value -= position.getMaterial();
			value -= 0.5*position.getDomination();

		}
		return value;
	}
	
	

	//cutoff test
	public boolean cutOff(Position pos, int depth) {

		if (depth >= maxD){
			return true;
		}
		if(pos.isMate()){
			return true;
		}
		if(pos.isStaleMate()){
			return true;
		}
		if (pos.isTerminal()){
			return true;
		}
		return false;
	}	
}




