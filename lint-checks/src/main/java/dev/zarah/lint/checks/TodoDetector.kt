package dev.zarah.lint.checks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.uast.UComment
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.EnumSet

class TodoDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UFile::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitFile(node: UFile) {
                val allComments = node.allCommentsInFile
                allComments.forEach { comment ->
                    val commentText = comment.text

                    // Drop any comment markers at the beginning of the text
                    val sanitisedText = commentText
                        .removePrefix("//") // A double slash (//)
                        .removePrefix("/*") // A slash+asterisk (/*)
                        .trimStart()
                        .removePrefix("*")  // A single asterisk (*) if in a block comment
                        .trimStart()

                    if (sanitisedText.startsWith("TODO", ignoreCase = true) && !isValidComment(sanitisedText)) {
                        // There is an issue somewhere, report specifics
                        reportUsage(context, comment)
                    }
                }
            }
        }
    }

    private fun isValidComment(commentText: String): Boolean {
        // Check if the matches the full format
        val matchResult = COMPLETE_PATTERN_REGEX.find(commentText)

        return matchResult?.groups?.isNotEmpty() ?: return false
    }

    private fun reportUsage(
        context: JavaContext,
        comment: UComment
    ) {

        // MISSING_DATE: Date is totally absent, or in the wrong place
        var issueFound = reportDateIssue(context, comment)
        if (issueFound) return

        // MISSING_ASSIGNEE: Assignee is totally absent
        issueFound = reportAssigneeIssue(context, comment)
        if (issueFound) return

        // All other issues fall through to here, like if all elements are there but in the wrong order
        val incident = Incident()
            .issue(IMPROPER_FORMAT)
            .location(context.getLocation(comment))
            .message("Improper format")

        // Only suggest the fix for non-block comments
        // Block comments are trickier to figure out, something to implement for the future!
        if (comment.sourcePsi.elementType != KtTokens.BLOCK_COMMENT) {
            incident.fix(createFix(comment, "Format this TODO"))
        }

        context.report(incident)
    }

    private fun reportAssigneeIssue(context: JavaContext, comment: UComment): Boolean {

        val commentText = comment.text

        // Check if everything else is there, but assignee is not present
        val assigneeMatches = ASSIGNEE_ONLY_CAPTURE.find(commentText)?.groups
        val assigneeMatch = assigneeMatches?.get(MATCH_KEY_ASSIGNEE)
        if (assigneeMatches != null) {
            val assignee = assigneeMatch?.value
            if (!assignee.isNullOrEmpty() && assignee.isNotBlank()) return false
        }

        // Assignee is missing, construct replacement
        var nextCharIndex = commentText.indexOf("TODO", ignoreCase = true) + 4 // length of "TODO"
        if (commentText[nextCharIndex] == '-') {
            ++nextCharIndex
        }

        val commentStartOffset = context.getLocation(comment).start?.offset ?: 0
        val endLocation = commentStartOffset + nextCharIndex

        val replacementText ="// TODO-${getUserName()}"
        val addAssigneeFix = LintFix.create()
            .name("Assign this TODO")
            .replace()
            .range(Location.create(
                file = context.file,
                contents = context.getContents(),
                startOffset = commentStartOffset,
                endOffset = endLocation
            ))
            .with(replacementText)
            .build()

        val incident = Incident()
            .issue(MISSING_ASSIGNEE)
            .location(context.getLocation(comment))
            .message("Missing assignee")
            .fix(addAssigneeFix)
        context.report(incident)

        return true
    }

    private fun reportDateIssue(context: JavaContext, comment: UComment): Boolean {
        val commentText = comment.text

        // Check if date is present
        val inParensMatches = DATE_ONLY_CAPTURE.find(commentText)?.groups
        if (inParensMatches == null) {
            val incident = Incident()
                .issue(MISSING_OR_INVALID_DATE)
                .location(context.getLocation(comment))
                .message("Missing date")

            // Only suggest the fix for non-block comments
            // Block comments are trickier to figure out, something to implement for the future!
            if (comment.sourcePsi.elementType != KtTokens.BLOCK_COMMENT) {
                incident.fix(createFix(comment, "Add date"))
            }

            context.report(incident)

            return true
        }

        val dateMatch = inParensMatches[MATCH_KEY_DATE]
        val parensValue = requireNotNull(dateMatch).value
        val message = when {
            parensValue == "" -> "Missing date"
            !isValidDate(parensValue) -> "Invalid date"
            else -> null
        }

        if (message == null) return false

        val commentStartOffset = context.getLocation(comment).start?.offset ?: 0
        val startLocation = commentStartOffset + dateMatch.range.first
        val endLocation = commentStartOffset + dateMatch.range.last

        val dateLocation = Location.create(
            file = context.file,
            contents = context.getContents(),
            startOffset = startLocation,
            endOffset = endLocation + 1,
        )

        val problemLocation = Location.create(
            file = context.file,
            contents = context.getContents(),
            startOffset = startLocation - 1,
            endOffset = endLocation + 2,
        )

        val dateFix = LintFix.create()
            .name("Update date")
            .replace()
            .range(dateLocation)
            .with(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN)))
            .build()

        val incident = Incident()
            .issue(MISSING_OR_INVALID_DATE)
            .location(problemLocation)
            .message(message)
            .fix(dateFix)
        context.report(incident)

        return true
    }

    private fun isValidDate(dateString: String): Boolean {
        try {
            val providedDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_PATTERN))
            val providedYear = providedDate.year
            return providedYear in 2024..2099
        } catch (e: DateTimeParseException) {
            return false
        }
    }

    private fun createFix(comment: UComment, message: String): LintFix {
        var replacementText = "// TODO"

        // We are going to manipulate the existing comment text
        // Drop anything before the word "TODO"
        // There may or may not be a colon, so remove that separately
        var commentText = comment.text
            .substringAfter("TODO")
            .substringAfter("todo")
            .removePrefix(":")
            .trimStart()

        // Find any assignee if available and re-use it
        var currentAssignee = getUserName()
        if (commentText.startsWith("-")) {
            val assigneeMatches = ASSIGNEE_CAPTURE_START_REGEX.find(commentText)
            if (assigneeMatches != null) {
                val assigneeMatchGroup = requireNotNull(assigneeMatches.groups[MATCH_KEY_ASSIGNEE])
                val assigneeRange = assigneeMatchGroup.range
                commentText = commentText.removeRange(assigneeRange).trimStart().removePrefix("-")
                    .removePrefix(":").trimStart()
                currentAssignee = assigneeMatchGroup.value.trim()
            }
        }

        replacementText += "-$currentAssignee"

        // Find the string enclosed in parentheses
        var dateReplacementValue = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
        val dateMatches = DATE_CAPTURE_REGEX.find(commentText)
        if (dateMatches != null) {
            val dateMatchGroup = requireNotNull(dateMatches.groups[MATCH_KEY_DATE])
            commentText = commentText.removeRange(dateMatches.groups.first()!!.range).trimStart()
            dateReplacementValue = dateMatchGroup.value
        }
        replacementText += " ($dateReplacementValue)"

        // Add a colon if the remaining text does not have it yet
        if (!commentText.startsWith(":")) {
            replacementText += ": "
        }

        replacementText += commentText.trimStart()

        val fix = LintFix.create()
            .name(message)
            .replace()
            .text(comment.text)
            .with(replacementText)
            .build()

        return fix
    }

    private fun getUserName(): String {
        val name = System.getProperty("user.name")
        return name.split(" ").joinToString("")
    }

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val MATCH_KEY_ASSIGNEE = "assignee"
        const val MATCH_KEY_DATE = "date"

        // Regex pattern for capturing anything between the first open parentheses
        // and the last close parentheses
        const val DATE_DELIMITER_CAPTURE_PATTERN = """\((?<$MATCH_KEY_DATE>[^\)]*)\)"""

        // Max year allowed is up to 2099
        const val DATE_FORMAT_PATTERN = """20[0-9]{2}-[01][0-9]-[0-3][0-9]"""
        private val DATE_CAPTURE_PATTERN = """\((?<$MATCH_KEY_DATE>$DATE_FORMAT_PATTERN)\)"""
        private val DATE_CAPTURE_REGEX = DATE_CAPTURE_PATTERN.toRegex()
        private val DATE_ONLY_CAPTURE = """.*TODO.*$DATE_DELIMITER_CAPTURE_PATTERN.*""".toRegex()

        // The assignee comes immediately after the dash and limited to one word
        private val ASSIGNEE_CAPTURE_PATTERN = """(?<$MATCH_KEY_ASSIGNEE>[^:\(\s-]+)"""
        private val ASSIGNEE_CAPTURE_START_REGEX = """^-$ASSIGNEE_CAPTURE_PATTERN""".toRegex()
        private val ASSIGNEE_ONLY_CAPTURE = """.*TODO-*$ASSIGNEE_CAPTURE_PATTERN.*\(.*\)""".toRegex()

        // When capturing a pattern in Regex, we can give the group a name to make it easier to access
        // Any matches will be in a List accessible by the name between `<` and `>`
        private val COMPLETE_PATTERN = """.*TODO-$ASSIGNEE_CAPTURE_PATTERN $DATE_CAPTURE_PATTERN:.*"""
        val COMPLETE_PATTERN_REGEX = COMPLETE_PATTERN.toRegex()

        private val IMPLEMENTATION = Implementation(
            TodoDetector::class.java,
            EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES)
        )

        private const val REQUIRED_FORMAT = "All TODOs must follow the format `TODO-Assignee (DATE_TODAY): Additional comments`"

        val IMPROPER_FORMAT: Issue = Issue.create(
            id = "ImproperTodoFormat",
            briefDescription = "TODO has improper format",
            explanation =
            """
                $REQUIRED_FORMAT
                
                The assignee and the date are required information.
            """,
            category = Category.CORRECTNESS,
            priority = 3,
            severity = Severity.ERROR,
            implementation = IMPLEMENTATION
        ).setAndroidSpecific(true)

        val MISSING_ASSIGNEE: Issue = Issue.create(
            id = "MissingTodoAssignee",
            briefDescription = "TODO with no assignee",
            explanation =
            """
                $REQUIRED_FORMAT
                
                Please put your name against this TODO. Assignees should be a camel-cased word, for example `ZarahDominguez`.
            """,
            category = Category.CORRECTNESS,
            priority = 3,
            severity = Severity.ERROR,
            implementation = IMPLEMENTATION
        ).setAndroidSpecific(true)

        val MISSING_OR_INVALID_DATE: Issue = Issue.create(
            id = "MissingTodoDate",
            briefDescription = "TODO with no date",
            explanation =
            """
                $REQUIRED_FORMAT
                
                Please put today's date in the yyyy-MM-dd format enclosed in parentheses, for example `(2024-07-20)`.
            """,
            category = Category.CORRECTNESS,
            priority = 3,
            severity = Severity.ERROR,
            implementation = IMPLEMENTATION
        ).setAndroidSpecific(true)

        val ISSUES = listOf(
            IMPROPER_FORMAT,
            MISSING_ASSIGNEE,
            MISSING_OR_INVALID_DATE,
        )
    }
}
