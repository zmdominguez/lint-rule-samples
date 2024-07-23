package dev.zarah.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodoDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = TodoDetector()

    override fun getIssues(): List<Issue> =TodoDetector.ISSUES

    @Test
    fun testValidComment() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah (2020-05-15): Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect("No warnings.")
    }

    @Test
    fun testValidBlockCommentMultiLine() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    /* 
                    TODO-Zarah (2020-05-15): Some comments
                    
                    There may be more text here.
                     */
                }
            """
            ).indented()
        )
            .run()
            .expect("No warnings.")
    }

    @Test
    fun testValidBlockCommentSingleLine() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    /* TODO-Zarah (2020-05-15): Some comments */
                }
            """
            ).indented()
        )
            .run()
            .expect("No warnings.")
    }

    //region Assignee tests
    @Test
    fun testMissingAssigneeAndDateWithColon() {
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
                    src/test/pkg/TestClass1.kt:3: Error: Missing date [MissingTodoDate]
                        // TODO: Some comments
                        ~~~~~~~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Add date:
                    @@ -3 +3
                    -     // TODO: Some comments
                    +     // TODO-$assignee ($dateToday): Some comments
                """.trimIndent()
            )
    }

    @Test
    fun testMissingAssignee() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO (2024-07-20): Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect("""
                src/test/pkg/TestClass1.kt:3: Error: Missing assignee [MissingTodoAssignee]
                    // TODO (2024-07-20): Some comments
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
            """.trimIndent())
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Assign this TODO:
                    @@ -3 +3
                    -     // TODO (2024-07-20): Some comments
                    +     // TODO-$assignee (2024-07-20): Some comments
                """.trimIndent()
            )
    }

    @Test
    fun testMissingAssigneeWithDash() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO- (2024-07-20): Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect("""
                src/test/pkg/TestClass1.kt:3: Error: Missing assignee [MissingTodoAssignee]
                    // TODO- (2024-07-20): Some comments
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
            """.trimIndent())
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Assign this TODO:
                    @@ -3 +3
                    -     // TODO- (2024-07-20): Some comments
                    +     // TODO-$assignee (2024-07-20): Some comments
                """.trimIndent()
            )
    }
    //endregion

    //region Date tests
    @Test
    fun testDateWithOtherParens() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah (2024-07-20): Some comments (more here!)
                }
            """
            ).indented()
        )
            .run()
            .expectClean()
    }

    @Test
    fun testMissingDate() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                src/test/pkg/TestClass1.kt:3: Error: Missing date [MissingTodoDate]
                    // TODO-Zarah Some comments
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Add date:
                    @@ -3 +3
                    -     // TODO-Zarah Some comments
                    +     // TODO-Zarah ($dateToday): Some comments
                """.trimIndent()
            )
    }

    @Test
    fun testMissingDateWithColon() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah: Some comments
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                src/test/pkg/TestClass1.kt:3: Error: Missing date [MissingTodoDate]
                    // TODO-Zarah: Some comments
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Add date:
                    @@ -3 +3
                    -     // TODO-Zarah: Some comments
                    +     // TODO-Zarah ($dateToday): Some comments
                """.trimIndent()
            )
    }

    @Test
    fun testInvalidDateFormat() {
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
            .skipTestModes(TestMode.SUPPRESSIBLE)
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.kt:3: Error: Invalid date [MissingTodoDate]
                        // TODO-Zarah (30 Sep. 2020): Some comments
                                      ~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """.trimIndent())
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Update date:
                    @@ -3 +3
                    -     // TODO-Zarah (30 Sep. 2020): Some comments
                    +     // TODO-Zarah ($dateToday): Some comments
                """.trimIndent()
            )
    }

    @Test
    fun testEmptyDate() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah (): Some comments
                }
            """
            ).indented()
        )
            .skipTestModes(TestMode.SUPPRESSIBLE)
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.kt:3: Error: Missing date [MissingTodoDate]
                        // TODO-Zarah (): Some comments
                                      ~~
                    1 errors, 0 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Update date:
                    @@ -3 +3
                    -     // TODO-Zarah (): Some comments
                    +     // TODO-Zarah ($dateToday): Some comments
                """.trimIndent()
            )
    }

    @Test
    fun testDateAtEnd() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah: Some comments (2024-07-20)
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.kt:3: Error: Improper format [ImproperTodoFormat]
                        // TODO-Zarah: Some comments (2024-07-20)
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Format this TODO:
                    @@ -3 +3
                    -     // TODO-Zarah: Some comments (2024-07-20)
                    +     // TODO-Zarah (2024-07-20): Some comments
                """.trimIndent()
            )
    }

    @Test
    fun testDateAfterColon() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah: (2024-07-20) Some comments
                }
            """
            ).indented()
        )
            .skipTestModes(TestMode.SUPPRESSIBLE)
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.kt:3: Error: Improper format [ImproperTodoFormat]
                        // TODO-Zarah: (2024-07-20) Some comments
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Format this TODO:
                    @@ -3 +3
                    -     // TODO-Zarah: (2024-07-20) Some comments
                    +     // TODO-Zarah (2024-07-20): Some comments
                """.trimIndent()
            )
    }

    @Test
    fun testDateInMiddle() {
        lint().files(
            kotlin(
                """
                package test.pkg
                class TestClass1 {
                    // TODO-Zarah: Some (2024-07-20) comments
                }
            """
            ).indented()
        )
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.kt:3: Error: Improper format [ImproperTodoFormat]
                        // TODO-Zarah: Some (2024-07-20) comments
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Format this TODO:
                    @@ -3 +3
                    -     // TODO-Zarah: Some (2024-07-20) comments
                    +     // TODO-Zarah (2024-07-20): Some  comments
                """.trimIndent()
            )
    }
    //endregion

    @Test
    fun testLowercaseComment() {
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
                    src/test/pkg/TestClass1.kt:3: Error: Missing date [MissingTodoDate]
                        // todo Some comments
                        ~~~~~~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """.trimIndent()
            )
            .expectFixDiffs(
                """
                    Fix for src/test/pkg/TestClass1.kt line 3: Add date:
                    @@ -3 +3
                    -     // todo Some comments
                    +     // TODO-zarah ($dateToday): Some comments
                """.trimIndent()
            )
    }

    companion object {
        private val dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        private val assignee = System.getProperty("user.name")
    }
}
