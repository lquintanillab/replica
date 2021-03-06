package sx

import groovy.sql.Sql
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

class ReplicaService {

       def dataSourceLocatorService

    def importar(){
      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)
      servers.each{ server ->
        importar(server)
      }
    }

    def importar(DataSourceReplica server){
        def query="Select * from audit_log where date_replicated is null order by date_created "
        auditImport(query, server)
    }

    def importar(String entityName){
        def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)
        servers.each{ server ->
            importar(entityName,server)
        }
    }

    def importar(String entityName,DataSourceReplica server){
        println "Importando ${entityName} para ${server.server}"
        def query="Select * from audit_log where name= '${entityName}' and date_replicated is null order by date_created "
        auditImport(query, server)
    }

    def importar(String entityName,String sucursal){
        println "Importando ${entityName} para ${sucursal}"
        def server=DataSourceReplica.findByActivaAndCentralAndServer(true,false,sucursal)
        def query="Select * from audit_log where name= '${entityName}' and date_replicated is null order by date_created "
        if(server){
             auditImport(query, server)
        }else{
            println "La conexion con ${sucursal} no esta activa !!! "
        }
       
    }

    def auditImport(String query, DataSourceReplica server ) {
        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)
        def datasourceCentral=dataSourceLocatorService.dataSourceLocator(central.server)
        def centralSql=new Sql(datasourceCentral)
        def datasourceOrigen=dataSourceLocatorService.dataSourceLocator(server.server)
        def sql=new Sql(datasourceOrigen)
        sql.rows(query).each{audit ->
            def config= EntityConfiguration.findByName(audit.name)
            if(config){
                def origenSql="select * from $config.tableName where $config.pk=?"
                def row=sql.firstRow(origenSql, [audit.persisted_object_id])
                println "________________________________________________________"
                println "-------------------------IMPORTANDO----------------------"
                println server.server
                println "________________________________________________________"
                if(audit.event_name=='DELETE' || row){
                    try {
                        switch (audit.event_name) {
                            case 'INSERT':
                                println audit
                                SimpleJdbcInsert insert=new SimpleJdbcInsert(datasourceCentral).withTableName(config.tableName)
                                def res=insert.execute(row)
                                if(res){
                                    sql.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                                }else{
                                    sql.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR",audit.id])
                                }
                                break
                            case 'UPDATE':
                                int updated=centralSql.executeUpdate(row, config.updateSql)
                                if(updated){
                                    sql.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ",audit.id])
                                }else{
                                    sql.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR ",audit.id])
                                }
                                break
                            case 'DELETE':
                                def res=centralSql.firstRow("SELECT *  FROM ${config.tableName} WHERE ${config.pk}=?",[audit.persisted_object_id])
                                if(res){
                                    def rs= centralSql.execute("DELETE FROM ${config.tableName} WHERE ${config.pk}=?",[audit.persisted_object_id])
                                    sql.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ELIMINADO",audit.id])
                                }else{
                                    sql.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REGISTRO NO EXISTENTE EN EL TARGET",audit.id])
                                }
                                break;
                            default:
                                break;
                        }
                        afterImport(audit,server)
                        if(config.name == 'SolicitudDeTraslado' || config.name == 'SolicitudDeTrasladoDet' ){
                            afterImportVales( audit, row, centralSql)
                        }
                        if(config.name == 'Traslado' || config.name == 'TrasladoDet' ){

                            afterImportTraslados(audit,row,server,centralSql )
                        }
                    }
                    catch (DuplicateKeyException dk) {            
                        sql.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])
                    }catch (Exception e){
                        log.error(e)
                        String err="Error importando a central Replica Service: "+ExceptionUtils.getRootCauseMessage(e)
                        sql.execute("UPDATE audit_log SET MESSAGE=?,DATE_REPLICATED=NOW() WHERE ID=? ", [err,audit.id])
                    }
                }
                else{
                }
            }
            else{
            }
        }
    }

    def afterImport(def auditOrigen,def serverOrigen ){
        switch (auditOrigen.name){
            case 'Existencia':
            case 'Cliente':
            case 'DireccionDeEntrega':
            case 'ClienteContactos':
            case 'ComunicacionEmpresa':
                disperseafterImport(auditOrigen,serverOrigen)
                break
            default:

                break
        }

    }

    def disperseafterImport(def auditOrigen,def serverOrigen ){
        def servers=DataSourceReplica.findAllByCentralAndServerNotEqual(false,serverOrigen.server)
        servers.each{ server ->

            def audit=new AuditLog()
            audit.name =auditOrigen.name
            audit.tableName=auditOrigen.table_name
            audit.persistedObjectId = auditOrigen.persisted_object_id
            audit.eventName =auditOrigen.event_name
            audit.source ='CENTRAL'
            audit.target =server.server
            audit.dateCreated = audit.lastUpdated = new Date()

            if(auditOrigen.name == 'Existencia' && server.server == 'CALLCENTER'){

            }

            audit.save(failOnError: true,flush: true)
        }
    }

    def afterImportVales(def auditOrigen,def row,def centralSql){
        def audit=new AuditLog()
        audit.name =auditOrigen.name
        audit.tableName=auditOrigen.table_name
        audit.persistedObjectId = auditOrigen.persisted_object_id
        audit.eventName =auditOrigen.event_name
        audit.source ='CENTRAL'
        audit.dateCreated = audit.lastUpdated = new Date()

        if(auditOrigen.name == 'SolicitudDeTraslado'){
            def sucursal=resolveSucursal(row.sucursal_atiende_id)
            audit.target=sucursal.nombre
        }else{
            def sol=centralSql.firstRow("select * from solicitud_de_traslado where id=?",[row.solicitud_de_traslado_id])
            def sucursal=resolveSucursal(sol.sucursal_atiende_id)
            audit.target=sucursal.nombre
        }
        audit.save(failOnError: true,flush: true)
    }

    def afterImportTraslados(def auditOrigen,def row,def serverOrigen, def centralSql ){
        def sucursal=new Sucursal()
        if(auditOrigen.name== 'Traslado'){
            sucursal=resolveSucursal(row.sucursal_id)
        }else{
            def trd=centralSql.firstRow("select * from traslado where id=?",[row.traslado_id])
            sucursal=resolveSucursal(trd.sucursal_id)
        }
        def audit=new AuditLog()
        audit.name =auditOrigen.name
        audit.tableName=auditOrigen.table_name
        audit.persistedObjectId = auditOrigen.persisted_object_id
        audit.eventName =auditOrigen.event_name
        audit.source ='CENTRAL'
        audit.dateCreated = audit.lastUpdated = new Date()
        audit.target=sucursal.nombre

        audit.save(failOnError: true,flush: true)
    }

    def resolveSucursal(def sucursalId){
        def sucursal=Sucursal.get(sucursalId)
    }

   def exportar(){
      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)
      servers.each{server ->
        exportar(server)
      }
    }

    def exportar(DataSourceReplica server){
     // println "Exportando por Replica Audit para  ${server.server} -- ${new Date()}"
      def query="Select * from audit_log where date_replicated is null and target = ${server.server} "
      auditExport(query,server)
    }

    def exportar(String entityName){
      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)
      servers.each{server ->
        exportar(entityName,server)
      }
    }

     def exportar(String entityName, DataSourceReplica server){
      //println "Exportando por Replica Audit para  ${server.server} -- ${new Date()}"
        println "Exportando ${entityName} para ${server.server}"
      def query="Select * from audit_log where name= '${entityName}' and date_replicated is null and target = '${server.server}' "
      auditExport(query,server)
    }

    def exportar(String entityName, String target){
      
        println "Exportando ${entityName} para ${target}"
         println "________________________________________________________"
                println "-------------------------EXPORTANDO----------------------"
               // println audit
                println "________________________________________________________"
        def server=DataSourceReplica.findByActivaAndCentralAndServer(true, false, target)
        def query="Select * from audit_log where name= '${entityName}' and date_replicated is null and target = '${target}' "
        if(server){
            auditExport(query,server)
        }else{
            println "La conexion con ${target} no esta activa !!!"
        } 
    }

    def auditExport(String query, DataSourceReplica server) {
        
        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)
        def datasourceCentral=dataSourceLocatorService.dataSourceLocator(central.server)
        def sqlCen=new Sql(datasourceCentral)
        def dataSourceSuc=dataSourceLocatorService.dataSourceLocator(server.server)
        def sqlSuc=new Sql(dataSourceSuc)
        sqlCen.rows(query).each { audit ->
            def config= EntityConfiguration.findByName(audit.name)
            if(config){
                def sqlEntity="select * from $config.tableName where $config.pk=?"
                def row=sqlCen.firstRow(sqlEntity, [audit.persisted_object_id])
                if(audit.event_name=='DELETE' || row){
                        try{
                        switch (audit.event_name) {
                            case 'INSERT':
                                SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSourceSuc).withTableName(config.tableName)
                                def res=insert.execute(row)
                                if(res){
                                    sqlCen.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                                }
                                break
                            case 'UPDATE':
                                int updated=sqlSuc.executeUpdate(row, config.updateSql)
                                if(updated) {
                                    sqlCen.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ", audit.id])
                                }else{
                                }
                                break
                            case 'DELETE':
                                    sqlSuc.execute("DELETE FROM ${audit.table_name} WHERE ID=?",[audit.persisted_object_id])
                                        sqlCen.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ELIMINADO: ", audit.id])
                                break;
                            default:
                                break;
                        }
                    }
                    catch (DuplicateKeyException dk) {
                            sqlCen.execute("UPDATE audit_log SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])
                    }
                    catch (Exception e){
                            println "${audit.id} -- ${audit.persisted_object_id}"
                            log.error(e)
                            String err="Error importando a central Replica Service: "+ExceptionUtils.getRootCauseMessage(e)
                            sql.execute("UPDATE audit_log SET MESSAGE=?,DATE_REPLICATED=NOW() WHERE ID=? ", [err,audit.id])
                    }
                }
            }
        }
    }

}
