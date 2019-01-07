//Author: James Jia

package chai;

import java.util.HashMap;

import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;

public class AlphaBetaTransposition implements ChessAI {
	
	//transposition table - holds hashcode and position's value and depth
	public HashMap<Long, int[]> tt;
	public static int WORSTCASE = -Integer.MAX_VALUE;
	public static int BESTCASE = Integer.MAX_VALUE;
	public int maxD;
	public int color;
	public short bestMove;
	public int nodes;

	
	//tell the AI what color it is - color will become its color (1 for black, 0 for white)
	public AlphaBetaTransposition(int col, int depth){
		tt = new HashMap<Long, int[]>();
		color = col;
		maxD = depth;
		nodes = 0;
		bestMove = 0;
	}
	
	//calls iterative deepening
	public short getMove(Position pos) {
		
		Position position = new Position(pos);
		short move = idabmmtt(position);
		System.out.println("Alpha Beta W/ Transposition Table nodes searched: " + nodes);
		return move;
	}
	
	//iterative deepening alpha beta mini max transposition table
	public short idabmmtt(Position pos) {
		
		//This loop will run minimax search for depth 1 up to depth maxD
		for (int i = maxD-1; i >= 0; i--) {
			
			//save results from each loop in bestMove instance variable
			bestMove = abttMiniMax(pos,i);
			
			
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
	
	//alpha beta transposition table mini max search
	public short abttMiniMax(Position pos, int startDepth) {

		short bestMove = 0;
		int bestValue = WORSTCASE;
		int currentValue;
		int alpha = WORSTCASE;
		int beta = BESTCASE;
		
		//try all moves and get their utility
		for (short move : pos.getAllMoves()) {

			try {
				pos.doMove(move);
				
				//if already in transposition table - uses position's getHashCode
				if (tt.containsKey(pos.getHashCode())) {
					
					//info array stores position's value and index
					int info[] = tt.get(pos.getHashCode());
					
					//if search depth is less than previous search depth
					if (maxD-startDepth < info[1]) {
						
						//update position's info with this position
						currentValue = info[0];
						int[] updateInfo = {currentValue, maxD-startDepth};
						tt.put(pos.getHashCode(), updateInfo);
						
					//otherwise, keep searching down the tree	
					} else {
						currentValue = mini(pos, startDepth, alpha, beta);
					}
				} 
				
				//if position not already in table, keep searching down tree
				//then add position into table with its info
				else {
					currentValue = mini(pos, startDepth, alpha, beta);
					int[] updateInfo = {currentValue, maxD-startDepth};
					tt.put(pos.getHashCode(), updateInfo);
				}

				if (currentValue > bestValue) {
					bestValue = currentValue;
					bestMove = move;
				}

				pos.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		return bestMove;
	}
	
	
	public int mini(Position pos, int depth, int alpha, int beta) {
		
		nodes++;
		int currentDepth = depth+1;
		
		//Base case: pass cutoff
		if (cutOff(pos, currentDepth)){
			return utility(pos);
		}
		
		int bestValue = BESTCASE;
		for (short move : pos.getAllMoves()) {
			try {
				pos.doMove(move);
				
				//if already in transposition table
				if (tt.containsKey(pos.getHashCode())) {
					//get the position's value and depth info
					int info[] = tt.get(pos.getHashCode());

					//if position's search depth (not current depth) <previously found
					//then update table with position's info
					if (maxD-currentDepth < info[1]) {
						bestValue = info[0];
						int[] updateInfo = {bestValue, maxD-currentDepth};
						tt.put(pos.getHashCode(), updateInfo);
						
					//not less than previously found - continue down tree	
					} 
					else {
						bestValue = Math.min(bestValue, max(pos, currentDepth, alpha, beta));
					}
					
				//not in table - continue down tree, then update table with position's info
				} 
				else {
					bestValue = Math.min(bestValue, max(pos, currentDepth, alpha, beta));
					int[] updateInfo = {bestValue, maxD-currentDepth};
					tt.put(pos.getHashCode(), updateInfo);
				}
				pos.undoMove();
				
				//alpha beta pruning
				if (bestValue<=alpha){
					return bestValue;
				}
				
				beta = Math.min(beta, bestValue);
				
			} catch (IllegalMoveException e) {
			}
		}
		return bestValue;
	}
	
	public int max(Position pos, int depth, int alpha, int beta) {

		nodes++;
		int currentDepth = depth+1;
		
		if (cutOff(pos, currentDepth)){
			return utility(pos);
		}
		
		int bestValue = WORSTCASE;
		
		//try all moves
		for (short move : pos.getAllMoves()) {
			try {
				pos.doMove(move);
				
				//if in table already
				if (tt.containsKey(pos.getHashCode())) {
					
					//get the position's value and depth info
					int info[] = tt.get(pos.getHashCode());

					//if position's search depth (not current depth) is less than previously found
					//then update table with position's info
					if (maxD-currentDepth < info[1]) {
						bestValue = info[0];
						int[] updateInfo = {bestValue, maxD-currentDepth};
						tt.put(pos.getHashCode(), updateInfo);
					} 
					
					//if position's search depth >= previously found - continue down tree
					else {
						bestValue = Math.max(bestValue, mini(pos, currentDepth, alpha, beta));
					}
				} 
				
				//if not already in table - continue down tree, and then update
				else {
					bestValue = Math.max(bestValue, mini(pos, currentDepth, alpha, beta));
					int[] updateInfo = {bestValue, maxD-currentDepth};
					tt.put(pos.getHashCode(), updateInfo);
				}
				pos.undoMove();
				
				
				//alpha beta pruning
				if(bestValue>=beta){
					return bestValue;
				}
				alpha=Math.max(alpha, bestValue);
				
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




