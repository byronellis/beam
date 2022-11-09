package org.apache.beam.sdk.extensions.yaml;

import com.google.auto.value.AutoValue;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.*;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;

@AutoValue
@AutoValue.CopyAnnotations
public abstract class YAMLTransform extends PTransform<PInput, PCollection<Row>> {

     abstract @Nullable
     Map<String,Object> map();

    @Override
    public PCollection<Row> expand(PInput input) {

        //We expand the main pipeline by default
        if(map().containsKey("main") && map().get("main") instanceof ArrayList) {

        }

        return null;
    }

    public static YAMLTransform yaml(String yaml) {
        Yaml y = new Yaml();
        return builder().setMap(y.load(yaml)).build();
    }



    abstract Builder toBuilder();

    static Builder builder() {
        return new AutoValue_YAMLTransform.Builder();
    }

    @AutoValue.Builder
    @AutoValue.CopyAnnotations
    public abstract static class Builder {

        public abstract Builder setMap(Map<String,Object> yaml);

        public abstract YAMLTransform build();
    }
}
