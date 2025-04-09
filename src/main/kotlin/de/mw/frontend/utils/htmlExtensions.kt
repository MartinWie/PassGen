package de.mw.frontend.utils

import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import kotlinx.html.stream.appendHTML
import kotlinx.html.unsafe

/**
 * Builds an HTML string using the provided builder action.
 *
 * @param builderAction The action to build the HTML content.
 * @return The generated HTML string.
 *
 * Example usage:
 * body {
 *     h1 { +"Hello, World!" }
 * }
 */
fun buildHTMLString(builderAction: TagConsumer<StringBuilder>.() -> Unit): String {
    return buildString {
        appendHTML().builderAction()
    }
}

/**
 * Embeds an SVG from a resource path directly into the HTML.
 *
 * @param svgPath The path to the SVG file in the resources directory.
 *
 * Example usage:
 * div {
 *     embedSvg("/icons/user.svg")
 * }
 */
fun HTMLTag.embedSvg(svgPath: String) {
    unsafe { +SvgLoader.loadSvg(svgPath) }
}

/**
 * Loads SVG content from a file in the resources directory.
 *
 * @param path The path to the SVG file in the resources directory.
 * @return The SVG content as a string.
 *
 * Example usage:
 * val svgContent = SvgLoader.loadSvg("/icons/logo.svg")
 */
object SvgLoader {
    /**
     * Loads SVG content from a file in the resources directory
     */
    fun loadSvg(path: String): String {
        val resource = SvgLoader::class.java.getResourceAsStream(path)
            ?: throw IllegalArgumentException("SVG file not found: $path")

        return resource.bufferedReader().use { it.readText() }
    }
}