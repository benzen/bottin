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
    getAllSmartList: "select id, name, archived from smart_list where archived is false;",
    insertSmartList: "insert into smart_list (name, archived) values (:name, false);",
    getSmartListById: "select id, name, archived from smart_list where id = :id;",
    archiveSmartList: "update smart_list set archived = true where id = :id ;",
    restoreSmartList: "update smart_list set archived = false where id = :id;",
  ]

  def getAll(){
    withSql { sql -> sql.rows(stmt.getAllSmartList)  }
  }
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
  def archiveSmartList(Long id){
    withSql { sql ->
      sql.executeUpdate(stmt.archiveSmartList, [id: id])
    }
  }
  def restoreSmartList(Long id){
    withSql { sql ->
      sql.executeUpdate(stmt.restoreSmartList, [id: id])
    }
  }
}
