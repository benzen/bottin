package org.code3.bottin

import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import groovy.json.JsonBuilder

@RestController
@RequestMapping("/api/contacts")
public class Api {

  @Autowired
  Repository repository

  @Autowired
  SearchIndex searchIndex

  @RequestMapping(value="", method = RequestMethod.GET, produces="application/json")
  def listContact(){
    def contacts = repository.listContacts()
    new JsonBuilder(contacts).toString()
  }

  @RequestMapping(value="", method = RequestMethod.POST, produces="application/json")
  def addContact(@RequestParam("type_organization") Boolean type_organization,
                 @RequestParam("firstname") String firstname,
                 @RequestParam("lastname") String lastname,
                 @RequestParam("organization_name") String organization_name,
                 @RequestParam("notes") String notes){
     def saved_contact = repository.addContact(type, firstname, lastname, organization_name, notes)
     searchIndex.indexContact(saved_contact)
     new JsonBuilder(saved_contact).toString()
  }

  @RequestMapping(value="/search", method= RequestMethod.GET, produces="application/json")
  def searchContact(@RequestParam("query")String query){
    def hits = searchIndex.searchContact(query)
    new JsonBuilder(hits).toString()
  }
}
