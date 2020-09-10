package exportacion.sucursales.callcenter

class ExportarExistenciaCallJob {
    static triggers = {
     cron name:   'expExistenciaCal',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia CallCenter ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('CALLCENTER')
    }
}
