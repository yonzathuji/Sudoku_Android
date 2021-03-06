package db;


import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import postpc.yonz.main.MyApplication;

public abstract class PuzzlesManager {

    private static Context context = MyApplication.getAppContext();

    public static boolean createVerificationBoard(String[][] puzzle) {
        try {
            File puzzleFile = new File(context.getExternalFilesDir(null), Config.VERIFICATION_PUZZLE);
            BufferedWriter writer = new BufferedWriter(new FileWriter(puzzleFile, false));
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    writer.write(String.valueOf(puzzle[y][x]));
                }
                writer.write("\n");
            }
            writer.close();
            return true;
        }
        catch (Exception e) {
            Log.e("PuzzleManager Error", e.getMessage() +
                    " : error generating verification file");
        }
        return false;
    }

    public static void  createSolutionBoard(int[][] puzzle) {
        try {
            File puzzleFile = new File(context.getExternalFilesDir(null), Config.SOLUTION_PUZZLE);
            BufferedWriter writer = new BufferedWriter(new FileWriter(puzzleFile, false));
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    writer.write(String.valueOf(puzzle[y][x]));
                }
                writer.write("\n");
            }
            writer.close();
        }
        catch (Exception e) {
            Log.e("PuzzleManager Error", e.getMessage() +
                    " : error generating solution file");
        }
    }

    public static boolean isPlayingPuzzleFileExists() {
        try {
            File puzzleFile = new File(context.getExternalFilesDir(null), Config.PLAYING_PUZZLE);
            List<String> lines = Files.readAllLines(puzzleFile.toPath());
            Log.e("DB:::", String.valueOf(lines.size()));
            return lines.size() == 9;
        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
        return false;
    }

    public static void newGame() {
        new File(context.getExternalFilesDir(null),Config.PLAYING_PUZZLE).delete();
        new File(context.getExternalFilesDir(null),Config.VERIFICATION_PUZZLE).delete();
        new File(context.getExternalFilesDir(null),Config.SOLUTION_PUZZLE).delete();
        new File(context.getExternalFilesDir(null),Config.NOTES_FILE).delete();
        new File(context.getExternalFilesDir(null),Config.ACTIONS_FILE).delete();
    }

    public static char[][] getVerificationBoard() {

        try {
            File puzzleFile = new File(context.getExternalFilesDir(null), Config.VERIFICATION_PUZZLE);
            List<String> lines = Files.readAllLines(puzzleFile.toPath());
            char[][] board = new char[9][9];
            int rowIndex = 0;
            for (String line : lines) {
                int colIndex = 0;
                for (char charValue : line.toCharArray()) {
                    board[rowIndex][colIndex] = charValue;
                    colIndex++;
                }
                rowIndex++;
            }
            return board;

        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
        return null;
    }

    public static void editVerificationFile(GameAction action) {
        try {
            File puzzleFile = new File(context.getExternalFilesDir(null), Config.VERIFICATION_PUZZLE);
            List<String> lines = Files.readAllLines(puzzleFile.toPath());
            String lineToChange = lines.get(action.tile.y);
            String newLine = lineToChange.substring(0, action.tile.x) +
                              String.valueOf(action.value) +
                              lineToChange.substring(action.tile.x + 1);
            lines.set(action.tile.y, newLine);
            Files.write(puzzleFile.toPath(), lines);
        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
    }

    public static boolean isVerificationComplete() {
        try {
            File verificationFile = new File(context.getExternalFilesDir(null), Config.VERIFICATION_PUZZLE);
            List<String> lines = Files.readAllLines(verificationFile.toPath());

            for (String line : lines) {
                if (line.contains("?")) {
                    return false;
                }
            }
        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
        return true;
    }

    public static void completeVerification() {
        try {
            File verificationFile = new File(context.getExternalFilesDir(null), Config.VERIFICATION_PUZZLE);
            File playingFile = new File(context.getExternalFilesDir(null), Config.PLAYING_PUZZLE);
            List<String> verificationLines = Files.readAllLines(verificationFile.toPath());
            List<String> playingLines = new ArrayList<>();

            for (String line : verificationLines) {
               playingLines.add(line.replace('?', '0'));
            }

            Files.write(playingFile.toPath(), playingLines);
        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
    }

    public static int[][] getOriginalPlayingBoard() {
        try {
            File puzzleFile = new File(context.getExternalFilesDir(null), Config.PLAYING_PUZZLE);
            List<String> lines = Files.readAllLines(puzzleFile.toPath());
            int[][] board = new int[9][9];
            int rowIndex = 0;
            for (String line : lines) {
                int colIndex = 0;
                for (char charValue : line.toCharArray()) {
                    board[rowIndex][colIndex] = charValue - 48;
                    colIndex++;
                }
                rowIndex++;
            }
            return board;

        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static int[][] getSolutionBoard() {
        try {
            File puzzleFile = new File(context.getExternalFilesDir(null), Config.SOLUTION_PUZZLE);
            List<String> lines = Files.readAllLines(puzzleFile.toPath());
            int[][] board = new int[9][9];
            int rowIndex = 0;
            for (String line : lines) {
                int colIndex = 0;
                for (char charValue : line.toCharArray()) {
                    board[rowIndex][colIndex] = charValue - 48;
                    colIndex++;
                }
                rowIndex++;
            }
            return board;

        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static List<GameAction> getUserActions() {
        try {
            List<GameAction> actions = new ArrayList<>();
            File actionsFile = new File(context.getExternalFilesDir(null), Config.ACTIONS_FILE);
            List<String> lines = Files.readAllLines(actionsFile.toPath());
            for(String line : lines) {
                actions.add(new GameAction(line));
            }
            return actions;

        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
        return null;
    }

    public static List<GameAction> getUserNotes() {
        try {
            List<GameAction> notes = new ArrayList<>();
            File notesFile = new File(context.getExternalFilesDir(null), Config.NOTES_FILE);
            List<String> lines = Files.readAllLines(notesFile.toPath());
            for(String line : lines) {
                if (line.length() < 3) continue;
                String tileString = line.substring(0,3);
                String[] notesStrings = line.substring(4).split(",");
                for (String noteString : notesStrings) {
                    notes.add(new GameAction(tileString + "," + noteString));
                }
            }
            return notes;
        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
        return null;
    }

    public static void writeUserAction(GameAction action) {
       try {
           File actionsFile = new File(context.getExternalFilesDir(null), Config.ACTIONS_FILE);
           BufferedWriter writer = new BufferedWriter(new FileWriter(actionsFile, true));
           writer.write(action.toString() + "\n");
           writer.close();
       }
       catch (IOException e) {
           Log.e("PuzzleManager Error", e.getMessage());
       }
    }

    public static void writeUserNote(GameAction action) {
        try {
            File notesFile = new File(context.getExternalFilesDir(null), Config.NOTES_FILE);
            try {
                List<String> lines = Files.readAllLines(notesFile.toPath());
                int lineIndex = 0;
                for (String line : lines) {
                    if (line.startsWith(action.tile.toString())) {
                        line += "," + String.valueOf(action.value) + "\n";
                        lines.set(lineIndex, line);
                        Files.write(notesFile.toPath(), lines);
                        return;
                    }
                    lineIndex++;
                }
                // tile not in notes-file so add a new line
                lines.add(action.tile.toString() + "," + String.valueOf(action.value) + "\n");
                Files.write(notesFile.toPath(), lines);
            }
            catch (IOException e) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(notesFile, true));
                writer.write(action.toString() + "\n");
                writer.close();
            }

        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }

    }

    public static void removeUserNotes(GameAction action) {
        try {
            File notesFile = new File(context.getExternalFilesDir(null), Config.NOTES_FILE);
            List<String> lines = Files.readAllLines(notesFile.toPath());
            int lineIndex = 0;
            for (String line : lines) {
                if (line.startsWith(action.tile.toString())) {
                    lines.remove(lineIndex);
                    Files.write(notesFile.toPath(), lines);
                    return;
                }
                lineIndex ++;
            }
        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
    }

    public static void popLastUserAction() {
        try {
            File actionsFile = new File(context.getExternalFilesDir(null), Config.ACTIONS_FILE);
            List<String> lines = Files.readAllLines(actionsFile.toPath());
            if (lines.isEmpty()) {
                return;
            }
            lines.remove(lines.size() - 1);
            Files.write(actionsFile.toPath(), lines);
        }
        catch (IOException e) {
            Log.e("PuzzleManager Error", e.getMessage());
        }
    }

    public static boolean isSolutionFileExists() {
        try {
            File solutionPuzzle = new File(context.getExternalFilesDir(null), Config.SOLUTION_PUZZLE);
            List<String> lines = Files.readAllLines(solutionPuzzle.toPath());
            return lines.size() == 9;
        }
        catch (IOException e){
            Log.e("PuzzleManager Error", e.getMessage());
        }
        return false;
    }

    public static void resetPlayingBoard() {
        new File(context.getExternalFilesDir(null),Config.PLAYING_PUZZLE).delete();
        new File(context.getExternalFilesDir(null),Config.NOTES_FILE).delete();
        new File(context.getExternalFilesDir(null),Config.ACTIONS_FILE).delete();
    }
}
