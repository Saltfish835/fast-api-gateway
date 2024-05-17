package org.example.gateway.core.predicates.path;

import org.example.gateway.common.config.PredicateConfig;

import java.util.List;

public class PathPredicateConfig extends PredicateConfig {

    public List<String> values;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "PathPredicateConfig{" +
                "values=" + values +
                ", id='" + id + '\'' +
                '}';
    }
}
