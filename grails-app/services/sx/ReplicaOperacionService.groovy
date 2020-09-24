package sx

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import javax.swing.text.html.parser.Entity

@Transactional
class ReplicaOperacionService {

    def replicaService

    def exportar(String entity,String id,boolean origen,String event_name,def centralSql,def targetSql,def datasourceTarget){

        def config= EntityConfiguration.findByName(entity)

        if(config){

            def origenSql="select * from $config.tableName where $config.pk=?"

            def row=centralSql.firstRow(origenSql, [id])

            if(row && origen){
                try {
                    switch (event_name) {
                        case 'INSERT':
                          //  println 'Insertando '+row
                            SimpleJdbcInsert insert=new SimpleJdbcInsert(datasourceTarget).withTableName(config.tableName)
                            def res=insert.execute(row)
                          //  println 'Registros Exportados: '+res
                            if(res){
                                centralSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME=? ", ["IMPORTADO",id,event_name])
                            }
                            break
                        case 'UPDATE':
                            int updated=targetSql.executeUpdate(row, config.updateSql)
                            if(updated)
                                centralSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME=? ", ["ACTUALIZADO: ",id,event_name])
                            break
                        default:
                            break;
                    }

                }
                catch (DuplicateKeyException dk) {
                    println dk.getMessage()
                   // println "Registro duplicado ${id}"
                    centralSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? ", ["Registro duplicado",id])

                }catch (Exception e){

                    log.error(e)
                    String err="Error exprtando: "+ExceptionUtils.getRootCauseMessage(e)
                    centralSql.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=null WHERE PERSISTED_OBJECT_ID=? ", [err,id])
                }

            }else if(row && !origen){

                try{
                    switch (event_name) {

                        case 'INSERT':
                           // println 'Insertando '+row
                            SimpleJdbcInsert insert=new SimpleJdbcInsert(datasourceTarget).withTableName(config.tableName)
                            def res=insert.execute(row)
                          //  println 'Registros Exportados: '+res
                            if(res){

                            }
                            break
                        case 'UPDATE':
                            int updated=targetSql.executeUpdate(row, config.updateSql)
                            if(updated)

                            break
                        default:
                            break;
                    }

                } catch (DuplicateKeyException dk) {
                    println dk.getMessage()
                   // println "Registro duplicado ${id}"
                }catch (Exception e){
                    //e.printStackTrace()
                    log.error(e)
                    String err="Error exportando: "+ExceptionUtils.getRootCauseMessage(e)
                    centralSql.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=null WHERE ID=? ", [err,id])
                }

            }
        }

    }

    def importar(String entity,String id,boolean origen,String event_name,def centralSql,def origenSql,def dataSourceCentral){

        def config=EntityConfiguration.findByName(entity)

        if(config) {
            def origenQuery = "select * from $config.tableName where $config.pk=?"
            def row = origenSql.firstRow(origenQuery, [id])

            if (row && origen) {
                  println 'Insertando ' + row.id +"//////////////////////////////////////////////////"
                try{
                    switch (event_name) {

                        case 'INSERT':
                            println '----------------------------------------------------------Insertando ' + row
                            SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSourceCentral).withTableName(config.tableName)

                            def res = insert.execute(row)

                            if (res) {
                              //  println '*************** Registros importados: ' + res
                                origenSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME=? ", ["IMPORTADO", id,event_name])
                            } else {
                             //   println '***************  No se encontraron registros para insertar'
                                origenSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME ", ["REVISAR", id,event_name])
                            }

                            break
                        case 'UPDATE':

                            //println "****************************** Actualizando " + entity + "  ******  " + id
                            int updated = centralSql.executeUpdate(row, config.updateSql)
                            if (updated) {
                              //  println "Se actualizo el registro se va a crear auditLog"
                                origenSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME=? ", ["ACTUALIZADO", id,event_name])
                            }
                            break
                        default:
                            break;

                    }

                    if(config.name == 'SolicitudDeTraslado' || config.name == 'SolicitudDeTrasladoDet' ){
                        println "Importando************************************************************************************* After Importr" +config.name
                        afterImportVales(config, row, centralSql,event_name)
                    }
                    if(config.name == 'Traslado' || config.name == 'TrasladoDet' ){

                        println "Importando************************************************************************************* After Importr" +config.name
                        afterImportTraslados(config,row,centralSql,event_name )
                    }

                }catch (DuplicateKeyException dk) {
                    println dk.getMessage()
                   // println "Registro duplicado ${id}**************************** -- ***************************************${id}"
                    origenSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? and event_name=? ", ["Registro duplicado",id,event_name])

                }catch (Exception e){
                    //e.printStackTrace()
                    log.error(e)
                    String err= "Error importando a central Rplica: "+ExceptionUtils.getRootCauseMessage(e)
                    origenSql.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=NOW() WHERE PERSISTED_OBJECT_ID=? and event_name=?", [err,id,event_name])
                }

            }else  if(row && !origen){
                try{
                    println "Importando**************************************************************************************" +config.name
                    switch (event_name) {

                        case 'INSERT':
                            println '----------------------------------------------------------Insertando COMO NO ORIGN ' + row
                            SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSourceCentral).withTableName(config.tableName)
                            def res = insert.execute(row)

                            if (res) {
                          //      println '*************** Registros importados: ' + res
                                origenSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME=? ", ["IMPORTADO", id,event_name])
                            } else {
                           //     println '***************  No se encontraron registros para insertar'
                                origenSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME ", ["REVISAR", id,event_name])
                            }

                            break
                        case 'UPDATE':

                           // println "****************************** Actualizando " + entity + "  ******  " + id
                            int updated = centralSql.executeUpdate(row, config.updateSql)

                            if (updated) {
                           //     println "Se actualizo el registro se va a crear auditLog"
                                origenSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME=? ", ["ACTUALIZADO", id,event_name])
                            }

                            break
                        default:
                            break;

                    }

                    if(config.name == 'SolicitudDeTraslado' || config.name == 'SolicitudDeTrasladoDet' ){
                        println "Importando************************************************************************************* After Importr" +config.name
                        afterImportVales(config, row, centralSql,event_name)
                    }
                    if(config.name == 'Traslado' || config.name == 'TrasladoDet' ){
                        println "Importando************************************************************************************* After Importr" +config.name
                        afterImportTraslados(config,row,centralSql,event_name )

                    }

                }catch (DuplicateKeyException dk) {
                    println dk.getMessage()
                   // println "Registro duplicado ${id} -- ${id}"
                    origenSql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME=? ", ["Registro duplicado",id,event_name])

                }catch (Exception e){
                    //e.printStackTrace()
                    log.error(e)
                    String err= "Error importando a central REplica operacion: "+ExceptionUtils.getRootCauseMessage(e)
                    origenSql.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=NOW() WHERE PERSISTED_OBJECT_ID=? AND EVENT_NAME=? ", [err,id,event_name])
                }

            }
        }

    }


    def afterImportVales(def config,def row,def centralSql,def event_name){

        def audit=new AuditLog()

        audit.name =config.name
        audit.tableName=config.tableName
        audit.persistedObjectId = row.id
        audit.eventName =event_name
        audit.source ='CENTRAL'
        audit.dateCreated = audit.lastUpdated = new Date()

        if(config.name == 'SolicitudDeTraslado'){
            def sucursal=resolveSucursal(row.sucursal_atiende_id)
            audit.target=sucursal.nombre
        }else{
            def sol=centralSql.firstRow("select * from solicitud_de_traslado where id=?",[row.solicitud_de_traslado_id])
            def sucursal=resolveSucursal(sol.sucursal_atiende_id)
            audit.target=sucursal.nombre
        }


        audit.save(failOnError: true,flush: true)

    }

    def afterImportTraslados(def config,def row, def centralSql, def event_name ){


        println "**********************************************************************After Import de Traslado Ejecutado para "
        def sucursal=new Sucursal()

        if(config.name== 'Traslado'){
            sucursal=resolveSucursal(row.sucursal_id)
        }else{
            def trd=centralSql.firstRow("select * from traslado where id=?",[row.traslado_id])
            sucursal=resolveSucursal(trd.sucursal_id)
        }


        def audit=new AuditLog()

        audit.name =config.name
        audit.tableName=config.tableName
        audit.persistedObjectId = row.id
        audit.eventName =event_name
        audit.source ='CENTRAL'
        audit.dateCreated = audit.lastUpdated = new Date()
        audit.target=sucursal.nombre

        audit.save(failOnError: true,flush: true)

            println "After Import de Traslado Ejecutado para "+ audit.target
    }

    def resolveSucursal(def sucursalId){

        def sucursal=Sucursal.get(sucursalId)

    }



}
