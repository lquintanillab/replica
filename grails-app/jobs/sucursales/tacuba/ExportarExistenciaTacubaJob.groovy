package exportacion.sucursales.tacuba

class ExportarExistenciaTacubaJob {
    static triggers = {
     cron name:   'expExistenciaTac',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia Tac ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('TACUBA')
    }
}
