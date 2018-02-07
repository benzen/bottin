package org.code3.bottin

class Predicate {
  String field
  String operator
  String value
}
/*
Fields
* address:
  type
  street
  locality
  region_code
  pobox
  postal_code
  country_code
  delivery_info
* telephone:
  number
  type
* email:
  type
  address
* relation
  role
  XXX (other side of relation seams pretty hard to do)

Operator
  * is
  * is not
  * startWith
  * dontStartWith
  * endWith
  * dontEndWith
  * contains
  * dontContains

Value is a straight string

*/
