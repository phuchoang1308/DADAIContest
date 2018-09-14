import java.util.*;

public class AIContest2018 {
    private static final int NUM_PLAYERS = 5;
    private static final int TEAM_A_ID = 0;
    private static final int TEAM_B_ID = 1;
    private static final int MOVE_CAP = 300;
    private static final int SHOOT_CAP = 200;
    private static final int GOAL_WIDTH = 3000;
    private static final int BALL_ACC = 120;
    private static final int F_MUL = 10;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print(setInitialPosition(14400, 9600));
        int iTeam = sc.nextInt();
        int mapW = sc.nextInt();
        int mapH = sc.nextInt();
        int numTurns = sc.nextInt();
        Game game = new Game(NUM_PLAYERS, TEAM_A_ID, TEAM_B_ID, MOVE_CAP, SHOOT_CAP, GOAL_WIDTH, BALL_ACC, F_MUL, iTeam, mapW, mapH, numTurns);
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
            game.getBall().setPos(new Position(ballX, ballY));
            game.getBall().setSpeed(new Position(ballSpeedX, ballSpeedY));
            for(int i = 0; i < NUM_PLAYERS; i++) {
                game.getTeamA().getPlayer(i).setPos(new Position(playerAX[i], playerAY[i]));
                game.getTeamB().getPlayer(i).setPos(new Position(playerBX[i], playerBY[i]));
            }
            game.play();
            System.out.print(game.getActionStr());
        }
    }

    private static String setInitialPosition(int mapW, int mapH) {
        StringBuilder result = new StringBuilder();
        result.append((mapW / 8 * 3) + " " + (mapH / 8 * 2) + " ");
        result.append((mapW / 8 * 3) + " " + (mapH / 8 * 3) + " ");
        result.append((mapW / 8 * 3) + " " + (mapH / 8 * 4) + " ");
        result.append((mapW / 8 * 3) + " " + (mapH / 8 * 5) + " ");
        result.append((mapW / 8 * 3) + " " + (mapH / 8 * 6) + " ");
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
    private int moveCap;
    private int shootCap;
    private int fMul;
    private int mapW;
    private int mapH;
    private int numTurns;
    private int turn;

    public Game(int numPlayers, int iTeamA, int iTeamB, int newMoveCap, int newShootCap, int goalWidth, int ballAcc, int newFMul, int iMe, int newMapW, int newMapH, int newNumTurns) {
        teamA = new Team(numPlayers, goalWidth);
        teamB = new Team(numPlayers, goalWidth);
        if(iMe == 0) {
            me = teamA;
            enemy = teamB;
        }
        else {
            me = teamB;
            enemy = teamA;
        }
        ball = new Ball(ballAcc);
        state = 0;
        moveCap = newMoveCap;
        shootCap = newShootCap;
        fMul = newFMul;
        mapW = newMapW;
        mapH = newMapH;
        numTurns = newNumTurns;
        turn = 0;
    }

    public void setTurn(int newTurn) {
        turn = newTurn;
    }

    public void setState(int newState) {
        state = newState;
        if(state == 1) {
            teamA.getGoal().setPos(new Position(0, mapH / 2));
            teamB.getGoal().setPos(new Position(mapW, mapH / 2));
        }
        else if(state == 4) {
            teamA.getGoal().setPos(new Position(0, mapH / 2));
            teamB.getGoal().setPos(new Position(0, mapH / 2));
        }
        else {
            teamA.getGoal().setPos(new Position(mapW, mapH / 2));
            teamB.getGoal().setPos(new Position(0, mapH / 2));
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

    public String getActionStr() {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < me.getNumPlayers(); i++) {
            result.append(me.getPlayer(i).getAction().getStr() + " ");
        }
        return result.toString();
    }


    public void play() {
        final int THREAT_RADIUS = 7200;
        final int CENTER_POINT_RADIUS = 3500;
        final int DIRECTION_RADIUS = 10;
        final int MARK_RADIUS = 300;
        final int[] NUM_DEFENDERS = new int[] {2, 2, 2, 3, 3, 4};
        final int[] FORCES = new int[] {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100};
        final Position UPPER_TARGET = enemy.getGoal().getPos().getMoveToward(new Position(mapW / 2, mapH), 3000);
        final Position MIDDLE_TARGET = enemy.getGoal().getPos().getMoveToward(new Position(mapW / 2, mapH / 2), 2400);
        final Position LOWER_TARGET = enemy.getGoal().getPos().getMoveToward(new Position(mapW / 2, mapH), 3000);
        int numThreats = 0;
        for(int i = 0; i < enemy.getNumPlayers(); i++) {
            if(me.getGoal().getDist(enemy.getPlayer(i).getPos()) <= THREAT_RADIUS) {
                numThreats++;
            }
        }
        boolean isEnemyShootable = false;
        for(int i = 0; i < enemy.getNumPlayers(); i++) {
            if(enemy.getPlayer(i).getPos().getDist(ball.getPos()) <= shootCap) {
                isEnemyShootable = true;
                break;
            }
        }
        int numDefenders = 4;
        if(!isEnemyShootable) {
            numDefenders = NUM_DEFENDERS[numThreats];
        }
        boolean[] usedPlayer = new boolean[me.getNumPlayers()];
        Arrays.fill(usedPlayer, false);
        boolean[] markedPlayer = new boolean[me.getNumPlayers()];
        Arrays.fill(markedPlayer, false);
        Position[] target = new Position[me.getNumPlayers()];
        Arrays.fill(target, null);
        List<Position> directions = new ArrayList<>();
        for(int x = -DIRECTION_RADIUS; x <= DIRECTION_RADIUS; x++) {
            for(int y = -DIRECTION_RADIUS; y <= DIRECTION_RADIUS; y++) {
                if(Math.abs(x) == DIRECTION_RADIUS || Math.abs(y) == DIRECTION_RADIUS) {
                    directions.add(new Position(x, y));
                }
            }
        }
        CenterPoint centerPoint = getBestCenterPoint(me, enemy, ball, moveCap, shootCap, mapW, mapH, null);
        Player mainPlayer = centerPoint.getPlayerA();
        usedPlayer[mainPlayer.getId()] = true;
        target[mainPlayer.getId()] = centerPoint.getBall().getPos();
        if(centerPoint.getState() == CenterPointState.CLASH || centerPoint.getState() == CenterPointState.B_POSSESS || centerPoint.getState() == CenterPointState.B_GOAL || centerPoint.getState() == CenterPointState.NON_POSSESS) {
            numDefenders = 4;
        }
        for(int iDefender = 0; iDefender < numDefenders; iDefender++) {
            Player mainThreat = null;
            double minDist = -1;
            for(int i = 0; i < enemy.getNumPlayers(); i++) {
                if(!markedPlayer[i]) {
                    if(mainThreat == null || me.getGoal().getDist(enemy.getPlayer(i).getPos()) < minDist) {
                        mainThreat = enemy.getPlayer(i);
                        minDist = me.getGoal().getDist(enemy.getPlayer(i).getPos());
                    }
                }
            }
            markedPlayer[mainThreat.getId()] = true;
            Player defender = null;
            minDist = -1;
            for(int i = 0; i < me.getNumPlayers(); i++) {
                if(!usedPlayer[i]) {
                    if(defender == null || mainThreat.getPos().getDist(me.getPlayer(i).getPos()) < minDist) {
                        defender = me.getPlayer(i);
                        minDist = mainThreat.getPos().getDist(me.getPlayer(i).getPos());
                    }
                }
            }
            usedPlayer[defender.getId()] = true;
            target[defender.getId()] = mainThreat.getPos().getMoveToward(centerPoint.getBall().getPos(), 300);
        }
        Player attacker1 = null;
        Player attacker2 = null;
        int yAttacker1 = -1;
        int yAttacker2 = -1;
        for(int i = 0; i < me.getNumPlayers(); i++) {
            if(!usedPlayer[me.getPlayer(i).getId()]) {
                if(attacker1 == null || me.getPlayer(i).getPos().getY() < yAttacker1) {
                    attacker2 = attacker1;
                    attacker1 = me.getPlayer(i);
                    yAttacker2 = yAttacker1;
                    yAttacker1 = me.getPlayer(i).getPos().getY();
                }
                else if(attacker2 == null || me.getPlayer(i).getPos().getY() < yAttacker2) {
                    attacker2 = me.getPlayer(i);
                    yAttacker2 = me.getPlayer(i).getPos().getY();
                }
            }
        }
        if(attacker2 != null) {
            target[attacker1.getId()] = centerPoint.getBall().getPos().getMoveToward(LOWER_TARGET, CENTER_POINT_RADIUS);
            target[attacker2.getId()] = centerPoint.getBall().getPos().getMoveToward(UPPER_TARGET, CENTER_POINT_RADIUS);
        }
        else if(attacker1 != null) {
            target[attacker1.getId()] = centerPoint.getBall().getPos().getMoveToward(MIDDLE_TARGET, CENTER_POINT_RADIUS);
        }
        for(int i = 0; i < me.getNumPlayers(); i++) {
            if(isEnemyShootable && me.getPlayer(i).getPos().getDist(ball.getPos()) <= shootCap) {
                me.getPlayer(i).getAction().shoot(new Position(enemy.getGoal().getPos().getX(), me.getPlayer(i).getPos().getY()), 100);
            }
            else if(me.getPlayer(i) == mainPlayer) {
                if(me.getPlayer(i).getPos().getDist(ball.getPos()) <= shootCap && centerPoint.getTurn() == 0) {
                    Position bestDestination = null;
                    int bestForce = -1;
                    CenterPoint bestCenterPoint = null;
                    for(Position direction : directions) {
                        for(int force : FORCES) {
                            Position destination = ball.getPos().plus(direction);
                            Ball newBall = new Ball(ball.getPos(), new Position(0, 0).getMoveOver(direction, force * fMul + ball.getAcc()), ball.getAcc());
                            CenterPoint newCenterPoint = getBestCenterPoint(me, enemy, newBall, moveCap, shootCap, mapW, mapH, me.getPlayer(i));
                            if(bestDestination == null || newCenterPoint.isBetter(bestCenterPoint)) {
                                bestDestination = destination;
                                bestForce = force;
                                bestCenterPoint = newCenterPoint;
                            }
                        }
                    }
                    if(bestCenterPoint.getState() == CenterPointState.A_GOAL) {
                        bestForce = 100;
                    }
                    me.getPlayer(i).getAction().shoot(bestDestination, bestForce);
                }
                else {
                    me.getPlayer(i).getAction().move(target[i]);
                }
            }
            else {
                me.getPlayer(i).getAction().move(target[i]);
            }
        }
    }

    private CenterPoint getBestCenterPoint(Team me, Team enemy, Ball ball, int moveCap, int shootCap, int mapW, int mapH, Player shooter) {
        CenterPoint result = null;
        int numTurns = 0;
        while(true) {
            Player mePlayer = getClosestPlayer(me, ball.getPos(), moveCap, shooter);
            Player enemyPlayer = getClosestPlayer(enemy, ball.getPos(), moveCap, shooter);
            boolean isMeReach = canReach(mePlayer.getPos(), ball.getPos(), numTurns, moveCap, shootCap, mePlayer == shooter);
            boolean isEnemyReach = canReach(enemyPlayer.getPos(), ball.getPos(), numTurns, moveCap, shootCap, enemyPlayer == shooter);
            CenterPointState state = null;
            if(isMeReach && !isEnemyReach) {
                state = CenterPointState.A_POSSESS;
            }
            else if(isMeReach && isEnemyReach) {
                state = CenterPointState.CLASH;
            }
            else if(!isMeReach && isEnemyReach) {
                state = CenterPointState.B_POSSESS;
            }
            else {
                state = CenterPointState.NON_POSSESS;
            }
            CenterPoint candidate = new CenterPoint(state, me, enemy, ball, mePlayer, enemyPlayer, numTurns);
            if(result == null || candidate.isBetter(result)) {
                result = candidate;
            }
            if(isEnemyReach) {
                break;
            }
            if(ball.isNextGoal(enemy.getGoal())) {
                candidate = new CenterPoint(CenterPointState.A_GOAL, me, enemy, ball, mePlayer, enemyPlayer, numTurns);
                if(result == null || candidate.isBetter(result)) {
                    result = candidate;
                }
                break;
            }
            if(ball.isNextGoal(me.getGoal())) {
                candidate = new CenterPoint(CenterPointState.B_GOAL, me, enemy, ball, mePlayer, enemyPlayer, numTurns);
                if(result == null || candidate.isBetter(result)) {
                    result = candidate;
                }
                break;
            }
            if(ball.hasNextBall()) {
                ball = ball.getNextBall(mapW, mapH);
                numTurns++;
            }
            else {
                break;
            }
        }
        return result;
    }

    private Player getClosestPlayer(Team team, Position pos, int moveCap, Player shooter) {
        Player result = null;
        double minDist = 0;
        for(int i = 0; i < team.getNumPlayers(); i++) {
            double newMinDist = team.getPlayer(i).getPos().getDist(pos);
            if(team.getPlayer(i) == shooter) {
                newMinDist += moveCap;
            }
            if(result == null || newMinDist < minDist) {
                result = team.getPlayer(i);
                minDist = newMinDist;
            }
        }
        return result;
    }

    private boolean canReach(Position start, Position end, int numTurnsLimit, int moveCap, int shootCap, boolean isShooter) {
        if(isShooter) {
            numTurnsLimit--;
        }
        while(numTurnsLimit >= 0) {
            if(start.getDist(end) <= shootCap) {
                return true;
            }
            start = start.getMoveToward(end, moveCap);
            numTurnsLimit--;
        }
        return false;
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
            players[i] = new Player(i);
        }
        score = 0;
        goal = new Goal(goalWidth);
    }

    public void setScore(int newScore) {
        score = newScore;
    }

    public int getNumPlayers() {
        return numPlayers;
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
    private Position speed;
    private int acc;

    public Ball(int newAcc) {
        pos = new Position();
        speed = new Position();
        acc = newAcc;
    }

    public Ball(Position newPos, Position newSpeed, int newAcc) {
        pos = newPos;
        speed = newSpeed;
        acc = newAcc;
    }

    public void setPos(Position newPos) {
        pos = newPos;
    }

    public void setSpeed(Position newSpeed) {
        speed = newSpeed;
    }

    public Position getPos() {
        return pos;
    }

    public Position getSpeed() {
        return speed;
    }

    public int getAcc() {
        return acc;
    }

    public boolean hasNextBall() {
        Position newSpeed = speed.getMoveToward(new Position(0, 0), acc);
        return newSpeed.getX() != 0 || newSpeed.getY() != 0;
    }

    public boolean isNextGoal(Goal goal) {
        Position newSpeed = speed.getMoveToward(new Position(0, 0), acc);
        Position newPos = pos.plus(newSpeed);
        Position lowerGoal = new Position(goal.getPos().getX(), goal.getPos().getY() - goal.getWidth() / 2);
        Position upperGoal = new Position(goal.getPos().getX(), goal.getPos().getY() + goal.getWidth() / 2);
        Position mid = newPos.subtract(pos);
        Position left = lowerGoal.subtract(pos);
        Position right = upperGoal.subtract(pos);
        if(mid.multiply(left) * mid.multiply(right) > 0) {
            return false;
        }
        mid = upperGoal.subtract(lowerGoal);
        left = pos.subtract(lowerGoal);
        right = newPos.subtract(upperGoal);
        if(mid.multiply(left) * mid.multiply(right) > 0) {
            return false;
        }
        return true;
    }

    public Ball getNextBall(int mapW, int mapH) {
        Position newSpeed = speed.getMoveToward(new Position(0, 0), acc);
        int newX = pos.getX() + newSpeed.getX();
        int newY = pos.getY() + newSpeed.getY();
        if(newX < 0) {
            newX = -newX;
        }
        if(newY < 0) {
            newY = -newY;
        }
        if(newX > mapW) {
            newX = mapW - (newX - mapW);
        }
        if(newY > mapH) {
            newY = mapH - (newY - mapH);
        }
        Position newPos = new Position(newX, newY);
        return new Ball(newPos, newSpeed, acc);
    }

    public Position getPosInGoal(Goal goal) {
        Position newSpeed = speed.getMoveToward(new Position(0, 0), acc);
        Position newPos = pos.plus(newSpeed);
        return pos.getMoveOver(newPos, (double)Math.abs(pos.getX() - goal.getPos().getX()) / Math.abs(pos.getX() - newPos.getX()) * pos.getDist(newPos));
    }
}

class Player {
    private Position pos;
    private Action action;
    private int id;

    public Player(int newId) {
        pos = new Position();
        action = new Action();
        id = newId;
    }

    public void setPos(Position newPos) {
        pos = newPos;
    }

    public Position getPos() {
        return pos;
    }

    public Action getAction() {
        return action;
    }

    public int getId() {
        return id;
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

    public void skip() {
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

    public String getStr() {
        return type + " " + x + " " + y + " " + f;
    }
}

class Goal {
    private Position pos;
    private int width;

    public Goal(int newWidth) {
        width = newWidth;
        pos = new Position();
    }

    public void setPos(Position newPos) {
        pos = newPos;
    }

    public Position getPos() {
        return pos;
    }

    public int getWidth() {
        return width;
    }

    public double getDist(Position otherPos) {
        if(otherPos.getY() < pos.getY() - width / 2) {
            return otherPos.getDist(new Position(pos.getX(), pos.getY() - width / 2));
        }
        else if(otherPos.getY() > pos.getY() + width / 2) {
            return otherPos.getDist(new Position(pos.getX(), pos.getY() + width / 2));
        }
        else {
            return Math.abs(pos.getX() - otherPos.getX());
        }
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

    public double getDist(Position other) {
        return Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y));
    }

    public Position plus(Position other) {
        return new Position(x + other.x, y + other.y);
    }

    public Position subtract(Position other) {
        return new Position(x - other.x, y - other.y);
    }

    public long multiply(Position other) {
        return (long)x * other.y - (long)y * other.x;
    }

    public Position getMoveToward(Position other, double moveLength) {
        if(moveLength >= getDist(other)) {
            return other;
        }
        int newX = (int)(x + (other.x - x) * moveLength / getDist(other));
        int newY = (int)(y + (other.y - y) * moveLength / getDist(other));
        return new Position(newX, newY);
    }

    public Position getMoveOver(Position other, double moveLength) {
        int newX = (int)(x + (other.x - x) * moveLength / getDist(other));
        int newY = (int)(y + (other.y - y) * moveLength / getDist(other));
        return new Position(newX, newY);
    }
}

class CenterPoint {
    private CenterPointState state;
    private Team teamA;
    private Team teamB;
    private Ball ball;
    private Player playerA;
    private Player playerB;
    private int turn;

    public CenterPoint(CenterPointState newState, Team newTeamA, Team newTeamB, Ball newBall, Player newPlayerA, Player newPlayerB, int newTurn) {
        state = newState;
        teamA = newTeamA;
        teamB = newTeamB;
        ball = newBall;
        playerA = newPlayerA;
        playerB = newPlayerB;
        turn = newTurn;
    }

    public CenterPointState getState() {
        return state;
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

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public int getTurn() {
        return turn;
    }

    public boolean isBetter(CenterPoint other) {
        if(state != other.state) {
            return state.ordinal() < other.state.ordinal();
        }
        else if(state == CenterPointState.A_GOAL) {
            return teamB.getGoal().getPos().getDist(ball.getPosInGoal(teamB.getGoal())) < other.teamB.getGoal().getPos().getDist(other.ball.getPosInGoal(other.teamB.getGoal()));
        }
        else if(state == CenterPointState.NON_POSSESS) {
            return turn > other.turn;
        }
        else {
            return teamB.getGoal().getDist(ball.getPos()) < other.teamB.getGoal().getDist(other.ball.getPos());
        }
    }
}

enum CenterPointState {
    A_GOAL, A_POSSESS, CLASH, B_POSSESS, B_GOAL, NON_POSSESS
}