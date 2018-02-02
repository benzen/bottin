package org.code3.bottin.repository

import groovy.sql.Sql
import groovy.sql.Sql
import java.sql.Statement
import javax.sql.DataSource
import org.code3.bottin.List
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ListRepository {

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
    insert_list: "insert into list (name) values (:name);",
    get_list_by_id: "select name, id from list where id = :id ;",
    get_members_by_list_id : "select list_id, contact_id from list_members where list_id = :id;",
    update_list_by_id: "update list set name = :name where list_id =:id;",
    update_members: "update list_members contact_id = :contact_id where id = :id;",
    insert_member: "insert into list_members (list_id, contact_id ) values (:list_id, :contact_id)",
    select_all: "select id, name from list;",

  ]
  def getListById(id){
    withSql { sql ->
      def list = getList (sql, id)
      list.members = getMembers(sql, id)
      list
    }
  }
  def getList(sql, id){
    new List(sql.firstRow(stmt.get_list_by_id, [id: id]))
  }

  def getMembers(sql, id){
    sql
    .rows(stmt.get_members_by_list_id, [id: id])
    .collect({ it -> it.contact_id })
  }
  def addList(list){
    withSql { sql ->
      def res = sql.executeInsert(stmt.insert_list, list)
      new List(sql.firstRow(stmt.get_list_by_id, [id: res[0][0]]))
    }
  }
  def listLists(){
    withSql { sql -> sql.rows(stmt.select_all)  }
  }
  def addMemberToList(listId, contactId){
    withSql { sql ->
      sql.executeInsert(stmt.insert_member, [list_id: listId, contact_id: contactId])
    }
  }
}
