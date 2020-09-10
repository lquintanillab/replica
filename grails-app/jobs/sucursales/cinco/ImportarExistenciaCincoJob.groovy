package importacion.sucursales.cinco


class ImportarExistenciaCalle4Job {
   static triggers = {
      cron name:   'impExistenciaCf',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Existencia  Cinco de Febrero ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorExistencia.importar('CF5FEBRERO')
    }
}
