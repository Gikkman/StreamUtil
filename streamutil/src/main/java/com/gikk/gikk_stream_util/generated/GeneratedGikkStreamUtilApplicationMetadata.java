package com.gikk.gikk_stream_util.generated;

import com.speedment.internal.core.runtime.ApplicationMetadata;
import javax.annotation.Generated;

/**
 * A {@link com.speedment.internal.core.runtime.ApplicationMetadata} class
 * for the {@link com.speedment.config.db.Project} named gikk_stream_util.
 * This class contains the meta data present at code generation time.
 * <p>
 * This file has been automatically generated by Speedment. Any changes made
 * to it will be overwritten.
 * 
 * @author Speedment
 */
@Generated("Speedment")
public class GeneratedGikkStreamUtilApplicationMetadata implements ApplicationMetadata {
    
    private final static StringBuilder METADATA = new StringBuilder();
    
    static {
        METADATA.append("{\n");
        METADATA.append("  \"config\": {\n");
        METADATA.append("    \"expanded\": true,\n");
        METADATA.append("    \"companyName\": \"gikk\",\n");
        METADATA.append("    \"name\": \"gikk_stream_util\",\n");
        METADATA.append("    \"packageLocation\": \"src/main/java/\",\n");
        METADATA.append("    \"dbmses\": [\n");
        METADATA.append("      {\n");
        METADATA.append("        \"expanded\": true,\n");
        METADATA.append("        \"port\": 3306.0,\n");
        METADATA.append("        \"schemas\": [\n");
        METADATA.append("          {\n");
        METADATA.append("            \"tables\": [\n");
        METADATA.append("              {\n");
        METADATA.append("                \"expanded\": true,\n");
        METADATA.append("                \"primaryKeyColumns\": [\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"name\": \"ID\",\n");
        METADATA.append("                    \"ordinalPosition\": 1.0\n");
        METADATA.append("                  }\n");
        METADATA.append("                ],\n");
        METADATA.append("                \"indexes\": [\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"expanded\": false,\n");
        METADATA.append("                    \"unique\": true,\n");
        METADATA.append("                    \"name\": \"PRIMARY\",\n");
        METADATA.append("                    \"indexColumns\": [\n");
        METADATA.append("                      {\n");
        METADATA.append("                        \"orderType\": \"ASC\",\n");
        METADATA.append("                        \"expanded\": true,\n");
        METADATA.append("                        \"name\": \"ID\",\n");
        METADATA.append("                        \"ordinalPosition\": 1.0\n");
        METADATA.append("                      }\n");
        METADATA.append("                    ],\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  },\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"expanded\": false,\n");
        METADATA.append("                    \"unique\": true,\n");
        METADATA.append("                    \"name\": \"USERNAME\",\n");
        METADATA.append("                    \"indexColumns\": [\n");
        METADATA.append("                      {\n");
        METADATA.append("                        \"orderType\": \"ASC\",\n");
        METADATA.append("                        \"expanded\": true,\n");
        METADATA.append("                        \"name\": \"USERNAME\",\n");
        METADATA.append("                        \"ordinalPosition\": 1.0\n");
        METADATA.append("                      }\n");
        METADATA.append("                    ],\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  }\n");
        METADATA.append("                ],\n");
        METADATA.append("                \"columns\": [\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"databaseType\": \"java.lang.Integer\",\n");
        METADATA.append("                    \"typeMapper\": \"com.speedment.config.db.mapper.identity.IntegerIdentityMapper\",\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"nullable\": false,\n");
        METADATA.append("                    \"autoIncrement\": false,\n");
        METADATA.append("                    \"name\": \"ID\",\n");
        METADATA.append("                    \"ordinalPosition\": 1.0,\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  },\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"databaseType\": \"java.lang.String\",\n");
        METADATA.append("                    \"typeMapper\": \"com.speedment.config.db.mapper.identity.StringIdentityMapper\",\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"nullable\": false,\n");
        METADATA.append("                    \"autoIncrement\": false,\n");
        METADATA.append("                    \"name\": \"USERNAME\",\n");
        METADATA.append("                    \"ordinalPosition\": 2.0,\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  },\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"databaseType\": \"java.lang.String\",\n");
        METADATA.append("                    \"typeMapper\": \"com.speedment.config.db.mapper.identity.StringIdentityMapper\",\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"nullable\": false,\n");
        METADATA.append("                    \"autoIncrement\": false,\n");
        METADATA.append("                    \"name\": \"STATUS\",\n");
        METADATA.append("                    \"ordinalPosition\": 3.0,\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  },\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"databaseType\": \"java.lang.Integer\",\n");
        METADATA.append("                    \"typeMapper\": \"com.speedment.config.db.mapper.identity.IntegerIdentityMapper\",\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"nullable\": false,\n");
        METADATA.append("                    \"autoIncrement\": false,\n");
        METADATA.append("                    \"name\": \"TIME_ONLINE\",\n");
        METADATA.append("                    \"ordinalPosition\": 4.0,\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  },\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"databaseType\": \"java.lang.Integer\",\n");
        METADATA.append("                    \"typeMapper\": \"com.speedment.config.db.mapper.identity.IntegerIdentityMapper\",\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"nullable\": false,\n");
        METADATA.append("                    \"name\": \"LINES_WRITTEN\",\n");
        METADATA.append("                    \"ordinalPosition\": 5.0,\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  },\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"databaseType\": \"java.lang.String\",\n");
        METADATA.append("                    \"typeMapper\": \"com.speedment.config.db.mapper.string.TrueFalseStringToBooleanMapper\",\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"nullable\": false,\n");
        METADATA.append("                    \"autoIncrement\": false,\n");
        METADATA.append("                    \"name\": \"IS_TRUSTED\",\n");
        METADATA.append("                    \"ordinalPosition\": 6.0,\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  },\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"databaseType\": \"java.lang.String\",\n");
        METADATA.append("                    \"typeMapper\": \"com.speedment.config.db.mapper.string.TrueFalseStringToBooleanMapper\",\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"nullable\": false,\n");
        METADATA.append("                    \"autoIncrement\": false,\n");
        METADATA.append("                    \"name\": \"IS_FOLLOWER\",\n");
        METADATA.append("                    \"ordinalPosition\": 7.0,\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  },\n");
        METADATA.append("                  {\n");
        METADATA.append("                    \"databaseType\": \"java.lang.String\",\n");
        METADATA.append("                    \"typeMapper\": \"com.speedment.config.db.mapper.string.TrueFalseStringToBooleanMapper\",\n");
        METADATA.append("                    \"expanded\": true,\n");
        METADATA.append("                    \"nullable\": false,\n");
        METADATA.append("                    \"autoIncrement\": false,\n");
        METADATA.append("                    \"name\": \"IS_SUBSCRIBER\",\n");
        METADATA.append("                    \"ordinalPosition\": 8.0,\n");
        METADATA.append("                    \"enabled\": true\n");
        METADATA.append("                  }\n");
        METADATA.append("                ],\n");
        METADATA.append("                \"name\": \"user\",\n");
        METADATA.append("                \"enabled\": true\n");
        METADATA.append("              }\n");
        METADATA.append("            ],\n");
        METADATA.append("            \"expanded\": true,\n");
        METADATA.append("            \"name\": \"gikk_stream_util\",\n");
        METADATA.append("            \"enabled\": true\n");
        METADATA.append("          }\n");
        METADATA.append("        ],\n");
        METADATA.append("        \"name\": \"db0\",\n");
        METADATA.append("        \"typeName\": \"MySQL\",\n");
        METADATA.append("        \"ipAddress\": \"127.0.0.1\",\n");
        METADATA.append("        \"enabled\": true,\n");
        METADATA.append("        \"username\": \"gikkman\"\n");
        METADATA.append("      }\n");
        METADATA.append("    ],\n");
        METADATA.append("    \"enabled\": true\n");
        METADATA.append("  }\n");
        METADATA.append("}\n");
    }
    
    @Override
    public String getMetadata() {
        return METADATA.toString();
    }
}