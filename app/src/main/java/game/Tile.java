package game;

public class Tile {
    public final int x, y;


    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Tile(Tile t) {
        x = t.x;
        y = t.y;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Tile))
            return false;

        Tile other = (Tile)obj;

        return other.x == x && other.y == y;
    }

    @Override
    public String toString() {
        return String.valueOf(x)+","+String.valueOf(y);
    }
}
