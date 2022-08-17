import XCTest
@testable import Beam

public struct MyTransform : PTransform {
    
    @PInput
    var input: PCollection<Data>

    @POutput
    var output: PCollection<Int>

    public func process() {
        output.output(input.value.count,timestamp: input.timestamp, window: input.window)
    }

    public var expansion : some PTransform {
        self
    }
}

final class PTransformTests: XCTestCase {
    func testSimpleDoFn() throws {
        _ = MyTransform(input:.constant(Data()),output:.constant())
    }
}
