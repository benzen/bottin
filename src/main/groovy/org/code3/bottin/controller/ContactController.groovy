package org.code3.bottin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import org.code3.bottin.List


@Controller
class ContactController {

  @Autowired
  ContactRepository contactRepository

  @Autowired
  ListRepository listRepository

  @Autowired
  SmartListRepository smartLisRepository

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
    model.addAttribute("simpleLists", listRepository.listLists())
    model.addAttribute("smartLists", smartLisRepository.getAll())
    return "contacts/list"
  }

  @PostMapping("/contacts/search")
  def contacts_search(ModelMap model, @RequestParam("query") String query){

    def contactsSearch = searchIndex.searchContact(query)
    def contacts = contactsSearch.collect {it.document}

    model.addAttribute("contacts", contacts)
    model.addAttribute("simpleLists", listRepository.listLists())
    return "contacts/list"
  }

  @GetMapping("/contacts/new")
  def contacts_new(ModelMap model){
    model.addAttribute("contact", new Contact())
    model.addAttribute("simpleLists", listRepository.listLists())
    model.addAttribute("smartLists", smartLisRepository.getAll())
    return "contacts/new"
  }

  @PostMapping("/contacts/add")
  def contact_create(@ModelAttribute("contact") Contact contact){
    def saved_contact = contactRepository.addContact(contact)
    searchIndex.indexContact(saved_contact)
    "redirect:/"
  }

  @GetMapping("/contacts/{contact_id}/show")
  def contact_show(ModelMap model, @PathVariable Long contact_id){
    def contact = contactRepository.getContact(contact_id)
    model.addAttribute("contact", contact)
    model.addAttribute("simpleLists", listRepository.listLists())
    model.addAttribute("smartLists", smartLisRepository.getAll())
    "contacts/show"

  }

  @GetMapping("/contacts/{contact_id}/edit")
  def contact_edit(ModelMap model, @PathVariable Long contact_id, @RequestParam(value="add", required=false) String addField){
    def contact = contactRepository.getContact(contact_id)

    if(addField == "telephone"){
      contact.telephones = contact.telephones ?: []
      contact.telephones.add( new Telephone())
    } else if (addField == "address"){
      contact.addresses = contact.addresses ?: []
      contact.addresses.add(new Address())
    } else if (addField == "email") {
      contact.emails =  contact.emails ?: []
      contact.emails.add(new Email())
    } else if (addField == "relation") {
      contact.relations =  contact.relations ?: []
      contact.relations.add(new Relation())
    }
    model.addAttribute("contact", contact)
    model.addAttribute("simpleLists", listRepository.listLists())
    model.addAttribute("smartLists", smartLisRepository.getAll())

    "contacts/edit"

  }

  @PostMapping("/contacts/{contact_id}/update")
  def contact_update(@PathVariable long contact_id, @ModelAttribute("contact") Contact contact, RedirectAttributes redirectAttributes){
    try{
      contactRepository.updateContact(contact_id, contact)
      searchIndex.unindex(contact_id)
      searchIndex.indexContact(contact)
      "redirect:/contacts/$contact_id/show"
    } catch (Exception e){
      //TODO what about a logger
      e.printStackTrace()
      redirectAttributes.addFlashAttribute("contact", contact)
      redirectAttributes.addFlashAttribute("error", "Fuck men")
      "redirect:/contacts/$contact_id/edit"
    }


  }

  @GetMapping("/contacts/{contact_id}/delete")
  def contact_delete(ModelMap model, @PathVariable long contact_id, RedirectAttributes redirectAttributes){
    try{
      contactRepository.archive_contact(contact_id)
      model.addAttribute("deletedContact", contact_id)
      model.addAttribute("simpleLists", listRepository.listLists())
      model.addAttribute("smartLists", smartLisRepository.getAll())
      searchIndex.unindex(contact_id)
      "redirect:/contacts/list"
    } catch (Exception e) {
      //TODO what about a logger
      e.printStackTrace()
      redirectAttributes.addFlashAttribute("error", "Failed to delete contact")
      "redirect:/contacts/$contact_id/show"
    }

  }

  @GetMapping("/contacts/{contact_id}/restore")
  def contact_restore(@PathVariable long contact_id, RedirectAttributes redirectAttributes){
    try{
      contactRepository.restore_contact(contact_id)
      def contact = contactRepository.getContact(contact_id)
      searchIndex.indexContact(contact)
      "redirect:/contacts/$contact_id/show"
    } catch (Exception e) {
      //TODO what about a logger
      e.printStackTrace()
      redirectAttributes.addFlashAttribute("error", "Failed to restore contact")
      "redirect:/contacts/$contact_id/show"
    }
  }

}
