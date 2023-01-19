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
    private Map<String, Entity> entityMap;
    private Map<String, List<String>> foreignKeyMap;

    public Optional<ERDiagram> convert(InputStream inputStream) {
        String sqls = inputStreamToString(inputStream);

        result = new ERDiagram();
        entityMap = new HashMap<>();
        foreignKeyMap = new HashMap<>();

        mapEntities(sqls);
        mapNaryRelationships();
        mapOtherRelationships();

        return Optional.of(result);
    }


    protected void mapEntities(String sqls) {
        for (String sql : sqls.split(";")) {
            CreateTable table;
            try {
                table = (CreateTable) CCJSqlParserUtil.parse(sql);
                System.out.println(table);

                String tableName = table.getTable().getName();
                Entity entity = new Entity(tableName);
                //todo better entityMap(same name)
                entityMap.put(entity.getName(), entity);

                addAttributes(table, entity);
                result.addEntity(entity);

                mapForeignKeys(tableName, table.getIndexes());
            } catch (JSQLParserException e) {
                e.printStackTrace();
            }
        }
    }

    protected void mapForeignKeys(String tableName, List<Index> indexes){
        if(indexes == null)
            return;
        System.out.println(indexes);
        foreignKeyMap.put(tableName, new ArrayList<>());
        for (Index i : indexes){
            if (i instanceof ForeignKeyIndex){
                //map every foreign key of this table
                foreignKeyMap.get(tableName).add(((ForeignKeyIndex) i).getTable().getName());
            }
        }
        System.out.println(foreignKeyMap);
    }

    protected boolean isReferencedByForeignKey(String table){
        for (Map.Entry<String, List<String>> e : foreignKeyMap.entrySet()) {
            String k = e.getKey();
            for (String v : e.getValue()) {
                if(v.equals(table))
                    return true;
            }
        }
        return false;
    }

    protected void mapNaryRelationships(){
        for (Map.Entry<String, List<String>> e : foreignKeyMap.entrySet()) {
            String table = e.getKey();
            List<String> foreignKeys = e.getValue();

            if(foreignKeys.size() >= 2 && !isReferencedByForeignKey(table)){ //TODO AND FK REFERENCES TO THIS ENTITY == 0
                System.out.println("mapping n-ary for: " + table + foreignKeys);

                Relation rel = new Relation(table);
                for (String fk : foreignKeys) {
                    rel.addEntity(entityMap.get(fk));
                }

                Entity tableEntity = entityMap.get(table);
                if(tableEntity.getAttributes() != null){
                    for(Attribute a : tableEntity.getAttributes()){
                        rel.addAttribute(a);
                    }
                }
                result.removeEntity(tableEntity);
                entityMap.remove(table);

                result.addRelation(rel);
            }

        }
    }

    protected void mapOtherRelationships(){
        for (Map.Entry<String, List<String>> e : foreignKeyMap.entrySet()) {
            String table = e.getKey();
            List<String> foreignKeys = e.getValue();

            if(foreignKeys.size() < 2 || isReferencedByForeignKey(table)) {
                for (String fk : foreignKeys) {
                    Relation rel = new Relation("rel"); //todo name change
                    rel.addEntity(entityMap.get(table));
                    rel.addEntity(entityMap.get(fk));
                    result.addRelation(rel);
                }
            }
        }
    }


    protected void addAttributes(CreateTable table, Entity entity){
        for(ColumnDefinition col : table.getColumnDefinitions()){
            //todo data type from col.getColDataType() to AttributeType
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



