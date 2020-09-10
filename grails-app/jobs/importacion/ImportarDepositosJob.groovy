package importacion


class ImportarDepositosJob {
   static triggers = {
      cron name:   'impDepositos',   startDelay: 20000, cronExpression: '0 0/2 * * * ?'
    }

    def importadorDeDepositos

    def execute() {
      
       println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando Depositos!!!!!! ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        importadorDeDepositos.importar()
    }
}
