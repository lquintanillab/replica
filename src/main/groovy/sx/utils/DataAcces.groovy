package sx.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier
import groovy.sql.Sql
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

import sx.EntityConfiguration
import sx.Sucursal


@Component
class DataAcces{


    @Autowired
    @Qualifier('dataSource')
    def dataSource


def dataSourceResolve(ip, bd, user, password){

    def urlJdbc='jdbc:mysql://'+ip+"/"+bd
    def driverManagerDs = new DriverManagerDataSource()
   	driverManagerDs.driverClassName = "com.mysql.jdbc.Driver"
    driverManagerDs.url = urlJdbc
    driverManagerDs.username = user
    driverManagerDs.password = password

    return driverManagerDs

}

def dataSourceResolve(urlJdbc, user, password){

    def driverManagerDs = new DriverManagerDataSource()
   	driverManagerDs.driverClassName = "com.mysql.jdbc.Driver"
    driverManagerDs.url = urlJdbc
    driverManagerDs.username = user
    driverManagerDs.password = password

    return driverManagerDs

}

def getSql(dataSource){
    def sql=new Sql(dataSource)
    return sql
}

def getSucursal(sucName){
def sucursal=Sucursal.findByNombre(sucName) 
return sucursal
}

def getConfig(entity){
    def config = EntityConfiguration.findByName(entity)
    return config
}


def getOperaciones(sql,table,periodo,sucursal_id,queryCustom){
    
    def query = "select id from ${table}  "
    
    def params=[]
    if(periodo && !sucursal_id){
        params=[periodo.fechaInicial , periodo.fechaFinal ]
        query = "select id from ${table} where date(date_created) between ? and ?  "
    }
    if(periodo && sucursal_id){
        params=[periodo.fechaInicial , periodo.fechaFinal, sucursal_id ]
        query = "select id from ${table} where date(date_created) between ? and ?  and sucursal_id = ? "
    }
    if(!periodo && sucursal_id){
        params=[sucursal_id ]
        query = "select id  from ${table} where sucursal_id = ? "
    }
    
    if(queryCustom){
        query= queryCustom   
    }
    
    def operaciones = sql.rows(query,params)
    
    return operaciones
    
}

def getOperacionId(sql,table,id){
    def query = "select * from ${table}  where id = ? "
    def operacion = sql.firstRow(query,[id])
    return operacion
}

def getDiferencias(operacionesPrin,operacionesSec){
    
    def diferencias=operacionesPrin.minus(operacionesSec)
    return diferencias
}

def importarOperacion(dataSource,table,operacion){
    
    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName(table)
    def res=insert.execute(operacion)
    
}

def actualizarOperacion(sql,config,operacion){
    
    int updated=sql.executeUpdate(operacion, config.updateSql)
    
}



}