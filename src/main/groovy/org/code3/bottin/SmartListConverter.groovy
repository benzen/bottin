package org.code3.bottin

import org.springframework.stereotype.Service

@Service
class SmartListConverter {
  def smartListToSql(SmartList smartList){
    def predicatesAsSql = smartList.predicates.collect({predicateToSql(it)})
    //Default value is added as simplest case where no pred is defined
    def preds = predicatesAsSql.join( smartList.match_all_predicates ? " and " : " or " ) ?: false
    """
      select
        contact.id
      from
        contact
      left join address on
        address.contact_id = contact.id
      left join telephone on
        telephone.contact_id = contact.id
      left join email on
        email.contact_id = contact.id
      where
        contact.archived = false  and
        (${preds});
    """.toString()
  }
  def predicateToSql(Predicate predicate){
    "${fieldToSql(predicate)} ${operatorToSql(predicate)} '${valueToSql(predicate)}'"
  }
  def fieldToSql(predicate){
    def mapping  = [
      "addressType": "address.addressType",
      "addressStreet": "address.street",
      "addressLocality": "address.locality",
      "contactFirstName": "contact.firstname",
      "contactLastName": "contact.lastname",
    ]
    if(mapping[predicate.field] == null){
      throw new Exception("In smart list field \"$predicate.field\" is illegal")
    }
    mapping[predicate.field]
  }
  def operatorToSql(predicate){
    def mapping = [
      "is": " = ",
      "isn't": " != ",
      "startWith": "ilike",
      "endWith": "ilike",
      "contains": "ilike",
    ]
    if(mapping[predicate.operator] == null){
      throw new Exception("In smart list operator \"$predicate.operator\" is illegal")
    }
    mapping[predicate.operator]
  }
  def valueToSql(predicate){
    //TODO should also sql escape the value
    if(predicate.operator == "startWith"){
      return "$predicate.value%"
    } else if(predicate.operator == "endWith"){
      return "%$predicate.value"
    } else if(predicate.operator == "contains"){
      return "%$predicate.value%"
    } else{
      "$predicate.value"
    }
  }
}
