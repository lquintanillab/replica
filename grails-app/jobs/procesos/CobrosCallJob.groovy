package procesos

class CobrosCallJob {
    
    def  importacionService

    static triggers = {
     cron name:   'cobrosCall',   startDelay: 10000, cronExpression: '0 0 20 * * ?'
     
    }

    def execute() {

        println "************************************************************"
        println "                                                          "
        println "                  Actualizando CobrosCall ${new Date()}  "
        println "                                                          "
        println "************************************************************"


        try{
            println "Se arranco la actualizacion de cobros call acumuladas ${new Date()}"
                importacionService.cobrosDepAndTransfCall()
            println "Se actualizaron  los cobroscall con exito ${new Date()}"
        }catch(Exception e){
          e.printStackTrace()
        }
        
        
    }
}
