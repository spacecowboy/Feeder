package com.nononsenseapps.feeder.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntryFilterRulesTest {
    private fun article(
        title: String? = null,
        url: String? = null,
        author: String? = null,
        tags: List<String>? = null,
        contentText: String? = null,
        contentHtml: String? = null,
        summary: String? = null,
    ) = ParsedArticle(
        id = "id",
        url = url,
        title = title,
        content_html = contentHtml,
        content_text = contentText,
        summary = summary,
        author = author?.let { ParsedAuthor(name = it) },
        tags = tags,
    )

    // ---------------------------------------------------------------- parsing

    @Test
    fun blankTextParsesToEmptyRuleSet() {
        assertEquals(EntryRuleSet.EMPTY, EntryRules.parse(""))
        assertEquals(EntryRuleSet.EMPTY, EntryRules.parse("   \n  \n"))
    }

    @Test
    fun parsesOneValidLine() {
        val result = EntryRules.parse("EntryTitle=spam")

        assertEquals(listOf(EntryRule(EntryRuleField.ENTRY_TITLE, "spam")), result.rules)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun fieldNameIsMatchedCaseInsensitively() {
        val result = EntryRules.parse("entrytitle=spam")

        assertEquals(listOf(EntryRule(EntryRuleField.ENTRY_TITLE, "spam")), result.rules)
    }

    @Test
    fun linesAreTrimmed() {
        val result = EntryRules.parse("   EntryTitle = spam   ")

        assertEquals(listOf(EntryRule(EntryRuleField.ENTRY_TITLE, "spam")), result.rules)
    }

    @Test
    fun splitsOnFirstEqualsOnlySoPatternsMayContainEquals() {
        val result = EntryRules.parse("EntryURL=a=b")

        assertEquals(listOf(EntryRule(EntryRuleField.ENTRY_URL, "a=b")), result.rules)
    }

    @Test
    fun blankLinesAreSkippedWithoutShiftingLaterLineNumbers() {
        val result = EntryRules.parse("EntryTitle=a\n\n\nnonsense")

        assertEquals(listOf(EntryRule(EntryRuleField.ENTRY_TITLE, "a")), result.rules)
        assertEquals(listOf(EntryRuleError.MissingSeparator(4)), result.errors)
    }

    @Test
    fun crlfParsesIdenticallyToLf() {
        assertEquals(
            EntryRules.parse("EntryTitle=a\nEntryURL=b"),
            EntryRules.parse("EntryTitle=a\r\nEntryURL=b"),
        )
    }

    @Test
    fun unknownFieldIsReportedAndSiblingsSurvive() {
        val result = EntryRules.parse("EntryFoo=a\nEntryTitle=b")

        assertEquals(listOf(EntryRule(EntryRuleField.ENTRY_TITLE, "b")), result.rules)
        assertEquals(listOf(EntryRuleError.UnknownField(1, "EntryFoo")), result.errors)
    }

    @Test
    fun missingSeparatorIsReported() {
        val result = EntryRules.parse("EntryTitle")

        assertTrue(result.rules.isEmpty())
        assertEquals(listOf(EntryRuleError.MissingSeparator(1)), result.errors)
    }

    @Test
    fun emptyPatternIsRejectedAndIsNotAMatchEverythingRule() {
        val result = EntryRules.parse("EntryTitle=")

        assertTrue(result.rules.isEmpty())
        assertEquals(listOf(EntryRuleError.EmptyPattern(1)), result.errors)
    }

    @Test
    fun invalidRegexIsReportedAndNoExceptionEscapes() {
        val result = EntryRules.parse("EntryTitle=(unclosed")

        assertTrue(result.rules.isEmpty())
        assertEquals(1, result.errors.size)
        assertTrue(result.errors.single() is EntryRuleError.InvalidRegex)
        assertEquals(1, result.errors.single().lineNumber)
    }

    @Test
    fun duplicateRulesAreBothParsed() {
        val result = EntryRules.parse("EntryTitle=a\nEntryTitle=a")

        assertEquals(2, result.rules.size)
    }

    @Test
    fun ruleCountIsCappedWithoutCrashing() {
        val text = (1..EntryRules.MAX_RULES + 50).joinToString("\n") { "EntryTitle=a$it" }

        val result = EntryRules.parse(text)

        assertEquals(EntryRules.MAX_RULES, result.rules.size)
        assertEquals(
            listOf(EntryRuleError.TooManyRules(EntryRules.MAX_RULES + 1, EntryRules.MAX_RULES)),
            result.errors,
        )
    }

    // --------------------------------------------------------------- matching

    @Test
    fun patternsMatchAnywhereAndAreNotAnchored() {
        val rule = EntryRule(EntryRuleField.ENTRY_TITLE, "spam")

        assertTrue(rule.matches(article(title = "Delicious spam recipes")))
    }

    @Test
    fun matchingIsCaseSensitiveByDefault() {
        val rule = EntryRule(EntryRuleField.ENTRY_TITLE, "spam")

        assertFalse(rule.matches(article(title = "SPAM")))
    }

    @Test
    fun inlineCaseInsensitiveFlagWorks() {
        val rule = EntryRule(EntryRuleField.ENTRY_TITLE, "(?i)spam")

        assertTrue(rule.matches(article(title = "SPAM")))
    }

    @Test
    fun entryUrlReadsUrlAndNeverExternalUrl() {
        val rule = EntryRule(EntryRuleField.ENTRY_URL, "marker")
        val withExternalOnly =
            article(url = "https://example.com/a").copy(external_url = "https://example.com/marker")

        assertFalse(rule.matches(withExternalOnly))
        assertTrue(rule.matches(article(url = "https://example.com/marker")))
    }

    @Test
    fun entryAuthorReadsAuthorName() {
        val rule = EntryRule(EntryRuleField.ENTRY_AUTHOR, "Bob")

        assertTrue(rule.matches(article(author = "Bob Smith")))
    }

    @Test
    fun missingAuthorMatchesEmptyStringButNotText() {
        assertTrue(EntryRule(EntryRuleField.ENTRY_AUTHOR, "^$").matches(article()))
        assertFalse(EntryRule(EntryRuleField.ENTRY_AUTHOR, "x").matches(article()))
    }

    @Test
    fun entryTagMatchesIfAnyTagMatches() {
        val rule = EntryRule(EntryRuleField.ENTRY_TAG, "linux")

        assertTrue(rule.matches(article(tags = listOf("android", "linux"))))
        assertFalse(rule.matches(article(tags = listOf("android", "windows"))))
    }

    // Note this is about EntryRule.matches only. On the allow side an untagged article
    // is judged by appliesTo instead - see the policy tests below.
    @Test
    fun untaggedArticleMatchesNoTagRuleNotEvenMatchAll() {
        val matchAll = EntryRule(EntryRuleField.ENTRY_TAG, ".*")

        assertFalse(matchAll.matches(article(tags = null)))
        assertFalse(matchAll.matches(article(tags = emptyList())))
    }

    @Test
    fun entryContentReadsContentText() {
        val rule = EntryRule(EntryRuleField.ENTRY_CONTENT, "marker")

        assertTrue(rule.matches(article(contentText = "a marker here", contentHtml = "<p>nope</p>")))
    }

    @Test
    fun entryContentFallsBackToContentHtml() {
        val rule = EntryRule(EntryRuleField.ENTRY_CONTENT, "marker")

        assertTrue(rule.matches(article(contentText = null, contentHtml = "<p>a marker here</p>")))
    }

    @Test
    fun entryContentFallsBackToContentHtmlWhenContentTextIsBlank() {
        val rule = EntryRule(EntryRuleField.ENTRY_CONTENT, "sponsor\\.example")

        // content_text is never null in practice - plainContent strips markup and urls,
        // so a blank or markup-only body has to fall through to the html.
        assertTrue(
            rule.matches(
                article(
                    contentText = "",
                    contentHtml = "<a href=\"https://sponsor.example/promo\">here</a>",
                ),
            ),
        )
    }

    @Test
    fun entryContentNeverReadsSummary() {
        val rule = EntryRule(EntryRuleField.ENTRY_CONTENT, "uniquemarker")

        assertFalse(rule.matches(article(contentText = "body", summary = "uniquemarker")))
    }

    @Test
    fun anchorsApplyToTheWholeFieldValue() {
        assertTrue(EntryRule(EntryRuleField.ENTRY_TITLE, "^Ad: ").matches(article(title = "Ad: buy now")))
        assertFalse(EntryRule(EntryRuleField.ENTRY_TITLE, "^Ad: ").matches(article(title = "An Ad: buy now")))
        assertTrue(EntryRule(EntryRuleField.ENTRY_TITLE, "sponsored$").matches(article(title = "This is sponsored")))
    }

    @Test
    fun endAnchorDoesNotBindToTheTruncationPointOfLongValues() {
        val padding = "x".repeat(EntryRule.MAX_MATCHED_LENGTH + 50_000)

        // The searched window ends mid-padding, but $ must still mean the real end.
        assertFalse(
            EntryRule(EntryRuleField.ENTRY_CONTENT, "x$").matches(article(contentText = padding)),
        )
        // ^ still anchors at the real start, which is inside the window.
        assertTrue(
            EntryRule(EntryRuleField.ENTRY_CONTENT, "^x").matches(article(contentText = padding)),
        )
        // Unanchored matching inside the window is unaffected.
        assertTrue(
            EntryRule(EntryRuleField.ENTRY_CONTENT, "marker")
                .matches(article(contentText = "marker$padding")),
        )
    }

    @Test
    fun contentBeyondTheLengthCapIsNotSearched() {
        val rule = EntryRule(EntryRuleField.ENTRY_CONTENT, "marker")
        val text = "x".repeat(EntryRule.MAX_MATCHED_LENGTH + 10) + "marker"

        assertFalse(rule.matches(article(contentText = text)))
    }

    // ----------------------------------------------------------------- policy

    @Test
    fun compilingEmptyTextsGivesNone() {
        assertEquals(EntryFilterRules.NONE, EntryFilterRules.compile("", ""))
        assertTrue(EntryFilterRules.NONE.isNoOp)
    }

    @Test
    fun blockedItemIsDropped() {
        val rules = EntryFilterRules.compile("EntryTitle=spam", "")

        assertFalse(EntryFilterPolicy.shouldKeep(article(title = "spam here"), rules))
    }

    @Test
    fun itemIsKeptWhenNothingBlocksAndNothingIsRequired() {
        val rules = EntryFilterRules.compile("EntryTitle=spam", "")

        assertTrue(EntryFilterPolicy.shouldKeep(article(title = "good stuff"), rules))
    }

    @Test
    fun allowRuleKeepsMatchingItem() {
        val rules = EntryFilterRules.compile("", "EntryTitle=keep")

        assertTrue(EntryFilterPolicy.shouldKeep(article(title = "please keep me"), rules))
    }

    @Test
    fun allowRuleDropsNonMatchingItem() {
        val rules = EntryFilterRules.compile("", "EntryTitle=keep")

        assertFalse(EntryFilterPolicy.shouldKeep(article(title = "something else"), rules))
    }

    @Test
    fun tagAllowRuleIsSkippedForUntaggedArticles() {
        val rules = EntryFilterRules.compile("", "EntryTag=(?i)linux")

        // The whole point: a feed which emits no <category> is not emptied.
        assertTrue(EntryFilterPolicy.shouldKeep(article(title = "no tags here"), rules))
        assertTrue(
            EntryFilterPolicy.shouldKeep(article(title = "no tags here", tags = emptyList()), rules),
        )
    }

    @Test
    fun tagAllowRuleStillFiltersTaggedArticles() {
        val rules = EntryFilterRules.compile("", "EntryTag=(?i)linux")

        assertTrue(EntryFilterPolicy.shouldKeep(article(tags = listOf("Linux", "kernel")), rules))
        assertFalse(EntryFilterPolicy.shouldKeep(article(tags = listOf("windows")), rules))
    }

    @Test
    fun otherAllowRulesStillApplyToUntaggedArticles() {
        val rules = EntryFilterRules.compile("", "EntryTag=(?i)linux\nEntryTitle=(?i)kernel")

        // The tag rule is skipped, but the title rule can still be evaluated.
        assertTrue(EntryFilterPolicy.shouldKeep(article(title = "New kernel released"), rules))
        assertFalse(EntryFilterPolicy.shouldKeep(article(title = "Unrelated news"), rules))
    }

    @Test
    fun tagBlockRuleStillDoesNotBlockUntaggedArticles() {
        val rules = EntryFilterRules.compile("EntryTag=.*", "")

        // Unchanged behaviour - an untagged article matches no EntryTag rule.
        assertTrue(EntryFilterPolicy.shouldKeep(article(title = "no tags here"), rules))
        assertFalse(EntryFilterPolicy.shouldKeep(article(tags = listOf("anything")), rules))
    }

    @Test
    fun blockRuleWinsOverAllowRule() {
        val rules = EntryFilterRules.compile("EntryTitle=spam", "EntryTitle=spam")

        assertFalse(EntryFilterPolicy.shouldKeep(article(title = "spam"), rules))
    }

    @Test
    fun oneInvalidBlockRuleDoesNotDisableTheValidOne() {
        val rules = EntryFilterRules.compile("EntryTitle=(unclosed\nEntryTitle=spam", "")

        assertFalse(EntryFilterPolicy.shouldKeep(article(title = "spam"), rules))
    }

    @Test
    fun allBlockRulesInvalidBlocksNothing() {
        val rules = EntryFilterRules.compile("EntryTitle=(unclosed", "")

        assertTrue(EntryFilterPolicy.shouldKeep(article(title = "anything"), rules))
    }

    @Test
    fun allAllowRulesInvalidKeepsEverythingRatherThanDroppingEverything() {
        val rules = EntryFilterRules.compile("", "EntryTitle=(unclosed")

        assertTrue(EntryFilterPolicy.shouldKeep(article(title = "anything"), rules))
    }
}
