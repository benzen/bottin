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

  def select_all_stmt = """
    select
      id,
      type_organization,
      firstname,
      lastname,
      organization_name,
      notes,
      avatar_url
    from contact;
  """
  def insert_stmt = """
    insert into contact (
      type_organization,
      firstname,
      lastname,
      organization_name,
      notes,
      avatar_url
      ) values (?, ?, ?, ?, ?, ?) ;
  """
  def get_contact_by_id_stmt = """
    select
      id,
      type_organization,
      firstname,
      lastname,
      organization_name,
      notes,
      avatar_url
    from
      contact where id = ?;
  """
  def update_contact_by_id_stmt = """
    update contact set
     type_organization = ?,
     firstname = ?,
     lastname = ?,
     organization_name = ?,
     notes = ?,
     avatar_url = ?
    where id = ?;
  """
  def get_telephones_by_contact_id_stmt = """
    select
      id,
      type,
      number
    from telephone
    where contact_id = ?
    order by index;
  """
  def insert_telephone_stmt = """
    insert into telephone (
      type,
      number,
      index,
      contact_id
    ) values (?, ?, ?, ?) ;
  """
  def update_telephone_stmt = """
    update telephone set
      type = ?,
      number = ?
      where
      index = ? and
      contact_id = ?;
  """

  def get_emails_by_contact_id_stmt = """
    select
      id,
      type,
      address
    from email
    where contact_id = ?
    order by index;
  """
  def insert_email_stmt = """
    insert into email (
      type,
      address,
      index,
      contact_id
    ) values (?, ?, ?, ?) ;
  """
  def update_email_stmt = """
    update email set
      type = ?,
      address = ?
      where
      index = ? and
      contact_id = ?;
  """

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
      insert_prepared_stmt.setString(6, contact.avatar_url)

      insert_prepared_stmt.execute()
      def rs = insert_prepared_stmt.getGeneratedKeys()
      def saved_contact = new Contact()
      if( !rs.next() ) {
        throw new Exception("Failed to save a contact")
      }
      return rsToContact(rs)

    }
  }
  def getContact(Long contact_id){
    withConnection { connection ->
      def get_by_id_prepared_stmt = connection.prepareStatement get_contact_by_id_stmt
      get_by_id_prepared_stmt.setLong(1, contact_id)
      get_by_id_prepared_stmt.executeQuery()
      def rs = get_by_id_prepared_stmt.resultSet
      if(!rs.next()){
        throw new Exception("Contact not found")
      }
      def contact = rsToContact(rs)
      contact.telephones = getContactTelephones(connection, contact_id)
      contact.emails = getContactEmails(connection, contact_id)
      return contact
    }
  }
  def updateContact(Long contact_id, Contact contact){
    withConnection { connection ->
      def update_contact_by_id_prepared_stmt = connection.prepareStatement update_contact_by_id_stmt
      update_contact_by_id_prepared_stmt.setBoolean(1, contact.type_organization)
      update_contact_by_id_prepared_stmt.setString(2, contact.firstname)
      update_contact_by_id_prepared_stmt.setString(3, contact.lastname)
      update_contact_by_id_prepared_stmt.setString(4, contact.organization_name)
      update_contact_by_id_prepared_stmt.setString(5, contact.notes)
      update_contact_by_id_prepared_stmt.setString(6, contact.avatar_url)
      update_contact_by_id_prepared_stmt.setLong(7, contact_id)
      def nb_row_updated = update_contact_by_id_prepared_stmt.executeUpdate()
      if(nb_row_updated > 0){
        updateContactTelephones(connection, contact_id, contact.telephones)
        updateContactEmails(connection, contact_id, contact.emails)
      }

    }
  }

  def getContactTelephones(connection, long contact_id){
    def get_telephones_by_contact_id_prepared_stmt = connection.prepareStatement get_telephones_by_contact_id_stmt
    get_telephones_by_contact_id_prepared_stmt.setLong(1, contact_id)
    get_telephones_by_contact_id_prepared_stmt.executeQuery()
    def rs = get_telephones_by_contact_id_prepared_stmt.resultSet
    rsToTelephones(rs)
  }
  def updateContactTelephones(connection, long contact_id, telephones){
    //Telphones will contains new and old telephones entities
    telephones.eachWithIndex { telephone, index ->
      if(telephone.id) {
        updateContactTelephone(connection, contact_id, index, telephone)
      } else {
        insertContactTelephone(connection, contact_id, index, telephone)
      }
    }
  }
  def updateContactTelephone(connection, contact_id, index, telephone){
    def update_telephone_prepared_stmt = connection.prepareStatement update_telephone_stmt
    update_telephone_prepared_stmt.setString(1, telephone.type)
    update_telephone_prepared_stmt.setString(2, telephone.number)
    update_telephone_prepared_stmt.setLong(3, index)
    update_telephone_prepared_stmt.setLong(4, contact_id)
    update_telephone_prepared_stmt.executeUpdate()
  }
  def insertContactTelephone(connection, contact_id, index, telephone){
    def insert_telephone_prepared_stmt = connection.prepareStatement insert_telephone_stmt
    insert_telephone_prepared_stmt.setString(1, telephone.type)
    insert_telephone_prepared_stmt.setString(2, telephone.number)
    insert_telephone_prepared_stmt.setLong(3, index)
    insert_telephone_prepared_stmt.setLong(4, contact_id)
    insert_telephone_prepared_stmt.executeUpdate()
  }

  def getContactEmails(connection, long contact_id){
    def get_emails_by_contact_id_prepared_stmt = connection.prepareStatement get_emails_by_contact_id_stmt
    get_emails_by_contact_id_prepared_stmt.setLong(1, contact_id)
    get_emails_by_contact_id_prepared_stmt.executeQuery()
    def rs = get_emails_by_contact_id_prepared_stmt.resultSet
    rsToEmails(rs)
  }

  def updateContactEmails(connection, long contact_id, emails){
    emails.eachWithIndex { email, index ->
      if(email.id) {
        updateContactEmail(connection, contact_id, index, email)
      } else {
        insertContactEmail(connection, contact_id, index, email)
      }
    }
  }

  def updateContactEmail(connection, contact_id, index, email){
    def update_email_prepared_stmt = connection.prepareStatement update_email_stmt
    update_email_prepared_stmt.setString(1, email.type)
    update_email_prepared_stmt.setString(2, email.address)
    update_email_prepared_stmt.setLong(3, index)
    update_email_prepared_stmt.setLong(4, contact_id)

    update_email_prepared_stmt.executeUpdate()
  }
  def insertContactEmail(connection, contact_id, index, email){
    def insert_email_prepared_stmt = connection.prepareStatement insert_email_stmt
    insert_email_prepared_stmt.setString(1, email.type)
    insert_email_prepared_stmt.setString(2, email.address)
    insert_email_prepared_stmt.setLong(3, index)
    insert_email_prepared_stmt.setLong(4, contact_id)
    insert_email_prepared_stmt.executeUpdate()
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
  def rsToContact(rs){
    new Contact([
      id: rs.getLong("id"),
      type_organization: rs.getBoolean("type_organization"),
      firstname: rs.getString("firstname"),
      lastname: rs.getString("lastname"),
      organization_name: rs.getString("organization_name"),
      notes: rs.getString("notes"),
      avatar_url: rs.getString("avatar_url")
    ])
  }
  def rsToTelephones(rs){
    def telephones = []
    while(rs.next()){
      def tel = new Telephone([
        id: rs.getLong("id"),
        type: rs.getString("type"),
        number: rs.getString("number"),
      ])
      telephones.add(tel)
    }
    return telephones
  }

  def rsToEmails(rs){
    def emails = []
    while(rs.next()){
      def email = new Email([
        id: rs.getLong("id"),
        type: rs.getString("type"),
        address: rs.getString("address")
      ])
      emails.add(email)
    }
    emails
  }


}
