public protocol PTransform {
    associatedtype Expansion : PTransform

    var expansion: Self.Expansion { get }
    func process()

    static func _makeTransform(_ inputs: PTransformInputs<Self>) -> PTransformOutputs
}

public extension Never {
    var expansion : Never {
        fatalError()
    }
    func process() {
        fatalError()
    }
}
extension Never : PTransform {
}


public protocol _PrimitivePTransform : PTransform where Expansion == Never {}
public extension _PrimitivePTransform {
    var expansion : Never {
        neverExpand(String(reflecting: Self.self))
    }
    func process() {
        neverProcess(String(reflecting: Self.self))
    }
}

public protocol ParentPTransform {
    var children: [AnyPTransform] { get }
}

protocol PTransformGroup : ParentPTransform {}




public func neverExpand(_ type: String) -> Never {
    fatalError("\(type) is a primitive `PTransform` which should not have an expansion")
}

public func neverProcess(_ type: String) {
    fatalError("\(type) is a primitive `PTransform` which should not process elements")
}

