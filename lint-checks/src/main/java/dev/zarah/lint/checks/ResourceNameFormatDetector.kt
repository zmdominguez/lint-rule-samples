package dev.zarah.lint.checks

import com.android.SdkConstants
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Attr

/**
 * This Lint rule checks if a widget's ID is formatted correctly. IDs are considered
 * valid if it uses lower `snake_case` and _does not_ start with an underscore ("`_`").
 */
@Suppress("UnstableApiUsage")
class ResourceNameFormatDetector : LayoutDetector() {

    private val regexPattern by lazy { "[A-Z]+".toRegex() }

    override fun getApplicableAttributes(): Collection<String> {
        return listOf(SdkConstants.ATTR_ID)
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) {

        // Names should not start with underscores
        val isValidNameStart = valueHasNoLeadingUnderscores(attribute)

        // Find all groups of capital letters
        val results = regexPattern.findAll(attribute.value)

        // The ID is formatted properly, nothing to report
        if (isValidNameStart && results.count() == 0) return

        // Otherwise, report the issue and suggest a fix
        context.report(
            issue = ResourceNameFormatDetector.ISSUE,
            location = context.parser.getValueLocation(context, attribute),
            message = "Improper resource name format",
            quickfixData = createQuickfix(context, attribute, results.toList())
        )
    }

    private fun valueHasNoLeadingUnderscores(attribute: Attr) =
        !attribute.value.substringAfter(SdkConstants.NEW_ID_PREFIX).startsWith("_")

    private fun createQuickfix(
        context: XmlContext,
        attribute: Attr,
        matches: List<MatchResult>
    ): LintFix {

        val rawIdValue = attribute.value

        val replacementBuilder = StringBuilder()
        var prefixPositionEnd =
            rawIdValue.indexOf(SdkConstants.NEW_ID_PREFIX) + SdkConstants.NEW_ID_PREFIX.length
        var lastMatchEndIndex = 0

        // If the ID starts with an invalid character, drop invalid characters first
        if (!valueHasNoLeadingUnderscores(attribute)) {
            val actualIdValue = rawIdValue.substringAfter(SdkConstants.NEW_ID_PREFIX)
            val trimmedName = actualIdValue.trimStart('_')

            // Add the new ID prefix
            replacementBuilder.append(SdkConstants.NEW_ID_PREFIX)

            // Only leading characters are incorrect for this ID
            if (matches.isEmpty()) {
                replacementBuilder.append(trimmedName)
            } else {
                // Adjust indices for number of leading underscores dropped
                val droppedCharLength = actualIdValue.length - trimmedName.length
                lastMatchEndIndex += droppedCharLength + replacementBuilder.length
                prefixPositionEnd += droppedCharLength
            }
        }

        // Then deal with the uppercase letters
        matches.forEachIndexed { index, matchResult ->
            // Get the chunk of the value in between matches
            val inBetweenChunk = rawIdValue.substring(lastMatchEndIndex, matchResult.range.first)
            replacementBuilder.append(inBetweenChunk)

            // We do not want the new name to start with an underscore
            val isValueFirstLetterUppercase = index == 0 && matchResult.range.first == prefixPositionEnd
            val prevChunkEndsInUnderscore = inBetweenChunk.endsWith("_")
            if (!isValueFirstLetterUppercase && !prevChunkEndsInUnderscore) {
                replacementBuilder.append("_")
            }

            // Convert this match to lowercase
            lastMatchEndIndex = matchResult.range.last + 1
            replacementBuilder.append(
                rawIdValue.substring(
                    matchResult.range.first,
                    lastMatchEndIndex
                ).lowercase()
            )

            // If this is the last match, grab the rest of the string
            if (index == matches.count() - 1) {
                replacementBuilder.append(rawIdValue.substring(lastMatchEndIndex))
            }
        }

        val replacementText = replacementBuilder.toString()
        val quickfix = LintFix.create()
            .name("Format resource name")
            .replace()
            .range(context.parser.getValueLocation(context, attribute))
            .with(replacementText)
            .build()

        return quickfix
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ResourceNameFormat",
            briefDescription = "Resource name is improperly formatted",
            explanation =
            """
                    Resource names should never start with an underscore and should always use `snake_case`.
            """,
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                ResourceNameFormatDetector::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }
}
