package org.code3.bottin

import groovy.json.JsonBuilder
import javax.servlet.http.HttpServletResponse
import org.code3.bottin.repository.ContactRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contacts")
public class Api {

  @Autowired
  ContactRepository contactRepository

  @Autowired
  SearchIndex searchIndex

  @RequestMapping(value="", method = RequestMethod.GET, produces="application/json")
  def listContact(){
    def contacts = contactRepository.listContacts()
    new JsonBuilder(contacts).toString()
  }

  @RequestMapping(value="", method = RequestMethod.POST, produces="application/json")
  def addContact(@RequestParam("type_organization") Boolean type_organization,
                 @RequestParam("firstname") String firstname,
                 @RequestParam("lastname") String lastname,
                 @RequestParam("organization_name") String organization_name,
                 @RequestParam("notes") String notes){
     def saved_contact = contactRepository.addContact(type, firstname, lastname, organization_name, notes)
     searchIndex.indexContact(saved_contact)
     new JsonBuilder(saved_contact).toString()
  }

  @RequestMapping(value="search", method= RequestMethod.GET, produces="application/json")
  def searchContact(@RequestParam("query")String query){
    def hits = searchIndex.searchContact(query)
    def formattedHits = hits.collect {it ->
      def doc = it.document
      [id: doc.id, name: doc.type_organization ? doc.organization_name : "$doc.firstname $doc.lastname"]}
    def resp = [success: true, results: formattedHits]
    new JsonBuilder(resp).toString()
  }

}
