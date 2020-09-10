package importacion.sucursales.andrade


class ImportarExistenciaAndradeJob {
   static triggers = {
      cron name:   'impExistenciaAnd',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando ExistenciaAndrade ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorExistencia.importar('ANDRADE')
    }
}
