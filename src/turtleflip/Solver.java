/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package turtleflip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Simon
 */
public class Solver {
    
    public static final boolean DEBUG = false;
    
    public static boolean solve(Board b, ArrayList<Move> moves, ArrayList<ArrayList<Move>> solutions) throws IllegalMoveException, InterruptedException{
        
        PriorityBlockingQueue<BoardState> pq = new PriorityBlockingQueue(1, new Comparator<BoardState>() {
            @Override
            public int compare(BoardState b1, BoardState b2) {
                //double normalized_num_down_1 = b1.board.getNumDown()/(double)b1.board.getNum();
                //double normalized_num_down_2 = b2.board.getNumDown()/(double)b2.board.getNum();
                
                //if(b1.board.getNumDown() - b2.board.getNumDown() == 0)
                if(b1.moves.size() - b2.moves.size() != 0)
                    return b1.moves.size() - b2.moves.size();
                else
                    return b1.board.getNumDown() - b2.board.getNumDown();
                
                /*double s1 = normalized_num_down_1*(double)b1.board.getNumDown();
                double s2 = normalized_num_down_2*b2.board.getNumDown();
                double score = s1 - s2;
                if(Math.abs(score) < .0001)
                    return 0;
                if(score < 0)
                    return -1;
                else
                    return 1;*/
                //return normalized_num_down_1*(double)b1.board.getNumDown() - normalized_num_down_2*b2.board.getNumDown();
            }
        });
        Map<Integer, Board> old_states = Collections.synchronizedMap(new HashMap<>(100));
        BoardState init = new BoardState();
        init.board = b;
        init.moves = moves;
        pq.add(init);
        
        if(init.board.isSolved()){
            solutions.add(init.moves);
            return true;
        }
        Thread[] workers = new Thread[1];
        for(int w=0; w< workers.length; w++){
            final int worker_id = w;
            workers[w] = new Thread(){
                public void run(){
                    try {
                        work(worker_id, pq, solutions, old_states);
                    } catch (IllegalMoveException ex) {
                        Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            workers[w].start();
        }
        // wait for workers to finish
        for(int w=0; w< workers.length; w++)
            workers[w].join();
        
        return solutions.size() > 0;
    }
    private static boolean work(
            int id, PriorityBlockingQueue<BoardState> pq, 
            ArrayList<ArrayList<Move>> solutions, 
            Map<Integer, Board> old_states
    ) throws IllegalMoveException, InterruptedException{
        BoardState bs = null;
        int iteration = 0;
        int max_iteration = 1000000;
        int min_down = Integer.MAX_VALUE;
        while(iteration < max_iteration){ // && solutions.isEmpty()
            // expand this board state
            bs = pq.poll(1L, TimeUnit.SECONDS);
            // check that we havn't seen this state before
            if(bs == null)
                break;
            if(iteration % 10000 == 0 && DEBUG)
                System.out.println(
                    "W: " + id + " " + iteration + " Moves:" + bs.moves.size() + 
                    " NumDown:" + bs.board.getNumDown() + 
                    " PQ_SIZE:" + pq.size() + 
                    " old_states_size: " + old_states.size());
            expandBoardState(bs, pq, solutions, old_states);
            iteration++;
        }
        if(iteration == max_iteration)
            System.out.println(id +  " Max iteration hit. Stopping. Found " + solutions.size() + " solutions");
        else if(pq.isEmpty())
            if(solutions.isEmpty())
                System.out.println("Impossible: analyzed " + iteration + " states");
            else
                System.out.println("Solved completely: analyzed " + iteration + " states");
        return false;
    }
    
    static void expandBoardState(
        BoardState bs,
        PriorityBlockingQueue<BoardState> pq,
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
                        BoardState newbs = new BoardState();
                        newbs.board = new Board(bs.board);
                        newbs.moves = new ArrayList<>();
                        bs.moves.stream().forEach((m) -> {
                            newbs.moves.add(m);
                        });
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
                            // use hash code, but fall back to equals to double check
                            Board old_board = (Board)old_states.get(newbs.board.hashCode());
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
    }
}
