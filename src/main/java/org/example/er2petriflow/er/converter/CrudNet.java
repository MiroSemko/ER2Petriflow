package org.example.er2petriflow.er.converter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.er2petriflow.generated.petriflow.Place;
import org.example.er2petriflow.generated.petriflow.Transition;

@Getter
@AllArgsConstructor
public class CrudNet {

    private Transition create;
    private Transition read;
    private Transition update;
    private Transition delete;

    private Transition layout;

    private Place created;

}
