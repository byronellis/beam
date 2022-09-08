public protocol PInput<Element> {
    associatedtype Element

    var element: Element { mutating get }
}

public protocol POutput<Element> {
    associatedtype Element

    mutating func emit(_ element: Element)
}

typealias PValue = PInput & POutput

public enum PCollection<Element> : PValue {

    case emit(Element)
    case receive((Element) -> ())

    public var element: Element {
        get {
            switch self {
                case .emit(let e):
                    return e
                case .receive:
                    fatalError()
            }
            
        }
    }

    mutating public func emit(_ element: Element) {
        switch self {
            case .emit:
                fatalError()
            case .receive(let fn):
                fn(element)
        }
    }

}

