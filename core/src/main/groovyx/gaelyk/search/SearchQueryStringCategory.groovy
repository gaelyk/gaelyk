package groovyx.gaelyk.search

import java.text.DateFormat
import java.text.SimpleDateFormat

class SearchQueryStringCategory {
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-M-d")

    static String greaterThan(String self, Object other){
        handleOverloadedComparisonOperator(' > ', self, other)
    }
    
    static String greaterThanEqual(String self, Object other){
        handleOverloadedComparisonOperator(' >= ', self, other)
    }
    
    static String lowerThan(String self, Object other){
        handleOverloadedComparisonOperator(' < ', self, other)
    }
    
    static String lowerThanEqual(String self, Object other){
        handleOverloadedComparisonOperator(' <= ', self, other)
    }
    
    static String isEqualTo(String self, Object other){
        handleOverloadedComparisonOperator(' = ', self, other)
    }
    
    static String isNotEqualTo(String self, Object other){
        "NOT (${handleOverloadedComparisonOperator(' = ', self, other)})"
    }
    
    static String isSameAs(String self, Object other){
        handleOverloadedComparisonOperator(': ', self, other)
    }
    
    static String and(String self, Object other){
        "($self) AND ($other)"
    }
    
    static String or(String self, Object other){
        "($self) OR ($other)"
    }
    
    static String not(String self){
        "NOT ($self)"
    }
    
    static String bitwiseNegate(String self){
        if(self.startsWith('"') && self.endsWith('"')){
            return "~$self"
        }
        "~\"$self\""
    }

    static String plus(String self, String other){
        handleOverloadedComparisonOperator(' + ', self, other)
    }

    static String plus(String self, Object other){
        handleOverloadedComparisonOperator(' + ', self, other)
    }
    
    static String minus(String self, Object other){
        handleOverloadedComparisonOperator(' - ', self, other)
    }
    
    static String multiply(String self, Object other){
        handleOverloadedComparisonOperator(' * ', self, other)
    }
    
    static String multiply(String self, Number other){
        handleOverloadedComparisonOperator(' * ', self, other)
    }
    
    static String div(String self, Object other){
        handleOverloadedComparisonOperator(' / ', self, other)
    }
    
    private static String handleOverloadedComparisonOperator(String operator, String self, Object other){
        String rightSide = other?.toString()
        if(other instanceof Date){
            return "$self$operator${DATE_FORMAT.format(other)}"
        }
        if(other instanceof Number){
            return "$self$operator$other"
        }
        return "$self$operator\"$other\""
    }
    
}
