package groovyx.gaelyk

import java.util.concurrent.Future

import spock.lang.Specification

class FutureValueWrapperSpec extends Specification {
    
    def "Wrap value into future and than retrieve it with get"(){
        String value = "Hello"
        Future<String> wrapped = FutureValueWrapper.wrap(value)
        
        expect:
        value.is(wrapped.get())
    }
    
    def "Wrap value into future using as keyword and than retrieve it with get"(){
        String value = "Hello"
        Future<String> wrapped = value as Future
        
        expect:
        value.is(wrapped.get())
    }

}
