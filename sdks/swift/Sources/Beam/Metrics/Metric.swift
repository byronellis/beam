public protocol MetricProtocol {}

@propertyWrapper 
public struct Metric<V> where V : MetricProtocol {
    
    private let initialValue : V
    var anyInitialValue :Any { initialValue }

    var getter: (() -> Any)?

    public init(wrappedValue value: V) {
        initialValue = value
    }
    
    public var wrappedValue : V {
        get { getter?() as? V ?? initialValue }
    }

}


public struct CounterMetric : MetricProtocol {

    public typealias MetricType = MetricRecord


    private let name: String
    private let output: (MetricType,Int64,Window) -> ()
        
    
    public init<Dest: PValueWriter>(_ name: String,destination: Dest) where Dest.Value == MetricType {
        self.name   = "\(name)-count"
        self.output = destination.output
    }

    public func increment(_ value: Int64 = 1) {
        output(.init(name:name,value:value),0,.global)
    }

}
