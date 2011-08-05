/*
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk

import java.text.MessageFormat

/**
 * The SerializablePropertyResourceBundle directly extends Properties 
 * because the classic java.util.PropertyResourceBundle isn't serializable itself.
 * It is used by i18nPlugin to store message.properties files in memcache, 
 * for better performance, when user decides to use the current browser locale.
 * 
 * This code was adapted from http://www.thatsjava.com/java-programming/133561/
 *
 * @author Serge Rehem
 */
class SerializablePropertyResourceBundle extends Properties implements Serializable {

	/**
     * This constructor receives the classic PropertyResourceBundle and store
     * all Properties calling setProperty() method.
     */
	public SerializablePropertyResourceBundle(PropertyResourceBundle rb) {
		String key;
		for (Enumeration e = rb.getKeys(); e.hasMoreElements() ;) {
			key = (String)e.nextElement();
			setProperty(key, rb.getString(key));
		}
	}

	@Override
	public String getString(String str) { 
		return getProperty(str);
	}

	@Override
	public String getProperty(String str) {
		super.getProperty(str)
	}

	/**
	 * Shortcut to get a message.properties key using 
     * $i18n.entryName notation
     */
    def propertyMissing(String name) { 
		this.getString(name) 
	}

	/**
	 * Shortcut to get a message.properties key using 
     * ${i18n.entryName(arg1, arg2, ..., argN)} notation
     */
    def methodMissing(String name, args) {
		MessageFormat.format(this.getString(name), args)
    }
}
