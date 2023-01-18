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


    public Optional<ERDiagram> convert(String sqls) {

        result = new ERDiagram();
        Map<String, Entity> entityMap = new HashMap<>();


        for (String sql : sqls.split(";")) {
            CreateTable table;
            try {
                table = (CreateTable) CCJSqlParserUtil.parse(sql);
                System.out.println(table);

                Entity entity = new Entity(table.getTable().getName());
                //todo better entityMap(same name)
                entityMap.put(entity.getName(), entity);

                addAttributes(table, entity);
                result.addEntity(entity);


                List<Index> indexes = table.getIndexes();
                System.out.println(indexes);

                if(indexes != null) {
                    List<Index> foreignKeys  = indexes.stream()
                            .filter(s -> s instanceof ForeignKeyIndex)
                            .collect(Collectors.toList());
                    if(foreignKeys.size() >= 2){
                        System.out.println(foreignKeys);

                        Relation rel = new Relation(table.getTable().getName());
                        for (Index k : foreignKeys) {
                            String e = ((ForeignKeyIndex) k).getTable().getName();
                            rel.addEntity(entityMap.get(e));
                        }

                        //todo if has attributes add. and delte original entity. THIS CHECK EARLIER IN AN IF STATEMENT?
                        if(entity.getAttributes() != null){
                            for(Attribute a : entity.getAttributes()){
                                rel.addAttribute(a);
                            }
                        }

                        result.addRelation(rel);

                        result.removeEntity(entity);

                    }
                    else {
                        for (Index i : foreignKeys) {
                            Relation rel = new Relation("rel"); //todo name change
                            rel.addEntity(entity);
                            String e = ((ForeignKeyIndex) i).getTable().getName();
                            rel.addEntity(entityMap.get(e));
                            result.addRelation(rel);
                        }
                    }
                }

            } catch (JSQLParserException e) {
                e.printStackTrace();
            }
        }

        return Optional.of(result);
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

}



