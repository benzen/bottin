package org.code3.bottin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.ModelAttribute


@Controller
class Pages {

  @Autowired
  Repository repository

  @Autowired
  SearchIndex searchIndex

  @GetMapping("/")
  def index(ModelMap model){
    def contactsSearch = searchIndex.searchContact("*")
    model.addAttribute("contacts", contactsSearch)
    return "contacts_list"
  }

  @GetMapping("/contacts/new")
  def contact_new(ModelMap model){
    model.addAttribute("contact", new Contact())
    return "contacts_new"
  }

  @PostMapping("/contacts/add")
  def contact_create(@ModelAttribute("contact") Contact contact){
    repository.addContact(contact)
    "redirect:/"
  }



}
