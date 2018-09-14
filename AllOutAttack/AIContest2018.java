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
        final int SWEEP_RADIUS = 3000;
        final int MIDFIELD_RADIUS = 1000;
        final int STRIKE_RADIUS = 1000;
        final int DIRECTION_RADIUS = 5;
        final int MARK_RADIUS = 400;
        final int[] FORCES = new int[] {16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100};
        final Position STRIKE_POINT = enemy.getGoal().getPos().getMoveToward(new Position(mapW / 2, mapH / 2), 500);
        final Position SWEEP_POINT = me.getGoal().getPos().getMoveToward(new Position(mapW / 2, mapH / 2), 500);
        final Position LOWER_SHOOT_TARGET = enemy.getGoal().getPos().plus(new Position(0, -1100));
        final Position UPPER_SHOOT_TARGET = enemy.getGoal().getPos().plus(new Position(0, 1100));
        boolean isEnemyShootable = false;
        for(int i = 0; i < enemy.getNumPlayers(); i++) {
            if(enemy.getPlayer(i).getPos().getDist(ball.getPos()) <= shootCap) {
                isEnemyShootable = true;
                break;
            }
        }
        Position[] target = new Position[me.getNumPlayers()];
        Arrays.fill(target, null);
        boolean[] markedPlayer = new boolean[me.getNumPlayers()];
        Arrays.fill(markedPlayer, false);
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
        CenterPoint centerPoint = getBestCenterPoint(0, me, enemy, ball, moveCap, shootCap, mapW, mapH, null);
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
                    CenterPoint predictCenterPoint = getBestCenterPoint(0, me, enemy, predictBall, moveCap, shootCap, mapW, mapH, enemy.getPlayer(0));
                    mainPlayer.getAction().move(predictCenterPoint.getBall().getPos());
                }
            }
            return;
        }
        Player mainPlayer = centerPoint.getPlayerA();
        target[mainPlayer.getId()] = centerPoint.getBall().getPos();
        Player nextMainPlayer = null;
        CenterPoint nextCenterPoint = null;
        CenterPoint subCenterPoint = null;
        CenterPoint mainCenterPoint = centerPoint;
        Position bestDestination = null;
        int bestForce = -1;
        if(centerPoint.getState() == CenterPointState.A_POSSESS) {
            CenterPoint[][] newCenterPoints = new CenterPoint[directions.size()][FORCES.length];
            int[] newForces = new int[directions.size()];
            for(int i = 0; i < directions.size(); i++) {
                Position direction = directions.get(i);
                for(int j = 0; j < FORCES.length; j++) {
                    int force = FORCES[j];
                    Ball newBall = new Ball(centerPoint.getBall().getPos(), new Position(0, 0).getMoveOver(direction, force * fMul + ball.getAcc()), ball.getAcc());
                    newCenterPoints[i][j] = getBestCenterPoint(centerPoint.getTurn(), me, enemy, newBall, moveCap, shootCap, mapW, mapH, mainPlayer);
                }
            }
            for(int i = 0; i < directions.size(); i++) {
                Position direction = directions.get(i);
                for(int j = 1; j < FORCES.length; j++) {
                    int force = FORCES[j];
                    CenterPoint newCenterPoint = newCenterPoints[i][j];
                    for(int deltaI = -1; deltaI <= 1; deltaI++) {
                        for(int deltaJ = -1; deltaJ <= 1; deltaJ++) {
                            int newI = (i + deltaI + directions.size()) % directions.size();
                            int newJ = j + deltaJ;
                            if(newJ < FORCES.length && newCenterPoint.isBetter(newCenterPoints[newI][newJ])) {
                                newCenterPoint = newCenterPoints[newI][newJ];
                            }
                        }
                    }
                    if(nextCenterPoint == null || newCenterPoint.isBetter(nextCenterPoint)) {
                        nextCenterPoint = newCenterPoint;
                        subCenterPoint = newCenterPoints[i][j];
                        bestDestination = ball.getPos().plus(direction);
                        bestForce = force;
                    }
                    else if(!nextCenterPoint.isBetter(newCenterPoint) && newCenterPoints[i][j].isBetter(subCenterPoint)) {
                        nextCenterPoint = newCenterPoint;
                        subCenterPoint = newCenterPoints[i][j];
                        bestDestination = ball.getPos().plus(direction);
                        bestForce = force;
                    }
                }
            }
            if(subCenterPoint.getState() == CenterPointState.A_GOAL) {
                bestForce = 100;
            }
        }
        if(mainPlayer.getPos().getDist(ball.getPos()) <= shootCap) {
            if(isEnemyShootable) {
                Random random = new Random();
                int randForce = 95 + random.nextInt(6);
                mainPlayer.getAction().shoot(ball.getPos().plus(new Position(0, 1)), randForce);
            }
            else if(centerPoint.getState() == CenterPointState.A_POSSESS && centerPoint.getTurn() == 0) {
                mainPlayer.getAction().shoot(bestDestination, bestForce);
            }
            else {
                mainPlayer.getAction().move(target[mainPlayer.getId()]);
            }
        }
        else {
            mainPlayer.getAction().move(target[mainPlayer.getId()]);
        }
        if(centerPoint.getState() == CenterPointState.A_POSSESS) {
            nextMainPlayer = subCenterPoint.getPlayerA();
            mainCenterPoint = subCenterPoint;
            if(nextMainPlayer != mainPlayer) {
                target[nextMainPlayer.getId()] = subCenterPoint.getBall().getPos();
                nextMainPlayer.getAction().move(target[nextMainPlayer.getId()]);
            }
        }
        Position sweepPoint = mainCenterPoint.getBall().getPos().getMoveToward(SWEEP_POINT, SWEEP_RADIUS);
        Position strikePoint = mainCenterPoint.getBall().getPos().getMoveToward(STRIKE_POINT, STRIKE_RADIUS);
        Position midfieldPoint = mainCenterPoint.getBall().getPos().getMoveOver(new Position(0, 0).getMoveOver(strikePoint, MIDFIELD_RADIUS).plus(
                                                                                new Position(0, 0).getMoveOver(sweepPoint, MIDFIELD_RADIUS)), MIDFIELD_RADIUS);
        Player sweeper = null;
        Player striker = null;
        Player midfielder = null;
        if(centerPoint.getState() == CenterPointState.A_POSSESS) {
            double minStrikeDist = -1;
            for(int i = 0; i < me.getNumPlayers(); i++) {
                if(target[i] == null) {
                    if(striker == null || me.getPlayer(i).getPos().getDist(strikePoint) < minStrikeDist) {
                        striker = me.getPlayer(i);
                        minStrikeDist = me.getPlayer(i).getPos().getDist(strikePoint);
                    }
                }
            }
            if(striker != null) {
                target[striker.getId()] = strikePoint;
                striker.getAction().move(target[striker.getId()]);
            }
            if(nextMainPlayer == mainPlayer) {
                double minMidfieldDist = -1;
                for(int i = 0; i < me.getNumPlayers(); i++) {
                    if(target[i] == null) {
                        if(midfielder == null || me.getPlayer(i).getPos().getDist(midfieldPoint) < minMidfieldDist) {
                            midfielder = me.getPlayer(i);
                            minMidfieldDist = me.getPlayer(i).getPos().getDist(midfieldPoint);
                        }
                    }
                }
                if(midfielder != null) {
                    target[midfielder.getId()] = midfieldPoint;
                    midfielder.getAction().move(target[midfielder.getId()]);
                }
            }
        }
        while(true) {
            Player mainThreat = null;
            double minDist = -1;
            for(int i = 0; i < enemy.getNumPlayers(); i++) {
                if(!markedPlayer[i]) {
                    if(me.getGoal().getDist(enemy.getPlayer(i).getPos()) < me.getGoal().getDist(sweepPoint)) {
                        if(mainThreat == null || me.getGoal().getDist(enemy.getPlayer(i).getPos()) < minDist) {
                            mainThreat = enemy.getPlayer(i);
                            minDist = me.getGoal().getDist(enemy.getPlayer(i).getPos());
                        }
                    }
                }
            }
            if(mainThreat == null) {
                break;
            }
            markedPlayer[mainThreat.getId()] = true;
            Player defender = null;
            minDist = -1;
            for(int i = 0; i < me.getNumPlayers(); i++) {
                if(target[i] == null) {
                    if(defender == null || mainThreat.getPos().getDist(me.getPlayer(i).getPos()) < minDist) {
                        defender = me.getPlayer(i);
                        minDist = mainThreat.getPos().getDist(me.getPlayer(i).getPos());
                    }
                }
            }
            if(defender == null) {
                break;
            }
            target[defender.getId()] = mainThreat.getPos().getMoveToward(me.getGoal().getPos(), MARK_RADIUS);
            if(defender.getPos().getDist(ball.getPos()) <= shootCap && isEnemyShootable) {
                defender.getAction().shoot(ball.getPos().plus(new Position(1, 0)), 100);
            }
            else {
                defender.getAction().move(target[defender.getId()]);
            }
        }
        double minSweepDist = -1;
        for(int i = 0; i < me.getNumPlayers(); i++) {
            if(target[i] == null) {
                if(sweeper == null || me.getPlayer(i).getPos().getDist(sweepPoint) < minSweepDist) {
                    sweeper = me.getPlayer(i);
                    minSweepDist = me.getPlayer(i).getPos().getDist(sweepPoint);
                }
            }
        }
        if(sweeper != null) {
            target[sweeper.getId()] = sweepPoint;
            sweeper.getAction().move(target[sweeper.getId()]);
        }
        while(true) {
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
            if(mainThreat == null) {
                break;
            }
            markedPlayer[mainThreat.getId()] = true;
            Player defender = null;
            minDist = -1;
            for(int i = 0; i < me.getNumPlayers(); i++) {
                if(target[i] == null) {
                    if(defender == null || mainThreat.getPos().getDist(me.getPlayer(i).getPos()) < minDist) {
                        defender = me.getPlayer(i);
                        minDist = mainThreat.getPos().getDist(me.getPlayer(i).getPos());
                    }
                }
            }
            if(defender == null) {
                break;
            }
            target[defender.getId()] = mainThreat.getPos().getMoveToward(mainCenterPoint.getBall().getPos(), MARK_RADIUS);
            if(defender.getPos().getDist(ball.getPos()) <= shootCap && isEnemyShootable) {
                defender.getAction().shoot(ball.getPos().plus(new Position(1, 0)), 100);
            }
            else {
                defender.getAction().move(target[defender.getId()]);
            }
        }
    }

    private CenterPoint getBestCenterPoint(int numTurns, Team me, Team enemy, Ball ball, int moveCap, int shootCap, int mapW, int mapH, Player shooter) {
        CenterPoint result = null;
        Position shooterPos = null;
        int shooterTurns = 0;
        if(shooter != null) {
            shooterPos = shooter.getPos();
            while(shooterPos.getDist(ball.getPos()) > shootCap) {
                shooterPos = shooterPos.getMoveToward(ball.getPos(), moveCap);
                shooterTurns++;
            }
            shooterTurns++;
        }
        while(true) {
            Player mePlayer = getClosestPlayer(me, ball.getPos(), moveCap, shooter, shooterPos);
            Player enemyPlayer = getClosestPlayer(enemy, ball.getPos(), moveCap, shooter, shooterPos);
            int meNumTurns = -1;
            int enemyNumTurns = -1;
            if(ball.hasNextBall()) {
                meNumTurns = numTurnsReach(mePlayer.getPos(), ball.getPos(), numTurns, moveCap, shootCap, mePlayer == shooter, shooterPos, shooterTurns);
                enemyNumTurns = numTurnsReach(enemyPlayer.getPos(), ball.getPos(), numTurns, moveCap, shootCap, enemyPlayer == shooter, shooterPos, shooterTurns);
            }
            else {
                meNumTurns = numTurnsReach(mePlayer.getPos(), ball.getPos(), -1, moveCap, shootCap, mePlayer == shooter, shooterPos, shooterTurns);
                enemyNumTurns = numTurnsReach(enemyPlayer.getPos(), ball.getPos(), -1, moveCap, shootCap, enemyPlayer == shooter, shooterPos, shooterTurns);
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

    private Player getClosestPlayer(Team team, Position pos, int moveCap, Player shooter, Position shooterPos) {
        Player result = null;
        double minDist = 0;
        for(int i = 0; i < team.getNumPlayers(); i++) {
            double newMinDist = team.getPlayer(i).getPos().getDist(pos);
            if(team.getPlayer(i) == shooter) {
                newMinDist = shooter.getPos().getDist(shooterPos) + shooterPos.getDist(pos) + moveCap;
            }
            if(result == null || newMinDist < minDist) {
                result = team.getPlayer(i);
                minDist = newMinDist;
            }
        }
        return result;
    }

    private int numTurnsReach(Position start, Position end, int numTurnsLimit, int moveCap, int shootCap, boolean isShooter, Position shooterPos, int shooterTurns) {
        int result = 0;
        if(isShooter) {
            result += shooterTurns;
            start = shooterPos;
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
            newSpeed = new Position(-newSpeed.getX(), newSpeed.getY());
        }
        if(newY < 0) {
            newY = -newY;
            newSpeed = new Position(newSpeed.getX(), -newSpeed.getY());
        }
        if(newX > mapW) {
            newX = mapW - (newX - mapW);
            newSpeed = new Position(-newSpeed.getX(), newSpeed.getY());
        }
        if(newY > mapH) {
            newY = mapH - (newY - mapH);
            newSpeed = new Position(newSpeed.getX(), -newSpeed.getY());
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

    public Position divide(int d) {
        return new Position(x / d, y / d);
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