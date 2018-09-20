package db;

public class Config {
    public static final String PLAYING_PUZZLE = "playing_puzzle.txt"; // format:
        // 123456789
        // 000000000
        // .........
        // 123123440

    public static final String VERIFICATION_PUZZLE = "verification_puzzle.txt"; // format:
    // 12345?789
    // 000000?00
    // .........
    // 12?123440

    public static final String SOLUTION_PUZZLE = "solution_puzzle.txt"; // format:
    // 123456789
    // 432423523
    // .........
    // 123123440

    public static final String NOTES_FILE = "notes.txt"; // format:
    // x,y,value,value,value,...,value
    // 0,0,1,2,3,9
    // 5,2,9,4,2
    // 8,1

    public static final String ACTIONS_FILE  = "actions.txt"; // format:
    // x,y,value
    // 1,0,2
    // 8,2,0

}
