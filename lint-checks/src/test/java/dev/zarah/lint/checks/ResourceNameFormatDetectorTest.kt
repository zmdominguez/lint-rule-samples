package dev.zarah.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

@Suppress("UnstableApiUsage")
class ResourceNameFormatDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = ResourceNameFormatDetector()

    override fun getIssues(): MutableList<Issue> = mutableListOf(ResourceNameFormatDetector.ISSUE)

    @Test
    fun testValidName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/some_textview"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expectClean()
    }

    @Test
    fun testLowerCamelCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/someTextView"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/someTextView"
                                ~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for res/layout/layout.xml line 3: Format resource name:
                    @@ -3 +3
                    -     android:id="@+id/someTextView"
                    +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }

    @Test
    fun testUpperCamelCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/SomeTextView"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/SomeTextView"
                                ~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/SomeTextView"
                +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }

    @Test
    fun testLowerCamelCaseWithAcronymName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/SomeIDTextView"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                    res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                        android:id="@+id/SomeIDTextView"
                                    ~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/SomeIDTextView"
                +     android:id="@+id/some_idtext_view"
                """.trimIndent()
            )
    }

    @Test
    fun testUpperSnakeCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/Some_Text_View"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/Some_Text_View"
                                ~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/Some_Text_View"
                +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }

    @Test
    fun testScreamingSnakeCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/SOME_TEXT_VIEW"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/SOME_TEXT_VIEW"
                                ~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/SOME_TEXT_VIEW"
                +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }

    @Test
    fun testUnderscoreStartSnakeCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/_some_text_view"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/_some_text_view"
                                ~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/_some_text_view"
                +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }

    @Test
    fun testMultipleUnderscoresStartSnakeCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/__some_text_view"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/__some_text_view"
                                ~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/__some_text_view"
                +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }

    @Test
    fun testUnderscoreStartCamelCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/_someTextView"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/_someTextView"
                                ~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/_someTextView"
                +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }

    @Test
    fun testUnderscoreStartUpperCamelCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/_SomeTextView"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/_SomeTextView"
                                ~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/_SomeTextView"
                +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }

    @Test
    fun testUnderscoreStartScreamingSnakeCaseName() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/layout.xml",
                    """
                <TextView
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:id="@+id/_SOME_TEXT_VIEW"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:paddingLeft="8dp"
                    android:paddingRight="8dp"
                    android:text="This is a label" />
                """
                ).indented()
            )
            .run()
            .expect(
                """
                res/layout/layout.xml:3: Warning: Improper resource name format [ResourceNameFormat]
                    android:id="@+id/_SOME_TEXT_VIEW"
                                ~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                Fix for res/layout/layout.xml line 3: Format resource name:
                @@ -3 +3
                -     android:id="@+id/_SOME_TEXT_VIEW"
                +     android:id="@+id/some_text_view"
                """.trimIndent()
            )
    }
}
