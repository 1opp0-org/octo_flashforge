package net.hello


import com.android.ide.common.vectordrawable.Svg2Vector
import java.nio.file.Path
import kotlin.io.path.outputStream


fun main(args: Array<String>) {
    println("🛠️ Converts SVG files to Android VectorDrawable XML format")

    val input = Path.of("crossplatform/src/desktopMain" + "/composeResources/drawable/logo.svg")
    val output = Path.of("crossplatform/src/androidMain" + "/res/drawable/logo.xml")
    convert(input = input, output = output)

    println("Input file     = '$input'")
    println("Generated file = '$output'")

}


private fun convert(input: Path, output: Path) {


    Svg2Vector.parseSvgToXml(input, output.outputStream())

}
