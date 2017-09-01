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

  def select_all_stmt = "select id, type_organization, firstname, lastname, organization_name, notes from contact;"
  def insert_stmt = "insert into contact (type_organization, firstname, lastname, organization_name, notes) values (?, ?, ?, ?, ?) ;"
  def get_by_id_stmt = "select id, type_organization, firstname, lastname, organization_name, notes from contact where id = ?; "

  def listContacts() {

    withConnection { connection ->
      def contacts = []

      def select_all_prepared_stmt = connection.prepareStatement select_all_stmt
      select_all_prepared_stmt.execute()
      def rs = select_all_prepared_stmt.executeQuery()

      while( rs.next() ) {
        def contact = [
          id: rs.getInt("id"),
          type_organization: rs.getBoolean("type_organization"),
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
  def addContact(Contact contact){
    withConnection { connection ->

      def insert_prepared_stmt = connection.prepareStatement(insert_stmt, Statement.RETURN_GENERATED_KEYS)
      insert_prepared_stmt.setBoolean(1, contact.type_organization)
      insert_prepared_stmt.setString(2, contact.firstname)
      insert_prepared_stmt.setString(3, contact.lastname)
      insert_prepared_stmt.setString(4, contact.organization_name)
      insert_prepared_stmt.setString(5, contact.notes)

      insert_prepared_stmt.execute()
      def rs = insert_prepared_stmt.getGeneratedKeys()
      def saved_contact = new Contact()
      if( !rs.next() ) {
        throw new Exception("Failed to save a contact")
      }
      return resultSetToContact(rs)

    }
  }
  def getContact(Long contact_id){
    withConnection { connection ->
      def get_by_id_prepared_stmt = connection.prepareStatement get_by_id_stmt
      get_by_id_prepared_stmt.setLong(1, contact_id)
      get_by_id_prepared_stmt.executeQuery()
      def rs = get_by_id_prepared_stmt.getResultSet()
      if(!rs.next()){
        throw new Exception("Contact not found")
      }
      return resultSetToContact(rs)
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
  def resultSetToContact(rs){
    new Contact([
      id: rs.getLong("id"),
      type_organization: rs.getBoolean("type_organization"),
      firstname: rs.getString("firstname"),
      lastname: rs.getString("lastname"),
      organization_name: rs.getString("organization_name"),
      notes: rs.getString("notes")
    ])
  }
}
