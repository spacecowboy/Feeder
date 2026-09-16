package com.nononsenseapps.feeder.model

import java.util.regex.PatternSyntaxException

/**
 * The article fields a per-feed filtering rule can match against.
 *
 * Modelled on Miniflux's entry filtering rules, minus the fields Feeder cannot
 * support: EntryDate (non-regex syntax) and EntryCommentsURL (not carried by the
 * native gofeed bridge).
 */
enum class EntryRuleField(
    val fieldName: String,
) {
    ENTRY_TITLE("EntryTitle"),
    ENTRY_URL("EntryURL"),
    ENTRY_AUTHOR("EntryAuthor"),
    ENTRY_TAG("EntryTag"),
    ENTRY_CONTENT("EntryContent"),
    ;

    companion object {
        fun fromFieldName(name: String): EntryRuleField? = entries.firstOrNull { it.fieldName.equals(name, ignoreCase = true) }
    }
}

/**
 * A single `FieldName=regex` rule.
 *
 * [regex] lives in the class body, not the primary constructor, so data-class
 * equals/hashCode cover only (field, pattern) — Regex has identity equality.
 * The constructor THROWS on a bad pattern; always build via [EntryRuleSet.parse].
 */
data class EntryRule(
    val field: EntryRuleField,
    val pattern: String,
) {
    private val regex: Regex = Regex(pattern)

    fun matches(article: ParsedArticle): Boolean =
        when (field) {
            EntryRuleField.ENTRY_TITLE -> matchesValue(article.title)
            EntryRuleField.ENTRY_URL -> matchesValue(article.url)
            EntryRuleField.ENTRY_AUTHOR -> matchesValue(article.author?.name)
            // content_text is never null for parsed feeds (it is FeederGoItem.plainContent,
            // "" at worst), so the fallback has to test for blankness rather than null or
            // markup-targeting rules could never reach the HTML at all.
            EntryRuleField.ENTRY_CONTENT ->
                matchesValue(article.content_text?.takeIf { it.isNotBlank() } ?: article.content_html)
            // Miniflux iterates entry.Tags and stops on first hit, so an entry
            // with no tags matches no EntryTag rule at all — not even ".*".
            // See [appliesTo] for why that is not enough on the allow side.
            EntryRuleField.ENTRY_TAG -> article.tags?.any { matchesValue(it) } == true
        }

    /**
     * Whether this rule can say anything meaningful about [article].
     *
     * Only EntryTag can be inapplicable: many feeds emit no <category> at all, and an
     * untagged entry matches no EntryTag rule — not even ".*". On the block side that is
     * harmless, but as an allow rule it would drop every entry in such a feed. Treating
     * the rule as inapplicable instead means an untagged entry is judged only by the
     * allow rules that can actually be evaluated against it.
     */
    fun appliesTo(article: ParsedArticle): Boolean =
        when (field) {
            EntryRuleField.ENTRY_TAG -> !article.tags.isNullOrEmpty()
            else -> true
        }

    /** Missing scalars match as "", mirroring Go's zero-value strings. */
    private fun matchesValue(value: String?): Boolean {
        val text = value ?: ""

        if (text.length <= MAX_MATCHED_LENGTH) {
            return regex.containsMatchIn(text)
        }

        // Only the first MAX_MATCHED_LENGTH characters are searched, but the cut-off
        // point must not masquerade as the end of the text. Transparent, non-anchoring
        // bounds evaluate ^ and $ against the whole value, so ^ still anchors at the
        // real start while an end-anchored rule simply does not match instead of
        // matching at MAX_MATCHED_LENGTH.
        return regex
            .toPattern()
            .matcher(text)
            .region(0, MAX_MATCHED_LENGTH)
            .useTransparentBounds(true)
            .useAnchoringBounds(false)
            .find()
    }

    companion object {
        /**
         * Bounds worst-case backtracking on large article bodies. Values longer than
         * this are searched only up to this length; see [matchesValue] for how the
         * anchors behave there.
         */
        const val MAX_MATCHED_LENGTH = 100_000
    }
}

/** Why one line was rejected. [lineNumber] is 1-based over the raw text. */
sealed interface EntryRuleError {
    val lineNumber: Int

    data class MissingSeparator(
        override val lineNumber: Int,
    ) : EntryRuleError

    data class UnknownField(
        override val lineNumber: Int,
        val fieldName: String,
    ) : EntryRuleError

    data class EmptyPattern(
        override val lineNumber: Int,
    ) : EntryRuleError

    data class InvalidRegex(
        override val lineNumber: Int,
        val reason: String,
    ) : EntryRuleError

    /** The line is valid but exceeds [EntryRuleSet.MAX_RULES]; it and every later line are ignored. */
    data class TooManyRules(
        override val lineNumber: Int,
        val maxRules: Int,
    ) : EntryRuleError
}

data class EntryRuleSet(
    val rules: List<EntryRule>,
    val errors: List<EntryRuleError>,
) {
    companion object {
        val EMPTY = EntryRuleSet(emptyList(), emptyList())

        const val MAX_RULES = 100

        /**
         * Parses one `FieldName=regex` rule per line. Blank lines are skipped without
         * shifting the line numbers of later lines. Invalid lines produce an
         * [EntryRuleError] instead of throwing, and are simply left out of the result.
         */
        fun parse(text: String): EntryRuleSet {
            if (text.isBlank()) {
                return EntryRuleSet.EMPTY
            }

            val rules = mutableListOf<EntryRule>()
            val errors = mutableListOf<EntryRuleError>()

            for ((index, rawLine) in text.lineSequence().withIndex()) {
                val lineNumber = index + 1
                // Also disposes of the \r in CRLF line endings.
                val line = rawLine.trim()

                if (line.isEmpty()) {
                    continue
                }

                val separatorIndex = line.indexOf('=')
                if (separatorIndex < 0) {
                    errors.add(EntryRuleError.MissingSeparator(lineNumber))
                    continue
                }

                val fieldName = line.substring(0, separatorIndex).trim()
                // Split on the first '=' only, so patterns may contain '='.
                val pattern = line.substring(separatorIndex + 1).trim()

                val field = EntryRuleField.fromFieldName(fieldName)
                if (field == null) {
                    errors.add(EntryRuleError.UnknownField(lineNumber, fieldName))
                    continue
                }

                // Deliberate deviation from Miniflux: an empty pattern compiles fine and
                // matches everything, which would silently swallow an entire feed.
                if (pattern.isEmpty()) {
                    errors.add(EntryRuleError.EmptyPattern(lineNumber))
                    continue
                }

                // Reported rather than silently dropped: an unnoticed cap on an allow
                // list would discard every article matching only the dropped rules.
                if (rules.size >= MAX_RULES) {
                    errors.add(EntryRuleError.TooManyRules(lineNumber, MAX_RULES))
                    break
                }

                try {
                    rules.add(EntryRule(field, pattern))
                } catch (e: PatternSyntaxException) {
                    errors.add(EntryRuleError.InvalidRegex(lineNumber, e.description ?: e.message ?: ""))
                }
            }

            return EntryRuleSet(rules, errors)
        }
    }
}

data class EntryFilterRules(
    val blockRules: List<EntryRule>,
    val allowRules: List<EntryRule>,
) {
    val isNoOp: Boolean get() = blockRules.isEmpty() && allowRules.isEmpty()

    companion object {
        val NONE = EntryFilterRules(emptyList(), emptyList())

        fun compile(
            blockRulesText: String,
            allowRulesText: String,
        ): EntryFilterRules {
            val blockRules = EntryRuleSet.parse(blockRulesText).rules
            val allowRules = EntryRuleSet.parse(allowRulesText).rules
            return if (blockRules.isEmpty() && allowRules.isEmpty()) {
                NONE
            } else {
                EntryFilterRules(blockRules, allowRules)
            }
        }
    }
}

internal object EntryFilterPolicy {
    /**
     * Miniflux semantics: block rules are evaluated first and the first match drops
     * the item. Then, if any allow rules exist, the item must match at least one.
     *
     * Deviation from Miniflux: only the allow rules that [EntryRule.appliesTo] this
     * article get a vote. An EntryTag rule cannot judge an entry with no categories,
     * so a feed that emits none is not emptied by "EntryTag=..." alone. Allow rules on
     * other fields still apply as usual, so a mixed allow list keeps working.
     *
     * Note that invalid rules are dropped at parse time, so a feed whose rules are
     * all invalid fails open — everything is kept, in both directions. That is the
     * only safe direction for a filter that drops items permanently.
     */
    fun shouldKeep(
        article: ParsedArticle,
        rules: EntryFilterRules,
    ): Boolean =
        when {
            rules.isNoOp -> true
            rules.blockRules.any { it.matches(article) } -> false
            rules.allowRules.isEmpty() -> true
            else ->
                rules.allowRules
                    .filter { it.appliesTo(article) }
                    .let { applicable -> applicable.isEmpty() || applicable.any { it.matches(article) } }
        }
}
