package sx.importacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier
import groovy.sql.Sql
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

import sx.utils.Periodo



@Component
class VentasAcumuladas{

    @Autowired
    @Qualifier('dataSource')
    def dataSource


    def actualizar() {

        def sql = new Sql(dataSource)

        def fecha = new Date()

        def mes = Periodo.obtenerMes(fecha)
        def anio = Periodo.obtenerYear(fecha)

        if(mes == 0){
            mes = 11
            anio = anio - 1
        }

        def periodo = Periodo.getPeriodoEnUnMes(mes, anio)

        def query = """
        SELECT 
        v.fecha,
        DAYOFMONTH(v.fecha) as dia,	
        ROUND(IFNULL(SUM(CASE WHEN V.TIPO='CON' THEN V.SUBTOTAL*V.TIPO_DE_CAMBIO END),0)/1000) AS venta_mos	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='COD' THEN V.SUBTOTAL*V.TIPO_DE_CAMBIO END),0)/1000) AS venta_cam	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO IN ('COD','CON') THEN V.SUBTOTAL*V.TIPO_DE_CAMBIO END),0)/1000) AS venta_cont	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='CRE' THEN V.SUBTOTAL*V.TIPO_DE_CAMBIO END),0)/1000) AS venta_cre	
        ,ROUND(IFNULL(SUM(V.SUBTOTAL*V.TIPO_DE_CAMBIO),0)/1000) AS total_venta	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='CON' THEN (SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID) END),0)/1000) AS kilos_mos	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='COD' THEN (SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID) END),0)/1000) AS kilos_cam	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO IN ('CON','COD') THEN (SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID) END),0)/1000) AS kilos_cont	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='CRE' THEN (SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID) END),0)/1000) AS kilos_cre	
        ,ROUND(IFNULL(SUM((SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID)),0)/1000) AS total_kilos	
        ,COUNT(*) AS total_facs
        ,(SELECT count(*) FROM venta x join venta_det y on (x.id= y.venta_id) join cuenta_por_cobrar z on (z.id = x.cuenta_por_cobrar_id)	
            WHERE x.TIPO IN ('COD','CON','CRE') AND x.SW2 IS NULL AND z.FECHA = v.fecha) as total_partidas
        FROM VENTA F JOIN cuenta_por_cobrar V ON(F.CUENTA_POR_COBRAR_ID=V.ID) 	
        WHERE V.TIPO IN ('COD','CON','CRE') AND V.SW2 IS NULL AND V.FECHA BETWEEN ? AND ?
        GROUP BY V.FECHA
        ORDER BY V.FECHA
        """

        def query2 = """
        SELECT 
        v.fecha,
        DAYOFMONTH(v.fecha) as dia,	
        ROUND(IFNULL(SUM(CASE WHEN V.TIPO='CON' THEN V.SUBTOTAL*V.TIPO_DE_CAMBIO END),0)/1000) AS venta_mos	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='COD' THEN V.SUBTOTAL*V.TIPO_DE_CAMBIO END),0)/1000) AS venta_cam	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO IN ('COD','CON') THEN V.SUBTOTAL*V.TIPO_DE_CAMBIO END),0)/1000) AS venta_cont	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='CRE' THEN V.SUBTOTAL*V.TIPO_DE_CAMBIO END),0)/1000) AS venta_cre	
        ,ROUND(IFNULL(SUM(V.SUBTOTAL*V.TIPO_DE_CAMBIO),0)/1000) AS total_venta	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='CON' THEN (SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID) END),0)/1000) AS kilos_mos	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='COD' THEN (SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID) END),0)/1000) AS kilos_cam	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO IN ('CON','COD') THEN (SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID) END),0)/1000) AS kilos_cont	
        ,ROUND(IFNULL(SUM(CASE WHEN V.TIPO='CRE' THEN (SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID) END),0)/1000) AS kilos_cre	
        ,ROUND(IFNULL(SUM((SELECT SUM(D.CANTIDAD/(CASE WHEN P.UNIDAD='MIL' THEN 1000 ELSE 1 END)*P.KILOS) FROM VENTA_DET D JOIN producto P ON (P.ID=D.PRODUCTO_ID)WHERE D.VENTA_ID=F.ID)),0)/1000) AS total_kilos	
        ,COUNT(*) AS total_facs
        ,(SELECT count(*) FROM venta x join venta_det y on (x.id= y.venta_id) join cuenta_por_cobrar z on (z.id = x.cuenta_por_cobrar_id)	
            WHERE x.TIPO IN ('COD','CON','CRE') AND x.SW2 IS NULL AND z.FECHA = v.fecha) as total_partidas
        FROM VENTA F JOIN cuenta_por_cobrar V ON(F.CUENTA_POR_COBRAR_ID=V.ID) 	
        WHERE V.TIPO IN ('COD','CON','CRE') AND V.SW2 IS NULL AND V.FECHA = (CURRENT_DATE)
        GROUP BY V.FECHA
        ORDER BY V.FECHA
        """

        def currentLogs = sql.rows(query,[periodo.fechaInicial, periodo.fechaFinal])

        if(currentLogs){
                sql.execute("delete from venta_current_log where fecha between ? and ? ",[periodo.fechaInicial, periodo.fechaFinal])
            
                currentLogs.each{ log ->
                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName('venta_current_log')
                    def res=insert.execute(log)
                }
        }
            
        def log = sql.rows(query2)

        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName('venta_log')
        def res=insert.execute(log)

    }

}
