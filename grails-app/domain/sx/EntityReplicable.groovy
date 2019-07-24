package sx

class EntityReplicable {

    String name

    String tableName

    String descripcion

    String dataBase

    Boolean replicable = true

    Boolean operativa = true

    static constraints = {
        name nullable:true
        tableName nullable: true
        descripcion nullable: true
        dataBase nullable: true
    }
}
