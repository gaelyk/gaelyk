package groovyx.gaelyk

import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig

import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.NamespaceManager
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory

import static com.google.appengine.api.datastore.Entity.*
import static com.google.appengine.api.datastore.Query.*
import static com.google.appengine.api.datastore.Query.FilterOperator.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

/**
 * Test for the datastore metadata querying methods.
 *
 * @author Benjamin Muschko
 * @author Guillaume Laforge
 */
class DatastoreMetadataExtensionMethodsTest extends GroovyTestCase {

    private LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig()
    )

    private DatastoreService datastore
    private namespace = NamespaceManager

    public void setUp() {
        helper.setUp()
        datastore = DatastoreServiceFactory.datastoreService
    }

    public void tearDown() {
        helper.tearDown()
        datastore = null
    }

    void testGetNamespacesForDefaultFetchOptions() {
        addNamespaces()

        def namespaces = datastore.namespaces

        assert namespaces.key.name == ["tenant1", "tenant2", "tenant3"]
    }

    void testGetNamespacesForLimitedResultSet() {
        addNamespaces()

        def namespaces = datastore.getNamespaces(withLimit(1))

        assert namespaces.key.name == ["tenant1"]
    }

    void testGetNamespacesForFilteredResultSetWithDefaultFetchOptions() {
        addNamespaces()

        def namespaces = datastore.getNamespaces { query ->
            query.addFilter(KEY_RESERVED_PROPERTY, GREATER_THAN_OR_EQUAL, makeNamespaceKey("tenant2"))
        }

        assert namespaces.key.name == ["tenant2", "tenant3"]
    }

    void testGetNamespacesForFilteredResultSetWithLimitedFetchOptions() {
        addNamespaces()

        def namespaces = datastore.getNamespaces(withLimit(1)) { query ->
            query.addFilter(KEY_RESERVED_PROPERTY, GREATER_THAN_OR_EQUAL, makeNamespaceKey("tenant2"))
        }

        assert namespaces.key.name == ["tenant2"]
    }

    void testGetKindsForDefaultFetchOptions() {
        addKinds()

        def kinds = datastore.kinds

        assert kinds.key.name == ["Comment", "Goal", "User"]
    }

    void testGetKindsForLimitedResultSet() {
        addKinds()

        def kinds = datastore.getKinds(withLimit(2))

        assert kinds.key.name == ["Comment", "Goal"]
    }

    void testGetKindsForFilteredResultSetWithDefaultFetchOptions() {
        addKinds()

        def kinds = datastore.getKinds { query ->
            query.addFilter(KEY_RESERVED_PROPERTY, GREATER_THAN_OR_EQUAL, makeKindKey("G"))
        }

        assert kinds.key.name == ["Goal", "User"]
    }

    void testGetKindsForFilteredResultSetWithLimitedFetchOptions() {
        addKinds()

        def kinds = datastore.getKinds(withLimit(1)) { query ->
            query.addFilter(KEY_RESERVED_PROPERTY, GREATER_THAN_OR_EQUAL, makeKindKey("G"))
        }

        assert kinds.key.name == ["Goal"]
    }

    void testGetPropertiesForDefaultFetchOptions() {
        addKinds()

        def properties = datastore.properties

        assert properties.key.parent.name == ["Comment", "Comment", "Goal", "Goal", "User", "User"]
        assert properties.key.name == ["text", "userId", "description", "name", "firstname", "lastname"]
    }

    void testGetPropertiesForLimitedResultSet() {
        addKinds()

        def properties = datastore.getProperties(withLimit(4))

        assert properties.key.parent.name == ["Comment", "Comment", "Goal", "Goal"]
        assert properties.key.name == ["text", "userId", "description", "name"]
    }

    void testGetPropertiesForFilteredResultSetWithDefaultFetchOptions() {
        addKinds()

        def properties = datastore.getProperties { query ->
            query.addFilter(KEY_RESERVED_PROPERTY, GREATER_THAN_OR_EQUAL, makeKindKey("G"))
        }

        assert properties.key.parent.name == ["Goal", "Goal", "User", "User"]
        assert properties.key.name == ["description", "name", "firstname", "lastname"]
    }

    void testGetPropertiesForFilteredResultSetWithLimitedFetchOptions() {
        addKinds()

        def properties = datastore.getProperties(withLimit(2)) { query ->
            query.addFilter(KEY_RESERVED_PROPERTY, GREATER_THAN_OR_EQUAL, makeKindKey("G"))
        }

        assert properties.key.parent.name == ["Goal", "Goal"]
        assert properties.key.name == ["description", "name"]
    }

    void testGetPropertiesForGoalKindWithDefaultFetchOptions() {
        addKinds()

        def properties = datastore.getProperties("Goal")

        assert properties.key.parent.name == ["Goal", "Goal"]
        assert properties.key.name == ["description", "name"]
    }

    void testGetPropertiesForGoalKindFilteredResultSetWithDefaultFetchOptions() {
        addKinds()

        def properties = datastore.getProperties("Goal", withLimit(1)) { query ->
            query.addFilter(KEY_RESERVED_PROPERTY, GREATER_THAN_OR_EQUAL, makePropertyKey("Goal", "d"))
        }

        assert properties.key.parent.name == ["Goal"]
        assert properties.key.name == ["description"]
    }

    void testGetPropertiesForGoalKindFilteredResultSetWithLimitedFetchOptions() {
        addKinds()

        def properties = datastore.getProperties("Goal") { query ->
            query.addFilter(KEY_RESERVED_PROPERTY, GREATER_THAN_OR_EQUAL, makePropertyKey("Goal", "n"))
        }

        assert properties.key.parent.name == ["Goal"]
        assert properties.key.name == ["name"]
    }

    void testGetPropertiesForGoalKindWithLimitedFetchOptions() {
        addKinds()

        def properties = datastore.getProperties("Goal", withLimit(1))

        assert properties.key.parent.name == ["Goal"]
        assert properties.key.name == ["description"]
    }

    void testGetProperty() {
        addKinds()

        def property = datastore.getProperty("Goal", "name")

        assert property != null
        assert property.getProperty("property_representation") == ["STRING"]
    }

    private void addNamespaces() {
        namespace.of("tenant1") {
            new Entity("User", "User").save()
        }

        namespace.of("tenant2") {
            new Entity("Goal", "Goal").save()
        }

        namespace.of("tenant3") {
            new Entity("Comment", "Comment").save()
        }
    }

    private void addKinds() {
        def user = new Entity("User", "User")
        user.firstname = "John"
        user.lastname = "Doe"

        def goal = new Entity("Goal", "Goal")
        goal.name = "Write book"
        goal.description = "Finish writing a novel"

        def comment = new Entity("Comment", "Comment")
        comment.userId = "123"
        comment.text = "Great posting!"

        user.save()
        goal.save()
        comment.save()
    }

    private Key makeNamespaceKey(String namespace) {
        KeyFactory.createKey(NAMESPACE_METADATA_KIND, namespace)
    }

    private Key makeKindKey(String kind) {
        KeyFactory.createKey(KIND_METADATA_KIND, kind)
    }

    private Key makePropertyKey(String kind, String property) {
        KeyFactory.createKey(makeKindKey(kind), PROPERTY_METADATA_KIND, property)
    }
}
