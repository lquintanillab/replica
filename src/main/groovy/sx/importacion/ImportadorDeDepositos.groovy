package sx.importacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.DataSourceReplica
import sx.EntityConfiguration
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

import groovy.sql.Sql



@Component
class ImportadorDeDepositos{

    @Autowired
    @Qualifier('dataSourceLocatorService')
    def dataSourceLocatorService
          

    def importar(){
      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)
      servers.each{ server ->
        importar(server)
      }
    }

    def importar(DataSourceReplica server){
        def query="select * from audit_log where date_replicated is null  and name='SolicitudDeDeposito'"
        importar(query, server)
    }

    def importar(String query, DataSourceReplica server){

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)
        def datasourceCentral=dataSourceLocatorService.dataSourceLocator(central.server)
        def centralSql=new Sql(datasourceCentral)
        def datasourceOrigen=dataSourceLocatorService.dataSourceLocator(server.server)
        def sql=new Sql(datasourceOrigen)

        def configCobro= EntityConfiguration.findByName("Cobro")
        def configCobroDepo= EntityConfiguration.findByName("CobroDeposito")
        def configCobroTran= EntityConfiguration.findByName("CobroTransferencia")

         sql.rows(query).each{audit ->
            def config= EntityConfiguration.findByName(audit.name)

            if(config){
                def origenSql="select * from $config.tableName where $config.pk=?"

                def solicitud=sql.firstRow(origenSql, [audit.persisted_object_id])

                if(audit.event_name=='DELETE' || solicitud){

                    try {
                        switch (audit.event_name) {

                            case 'INSERT':

                               def cobro=sql.firstRow("select * from cobro where id=?",[solicitud.cobro_id])

                               if(cobro){

                                   def cobroCen=centralSql.firstRow("select * from cobro where id=?",[cobro.id])

                                   if(!cobroCen){
                                        SimpleJdbcInsert insert=new SimpleJdbcInsert(datasourceCentral).withTableName('cobro')
                                        def res=insert.execute(cobro)

                                        if(cobro.forma_de_pago=='TRANSFERENCIA'){
                                            def cobroTranCen=centralSql.firstRow("select * from cobro_transferencia where cobro_id=?",[cobro.id])
                                            if(!cobroTranCen){
                                            def cobroTran=sql.firstRow("select * from cobro_transferencia where cobro_id=?",[cobro.id])
                                                if(cobroTran){
                                                    SimpleJdbcInsert insertTran=new SimpleJdbcInsert(datasourceCentral).withTableName(configCobroTran.tableName)
                                                    def resTran=insertTran.execute(cobroTran)
                                                }
                                            }
                                        }

                                        if(cobro.forma_de_pago=='DEPOSITO'){
                                            def cobroDepCen=centralSql.firstRow("select * from cobro_deposito where cobro_id=?",[cobro.id])
                                            if(!cobroDepCen){
                                            def cobroDep=sql.firstRow("select * from cobro_deposito where cobro_id=?",[cobro.id])
                                                if(cobroDep){
                                                    SimpleJdbcInsert insertDep=new SimpleJdbcInsert(datasourceCentral).withTableName(configCobroDepo.tableName)
                                                    def resDep=insertDep.execute(cobroDep)
                                                }
                                            }
                                        }


                                   }


                               }

                                SimpleJdbcInsert insertSolicitud=new SimpleJdbcInsert(datasourceCentral).withTableName(config.tableName)
                                def res=insertSolicitud.execute(solicitud)

                            break

                            case 'UPDATE':
                                int updated=centralSql.executeUpdate(solicitud, config.updateSql)
                                if(updated){
                                    sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ",audit.id])
                                }else{
                                    sql.execute("UPDATE AUDIT SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR ",audit.id])
                                }
                            break
                        }
                    }catch (Exception e){
                        println(e)
                        String err="Error importando a central: "
                        sql.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=NOW() WHERE ID=? ", [err,audit.id])
                    }
                }
            }
         }
    }
}