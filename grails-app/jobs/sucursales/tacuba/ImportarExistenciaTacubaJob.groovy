package importacion.sucursales.tacuba


class ImportarExistenciaTacubaJob {
   static triggers = {
      cron name:   'impExistenciaTac',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def importadorExistencia

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Existencia  Tacuba ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorExistencia.importar('TACUBA')
    }
}
