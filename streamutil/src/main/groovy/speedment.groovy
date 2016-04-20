import com.speedment.config.parameters.*

name = "gikk_stream_util";
packageLocation = "src/main/java";
packageName = "com.company.speedment.test";
enabled = true;
expanded = true;
dbms {
    ipAddress = "127.0.0.1";
    name = "db0";
    port = 3306;
    typeName = "MySQL";
    username = "gikkman";
    enabled = true;
    expanded = true;
    schema {
        columnCompressionType = ColumnCompressionType.NONE;
        fieldStorageType = FieldStorageType.WRAPPER;
        name = "gikk_stream_util";
        schemaName = "gikk_stream_util";
        storageEngineType = StorageEngineType.ON_HEAP;
        defaultSchema = false;
        enabled = true;
        expanded = true;
        table {
            columnCompressionType = ColumnCompressionType.INHERIT;
            fieldStorageType = FieldStorageType.INHERIT;
            name = "user";
            storageEngineType = StorageEngineType.INHERIT;
            tableName = "user";
            enabled = true;
            expanded = true;
            column {
                columnCompressionType = ColumnCompressionType.INHERIT;
                databaseType = java.lang.Integer.class;
                fieldStorageType = FieldStorageType.INHERIT;
                name = "ID";
                typeMapper = com.speedment.internal.core.config.mapper.identity.IntegerIdentityMapper.class;
                autoincrement = true;
                enabled = true;
                expanded = true;
                nullable = false;
            }
            column {
                columnCompressionType = ColumnCompressionType.INHERIT;
                databaseType = java.lang.String.class;
                fieldStorageType = FieldStorageType.INHERIT;
                name = "USERNAME";
                typeMapper = com.speedment.internal.core.config.mapper.identity.StringIdentityMapper.class;
                autoincrement = false;
                enabled = true;
                expanded = true;
                nullable = false;
            }
            column {
                columnCompressionType = ColumnCompressionType.INHERIT;
                databaseType = java.lang.String.class;
                fieldStorageType = FieldStorageType.INHERIT;
                name = "STATUS";
                typeMapper = com.speedment.internal.core.config.mapper.identity.StringIdentityMapper.class;
                autoincrement = false;
                enabled = true;
                expanded = true;
                nullable = false;
            }
            column {
                columnCompressionType = ColumnCompressionType.INHERIT;
                databaseType = java.lang.Integer.class;
                fieldStorageType = FieldStorageType.INHERIT;
                name = "TIME_ONLINE";
                typeMapper = com.speedment.internal.core.config.mapper.identity.IntegerIdentityMapper.class;
                autoincrement = false;
                enabled = true;
                expanded = true;
                nullable = false;
            }
            column {
                columnCompressionType = ColumnCompressionType.INHERIT;
                databaseType = java.lang.Integer.class;
                fieldStorageType = FieldStorageType.INHERIT;
                name = "LINES_WRITTEN";
                typeMapper = com.speedment.internal.core.config.mapper.identity.IntegerIdentityMapper.class;
                autoincrement = false;
                enabled = true;
                expanded = true;
                nullable = false;
            }
            column {
                columnCompressionType = ColumnCompressionType.INHERIT;
                databaseType = java.lang.String.class;
                fieldStorageType = FieldStorageType.INHERIT;
                name = "IS_TRUSTED";
                typeMapper = com.speedment.internal.core.config.mapper.string.TrueFalseStringToBooleanMapper.class;
                autoincrement = false;
                enabled = true;
                expanded = true;
                nullable = false;
            }
            column {
                columnCompressionType = ColumnCompressionType.INHERIT;
                databaseType = java.lang.String.class;
                fieldStorageType = FieldStorageType.INHERIT;
                name = "IS_FOLLOWER";
                typeMapper = com.speedment.internal.core.config.mapper.string.TrueFalseStringToBooleanMapper.class;
                autoincrement = false;
                enabled = true;
                expanded = true;
                nullable = false;
            }
            column {
                columnCompressionType = ColumnCompressionType.INHERIT;
                databaseType = java.lang.String.class;
                fieldStorageType = FieldStorageType.INHERIT;
                name = "IS_SUBSCRIBER";
                typeMapper = com.speedment.internal.core.config.mapper.string.TrueFalseStringToBooleanMapper.class;
                autoincrement = false;
                enabled = true;
                expanded = true;
                nullable = false;
            }
            primaryKeyColumn {
                name = "ID";
                enabled = true;
                expanded = true;
            }
            index {
                name = "PRIMARY";
                enabled = true;
                expanded = true;
                unique = true;
                indexColumn {
                    name = "ID";
                    orderType = OrderType.ASC;
                    enabled = true;
                    expanded = true;
                }
            }
            index {
                name = "USERNAME";
                enabled = true;
                expanded = true;
                unique = true;
                indexColumn {
                    name = "USERNAME";
                    orderType = OrderType.ASC;
                    enabled = true;
                    expanded = true;
                }
            }
        }
    }
}