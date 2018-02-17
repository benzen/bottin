package org.code3.bottin

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.stereotype.Service

@Service
class ExcelExtractor {

  def conf = [
    [header: "Firstname", getter: {contact -> contact.firstname}],
    [header: "Lastname", getter: {contact -> contact.lastname}],
    [header: "Organization", getter: {contact -> contact.organization_name}],
    [header: "Email", getter: {contact -> contact?.emails ? contact?.emails[0]?.address : ""  }],
  ]

  private def exportContactsToExcelFile(contacts, out){
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

    contacts.each { member ->
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
