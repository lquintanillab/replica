package importacion.sucursales.calle4


class ImportarExistenciaCalle4Job {
   static triggers = {
      cron name:   'impExistenciaCa4',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Existencia  Calle 4 ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorExistencia.importar('CALLE 4')
    }
}
