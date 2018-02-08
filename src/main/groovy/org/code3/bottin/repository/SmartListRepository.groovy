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
    getAllSmartList: "select name, id from smart_list;",
    insertSmartList: "insert into smart_list (name) values (:name);",
    getSmartListById: "select id, name from smart_list where id = :id;",
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
  }
