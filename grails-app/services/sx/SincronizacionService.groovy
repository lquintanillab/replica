package sx

import groovy.sql.Sql
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert


class SincronizacionService {

    def dataSourceLocatorService

    def replicaClientesCredito1(){
        def clientes=AuditLog.findAllByNameAndTargetAndDateReplicated('ClienteCredito','Central',null)

        clientes.each{cl->

        //    println "Cliente   "+cl.persistedObjectId

            def sucursales=Sucursal.findAllByActiva(true)

            sucursales.each{ sucursal ->
    //           println sucursal.nombre

                AuditLog audit=new AuditLog()

                audit.dateCreated=new Date()
                audit.lastUpdated=new Date()
                audit.name=cl.name
                audit.tableName=cl.tableName
                audit.source='CENTRAL'
                audit.target=sucursal.nombre
                audit.persistedObjectId=cl.persistedObjectId
                audit.eventName=cl.eventName
                audit.save(failOnerror:true , flush:true)
            }

            cl.dateReplicated=new Date()
            cl.save(failOnerror:true , flush:true)

        }

    }


    
    def replicaClientesCredito(){
        def clientes=AuditLog.where{target == 'CENTRAL' && dateReplicated == null && (name == 'ClienteCredito')}.findAll()

        clientes.each{cl->

        //    println "Cliente   "+cl.persistedObjectId

            def sucursales=Sucursal.findAllByActiva(true)

            sucursales.each{ sucursal ->
            println sucursal.nombre

                AuditLog audit=new AuditLog()

                audit.dateCreated=new Date()
                audit.lastUpdated=new Date()
                audit.name=cl.name
                audit.tableName=cl.tableName
                audit.source='CENTRAL'
                audit.target=sucursal.nombre
                audit.persistedObjectId=cl.persistedObjectId
                audit.eventName=cl.eventName
                audit.save(failOnerror:true , flush:true)
            }

            cl.dateReplicated=new Date()
            cl.save(failOnerror:true , flush:true)

        }

    }

    def depuraReplicaOficinas(){

        def audits= AuditLog.where{target == 'OFICINAS' && dateReplicated == null && (name != 'ClienteCredito' )}

        audits.each{audit ->
            audit.dateReplicated=new Date()
            audit.save(failOnerror:true , flush:true)
        }

    }

    def arreglaDestinoCompras(){

        def audits= AuditLog.where{(name=='Compra' || name=='CompraDet') && target=='CENTRAL' && source!='OFICINAS'}

        audits.each{audit ->
            audit.target=audit.source
            audit.save(failOnerror:true , flush:true)
        }

        def auditsCentral=AuditLog.where{(name=='Compra' || name=='CompraDet') && target=='CENTRAL' && source=='OFICINAS' && dateReplicated== null}

        auditsCentral.each{au ->
            def sucursales=Sucursal.findAllByActiva(true)

            sucursales.each{ sucursal ->
  //              println sucursal.nombre

                AuditLog audit=new AuditLog()

                audit.dateCreated=new Date()
                audit.lastUpdated=new Date()
                audit.name=au.name
                audit.tableName=au.tableName
                audit.source='CENTRAL'
                audit.target=sucursal.nombre
                audit.persistedObjectId=au.persistedObjectId
                audit.eventName=au.eventName
                audit.save(failOnerror:true , flush:true)
            }
            au.dateReplicated=new Date()
            au.save(failOnerror:true , flush:true)


        }

    }






}
