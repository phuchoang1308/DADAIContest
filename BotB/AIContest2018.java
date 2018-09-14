import java.util.*;

public class AIContest2018 {
    private static final int NUM_PLAYERS = 5;
    private static final int TEAM_A_ID = 0;
    private static final int TEAM_B_ID = 1;
    private static final int MOVE_CAP = 300;
    private static final int SHOOT_CAP = 200;
    private static final int GOAL_WIDTH = 3000;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print(setInitialPosition(14400, 9600));
        int iTeam = sc.nextInt();
        int mapW = sc.nextInt();
        int mapH = sc.nextInt();
        int numTurns = sc.nextInt();
        Game game = new Game(NUM_PLAYERS, TEAM_A_ID, TEAM_B_ID, MOVE_CAP, SHOOT_CAP, GOAL_WIDTH, iTeam, mapW, mapH, numTurns);
        for(int iTurn = 0; iTurn < numTurns; iTurn++) {
            int turn = sc.nextInt();
            int scoreTeamA = sc.nextInt();
            int scoreTeamB = sc.nextInt();
            int stateMatch = sc.nextInt();
            int ballX = sc.nextInt();
            int ballY = sc.nextInt();
            int ballSpeedX = sc.nextInt();
            int ballSpeedY = sc.nextInt();
            int[] iPlayerA = new int[NUM_PLAYERS];
            int[] playerAX = new int[NUM_PLAYERS];
            int[] playerAY = new int[NUM_PLAYERS];
            int[] iPlayerB = new int[NUM_PLAYERS];
            int[] playerBX = new int[NUM_PLAYERS];
            int[] playerBY = new int[NUM_PLAYERS];
            for(int i = 0; i < NUM_PLAYERS; i++) {
                iPlayerA[i] = sc.nextInt();
                playerAX[i] = sc.nextInt();
                playerAY[i] = sc.nextInt();
            }
            for(int i = 0; i < NUM_PLAYERS; i++) {
                iPlayerB[i] = sc.nextInt();
                playerBX[i] = sc.nextInt();
                playerBY[i] = sc.nextInt();
            }
            game.setTurn(turn);
            game.setState(stateMatch);
            game.getTeamA().setScore(scoreTeamA);
            game.getTeamB().setScore(scoreTeamB);
            game.getBall().getPos().set(ballX, ballY);
            game.getBall().getSpeed().set(ballSpeedX, ballSpeedY);
            for(int i = 0; i < NUM_PLAYERS; i++) {
                game.getTeamA().getPlayer(i).getPos().set(playerAX[i], playerAY[i]);
                game.getTeamB().getPlayer(i).getPos().set(playerBX[i], playerBY[i]);
            }
            game.play();
            System.out.print(game.getActionStr());
        }
    }

    private static String setInitialPosition(int mapW, int mapH) {
        StringBuilder result = new StringBuilder();
        result.append((mapW / 8 * 2) + " " + (mapH / 3) + " ");
        result.append((mapW / 8 * 2) + " " + (mapH / 3 * 2) + " ");
        result.append((mapW / 8 * 3) + " " + (mapH / 2) + " ");
        result.append((mapW / 8 * 3) + " " + (mapH / 3) + " ");
        result.append((mapW / 8 * 3) + " " + (mapH / 3 * 2) + " ");
        return result.toString();
    }
}

class Game {
    private Team teamA;
    private Team teamB;
    private Team me;
    private Team enemy;
    private Ball ball;
    private int state;
    private int mapW;
    private int mapH;
    private int numPlayers;
    private int moveCap;
    private int shootCap;
    private int numTurns;
    private int turn;
    private Action[] actions;

    public Game(int newNumPlayers, int iTeamA, int iTeamB, int newMoveCap, int newShootCap, int goalWidth, int iMe, int newMapW, int newMapH, int newNumTurns) {
        teamA = new Team(newNumPlayers, goalWidth);
        teamB = new Team(newNumPlayers, goalWidth);
        if(iMe == 0) {
            me = teamA;
            enemy = teamB;
        }
        else {
            me = teamB;
            enemy = teamA;
        }
        state = 0;
        ball = new Ball();
        mapW = newMapW;
        mapH = newMapH;
        numPlayers = newNumPlayers;
        moveCap = newMoveCap;
        shootCap = newShootCap;
        numTurns = newNumTurns;
        turn = 0;
        actions = new Action[numPlayers];
        for(int i = 0; i < numPlayers; i++) {
            actions[i] = new Action();
        }
    }

    public void setTurn(int newTurn) {
        turn = newTurn;
    }

    public void setState(int newState) {
        state = newState;
        if(state == 1) {
            teamA.getGoal().getPos().set(0, mapH / 2);
            teamB.getGoal().getPos().set(mapW, mapH / 2);
        }
        else if(state == 4) {
            teamA.getGoal().getPos().set(0, mapH / 2);
            teamB.getGoal().getPos().set(0, mapH / 2);
        }
        else {
            teamA.getGoal().getPos().set(mapW, mapH / 2);
            teamB.getGoal().getPos().set(0, mapH / 2);
        }
    }

    public Team getTeamA() {
        return teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public Ball getBall() {
        return ball;
    }

    public void play() {
        if(state == 4) {
            Random rand = new Random();
            for(int i = 0; i < numPlayers; i++) {
                actions[i].skip();
            }
            if(turn == 0 && me == teamA || turn == 1 && me == teamB) {
                if(isShootable(me.getPlayer(0))) {
                    int randDir = rand.nextInt(2);
                    if(randDir == 0) {
                        actions[0].shoot(enemy.getGoal().getLowerShootPoint(), 100);
                    }
                    else {
                        actions[0].shoot(enemy.getGoal().getUpperShootPoint(), 100);
                    }
                }
            }
            else {
                if(isShootable(me.getPlayer(0))) {
                    actions[0].shoot(new Position(mapW / 2, ball.getPos().getY()), 100);
                }
                else if(getCatchPoint() != null) {
                    actions[0].move(getCatchPoint());
                }
                else {
                    int randDir = rand.nextInt(2);
                    if(randDir == 0) {
                        actions[0].move(me.getGoal().getLowerShootPoint());
                    }
                    else {
                        actions[0].move(me.getGoal().getUpperShootPoint());
                    }
                }
            }
            return;
        }
        boolean[] usedPlayer = new boolean[numPlayers];
        boolean[] markedPlayer = new boolean[numPlayers];
        int[] mark = new int[numPlayers];
        Position[] destinations = new Position[numPlayers];
        Arrays.fill(usedPlayer, false);
        Arrays.fill(markedPlayer, false);
        int mainPlayer = 0;
        for(int i = 1; i < numPlayers; i++) {
            if(me.getPlayer(i).getPos().getSqrDist(ball.getNextPos(mapW, mapH)) < me.getPlayer(mainPlayer).getPos().getSqrDist(ball.getNextPos(mapW, mapH))) {
                mainPlayer = i;
            }
        }
        int enemyMainPlayer = 0;
        for(int i = 1; i < numPlayers; i++) {
            if(enemy.getPlayer(i).getPos().getSqrDist(ball.getNextPos(mapW, mapH)) < enemy.getPlayer(enemyMainPlayer).getPos().getSqrDist(ball.getNextPos(mapW, mapH))) {
                enemyMainPlayer = i;
            }
        }
        usedPlayer[mainPlayer] = true;
        int numThreats = 0;
        for(int i = 0; i < numPlayers; i++) {
            if(Math.abs(enemy.getPlayer(i).getPos().getX() - me.getGoal().getPos().getX()) <= mapW / 2) {
                numThreats++;
            }
        }
        int[] losing = {1, 1, 1, 2, 2, 3};
        int[] drawing = {1, 2, 2, 3, 3, 4};
        int[] winning = {2, 2, 3, 4, 4, 4};
        int numDefenders = 0;
        if(me.getScore() < enemy.getScore()) {
            numDefenders = losing[numThreats];
        }
        else if(me.getScore() == enemy.getScore()) {
            numDefenders = drawing[numThreats];
        }
        else {
            numDefenders = winning[numThreats];
        }
        for(int rep = 0; rep < numDefenders; rep++) {
            int threat = -1;
            for(int i = 0; i < numPlayers; i++) {
                if(!markedPlayer[i] && (threat == -1 || enemy.getPlayer(i).getPos().getSqrDist(me.getGoal().getAim(enemy.getPlayer(i).getPos())) <
                                                        enemy.getPlayer(threat).getPos().getSqrDist(me.getGoal().getAim(enemy.getPlayer(threat).getPos())))) {
                    threat = i;
                }
            }
            if(threat == -1) {
                break;
            }
            int marker = -1;
            for(int i = 0; i < numPlayers; i++) {
                if(!usedPlayer[i] && (marker == -1 || me.getPlayer(i).getPos().getSqrDist(enemy.getPlayer(threat).getPos()) <
                                                      me.getPlayer(marker).getPos().getSqrDist(enemy.getPlayer(threat).getPos()))) {
                    marker = i;
                }
            }
            mark[marker] = threat;
            usedPlayer[marker] = true;
            markedPlayer[threat] = true;
        }
        int minY = -1;
        for(int i = 0; i < numPlayers; i++) {
            if(!usedPlayer[i]) {
                if(minY == -1 || me.getPlayer(i).getPos().getY() < me.getPlayer(minY).getPos().getY()) {
                    minY = i;
                }
            }
        }
        int maxY = -1;
        for(int i = 0; i < numPlayers; i++) {
            if(!usedPlayer[i] && i != minY) {
                if(maxY == -1 || me.getPlayer(i).getPos().getY() > me.getPlayer(maxY).getPos().getY()) {
                    maxY = i;
                }
            }
        }
        int midY = -1;
        for(int i = 0; i < numPlayers; i++) {
            if(!usedPlayer[i] && i != minY && i != maxY) {
                if(midY == -1) {
                    midY = i;
                }
            }
        }
        if(numDefenders == 1) {
            destinations[minY] = getLowerDestination();
            destinations[midY] = getMiddleDestination();
            destinations[maxY] = getUpperDestination();
        }
        else if(numDefenders == 2) {
            destinations[minY] = getLowerDestination();
            destinations[maxY] = getUpperDestination();
        }
        else if(numDefenders == 3) {
            destinations[minY] = getMiddleDestination();
        }
        int striker = -1;
        for(int i = 0; i < numPlayers; i++) {
            if(striker == -1 || me.getPlayer(i).getPos().getSqrDist(enemy.getGoal().getBestShootPoint(numPlayers, enemy)) < 
                                me.getPlayer(striker).getPos().getSqrDist(enemy.getGoal().getBestShootPoint(numPlayers, enemy))) {
                striker = i;
            }
        }
        int secondStriker = -1;
        for(int i = 0; i < numPlayers; i++) {
            if(i != striker && !usedPlayer[i]) {
                if(secondStriker == -1 || me.getPlayer(i).getPos().getSqrDist(enemy.getGoal().getBestShootPoint(numPlayers, enemy)) < 
                                          me.getPlayer(secondStriker).getPos().getSqrDist(enemy.getGoal().getBestShootPoint(numPlayers, enemy))) {
                    secondStriker = i;
                }
            }
        }
        boolean isEnemyShootable = false;
        for(int i = 0; i < numPlayers; i++) {
            if(isShootable(enemy.getPlayer(i))) {
                isEnemyShootable = true;
            }
        }
        boolean backrun = false;
        for(int i = 0; i < numPlayers; i++) {
            if(isShootable(me.getPlayer(i))) {
                if(Math.abs(ball.getPos().getX() - enemy.getGoal().getPos().getX()) <= mapW / 7 * 2) {
                    actions[i].shoot(enemy.getGoal().getBestShootPoint(numPlayers, enemy), 100);
                }
                else if(Math.abs(ball.getPos().getX() - me.getGoal().getPos().getX()) <= mapW / 2 && isEnemyShootable) {
                    actions[i].shoot(new Position(enemy.getGoal().getPos().getX(), ball.getPos().getY()), 100);
                }
                else if(Math.abs(ball.getPos().getX() - me.getGoal().getPos().getX()) <= mapW / 4) {
                    if(ball.getPos().getY() < mapH / 2) {
                        actions[i].shoot(new Position(mapW / 2, 0), 100);
                    }
                    else {
                        actions[i].shoot(new Position(mapW / 2, mapH), 100);
                    }
                }
                // else if(me.getScore() >= enemy.getScore() && isEnemyShootable && !backrun && 
                //         Math.abs(ball.getPos().getX() - enemy.getGoal().getPos().getX()) <= mapW / 3 * 2) {
                //     backrun = true;
                //     actions[i].move(me.getGoal().getAim(ball.getPos()));
                // }
                else if(i == striker) {
                    if(secondStriker != -1) {
                        actions[i].shoot(getPassPoint(me.getPlayer(secondStriker)), 80);
                    }
                    else {
                        actions[i].shoot(enemy.getGoal().getBestShootPoint(numPlayers, enemy), 60);
                    }
                }
                else {
                    actions[i].shoot(getPassPoint(me.getPlayer(striker)), 100);
                }
            }
            else {
                if(i == mainPlayer) {
                    // if(me.getScore() >= enemy.getScore() && isEnemyShootable && Math.abs(ball.getPos().getX() - me.getGoal().getPos().getX()) <= mapW / 2) {
                    //     actions[i].move(me.getGoal().getAim(ball.getPos()));
                    // }
                    actions[i].move(ball.getNextPos(mapW, mapH));
                }
                else {
                    if(usedPlayer[i]) {
                        if(mark[i] == enemyMainPlayer && me.getScore() >= enemy.getScore()) {
                            actions[i].move(getMarkPoint2(enemy.getPlayer(mark[i])));
                        }
                        else {
                            actions[i].move(getMarkPoint(enemy.getPlayer(mark[i])));
                        }
                    }
                    else {
                        actions[i].move(destinations[i]);
                    }
                }
            }
        }
    }

    public String getActionStr() {
        StringBuilder result = new StringBuilder();
        for(int iPlayer = 0; iPlayer < numPlayers; iPlayer++) {
            result.append(actions[iPlayer].getStr() + " ");
        }
        return result.toString();
    }

    private boolean isShootable(Player player) {
        return player.getPos().getSqrDist(ball.getPos()) <= shootCap * shootCap;
    }

    private Position getMarkPoint(Player player) {
        int difX = ball.getNextPos(mapW, mapH).getX() - player.getPos().getX();
        int difY = ball.getNextPos(mapW, mapH).getY() - player.getPos().getY();
        return new Position(player.getPos().getX() + difX / 10, player.getPos().getY() + difY / 10);
    }

    private Position getMarkPoint2(Player player) {
        int difX = me.getGoal().getPos().getX() - player.getPos().getX();
        int difY = me.getGoal().getPos().getY() - player.getPos().getY();
        return new Position(player.getPos().getX() + difX / 8, player.getPos().getY() + difY / 8);
    }

    private Position getLowerDestination() {
        if(ball.getPos().getX() >= enemy.getGoal().getPos().getX()) {
            return new Position(Math.max(ball.getPos().getX() - mapW / 6, enemy.getGoal().getPos().getX() + mapW / 6), mapH / 3);
        }
        else {
            return new Position(Math.min(ball.getPos().getX() + mapW / 6, enemy.getGoal().getPos().getX() - mapW / 6), mapH / 3);
        }
    }

    private Position getMiddleDestination() {
        if(ball.getPos().getX() >= enemy.getGoal().getPos().getX()) {
            return new Position(Math.max(ball.getPos().getX() - mapW / 5, enemy.getGoal().getPos().getX() + mapW / 6), mapH / 2);
        }
        else {
            return new Position(Math.min(ball.getPos().getX() + mapW / 5, enemy.getGoal().getPos().getX() - mapW / 6), mapH / 2);
        }
    }

    private Position getUpperDestination() {
        if(ball.getPos().getX() >= enemy.getGoal().getPos().getX()) {
            return new Position(Math.max(ball.getPos().getX() - mapW / 5, enemy.getGoal().getPos().getX() + mapW / 6), mapH / 3 * 2);
        }
        else {
            return new Position(Math.min(ball.getPos().getX() + mapW / 5, enemy.getGoal().getPos().getX() - mapW / 6), mapH / 3 * 2);
        }
    }

    private Position getPassPoint(Player player) {
        int difX = enemy.getGoal().getBestShootPoint(numPlayers, enemy).getX() - player.getPos().getX();
        int difY = enemy.getGoal().getBestShootPoint(numPlayers, enemy).getY() - player.getPos().getY();
        return new Position(player.getPos().getX() + difX / 6, player.getPos().getY() + difY / 6);
    }

    private int getF(Position start, Position end, double alpha) {
        return Math.min((int)(Math.sqrt(Math.sqrt(start.getSqrDist(end))) * alpha), 100);
    }

    private Position getCatchPoint() {
        if(ball.getSpeed().getX() * (me.getGoal().getPos().getX() - ball.getPos().getX()) <= 0) {
            return null;
        }
        int difX = me.getGoal().getPos().getX() - ball.getPos().getX();
        int difY = (int)((double)difX / ball.getSpeed().getX() * ball.getSpeed().getY());
        return new Position(ball.getPos().getX() + difX, ball.getPos().getY() + difY);
    }
}

class Team {
    private int numPlayers;
    private Player[] players;
    private int score;
    private Goal goal;

    public Team(int newNumPlayers, int goalWidth) {
        numPlayers = newNumPlayers;
        players = new Player[newNumPlayers];
        for(int i = 0; i < newNumPlayers; i++) {
            players[i] = new Player();
        }
        score = 0;
        goal = new Goal(goalWidth);
    }

    public void setScore(int newScore) {
        score = newScore;
    }

    public int getScore() {
        return score;
    }

    public Player getPlayer(int iPlayer) {
        return players[iPlayer];
    }

    public Goal getGoal() {
        return goal;
    }
}

class Ball {
    private Position pos;
    private Speed speed;

    public Ball() {
        pos = new Position();
        speed = new Speed();
    }

    public Position getPos() {
        return pos;
    }

    public Speed getSpeed() {
        return speed;
    }

    public Position getNextPos(int mapW, int mapH) {
        int x = pos.getX() + speed.getX();
        int y = pos.getY() + speed.getY();
        if(x < 0) {
            x = -x;
        }
        if(y < 0) {
            y = -y;
        }
        if(x > mapW) {
            x = mapW - (x - mapW);
        }
        if(y > mapH) {
            y = mapH - (y - mapH);
        }
        return new Position(x, y);
    }
}

class Action {
    private int type;
    private int x;
    private int y;
    private int f;

    public Action() {
        type = 0;
        x = 0;
        y = 0;
        f = 0;
    }

    public void move(Position pos) {
        type = 1;
        x = pos.getX();
        y = pos.getY();
        f = 0;
    }

    public void shoot(Position pos, int newF) {
        type = 2;
        x = pos.getX();
        y = pos.getY();
        f = newF;
    }

    public void skip() {
        type = 0;
        x = 0;
        y = 0;
        f = 0;
    }

    public String getStr() {
        return type + " " + x + " " + y + " " + f;
    }
}

class Player {
    private Position pos;

    public Player() {
        pos = new Position();
    }

    public Position getPos() {
        return pos;
    }
}

class Goal {
    private Position pos;
    private int width;

    public Goal(int newWidth) {
        width = newWidth;
        pos = new Position();
    }

    public Position getPos() {
        return pos;
    }

    public int getWidth() {
        return width;
    }

    public Position getAim(Position shooter) {
        if(shooter.getY() < pos.getY() - width / 4) {
            return new Position(pos.getX(), pos.getY() - width / 4);
        }
        if(shooter.getY() > pos.getY() + width / 4) {
            return new Position(pos.getX(), pos.getY() + width / 4);
        }
        return new Position(pos.getX(), shooter.getY());
    }

    public Position getBestShootPoint(int numPlayers, Team defender) {
        Position[] candidates = new Position[] {pos, new Position(pos.getX(), pos.getY() - width / 4), new Position(pos.getX(), pos.getY() + width / 4)};
        int[] dists = new int[] {-1, -1, -1};
        for(int i = 0; i < numPlayers; i++) {
            for(int j = 0; j < 3; j++) {
                if(dists[j] == -1 || defender.getPlayer(i).getPos().getSqrDist(candidates[j]) < dists[j]) {
                    dists[j] = defender.getPlayer(i).getPos().getSqrDist(candidates[j]);
                }
            }
        }
        if(dists[0] > dists[1] && dists[0] > dists[2]) {
            return candidates[0];
        }
        else if(dists[1] > dists[2]) {
            return candidates[1];
        }
        else {
            return candidates[2];
        }
    }

    public Position getLowerShootPoint() {
        return new Position(pos.getX(), pos.getY() - width / 4);
    }

    public Position getUpperShootPoint() {
        return new Position(pos.getX(), pos.getY() + width / 4);
    }
}

class Position {
    private int x;
    private int y;

    public Position() {
        x = 0;
        y = 0;
    }

    public Position(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void set(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public int getSqrDist(Position other) {
        return (other.x - x) * (other.x - x) + (other.y - y) * (other.y - y);
    }
}

class Speed {
    private int x;
    private int y;

    public Speed() {
        x = 0;
        y = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void set(int newX, int newY) {
        x = newX;
        y = newY;
    }
}