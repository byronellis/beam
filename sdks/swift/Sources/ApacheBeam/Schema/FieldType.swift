/// FieldType is essentially a richer form of Coder and fortunately shares a lot of the same encoding so we can reuse it
public indirect enum FieldType {
    case unspecified

    case byte, int16, int32, int64, float, double, string, datetime, boolean, bytes
    case decimal(Int, Int)

    case logical(String, Schema)
    case row(Schema)

    case nullable(FieldType)
    case array(FieldType)
    case repeated(FieldType)
    case map(FieldType, FieldType)

    public var baseType: FieldType {
        switch self {
        case let .nullable(baseType):
            baseType
        case let .array(baseType):
            baseType
        case let .repeated(baseType):
            baseType
        default:
            self
        }
    }
}
