package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.DataSourceReplica
import sx.EntityConfiguration
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert



@Component
class ExportadorDeDepositos{

  @Autowired
   @Qualifier('dataSourceLocatorService')
  def dataSourceLocatorService
  @Autowired
  @Qualifier('dataSource')
  def dataSource
  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def exportar(){
 

    def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      servers.each(){server ->

        exportarServer(server)
      }
  }

  def exportar(nombreSuc){

        def server=DataSourceReplica.findByServer(nombreSuc)
       
          exportarServer(server)
  }

  def exportarServer(server){

     // println "Importando Server"+server.server

    def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)

    def sqlSuc=new Sql(dataSourceSuc)
    def sqlCen=new Sql(dataSource)

    def configDepo= EntityConfiguration.findByName("SolicitudDeDeposito")
    def configCobro= EntityConfiguration.findByName("Cobro")
    def configCobroDepo= EntityConfiguration.findByName("CobroDeposito")
    def configCobroTran= EntityConfiguration.findByName("CobroTransferencia")

    def querySolCen="select * from audit_log where date_replicated is null and target=? and name='SolicitudDeDeposito'"

    def audits=sqlCen.rows(querySolCen,[server.server])

    audits.each{audit ->

              def queryDep="select * from solicitud_de_deposito where id=?"
              def deposito=sqlCen.firstRow(queryDep,[audit.persisted_object_id])

                 //   println "Exportando Deposito"+ deposito.id

              if(deposito){

                try{
                  switch(audit.event_name) {
                    case 'UPDATE':

                          def cobroCen=sqlCen.firstRow("select * from cobro where id=?",[deposito.cobro_id])

                          if(cobroCen){

                             // println "Exportando Cobro"+cobro.id

                            def cobroSuc=sqlSuc.firstRow("select * from cobro where id=?",[cobroCen.id])

                            if(!cobroSuc){
                              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSourceSuc).withTableName(configCobro.tableName)
                              def res=insert.execute(cobroCen)
                            }
                            if(cobroCen.forma_de_pago=='TRANSFERENCIA'){
                                def cobroTranSuc=sqlSuc.firstRow("select * from cobro_transferencia where cobro_id=?",[cobroCen.id])
                                if(!cobroTranSuc){
                                  def cobroTranCen=sqlCen.firstRow("select * from cobro_transferencia where cobro_id=?",[cobroCen.id])
                                  if(cobroTranCen){
                                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSourceSuc).withTableName(configCobroTran.tableName)
                                    def res=insert.execute(cobroTranCen)
                                  }
                                }
                            }
                            if(cobroCen.forma_de_pago=='DEPOSITO'){
                              def cobroDepSuc=sqlSuc.firstRow("select * from cobro_deposito where cobro_id=?",[cobroSuc.id])
                              if(!cobroDepSuc){
                                def cobroDepCen=sqlCen.firstRow("select * from cobro_transferencia where cobro_id =?",[cobrCen.id])
                                if(cobroDepCen){
                                  SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSourceSuc).withTableName(configCobroDep.tableName)
                                  def res=insert.execute(cobroDepCen)
                                }
                              }
                            }
                          }

                          int updated=sqlSuc.executeUpdate(deposito, configDepo.updateSql)

                          if(updated) {
                            
                              sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ", audit.id])
                          }
                    break

                    case 'INSERT':

                    break

                    default:
                    break
                  }

               }
               catch (DuplicateKeyException dk) {
                      println dk.getMessage()
                 
                      sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])

                  }catch (Exception e){

                    String err="Error importando a central: "
                      sqlSuc.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=null WHERE ID=? ", [err,audit.id])
                  }

              }
        }
  }

}
