PATH=".build/debug:${PATH}" protoc --swift_opt=FileNaming=PathToUnderscores --swift_out=./Sources/BeamProto/ \
    -I ../../model/pipeline/src/main/proto/ \
    -I ../../model/fn-execution/src/main/proto/ \
 ../../model/fn-execution/src/main/proto/org/apache/beam/model/fn_execution/v1/*.proto \
 ../../model/pipeline/src/main/proto/org/apache/beam/model/pipeline/v1/*.proto


