package org.code3.bottin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class SmartListController {

  @Autowired
  SmartListRepository smartLisRepository

  @Autowired
  ListRepository listRepository

  @GetMapping("/smartLists/new")
  def newSmartList(ModelMap model){
    model.addAttribute("simpleLists", listRepository.listLists())
    model.addAttribute("smartLists", smartLisRepository.getAll())
    model.addAttribute("smartList", new SmartList())
    "smartLists/new"
  }

  @PostMapping("/smartLists/add")
  def createSmartList(@ModelAttribute("smartList") SmartList smartList){
    def savedSmartList = smartLisRepository.addSmartList(smartList)
    "redirect:/smartLists/$savedSmartList.id/show"
  }

  @GetMapping("/smartLists/{id}/show")
  def getSmartList(ModelMap model, @PathVariable Long id){
    def smartList = smartLisRepository.getSmartListById(id)

    model.addAttribute("smartList", smartList)
    model.addAttribute("matchingContacts", [])
    model.addAttribute("smartLists", smartLisRepository.getAll())
    model.addAttribute("simpleLists", listRepository.listLists())

    "smartLists/show"
  }

  @GetMapping("/smartLists/{id}/delete")
  def archive(@PathVariable long id, RedirectAttributes redirectAttributes){
    smartLisRepository.archiveSmartList(id)
    redirectAttributes.addAttribute("archivedList", id)
    "redirect:/"
  }

  @GetMapping("/smartLists/{id}/restore")
  def restore(@PathVariable long id, RedirectAttributes redirectAttributes){
    smartLisRepository.restoreSmartList(id)
    redirectAttributes.addAttribute("restoredList", id)
    "redirect:/smartLists/$id/show"
  }

}
