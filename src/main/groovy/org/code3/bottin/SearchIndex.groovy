package org.code3.bottin

import java.nio.file.FileSystems
import java.nio.file.Path
import javax.annotation.PostConstruct
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.util.Version
import org.apache.lucene.search.FuzzyQuery
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.SimpleFSDirectory
import org.springframework.stereotype.Service
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BooleanClause

@Service
public class SearchIndex {

  def index_path = "_index"
  def index
  def config

  @PostConstruct
  def postConstruct(){
    Path path = FileSystems.getDefault().getPath(index_path)
    def analyzer = new StandardAnalyzer()
    index = SimpleFSDirectory.open(path)
    config = new IndexWriterConfig(analyzer)
  }

  def indexContact(contact){
    def indexWriter = new IndexWriter(index, config)
    def doc = contactToDocument(contact)
    indexWriter.addDocument(doc)
    indexWriter.close()
  }

  def searchContact(searchQuery){
    BooleanQuery.Builder bqb = new BooleanQuery.Builder()
    bqb.add(new FuzzyQuery(new Term("firstname", searchQuery)), BooleanClause.Occur.SHOULD)
    bqb.add(new FuzzyQuery(new Term("lastname", searchQuery)), BooleanClause.Occur.SHOULD)
    bqb.add(new FuzzyQuery(new Term("type_organization", searchQuery)), BooleanClause.Occur.SHOULD)
    bqb.add(new FuzzyQuery(new Term("organization_name", searchQuery)), BooleanClause.Occur.SHOULD)
    bqb.add(new FuzzyQuery(new Term("notes", searchQuery)), BooleanClause.Occur.SHOULD)
    Query q = bqb.build()

    def indexSearcher =  new IndexSearcher(DirectoryReader.open(index))
    def hits = indexSearcher.search(q, 100).scoreDocs
    println "hits $hits.size()"
    hits.collect {[
      score: it.score,
      document: documentToContact(indexSearcher.doc(it.doc))
    ]}
  }

  def documentToContact(doc){
    def contact = new Contact([
      firstname: doc.get("firstname"),
      lastname: doc.get("lastname"),
      type: doc.get("type_organization"),
      notes: doc.get("notes"),
      organization_name: doc.get("organization_name")
    ])
  }
  def contactToDocument(contact) {
    Document doc = new Document()
    doc.add(new TextField("firstname", contact.firstname, Field.Store.YES))
    doc.add(new TextField("lastname", contact.lastname, Field.Store.YES))
    doc.add(new TextField("type_organization", contact.type, Field.Store.YES))
    doc.add(new TextField("organization_name", contact.organization_name, Field.Store.YES))
    doc.add(new TextField("notes", contact.notes, Field.Store.YES))
    doc
  }
}
