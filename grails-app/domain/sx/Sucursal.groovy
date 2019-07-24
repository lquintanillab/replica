package sx

class Sucursal {

    String id

    Boolean activa= true

    String nombre

    String clave

    Date dateCreated

    Date lastUpdated


    static constraints = {
        clave minSize:1, maxSize:20, unique:true
        nombre unique:true

    }

    static mapping = {
        id generator:'uuid'
    }

}
