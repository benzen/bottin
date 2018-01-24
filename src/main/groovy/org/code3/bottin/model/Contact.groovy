package org.code3.bottin

public class Contact {
  Long id
  boolean type_organization
  String firstname
  String lastname
  String organization_name
  String notes
  String avatar_url
  boolean archived

  java.util.List<Telephone>  telephones
  java.util.List<Email> emails
  java.util.List<Address> addresses
  java.util.List<Relation> relations

}
