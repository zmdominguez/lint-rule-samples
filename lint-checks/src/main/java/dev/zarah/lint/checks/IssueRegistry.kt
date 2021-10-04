package dev.zarah.lint.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
class IssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = listOf(
        DeprecatedColorInXmlDetector.ISSUE,
        TodoDetector.ISSUE,
    )

    override val api = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "Woolworths Group",
    )
}