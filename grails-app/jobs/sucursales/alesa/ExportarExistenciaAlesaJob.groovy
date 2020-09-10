package exportacion.sucursales.alesa

class ExportarExistenciaAlesaJob {
    static triggers = {
     cron name:   'expExistenciaAle',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia Alesa ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('ALESA')
    }
}
