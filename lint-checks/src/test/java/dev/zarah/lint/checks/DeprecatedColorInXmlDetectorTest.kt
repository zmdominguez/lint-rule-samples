package dev.zarah.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.ProjectDescription
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

@Suppress("UnstableApiUsage")
class DeprecatedColorInXmlDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = DeprecatedColorInXmlDetector()

    override fun getIssues(): MutableList<Issue> = mutableListOf(DeprecatedColorInXmlDetector.ISSUE)

    @Test
    fun testAllowedColor() {
        lint().files(VIEW_WITH_VALID_COLOUR)
            .testModes(TestMode.PARTIAL)
            .run()
            .expectClean()
    }

    @Test
    fun testPlatformColor() {
        lint().files(VIEW_WITH_ANDROID_COLOUR)
            .testModes(TestMode.PARTIAL)
            .run()
            .expectClean()
    }

    @Test
    fun testSuppressedDeprecatedColorInWidget() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            xml(
                "res/layout/layout.xml",
                """
                    <View xmlns:android="http://schemas.android.com/apk/res/android"
                        xmlns:tools="http://schemas.android.com/tools"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content">
                        <TextView android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:textColor="@color/red_error" 
                            tools:ignore="DeprecatedColorInXml"/>
                    </View>
            """
            ).indented()
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expectClean()
    }

    @Test
    fun testSuppressedDeprecatedColorInLayoutFile() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            xml(
                "res/layout/layout.xml",
                """
                <View xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:tools="http://schemas.android.com/tools"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    tools:ignore="DeprecatedColorInXml">
                        <TextView android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:textColor="@color/red_error" />
                </View>
            """
            ).indented()
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expectClean()
    }

    @Test
    fun testMultipleSuppressedDeprecatedColorInLayoutFile() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            xml(
                "res/layout/layout.xml",
                """
                <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@color/red_error"
                    tools:ignore="Orientation,DeprecatedColorInXml" />
            """
            ).indented()
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expectClean()
    }

    @Test
    fun testDeprecatedColorInLayoutFile() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            VIEW_WITH_DEPRECATED_COLOUR
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                    res/layout/layout.xml:4: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                        android:background="@color/red_error" />
                                            ~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedColorInDrawable() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            SHAPE_WITH_DEPRECATED_COLOUR
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                    res/drawable/shape.xml:3: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                        <solid android:color="@color/red_error" />
                                              ~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedColorInColorXml() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            DEPRECATED_COLOUR_FILE2,
            SELECTOR_WITH_DEPRECATED_COLOUR
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                    res/color/text_selector.xml:2: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                        <item android:color="@color/red_error" android:alpha="0.99" android:state_pressed="true" />
                                             ~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedColorInStyle() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            DEPRECATED_COLOUR_FILE2,
            xml(
                "res/values/styles.xml",
                """
                    <style name="TrolleyRewardsPreferenceText" parent="Body">
                        <item name="android:textColor">@color/product_item_desc_text_color</item>
                    </style>
                """
            ).indented()
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                   res/values/styles.xml:2: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                       <item name="android:textColor">@color/product_item_desc_text_color</item>
                                                      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedColorInAlias() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            xml(
                "res/values/colors.xml",
                """
                    <resources>
                        <color name="red_alias">@color/red_error</color>
                    </resources>
                """
            ).indented()
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                   res/values/colors.xml:2: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                       <color name="red_alias">@color/red_error</color>
                                               ~~~~~~~~~~~~~~~~
                   1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedSelectorInLayoutFile() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            DEPRECATED_SELECTOR,
            VIEW_WITH_DEPRECATED_SELECTOR,
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                   res/layout/layout.xml:4: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                       android:textColor="@color/some_selector_deprecated" />
                                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedColorInSelector() {
        lint().files(
            DEPRECATED_COLOUR_FILE,
            DEPRECATED_COLOUR_FILE2,
            xml(
                "res/color/some_selector.xml",
                """
                    <selector xmlns:android="http://schemas.android.com/apk/res/android">
                        <item android:color="@color/safe_colour" android:state_enabled="false" />
                        <item android:color="@color/red_error" />
                    </selector>
                    """
            ).indented()
        )
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                   res/color/some_selector.xml:3: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                       <item android:color="@color/red_error" />
                                            ~~~~~~~~~~~~~~~~
                   1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedColourInLibraryModule() {
        val deprecatedModule = ProjectDescription()
            .name("deprecated-library")
            .type(ProjectDescription.Type.LIBRARY)
            .files(DEPRECATED_COLOUR_FILE)

        val appModule = ProjectDescription()
            .name("app")
            .files(VIEW_WITH_DEPRECATED_COLOUR)
            .dependsOn(deprecatedModule)

        lint()
            .projects(deprecatedModule, appModule)
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                    res/layout/layout.xml:4: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                        android:background="@color/red_error" />
                                            ~~~~~~~~~~~~~~~~
                    1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedColourInTransitiveDependency() {
        val deprecatedModule = ProjectDescription()
            .name("deprecated-library")
            .type(ProjectDescription.Type.LIBRARY)
            .files(DEPRECATED_COLOUR_FILE)

        val anotherDeprecatedModule = ProjectDescription()
            .name("another-module")
            .dependsOn(deprecatedModule)
            .type(ProjectDescription.Type.LIBRARY)
            .files(DEPRECATED_SELECTOR,
                VIEW_WITH_ANDROID_COLOUR)

        val appModule = ProjectDescription()
            .name("app")
            .files(VIEW_WITH_DEPRECATED_SELECTOR)
            .dependsOn(anotherDeprecatedModule)

        lint()
            .projects(deprecatedModule, anotherDeprecatedModule, appModule)
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                   res/layout/layout.xml:4: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                       android:textColor="@color/some_selector_deprecated" />
                                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                   1 errors, 0 warnings
                """
            )
    }

    @Test
    fun testDeprecatedColourInLibraryAndApp() {
        val deprecatedModule = ProjectDescription()
            .name("deprecated-library")
            .type(ProjectDescription.Type.LIBRARY)
            .files(DEPRECATED_COLOUR_FILE,
                DEPRECATED_SELECTOR,
                VIEW_WITH_DEPRECATED_COLOUR)

        val appModule = ProjectDescription()
            .name("app")
            .files(VIEW_WITH_DEPRECATED_SELECTOR)
            .dependsOn(deprecatedModule)

        lint()
            .projects(deprecatedModule, appModule)
            .testModes(TestMode.PARTIAL)
            .run()
            .expect(
                """
                    res/layout/layout.xml:4: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                        android:textColor="@color/some_selector_deprecated" />
                                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    ../deprecated-library/res/layout/layout.xml:4: Error: Deprecated colours should not be used [DeprecatedColorInXml]
                        android:background="@color/red_error" />
                                            ~~~~~~~~~~~~~~~~
                    2 errors, 0 warnings
                """
            )
    }

    companion object {
        val DEPRECATED_COLOUR_FILE: TestFile = xml(
            "res/values/colors_deprecated.xml",
            """
                <resources>
                    <color name="red_error">#d6163e</color>
                    <color name="another_value">#d6163e</color>
                    <color name="and_another_value">#d6163e</color>
                </resources>
            """
        ).indented()

        val DEPRECATED_COLOUR_FILE2: TestFile = xml(
            "res/color/legacy_colors_deprecated.xml",
            """
                <resources>
                    <color name="product_item_desc_text_color">#AC354148</color>
                </resources>
            """
        ).indented()

        val DEPRECATED_SELECTOR: TestFile = xml(
            "res/color/some_selector_deprecated.xml",
            """
                <!-- Some comments here -->
                <selector xmlns:android="http://schemas.android.com/apk/res/android">
                    <item android:color="@color/unavailable_text_colour" android:state_enabled="false" />
                    <item android:color="@color/color_accent" />
                </selector>
            """
        ).indented()

        val VIEW_WITH_VALID_COLOUR: TestFile = xml(
            "res/layout/layout.xml",
            """
                <View xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@color/rewards_color_primary" />
            """
        ).indented()

        val VIEW_WITH_ANDROID_COLOUR: TestFile = xml(
            "res/layout/layout.xml",
            """
                <View xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@android:color/white" />
            """
        ).indented()

        val VIEW_WITH_DEPRECATED_COLOUR: TestFile = xml(
            "res/layout/layout.xml",
            """
                <View xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:background="@color/red_error" />
            """
        ).indented()

        val SHAPE_WITH_DEPRECATED_COLOUR: TestFile = xml(
            "res/drawable/shape.xml",
            """
                <shape xmlns:android="http://schemas.android.com/apk/res/android"
                        android:shape="oval">
                    <solid android:color="@color/red_error" />
                </shape>
            """
        ).indented()

        val SELECTOR_WITH_DEPRECATED_COLOUR: TestFile = xml(
            "res/color/text_selector.xml",
            """
                <selector xmlns:android="http://schemas.android.com/apk/res/android">
                    <item android:color="@color/red_error" android:alpha="0.99" android:state_pressed="true" />
                </selector>
            """
        ).indented()

        val VIEW_WITH_DEPRECATED_SELECTOR: TestFile = xml(
            "res/layout/layout.xml",
            """
                <TextView xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textColor="@color/some_selector_deprecated" />
                """
        ).indented()
    }
}
