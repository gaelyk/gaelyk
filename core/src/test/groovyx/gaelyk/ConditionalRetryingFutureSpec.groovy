package groovyx.gaelyk

import static ConditionalRetryingFuture.asLongAs

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import spock.lang.Specification

class ConditionalRetryingFutureSpec extends Specification {
    
    def "Fail after three attemps"(){
        Future<String> future = Mock()
        
        when:
        int counter = 0
        Future<String> retrying = asLongAs { 
            ++counter < 3
        } tryResolve {
            future
        }
        retrying.get()
        
        then:
        thrown ExecutionException
        3 * future.get() >> {throw new ExecutionException("Failed", new IllegalStateException("DID NOT SUCCEED")) }
    }
    
    def "Return default value after three attemps"(){
        Future<String> future = Mock()
        
        when:
        int counter = 0
        Future<String> retrying = asLongAs {
            ++counter < 3
        } tryResolve {    
            future
        } thenReturn { ret, exp ->
            "Hello"
        }
        def result = retrying.get()
        
        then:
        result == "Hello"
        3 * future.get() >> {throw new ExecutionException("Failed", new IllegalStateException("DID NOT SUCCEED")) }
    }
    
    def "Fail after three attemps with timeout"(){
        Future<String> future = Mock()
        
        when:
        int counter = 0
        Future<String> retrying = asLongAs { 
            ++counter < 3
        } tryResolve {
            future
        }
        retrying.get(100, TimeUnit.SECONDS)
        
        then:
        thrown ExecutionException
        3 * future.get(100, TimeUnit.SECONDS) >> {throw new ExecutionException("Failed", new IllegalStateException("DID NOT SUCCEED")) }
    }
    
    def "Recover from failure"(){
        Future<String> future = Mock()
        
        when:
        int counter = 0
        Future<String> retrying = asLongAs { 
            ++counter < 3
        } tryResolve {
            future
        }
        String result = retrying.get()
        
        then:
        result == "Hello"
        1 * future.get() >> {throw new ExecutionException("Failed", new IllegalStateException("DID NOT SUCCEED")) }
        1 * future.get() >> "Hello"
    }
    
    def "Succeed for a first time"(){
        Future<String> future = Mock()
        
        when:
        int counter = 0
        Future<String> retrying = asLongAs { 
            ++counter < 3
        } tryResolve {
            future
        }
        String result = retrying.get()
        
        then:
        result == "Hello"
        1 * future.get() >> "Hello"
    }

}
