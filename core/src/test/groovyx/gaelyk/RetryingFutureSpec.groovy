package groovyx.gaelyk

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit;

import spock.lang.Specification

class RetryingFutureSpec extends Specification {
    
    def "Fail after three attemps"(){
        Future<String> future = Mock()
        
        when:
        Future<String> retrying = RetryingFuture.retry(3) {
            future
        }
        retrying.get()
        
        then:
        thrown ExecutionException
        3 * future.get() >> {throw new ExecutionException("Failed") }
    }
    
    def "Fail after three attemps with timeout"(){
        Future<String> future = Mock()
        
        when:
        Future<String> retrying = RetryingFuture.retry(3) {
            future
        }
        retrying.get(100, TimeUnit.SECONDS)
        
        then:
        thrown ExecutionException
        3 * future.get(100, TimeUnit.SECONDS) >> {throw new ExecutionException("Failed") }
    }
    
    def "Recover from failure"(){
        Future<String> future = Mock()
        
        when:
        Future<String> retrying = RetryingFuture.retry(3) {
            future
        }
        String result = retrying.get()
        
        then:
        result == "Hello"
        1 * future.get() >> {throw new ExecutionException("Failed") } 
        1 * future.get() >> "Hello"
    }
    
    def "Succeed for a first time"(){
        Future<String> future = Mock()
        
        when:
        Future<String> retrying = RetryingFuture.retry(3) {
            future
        }
        String result = retrying.get()
        
        then:
        result == "Hello"
        1 * future.get() >> "Hello"
    }
    
    def "Recover from failure - different notation"(){
        Future<String> future = Mock()
        
        when:
        Future<String> retrying = 3 * { future }
        String result = retrying.get()
        
        then:
        result == "Hello"
        1 * future.get() >> {throw new ExecutionException("Failed") }
        1 * future.get() >> "Hello"
    }

}
