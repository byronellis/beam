public struct EmptyPTransform : _PrimitivePTransform {
    @inlinable public init() {}
}

@resultBuilder
public enum PTransformBuilder {
    
    public static func buildBlock() -> EmptyPTransform { EmptyPTransform() }
    
    public static func buildBlock<Transform>(_ transform: Transform) -> Transform where Transform : PTransform {
        transform
    }
}

public extension PTransformBuilder {
    static func buildBlock<T0,T1>(_ t0: T0,_ t1: T1) -> PTransformTuple<(T0,T1)> where T0: PTransform, T1: PTransform {
        PTransformTuple(t0,t1)
    }
    
}