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
package groovyx.gaelyk.query

import com.google.appengine.api.datastore.Query

/**
 * A sort clause representing an ordering along a certain entity property.
 * The syntax of a sort clause looks like <code>sort desc by column</code> or <code>sort asc by column</code>.
 *
 * @author Guillaume Laforge
 *
 * @since 1.0
 */
class SortClause extends Clause {
    /**
     * The direction used for sorting: either ascending or descending.
     */
    Query.SortDirection direction

    /**
     * The by() method is the second part of the sort clause, and references the column on which the sorting is done.
     *
     * @param col the column on which the sort clause is applied
     * @return the query builder, for chaining purpose
     * @throws QuerySyntaxException when a problem is encountered in the syntax of the sort clause
     */
    QueryBuilder by(col) {
        if (builder.@coercedClass) {
            if (!builder.@coercedClass.metaClass.properties.name.contains(col)) {
                throw new QuerySyntaxException("Your sort clause on '${col}' is not possible as ${builder.@coercedClass.name} doesn't contain that property")
            }
        }

        column = col.toString()
        builder.@clauses << this
        return builder
    }
}