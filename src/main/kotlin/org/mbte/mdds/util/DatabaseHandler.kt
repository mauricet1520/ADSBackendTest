package org.mbte.mdds.util

import org.mbte.mdds.tests.Contact
import java.sql.Connection
import java.sql.DriverManager

class DatabaseHandler(private val url: String) {
    init {
        // Register the SQLite JDBC driver
        Class.forName("org.sqlite.JDBC")
    }

    fun initContactsTable() {
        getConnection()?.use { connection ->
            val statement = connection.createStatement()
            val sql = ("CREATE TABLE IF NOT EXISTS contacts ("
                    + "id TEXT PRIMARY KEY,"
                    + "company_name TEXT NOT NULL,"
                    + "name TEXT NOT NULL,"
                    + "title TEXT NOT NULL,"
                    + "address TEXT NOT NULL,"
                    + "city TEXT NOT NULL,"
                    + "email TEXT NOT NULL,"
                    + "region TEXT,"
                    + "zip TEXT,"
                    + "country TEXT NOT NULL,"
                    + "phone TEXT NOT NULL,"
                    + "fax TEXT"
                    + ");")
            statement.executeUpdate(sql)
        }
    }

    fun insertContact(contact: Contact) {
        getConnection()?.use { connection ->
            val statement = connection.createStatement()
            val sql = """
            INSERT INTO contacts (id, company_name, name, title, address, city, email, region, zip, country, phone, fax)
            VALUES (
                '${contact.id}',
                '${contact.companyName}',
                '${contact.name}',
                '${contact.title}',
                '${contact.address}',
                '${contact.city}',
                '${contact.email}',
                '${contact.region}',
                '${contact.zip}',
                '${contact.country}',
                '${contact.phone}',
                '${contact.fax}'
            )
        """.trimIndent()
            kotlin.runCatching { statement.executeUpdate(sql) }
                .onFailure {
                    System.err.println("Failed to execute SQL: $sql")
                    it.printStackTrace()
                }

        }
    }

    fun getAllContacts(): List<Contact> {
        getConnection()?.use { connection ->
            val statement = connection.createStatement()
            val sql = "SELECT * FROM contacts"
            val result = kotlin.runCatching { statement.executeQuery(sql) }
            return if (result.isFailure) {
                System.err.println("Failed to execute SQL: $sql")
                result.exceptionOrNull()?.printStackTrace()
                emptyList()
            } else {
                val contacts = mutableListOf<Contact>()
                val resultSet = result.getOrNull()
                resultSet?.use { rs ->
                    while (rs.next()) {
                        val contact = Contact(
                            id = rs.getString("id"),
                            companyName = rs.getString("company_name"),
                            name = rs.getString("name"),
                            title = rs.getString("title"),
                            address = rs.getString("address"),
                            city = rs.getString("city"),
                            email = rs.getString("email"),
                            region = rs.getString("region"),
                            zip = rs.getString("zip"),
                            country = rs.getString("country"),
                            phone = rs.getString("phone"),
                            fax = rs.getString("fax")
                        )
                        contacts.add(contact)
                    }
                }
                contacts.toList()
            }
        }
        return emptyList()
    }

    private fun getConnection(): Connection? {
        return try {
            DriverManager.getConnection(url)
        } catch (e: Exception) {
            System.err.println("Failed to get connection to SQLite!!")
            e.printStackTrace()
            null
        }
    }
}