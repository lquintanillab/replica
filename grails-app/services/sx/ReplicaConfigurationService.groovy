package sx

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

@Transactional
class ReplicaConfigurationService {
  def dataSource


  def resolverPk(def sql,String table,String db) {
      def pk=sql.firstRow("""
    SELECT COLUMN_NAME FROM information_schema.COLUMNS
    WHERE (TABLE_SCHEMA = ?)
    AND (TABLE_NAME = ?)
    AND (COLUMN_KEY = 'PRI');
  """,[db,table]).COLUMN_NAME
      return pk
  }

  def resolverUpdateQuery(def sql,String table,String db) {
      def pk=sql.firstRow("""
    SELECT COLUMN_NAME FROM information_schema.COLUMNS
    WHERE (TABLE_SCHEMA = ?)
    AND (TABLE_NAME = ?)
    AND (COLUMN_KEY = 'PRI');
  """,[db,table]).COLUMN_NAME


      def columns=sql.rows("""
    SELECT COLUMN_NAME FROM information_schema.COLUMNS
    WHERE (TABLE_SCHEMA = ?)
    AND (TABLE_NAME = ?)
    AND (COLUMN_KEY <> 'PRI');
  """,[db,table])

      def res="UPDATE $table SET "
      res+=columns.collect {it.COLUMN_NAME+"=:"+it.COLUMN_NAME}.join(",")

      res+=" WHERE $pk=:$pk"

      return res;
  }

  def buscarTablas(def dataBase){

      def tables=sql.rows("""SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE (TABLE_SCHEMA = ?)""",[dataBase])

  }


  def instalarConfiguraciones(){

      def entitys=EntityReplicable.findAll()

      entitys.each {entity ->
          println "Instalando configuraciones para ${entity.name}"
          instalarConfiguracionesEntity(entity)

      }

  }

  def  instalarConfiguraciones(String entityStr){
    def entity=EntityReplicable.findByName(entityStr)
    instalarConfiguracionesEntity(entity)
  }

  def instalarConfiguracionesEntity(EntityReplicable entity){

      def config=EntityConfiguration.findByName(entity.name)

      if(!config){
          try{
              
              crearConfiguracion(entity.name,entity.tableName,entity.dataBase)
          }catch (e){
              e.printStackTrace()

          }

      }

  }

  def crearConfiguracion(def name,def table,def db){
      SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName(table)
      def sql=new Sql(dataSource)
      insert.compile()
      def config=new EntityConfiguration(name:name,tableName:table)
      config.updateSql=resolverUpdateQuery(sql,config.tableName,db)
      config.insertSql=insert.getInsertString()
      config.pk=resolverPk(sql, config.tableName,db)

      config.save(failOnError:true)
  }

}
