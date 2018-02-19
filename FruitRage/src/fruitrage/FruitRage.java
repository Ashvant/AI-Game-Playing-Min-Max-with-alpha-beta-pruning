/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fruitrage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Double.min;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import static jdk.nashorn.internal.objects.NativeMath.max;

/**
 *
 * @author ashvant
 */
public class FruitRage {
        /**
     * @param args the command line arguments
     */
    static int count = 1;
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String FILENAME = "input1.txt";
        
        
        FileReader fr = new FileReader(FILENAME);
        BufferedReader br = new BufferedReader(fr);
        String sCurrentLine;
        int lineNumber = 0;
        int boardSize=0,type=0;
        char[][] board = null;
        double time = 0;
        
            while ((sCurrentLine = br.readLine()) != null)
            {       
                switch (lineNumber) {
                    case 0:
                        boardSize = Integer.parseInt(sCurrentLine);
                        board = new char[boardSize][boardSize];
                        break;
                    case 1:
                        type = Integer.parseInt(sCurrentLine);
                        break;
                    case 2:
                        time = Double.parseDouble(sCurrentLine);
                        break;
                    default:
                        int iterator = 0;                      
                        while(iterator<boardSize){
                            board[lineNumber-3][iterator] = (sCurrentLine.charAt(iterator));
                            iterator++;
                        }
                        break;
                }
                lineNumber++;
            }
            MinMaxDecision(board,boardSize,type,time);
            br.close();
            fr.close();
    }
    private static void MinMaxDecision(char[][] board, int boardSize, int type, double time) {
        int bestMove = Integer.MIN_VALUE;
        int bestRow = -1;
        int bestCol = -1;
        int bestScore = 0;
        int returnValue;
        StateNew bestState = null;
        StateNew currentState = new StateNew(board,1);
        boolean[][] visited = new boolean[boardSize][boardSize];
        int row,col;
        for(row = 0;row<boardSize;row++){
            for(col = 0;col<boardSize;col++){
                if(board[row][col]!='*' && !visited[row][col]){
                    int alpha = Integer.MIN_VALUE;
                    int beta = Integer.MAX_VALUE;
                    int depth = 0;
                    StateNew nextState = new StateNew(currentState);
                    count =1;
                    nextState = getConnectedComponent(nextState,visited,row,col,false);
                    nextState.scoreSum = (count*count);
                    nextState.applyGravity();
                    /*for(int i=0;i<boardSize;i++){
                        for(int j=0;j<boardSize;j++){
                            System.out.print(nextState.board[i][j]);
                        }
                        System.out.print("\n");
                    }
                   System.out.println(nextState.scoreSum);*/
                   returnValue =  MinValue(nextState,depth,alpha,beta)+nextState.scoreSum;
                    if(returnValue>bestMove){
                        bestState = nextState;
                        bestMove = returnValue;
                        bestRow = row;
                        bestCol = col;
                    }
                }
            }
        }
        for(int i=0;i<boardSize;i++){
                        for(int j=0;j<boardSize;j++){
                            System.out.print(bestState.board[i][j]);
                        }
                        System.out.print("\n");
                    }
        System.out.println(bestRow+" "+bestCol);
        System.out.println(bestState.scoreSum);
    }
    private static StateNew getConnectedComponent(StateNew successorState, boolean[][] visited,int row,int col,boolean isMin) {
        int rowToCheck[] ={0,0,1,-1};
        int colToCheck[] ={1,-1,0,0};
        visited[row][col] = true;
        int value = successorState.board[row][col];
        int size = successorState.board[0].length;
        successorState.board[row][col] = '*';
        for(int check = 0;check<rowToCheck.length;check++){
            int rowAdj = row+rowToCheck[check];
            int colAdj = col+colToCheck[check];
            if(rowAdj>=0 && colAdj>=0 && rowAdj<size && colAdj<size){
                if(value == successorState.board[rowAdj][colAdj]&&!visited[rowAdj][colAdj]){
                    count++;
                    getConnectedComponent(successorState,visited,rowAdj,colAdj,isMin);
                    successorState.board[rowAdj][colAdj] = '*';
                }
            }
        }
        return successorState;
    }
    
    private static int MaxValue(StateNew currentState,int depth,int alpha,int beta) {
        int iterator;
        Queue<StateNew> successorList = new LinkedList<StateNew>();
        if(depth == 3 || currentState.isEmpty()){
            return currentState.calEval(true);
        }else{
            successorList = calculateSucessors(currentState,false);
            while(!successorList.isEmpty()){
                StateNew successorState = successorList.poll();
                alpha = (int) max(alpha,MinValue(successorState,depth+1,alpha,beta)+successorState.scoreSum);
                if(alpha>= beta){
                    return beta;
                }
            }
            return alpha;
        }
    }
    private static int MinValue(StateNew currentState,int depth, int alpha, int beta) {
        int iterator = 0;
        Queue<StateNew> successorList = new LinkedList<StateNew>();
        if(depth == 3 || currentState.isEmpty()){
            return currentState.calEval(false);
        }else{
            successorList = calculateSucessors(currentState,true);
            while(!successorList.isEmpty()){
                StateNew successorState = successorList.poll();
                beta = (int) min(beta,MaxValue(successorState,depth+1,alpha,beta)- successorState.scoreSum);
                if(alpha>= beta){
                    return alpha;
                }
            }
            return beta;
        }
    }
    
    private static Queue<StateNew> calculateSucessors(StateNew currentState,boolean isMin) {
        Queue<StateNew> successorList = new LinkedList<StateNew>();
        int iterator,iterator2;
        boolean[][] visited = new boolean[currentState.board[0].length][currentState.board[0].length];
        for(iterator = 0;iterator<currentState.board[0].length;iterator++){
            for(iterator2 = 0;iterator2<currentState.board[0].length;iterator2++){
                StateNew successorState = new StateNew(currentState);
                if(!visited[iterator][iterator2] && successorState.board[iterator][iterator2]!='*'){
                    count = 1;
                    successorState = getConnectedComponent(successorState,visited,iterator,iterator2,isMin);
                    if(isMin){
                        successorState.scoreSum = (count*count);
                    }else{
                        successorState.scoreSum = (count*count);
                    }
                    successorState.applyGravity();
                    successorList.add(successorState);
                }
            }
        }
        return successorList;
    }
}