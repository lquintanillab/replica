package exportacion.sucursales.cinco

class ExportarExistenciaCincoJob {
    static triggers = {
     cron name:   'expExistenciaCf',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def exportadorExistencia

    def execute() {

        println "************************************************"
        println "*                                              *"
        println "*                                              *"
        println "     Exportando Existencia Cinco De Febrero ${new Date()}"
        println "*                                              *"
        println "*                                              *"
        println "************************************************"

        exportadorExistencia.exportar('CF5FEBRERO')
    }
}
