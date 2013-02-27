package groovyx.gaelyk.datastore

import groovy.transform.CompileStatic


@CompileStatic
@Entity
class Order {
    @Key long id
    @Version long version
}
