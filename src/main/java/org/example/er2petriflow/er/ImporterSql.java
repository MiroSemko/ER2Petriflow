package org.example.er2petriflow.er;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.example.er2petriflow.er.domain.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ImporterSql {
    private ERDiagram result;
    private Map<String, List<String>> foreignKeyMap;

    public Optional<ERDiagram> convert(InputStream inputStream) {
        String sqls = inputStreamToString(inputStream);

        result = new ERDiagram();
        foreignKeyMap = new HashMap<>();

        mapEntities(sqls);
        mapNaryRelations();
        mapOtherRelations();

        return Optional.of(result);
    }

    public Optional<ERDiagram> convert(String inputStream) {
        String sqls = inputStream;

        result = new ERDiagram();
        foreignKeyMap = new HashMap<>();

        mapEntities(sqls);
        mapNaryRelations();
        mapOtherRelations();

        System.out.println(result.toVisualString());
        return Optional.of(result);
    }


    protected void mapEntities(String sqls) {
        for (String sql : sqls.split(";")) {
            CreateTable table;
            try {
                table = (CreateTable) CCJSqlParserUtil.parse(sql);
//                System.out.println(table);

                String tableName = table.getTable().getName();
                Entity entity = new Entity(tableName);

                addAttributes(table, entity);
                result.addEntity(entity);

                mapForeignKeys(tableName, table.getIndexes());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            }
        }
    }


    protected void mapForeignKeys(String tableName, List<Index> indexes) {
        if (indexes == null)
            return;
//        System.out.println(indexes);
        foreignKeyMap.put(tableName, new ArrayList<>());
        for (Index i : indexes) {
            if (i instanceof ForeignKeyIndex) {
                //map every foreign key of this table
                String referencedTableName = ((ForeignKeyIndex) i).getTable().getName();
                if (Objects.equals(referencedTableName, tableName)) { //skip if the relation is unary
                    continue;
                }
                foreignKeyMap.get(tableName).add(referencedTableName);
            }
        }
//        System.out.println(foreignKeyMap);
    }


    protected boolean isReferencedByForeignKey(String table) {
        for (Map.Entry<String, List<String>> e : foreignKeyMap.entrySet()) {
            String k = e.getKey();
            for (String v : e.getValue()) {
                if (v.equals(table))
                    return true;
            }
        }
        return false;
    }


    protected void mapNaryRelations() {
        for (Map.Entry<String, List<String>> e : foreignKeyMap.entrySet()) {
            String table = e.getKey();
            List<String> foreignKeys = e.getValue();

            if (foreignKeys.size() >= 2 && !isReferencedByForeignKey(table)) {
//                System.out.println("mapping n-ary for: " + table + foreignKeys);

                Relation rel = new Relation(table);
                for (String fk : foreignKeys) {
                    rel.addEntity(result.getEntityByName(fk));
                }

                Entity tableEntity = result.getEntityByName(table);
                if (tableEntity.getAttributes() != null) {
                    for (Attribute a : tableEntity.getAttributes()) {
                        rel.addAttribute(a);
                    }
                }
                result.removeEntity(tableEntity);

                result.addRelation(rel);
            }
        }
    }


    protected void mapOtherRelations() {
        for (Map.Entry<String, List<String>> e : foreignKeyMap.entrySet()) {
            String table = e.getKey();
            List<String> foreignKeys = e.getValue();

            if (foreignKeys.size() < 2 || isReferencedByForeignKey(table)) {
                for (String fk : foreignKeys) {
                    Relation rel = new Relation(fk + "-" + table);
                    rel.addEntity(result.getEntityByName(table));
                    rel.addEntity(result.getEntityByName(fk));
                    result.addRelation(rel);
                }
            }
        }
    }


    protected void addAttributes(CreateTable table, Entity entity) {
        for (ColumnDefinition col : table.getColumnDefinitions()) {
            Attribute attribute = new Attribute(
                    col.getColumnName(),
                    AttributeType.resolve(col.getColDataType().toString().replaceAll("[^a-zA-Z]", ""))
            );

            entity.addAttribute(attribute);
        }
    }


    public String inputStreamToString(InputStream inputStream) {
        return new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
