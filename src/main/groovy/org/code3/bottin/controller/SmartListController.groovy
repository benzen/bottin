package org.code3.bottin

import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class SmartListController {

  @Autowired
  SmartListRepository smartListRepository

  @Autowired
  ListRepository listRepository

  @Autowired
  ContactRepository contactRepository

  @Autowired
  ExcelExtractor excelExtractor


  @GetMapping("/smartLists/new")
  def newSmartList(ModelMap model){
    model.addAttribute("simpleLists", listRepository.listLists())
    model.addAttribute("smartLists", smartListRepository.getAll())
    model.addAttribute("smartList", new SmartList())
    "smartLists/new"
  }

  @PostMapping("/smartLists/add")
  def createSmartList(@ModelAttribute("smartList") SmartList smartList){
    def savedSmartList = smartListRepository.addSmartList(smartList)
    "redirect:/smartLists/$savedSmartList.id/show"
  }

  @GetMapping("/smartLists/{id}/show")
  def getSmartList(ModelMap model, @PathVariable Long id){
    def smartList = smartListRepository.getSmartListById(id)
    def matchingContacts = contactRepository.getAllBySmartList(smartList)

    model.addAttribute("smartList", smartList)
    model.addAttribute("matchingContacts", matchingContacts)
    model.addAttribute("smartLists", smartListRepository.getAll())
    model.addAttribute("simpleLists", listRepository.listLists())

    "smartLists/show"
  }

  @GetMapping("/smartLists/{id}/edit")
  def editSmartList(ModelMap model, @PathVariable Long id, @RequestParam(value="nbPredicateToAdd", required=false) Integer nbPredicateToAdd, @RequestParam(value="deletePredicate", required=false) Integer deletePredicate){
    def smartList = smartListRepository.getSmartListById(id)

    if(!smartList.predicates){
      smartList.predicates = []
    }
    println "smartList.predicates ${smartList.predicates.size()} nbPredicateToAdd ${nbPredicateToAdd}"
    if(nbPredicateToAdd !=  null){
      nbPredicateToAdd.times {
        smartList.predicates.push(new Predicate())
      }
    }

    if(deletePredicate != null){
      println "smartList.predicates ${smartList.predicates.size()}, deletePredicate ${deletePredicate}"
      smartList.predicates.remove(deletePredicate)
    }

    model.addAttribute("smartList", smartList)
    model.addAttribute("smartLists", smartListRepository.getAll())
    model.addAttribute("simpleLists", listRepository.listLists())
    model.addAttribute("nbPredicateToAdd", nbPredicateToAdd ?: 0)

    "smartLists/edit"
  }

  @PostMapping("/smartLists/{id}/update")
  def updateSmartList(@ModelAttribute("smartList") SmartList smartList, @PathVariable Long id){
    smartListRepository.updateSmartList(id, smartList)
    "redirect:/smartLists/${id}/show"
  }



  @GetMapping("/smartLists/{id}/delete")
  def archive(@PathVariable long id, RedirectAttributes redirectAttributes){
    smartListRepository.archiveSmartList(id)
    redirectAttributes.addAttribute("archivedList", id)
    "redirect:/"
  }

  @GetMapping("/smartLists/{id}/restore")
  def restore(@PathVariable long id, RedirectAttributes redirectAttributes){
    smartListRepository.restoreSmartList(id)
    redirectAttributes.addAttribute("restoredList", id)
    "redirect:/smartLists/$id/show"
  }

  @GetMapping("/smartLists/{id}/extract")
  def extract(@PathVariable long id, HttpServletResponse response){

    def smartList = smartListRepository.getSmartListById(id)
    def contactIds = contactRepository.getIdsBySmartList(smartList)
    def contacts = contactIds.collect { contactId -> contactRepository.getContact(contactId) }

    response.setHeader("Content-disposition", "attachment; filename=extract.xls")
    response.setContentType("application/vnd.ms-excel")

    def out = response.outputStream
    excelExtractor.exportContactsToExcelFile(contacts, out)
    out.flush()
    out.close()

  }


}
