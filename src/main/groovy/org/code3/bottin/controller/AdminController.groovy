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

}
