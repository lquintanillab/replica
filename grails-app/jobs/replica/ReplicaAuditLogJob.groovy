package replica

class ReplicaAuditLogJob {
       static triggers = {
      cron name:   'auditLog',   startDelay: 20000, cronExpression: '0 0/1 * * * ?'
    }

    def replicaService

    def execute() {
        // execute job
        //replicaService.importar()
        //replicaService.exportar()
    }
}
