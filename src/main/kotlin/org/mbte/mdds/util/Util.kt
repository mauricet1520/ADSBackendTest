package org.mbte.mdds.util

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

private val docBuilderFactory = DocumentBuilderFactory.newInstance();

fun newDocumentBuilder(): DocumentBuilder = docBuilderFactory.newDocumentBuilder()

fun loadXmlFromString(xml: String): Document = newDocumentBuilder().parse(InputSource(StringReader(xml)))

fun loadXmlFromFile(f: File): Document? {
	runCatching { 
		FileInputStream(f).use { fis ->
			return loadXmlFromString(getStreamContents(fis))
		}
	}.onFailure { 
		System.err.println("Failed to load XML from ${f.absolutePath}");
        throw it
	}
	return null
}

fun getDirectChildren(node: Node): MutableSet<Element> {
	val children = mutableSetOf<Element>()
	val nodeList = node.childNodes
	for (i in 0 until nodeList.length) {
		val child = nodeList.item(i)
		if (child.nodeType == Node.ELEMENT_NODE) {
			children.add(child as Element)
		} 
	}
	return children
}

/**
 * @param is input stream
 * @return null or the content
 */
fun getStreamContents(`is`: InputStream): String {
    return try {
        val buffer = CharArray(0x10000)
        val out = StringBuilder()
        InputStreamReader(`is`, Charsets.UTF_8).use {
            var read: Int
            do {
                read = it.read(buffer, 0, buffer.size)
                if (read > 0) {
                    out.append(buffer, 0, read)
                }
            } while (read >= 0)
        }
        out.toString()
    } catch (ioe: IOException) {
        System.err.println("Failed to get stream contents!")
        ioe.printStackTrace()
        return ""
    }
}
