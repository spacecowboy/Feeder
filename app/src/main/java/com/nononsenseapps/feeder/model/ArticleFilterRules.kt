package com.nononsenseapps.feeder.model

import java.util.regex.PatternSyntaxException

/**
 * The article fields a per-feed filtering rule can match against.
 *
 * Modelled on Miniflux's entry filtering rules, minus the fields Feeder cannot
 * support: Miniflux's EntryDate (non-regex syntax) and EntryCommentsURL (not carried
 * by the native gofeed bridge).
 *
 * The names deliberately drop Miniflux's "Entry" prefix - Feeder calls these articles,
 * so a rule reads "Title=..." rather than "EntryTitle=...".
 */
enum class ArticleRuleField(
    val fieldName: String,
) {
    TITLE("Title"),
    URL("URL"),
    AUTHOR("Author"),
    TAG("Tag"),
    CONTENT("Content"),
    ;

    companion object {
        fun fromFieldName(name: String): ArticleRuleField? = entries.firstOrNull { it.fieldName.equals(name, ignoreCase = true) }
    }
}

/**
 * A single `FieldName=regex` rule.
 *
 * [regex] lives in the class body, not the primary constructor, so data-class
 * equals/hashCode cover only (field, pattern) — Regex has identity equality.
 * The constructor THROWS on a bad pattern; always build via [ArticleRuleSet.parse].
 */
data class ArticleRule(
    val field: ArticleRuleField,
    val pattern: String,
) {
    private val regex: Regex = Regex(pattern)

    fun matches(article: ParsedArticle): Boolean =
        when (field) {
            ArticleRuleField.TITLE -> matchesValue(article.title)
            ArticleRuleField.URL -> matchesValue(article.url)
            ArticleRuleField.AUTHOR -> matchesValue(article.author?.name)
            // content_text is never null for parsed feeds (it is FeederGoItem.plainContent,
            // "" at worst), so the fallback has to test for blankness rather than null or
            // markup-targeting rules could never reach the HTML at all.
            ArticleRuleField.CONTENT ->
                matchesValue(article.content_text?.takeIf { it.isNotBlank() } ?: article.content_html)
            // Miniflux iterates entry.Tags and stops on first hit, so an article
            // with no tags matches no Tag rule at all — not even ".*".
            // See [appliesTo] for why that is not enough on the allow side.
            ArticleRuleField.TAG -> article.tags?.any { matchesValue(it) } == true
        }

    /**
     * Whether this rule can say anything meaningful about [article].
     *
     * Only Tag can be inapplicable: many feeds emit no <category> at all, and an
     * untagged article matches no Tag rule — not even ".*". On the block side that is
     * harmless, but as an allow rule it would drop every article in such a feed. Treating
     * the rule as inapplicable instead means an untagged article is judged only by the
     * allow rules that can actually be evaluated against it.
     */
    fun appliesTo(article: ParsedArticle): Boolean =
        when (field) {
            ArticleRuleField.TAG -> !article.tags.isNullOrEmpty()
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
sealed interface ArticleRuleError {
    val lineNumber: Int

    data class MissingSeparator(
        override val lineNumber: Int,
    ) : ArticleRuleError

    data class UnknownField(
        override val lineNumber: Int,
        val fieldName: String,
    ) : ArticleRuleError

    data class EmptyPattern(
        override val lineNumber: Int,
    ) : ArticleRuleError

    data class InvalidRegex(
        override val lineNumber: Int,
        val reason: String,
    ) : ArticleRuleError

    /** The line is valid but exceeds [ArticleRuleSet.MAX_RULES]; it and every later line are ignored. */
    data class TooManyRules(
        override val lineNumber: Int,
        val maxRules: Int,
    ) : ArticleRuleError
}

data class ArticleRuleSet(
    val rules: List<ArticleRule>,
    val errors: List<ArticleRuleError>,
) {
    companion object {
        val EMPTY = ArticleRuleSet(emptyList(), emptyList())

        const val MAX_RULES = 100

        /**
         * Parses one `FieldName=regex` rule per line. Blank lines are skipped without
         * shifting the line numbers of later lines. Invalid lines produce an
         * [ArticleRuleError] instead of throwing, and are simply left out of the result.
         */
        fun parse(text: String): ArticleRuleSet {
            if (text.isBlank()) {
                return ArticleRuleSet.EMPTY
            }

            val rules = mutableListOf<ArticleRule>()
            val errors = mutableListOf<ArticleRuleError>()

            for ((index, rawLine) in text.lineSequence().withIndex()) {
                val lineNumber = index + 1
                // Also disposes of the \r in CRLF line endings.
                val line = rawLine.trim()

                if (line.isEmpty()) {
                    continue
                }

                val separatorIndex = line.indexOf('=')
                if (separatorIndex < 0) {
                    errors.add(ArticleRuleError.MissingSeparator(lineNumber))
                    continue
                }

                val fieldName = line.substring(0, separatorIndex).trim()
                // Split on the first '=' only, so patterns may contain '='.
                val pattern = line.substring(separatorIndex + 1).trim()

                val field = ArticleRuleField.fromFieldName(fieldName)
                if (field == null) {
                    errors.add(ArticleRuleError.UnknownField(lineNumber, fieldName))
                    continue
                }

                // Deliberate deviation from Miniflux: an empty pattern compiles fine and
                // matches everything, which would silently swallow an entire feed.
                if (pattern.isEmpty()) {
                    errors.add(ArticleRuleError.EmptyPattern(lineNumber))
                    continue
                }

                // Reported rather than silently dropped: an unnoticed cap on an allow
                // list would discard every article matching only the dropped rules.
                if (rules.size >= MAX_RULES) {
                    errors.add(ArticleRuleError.TooManyRules(lineNumber, MAX_RULES))
                    break
                }

                try {
                    rules.add(ArticleRule(field, pattern))
                } catch (e: PatternSyntaxException) {
                    errors.add(ArticleRuleError.InvalidRegex(lineNumber, e.description ?: e.message ?: ""))
                }
            }

            return ArticleRuleSet(rules, errors)
        }
    }
}

data class ArticleFilterRules(
    val blockRules: List<ArticleRule>,
    val allowRules: List<ArticleRule>,
) {
    val isNoOp: Boolean get() = blockRules.isEmpty() && allowRules.isEmpty()

    companion object {
        val NONE = ArticleFilterRules(emptyList(), emptyList())

        fun compile(
            blockRulesText: String,
            allowRulesText: String,
        ): ArticleFilterRules {
            val blockRules = ArticleRuleSet.parse(blockRulesText).rules
            val allowRules = ArticleRuleSet.parse(allowRulesText).rules
            return if (blockRules.isEmpty() && allowRules.isEmpty()) {
                NONE
            } else {
                ArticleFilterRules(blockRules, allowRules)
            }
        }
    }
}

internal object ArticleFilterPolicy {
    /**
     * Miniflux semantics: block rules are evaluated first and the first match drops
     * the item. Then, if any allow rules exist, the item must match at least one.
     *
     * Deviation from Miniflux: only the allow rules that [ArticleRule.appliesTo] this
     * article get a vote. A Tag rule cannot judge an article with no categories,
     * so a feed that emits none is not emptied by "Tag=..." alone. Allow rules on
     * other fields still apply as usual, so a mixed allow list keeps working.
     *
     * Note that invalid rules are dropped at parse time, so a feed whose rules are
     * all invalid fails open — everything is kept, in both directions. That is the
     * only safe direction for a filter that drops items permanently.
     */
    fun shouldKeep(
        article: ParsedArticle,
        rules: ArticleFilterRules,
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
