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
import groovyx.gaelyk.SerializablePropertyResourceBundle 

log.info "Registering i18n plugin..."

binding {
        I18N_FILES_PATH = "WEB-INF/i18n"
        BUNDLE_NAME = "messages"
        DEFAULT_LOCALE = "pt_BR"

        // Instantiate your resource bundle for default locale here
        i18n = new SerializablePropertyResourceBundle(new PropertyResourceBundle(
                new FileReader("$I18N_FILES_PATH/${BUNDLE_NAME}_${DEFAULT_LOCALE}.properties")))
}

// Use this section if you want to use the browser locale
before {
        bundleName = "${binding.BUNDLE_NAME}_${request.locale}"
        if  (request.locale in memcache) {
            binding.i18n = memcache[request.locale]
        } else {
            bundle = new SerializablePropertyResourceBundle(new PropertyResourceBundle(
				new FileReader("${binding.I18N_FILES_PATH}/${bundleName}.properties")))
            binding.memcache.put(request.locale, bundle)
            binding.i18n = bundle
        }
}
