package minesweeper;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH; 32*27
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR; 32*18 + 64
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int FPS = 30;

    public String configPath;

    public static Random random = new Random();
	
	public static int[][] mineCountColour = new int[][] {
            {0,0,0}, // black
            {0,0,255}, //Blue
            {0,133,0}, //Green
            {255,0,0}, //Red
            {0,0,132}, //Blue
            {132,0,0}, //Red
            {0,132,132}, //Teal
            {132,0,132}, //Purple
            {32,32,32} //Dark Grey
    };
	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.
    public static int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private static int bombCount = 100;
    PImage tile1;
    PImage tile2;
    PImage tile;
    PImage wall0;
    PImage flag;
    PImage[] mineExplosion;
    PImage mine0;
    private static final int totalMineFrames = 10;
    private static int totalExplosionFrames = 0;
    private static SortedMap<Float,List<Integer>> mines;
    private static int winFlag = 0;
    private static int gameEndTime = 0;

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
		//See PApplet javadoc:
		//loadJSONObject(configPath);
		//loadImage(this.getClass().getResource(filename).getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        //create attributes for data storage, eg. board
        tile1 = loadImage("./src/main/resources/minesweeper/tile1.png");
        tile2 = loadImage("./src/main/resources/minesweeper/tile2.png");
        tile = loadImage("./src/main/resources/minesweeper/tile.png");
        wall0 = loadImage("./src/main/resources/minesweeper/wall0.png");
        mine0 = loadImage("./src/main/resources/minesweeper/mine0.png");
        flag = loadImage("./src/main/resources/minesweeper/flag.png");
        //setup explosion
        mines = new TreeMap<>();
        mineExplosion = new PImage[totalMineFrames];
        for (int i = 0; i < 10; i++) {
            mineExplosion[i] = loadImage("./src/main/resources/minesweeper/mine"+i+".png");
        }
        //populate board
        Arrays.fill(board[0], -1000);
        Arrays.fill(board[1], -1000);
        for (int i = 2; i < BOARD_HEIGHT; i++) {
            Arrays.fill(board[i], 0);
        }
        //generate bomb
        int bombGenerated = 0;
        while(bombGenerated < bombCount){
            int x = random.nextInt(BOARD_WIDTH);
            int y = random.nextInt(BOARD_HEIGHT - 2) + 2;
            if(board[y][x] != -1){
                board[y][x] = -1;
                bombGenerated ++;
            }
        }
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){

    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        
    }

    private void checkAdjacentMines(int indexY, int indexX){
        int boardStatus = board[indexY][indexX];
        if(boardStatus == 0){
            int adjacentMineCount = 0;
            //
            if(indexX - 1 >= 0){
                if(board[indexY][indexX-1] == -1 || board[indexY][indexX-1] == -3) {
                    adjacentMineCount++;
                }
            }
            if(indexY - 1 >= 0){
                if(board[indexY-1][indexX] == -1 || board[indexY-1][indexX] == -3){
                    adjacentMineCount++;
                }
            }
            if(indexX - 1 >= 0 && indexY - 1 >=0){
                if(board[indexY-1][indexX-1] == -1 || board[indexY-1][indexX-1] == -3){
                    adjacentMineCount++;
                }
            }
            if(indexX - 1 >= 0 && indexY + 1 < BOARD_HEIGHT){
                if(board[indexY+1][indexX-1] == -1 || board[indexY+1][indexX-1] == -3){
                    adjacentMineCount++;
                }
            }
            if(indexY - 1 >= 0 && indexX + 1 < BOARD_WIDTH){
                if(board[indexY-1][indexX+1] == -1 || board[indexY-1][indexX+1] == -3){
                    adjacentMineCount++;
                }
            }
            if(indexY + 1 < BOARD_HEIGHT){
                if(board[indexY+1][indexX] == -1 || board[indexY+1][indexX] == -3){
                    adjacentMineCount++;
                }
            }
            if(indexX + 1 < BOARD_WIDTH){
                if(board[indexY][indexX+1] == -1 || board[indexY][indexX+1] == -3){
                    adjacentMineCount++;
                }
            }
            if(indexY + 1 < BOARD_HEIGHT && indexX + 1 < BOARD_WIDTH){
                if(board[indexY+1][indexX+1] == -1 || board[indexY+1][indexX+1] == -3){
                    adjacentMineCount++;
                }
            }
            if(adjacentMineCount > 0){
                board[indexY][indexX] = adjacentMineCount;
            }
            else{
                board[indexY][indexX] = 1000; //no mine and checked
                if(indexX - 1 >= 0){
                    checkAdjacentMines(indexY, indexX - 1);
                }
                if(indexY - 1 >= 0){
                    checkAdjacentMines(indexY-1, indexX);
                }
                if(indexX - 1 >= 0 && indexY - 1 >=0){
                    checkAdjacentMines(indexY-1, indexX-1);
                }
                if(indexX - 1 >= 0 && indexY + 1 < BOARD_HEIGHT){
                    checkAdjacentMines(indexY+1, indexX-1);
                }
                if(indexY - 1 >= 0 && indexX + 1 < BOARD_WIDTH){
                    checkAdjacentMines(indexY-1, indexX+1);
                }
                if(indexY + 1 < BOARD_HEIGHT){
                    checkAdjacentMines(indexY+1, indexX);
                }
                if(indexX + 1 < BOARD_WIDTH){
                    checkAdjacentMines(indexY, indexX+1);
                }
                if(indexY + 1 < BOARD_HEIGHT && indexX + 1 < BOARD_WIDTH){
                    checkAdjacentMines(indexY+1, indexX+1);
                }
            }
        }
    }

    private void checkWin(){
        int win = 1;
        for (int j = 2; j < BOARD_HEIGHT; j++) {
            for (int i = 0; i < BOARD_WIDTH; i++) {
                if(board[j][i] == 0 || board[j][i] == -2){
                    win = 0;
                    break;
                }
            }
        }
        if(win == 1){
            winFlag = 1;
            gameEndTime = millis()/1000;
        }
    }

    private void findAllMines(int indexY, int indexX){
        for(int y = 0; y < BOARD_HEIGHT; y++) {
            for(int x = 0; x < BOARD_WIDTH; x++) {
                if(board[y][x] == -1 || board[y][x] == -3){
                    float distance = (x - indexX)*(x - indexX) + (y - indexY)*(y - indexY);
                    mines.put(distance,Arrays.asList(y,x,0));
                }
            }
        }
        for(Map.Entry<Float,List<Integer>> entry : mines.entrySet()){
            int y = entry.getValue().get(0);
            int x = entry.getValue().get(1);
            entry.setValue(Arrays.asList(y,x,-totalExplosionFrames));
            totalExplosionFrames += 3;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //System.out.println(e.getButton());
        if(winFlag == 0){
            int indexY = mouseY/CELLSIZE;
            int indexX = mouseX/CELLSIZE;
            int status = board[indexY][indexX];
            if(e.getButton() == 39){ //right click
                if(status == 0){
                    board[indexY][indexX] = -2; //no bomb but flagged
                }
                else if (status == -1){
                    board[indexY][indexX] = -3; //has bomb and flagged
                }
                else if (status == -2){
                    board[indexY][indexX] = 0;
                }
                else if (status == -3){
                    board[indexY][indexX] = -1;
                }
            }
            else if(e.getButton() == 37){ //left click
                if(status == 0){
                    checkAdjacentMines(indexY, indexX);
                    checkWin();
                }
                else if(status == -1){
                    gameEndTime = millis()/1000;
                    winFlag = -1; //you lose
                    findAllMines(indexY, indexX);
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        background(205);
        //Show time
        textSize(32);
        if(winFlag == 1){ //you win
            fill(255,0,0);
            text("You win!", CELLSIZE*1, 60);
            fill(255,255,255); //white
            text("Time:", CELLSIZE*20, 60);
            text(gameEndTime,CELLSIZE*23,60);
        }
        else if(winFlag == -1){ // you lose
            fill(255,0,0);
            text("You lose!", CELLSIZE*1, 60);
            fill(255,255,255); //white
            text("Time:", CELLSIZE*20, 60);
            text(gameEndTime,CELLSIZE*23,60);
            for(Map.Entry<Float,List<Integer>> entry : mines.entrySet()){
                int y = entry.getValue().get(0);
                int x = entry.getValue().get(1);
                int frame = entry.getValue().get(2);
                if(frame >= 0){
                    board[y][x] = -4;
                    image(mineExplosion[frame],x*CELLSIZE,y*CELLSIZE);
                }
                if(frame < 9){
                    frame++;
                    entry.setValue(Arrays.asList(y,x,frame));
                }
            }
        }
        else{
            fill(255,255,255); //white
            text("Time:", CELLSIZE*20, 60);
            text(millis()/1000,CELLSIZE*23,60);
        }
        //draw game board
        for(int y = 0; y < BOARD_HEIGHT; y++) {
            for(int x = 0; x < BOARD_WIDTH; x++) {
                //-1000 means blank
                if(board[y][x] == 0 || board[y][x] == -1) { //normal tiles
                    image(tile1,x*CELLSIZE,y*CELLSIZE); //0 means normal
                }
                else if(board[y][x] == -2 || board[y][x] == -3){ //flagged tiles
                    image(tile1,x*CELLSIZE,y*CELLSIZE);
                    image(flag,x*CELLSIZE,y*CELLSIZE);
                } else if (board[y][x] == 1000) { //revealed tiles
                    image(tile,x*CELLSIZE,y*CELLSIZE);
                }
                else if(board[y][x] > 0){ //revealed tiles with number
                    int mineCount = board[y][x];
                    image(tile,x*CELLSIZE,y*CELLSIZE);
                    fill(mineCountColour[mineCount][0],mineCountColour[mineCount][1],mineCountColour[mineCount][2]);
                    text(board[y][x],x*CELLSIZE+8,y*CELLSIZE+24);
                }
            }
        }
        //draw mouseHovered
        if(mouseY >= 2*CELLSIZE && board[mouseY/CELLSIZE][mouseX/CELLSIZE] == 0){
            image(tile2,(mouseX/CELLSIZE)*CELLSIZE,(mouseY/CELLSIZE)*CELLSIZE);
        }
    }


    public static void main(String[] args) {
        PApplet.main("minesweeper.App");
        try{
            bombCount = Integer.parseInt(args[0]);
        }
        catch(Exception e){

        }
        //System.out.println(System.getProperty("user.dir")); //get current working directory
    } //packageName.App

}
