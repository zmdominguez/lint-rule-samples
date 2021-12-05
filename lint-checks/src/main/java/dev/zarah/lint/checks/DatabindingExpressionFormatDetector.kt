package dev.zarah.lint.checks

import com.android.SdkConstants
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScannerConstants
import com.android.tools.lint.detector.api.isDataBindingExpression
import org.w3c.dom.Attr

@Suppress("UnstableApiUsage")
class DatabindingExpressionFormatDetector : LayoutDetector() {

    private val validPattern = Regex("@=?\\{\\s.*\\s}")

    override fun getApplicableAttributes(): Collection<String>? {
        return XmlScannerConstants.ALL
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        val attributeValue = attribute.nodeValue

        // This is a databinding expression and the pattern does not match
        if (isDataBindingExpression(attributeValue) && !attributeValue.matches(validPattern)) {

            val (quickfix, location) = createQuickfix(context, attribute)
            val locationToReport = location ?: context.getValueLocation(attribute)
            context.report(
                issue = DatabindingExpressionFormatDetector.ISSUE,
                scope = attribute,
                location = locationToReport,
                message = "Please put one whitespace between the braces and the expression",
                quickfixData = quickfix
            )
        }
    }

    private fun createQuickfix(context: XmlContext, attribute: Attr): Pair<LintFix?, Location?> {
        // This is the contents of the whole XML file
        val rawText = context.getContents() ?: return Pair(null, null)

        // Find the full contents of the node, including the attribute name (i.e. `android:background`)
        val nodeStart = context.parser.getNodeStartOffset(context, attribute)
        if (nodeStart == -1) return Pair(null, null)
        val nodeEnd = context.parser.getNodeEndOffset(context, attribute)
        val rawNodeText = rawText.substring(nodeStart, nodeEnd)

        // Find the databinding prefix used
        val prefixExpression = if (rawNodeText.contains(SdkConstants.PREFIX_TWOWAY_BINDING_EXPR)) {
            SdkConstants.PREFIX_TWOWAY_BINDING_EXPR
        } else SdkConstants.PREFIX_BINDING_EXPR

        // First character after the opening `"` in the attribute value
        val attributeValueStart = rawNodeText.indexOf(prefixExpression)

        // Get the actual databinding expression (i.e., what is between `{` and `}`)
        val expressionStart = attributeValueStart + prefixExpression.length // length of the expression marker
        val expressionEnd = rawNodeText.lastIndexOf("}")
        val rawExpressionValue = rawNodeText.substring(expressionStart, expressionEnd)

        // We cannot use `context.parser.getValueLocation()` since there may be escaped characters
        // Get the Location value for the actual expression within the file (not including the quotation marks)
        val attributeValueLocation = Location.create(context.file, rawText, nodeStart + attributeValueStart, nodeEnd - 1)

        val replacementText = "$prefixExpression ${rawExpressionValue.trim()} }"
        val quickfix = fix()
            .name("Fix databinding expression formatting")
            .replace()
            .range(attributeValueLocation)
            .with(replacementText)
            .build()

        return Pair(quickfix, attributeValueLocation)
    }

    companion object {
        val ISSUE = Issue.create(
            id = "DatabindingExpressionFormat",
            briefDescription = "Proper formatting of data binding expressions",
            explanation =
            """
                    One of our formatting rules for the team is to provide a whitespace between braces for databinding expressions. 
                    This check makes sure databinding expressions follow the above convention.
            """,
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                DatabindingExpressionFormatDetector::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }
}
