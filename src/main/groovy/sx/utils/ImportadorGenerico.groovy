package sx.utils

import groovy.sql.Sql
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import sx.utils.Periodo



class ImportadorGenerico{



def importar(){

    def dataSource=ctx.getBean('dataSource')

    def entity='inventario'

    def mes=8
    def mesPeriodo = mes-1 
    def year=2018

    def sucursal=Sucursal.findByNombre('TACUBA')
    def urlJdbcSuc ="jdbc:mysql://10.10.1.101:3306/siipapx"

    def userSuc = 'root'
    def passwordSuc = 'sys'

    def periodo=Periodo.getPeriodoEnUnMes(mesPeriodo,year)
    def config = EntityConfiguration.findByTableName(entity)
    def dataSourceSuc=dataSourceResolve(urlJdbcSuc, userSuc, passwordSuc)

    def sqlSuc = new Sql(dataSourceSuc)
    def sqlCen = new Sql(dataSource)
    
    //
    def operacionesSuc = getOperaciones(sqlSuc,config.tableName,null,sucursal.id,null)
    def operacionesCen = getOperaciones(sqlCen,config.tableName,null,sucursal.id,null)

    //
    //def operacionesSuc = getOperaciones(sqlSuc,config.tableName,sucursal.id,periodo,null)
    //def operacionesCen = getOperaciones(sqlCen,config.tableName,sucursal.id,periodo,null)

    //
    //def queryCustom="select a.id from cuenta_por_cobrar c join aplicacion_de_cobro a on (a.cuenta_por_cobrar_id=c.id) where date(a.fecha) between ? and ? and c.sucursal_id= ? "
    //def operacionesSuc = getOperaciones(sqlSuc,config.tableName,periodo,sucursal.id,queryCustom)
    //def operacionesCen = getOperaciones(sqlCen,config.tableName,periodo,sucursal.id,queryCustom)



    def diferencias = getDiferencias(operacionesSuc,operacionesCen)



    println "OperacionesSuc: "+operacionesSuc.size()
    println "OperacionesCen: "+operacionesCen.size()
    println "Diferencias: "+ diferencias.size()

    diferencias.each{diferencia ->
        def operacionSuc= getOperacionId(sqlSuc,config.tableName,diferencia.id)
        def operacionCen= getOperacionId(sqlCen,config.tableName,diferencia.id)
        if(operacionSuc){
            if(!operacionCen){
                println "Importar!!! "
                importarOperacion(dataSource,config.tableName,operacionSuc)    
            }
        }
    }

}





}




