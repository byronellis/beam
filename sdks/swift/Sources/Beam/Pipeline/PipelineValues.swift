protocol PipelineValueStorage {
    var getter: (()-> Any)? { get set }
    var anyIntialValue: Any { get }
}
protocol WritablePipelineValueStorage {
    var setter: ((Any) -> ())? { get set }
}
public struct PipelineValues : CustomStringConvertible {
    public var description : String {
        "PipelineValues: \(values.count)"
    }

    private var values: [ObjectIdentifier: Any] = [:]

    public init() {}

    public subscript<K>(key: K.Type) -> K.Value where K : PipelineKey {
        get {
            if let val = values[ObjectIdentifier(key)] as? K.Value {
                return val
            }
            return K.defaultValue
        }
        set {
            values[ObjectIdentifier(key)] = newValue
        }
    }

    public mutating func merge(_ other: Self?) {
        if let other = other {
            values.merge(other.values) { _, new in new }
        }
    }

    public func merging(_ other: Self?) -> Self {
        var merged = self
        merged.merge(other)
        return merged
    }

}