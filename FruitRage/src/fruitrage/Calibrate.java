/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fruitrage;

import static fruitrage.FruitRageNew.count;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Double.min;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import static jdk.nashorn.internal.objects.NativeMath.max;

/**
 *
 * @author ashvant
 */
public class Calibrate {
    
     public static void main(String[] args) throws FileNotFoundException, IOException {
         int boardSize=17;
         char[][] board = null;
         double[][] TimeData =  new double[26][5];
        long startTime;
        long elapsedTime;
        Random r = new Random();
        while(boardSize<21){
            board = new char[boardSize][boardSize];
            for(int i=0;i<boardSize;i++){
                for(int j=0;j<boardSize;j++){
                      board[i][j] = (char) r.nextInt(10);
                  }
                  
                }
           
            for(int k=1;k<4;k++){
                elapsedTime = 0;
                startTime = System.currentTimeMillis();
                MinMaxDecision(board,boardSize,k);
                elapsedTime = (new Date()).getTime() - startTime;
                TimeData[boardSize][k]  = elapsedTime/1000;
            }
            boardSize++;
            
        }
        for(int i=1;i<21;i++){
                for(int j=0;j<5;j++){
                  System.out.print(TimeData[i][j]+"\t");
                }
                System.out.print("\n");
            }
   }
   private static void MinMaxDecision(char[][] board, int boardSize,int ahead) {
     int bestMove = Integer.MIN_VALUE;
     int bestRow = -1;
     int bestCol = -1;
     int bestScore=0;
     int returnValue;
     StateNew bestState = null;
     StateNew currentState = new StateNew(board,1);
        boolean[][] visited = new boolean[boardSize][boardSize];
        int row,col;
        Comparator<StateNew> comparator = new MyComparator();
        PriorityQueue<StateNew> queue = 
        new PriorityQueue<StateNew>(20, comparator);
        for(row = 0;row<boardSize;row++){
            for(col = 0;col<boardSize;col++){
                if(board[row][col]!='*' && !visited[row][col]){
                    StateNew nextState = new StateNew(currentState);
                    nextState.row = row;
                    nextState.col = col;
                    count =1;
                    nextState = getConnectedComponent(nextState,visited,row,col,false);
                    nextState.scoreSum = (count*count);
                    nextState.applyGravity();
                    queue.add(nextState);
                  
                }
            }
         }
       
            bestState = queue.peek();
            bestRow = queue.peek().row;
            bestCol = queue.peek().col;
            int iterator =0;
            while(!queue.isEmpty()){
                if(iterator>=10){
                    break;
                }
                int alpha = Integer.MIN_VALUE;
                int beta = Integer.MAX_VALUE;
                int depth = 0;
                StateNew state = queue.poll();
                returnValue =  MinValue(state,depth,alpha,beta,ahead)+state.scoreSum;
                if(returnValue>bestMove){
                           bestState = state;
                           bestMove = returnValue;
                           bestRow = state.row;
                           bestCol = state.col;
                           bestScore = state.scoreSum;
                  }
                iterator++;
                
           }
        
        
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
    
    private static int MaxValue(StateNew currentState,int depth,int alpha,int beta,int ahead) {
        int iterator;
        Queue<StateNew> successorList = new LinkedList<StateNew>();
        if(depth == ahead || currentState.isEmpty()){
            return currentState.calEval(true);
        }else{
            successorList = calculateSucessors(currentState,false);
            while(!successorList.isEmpty()){
                StateNew successorState = successorList.poll();
                alpha = (int) max(alpha,MinValue(successorState,depth+1,alpha,beta,ahead)+successorState.scoreSum);
                if(alpha>= beta){
                    return beta;
                }
            }
            return alpha;
        }
    }
    private static int MinValue(StateNew currentState,int depth, int alpha, int beta,int ahead) {
        int iterator = 0;
        Queue<StateNew> successorList = new LinkedList<StateNew>();
        if(depth == ahead || currentState.isEmpty()){
            return currentState.calEval(false);
        }else{
            successorList = calculateSucessors(currentState,true);
            while(!successorList.isEmpty()){
                StateNew successorState = successorList.poll();
                beta = (int) min(beta,MaxValue(successorState,depth+1,alpha,beta,ahead)- successorState.scoreSum);
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

    private static void setLookAhead(int boardSize, double time) {
       
    }
}
