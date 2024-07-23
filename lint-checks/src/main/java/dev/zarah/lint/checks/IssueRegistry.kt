package dev.zarah.lint.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class IssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = buildList {
        add(DatabindingExpressionFormatDetector.ISSUE)
        add(DeprecatedColorInXmlDetector.ISSUE)
        add(ResourceNameFormatDetector.ISSUE)
        addAll(TodoDetector.ISSUES)
    }

    override val api = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "Zarah Dominguez",
        feedbackUrl = "https://github.com/zmdominguez/lint-rule-samples",
    )
}