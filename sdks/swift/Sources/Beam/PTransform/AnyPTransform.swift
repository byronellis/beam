public struct AnyPTransform : _PrimitivePTransform {
    let type: Any.Type
    let typeConstructorName: String
    var transform: Any
    let expansionClosure: (Any) -> AnyPTransform
    let expansionType: Any.Type
    
    public init<T>(_ transform: T) where T: PTransform {
        if let anyTransform = transform as? AnyPTransform {
            self = anyTransform
        } else {
            type = T.self
            typeConstructorName = "init"
            expansionType = T.Expansion.self
            self.transform = transform
            expansionClosure = { AnyPTransform(($0 as! T).expansion) }
            
        }
    }
}