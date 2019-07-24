package sx


import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.utils.MonedaUtils


@ToString(includeNames=true,includePackage=false, excludes = ['id, version, partidas'])
@EqualsAndHashCode(includeFields = true,includes = ['id, serie','folio'])
class NotaDeCredito {

    String id

    Cliente cliente

    String nombre

    String serie

    Long folio = 0

    String tipo

    String tipoCartera

    String tipoDeCalculo = 'PORCENTAJE'

    String baseDelCalculo = 'Saldo'

    Date fecha = new Date()

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal tc = 1.0

    BigDecimal importe = 0.0

    BigDecimal impuesto = 0.0

    BigDecimal impuestoTasa = MonedaUtils.IVA

    BigDecimal total = 0.0

    String comentario

    Sucursal sucursal

    BigDecimal descuento = 0.0

    BigDecimal descuento2 = 0.0

    Boolean financiero = false

    String sw2

    Date dateCreated

    Date lastUpdated

    String usoDeCfdi

    String formaDePago

    Long rmd

    String rmdSucursal

    Boolean sinReferencia = false

    String createUser
    String updateUser

    Cobro cobro

    static constraints = {
        serie maxSize: 20
        folio unique:'serie'
        tipoCartera inList: ['CRE','CON', 'CHE', 'JUR', 'COD']
        tipo(nullable:false,inList:['BONIFICACION', 'DEVOLUCION'])
        tc(scale:6,validator:{ val,obj ->
            if(obj.moneda!=MonedaUtils.PESOS && val <= 1.0)
                return "tipoDeCambioError"
            else
                return true
        })
        comentario nullable:true
        sw2 nullable: true
        createUser nullable: true
        updateUser nullable: true
        usoDeCfdi nullable: true, maxSize:3
        formaDePago nullable: true, maxSize: 40
        rmd nullable: true
        rmdSucursal nullable: true, maxSize: 30
        nombre nullable: true
        tipoDeCalculo nullable: true, maxSize: 20
        baseDelCalculo nullable: true, maxSize: 20
    }



    static mapping ={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        fecha type: 'date'
    }

    BigDecimal getTotalMN(String property){
        return "${property}"*tc
    }

    def beforeInsert() {
        updateNombre();
    }

    def updateNombre() {
        if(!this.nombre && this.cliente) {
            this.nombre = this.cliente.nombre;
        }
    }


}
