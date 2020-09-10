package exportacion.sucursales.solis

class ExportarExistenciaCincoJob {
    static triggers = {
     cron name:   'expExistenciaSol',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia Solis ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('SOLIS')
    }
}
