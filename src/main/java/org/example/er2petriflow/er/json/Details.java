package org.example.er2petriflow.er.json;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Details {

    private String name;
    private Integer id;
    private boolean isUnique;
    private String type;

    public boolean getIsUnique() {
        return isUnique;
    }
    public void setIsUnique(boolean unique) {
        isUnique = unique;
    }
}
