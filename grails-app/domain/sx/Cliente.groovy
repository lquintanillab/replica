package sx

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@ToString(includes = 'nombre,clave',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='nombre,rfc')
class Cliente {

    static  auditable=true

    String	id

    String clave

    Boolean	activo	 = true

    String	rfc

    String	nombre

    String email

    Boolean	permiteCheque	 = false

    BigDecimal	chequeDevuelto	 = 0

    Boolean	juridico	 = false

    Long	folioRFC	 = 1

    Long	formaDePago	 = 1

    Long	sw2

    Sucursal	sucursal

    Direccion direccion

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    ClienteCredito credito

    static constraints = {
        rfc maxSize:13
        sw2 nullable:true
        dateCreated nullable:true
        lastUpdated nullable:true
        sucursal nullable: true
        direccion nullable: true
        email nullable: true
        credito nullable: true
        createUser nullable: true
        updateUser nullable: true
    }

    static hasOne = [credito: ClienteCredito]

    static embedded = ['direccion']

    static mapping={
        id generator:'uuid'
        medios cascade: "all-delete-orphan"
    }

    String toString() {
        "${nombre} (${clave})"
    }

}
