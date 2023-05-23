public struct Schema {
    private var rootField: Field

    init(@SchemaBuilder builder: () -> Field) {
        rootField = builder()
    }
}