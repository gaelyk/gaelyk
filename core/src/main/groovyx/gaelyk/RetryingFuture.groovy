package groovyx.gaelyk

import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * RetryingFuture is implementation of {@link Future} which tries {@link #retries} times
 * before throwing {@link ExecutionException}.
 * 
 * @author Vladimir Orany
 *
 * @param <R> result of the {@link #get()} method
 */
class RetryingFuture<R> implements Future<R> {
    
    private final Callable<Future<R>> factory
    private final int retries

    private Future<R> current
    private boolean cancelled = false
    private boolean done      = false
    
    
    private RetryingFuture(int retries, Callable<Future<R>> factory){
        assert retries > 0
        this.retries = retries
        this.current = factory()
        this.factory = factory
    }
    
    /**
     * Creates new retry future which retries particular times before failing
     * @param retries number of retries
     * @param factory closure to construct the future
     * @return future which retries particular times before failing
     */
    static <R> Future<R> retry(int retries, Closure<Future<R>> factory){
        if(retries <= 0){
            return factory()
        }
        new RetryingFuture(retries, factory)
    }
    
    /**
     * Creates new retry future which retries particular times before failing
     * @param retries number of retries
     * @param factory closure to construct the future
     * @return future which retries particular times before failing
     */
    static <R> Future<R> retry(int retries, Callable<Future<R>> factory){
        if(retries <= 0){
            return factory()
        }
        new RetryingFuture(retries, factory)
    }

    @Override public boolean cancel(boolean mayInterruptIfRunning) {
        
        return cancelled = current.cancel(mayInterruptIfRunning)
    }

    @Override public boolean isCancelled() {
        return cancelled
    }

    @Override public boolean isDone() {
        return done
    }

    @Override public R get() throws InterruptedException, ExecutionException {
        while(retries-- > 0){
            try {
                R result = current.get()
                done = true
                return result
            } catch(ExecutionException ex){
                if(retries <= 0){
                    throw ex
                }
                current = factory()
            }
        }
        // should not happen
        return null
    }

    @Override public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        while(retries-- > 0){
            try {
                R result = current.get(timeout, unit)
                done = true
                return result
            } catch(ExecutionException ex){
                if(retries <= 0){
                    throw ex
                }
                current = factory()
            }
        }
        // should not happen
        return null
    }

}
