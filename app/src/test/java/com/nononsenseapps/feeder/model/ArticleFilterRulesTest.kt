package com.nononsenseapps.feeder.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArticleFilterRulesTest {
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
        assertEquals(ArticleRuleSet.EMPTY, ArticleRuleSet.parse(""))
        assertEquals(ArticleRuleSet.EMPTY, ArticleRuleSet.parse("   \n  \n"))
    }

    @Test
    fun parsesOneValidLine() {
        val result = ArticleRuleSet.parse("Title=spam")

        assertEquals(listOf(ArticleRule(ArticleRuleField.TITLE, "spam")), result.rules)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun fieldNameIsMatchedCaseInsensitively() {
        val result = ArticleRuleSet.parse("title=spam")

        assertEquals(listOf(ArticleRule(ArticleRuleField.TITLE, "spam")), result.rules)
    }

    @Test
    fun linesAreTrimmed() {
        val result = ArticleRuleSet.parse("   Title = spam   ")

        assertEquals(listOf(ArticleRule(ArticleRuleField.TITLE, "spam")), result.rules)
    }

    @Test
    fun splitsOnFirstEqualsOnlySoPatternsMayContainEquals() {
        val result = ArticleRuleSet.parse("URL=a=b")

        assertEquals(listOf(ArticleRule(ArticleRuleField.URL, "a=b")), result.rules)
    }

    @Test
    fun blankLinesAreSkippedWithoutShiftingLaterLineNumbers() {
        val result = ArticleRuleSet.parse("Title=a\n\n\nnonsense")

        assertEquals(listOf(ArticleRule(ArticleRuleField.TITLE, "a")), result.rules)
        assertEquals(listOf(ArticleRuleError.MissingSeparator(4)), result.errors)
    }

    @Test
    fun crlfParsesIdenticallyToLf() {
        assertEquals(
            ArticleRuleSet.parse("Title=a\nURL=b"),
            ArticleRuleSet.parse("Title=a\r\nURL=b"),
        )
    }

    @Test
    fun unknownFieldIsReportedAndSiblingsSurvive() {
        val result = ArticleRuleSet.parse("Foo=a\nTitle=b")

        assertEquals(listOf(ArticleRule(ArticleRuleField.TITLE, "b")), result.rules)
        assertEquals(listOf(ArticleRuleError.UnknownField(1, "Foo")), result.errors)
    }

    @Test
    fun missingSeparatorIsReported() {
        val result = ArticleRuleSet.parse("Title")

        assertTrue(result.rules.isEmpty())
        assertEquals(listOf(ArticleRuleError.MissingSeparator(1)), result.errors)
    }

    @Test
    fun emptyPatternIsRejectedAndIsNotAMatchEverythingRule() {
        val result = ArticleRuleSet.parse("Title=")

        assertTrue(result.rules.isEmpty())
        assertEquals(listOf(ArticleRuleError.EmptyPattern(1)), result.errors)
    }

    @Test
    fun invalidRegexIsReportedAndNoExceptionEscapes() {
        val result = ArticleRuleSet.parse("Title=(unclosed")

        assertTrue(result.rules.isEmpty())
        assertEquals(1, result.errors.size)
        assertTrue(result.errors.single() is ArticleRuleError.InvalidRegex)
        assertEquals(1, result.errors.single().lineNumber)
    }

    @Test
    fun duplicateRulesAreBothParsed() {
        val result = ArticleRuleSet.parse("Title=a\nTitle=a")

        assertEquals(2, result.rules.size)
    }

    @Test
    fun ruleCountIsCappedWithoutCrashing() {
        val text = (1..ArticleRuleSet.MAX_RULES + 50).joinToString("\n") { "Title=a$it" }

        val result = ArticleRuleSet.parse(text)

        assertEquals(ArticleRuleSet.MAX_RULES, result.rules.size)
        assertEquals(
            listOf(ArticleRuleError.TooManyRules(ArticleRuleSet.MAX_RULES + 1, ArticleRuleSet.MAX_RULES)),
            result.errors,
        )
    }

    // --------------------------------------------------------------- matching

    @Test
    fun patternsMatchAnywhereAndAreNotAnchored() {
        val rule = ArticleRule(ArticleRuleField.TITLE, "spam")

        assertTrue(rule.matches(article(title = "Delicious spam recipes")))
    }

    @Test
    fun matchingIsCaseSensitiveByDefault() {
        val rule = ArticleRule(ArticleRuleField.TITLE, "spam")

        assertFalse(rule.matches(article(title = "SPAM")))
    }

    @Test
    fun inlineCaseInsensitiveFlagWorks() {
        val rule = ArticleRule(ArticleRuleField.TITLE, "(?i)spam")

        assertTrue(rule.matches(article(title = "SPAM")))
    }

    @Test
    fun urlRuleReadsUrlAndNeverExternalUrl() {
        val rule = ArticleRule(ArticleRuleField.URL, "marker")
        val withExternalOnly =
            article(url = "https://example.com/a").copy(external_url = "https://example.com/marker")

        assertFalse(rule.matches(withExternalOnly))
        assertTrue(rule.matches(article(url = "https://example.com/marker")))
    }

    @Test
    fun authorRuleReadsAuthorName() {
        val rule = ArticleRule(ArticleRuleField.AUTHOR, "Bob")

        assertTrue(rule.matches(article(author = "Bob Smith")))
    }

    @Test
    fun missingAuthorMatchesEmptyStringButNotText() {
        assertTrue(ArticleRule(ArticleRuleField.AUTHOR, "^$").matches(article()))
        assertFalse(ArticleRule(ArticleRuleField.AUTHOR, "x").matches(article()))
    }

    @Test
    fun tagRuleMatchesIfAnyTagMatches() {
        val rule = ArticleRule(ArticleRuleField.TAG, "linux")

        assertTrue(rule.matches(article(tags = listOf("android", "linux"))))
        assertFalse(rule.matches(article(tags = listOf("android", "windows"))))
    }

    // Note this is about ArticleRule.matches only. On the allow side an untagged article
    // is judged by appliesTo instead - see the policy tests below.
    @Test
    fun untaggedArticleMatchesNoTagRuleNotEvenMatchAll() {
        val matchAll = ArticleRule(ArticleRuleField.TAG, ".*")

        assertFalse(matchAll.matches(article(tags = null)))
        assertFalse(matchAll.matches(article(tags = emptyList())))
    }

    @Test
    fun contentRuleReadsContentText() {
        val rule = ArticleRule(ArticleRuleField.CONTENT, "marker")

        assertTrue(rule.matches(article(contentText = "a marker here", contentHtml = "<p>nope</p>")))
    }

    @Test
    fun contentRuleFallsBackToContentHtml() {
        val rule = ArticleRule(ArticleRuleField.CONTENT, "marker")

        assertTrue(rule.matches(article(contentText = null, contentHtml = "<p>a marker here</p>")))
    }

    @Test
    fun contentRuleFallsBackToContentHtmlWhenContentTextIsBlank() {
        val rule = ArticleRule(ArticleRuleField.CONTENT, "sponsor\\.example")

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
    fun contentRuleNeverReadsSummary() {
        val rule = ArticleRule(ArticleRuleField.CONTENT, "uniquemarker")

        assertFalse(rule.matches(article(contentText = "body", summary = "uniquemarker")))
    }

    @Test
    fun anchorsApplyToTheWholeFieldValue() {
        assertTrue(ArticleRule(ArticleRuleField.TITLE, "^Ad: ").matches(article(title = "Ad: buy now")))
        assertFalse(ArticleRule(ArticleRuleField.TITLE, "^Ad: ").matches(article(title = "An Ad: buy now")))
        assertTrue(ArticleRule(ArticleRuleField.TITLE, "sponsored$").matches(article(title = "This is sponsored")))
    }

    @Test
    fun endAnchorDoesNotBindToTheTruncationPointOfLongValues() {
        val padding = "x".repeat(ArticleRule.MAX_MATCHED_LENGTH + 50_000)

        // The searched window ends mid-padding, but $ must still mean the real end.
        assertFalse(
            ArticleRule(ArticleRuleField.CONTENT, "x$").matches(article(contentText = padding)),
        )
        // ^ still anchors at the real start, which is inside the window.
        assertTrue(
            ArticleRule(ArticleRuleField.CONTENT, "^x").matches(article(contentText = padding)),
        )
        // Unanchored matching inside the window is unaffected.
        assertTrue(
            ArticleRule(ArticleRuleField.CONTENT, "marker")
                .matches(article(contentText = "marker$padding")),
        )
    }

    @Test
    fun contentBeyondTheLengthCapIsNotSearched() {
        val rule = ArticleRule(ArticleRuleField.CONTENT, "marker")
        val text = "x".repeat(ArticleRule.MAX_MATCHED_LENGTH + 10) + "marker"

        assertFalse(rule.matches(article(contentText = text)))
    }

    // ----------------------------------------------------------------- policy

    @Test
    fun compilingEmptyTextsGivesNone() {
        assertEquals(ArticleFilterRules.NONE, ArticleFilterRules.compile("", ""))
        assertTrue(ArticleFilterRules.NONE.isNoOp)
    }

    @Test
    fun blockedItemIsDropped() {
        val rules = ArticleFilterRules.compile("Title=spam", "")

        assertFalse(ArticleFilterPolicy.shouldKeep(article(title = "spam here"), rules))
    }

    @Test
    fun itemIsKeptWhenNothingBlocksAndNothingIsRequired() {
        val rules = ArticleFilterRules.compile("Title=spam", "")

        assertTrue(ArticleFilterPolicy.shouldKeep(article(title = "good stuff"), rules))
    }

    @Test
    fun allowRuleKeepsMatchingItem() {
        val rules = ArticleFilterRules.compile("", "Title=keep")

        assertTrue(ArticleFilterPolicy.shouldKeep(article(title = "please keep me"), rules))
    }

    @Test
    fun allowRuleDropsNonMatchingItem() {
        val rules = ArticleFilterRules.compile("", "Title=keep")

        assertFalse(ArticleFilterPolicy.shouldKeep(article(title = "something else"), rules))
    }

    @Test
    fun tagAllowRuleIsSkippedForUntaggedArticles() {
        val rules = ArticleFilterRules.compile("", "Tag=(?i)linux")

        // The whole point: a feed which emits no <category> is not emptied.
        assertTrue(ArticleFilterPolicy.shouldKeep(article(title = "no tags here"), rules))
        assertTrue(
            ArticleFilterPolicy.shouldKeep(article(title = "no tags here", tags = emptyList()), rules),
        )
    }

    @Test
    fun tagAllowRuleStillFiltersTaggedArticles() {
        val rules = ArticleFilterRules.compile("", "Tag=(?i)linux")

        assertTrue(ArticleFilterPolicy.shouldKeep(article(tags = listOf("Linux", "kernel")), rules))
        assertFalse(ArticleFilterPolicy.shouldKeep(article(tags = listOf("windows")), rules))
    }

    @Test
    fun otherAllowRulesStillApplyToUntaggedArticles() {
        val rules = ArticleFilterRules.compile("", "Tag=(?i)linux\nTitle=(?i)kernel")

        // The tag rule is skipped, but the title rule can still be evaluated.
        assertTrue(ArticleFilterPolicy.shouldKeep(article(title = "New kernel released"), rules))
        assertFalse(ArticleFilterPolicy.shouldKeep(article(title = "Unrelated news"), rules))
    }

    @Test
    fun tagBlockRuleStillDoesNotBlockUntaggedArticles() {
        val rules = ArticleFilterRules.compile("Tag=.*", "")

        // Unchanged behaviour - an untagged article matches no Tag rule.
        assertTrue(ArticleFilterPolicy.shouldKeep(article(title = "no tags here"), rules))
        assertFalse(ArticleFilterPolicy.shouldKeep(article(tags = listOf("anything")), rules))
    }

    @Test
    fun blockRuleWinsOverAllowRule() {
        val rules = ArticleFilterRules.compile("Title=spam", "Title=spam")

        assertFalse(ArticleFilterPolicy.shouldKeep(article(title = "spam"), rules))
    }

    @Test
    fun oneInvalidBlockRuleDoesNotDisableTheValidOne() {
        val rules = ArticleFilterRules.compile("Title=(unclosed\nTitle=spam", "")

        assertFalse(ArticleFilterPolicy.shouldKeep(article(title = "spam"), rules))
    }

    @Test
    fun allBlockRulesInvalidBlocksNothing() {
        val rules = ArticleFilterRules.compile("Title=(unclosed", "")

        assertTrue(ArticleFilterPolicy.shouldKeep(article(title = "anything"), rules))
    }

    @Test
    fun allAllowRulesInvalidKeepsEverythingRatherThanDroppingEverything() {
        val rules = ArticleFilterRules.compile("", "Title=(unclosed")

        assertTrue(ArticleFilterPolicy.shouldKeep(article(title = "anything"), rules))
    }
}
