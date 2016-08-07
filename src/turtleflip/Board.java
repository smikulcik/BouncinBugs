/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package turtleflip;

import java.util.Arrays;

/**
 *
 * @author Simon
 */
public class Board {
    int[][] board;
    public static final int SIZE = 4;  // 4x4 grid
    
    public static final int UP = 1;
    public static final int DOWN = -1;
    
    
    private volatile int hash = 0;
    
    public Board(){
        board = new int[SIZE][SIZE];
    }
    public Board(Board b){
        board = new int[SIZE][SIZE];
        for(int i=0;i < SIZE; i++)
            System.arraycopy(b.board[i], 0, board[i], 0, SIZE);
    }
    
    public static char toLetter(Coord coord){
        String alph = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int index = coord.row*Board.SIZE + coord.col;
        if(index >= 0 && index < 26)
            return alph.charAt(index);
        return '?';
    }
    
    public static Coord fromLetter(char letter){
        String alph = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int index = alph.indexOf(letter);
        if(index < 0){
            throw new IllegalArgumentException("Invalid letter" + letter);
        }
        int r = index / SIZE;
        int c = index % SIZE;
        return new Coord(r, c);
    }
    
    public void place(Coord c, int state){
        board[c.row][c.col] = state;
    }
    // returns null if legal, reason otherwise
    public String check_move(Move m){
        if( m.start.row < 0 || m.start.row >= SIZE ||
            m.start.col < 0 || m.start.col >= SIZE ||
            m.end.row < 0 || m.end.row >= SIZE ||
            m.end.col < 0 || m.end.col >= SIZE)
            return "must be valid coords";
        if(board[m.start.row][m.start.col] == 0)
            return "must have piece to move";
        if(m.start.row == m.end.row && m.start.col == m.end.col)
            return "must not move to starting location";
        if(board[m.end.row][m.end.col] != 0)
            return "must be empty in destination";
        if((Math.abs(m.start.col - m.end.col) != 2 && (Math.abs(m.start.col - m.end.col) != 0)) ||
           (Math.abs(m.start.row - m.end.row) != 2 && (Math.abs(m.start.row - m.end.row) != 0)))
            return "must be the right distance away";
        if(board[(m.start.row + m.end.row)/2][(m.start.col + m.end.col)/2] == 0)
            return "there must be a piece to jump";
        return null;
    }
    public void move(Move m)
            throws IllegalMoveException{
        
        String reason = check_move(m);
        if(reason != null)
            throw new IllegalMoveException(reason);
        
        board[m.end.row][m.end.col] = board[m.start.row][m.start.col];
        board[(m.start.row + m.end.row)/2][(m.start.col + m.end.col)/2] *= -1; // flip
        board[m.start.row][m.start.col] = 0;
    }
    public boolean isSolved(){
        
        for(int i=0;i < SIZE; i++){
            for(int j=0;j<SIZE;j++){
                if(board[i][j] == DOWN){
                    return false;
                }
            }
        }
        return true;
    }
    public int getNumDown(){
        int count = 0;
        for(int i=0;i < SIZE; i++){
            for(int j=0;j<SIZE;j++){
                if(board[i][j] == DOWN){
                    count++;
                }
            }
        }
        return count;
    }
    public int getNum(){
        int count = 0;
        for(int i=0;i < SIZE; i++){
            for(int j=0;j<SIZE;j++){
                if(board[i][j] != 0){
                    count++;
                }
            }
        }
        return count;
    }
    public void print(){
        System.out.print("  |");
        for(int i=0;i < SIZE; i++){
            System.out.print(i + " ");
        }
        System.out.println();
        for(int i=-1;i < SIZE; i++){
            System.out.print("--");
        }
        System.out.println();
        for(int i=0;i < SIZE; i++){
            System.out.print(i + " |");
            for(int j=0;j < SIZE; j++){
                if(board[i][j] == DOWN)
                    System.out.print("X ");
                else if(board[i][j] == UP)
                    System.out.print("O ");
                else
                    System.out.print( "  ");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    @Override
    public boolean equals(Object other){
        
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Board))return false;
        Board b = (Board)other;
        for(int i=0;i<SIZE;i++)
            for(int j=0;j<SIZE;j++)
                if(board[i][j] != b.board[i][j])
                    return false;
        return true;
    }

    @Override
    public int hashCode() {
        if(hash == 0) {
            for(int i=0;i<SIZE;i++)
                for(int j=0;j<SIZE;j++){
                    hash = hash << 2; 
                    if(this.board[i][j] == 0)
                        hash = hash*3 + 0x0;
                    if(this.board[i][j] == 1)
                        hash = hash*3 + 0x1;
                    if(this.board[i][j] == -1)
                        hash = hash*3 + 0x2;
                }
        }
        return hash;
    }
}
