import Foundation

@resultBuilder
public struct SchemaBuilder {
    public static func buildBlock() -> EmptyField {
        EmptyField()
    }
}