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
    getSmartListById: "select id, name, archived, match_all_predicates from smart_list where id = :id;",
    archiveSmartList: "update smart_list set archived = true where id = :id ;",
    restoreSmartList: "update smart_list set archived = false where id = :id;",
    updateSmartList: "update smart_list set name = :name, match_all_predicates = :match_all_predicates where id = :id;",
    updatePredicate: """
      update predicate
      set
        field = :field,
        operator = :operator,
        value = :value,
        smart_list_id = :smart_list_id,
        index = :index
      where id = :id
    """,
    insertPredicate: """
      insert into predicate
      (smart_list_id, index, field, operator, value)
      values
      (:smart_list_id, :index, :field, :operator, :value)
    """,
    getPredicatesBySmartListId: """
      select
        id,
        field,
        operator,
        value
      from
        predicate
      where
        smart_list_id = :smart_list_id
      order by index;
    """,
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
      def smartList = new SmartList(sql.firstRow(stmt.getSmartListById, [id: id]))
      smartList.predicates = getPredicatesBySmartListId(sql, id)
      smartList
    }
  }
  def updateSmartList(long id, smartList){
    withSql { sql ->
      def params = [
        id: smartList.id,
        name: smartList.name,
        match_all_predicates: smartList.match_all_predicates
      ]
      sql.executeUpdate(stmt.updateSmartList, params)
      def index = 0
      smartList.predicates.each { predicate ->
        if(predicate.id){
          updatePredicate(sql, smartList, index, predicate)
        } else {
          insertPredicate(sql, smartList, index, predicate)
        }
        index++
      }
      index = 0
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

  def insertPredicate(sql, smartList, index, predicate){
    def params = [
      smart_list_id: smartList.id,
      index: index,
      field: predicate.field,
      operator: predicate.operator,
      value: predicate.value
    ]
    sql.executeInsert(stmt.insertPredicate, params)
  }
  def updatePredicate(sql, smartList, index, predicate){
    def params = [
      id: predicate.id,
      smart_list_id: smartList.id,
      index: index,
      field: predicate.field,
      operator: predicate.operator,
      value: predicate.value,
    ]
    sql.executeUpdate(stmt.updatePredicate, params)
  }

  def getPredicatesBySmartListId(sql, smartListId){
    def params = [
      smart_list_id: smartListId
    ]
    sql.rows(stmt.getPredicatesBySmartListId, params).collect({ new Predicate(it)})
  }
}
