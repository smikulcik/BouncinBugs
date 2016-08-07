/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package turtleflip;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author Simon
 */
public class TurtleFlip {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IllegalMoveException, InterruptedException {
        Board b = new Board();
        b.place(new Coord(1, 2), Board.DOWN);
        b.place(new Coord(2, 2), Board.DOWN);
        b.place(new Coord(0, 3), Board.UP);
        b.place(new Coord(3, 3), Board.UP);
        b.place(new Coord(0, 0), Board.UP);
        int x = -1;
        int[][] test1 = {
            {0,x,x,0},
            {x,x,x,x},
            {x,x,x,x},
            {0,x,x,0}
        };// H-P, N-H, C-A, K-C, P-N, A-K, E-M, L-D, O-E, J-L, M-O, N-P
        int[][] test2 = {
            {x,x,x,x},
            {x,0,0,0},
            {0,0,0,x},
            {x,x,x,x}
        }; // impossible
        int[][] test3 = {
            {x,x,x,x},
            {0,x,x,0},
            {0,x,x,0},
            {x,x,x,x}
        };  //17 moves B-L, C-I, M-E, G-M, N-H, P-N, H-P, E-G, J-B, D-J, O-E, M-O, F-H, J-D, L-J, P-F, I-C
        //12 moves K-I, C-K, A-C, M-E, I-A, N-H, D-L, B-D, C-I, P-N, O-M, H-P, 
        int[][] test4 = {
            {x,x,x,0},
            {x,x,x,0},
            {x,x,x,0},
            {0,0,0,0}
        }; // impossible
        int[][] test5 = {
            {x,x,x,0},
            {x,0,x,0},
            {0,x,0,x},
            {0,x,x,x}
        };//10 moves N-F, P-N, C-K, A-C, B-D, C-I, I-A, O-M, N-H, H-P
        int[][] test6 = {
            {1,x,1,0},
            {x,1,x,0},
            {1,x,1,0},
            {0,0,0,0}
        };//B-L, F-H, H-P, E-O, J-D, P-N
        b.board = test6;
        b.print();
        //System.out.println(Integer.toBinaryString(b.hashCode()));
        ArrayList<ArrayList<Move>> solutions = Solver.solve(b);
        
        ArrayList<Move> best = solutions.get(0);  // one of the best solutions
        playthrough_moves(new Board(b), best);
        System.out.println("BEST: " + 
            String.join(
                ", ", 
                best.stream()
                    .map(Move::toString)
                    .collect(Collectors.toList())
            )
        );
        System.out.println("Number of solutions: " + solutions.size());
    }
    
    public static void playthrough_moves(Board b, ArrayList<Move> moves){
        System.out.println("Solution");
        b.print();
        moves.stream().forEach((Move move) -> {
            System.out.println(move);
            try{
                b.move(move);
            }catch(IllegalMoveException e){
                System.err.println("Uhh, this move is bad.." + move);
                return;
            }
            b.print();
        });
        System.out.println(moves.size() + " moves");
    }
}
