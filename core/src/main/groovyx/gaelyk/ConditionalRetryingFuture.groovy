package groovyx.gaelyk

import groovy.transform.ThreadInterrupt

import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * ConditinalRetryingFuture is implementation of {@link Future} which tries until the
 * {@link #condition} is met before throwing {@link ExecutionException}.
 * 
 * @author Vladimir Orany
 *
 * @param <R> result of the {@link #get()} method
 */
@ThreadInterrupt
class ConditionalRetryingFuture<R> implements Future<R> {

    private final Callable<Future<R>> factory
    private final Callable condition

    private Future<R> current
    private boolean cancelled = false
    private boolean done      = false


    private ConditionalRetryingFuture(Callable<?> condition, Callable<Future<R>> factory){
        this.current = factory()
        this.factory = factory
        this.condition = condition
    }

    static ConditionalRetryingFutureBuilder asLongAs(Callable<?> condition){
        new ConditionalRetryingFutureBuilder(condition)
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
        while(true){
            try {
                R result = current.get()
                done = true
                return result
            } catch(ExecutionException ex){
                if(!condition(ex.cause)){
                    throw ex
                }
                current = factory()
            }
        }
        // should not happen
        return null
    }

    @Override public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        while(true){
            try {
                R result = current.get(timeout, unit)
                done = true
                return result
            } catch(ExecutionException ex){
                if(!condition(ex.cause)){
                    throw ex
                }
                current = factory()
            }
        }
        // should not happen
        return null
    }

}

class ConditionalRetryingFutureBuilder {

    private final Callable<?> condition

    private ConditionalRetryingFutureBuilder(Callable<?> condition){
        this.condition = condition
    }

    public <R> ConditionalRetryingFuture<R> tryResolve(Closure<Future<R>> factory){
        new ConditionalRetryingFuture<R>(condition, factory)
    }

}
