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

  List<Telephone>  telephones
  List<Email> emails
  List<Address> addresses

}
