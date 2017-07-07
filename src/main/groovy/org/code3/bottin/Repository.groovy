package org.code3.bottin

import groovy.sql.Sql
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Statement

@Service
public class Repository {

  @Autowired
  DataSource datasource

  def select_all_stmt = "select id, type, firstname, lastname, organization_name, notes from contact;"
  def insert_stmt = "insert into contact (type, firstname, lastname, organization_name, notes) values (?, ?, ?, ?, ?);"

  def listContacts() {

    withConnection { connection ->
      def contacts = []

      def select_all_prepared_stmt = connection.prepareStatement select_all_stmt
      select_all_prepared_stmt.execute()
      def rs = select_all_prepared_stmt.executeQuery()

      while( rs.next() ) {
        def contact = [
          id: rs.getInt("id"),
          firstname: rs.getString("firstname"),
          lastname: rs.getString("lastname"),
          organization_name: rs.getString("organization_name"),
          notes: rs.getString("notes")
        ]
        contacts.push(contact)
      }
      contacts
    }
  }

  def addContact(String type, String firstname, String lastname, String organization_name, String notes){
    withConnection { connection ->

      def insert_prepared_stmt = connection.prepareStatement(insert_stmt, Statement.RETURN_GENERATED_KEYS)
      insert_prepared_stmt.setString(1, type)
      insert_prepared_stmt.setString(2, firstname)
      insert_prepared_stmt.setString(3, lastname)
      insert_prepared_stmt.setString(4, organization_name)
      insert_prepared_stmt.setString(5, notes)

      insert_prepared_stmt.execute()
      def rs = insert_prepared_stmt.getGeneratedKeys()
      def saved_contact = [:]
      if( rs.next() ) {
        saved_contact = [
          id: rs.getInt("id"),
          firstname: firstname,
          lastname: lastname,
          organization_name: organization_name,
          notes: notes
        ]
      }
      saved_contact
    }
  }

  def withConnection(closure){
    def connection
    try{
      connection = datasource.getConnection()
      closure(connection)
    }
    finally {
      connection?.close()
    }
  }
}
