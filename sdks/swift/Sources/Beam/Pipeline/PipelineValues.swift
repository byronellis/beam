public struct PipelineValues : CustomStringConvertible {
    public var description : String {
        "PipelineValues: \(values.count)"
    }

    private var values: [ObjectIdentifier: Any] = [:]

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
}