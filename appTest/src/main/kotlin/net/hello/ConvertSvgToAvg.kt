package net.hello


import com.android.ide.common.vectordrawable.Svg2Vector
import java.nio.file.Path
import kotlin.io.path.outputStream

/**
 * Converts a single SVG file to a single Android VectorDrawable XML file.
 *
 * To convert a single SVG file to the multiple images used by the launcher icon wizard, see [LauncherIconGenerator] from https://cs.android.com/android-studio/platform/tools/adt/idea/+/mirror-goog-studio-main:android-npw/src/com/android/tools/idea/npw/assetstudio/LauncherIconGenerator.java
 */
fun main(args: Array<String>) {
    println("🛠️ Converts SVG files to Android VectorDrawable XML format")

    val input = Path.of("crossplatform/src/desktopMain" + "/composeResources/drawable/logo.svg")
    val output = Path.of("crossplatform/src/androidMain" + "/res/drawable/logo.xml")
    val error = convert(input = input, output = output)

    println("Input file     = '$input'")
    println("Generated file = '$output'")
    println("Error message = '$error'")

}


/**
 * Converts an SVG file to an Android VectorDrawable XML file.
 *
 * @param input The path to the input SVG file.
 * @param output The path where the generated XML file will be saved.
 * @return Error message`
 */
private fun convert(input: Path, output: Path): String {


    return Svg2Vector.parseSvgToXml(input, output.outputStream())

}
