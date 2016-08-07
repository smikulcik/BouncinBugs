/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bouncinbugs;

/**
 *
 * @author Simon
 */

public class Move {
    Coord start;
    Coord end;
    public Move(Coord start, Coord end){
        this.start = start;
        this.end = end;
    }
    @Override
    public String toString(){
        return Board.toLetter(start) + "-" + Board.toLetter(end);
    }
}