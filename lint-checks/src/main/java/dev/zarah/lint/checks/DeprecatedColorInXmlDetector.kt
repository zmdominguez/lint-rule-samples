package dev.zarah.lint.checks

import com.android.SdkConstants
import com.android.resources.ResourceFolderType
import com.android.resources.ResourceType
import com.android.resources.ResourceUrl
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintMap
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.PartialResult
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScannerConstants
import com.android.utils.forEach
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * This Lint rule gathers all colours that are defined in a file suffixed with `_deprecated`. This may mean:
 * - Any individual colour definitions
 * - Any selectors
 *
 * It then flags any usages of those colours as an error.
 */
@Suppress("UnstableApiUsage")
class DeprecatedColorInXmlDetector : ResourceXmlDetector() {

    private var deprecatedColourNames = mutableListOf<String>()
    private var colourUsagesLintMap: LintMap = LintMap()

    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        // Return true if we want to analyse resource files in the specified resource
        // folder type.
        return folderType in listOf(
            ResourceFolderType.LAYOUT,
            ResourceFolderType.DRAWABLE,
            ResourceFolderType.COLOR,
            ResourceFolderType.VALUES
        )
    }

    override fun getApplicableAttributes(): Collection<String>? {
        // Return the set of attribute names we want to analyze. The `visitAttribute` method
        // below will be called each time lint sees one of these attributes in a
        // layout XML resource file. In this case, we want to analyze every attribute
        // in every layout XML resource file.
        return XmlScannerConstants.ALL
    }

    override fun getApplicableElements(): Collection<String> = listOf(
        SdkConstants.TAG_COLOR,
        SdkConstants.TAG_ITEM
    )

    override fun checkPartialResults(context: Context, partialResults: PartialResult) {
        // Aggregate all the recorded colour usages and deprecated colours from all the projects
        val allColourUsagesLintMap = LintMap()
        val allDeprecatedColours = mutableListOf<String>()

        // Each project (aka module) would have its own PartialResults
        // Here we retrieve the values we have saved for each project
        partialResults.forEach { partialResult ->
            val partialResultValue = partialResult.value

            // Merge all the deprecated colours together into one massive list
            val colourName = partialResultValue[KEY_COLOUR_NAMES]
            allDeprecatedColours.addAll(colourName?.split(COLOUR_NAME_DELIMITER).orEmpty())

            // Merge all colour usages together into one massive LintMap
            val colourUsages = partialResultValue.getMap(KEY_COLOUR_USAGES)
            colourUsages?.let { allColourUsagesLintMap.putAll(colourUsages) }
        }

        // There are no deprecated colours, nothing to report
        if (allDeprecatedColours.isEmpty()) return

        // There are no colour usages, nothing to report
        if (allColourUsagesLintMap.isEmpty()) return

        // Check colour usages to see if any are using any of the deprecated colours
        allColourUsagesLintMap.forEach { key ->
            val colourName = key.substringBefore(COLOUR_NAME_DELIMITER)
            if (allDeprecatedColours.contains(colourName)) {
                var location = allColourUsagesLintMap.getLocation(key)

                // If for some reason the location is not available (should be impossible really)
                // Just reference the project
                if (location == null) {
                    location = Location.create(context.project.dir)
                }

                val incident = Incident(context)
                    .issue(ISSUE)
                    .location(location)
                    .message("Deprecated colours should not be used")
                context.report(incident)
            }
        }
    }

    override fun afterCheckEachProject(context: Context) {
        super.afterCheckEachProject(context)

        // Save all the information we have found for this project (aka module)
        // This information will be used later to figure out what needs to be reported
        val allColours = deprecatedColourNames.joinToString(COLOUR_NAME_DELIMITER)
        context.getPartialResults(ISSUE).map().apply {
            put(KEY_COLOUR_NAMES, allColours)
            put(KEY_COLOUR_USAGES, colourUsagesLintMap)
        }
    }

    /**
     * For each file, we gather all the names of deprecated colours
     */
    override fun visitDocument(context: XmlContext, document: Document) {
        // Is file in the `color` or `values` folder?
        val folderType = context.resourceFolderType

        if (folderType !in listOf(ResourceFolderType.COLOR, ResourceFolderType.VALUES)) return

        // Does file name contain `_deprecated`
        val isFileDeprecated = context.file.name.contains("_deprecated")
        if (!isFileDeprecated) return

        // Get all the deprecated colours we can find

        // If this is a selector, the root node will be `<selector>`
        // And the color name is the filename
        if (document.documentElement.tagName == SdkConstants.TAG_SELECTOR) {
            deprecatedColourNames.add(context.file.name.substringBefore(SdkConstants.DOT_XML))
            return
        }

        // If this is not a selector but a normal resource file,
        // Get all `color` tags, anything else will be ignored
        val allColorNodes = document.getElementsByTagName(SdkConstants.TAG_COLOR)
        allColorNodes.forEach { node ->
            // Get the attribute "name", which in our case will be the colour's name
            val namedNode = node.attributes.getNamedItem(SdkConstants.ATTR_NAME)
            val colorName = namedNode.nodeValue

            // Save that value as a deprecated colour
            deprecatedColourNames.add(colorName)
        }
    }

    /**
     * In most cases, a colour will be used as an attribute:
     * ```
     * <TextView android:background="@color/a_deprecated_color">
     * ```
     * or
     * ```
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     *     <item android:color="@color/a_deprecated_color" android:state_enabled="false" />
     *     <item android:color="@color/a_deprecated_color" />
     * </selector
     * ```
     *
     * Look for those usages here.
     */
    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        // The issue is suppressed for this attribute, skip it
        val isIssueSuppressed = context.driver.isSuppressed(context, ISSUE, attribute)
        if (isIssueSuppressed) return

        // Save the value and location of the XML attribute.
        saveColourUsage(
            attribute.nodeValue,
            location = context.getValueLocation(attribute),
        )
    }

    /**
     * Colours can also be defined as values of an `item` (i.e, not an attribute value!)
     * so we need to visit the element.
     *
     * For example, a deprecated colour can be used in a theme or a style:
     * ```
     * <style name="Brand.SponsoredSpan">
     *  <item name="textColor">@color/a_deprecated_color</item>
     * </style>
     * ```
     */
    override fun visitElement(context: XmlContext, element: Element) {
        // The issue is suppressed for this element, skip it
        val isIssueSuppressed = context.driver.isSuppressed(context, ISSUE, element)
        if (isIssueSuppressed) return

        // Check if the value of this element is a deprecated colour
        if (element.firstChild == null) return

        val fileContents = context.getContents()
        val colorValue = element.firstChild
        val colorLocationStart = context.parser.getNodeStartOffset(context, colorValue)
        val colorLocationEnd = context.parser.getNodeEndOffset(context, colorValue)

        saveColourUsage(
            element.firstChild.nodeValue,
            location = Location.create(context.file, fileContents, colorLocationStart, colorLocationEnd),
        )
    }

    /**
     * Now that we know where to look, record the colour usage
     */
    private fun saveColourUsage(
        value: String,
        location: Location,
    ) {
        // Attempt to parse the attribute value into a resource url reference.
        // Return immediately if the attribute value is not a resource url reference.
        val resourceUrl = ResourceUrl.parse(value) ?: return

        if (resourceUrl.type != ResourceType.COLOR) {
            // Ignore the attribute value if it isn't a color resource.
            return
        }

        if (resourceUrl.isFramework) {
            // Ignore the attribute value if this is a color resource from the Android framework
            // (i.e. `@android:color/***`).
            return
        }

        storeFoundColourReference(resourceUrl, location)
    }

    private fun storeFoundColourReference(resourceUrl: ResourceUrl,
                                          location: Location) {
        // We "remember" this colour usage for analysis later (`checkPartialResults` callback)
        // The key in the `LintMap` must be unique, so we hash the location
        val lintMapKey = "${resourceUrl.name}$COLOUR_NAME_DELIMITER${location.hashCode()}"
        colourUsagesLintMap.put(lintMapKey, location)
    }

    companion object {
        const val KEY_COLOUR_NAMES = "key_colour_names"
        const val KEY_COLOUR_USAGES = "key_colour_usages"

        const val COLOUR_NAME_DELIMITER = "::"

        val ISSUE = Issue.create(
            id = "DeprecatedColorInXml",
            briefDescription = "Prohibits usages of the deprecated colours in XML",
            explanation =
            """
                    This check makes sure that deprecated colours are not used in XML files.
            """,
            category = Category.CORRECTNESS,
            severity = Severity.ERROR,
            implementation = Implementation(
                DeprecatedColorInXmlDetector::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }
}
