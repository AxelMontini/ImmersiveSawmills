package axelmontini.immersivesawmills.common.utils.NonNullList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Axel Montini on 14/07/2017.
 */
public class NonNullList<T> extends ArrayList<T> {
    public NonNullList() {
        super();
    }

    @Override
    public boolean add(T t) {
        return t!=null?super.add(t):false;
    }
    @Override
    public void add(int index, T t) {
        if(t!=null)
            super.add(index, t);
    }
    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return super.addAll(collection.parallelStream().filter(t -> t!= null).collect(Collectors.toList()));
    }
    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        return super.addAll(index, collection.parallelStream().filter(t -> t!= null).collect(Collectors.toList()));
    }
    @Override
    public T set(int index, T t) {
        return t!=null?super.set(index, t):null;
    }
}
