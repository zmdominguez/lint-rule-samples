package dev.zarah.lint.checks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UComment
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("UnstableApiUsage")
class TodoDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UFile::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            override fun visitFile(node: UFile) {
                val allComments = node.allCommentsInFile
                allComments.forEach { comment ->
                    val commentText = comment.text
                    if (commentText.contains("TODO", ignoreCase = true) && !isValidComment(commentText)) {
                        reportUsage(context, comment)
                    }
                }
            }
        }
    }

    private fun isValidComment(commentText: String): Boolean {
        val regex = Regex("//\\s+TODO-\\w*\\s+\\(\\d{8}\\):.*")
        return commentText.matches(regex)
    }

    private fun reportUsage(
        context: JavaContext,
        comment: UComment
    ) {
        val replacementText = "TODO-${System.getProperty("user.name")} " +
            "(${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}):"

        val oldPattern = Regex("TODO|todo")

        val quickfixData = LintFix.create()
            .name("Assign this TODO")
            .replace()
            .pattern(oldPattern.pattern)
            .with(replacementText)
            .robot(true) // Can be applied automatically.
            .independent(true) // Does not conflict with other auto-fixes.
            .build()

        context.report(
            issue = ISSUE,
            location = context.getLocation(comment),
            message = "Please make sure to assign the TODO, include today's date in YYYYMMDD format, and the comment is properly formatted",
            quickfixData = quickfixData
        )
    }

    companion object {
        private val IMPLEMENTATION = Implementation(
            TodoDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        val ISSUE: Issue = Issue.create(
            id = "UnassignedTodo",
            briefDescription = "TODO with no assignee",
            explanation =
            """
                    This check makes sure that each TODO is assigned to somebody.
            """,
            category = Category.CORRECTNESS,
            priority = 3,
            severity = Severity.ERROR,
            implementation = IMPLEMENTATION
        ).setAndroidSpecific(true)
    }
}
