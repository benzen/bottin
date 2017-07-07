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

  @RequestMapping(value="", method = RequestMethod.GET, produces="application/json")
  def listContact(){
    def contacts = repository.listContacts()
    new JsonBuilder(contacts).toString()
  }

  @RequestMapping(value="", method = RequestMethod.POST, produces="application/json")
  def addContact(@RequestParam("type") String type,
                 @RequestParam("firstname") String firstname,
                 @RequestParam("lastname") String lastname,
                 @RequestParam("organization_name") String organization_name,
                 @RequestParam("notes") String notes){
     def saved_contact = repository.addContact(type, firstname, lastname, organization_name, notes)
     new JsonBuilder(saved_contact).toString()
  }
}
