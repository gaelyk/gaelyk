
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Switch rulesets to check for an explicitly specified rules only
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/*
ruleset {
    UnnecessaryGString
}
*/

ruleset {

    description 'Gaelyk CodeNarc RuleSet'

    ruleset( 'http://codenarc.sourceforge.net/StarterRuleSet-AllRulesByCategory.groovy.txt' ) {

        DuplicateNumberLiteral ( enabled : false )
        DuplicateStringLiteral ( enabled : false )
        LineLength             ( length  : 160   )
    }
}
