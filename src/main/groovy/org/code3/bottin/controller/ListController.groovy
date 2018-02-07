package org.code3.bottin

import javax.servlet.http.HttpServletResponse
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.code3.bottin.List
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
class ListController {

  @Autowired
  ListRepository listRepository

  @Autowired
  ContactRepository contactRepository

  @Autowired
  SearchIndex searchIndex

  @GetMapping("/lists/new")
  def lists_new(ModelMap model){
    model.addAttribute("simpleLists", listRepository.listLists())
    model.addAttribute("list", new List())
    "lists/new"
  }

  @PostMapping("/lists/add")
  def lists_create(@ModelAttribute("list") List list){
    def savedList = listRepository.addList(list)
    "redirect:/lists/$savedList.id/show"
  }

  @GetMapping("/lists/{list_id}/show")
  def list_show(ModelMap modelMap, @PathVariable Long list_id){
    def list = listRepository.getList(list_id)
    list.members = list.members.collect({ contactRepository.getContact(it) })
    modelMap.addAttribute("list", list)
    modelMap.addAttribute("simpleLists", listRepository.listLists())
    "lists/show"
  }

  @PostMapping("/lists/{list_id}/search")
  def list_show(ModelMap modelMap, @PathVariable Long list_id, @RequestParam("query") String query, RedirectAttributes redirectAttributes){

    def contactsSearch = searchIndex.searchContact(query)
    def searchResults = contactsSearch.collect {it.document}
    redirectAttributes.addFlashAttribute("searchResults", searchResults)
    "redirect:/lists/$list_id/edit"
  }

  @GetMapping("/lists/{listId}/add/{contactId}")
  def list_add_contact(@PathVariable Long listId, @PathVariable Long contactId){
    listRepository.addMemberToList(listId, contactId)
    "redirect:/lists/$listId/edit"
  }

  @GetMapping("/lists/{listId}/remove/{contactId}")
  def list_rm_contact(@PathVariable Long listId, @PathVariable Long contactId){
    listRepository.removeMemberFromList(listId, contactId)
    "redirect:/lists/$listId/edit"
  }

  @GetMapping("/lists/{list_id}/edit")
  def list_edit(ModelMap modelMap, @PathVariable Long list_id){
    def list = listRepository.getList(list_id)
    list.members = list.members.collect({ contactRepository.getContact(it) })
    modelMap.addAttribute("list", list)
    modelMap.addAttribute("simpleLists", listRepository.listLists())
    "lists/edit"
  }

  @GetMapping("/lists/{listId}/delete")
  def list_delete(@PathVariable Long listId, RedirectAttributes redirectAttributes){
    listRepository.archiveList(listId)
    redirectAttributes.addAttribute("archivedList", listId)
    "redirect:/lists/list"
  }

  @GetMapping("/lists/{listId}/restore")
  def list_restore(@PathVariable Long listId, RedirectAttributes redirectAttributes){
    listRepository.archiveList(listId)
    redirectAttributes.addAttribute("restoredList", listId)
    "redirect:/lists/list"
  }

  @GetMapping("/lists/{listId}/extract")
  def list_extract(@PathVariable long listId, HttpServletResponse response){
    def conf = [
      [header: "Firstname", getter: {contact -> contact.firstname}],
      [header: "Lastname", getter: {contact -> contact.lastname}],
      [header: "Organization", getter: {contact -> contact.organization_name}],
      [header: "Email", getter: {contact -> contact?.emails ? contact?.emails[0]?.address : ""  }],
    ]
    def list = listRepository.getList(listId)
    def members = list.members.collect({contactRepository.getContact(it)})


    response.setHeader("Content-disposition", "attachment; filename=extract.xls")
    response.setContentType("application/vnd.ms-excel")

    def out = response.outputStream
    exportMembersToExcelFile(conf, members, out)
    out.flush()
    out.close()

  }

  private def exportMembersToExcelFile(conf, members, out){
    def wb = new HSSFWorkbook()
    def rowIndex = 0
    def columnIndex = 0

    def sheet = wb.createSheet("Extract")
    def row = sheet.createRow(rowIndex)

    conf.each { col ->
      def cell = row.createCell(columnIndex)
      cell.setCellValue(col.header)
      columnIndex++
    }

    members.each { member ->
      rowIndex++
      columnIndex = 0
      row = sheet.createRow(rowIndex)
      conf.each { col ->
        def cell = row.createCell(columnIndex)
        cell.setCellValue(col.getter(member))
        columnIndex++
      }

    }
    wb.write(out)
  }


}
