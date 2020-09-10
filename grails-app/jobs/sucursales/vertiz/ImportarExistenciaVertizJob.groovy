package importacion.sucursales.vertiz


class ImportarExistenciaVertizJob {
   static triggers = {
      cron name:   'impExistenciaVer',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Existencia  Vertiz ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorExistencia.importar('VERTIZ 176')
    }
}
