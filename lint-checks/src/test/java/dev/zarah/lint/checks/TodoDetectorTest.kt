package dev.zarah.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

@Suppress("UnstableApiUsage")
class TodoDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = TodoDetector()

    override fun getIssues(): MutableList<Issue> = mutableListOf(TodoDetector.ISSUE)

    @Test
    fun testJavaFileNormalComment() {
        lint().files(
            java(
                """
                package test.pkg;
                public class TestClass1 {
                    // In a comment, mentioning "lint" has no effect
                }
            """
            ).indented()
        )
            .run()
            .expect("No warnings.")
    }

    @Test
    fun testJavaFileValidComment() {
        lint().files(
            java(
                """
                package test.pkg;
                public class TestClass1 {
                    // TODO-Zarah (20200515): Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect("No warnings.")
    }

    @Test
    fun testKotlinFileValidComment() {
        lint().files(
            kotlin(
                """
                package test.pkg;
                class TestClass1 {
                    // TODO-Zarah (20200515): Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect("No warnings.")
    }

    @Test
    fun testJavaFileInvalidComment() {
        lint().files(
            java(
                """
                package test.pkg;
                public class TestClass1 {
                    // TODO (20200515): Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                src/test/pkg/TestClass1.java:3: Error: Please make sure to assign the TODO, include today's date in YYYYMMDD format, and the comment is properly formatted [UnassignedTodo]
                    // TODO (20200515): Some comments
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testKotlinFileInvalidComment() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO: Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                src/test/pkg/TestClass1.kt:3: Error: Please make sure to assign the TODO, include today's date in YYYYMMDD format, and the comment is properly formatted [UnassignedTodo]
                    // TODO: Some comments
                    ~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testKotlinFileDateFormat() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah (30 Sep. 2020): Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                src/test/pkg/TestClass1.kt:3: Error: Please make sure to assign the TODO, include today's date in YYYYMMDD format, and the comment is properly formatted [UnassignedTodo]
                    // TODO-Zarah (30 Sep. 2020): Some comments
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testKotlinFileLowercaseComment() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // todo Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                src/test/pkg/TestClass1.kt:3: Error: Please make sure to assign the TODO, include today's date in YYYYMMDD format, and the comment is properly formatted [UnassignedTodo]
                    // todo Some comments
                    ~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testJavaFileLowercaseComment() {
        lint().files(
            java(
                """
                package test.pkg;
                public class TestClass1 {
                    // todo Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                src/test/pkg/TestClass1.java:3: Error: Please make sure to assign the TODO, include today's date in YYYYMMDD format, and the comment is properly formatted [UnassignedTodo]
                    // todo Some comments
                    ~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """
            )
    }
}
