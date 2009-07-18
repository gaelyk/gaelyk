/*
 * Copyright 2009 the original author or authors.
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
package groovyx.gaelyk.servlet

import groovy.servlet.ServletBinding
import groovy.servlet.TemplateServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * The Gaelyk template servlet extends Groovy's own template servlet 
 * to inject Google App Engine dedicated services in the binding of the Groolets.
 *
 * @author Marcel Overdijk
 * @author Guillaume Laforge
 *
 * @see groovy.servlet.TemplateServlet
 */
class GaelykTemplateServlet extends TemplateServlet {

    @Override
    protected void setVariables(ServletBinding binding) {
        GaelykBindingEnhancer enhancer = new GaelykBindingEnhancer(binding)
        enhancer.bind()
    }

    @Override
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        use (GaelykCategory) {
            super.service(request, response)
        }
    }
}
