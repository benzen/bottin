package org.code3.bottin

import org.springframework.stereotype.Controller
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AdminController{

  @Autowired
  SearchIndex searchIndex

  @Autowired
  ContactRepository contactRepository

  @GetMapping("admin/reindex")
  def reindex(){
    searchIndex.removeAll()
    def contacts = contactRepository.listContacts()
    contacts.each { contact ->
      searchIndex.indexContact(contact)
    }
    "redirect:/"

  }

  // @GetMapping("admin/test")
  // def test(){
  //   def smartList = new SmartList([
  //     matchAllPredicates: false,
  //     predicates: [
  //       // new Predicate(field: "addressLocality", operator: "is", value: "Montréal"),
  //       // new Predicate(field: "addressStreet", operator: "is", value: "adam street"),
  //       new Predicate(field: "addressStreet", operator: "contains", value: "Adam"),
  //     ],
  //   ])
  //
  //   def conv = new SmartListConverter()
  //   def sql = conv.smartListToSql(smartList)
  //   println sql
  // }

}
