package exportacion.sucursales.vertiz

class ExportarExistenciaVertizJob {
    static triggers = {
     cron name:   'expExistenciaVer',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia Vertiz ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('VERTIZ 176')
    }
}
