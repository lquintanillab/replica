package sx

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import sx.utils.Periodo

@Transactional
class ImportacionService {

    def dataSource
 
    DataSourceLocatorService dataSourceLocatorService


    def cobrosDepAndTransfCall() {
        
        def dataSources = DataSourceReplica.findAll("from DataSourceReplica where activa is true and sucursal is true and central is false")
        def sqlCen = new Sql(dataSource)

        def queryCen = """
                            SELECT c.id,c.forma_de_pago FROM cobro c left join cobro_deposito d on (c.id = d.cobro_id)
                            where c.forma_de_pago like 'DEPOSITO%%' and d.id is null
                            AND c.fecha>='2020/08/01'
                            union
                            SELECT c.id,c.forma_de_pago FROM cobro c left join cobro_transferencia d on (c.id = d.cobro_id)
                            where c.forma_de_pago = 'TRANSFERENCIA' and d.id is null
                            AND c.fecha>='2020/08/01'
                        """

        def cobrosCen = sqlCen.rows(queryCen)

        dataSources.each{

            def sucursal = Sucursal.findByNombre(it.server)
            println sucursal.nombre
            def dataSourceSuc = dataSourceLocatorService.dataSourceLocator(it.server)
            def sqlSuc = new Sql(dataSourceSuc)
            def queryDepSuc = "select * from cobro_deposito where cobro_id = ?"
            def tableName = "cobro_deposito"

            cobrosCen.each{
                if(it.forma_de_pago =='TRANSFERENCIA'){
                    queryDepSuc = "select * from cobro_transferencia where cobro_id = ?"
                    tableName ='cobro_transferencia'
                }

                def cobroSuc = sqlSuc.firstRow(queryDepSuc,[it.id])
                if(cobroSuc){
                    println it.forma_de_pago
                    println cobroSuc
                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName(tableName)
                    def res=insert.execute(cobroSuc)
                }
            }
        }
    }
}
