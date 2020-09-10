package exportacion.sucursales.andrade

class ExportarExistenciaAndradeJob {
    static triggers = {
     cron name:   'expExistenciaAnd',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia Andrade ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('ANDRADE')
    }
}
