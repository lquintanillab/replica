package importacion.sucursales.bolivar


class ImportarExistenciaBolivarJob {
   static triggers = {
      cron name:   'impExistenciaBol',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Existencia  Bolivar ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorExistencia.importar('BOLIVAR')
    }
}
