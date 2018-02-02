package org.code3.bottin.repository

import groovy.sql.Sql
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Statement
import org.code3.bottin.Relation
import org.code3.bottin.Address
import org.code3.bottin.Email
import org.code3.bottin.Telephone
import org.code3.bottin.Contact
import groovy.sql.Sql

@Service
public class ContactRepository {

  @Autowired
  DataSource datasource

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

  def stmt = [
    select_all: """
      select
        id,
        type_organization,
        firstname,
        lastname,
        organization_name,
        notes,
        avatar_url
      from contact where archived = false;
    """,
    insert_contact: """
      insert into contact (
        type_organization,
        firstname,
        lastname,
        organization_name,
        notes,
        avatar_url
        ) values (:type_organization, :firstname, :lastname, :organization_name, :notes, :avatar_url) ;
    """,
    get_contact_by_id: """
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
    """,
    update_contact_by_id: """
      update contact set
       type_organization = :type_organization,
       firstname = :firstname,
       lastname = :lastname,
       organization_name = :organization_name,
       notes = :notes,
       avatar_url = :avatar_url
      where id = :id;
    """,
    get_telephones_by_contact_id: """
      select
        id,
        type,
        number
      from telephone
      where contact_id = :contact_id
      order by index;
    """,
    insert_telephone: """
      insert into telephone (
        type,
        number,
        index,
        contact_id
      ) values (:type, :number, :index, :contact_id);
    """,
    update_telephone: """
      update telephone set
        type = :type,
        number = :number
        where
        index = :index and contact_id = :contact_id;
    """,
    get_emails_by_contact_id: """
      select
        id,
        type,
        address
      from email
      where contact_id = :contact_id
      order by index;
    """,
    insert_email_stmt: """
      insert into email (
        type,
        address,
        index,
        contact_id
      ) values (:type, :address, :index, :contact_id) ;
    """,
    update_email: """
      update email set
        type = :type,
        address = :address
        where
        index = :index and
        contact_id = :contact_id;
    """,
    get_addresses_by_contact_id: """
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
    """,
    insert_address: """
      insert into address (
        type, unit, street, locality, region_code, pobox, postal_code, country_code, delivery_info, index, contact_id
      ) values (:type, :unit, :street, :locality, :region_code, :pobox, :postal_code, :country_code, :delivery_info, :index, :contact_id) ;
    """,
    update_address: """
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
    """,
    archive_contact: """
      update contact set archived = true where id=:id
    """,
    restore_contact: """
      update contact set archived = false where id=:id
    """,
    // update_relation: """update relation set "left" = :left_contact, "right" = :right_contact, role = :role where id = :id;""",
    update_relation: """
      update relation set
        "role" = :role_description,
        "left" = :left_contact,
        "right" = :righ_contact
      where
        id = :relation_id;
    """,
    insert_relation: """
      insert into relation (
        "left",
        "right",
        role
      ) values (
        :left_contact,
        :right_contact,
        :role
      );
    """,
    get_relation_by_contact: """
      select
        "left",
        "right",
        role,
        id
      from relation where
      "left" = :contact_id OR
      "right" = :contact_id;
    """
  ]

  def listContacts() {
    withSql { sql -> sql.rows(stmt.select_all)  }
  }
  def addContact(Contact contact){
    withSql { sql ->
      def res = sql.executeInsert(stmt.insert_contact, contact)
      new Contact(sql.firstRow(stmt.get_contact_by_id, [contact_id: res[0][0]]))
    }
  }
  def getContact(Long contact_id){
    withSql { sql ->
      def contact = getContact(sql, contact_id)
      if(!contact){
        throw new Exception("Contact not found")
      }
      contact.telephones = getContactTelephones(sql, contact_id)
      contact.emails = getContactEmails(sql, contact_id)
      contact.addresses = getContactAddresses(sql, contact_id)
      contact.relations = getContactRelations(sql, contact_id)

      contact
    }
  }
  def getContact(sql, long contact_id){
    new Contact(sql.firstRow(stmt.get_contact_by_id, [contact_id: contact_id]))
  }
  def updateContact(Long contact_id, Contact contact){
    withSql { sql ->
      def nb_rows_updated = sql.executeUpdate(stmt.update_contact_by_id, contact)

      if(nb_rows_updated > 0){
        updateContactTelephones(sql, contact_id, contact.telephones)
        updateContactEmails(sql, contact_id, contact.emails)
        updateContactAddresses(sql, contact_id, contact.addresses)
        updateRelations(sql, contact_id, contact.relations)
      }

    }
  }
  def archive_contact(Long contact_id){
    withSql { sql -> sql.executeUpdate(stmt.archive_contact, [contact_id: contact_id]) }

  }
  def restore_contact(Long contact_id){
    withSql { sql -> sql.executeUpdate(stmt.restore_contact, [contact_id: contact_id]) }
  }

  def getContactTelephones(sql, long contact_id){
    sql.rows(stmt.get_telephones_by_contact_id, [contact_id: contact_id]).collect({new Telephone(it)})
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
    sql.executeUpdate(stmt.update_telephone, params)
  }
  def insertContactTelephone(sql, contact_id, index, telephone){
    def params = [
      id: telephone.id,
      type:telephone.type,
      number: telephone.number,
      contact_id: contact_id,
      index: index
    ]
    sql.executeInsert(stmt.insert_telephone, params)
  }

  def getContactEmails(sql, long contact_id){
    sql.rows(stmt.get_emails_by_contact_id, [contact_id: contact_id]).collect({new Email(it)})
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
    sql.executeUpdate(stmt.update_email, params)
  }
  def insertContactEmail(sql, contact_id, index, email){
    def params = [
      contact_id: contact_id,
      index: index,
      id: email.id,
      address: email.address,
      type: email.type
    ]
    sql.executeInsert(params, stmt.insert_email)
  }

  def getContactAddresses(sql, long contact_id){
    sql.rows(stmt.get_addresses_by_contact_id, [contact_id: contact_id]).collect({new Address(it)})
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
    sql.executeUpdate(stmt.update_address, params)
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
    sql.executeInsert(stmt.insert_address, params)
  }

  def getContactRelations(sql, long contact_id) {
    def rows = sql.rows(stmt.get_relation_by_contact, [contact_id: contact_id])
    rows.collect({ row ->
      new Relation([
        id: row.id,
        other: getContact(sql, contact_id == row.left ? row.left : row.right),
        role: row.role
      ])
    })
  }
  def updateRelations(sql, contact_id, relations){
    relations.each { relation ->
      if(relation.id){
        updateRelation(sql, contact_id, relation)
      } else {
        insertRelation(sql, contact_id, relation)
      }
    }
  }
  def updateRelation(sql, contact_id, relation){
    def params = [
      relation_id: relation.id,
      left_contact: relation.other.id,
      right_contact: contact_id,
      role: relation.role
    ]
    sql.executeUpdate(stmt.update_relation, params)
  }
  def insertRelation(sql, contact_id, relation) {
    def params = [
      left_contact: relation.other.id,
      right_contact: contact_id,
      role: relation.role,
    ]
    sql.executeInsert(stmt.insert_relation, params)
  }
}
