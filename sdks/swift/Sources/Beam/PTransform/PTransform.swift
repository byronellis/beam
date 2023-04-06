public enum ProcessContinuation {
    case next
    case waitFor(seconds: Int)
    case waitUntil(timestamp: Int64)
    case done
}
public protocol DoFn<Input,Output> {
    associatedtype Input
    associatedtype Output

    associatedtype Emitter : POutput where Emitter.Element == Output

    func processElement(_ element: Input,output: Emitter) -> ProcessContinuation
}


public protocol PTransform<Element> {
    associatedtype Element
    associatedtype Expansion : POutput where Expansion.Element == Element

    func expand<T>(_ from: T.Type) -> Expansion
}

extension PTransform where Self.Expansion == Self {
    public func expand<T>(_ from: T.Type) -> Self {
        return self
    }
}

public struct ParDo<Fn: DoFn> : POutput {
    public typealias Element = Fn.Output
    internal let _fn: Fn 

    public init(_ fn: Fn) {
        _fn = fn;
    }

    public mutating func emit(_ element: Fn.Output) {
        fatalError() 
    }

}

extension ParDo : PTransform {
}

