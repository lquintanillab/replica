package replica

class ReplicaAuditJob {
     static triggers = {
      cron name:   'audit',   startDelay: 20000, cronExpression: '0 0/1 * * * ?'
    }

    def replicaAuditService

    def execute() {
        // execute job
        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Importando ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        replicaAuditService.importar()

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "      Exportando ${new Date()}         "
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        replicaAuditService.exportar()
    }
}
