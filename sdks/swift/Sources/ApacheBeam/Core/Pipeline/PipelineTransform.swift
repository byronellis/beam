/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Foundation

/// Enum for pipeline representable transforms as opposed to composite transforms
/// which are a user-side construct represented by PTransform
public enum PipelineTransform {
    case pardo(AnyPCollection,String,SerializableFn,[AnyPCollection])
    case impulse(AnyPCollection,AnyPCollection)
    case flatten([AnyPCollection],AnyPCollection)
    case groupByKey(AnyPCollection,AnyPCollection)
    case custom(AnyPCollection,String,Data,Environment?,[AnyPCollection])
    case composite(AnyPCollection,AnyPTransform)
    case external(AnyPCollection,[AnyPCollection])
}

extension PipelineTransform : PipelineMember {
    var roots: [PCollection<Never>] {
        switch self {
            
        case .pardo(let p, _, _, _):
            return p.roots
        case .impulse(let p, _):
            return p.roots
        case .flatten(let p, _):
            return p.flatMap({ $0.roots })
        case .groupByKey(let p, _):
            return p.roots
        case .custom(let p, _, _, _, _):
            return p.roots
        case .composite(let p, _):
            return p.roots
        case .external(let p, _):
            return p.roots
        }
    }
    
    
}

