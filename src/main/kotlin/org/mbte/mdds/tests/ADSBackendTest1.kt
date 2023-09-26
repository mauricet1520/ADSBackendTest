package org.mbte.mdds.tests

import org.json.JSONArray
import org.json.JSONObject
import org.mbte.mdds.util.DatabaseHandler
import org.mbte.mdds.util.loadXmlFromFile
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    val test = ADSBackendTest1()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val userHome = File(System.getProperty("user.home"))
    val time = dateFormatter.format(LocalDateTime.now()).replace(" ", "_").replace(":", ";")
    val dbFile = File(userHome, "${test.javaClass.simpleName}-$time.db")
    dbFile.createNewFile()
    val dbHandler = DatabaseHandler("jdbc:sqlite:${dbFile.absolutePath}")
    dbHandler.initContactsTable()

    //Load the xml file 'ab.xml'
    val doc = test.loadXml("ab.xml")

    if (doc != null) {
        // Load the address book contents from 'ab.xml'
        val addressBook = test.loadAddressBook(doc)

        // Insert each contact into the Database
        addressBook.contacts.forEach { dbHandler.insertContact(contact = it) }

        // Retrieve all contacts from the Database
        val contactsFromDb = dbHandler.getAllContacts()

        //Convert the contacts from the Database into Json and write to file
        test.printOutput(test.convertToJson(AddressBook(contactsFromDb)), createJsonFile())
    }
    /**
     * 1. Load the xml file 'ab.xml'
     * 2. Load the address book contents from 'ab.xml'
     * 3. Insert each contact into the Database
     * 4. Retrieve all contacts from the Database
     * 5. Convert the contacts from the Database into Json and write to file
     */

    println("Assessment complete.")
    println("Database file located at ${dbFile.absolutePath}")
}

private fun createJsonFile(): File {
    val resourceUrl = ADSBackendTest1::class.java.classLoader.getResource("addressbook.json")

    val filePath = resourceUrl?.toURI()?.path
    return File(filePath.toString())
}

data class AddressBook(val contacts: List<Contact>)

data class Contact(
    val id: String,
    val companyName: String,
    val name: String,
    val title: String,
    val address: String,
    val city: String,
    val email: String,
    val region: String?,
    val zip: String?,
    val country: String,
    val phone: String,
    val fax: String?
)

interface AddressBookInterface {
    fun loadXml(fileName: String): Document?
    fun loadAddressBook(doc: Document): AddressBook
    fun convertToJson(addressBook: AddressBook): JSONObject
    fun printOutput(json: JSONObject, output: File)
}

class ADSBackendTest1() : AddressBookInterface {
    override fun loadXml(fileName: String): Document? {
        val resourceUrl = ADSBackendTest1::class.java.classLoader.getResource(fileName)
        val filePath = resourceUrl?.toURI()?.path
        val file = File(filePath.toString())
        return loadXmlFromFile(file)
    }

    override fun loadAddressBook(doc: Document): AddressBook {
        val contacts = ArrayList<Contact>()
        val rootElement: Element? = doc.documentElement
        val nodeList: NodeList? = rootElement?.childNodes
        if (nodeList != null) {
            for (i in 0 until nodeList.length) {
                val node = nodeList.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val element = node as Element

                    contacts.add(
                        Contact(
                            id = escapeSingleQuotes(element.getElementsByTagName("CustomerID").item(0).textContent),
                            companyName = escapeSingleQuotes(element.getElementsByTagName("CompanyName").item(0).textContent),
                            name = escapeSingleQuotes(element.getElementsByTagName("ContactName").item(0).textContent),
                            title = escapeSingleQuotes(element.getElementsByTagName("ContactTitle").item(0).textContent),
                            email = escapeSingleQuotes(element.getElementsByTagName("Email").item(0).textContent),
                            zip = element.getElementsByTagName("PostalCode").item(0)?.textContent,
                            country = escapeSingleQuotes(element.getElementsByTagName("Country").item(0).textContent),
                            phone = escapeSingleQuotes(element.getElementsByTagName("Phone").item(0).textContent),
                            fax = element.getElementsByTagName("Fax").item(0)?.textContent,
                            region = element.getElementsByTagName("Region").item(0)?.textContent,
                            address = escapeSingleQuotes(element.getElementsByTagName("Address").item(0).textContent),
                            city = escapeSingleQuotes(element.getElementsByTagName("City").item(0).textContent)
                        )
                    )
                }
            }
        }
        return AddressBook(contacts)
    }

    override fun convertToJson(addressBook: AddressBook): JSONObject {

        val jsonAddressBook = JSONObject()
        val jsonContacts = JSONArray()
        for (contact in addressBook.contacts) {
            val jsonContact = JSONObject()
            jsonContact.put("name", contact.name)
            jsonContact.put("email", contact.email)
            jsonContact.put("title", contact.title)
            jsonContact.put("companyName", contact.companyName)
            jsonContact.put("id", contact.id)
            jsonContact.put("zip", contact.zip)
            jsonContact.put("country", contact.country)
            jsonContact.put("phone", contact.phone)
            jsonContact.put("region", contact.region)
            jsonContact.put("address", contact.address)
            jsonContact.put("city", contact.city)
            jsonContact.put("fax", contact.city)

            jsonContacts.put(jsonContact)
        }

        jsonAddressBook.put("AddressBook", JSONObject().put("Contact", jsonContacts))
        println(JSONObject.wrap(addressBook))

        return jsonAddressBook
    }

    override fun printOutput(json: JSONObject, output: File) {
        val fileWriter = FileWriter(output)
        fileWriter.use {
            it.write(json.toString())
        }
        println("JSON written to file: $output")
    }

    private fun escapeSingleQuotes(originalString: String): String {
        return originalString.replace("'", "''")
    }
}