package sx

class EntityConfiguration {

    String name

    String tableName

    String pk

    String insertSql

    String updateSql

    String excludeInsertColumns

    String excludeUpdateColumns

    String afterImport

    String afterExport

    Date	dateCreated

    Date	lastUpdated


    static constraints = {
        name nullable:false,maxSize:50,unique:true
        tableName nullable: true
        pk nullable:false,maxSize:50
        insertSql nullable:true,maxSize:5000
        updateSql blank:false,maxSize:5000
        excludeInsertColumns nullable:true,maxSize:250
        excludeUpdateColumns nullable:true,maxSize:250
        afterImport nullable:true,maxSize:5000
        afterExport nullable:true,maxSize:5000
    }
}
