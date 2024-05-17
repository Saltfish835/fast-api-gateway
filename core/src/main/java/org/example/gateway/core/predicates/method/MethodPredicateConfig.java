package org.example.gateway.core.predicates.method;

import org.example.gateway.common.config.PredicateConfig;

import java.util.List;

public class MethodPredicateConfig extends PredicateConfig {

    public List<String> values;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "MethodPredicateConfig{" +
                "values=" + values +
                ", id='" + id + '\'' +
                '}';
    }
}
