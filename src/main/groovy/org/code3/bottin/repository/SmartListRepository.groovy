package org.code3.bottin

import groovy.sql.Sql
import groovy.sql.Sql
import java.sql.Statement
import javax.sql.DataSource
import org.code3.bottin.List
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SmartListRepository {

  @Autowired
  DataSource datasource

  def withSql(closure){
    def sql
    try{
      sql =  new Sql(datasource.getConnection())
      closure(sql)
    }
    finally {
      sql?.close()
    }
  }

  def stmt = [
    insertSmartList: "insert into smart_list (name) values (:name);",
    getSmartListById: "select id, name from smart_list where id = :id;"
  ]
  // def getList(id){
  //   withSql { sql ->
  //     def list = getList(sql, id)
  //     list.members = getMembers(sql, id)
  //     list
  //   }
  // }
  // def getList(sql, id){
  //   new List(sql.firstRow(stmt.get_list_by_id, [id: id]))
  // }
  //
  // def getMembers(sql, id){
  //   sql
  //   .rows(stmt.get_members_by_list_id, [id: id])
  //   .collect({ it -> it.contact_id })
  // }
  def addSmartList(smartList){
    withSql { sql ->
      def res = sql.executeInsert(stmt.insertSmartList, smartList)
      new SmartList(sql.firstRow(stmt.getSmartListById, [id: res[0][0]]))

    }
  }
  def getSmartListById(Long id){
    withSql { sql ->
      new SmartList(sql.firstRow(stmt.getSmartListById, [id: id]))
    }
  }
  // def listLists(){
  //   withSql { sql -> sql.rows(stmt.select_all)  }
  // }
  // def addMemberToList(listId, contactId){
  //   withSql { sql ->
  //     sql.executeInsert(stmt.insert_member, [list_id: listId, contact_id: contactId])
  //   }
  // }
  //
  // def removeMemberFromList(listId, contactId){
  //   withSql { sql ->
  //       sql.executeUpdate(stmt.remove_member, [list_id: listId, contact_id: contactId])
  //   }
  // }
  //
  // def archiveList(listId){
  //   withSql { sql ->
  //     sql.executeUpdate(stmt.archive_list, [list_id: listId])
  //   }
  // }
  // def restoreList(listId){
  //   withSql { sql ->
  //     sql.executeUpdate(stmt.restore_list, [list_id: listId])
  //   }
  // }
}
