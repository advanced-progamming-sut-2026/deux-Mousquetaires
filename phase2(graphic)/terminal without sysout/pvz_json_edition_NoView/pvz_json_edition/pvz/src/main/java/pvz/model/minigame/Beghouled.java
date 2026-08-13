package pvz.model.minigame;

public class Beghouled extends MiniGame {

    private static final int ROWS = 5;
    private static final int COLS = 8;
    private final char[] basePool = {'P', 'W', 'F', 'C', 'S'};
    private final char[][] board = new char[ROWS][COLS];
    private final int[][] craterTimer = new int[ROWS][COLS];
    private int sun;
    private int target;
    private int swaps;
    private char peaSymbol = 'P';   // P ->(500) -->(1500)
    private char wallSymbol = 'W';  // W ->(500)
    private char puffSymbol = 'F';  // F ->(250)
    private char lobSymbol = 'C';   // C ->(1000) -->(750)

    public Beghouled(int level) {
        super(level);
    }

    @Override
    public void start() {
        target = 750 + 250 * level;
        fillBoard();
        printBoard();
    }

    @Override
    public void handle(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("swap")) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("swap\\s*\\((\\d+)\\s*,\\s*(\\d+)\\)\\s*\\((\\d+)\\s*,\\s*(\\d+)\\)")
                    .matcher(trimmed);
            if (!matcher.matches()) {
                return;}

            swap(Integer.parseInt(matcher.group(1)),Integer.parseInt(matcher.group(2)),Integer.parseInt(matcher.group(3)),Integer.parseInt(matcher.group(4)));
            return;
        }
        String[] parts = trimmed.split("\\s+");
        switch (parts[0]) {
            case "upgrade":
                if (parts.length < 2) {
                    return;}
                upgrade(parts[1].toLowerCase());
                return;
            case "board":
                printBoard();
                return;
            case "status":
                return;
            case "quit":
                lose("You walked away from the puzzle.");
                return;
            default:
        }
    }

    private void swap(int x1, int y1, int x2, int y2) {
        if (!inBounds(x1, y1) || !inBounds(x2, y2)) {
            return;
        }
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) != 1) {
            return;
        }
        int row1 = y1 - 1,col1 = x1 - 1,row2 = y2 - 1,col2 = x2 - 1;
        if (board[row1][col1] == '#' || board[row2][col2] == '#') {
            return;
        }
        doSwap(row1, col1, row2, col2);
        if (!anyMatch()) {
            doSwap(row1, col1, row2, col2);
            return;
        }
        swaps++;
        resolveMatches();
        healCraters();
        if (sun >= target) {
            win("You gathered " + sun + " sun - Beghouled level " + level + " complete!");
            return;}

        if (!anyPossibleMove()) {
            fillBoard();}

        printBoard();
    }

    private void resolveMatches() {
        int combo = 0;
        while (true) {
            boolean[][] matched = findMatches();
            int count = 0;
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {if (matched[r][c]) count++;}
            }
            if (count == 0) break;
            combo++;
            int gained = count * 10 + (combo > 1 ? 1 : 0); // +1 sun per extra combo
            sun += gained;
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (matched[r][c]) {
                        if (random.nextInt(100) < 10) {
                            board[r][c] = '#';
                            craterTimer[r][c] = 3;}
                        else board[r][c] = randomSymbol();
                    }
                }
            }
        }
    }

    private boolean[][] findMatches() {
        boolean[][] matched = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c + 2 < COLS; c++) {
                char ch = board[r][c];
                if (ch != '#' && ch == board[r][c + 1] && ch == board[r][c + 2]) {
                    matched[r][c] = matched[r][c + 1] = matched[r][c + 2] = true;}
            }
        }
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r + 2 < ROWS; r++) {
                char ch = board[r][c];
                if (ch != '#' && ch == board[r + 1][c] && ch == board[r + 2][c]) {
                    matched[r][c] = matched[r + 1][c] = matched[r + 2][c] = true;}
            }
        }
        return matched;
    }

    private boolean anyMatch() {
        boolean[][] matched = findMatches();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {if (matched[r][c]) return true;}
        }
        return false;
    }

    private boolean anyPossibleMove() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (c + 1 < COLS && tryMove(r, c, r, c + 1)) return true;
                if (r + 1 < ROWS && tryMove(r, c, r + 1, c)) return true;
            }
        }
        return false;
    }

    private boolean tryMove(int r1, int c1, int r2, int c2) {
        if (board[r1][c1] == '#' || board[r2][c2] == '#') return false;
        doSwap(r1, c1, r2, c2);
        boolean ok = anyMatch();
        doSwap(r1, c1, r2, c2);
        return ok;
    }

    private void doSwap(int r1, int c1, int r2, int c2) {
        char temp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = temp;
    }

    private void healCraters() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == '#' && --craterTimer[r][c] <= 0) board[r][c] = randomSymbol();
            }
        }
    }

    private void fillBoard() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {board[r][c] = randomSymbol();}
        }

        ///clear
        boolean[][] matched = findMatches();
        for (int guard = 0; guard < 20; guard++) {
            boolean any = false;
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (matched[r][c]) {
                        board[r][c] = randomSymbol();
                        any = true;}
                }
            }
            if (!any) break;
            matched = findMatches();
        }
    }

    private char randomSymbol() {
        char base = basePool[random.nextInt(basePool.length)];
        switch (base) {
            case 'P': return peaSymbol;
            case 'W': return wallSymbol;
            case 'F': return puffSymbol;
            case 'C': return lobSymbol;
            default: return 'S';
        }
    }

    /// upgarde
    private void upgrade(String name) {
        switch (name) {
            case "repeater":
                buyUpgrade(500, peaSymbol == 'P', () -> replaceSymbol('P', peaSymbol = 'R'), "Peashooters upgraded to Repeaters!");
                return;
            case "gatling":
                buyUpgrade(1500, peaSymbol == 'R', () -> replaceSymbol('R', peaSymbol = 'G'), "Repeaters upgraded to Gatling Peas!");
                return;
            case "tallnut":
                buyUpgrade(500, wallSymbol == 'W', () -> replaceSymbol('W', wallSymbol = 'T'), "Wall-nuts upgraded to Tall-nuts!");
                return;
            case "fume":
                buyUpgrade(250, puffSymbol == 'F', () -> replaceSymbol('F', puffSymbol = 'U'), "Puff-shrooms upgraded to Fume-shrooms!");
                return;
            case "melon":
                buyUpgrade(1000, lobSymbol == 'C', () -> replaceSymbol('C', lobSymbol = 'M'), "Cabbage-pults upgraded to Melon-pults!");
                return;
            case "winter":
                buyUpgrade(750, lobSymbol == 'M', () -> replaceSymbol('M', lobSymbol = 'I'), "Melon-pults upgraded to Winter Melons!");
                return;
            default:
        }
    }

    private void buyUpgrade(int cost, boolean prerequisite, Runnable apply, String message) {
        if (!prerequisite) {
            return;}
        if (sun < cost) {
            return;}
        sun -= cost;
        apply.run();
        printBoard();
    }

    private void replaceSymbol(char from, char to) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == from) board[r][c] = to;
            }
        }
    }

    /// render
    private boolean inBounds(int x, int y) {
        return x >= 1 && x <= COLS && y >= 1 && y <= ROWS;
    }

    private void printBoard() {
        StringBuilder stringBuilder = new StringBuilder("Sun: " + sun + "/" + target + "\n   ");
        for (int c = 1; c <= COLS; c++) {
            stringBuilder.append(" ").append(c);
        }
        stringBuilder.append("\n");
        for (int r = 0; r < ROWS; r++) {
            stringBuilder.append(" ").append(r + 1).append(" ");
            for (int c = 0; c < COLS; c++) {stringBuilder.append(" ").append(board[r][c]);}
            stringBuilder.append("\n");
        }
        stringBuilder.append("P/R/G=pea family W/T=nuts F/U=shrooms C/M/I=pults S=sunflower #=crater");
    }
}
