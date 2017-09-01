package org.code3.bottin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable

@Controller
class Pages {

  @Autowired
  Repository repository

  @Autowired
  SearchIndex searchIndex

  @GetMapping("/")
  def index(ModelMap model){
    "redirect:/contacts/list"
  }
  @GetMapping("/contacts/list")
  def contacts_list(ModelMap model){
    def contactsSearch = searchIndex.searchContact("*")
    def contacts = contactsSearch.collect {it.document}

    model.addAttribute("contacts", contacts)
    return "contacts_list"
  }


  @GetMapping("/contacts/new")
  def contacts_new(ModelMap model){
    model.addAttribute("contact", new Contact())
    return "contacts_new"
  }

  @PostMapping("/contacts/add")
  def contact_create(@ModelAttribute("contact") Contact contact){
    def saved_contact = repository.addContact(contact)
    searchIndex.indexContact(saved_contact)
    "redirect:/"
  }

  @GetMapping("/contacts/{contact_id}/show")
  def contact_show(ModelMap modelmap, @PathVariable Long contact_id){
    def contact = repository.getContact(contact_id)
    modelmap.addAttribute("contact", contact)
    "contacts_show"

  }


}
