package groovyx.gaelyk

import com.google.appengine.api.datastore.Email
import com.google.appengine.api.datastore.Text
import com.google.appengine.api.blobstore.BlobKey
import com.google.appengine.api.datastore.Link
import com.google.appengine.api.datastore.PhoneNumber
import com.google.appengine.api.datastore.PostalAddress
import com.google.appengine.api.datastore.Rating
import com.google.appengine.api.xmpp.JID

class GaelykCategoryTest extends GroovyTestCase {

    void testMapToQueryString() {
        use(GaelykCategory) {
            assert [:].toQueryString() == ""
            assert [title: 'Harry Potter'].toQueryString() == "title=Harry+Potter"
            assert [a: 1, b: 2].toQueryString() == "a=1&b=2"
            assert [first: 'this is a field', second: 'was it clear (already)?'].toQueryString() ==
                    "first=this+is+a+field&second=was+it+clear+%28already%29%3F"
        }
    }

    void testStringCoercion() {
        use(GaelykCategory) {
            assert "foo@bar.com" as Email == new Email("foo@bar.com")
            assert "text" as Text == new Text("text")
            assert "1234" as BlobKey == new BlobKey("1234")
            assert "http://gaelyk.appspot.com" as Link == new Link("http://gaelyk.appspot.com")
            assert "123456" as PhoneNumber == new PhoneNumber("123456")
            assert "1, Main Street" as PostalAddress == new PostalAddress("1, Main Street")
            assert "5" as Rating == new Rating(5)
            assert ("foo@gmail.com" as JID).toString() == new JID("foo@gmail.com").toString()
        }
    }
}