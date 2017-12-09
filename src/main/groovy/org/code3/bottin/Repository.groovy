package org.code3.bottin

import groovy.sql.Sql
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Statement
import groovy.sql.Sql

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
    from contact where archived = false;
  """
  def insert_stmt = """
    insert into contact (
      type_organization,
      firstname,
      lastname,
      organization_name,
      notes,
      avatar_url
      ) values (:type_organization, :firstname, :lastname, :organization_name, :notes, :avatar_url) ;
  """
  def get_contact_by_id_stmt = """
    select
      id,
      type_organization,
      firstname,
      lastname,
      organization_name,
      notes,
      avatar_url,
      archived
    from
      contact where id = :contact_id;
  """
  def update_contact_by_id_stmt = """
    update contact set
     type_organization = :type_organization,
     firstname = :firstname,
     lastname = :lastname,
     organization_name = :organization_name,
     notes = :notes,
     avatar_url = :avatar_url
    where id = :id;
  """
  def get_telephones_by_contact_id_stmt = """
    select
      id,
      type,
      number
    from telephone
    where contact_id = :contact_id
    order by index;
  """
  def insert_telephone_stmt = """
    insert into telephone (
      type,
      number,
      index,
      contact_id
    ) values (:type, :number, :index, :contact_id);
  """
  def update_telephone_stmt = """
    update telephone set
      type = :type,
      number = :number
      where
      index = :index and contact_id = :contact_id;
  """

  def get_emails_by_contact_id_stmt = """
    select
      id,
      type,
      address
    from email
    where contact_id = :contact_id
    order by index;
  """
  def insert_email_stmt = """
    insert into email (
      type,
      address,
      index,
      contact_id
    ) values (:type, :address, :index, :contact_id) ;
  """
  def update_email_stmt = """
    update email set
      type = :type,
      address = :address
      where
      index = :index and
      contact_id = :contact_id;
  """

  def get_addresses_by_contact_id_stmt = """
    select
      id,
      type,
      unit,
      street,
      locality,
      region_code,
      pobox,
      postal_code,
      country_code,
      delivery_info
    from address
    where contact_id = :contact_id
    order by index;
  """
  def insert_address_stmt = """
    insert into address (
      type,
      unit,
      street,
      locality,
      region_code,
      pobox,
      postal_code,
      country_code,
      delivery_info,
      index,
      contact_id
    ) values (:type, :unit, :street, :locality, :region_code, :pobox, :postal_code, :country_code, :delivery_info, :index, :contact_id) ;
  """
  def update_address_stmt = """
    update address set
      type = :type,
      unit = :unit,
      street = :street,
      locality = :locality,
      region_code = :region_code,
      pobox = :pobox,
      postal_code = :postaL_code,
      country_code = :country_code,
      delivery_info = :delivery_info
      where
      index = :index and
      contact_id = :contact_id;
  """

  def archive_contact_stmt = """
    update contact set archived = true where id=:id
  """

  def restore_contact_stmt = """
    update contact set archived = false where id=:id
  """

  def listContacts() {
    withSql { sql -> sql.rows(select_all_stmt)  }
  }

  def addContact(Contact contact){
    withSql { sql ->
      def res = sql.executeInsert(insert_stmt, contact)
      new Contact(sql.firstRow(get_contact_by_id_stmt, [contact_id: res[0][0]]))
    }
  }

  def getContact(Long contact_id){
    withSql { sql ->
      def contact = new Contact(sql.firstRow(get_contact_by_id_stmt, [contact_id: contact_id]))
      if(!contact){
        throw new Exception("Contact not found")
      }
      contact.telephones = getContactTelephones(sql, contact_id)
      contact.emails = getContactEmails(sql, contact_id)
      contact.addresses = getContactAddresses(sql, contact_id)
      contact
    }
  }
  def updateContact(Long contact_id, Contact contact){
    withSql { sql ->
      def nb_rows_updated = sql.executeUpdate(update_contact_by_id_stmt, contact)

      if(nb_rows_updated > 0){
        updateContactTelephones(sql, contact_id, contact.telephones)
        updateContactEmails(sql, contact_id, contact.emails)
        updateContactAddresses(sql, contact_id, contact.addresses)
      }

    }
  }
  def archive_contact(Long contact_id){
    withSql { sql -> sql.executeUpdate(archive_contact_stmt, [contact_id: contact_id]) }

  }
  def restore_contact(Long contact_id){
    withSql { sql -> sql.executeUpdate(restore_contact_stmt, [contact_id: contact_id]) }
  }

  def getContactTelephones(sql, long contact_id){
    sql.rows(get_telephones_by_contact_id_stmt, [contact_id: contact_id]).collect({new Telephone(it)})
  }

  def updateContactTelephones(sql, long contact_id, telephones){
    //Telphones will contains new and old telephones entities
    telephones.eachWithIndex { telephone, index ->
      if(telephone.id) {
        updateContactTelephone(sql, contact_id, index, telephone)
      } else {
        insertContactTelephone(sql, contact_id, index, telephone)
      }
    }
  }
  def updateContactTelephone(sql, contact_id, index, telephone){
    def params = [
      id: telephone.id,
      type:telephone.type,
      number: telephone.number,
      contact_id: contact_id,
      index: index
    ]
    sql.executeUpdate(update_telephone_stmt, params)
  }
  def insertContactTelephone(sql, contact_id, index, telephone){
    def params = [
      id: telephone.id,
      type:telephone.type,
      number: telephone.number,
      contact_id: contact_id,
      index: index
    ]
    sql.executeInsert(insert_telephone_stmt, params)
  }

  def getContactEmails(sql, long contact_id){
    sql.rows(get_emails_by_contact_id_stmt, [contact_id: contact_id]).collect({new Email(it)})
  }
  def updateContactEmails(sql, long contact_id, emails){
    emails.eachWithIndex { email, index ->
      if(email.id) {
        updateContactEmail(sql, contact_id, index, email)
      } else {
        insertContactEmail(sql, contact_id, index, email)
      }
    }
  }
  def updateContactEmail(sql, contact_id, index, email){
    def params = [
      contact_id: contact_id,
      index: index,
      id: email.id,
      address: email.address,
      type: email.type
    ]
    sql.executeUpdate(update_email_stmt, params)
  }
  def insertContactEmail(sql, contact_id, index, email){
    def params = [
      contact_id: contact_id,
      index: index,
      id: email.id,
      address: email.address,
      type: email.type
    ]
    sql.executeInsert(params, insert_email_stmt)
  }


  def getContactAddresses(sql, long contact_id){
    sql.rows(get_addresses_by_contact_id_stmt, [contact_id: contact_id]).collect({new Address(it)})
  }
  def updateContactAddresses(sql, long contact_id, addresses){
    addresses.eachWithIndex { address, index ->
      if(address.id) {
        updateContactAddress(sql, contact_id, index, address)
      } else {
        insertContactAddress(sql, contact_id, index, address)
      }
    }
  }
  def updateContactAddress(sql, contact_id, index, address){
    // x = [a:1, b:2]
    // y = [c:3, d:4]
    //
    // z = [*:x, *:y]
    def params = [
      type: address.type,
      unit: address.unit,
      street: address.street,
      locality: address.locality,
      region_code: address.region_code,
      pobox: address.pobox,
      postaL_code: address.postal_code,
      country_code: address.country_code,
      delivery_info: address.delivery_info,
      index: index,
      contact_id: contact_id
    ]
    sql.executeUpdate(update_address_stmt, params)
  }
  def insertContactAddress(sql, contact_id, index, address){
    def params = [
      type: address.type,
      unit: address.unit,
      street: address.street,
      locality: address.locality,
      region_code: address.region_code,
      pobox: address.pobox,
      postaL_code: address.postal_code,
      country_code: address.country_code,
      delivery_info: address.delivery_info,
      index: index,
      contact_id: contact_id
    ]
    sql.executeInsert(insert_address_stmt, params)
  }

  def withSql(closure){
    def sql
    try{
      sql =  new Sql(datasource.getConnection())
      closure(sql)
    }
    finally {
      sql?.close()
    }
  }


}
