package fruitrage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import static java.lang.Double.min;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import static jdk.nashorn.internal.objects.NativeMath.max;

/**
 *
 * @author ashvant
 */
public class FruitRageNew {
        /**
     * @param args the command line arguments
     */
    static int count = 1;
    static int lookAhead;
    static int sizeLimit =15;
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String FILENAME = "input.txt";
        
        
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
     long startTime = System.currentTimeMillis();
     long elapsedTime = 0;
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
                 	elapsedTime = (new Date()).getTime() - startTime;
                }
            }
         }
            setLookAhead(boardSize,time,queue.size());
            bestState = queue.peek();
            bestRow = queue.peek().row;
            bestCol = queue.peek().col;
            int iterator =0;
            while(!queue.isEmpty()){
                if(iterator>=sizeLimit){
                    break;
                }
                if(elapsedTime>(time-1.5)*1000){
                    break;
                }
                int alpha = Integer.MIN_VALUE;
                int beta = Integer.MAX_VALUE;
                int depth = 0;
                StateNew state = queue.poll();
                returnValue =  MinValue(state,depth,alpha,beta)+state.scoreSum;
                if(returnValue>bestMove){
                           bestState = state;
                           bestMove = returnValue;
                           bestRow = state.row;
                           bestCol = state.col;
                           bestScore = state.scoreSum;
                  }
                iterator++;
             	elapsedTime = (new Date()).getTime() - startTime;
           }
           
           /*for(int i=0;i<boardSize;i++){
                for(int j=0;j<boardSize;j++){
                    System.out.print(bestState.board[i][j]);
                }
                System.out.print("\n");
            }
           System.out.println((char)(bestCol+65)+""+(bestRow+1));
           System.out.println(bestScore);
           System.out.println(bestMove);*/
           printResult(bestRow,bestCol,bestState.board);
        
        
    }
    
    private static void printResult(int bestRow,int bestCol,char[][] board){
        Writer writer = null;
        int iterator = 0,iterator2=0;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream("output.txt"), "utf-8"));
            writer.write((char)(bestCol+65)+""+(bestRow+1));
           writer.write("\n");
            for(iterator =0;iterator<board[0].length;iterator++){
                for(iterator2 = 0;iterator2<board[0].length;iterator2++){
                    writer.write(String.valueOf(board[iterator][iterator2]));
                }
                writer.write("\n");
              }
        } catch (IOException ex) {
          // report
        } finally {
           try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }
    
    public static StateNew getConnectedComponent(StateNew successorState, boolean[][] visited,int row,int col,boolean isMin) {
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
        Comparator<StateNew> comparator = new MyComparator();
        PriorityQueue<StateNew> successorList = 
        new PriorityQueue<StateNew>(20, comparator);
        int iterator =0;
        if(depth == lookAhead || currentState.isEmpty()){
            return currentState.calEval(true);
        }else{
            successorList = calculateSucessors(currentState,false);
            while(!successorList.isEmpty() && iterator<20){
               StateNew successorState = successorList.poll();
                iterator++;
                alpha = (int) max(alpha,MinValue(successorState,depth+1,alpha,beta)+successorState.scoreSum);
                if(alpha>= beta){
                    return beta;
                }
            }
            
            return alpha;
        }
    }
    private static int MinValue(StateNew currentState,int depth, int alpha, int beta) {
        Comparator<StateNew> comparator = new MyComparator();
        PriorityQueue<StateNew> successorList = 
        new PriorityQueue<StateNew>(20, comparator);
        int iterator = 0;
        if(depth == lookAhead || currentState.isEmpty()){
            return currentState.calEval(false);
        }else{
            successorList = calculateSucessors(currentState,true);
            while(!successorList.isEmpty()&&iterator<20){
                StateNew successorState = successorList.poll();
                iterator++;
                beta = (int) min(beta,MaxValue(successorState,depth+1,alpha,beta)- successorState.scoreSum);
                if(alpha>= beta){
                    return alpha;
                }
            }
            return beta;
        }
    }
    
    private static PriorityQueue<StateNew> calculateSucessors(StateNew currentState,boolean isMin) {
        Comparator<StateNew> comparator = new MyComparator();
        PriorityQueue<StateNew> successorList = 
        new PriorityQueue<StateNew>(20, comparator);
        int iterator,iterator2;
        boolean[][] visited = new boolean[currentState.board[0].length][currentState.board[0].length];
        for(iterator = 0;iterator<currentState.board[0].length;iterator++){
            for(iterator2 = 0;iterator2<currentState.board[0].length;iterator2++){
                StateNew successorState = new StateNew(currentState);
                if(!visited[iterator][iterator2] && successorState.board[iterator][iterator2]!='*'){
                    count = 1;
                    successorState = getConnectedComponent(successorState,visited,iterator,iterator2,isMin);
                    successorState.scoreSum = (count*count);
                    successorState.applyGravity();
                    successorList.add(successorState);
                }
            }
        }
        return successorList;
    }

    private static void setLookAhead(int boardSize, double time,int branchSize) {
       switch(boardSize){
           case 1:
           case 2:
           case 3:
           case 4:
               if(time<4){
                   lookAhead = 2;
                   sizeLimit = 20;
               }else{
                   lookAhead =4;
                   sizeLimit =40;
               }
               break;
           case 5:
           case 6:
           case 7:
               if(branchSize>10){
                   if(time<4){
                        lookAhead = 1;
                        sizeLimit = 20;
                   }else{
                        lookAhead =3;
                        sizeLimit = 40;
                   }
               }else{
                   if(time<4){
                        lookAhead = 3;
                        sizeLimit = 20;
                   }else{
                        lookAhead =4;
                        sizeLimit = 40;
                   }
               }
              break;
           case 8:
               if(branchSize>20){
                   if(time<4){
                        lookAhead = 1;
                        sizeLimit = 20;
                   }else{
                        lookAhead =3;
                        sizeLimit =40;
                   }
               }else{
                   if(time<4){
                        lookAhead = 3;
                        sizeLimit = 20;
                   }else{
                        lookAhead =4;
                        sizeLimit = 40;
                   }
               }
              break;
           case 9:
               if(branchSize>10){
                   if(time<4){
                        lookAhead = 1;
                        sizeLimit = 20;
                   }else{
                        lookAhead =3;
                        sizeLimit = 40;
                   }
               }else{
                   if(time<4){
                        lookAhead = 2;
                        sizeLimit = 20;
                   }else{
                        lookAhead =4;
                        sizeLimit = 40;
                   }
               }
              break;
           case 10:
               if(branchSize>10){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else{
                        lookAhead =3;
                        sizeLimit =40;
                    }
               }else{
                   if(time<4){
                        lookAhead =2;
                        sizeLimit =10;
                    }else{
                        lookAhead =4;
                        sizeLimit =40;
                    }
               }
               break;
           case 11:
               if(branchSize>20){
                   if(time<10){
                        lookAhead =2;
                        sizeLimit =10;
                    }else if(time<220){
                        lookAhead =3;
                        sizeLimit =40;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>10){
                   if(time<8){
                        lookAhead =2;
                        sizeLimit =10;
                    }else{
                        lookAhead =4;
                        sizeLimit =40;
                    }
               }else{
                   if(time<4){
                        lookAhead =2;
                        sizeLimit =10;
                    }else{
                        lookAhead =4;
                        sizeLimit =40;
                    }
               }
               break;
            case 12:
               if(branchSize>40){
                   if(time<4){
                       lookAhead =1;
                        sizeLimit =6;
                   }else if(time<40){
                        lookAhead =2;
                        sizeLimit =30;
                    }else if(time<230){
                        lookAhead =3;
                        sizeLimit =30;
                    }else {
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>25){
                   if(time<6){
                        lookAhead =1;
                        sizeLimit =10;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>15){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                   }else if(time<18){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else{
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }
               break;
            case 13:
               if(branchSize>70){
                   if(time<4){
                       lookAhead =1;
                        sizeLimit =6;
                   }else if(time<40){
                        lookAhead =2;
                        sizeLimit =30;
                    }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>40){
                   if(time<4){
                       lookAhead = 1;
                        sizeLimit =10;
                   }else if(time<40){
                        lookAhead =2;
                        sizeLimit =30;
                    }else if(time<250){
                        lookAhead =3;
                        sizeLimit =30;
                    }else {
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>15){
                   if(time<4){
                       lookAhead = 2;
                        sizeLimit =10;
                   }else if(time<50){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>10){
                   if(time<4){
                       lookAhead = 1;
                        sizeLimit =10;
                   }else if(time<18){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else{
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else{
                       lookAhead =4;
                       sizeLimit =30;
                   }
               }
               break;
            case 14:
               if(branchSize>70){
                   if(time<10){
                        lookAhead =1;
                        sizeLimit =8;
                    }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>40){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<200){
                        lookAhead =3;
                        sizeLimit =30;
                    }else {
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>15){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<200){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>10){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<20){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else{
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else{
                       lookAhead =3;
                       sizeLimit =30;
                   }
               }
               break;
            case 15:
               if(branchSize>60){
                   if(time<10){
                        lookAhead =1;
                        sizeLimit =8;
                    }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>40){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =6;
                    }else if(time<200){
                        lookAhead =3;
                        sizeLimit =30;
                    }else {
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>15){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<200){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>10){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<18){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else{
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else{
                       lookAhead =4;
                       sizeLimit =30;
                   }
                   
               }
               break;
            case 16:
               if(branchSize>60){
                   if(time<5){
                        lookAhead =1;
                        sizeLimit =8;
                    }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>40){
                   if(time<5){
                        lookAhead =1;
                        sizeLimit =8;
                    }else if(time<200){
                        lookAhead =3;
                        sizeLimit =20;
                    }else {
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>15){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<200){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else if(branchSize>10){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<40){
                        lookAhead =3;
                        sizeLimit =20;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else{
                   if(time<5){
                        lookAhead =1;
                        sizeLimit =10;
                    }else{
                       lookAhead =4;
                       sizeLimit =30;
                   }
                   
               }
               break;
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
                if(branchSize>225){
                        if(time<4){
                            lookAhead = 1;
                        }else if(time<100){
                            lookAhead =2;
                            sizeLimit = 30;
                        }else{
                            lookAhead =3;
                            sizeLimit =30;
                        }
               }else if(branchSize>100){
                   if(time<5){
                        lookAhead =0;
                        sizeLimit =8;
                    }else if(time<40){
                        lookAhead =2;
                        sizeLimit =30;
                    }else {
                       lookAhead =3;
                       sizeLimit =30;
                   }
               }else if(branchSize>60){
                   if(time<15){
                        lookAhead =1;
                        sizeLimit =8;
                    }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>40){
                   if(time<10){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<40){
                        lookAhead =2;
                        sizeLimit =30;
                    }else if(time<80){
                        lookAhead =2;
                        sizeLimit =30;
                    }else {
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>15){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<50){
                        lookAhead =2;
                        sizeLimit =30;
                    }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>10){
                   if(time<4){
                        lookAhead =2;
                        sizeLimit =10;
                    }else if(time<25){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else{
                   if(time<10){
                        lookAhead =2;
                        sizeLimit =30;
                    }else{
                       lookAhead =4;
                       sizeLimit =40;
                   }
                   
               }
               break;
            case 23:
            case 24:
            case 25:
            case 26:
               if(branchSize>225){
                   if(time<15){
                       lookAhead =1;
                   }else if(time<200){
                       lookAhead =2;
                       sizeLimit =25;
                   }else{
                       lookAhead =3;
                       sizeLimit =25;
                   }     
               }else if(branchSize>100){
                   if(time<5){
                        lookAhead =1;
                        sizeLimit =8;
                    }else if(time<150){
                       lookAhead =2;
                       sizeLimit =30;
                   }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>60){
                   if(time<15){
                        lookAhead =1;
                        sizeLimit =8;
                    }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>40){
                   if(time<10){
                        lookAhead =1;
                        sizeLimit =10;
                    }else{
                        lookAhead =3;
                        sizeLimit =30;
                    }
               }else if(branchSize>15){
                   if(time<4){
                        lookAhead =1;
                        sizeLimit =10;
                    }else if(time<50){
                        lookAhead =3;
                        sizeLimit =15;
                    }else{
                        lookAhead =3;
                        sizeLimit =20;
                    }
               }else if(branchSize>10){
                   if(time<4){
                        lookAhead =2;
                        sizeLimit =10;
                    }else if(time<25){
                        lookAhead =3;
                        sizeLimit =30;
                    }else{
                        lookAhead =4;
                        sizeLimit =30;
                    }
               }else{
                   if(time<10){
                        lookAhead =2;
                        sizeLimit =10;
                    }else{
                       lookAhead =4;
                       sizeLimit =30;
                   }
                   
               }
               break;
       }
      /* System.out.println(branchSize);
       System.out.println(lookAhead);
       System.out.println(sizeLimit);*/
       
    }
}
class StateNew{
    char[][] board;
    int scoreSum;
    ArrayList<String> moves;
    ArrayList<Integer> scoreList;
    int row=0;
    int col = 0;
    public StateNew(char[][] nursery,int evalsent){
        board = new char[nursery[0].length][nursery[0].length];
        for (int i=0; i < nursery[0].length; i++) {
            System.arraycopy(nursery[i], 0, board[i], 0, nursery[0].length);
        }
        scoreSum = evalsent;
    }
    public StateNew(StateNew state){
        board = new char[state.board[0].length][state.board[0].length];
        for (int i = 0; i < state.board.length; i++) {
            System.arraycopy(state.board[i], 0, board[i], 0, state.board.length);
        }
        scoreSum = 0;
    }

    int calEval(boolean isMax) {
        int eval = 0;
       /* Comparator<StateNew> comparator = new MyComparator();
        boolean[][] visited = new boolean[this.board[0].length][this.board[0].length];
        PriorityQueue<StateNew> queue = 
        new PriorityQueue<StateNew>(20, comparator);
        StateNew currentState = new StateNew(this);
        for(row = 0;row<this.board[0].length;row++){
            for(col = 0;col<this.board[0].length;col++){
                if(currentState.board[row][col]!='*'){
                    count =1;
                    StateNew nextState = FruitRageNew.getConnectedComponent(currentState,visited,row,col,false);
                    nextState.scoreSum = (count*count);
                    nextState.applyGravity();
                    queue.add(nextState);
                    currentState = new StateNew(nextState);
                }
            }
         }
        int iterator=0;
        while(!queue.isEmpty()){
            StateNew state = queue.poll();
            if(isMax){
                if(iterator%2==0){
                    eval= eval + state.scoreSum;
                }else{
                    eval= eval - state.scoreSum;
                }
            }else{
                if(iterator%2==0){
                    eval= eval - state.scoreSum;
                }else{
                    eval= eval + state.scoreSum;
                }
            }
            
        }*/
      return eval;
    }
    boolean isEmpty(){
        boolean result =true;
        for(int col = 0;col<this.board[0].length;col++){
            for(int row = 0;row<this.board[0].length;row++){
                if(board[row][col] != '*'){
                    result  = false;
                    return result;
                }
            }
        }
        return result;
    }
    void applyGravity() {
        for(int col = 0;col<this.board[0].length;col++){
            for(int row = 0;row<this.board[0].length;row++){
                if(board[row][col] == '*'){
                    for(int i=row;i>0;i--){
                        board[i][col] = board[i-1][col];
                    }
                    board[0][col] = '*';
                }
            }
        }
    }

}
class MyComparator implements Comparator<StateNew>
{
    public int compare(StateNew s1,StateNew s2)
    {   
        if(s1.scoreSum<s2.scoreSum){
            return 1;
        }else if(s1.scoreSum>s2.scoreSum){
            return -1;
        }else{
            return 0;
        }
    }
}