public struct PTransformInputs<T> {
    public let transform: T

    public let updateTransform: ((inout T) -> ()) -> ()

    public let pipeline : PipelineBox

}

public struct PTransformOutputs {
    let pipeline : PipelineBox
}

public final class PipelineBox {
    public let pipeline : PipelineValues
    public init(_ pipeline: PipelineValues) {
        self.pipeline = pipeline
    }
}


public extension PTransformOutputs {
    init<T>(inputs: PTransformInputs<T>,pipeline: PipelineValues? = nil) {
        self.pipeline = pipeline.map(PipelineBox.init) ?? inputs.pipeline
    }
}

public extension PTransform {
    static func _makeTransform(_ inputs: PTransformInputs<Self>) -> PTransformOutputs {
        .init(inputs: inputs)
    }
}