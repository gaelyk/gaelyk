package groovyx.gaelyk

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * This class wraps value to the {@link Future}.
 * 
 * All calls to {@link #get()} or {@link #get(long, TimeUnit)}
 * returns the same value which is passed to {@link #wrap(V)}
 * @author Vladimir Orany
 *
 * @param <V>
 */
class FutureValueWrapper<V> implements Future<V>{
    
    private final V value
    
    private FutureValueWrapper(V value){
        this.value = value
    }

    static <V> Future<V> wrap(V value){
        new FutureValueWrapper<V>(value)
    }
    
    @Override public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override public boolean isCancelled() {
        return false;
    }

    @Override public boolean isDone() {
        return true;
    }

    @Override public V get() throws InterruptedException, ExecutionException {
        return value;
    }

    @Override public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return value;
    }
    
}
