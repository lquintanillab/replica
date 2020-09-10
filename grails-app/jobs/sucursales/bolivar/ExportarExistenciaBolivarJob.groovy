package exportacion.sucursales.bolivar

class ExportarExistenciaBolivarJob {
    static triggers = {
     cron name:   'expExistenciaBol',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia Bolivar ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('BOLIVAR')
    }
}
