@propertyWrapper @dynamicMemberLookup
public struct PInput<Value> {
    public var wrappedValue : Value {
        get { get() }
        nonmutating set { set(newValue) }
    }
    private let get: () -> Value
    private let set: (Value) -> ()

    public var projectedValue : PInput<Value> { self }

    public init(get: @escaping () -> Value, set: @escaping (Value) -> ()) {
        self.get = get
        self.set = { set($0) }
    }

    public subscript<Subject>(dynamicMember keyPath: WritableKeyPath<Value,Subject>) -> PInput<Subject> {
        fatalError()
    }
}

extension PInput : PValueReader where Value : PValueReader {

    public static func constant(_ value: Value.Value) -> Self {
        .init(get: { PCollection.element(value) as! Value },set: { _ in })
    }


    public var value: Value.Value {
        get {
            return self.wrappedValue.value
        }
    }
    public var timestamp: Int64 { 
        get { 
            return self.wrappedValue.timestamp 
        }
    }
    public var window : Window {
        get {
            return self.wrappedValue.window
        }
    }
}