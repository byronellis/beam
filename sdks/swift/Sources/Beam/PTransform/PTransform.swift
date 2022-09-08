public protocol PTransform<Source,Dest> {
    associatedtype Source
    associatedtype Dest
    associatedtype Output : POutput where Output.Element == Dest

    var output: Output { get }
    func processElement(_ element: Source)
    
}

