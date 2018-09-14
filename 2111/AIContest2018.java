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
        final int DIRECTION_RADIUS = 5;
        final int MARK_RADIUS = 500;
        final int[] NUM_DEFENDERS = new int[] {1, 1, 2, 2, 2, 3};
        final int[] FORCES = new int[] {0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100};
        final Position LOWER_TARGET = enemy.getGoal().getPos().plus(enemy.getGoal().getPos().plus(new Position(0, -1500)).getMoveToward(new Position(mapW / 2, -1500), 1500));
        final Position MIDDLE_TARGET = enemy.getGoal().getPos().getMoveToward(new Position(mapW / 2, mapH / 2), 1500);
        final Position UPPER_TARGET = enemy.getGoal().getPos().plus(enemy.getGoal().getPos().plus(new Position(0, 1500)).getMoveToward(new Position(mapW / 2, 1500), 1500));
        final Position LOWER_SHOOT_TARGET = enemy.getGoal().getPos().plus(new Position(0, -1200));
        final Position UPPER_SHOOT_TARGET = enemy.getGoal().getPos().plus(new Position(0, 1200));
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
        int numDefenders = 3;
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
        for(int i = -DIRECTION_RADIUS; i < DIRECTION_RADIUS; i++) {
            directions.add(new Position(i, -DIRECTION_RADIUS));
        }
        for(int i = -DIRECTION_RADIUS; i < DIRECTION_RADIUS; i++) {
            directions.add(new Position(DIRECTION_RADIUS, i));
        }
        for(int i = -DIRECTION_RADIUS; i < DIRECTION_RADIUS; i++) {
            directions.add(new Position(-i, DIRECTION_RADIUS));
        }
        for(int i = -DIRECTION_RADIUS; i < DIRECTION_RADIUS; i++) {
            directions.add(new Position(-DIRECTION_RADIUS, -i));
        }
        CenterPoint centerPoint = getBestCenterPoint(me, enemy, ball, moveCap, shootCap, mapW, mapH, null);
        if(state == 4) {
            Random random = new Random();
            Player mainPlayer = me.getPlayer(0);
            for(int i = 0; i < me.getNumPlayers(); i++) {
                me.getPlayer(i).getAction().skip();
            }
            if(turn == 0 && me == teamA || turn == 1 && me == teamB) {
                int randDir = random.nextInt(2);
                if(randDir == 0) {
                    mainPlayer.getAction().shoot(LOWER_SHOOT_TARGET, 100);
                }
                else {
                    mainPlayer.getAction().shoot(UPPER_SHOOT_TARGET, 100);
                }
            }
            else {
                if(mainPlayer.getPos().getDist(ball.getPos()) <= shootCap) {
                    mainPlayer.getAction().shoot(new Position(mapW / 2, ball.getPos().getY()), 100);
                }
                else if(!isEnemyShootable) {
                    mainPlayer.getAction().move(centerPoint.getBall().getPos());
                }
                else {
                    int randDir = random.nextInt(2);
                    Ball predictBall = new Ball(ball.getPos(), new Position(0, 0).getMoveOver(LOWER_SHOOT_TARGET.subtract(ball.getPos()), 100 * fMul + ball.getAcc()), ball.getAcc());
                    CenterPoint predictCenterPoint = getBestCenterPoint(me, enemy, predictBall, moveCap, shootCap, mapW, mapH, enemy.getPlayer(0));
                    mainPlayer.getAction().move(predictCenterPoint.getBall().getPos());
                }
            }
            return;
        }
        Player mainPlayer = centerPoint.getPlayerA();
        usedPlayer[mainPlayer.getId()] = true;
        target[mainPlayer.getId()] = centerPoint.getBall().getPos();
        if(mainPlayer.getPos().getDist(ball.getPos()) <= shootCap && centerPoint.getTurn() == 0) {
            if(isEnemyShootable) {
                mainPlayer.getAction().shoot(new Position(enemy.getGoal().getPos().getX(), mainPlayer.getPos().getY()), 100);
            }
            else {
                Position bestDestination = null;
                int bestForce = -1;
                centerPoint = null;
                CenterPoint[] newCenterPoints2 = new CenterPoint[directions.size()];
                int[] newForces = new int[directions.size()];
                for(int j = 0; j < directions.size(); j++) {
                    Position direction = directions.get(j);
                    CenterPoint[] newCenterPoints = new CenterPoint[FORCES.length];
                    for(int i = 0; i < FORCES.length; i++) {
                        int force = FORCES[i];
                        Ball newBall = new Ball(ball.getPos(), new Position(0, 0).getMoveOver(direction, force * fMul + ball.getAcc()), ball.getAcc());
                        newCenterPoints[i] = getBestCenterPoint(me, enemy, newBall, moveCap, shootCap, mapW, mapH, mainPlayer);
                    }
                    for(int i = 0; i < newCenterPoints.length; i++) {
                        CenterPoint newCenterPoint = newCenterPoints[i];
                        int newForce = FORCES[i];
                        if(i - 1 >= 0 && newCenterPoint.isBetter(newCenterPoints[i - 1])) {
                            newCenterPoint = newCenterPoints[i - 1];
                            newForce = FORCES[i - 1];
                        }
                        if(i + 1 < newCenterPoints.length && newCenterPoint.isBetter(newCenterPoints[i + 1])) {
                            newCenterPoint = newCenterPoints[i + 1];
                            newForce = FORCES[i + 1];
                        }
                        if(newCenterPoints2[j] == null || newCenterPoint.isBetter(newCenterPoints2[j])) {
                            newCenterPoints2[j] = newCenterPoint;
                            newForces[j] = newForce;
                        }
                    }
                }
                for(int j = 0; j < directions.size(); j++) {
                    Position destination = ball.getPos().plus(directions.get(j));
                    CenterPoint newCenterPoint = newCenterPoints2[j];
                    int newForce = newForces[j];
                    if(newCenterPoint.isBetter(newCenterPoints2[(j - 1 + directions.size()) % directions.size()])) {
                        newCenterPoint = newCenterPoints2[(j - 1 + directions.size()) % directions.size()];
                        newForce = newForces[(j - 1 + directions.size()) % directions.size()];
                    }
                    if(newCenterPoint.isBetter(newCenterPoints2[(j + 1) % directions.size()])) {
                        newCenterPoint = newCenterPoints2[(j + 1) % directions.size()];
                        newForce = newForces[(j + 1) % directions.size()];
                    }
                    if(bestDestination == null || newCenterPoint.isBetter(centerPoint)) {
                        bestDestination = destination;
                        bestForce = newForce;
                        centerPoint = newCenterPoint;
                    }
                }
                if(centerPoint.getState() == CenterPointState.A_GOAL) {
                    bestForce = 100;
                }
                mainPlayer.getAction().shoot(bestDestination, bestForce);
            }
        }
        else {
            mainPlayer.getAction().move(target[mainPlayer.getId()]);
        }
        if(centerPoint.getState() == CenterPointState.CLASH || centerPoint.getState() == CenterPointState.B_POSSESS || centerPoint.getState() == CenterPointState.B_GOAL || centerPoint.getState() == CenterPointState.NON_POSSESS) {
            numDefenders = 3;
        }
        Position sweepPoint = centerPoint.getBall().getPos().getMoveToward(me.getGoal().getPos(), CENTER_POINT_RADIUS);
        Player sweeper = null;
        double minSweepDist = -1;
        for(int i = 0; i < me.getNumPlayers(); i++) {
            if(!usedPlayer[i]) {
                if(sweeper == null || me.getPlayer(i).getPos().getDist(sweepPoint) < minSweepDist) {
                    sweeper = me.getPlayer(i);
                    minSweepDist = me.getPlayer(i).getPos().getDist(sweepPoint);
                }
            }
        }
        usedPlayer[sweeper.getId()] = true;
        target[sweeper.getId()] = sweepPoint;
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
            if(me.getPlayer(i) != mainPlayer) {
                if(isEnemyShootable && me.getPlayer(i).getPos().getDist(ball.getPos()) <= shootCap) {
                    me.getPlayer(i).getAction().shoot(new Position(enemy.getGoal().getPos().getX(), me.getPlayer(i).getPos().getY()), 100);
                }
                else {
                    me.getPlayer(i).getAction().move(target[i]);
                }
            }
        }
    }

    private CenterPoint getBestCenterPoint(Team me, Team enemy, Ball ball, int moveCap, int shootCap, int mapW, int mapH, Player shooter) {
        CenterPoint result = null;
        int numTurns = 0;
        while(true) {
            Player mePlayer = getClosestPlayer(me, ball.getPos(), moveCap, shooter);
            Player enemyPlayer = getClosestPlayer(enemy, ball.getPos(), moveCap, shooter);
            int meNumTurns = -1;
            int enemyNumTurns = -1;
            if(ball.hasNextBall()) {
                meNumTurns = numTurnsReach(mePlayer.getPos(), ball.getPos(), numTurns, moveCap, shootCap, mePlayer == shooter);
                enemyNumTurns = numTurnsReach(enemyPlayer.getPos(), ball.getPos(), numTurns, moveCap, shootCap, enemyPlayer == shooter);
            }
            else {
                meNumTurns = numTurnsReach(mePlayer.getPos(), ball.getPos(), -1, moveCap, shootCap, mePlayer == shooter);
                enemyNumTurns = numTurnsReach(enemyPlayer.getPos(), ball.getPos(), -1, moveCap, shootCap, enemyPlayer == shooter);
            }
            CenterPointState state = null;
            if(meNumTurns <= numTurns && enemyNumTurns > numTurns) {
                state = CenterPointState.A_POSSESS;
            }
            else if(meNumTurns <= numTurns && enemyNumTurns <= numTurns) {
                state = CenterPointState.CLASH;
            }
            else if(meNumTurns > numTurns && enemyNumTurns <= numTurns) {
                state = CenterPointState.B_POSSESS;
            }
            else {
                if(!ball.hasNextBall()) {
                    if(meNumTurns < enemyNumTurns) {
                        state = CenterPointState.A_POSSESS;
                    }
                    else if(meNumTurns == enemyNumTurns) {
                        state = CenterPointState.CLASH;
                    }
                    else {
                        state = CenterPointState.B_POSSESS;
                    }
                }
                else {
                    state = CenterPointState.NON_POSSESS;
                }
            }
            CenterPoint candidate = new CenterPoint(state, me, enemy, ball, mePlayer, enemyPlayer, numTurns);
            if(result == null || candidate.isBetter(result)) {
                result = candidate;
            }
            if(enemyNumTurns <= numTurns) {
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

    private int numTurnsReach(Position start, Position end, int numTurnsLimit, int moveCap, int shootCap, boolean isShooter) {
        int result = 0;
        if(isShooter) {
            result++;
        }
        while((numTurnsLimit == -1 || result <= numTurnsLimit) && start.getDist(end) > shootCap) {
            result++;
            start = start.getMoveToward(end, moveCap);
        }
        return result;
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
        if(mid.multiply(left) * mid.multiply(right) >= 0) {
            return false;
        }
        mid = upperGoal.subtract(lowerGoal);
        left = pos.subtract(lowerGoal);
        right = newPos.subtract(upperGoal);
        if(mid.multiply(left) * mid.multiply(right) >= 0) {
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