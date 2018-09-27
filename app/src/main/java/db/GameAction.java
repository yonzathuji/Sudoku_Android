package db;


import game.Tile;

public class GameAction {
    public Tile tile;
    public int value;
    public boolean isNote;

    public GameAction(Tile t, int value, boolean isNote) {
        if (t != null) {
            this.tile = new Tile(t);
        }
        this.value = value;
        this.isNote = isNote;
    }

    GameAction(String line) {
        String[] parts = line.split(",");
        tile = new Tile(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        value = Integer.parseInt(parts[2]);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof GameAction)) {
            return false;
        }

        GameAction other = (GameAction)obj;

        return other.tile.equals(tile) && other.value == value && other.isNote == isNote;
    }

    @Override
    public String toString() {
        return tile.toString() + "," + String.valueOf(value);
    }
}
