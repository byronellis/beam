public protocol PValueReader<Value> {
    associatedtype Value

    var value: Value { get }
    var timestamp: Int64 { get }
    var window: Window { get }

}
public protocol PValueReadable<Value> {
    associatedtype Value
    associatedtype Reader : PValueReader where Reader.Value == Value
    func makeReader() -> Reader
}
extension PValueReadable where Self : PValueReader {
    public typealias _Default_Reader = Self
}
extension PValueReadable where  Self.Reader == Self {
    @inlinable public func makeReader() -> Self {
        self
    }
}
public protocol PValueWriter<Value> {
    associatedtype Value

    func output(_ value: Value,timestamp: Int64,window: Window)
}

public protocol PValueWritable<Value> {
    associatedtype Value
    associatedtype Writer : PValueReader where Writer.Value == Value

    func makeWriter() -> Writer
}
extension PValueWritable where Self : PValueWriter {
    public typealias _Default_Writer = Self
}
extension PValueWritable where Self.Writer == Self {
    @inlinable public func makeWriter() -> Self {
        self
    }
}

enum PCollection<Value> : PValueReader, PValueReadable, PValueWriter, PValueWritable {
    case done
    case consume(_ receiver: (Value,Int64,Window) -> Void)
    case value(_ value: Value,timestamp: Int64, window: Window)

    var value: Value { 
        get {
            switch self {
                case .done:
                    fatalError()
                case .consume:
                    fatalError()
                case let .value(value,_,_):
                    return value
            }
        }
    }

    var timestamp: Int64 {
        get {
            switch self {
                case .done:
                    fatalError()
                case .consume:
                    fatalError()
                case let .value(_,timestamp,_):
                    return timestamp
            }
        }
    }

    var window: Window {
        get {
            switch self {
                case .done:
                    fatalError()
                case .consume:
                    fatalError()
                case let .value(_,_,window):
                    return window
            }
        }
    }

    func output(_ value: Value, timestamp: Int64, window: Window) {
        switch self {
            case .done:
                fatalError()
            case let .consume(receiver):
                receiver(value,timestamp,window)
            case .value:
                fatalError()
        }
    }

}