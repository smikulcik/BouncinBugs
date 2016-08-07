/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bouncinbugs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 *
 * @author Simon
 */
public class Solver {
    
    public static final boolean DEBUG = false;
    
    public static ArrayList<ArrayList<Move>> solve(Board b)
            throws IllegalMoveException, InterruptedException{
        ArrayList<ArrayList<Move>> solutions = new ArrayList<>();
        PriorityQueue<BoardState> pq = new PriorityQueue(
            1, 
            new Comparator<BoardState>() {
                @Override
                public int compare(BoardState b1, BoardState b2) {
                    // by least moves, then by least DOWN
                    if(b1.moves.size() - b2.moves.size() != 0)
                        return b1.moves.size() - b2.moves.size();
                    else
                        return b1.board.getNumDown() - b2.board.getNumDown();
                }
            }
        );
        Map<Integer, Board> old_states = new HashMap<>(100);
        BoardState init = new BoardState(b);
        pq.add(init);
        
        if(init.board.isSolved()){
            solutions.add(init.moves);
            return solutions;
        }
        BoardState bs;
        int iteration = 0;
        int max_iteration = 1000000;
        int min_down = Integer.MAX_VALUE;
        while(iteration < max_iteration){
            // expand this board state
            bs = pq.poll();
            // bs is null if we are done
            if(bs == null)
                break;
            if(iteration % 10000 == 0 && DEBUG)
                System.out.println(
                    iteration + " Moves:" + bs.moves.size() + 
                    " NumDown:" + bs.board.getNumDown() + 
                    " PQ_SIZE:" + pq.size() + 
                    " old_states_size: " + old_states.size());
            expandBoardState(bs, pq, solutions, old_states);
            iteration++;
        }
        if(iteration == max_iteration)
            System.out.println(
                " Max iteration hit. Stopping. Found " +
                solutions.size() + " solutions"
            );
        else if(pq.isEmpty())
            if(solutions.isEmpty())
                System.out.println(
                    "Impossible: analyzed " + iteration + " states");
            else
                System.out.println(
                    "Solved completely: analyzed " + iteration + " states");
        
        return solutions;
    }
    
    static void expandBoardState(
        BoardState bs,
        PriorityQueue<BoardState> pq,
        ArrayList<ArrayList<Move>> solutions,
        Map<Integer, Board> old_states
    ) throws IllegalMoveException{
        for(int i = 0; i < Board.SIZE; i++)for(int j = 0; j < Board.SIZE; j++){
            if(bs.board.board[i][j] != 0){  // try each piece
                for(int k = 0; k < Board.SIZE; k++)for(int l = 0; l < Board.SIZE; l++){  // try every move
                    // ok now try the move
                    Move move = new Move(new Coord(i, j), new Coord(k, l));
                    if(bs.board.check_move(move) == null){
                        // create new board stat instance, save for later
                        BoardState newbs = new BoardState(bs);
                        // try new move and see if solved it
                        newbs.board.move(move);
                        newbs.moves.add(move);

                        if(newbs.board.isSolved()){
                            solutions.add(newbs.moves);
                            if(DEBUG)System.out.println("Found Solution: " +
                                String.join(
                                    ", ", 
                                    newbs.moves.stream()
                                        .map(Move::toString)
                                        .collect(Collectors.toList())
                                )
                            );
                            return;
                        }

                        // only add new state if it is new
                        boolean is_old_board = false;
                        if(old_states.containsKey(newbs.board.hashCode())){
                            // use hash code, but fall back to 
                            //    equals to double check
                            Board old_board = (Board)old_states.get(
                                newbs.board.hashCode());
                            if(newbs.board.equals(old_board)){
                                is_old_board = true;
                            }else{
                                System.err.println("HASH COLLISION DETECTED");
                                newbs.board.print();
                                old_board.print();
                                System.err.println(newbs.board.hashCode());
                                System.err.println(old_board.hashCode());
                                //System.exit(1);
                            }
                        }
                        if(!is_old_board){
                            pq.add(newbs);
                            old_states.put(newbs.board.hashCode(), newbs.board);
                        }
                    }
                }
            }
        }
    }
    static class BoardState {
        ArrayList<Move> moves;
        Board board;
        public BoardState(Board board){
            this.moves = new ArrayList<>();
            this.board = board;
        }
        public BoardState(BoardState bs){
            this.moves = new ArrayList<>();
            this.board = new Board(bs.board);
            
            bs.moves.stream().forEach((m) -> {
                moves.add(m);
            });
        }
        
    }
}
