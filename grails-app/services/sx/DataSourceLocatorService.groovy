package sx

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Transactional
class DataSourceLocatorService {

      def dataSourceLocator(String server){
          def serverDs=DataSourceReplica.findByServer(server)
          dataSourceLocatorServer(serverDs)
      }


      def dataSourceLocatorServer( DataSourceReplica serverDs){

          def driverManagerDs=new DriverManagerDataSource()
          driverManagerDs.driverClassName="com.mysql.jdbc.Driver"
          driverManagerDs.url=serverDs.url
          driverManagerDs.username=serverDs.username
          driverManagerDs.password=serverDs.password

          return  driverManagerDs
      }


}
