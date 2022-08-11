public protocol PTransform {
    associatedtype Expansion : PValueReadable

    var expansion: Self.Expansion { get }

    static func _makeTransform(_ inputs: PTransformInputs<Self>) -> PTransformOutputs
}

public extension Never {
    var expansion : Never {
        fatalError()
    }
}
extension Never : PTransform, PValueReadable {}


public protocol _PrimitivePTransform : PTransform where Expansion == Never {}
public extension _PrimitivePTransform {
    var expansion : Never {
        neverExpand(String(reflecting: Self.self))
    }
}

public protocol ParentPTransform {
    var children: [AnyPTransform] { get }
}

protocol PTransformGroup : ParentPTransform {}




public func neverExpand(_ type: String) -> Never {
    fatalError("\(type) is a primitive `PTransform` which should not have an expansion")
}

