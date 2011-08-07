import com.google.appengine.api.datastore.*

def e = new Entity("person")
e.firstname = 'Marco'
e.lastname = 'Vermeulen'
e.save()