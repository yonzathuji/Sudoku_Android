package game;

import android.support.annotation.NonNull;
import android.util.ArraySet;

import java.util.Iterator;
import java.util.Set;

public class Notes implements Iterable<Integer>{

    private Set<Integer> values;

    Notes(){
        values = new ArraySet<>();
    }

    void addNote(int note){
        values.add(note);
    }

    void deleteAllNotes(){
        values.clear();
    }

    int size() { return values.size(); }

    @NonNull
    @Override
    public Iterator<Integer> iterator() {
        return values.iterator();
    }
}
