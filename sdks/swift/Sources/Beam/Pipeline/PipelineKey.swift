public protocol PipelineKey {
    associatedtype Value
    static var defaultValue : Value { get }
}

