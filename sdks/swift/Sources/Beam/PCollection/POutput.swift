@propertyWrapper @dynamicMemberLookup
public struct POutput<Value> {
    public var wrappedValue : Value {
        get { get() }
        nonmutating set { set(newValue) }
    }
    private let get: () -> Value
    private let set: (Value) -> ()

    public var projectedValue : POutput<Value> { self }

    public init(get: @escaping () -> Value, set: @escaping (Value) -> ()) {
        self.get = get
        self.set = { set($0) }
    }

    public subscript<Subject>(dynamicMember keyPath: WritableKeyPath<Value,Subject>) -> PInput<Subject> {
        fatalError()
    }
}

extension POutput : PValueWriter where Value : PValueWriter {

    public static func constant() -> Self {
        .init(get: { PCollection<Value.Value>.consume({ _,_,_ in }) as! Value },set: { _ in })
    }

    public func output(_ value: Value.Value,timestamp: Int64,window: Window) {
        self.wrappedValue.output(value,timestamp:timestamp,window:window)
    }


}