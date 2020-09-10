package importacion.sucursales.solis


class ImportarExistenciaSolisJob {
   static triggers = {
      cron name:   'impExistenciaSol',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Existencia  Solis ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorExistencia.importar('SOLIS')
    }
}
