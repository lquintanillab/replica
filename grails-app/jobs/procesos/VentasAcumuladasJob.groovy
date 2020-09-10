package procesos

class VentasAcumuladasJob {
    
    def  ventasAcumuladas

    static triggers = {
     cron name:   'ventas',   startDelay: 10000, cronExpression: '0 40 23 * * ?'
     
    }

    def execute() {

        println "************************************************************"
        println "                                                          "
        println "                  Actualizando Ventas Acumuladas ${new Date()}  "
        println "                                                          "
        println "************************************************************"


        try{
            println "Se arranco la actualizacion de ventas acumuladas ${new Date()}"
            ventasAcumuladas.actualizar()
            println "Se actualizaron  los saldos con exito ${new Date()}"
        }catch(Exception e){
          e.printStackTrace()
        }
        
        
    }
}
