package groovyx.gaelyk.datastore;

import groovy.lang.MissingPropertyException;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.datastore.Key;

public class ExampleDatastoreEntity implements DatastoreEntity<Long> {

    private long id;
    private long version;
    private String indexed1;
    private int indexed2;
    private String unindexed1;
    private String unindexed2;
    private long ignored;
    private EDEType type;

    @Override public boolean hasDatastoreKey() {
        return true;
    }
    
    @Override public boolean hasDatastoreNumericKey() {
        return true;
    }

    @Override public Long getDatastoreKey() {
        return id;
    }

    @Override public void setDatastoreKey(Long key) {
        this.id = key;
    }

    @Override public boolean hasDatastoreVersion() {
        return true;
    }

    @Override public long getDatastoreVersion() {
        return version;
    }

    @Override public void setDatastoreVersion(long version) {
        this.version = version;
    }

    @Override public List<String> getDatastoreIndexedProperties() {
        return Arrays.asList("indexed1", "indexed2", "type");
    }

    @Override public List<String> getDatastoreUnindexedProperties() {
        return Arrays.asList("unindexed1", "unindexed2");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getIndexed1() {
        return indexed1;
    }

    public void setIndexed1(String indexed1) {
        this.indexed1 = indexed1;
    }

    public int getIndexed2() {
        return indexed2;
    }

    public void setIndexed2(int indexed2) {
        this.indexed2 = indexed2;
    }

    public String getUnindexed1() {
        return unindexed1;
    }

    public void setUnindexed1(String unindexed1) {
        this.unindexed1 = unindexed1;
    }

    public String getUnindexed2() {
        return unindexed2;
    }

    public void setUnindexed2(String unindexed2) {
        this.unindexed2 = unindexed2;
    }

    public long getIgnored() {
        return ignored;
    }

    public void setIgnored(long ignored) {
        this.ignored = ignored;
    }
    
    
    @Override public Object getProperty(String propertyName) {
        if(propertyName == "indexed1"){
            return this.indexed1;
        }
        if(propertyName == "indexed2"){
            return this.indexed2;
        }
        if(propertyName == "unindexed1"){
            return this.unindexed1;
        }
        if(propertyName == "unindexed2"){
            return this.unindexed2;
        }
        if(propertyName == "ignored"){
            return this.ignored;
        }
        if(propertyName == "id"){
            return this.id;
        }
        if(propertyName == "version"){
            return this.version;
        }
        if(propertyName == "type"){
            return this.type;
        }
        throw new MissingPropertyException("No such property " + propertyName);
    }
    
    @Override public void setProperty(String propertyName, Object value) {
        if(propertyName == "indexed1"){
            this.indexed1 = (String) value;
            return;
        } 
        if(propertyName == "indexed2"){
            this.indexed2 = (Integer) value;
            return;
        }
        if(propertyName == "unindexed1"){
            this.unindexed1 = (String) value;
            return;
        }
        if(propertyName == "unindexed2"){
            this.unindexed2 = (String) value;
            return;
        }
        if(propertyName == "ignored"){
            this.ignored = (Long) value;
            return;
        }
        if(propertyName == "id"){
            this.id = (Long) value;
            return;
        }
        if(propertyName == "version"){
            this.version = (Long) value;
            return;
        }
        if(propertyName == "type"){
            if(value instanceof EDEType){
                this.type = (EDEType) value;                
            } else if(value instanceof String){
                this.type = EDEType.valueOf((String)value);
            }
            return;
        }
        throw new MissingPropertyException("No such property " + propertyName);
    }

    @Override public boolean hasDatastoreParent() {
        return false;
    }

    @Override public Key getDatastoreParent() {
        return null;
    }

    @Override public void setDatastoreParent(Key parent) {}

    public EDEType getType() {
        return type;
    }

    public void setType(EDEType type) {
        this.type = type;
    }
    

}
