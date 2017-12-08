package org.code3.bottin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam


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
    return "contacts/list"
  }

  @PostMapping("/contacts/search")
  def contacts_search(ModelMap model, @RequestParam("query") String query){

    def contactsSearch = searchIndex.searchContact(query)
    def contacts = contactsSearch.collect {it.document}

    model.addAttribute("contacts", contacts)
    return "contacts/list"
  }

  @GetMapping("/contacts/new")
  def contacts_new(ModelMap model){
    model.addAttribute("contact", new Contact())
    return "contacts/new"
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
    "contacts/show"

  }

  @GetMapping("/contacts/{contact_id}/edit")
  def contact_edit(ModelMap modelMap, @PathVariable Long contact_id, @RequestParam(value="add", required=false) String addField){
    def contact = repository.getContact(contact_id)

    if(addField == "telephone"){
      contact.telephones = contact.telephones ?: []
      contact.telephones.add( new Telephone())
    }else if (addField == "address"){
      contact.addresses = contact.addresses ?: []
      contact.addresses.add(new Address())
    }else if (addField == "email") {
      contact.emails =  contact.emails ?: []
      contact.emails.add(new Email())
    }
    modelMap.addAttribute("contact", contact)

    "contacts/edit"

  }

  @PostMapping("/contacts/{contact_id}/update")
  def contact_update(ModelMap modelMap, @PathVariable long contact_id, @ModelAttribute("contact") Contact contact){
    try{
      repository.updateContact(contact_id, contact)
      "redirect:/contacts/$contact_id/show"
    } catch (Exception e){
      //TODO what about a logger
      println e
      modelMap.addAttribute("contact", contact)
      modelMap.addAttribute("error", "Fuck men")
      "contacts/edit"
    }


  }

}
