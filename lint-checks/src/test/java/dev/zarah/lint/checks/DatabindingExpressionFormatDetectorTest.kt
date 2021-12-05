package dev.zarah.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

@Suppress("UnstableApiUsage")
class DatabindingExpressionFormatDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = DatabindingExpressionFormatDetector()

    override fun getIssues(): List<Issue> = listOf(DatabindingExpressionFormatDetector.ISSUE)

    @Test
    fun testNoBindingExpression() {
        lint().files(
            xml(
                "res/layout/layout.xml",
                """
                <View xmlns:android="http://schemas.android.com/apk/res/android"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:background="@color/color_primary" />
                """.trimIndent()
            ).indented()
        )
            .run()
            .expectClean()
    }

    @Test
    fun testValidBindingExpression() {
        lint().files(
            xml(
                "res/layout/layout.xml",
                """
                <layout xmlns:android="http://schemas.android.com/apk/res/android">
                
                    <data>
                
                    </data>
                    <View android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@{ viewModel.background() }" />
                </layout>
                """.trimIndent()
            ).indented()
        )
            .run()
            .expectClean()
    }

    @Test
    fun testValidTwoWayBindingExpression() {
        lint().files(
            xml(
                "res/layout/layout.xml",
                """
                <layout xmlns:android="http://schemas.android.com/apk/res/android">
                
                    <data>
                
                    </data>
                    <CheckBox android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:checked="@={ viewModel.isChecked() }" />
                </layout>
                """.trimIndent()
            ).indented()
        )
            .run()
            .expectClean()
    }

    @Test
    fun testNoSpacesBindingExpression() {
        lint().files(
            xml(
                "res/layout/layout.xml",
                """
                <layout xmlns:android="http://schemas.android.com/apk/res/android">
                
                    <data>
                
                    </data>
                    <View android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@{viewModel.background}" />
                </layout>
                """.trimIndent()
            ).indented()
        )
            .run()
            .expect(
                """
                    res/layout/layout.xml:8: Warning: Please put one whitespace between the braces and the expression [DatabindingExpressionFormat]
                            android:background="@{viewModel.background}" />
                                                ~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs("""
                Fix for res/layout/layout.xml line 8: Fix databinding expression formatting:
                @@ -8 +8
                -         android:background="@{viewModel.background}" />
                +         android:background="@{ viewModel.background }" />
            """.trimIndent())
    }

    @Test
    fun testNoSpacesTwoWayBindingExpression() {
        lint().files(
            xml(
                "res/layout/layout.xml",
                """
                <layout xmlns:android="http://schemas.android.com/apk/res/android">
                
                    <data>
                
                    </data>
                    <CheckBox android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:checked="@={viewModel.isChecked()}" />
                </layout>
                """.trimIndent()
            ).indented()
        )
            .run()
            .expect(
                """
                    res/layout/layout.xml:8: Warning: Please put one whitespace between the braces and the expression [DatabindingExpressionFormat]
                            android:checked="@={viewModel.isChecked()}" />
                                             ~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs("""
                Fix for res/layout/layout.xml line 8: Fix databinding expression formatting:
                @@ -8 +8
                -         android:checked="@={viewModel.isChecked()}" />
                +         android:checked="@={ viewModel.isChecked() }" />
            """.trimIndent())
    }

    @Test
    fun testNoTrailingSpaceBindingExpression() {
        lint().files(
            xml(
                "res/layout/layout.xml",
                """
                <layout xmlns:android="http://schemas.android.com/apk/res/android">
                
                    <data>
                
                    </data>
                    <View android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@{ viewModel.background}" />
                </layout>
                """.trimIndent()
            ).indented()
        )
            .run()
            .expect(
                """
                    res/layout/layout.xml:8: Warning: Please put one whitespace between the braces and the expression [DatabindingExpressionFormat]
                            android:background="@{ viewModel.background}" />
                                                ~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs("""
                Fix for res/layout/layout.xml line 8: Fix databinding expression formatting:
                @@ -8 +8
                -         android:background="@{ viewModel.background}" />
                +         android:background="@{ viewModel.background }" />
            """.trimIndent())
    }

    @Test
    fun testNoLeadingSpaceBindingExpression() {
        lint().files(
            xml(
                "res/layout/layout.xml",
                """
                <layout xmlns:android="http://schemas.android.com/apk/res/android">
                
                    <data>
                
                    </data>
                    <View android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@{viewModel.background }" />
                </layout>
                """.trimIndent()
            ).indented()
        )
            .run()
            .expect(
                """
                    res/layout/layout.xml:8: Warning: Please put one whitespace between the braces and the expression [DatabindingExpressionFormat]
                            android:background="@{viewModel.background }" />
                                                ~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs("""
                Fix for res/layout/layout.xml line 8: Fix databinding expression formatting:
                @@ -8 +8
                -         android:background="@{viewModel.background }" />
                +         android:background="@{ viewModel.background }" />
            """.trimIndent())
    }

    @Test
    fun testExpressionWithEscapeCharacters() {
        lint().files(
            xml(
                "res/layout/layout.xml",
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <layout xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:app="http://schemas.android.com/apk/res-auto">

                        <data>
                            <variable
                                name="hasValue"
                                type="Boolean" />

                            <variable
                                name="isFeatureOn"
                                type="Boolean" />
                        </data>

                        <View android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            app:visible="@{hasValue &amp;&amp; isFeatureOn}" />
                    </layout>
                """.trimIndent()
            ).indented()
        )
            .run()
            .expect(
                """
                    res/layout/layout.xml:17: Warning: Please put one whitespace between the braces and the expression [DatabindingExpressionFormat]
                            app:visible="@{hasValue &amp;&amp; isFeatureOn}" />
                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs("""
                Fix for res/layout/layout.xml line 17: Fix databinding expression formatting:
                @@ -17 +17
                -         app:visible="@{hasValue &amp;&amp; isFeatureOn}" />
                +         app:visible="@{ hasValue &amp;&amp; isFeatureOn }" />
            """.trimIndent())
    }
}