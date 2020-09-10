package importacion


class ImportarExistenciaJob {
   static triggers = {
      cron name:   'impExistencia',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Existencia ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

       // importadorExistencia.importar()
    }
}
