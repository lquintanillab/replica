package importacion.sucursales.alesa


class ImportarExistenciaAlesaJob {
   static triggers = {
      cron name:   'impExistenciaAle',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Existencia Alesa ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorExistencia.importar('ALESA')
    }
}
