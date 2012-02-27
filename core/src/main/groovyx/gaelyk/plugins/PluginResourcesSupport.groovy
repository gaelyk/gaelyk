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
package groovyx.gaelyk.plugins

import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.util.ResourceException
import groovyx.gaelyk.plugins.PluginsHandler



/**
 * This class provides support for accessing resources from the binary plugins.
 * 
 * @author Vladimir Orany
 *
 */
public class PluginResourceSupport {

	public static final String PLUGIN_RESOURCES_PREFIX = "/gaelyk-plugins/"
	
	private static final String GROOVY_FILE_EXT = ".groovy"
	private static final Pattern GAELYK_PLUGIN_URL_PATTERN = Pattern.compile("/?gaelyk-plugins/([a-zA-Z0-9]+)/(.*)")


	private PluginResourceSupport() {}

	/**
	 * Obtains the resource connection to the given file for specified folder and name.
	 * @param folder the name of the folder in binary plugin such as "groovy" for groovlets or "templates" for gtpl files
	 * @param name name of file to be found
	 * @return resource connection to the given file for specified folder and name
	 * @throws ResourceException if given file doesn't exist
	 */
	public static URLConnection getResourceConnection(String folder, String name) throws ResourceException {
		try {
			return getPluginFileURL(folder, name).openConnection()
		} catch (IOException e) {
			throw new ResourceException("Error openning file url connection", e)
		}
	}

	/**
	 * Checks whether the given path belongs to binary plugin.
	 * @param path path to be checked
	 * @return <code>true</code> if the given path belongs to binary plugin
	 */
	public static boolean isPluginPath(String path){
		if(path == null){
			return false
		}
		Matcher matcher = GAELYK_PLUGIN_URL_PATTERN.matcher(path)
		return matcher.matches()
	}
	
	/**
	* Obtains the URL for the given file for specified folder and name.
	* @param folder the name of the folder in binary plugin such as "groovy" for groovlets or "templates" for gtpl files
	* @param name name of file to be found
	* @return URL for the given file for specified folder and name
	* @throws ResourceException if given file doesn't exist
	*/
	public static URL getPluginFileURL(String folder, String name) throws ResourceException {
		if(name.startsWith("file:")){
			try {
				return new URL(name)
			} catch (MalformedURLException e) {
				throw new ResourceException("Error creating file url", e)
			}
		}

		String pluginName = getPluginName(name)
		if(!PluginsHandler.instance.isInstalled(pluginName)){
			throw new ResourceException("Resource \"" + name + "\" not found! The plugin is not installed!")
		}


		try {
			return new URL(getPluginBase(pluginName) + folder + "/" + getResourceName(name))
		} catch (MalformedURLException e) {
			throw new ResourceException("Resource \"" + name + "\" not found!")
		}
	}
	
	// helper methods

	private static String getPluginName(String path){
		if(path == null){
			return null;
		}
		Matcher matcher = GAELYK_PLUGIN_URL_PATTERN.matcher(path)
		if(matcher.matches()){
			return matcher.group(1)
		} else {
			return null;
		}
	}

	private static String getResourceName(String path){
		if(path == null){
			return null
		}
		Matcher matcher = GAELYK_PLUGIN_URL_PATTERN.matcher(path)
		if(matcher.matches()){
			return matcher.group(2)
		} else {
			return null
		}
	}

	private static String getPluginDescriptorURL(String name){
		if(name == null){
			return null
		}
		String tryName = "META-INF/gaelyk-plugins/" + name + GROOVY_FILE_EXT
		URL url = Thread.currentThread().getContextClassLoader().getResource(tryName)
		if(url != null){
			return url.toExternalForm()
		}

		tryName = "/" + tryName

		url = Thread.currentThread().getContextClassLoader().getResource(tryName)
		if(url != null){
			return url.toExternalForm()
		}
		return null
	}

	private static String getPluginBase(String name){
		String pluginDescriptorURL = getPluginDescriptorURL(name)
		if(pluginDescriptorURL == null){
			return null
		}
		return pluginDescriptorURL.substring(0, pluginDescriptorURL.length() - GROOVY_FILE_EXT.length() - name.length())
	}
}
