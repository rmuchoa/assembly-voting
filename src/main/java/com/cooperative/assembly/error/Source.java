package com.cooperative.assembly.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Source {

    private String pointer;
    private String parameter;

    public Source(String pointer) {
        this.pointer = pointer;
    }

}
