package exportacion.sucursales.calle4

class ExportarExistenciaCalle4Job {
    static triggers = {
     cron name:   'expExistenciaCa4',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia Calle 4 ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('CALLE 4')
    }
}
