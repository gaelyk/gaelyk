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

    private static final Closure DEFAULT_AFTER_BLOCK = { ret, exp -> 
        if(exp){ throw new ExecutionException(exp) } 
        ret 
    }
    
    private final Callable<Future<R>> factory
    private final Callable condition
    private final Callable after

    private Future<R> current
    private boolean cancelled = false
    private boolean done      = false


    private ConditionalRetryingFuture(Callable<?> condition, Callable<Future<R>> factory, Callable<R> after = null){
        this.current = factory()
        this.factory = factory
        this.condition = condition
        this.after = after ?: DEFAULT_AFTER_BLOCK
    }

    static ConditionalRetryingFutureBuilder asLongAs(Callable<?> condition){
        new ConditionalRetryingFutureBuilder(condition)
    }
    
    ConditionalRetryingFuture thenReturn(Callable<R> after){
        new ConditionalRetryingFuture(this.condition, this.factory, after)
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
                return handleAfter(result, null)
            } catch(ExecutionException ex){
                if(!condition(ex.cause)){
                    return handleAfter(null, ex.cause)
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
                return handleAfter(result, null)
            } catch(ExecutionException ex){
                if(!condition(ex.cause)){
                    return handleAfter(null, ex.cause)
                }
                current = factory()
            }
        }
        // should not happen
        return null
    }
    
    private R handleAfter(ret, Exception exp){
        try {
            if(after instanceof Closure){
                if(after.maximumNumberOfParameters == 0){
                    if(exp){
                        throw exp
                    }
                    return after()
                } else if(after.maximumNumberOfParameters == 1){
                    if(exp){
                        throw exp
                    }
                    return after(ret)
                }
            }
            return after(ret, exp)
        } catch(ExecutionException e){
            // throw directly
            throw e
        } catch(e){
            // wrap
            throw new ExecutionException(e)
        }

    }

}

class ConditionalRetryingFutureBuilder {

    private final Callable condition

    private ConditionalRetryingFutureBuilder(Callable condition){
        this.condition = condition
    }
    
    public <R> ConditionalRetryingFuture<R> tryResolve(Closure<Future<R>> factory){
        new ConditionalRetryingFuture<R>(condition, factory)
    }

}
