/*
 * Copyright 2009-2010 the original author or authors.
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
package groovyx.gaelyk.routes

import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletOutputStream

/**
 * Cached response used to implement the caching capabilities through the URL routing filter
 * 
 * @author Guillaume Laforge
 */
class CachedResponse /* implements HttpServletResponse */ {
    @Delegate HttpServletResponse response

    ByteArrayOutputStream output = new ByteArrayOutputStream(8192)

    /**
     * @return the associated writer
     */
    PrintWriter getWriter() {
        new PrintWriter(output)
    }

    /**
     * @return the associated output stream
     */
    ServletOutputStream getOutputStream() {
        new CustomServletOutputStream(output)
    }

    /**
     * Custom extension of <code>CustomServletOutpuStream</code>
     */
    static class CustomServletOutputStream extends ServletOutputStream {
        final OutputStream output

        protected CustomServletOutputStream() {
            super()
        }

        CustomServletOutputStream(OutputStream output) {
            this.output = output
        }

        void write(int i) {
            output << i
        }

        void write(byte[] bytes, int offset, int length) {
            output.write(bytes, offset, length)
        }

        void flush() {
            output.flush()
        }

        void close() {
            output.close()
        }
    }
}