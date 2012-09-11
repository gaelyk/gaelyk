/*
 * Copyright 2009-2012 the original author or authors.
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
package groovyx.gaelyk.cache

import javax.servlet.http.HttpServletResponseWrapper
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse

/**
 * Cached response used to implement the caching capabilities through the URL routing filter
 * 
 * @author Guillaume Laforge
 */
@groovy.transform.CompileStatic
class CachedResponse extends HttpServletResponseWrapper {

    ByteArrayOutputStream output = new ByteArrayOutputStream(8192)
    CustomServletOutputStream stream = new CustomServletOutputStream()
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"))

    CachedResponse(HttpServletResponse response) {
        super(response)
        stream.out = output
    }

    /**
     * @return the associated writer
     */
    PrintWriter getWriter() { writer }

    /**
     * @return the associated output stream
     */
    ServletOutputStream getOutputStream() { stream }

    /**
     * Custom extension of <code>CustomServletOutpuStream</code>
     */
    static class CustomServletOutputStream extends ServletOutputStream {
        OutputStream out

        void write(int i) {
            out.write(i)
        }

        void write(byte[] bytes) {
            out.write(bytes)
        }

        void write(byte[] bytes, int offset, int length) {
            out.write(bytes, offset, length)
        }

        void flush() {
            out.flush()
        }

        void close() {
            out.close()
        }
    }
}