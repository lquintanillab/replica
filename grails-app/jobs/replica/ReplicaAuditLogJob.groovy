package replica

class ReplicaAuditLogJob {
       static triggers = {
      cron name:   'auditLog',   startDelay: 20000, cronExpression: '0 0/1 * * * ?'
    }

    def replicaService

    def execute() {
        // execute job
        //replicaService.importar()
        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando por AuditLog ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"
        replicaService.exportar('Producto')
    }
}
